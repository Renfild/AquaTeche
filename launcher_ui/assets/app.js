(() => {
  const $ = (id) => document.getElementById(id);
  let afterId = 0;
  let busy = false;
  let authed = false;

  const pages = {
    auth: $("page-auth"),
    play: $("page-play"),
    settings: $("page-settings"),
    log: $("page-log"),
  };

  function showPage(name) {
    Object.entries(pages).forEach(([k, el]) => el?.classList.toggle("active", k === name));
    document.querySelectorAll(".nav-btn").forEach((b) => {
      b.classList.toggle("active", b.dataset.page === name);
    });
  }

  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.addEventListener("click", () => showPage(btn.dataset.page));
  });

  function applyCfg(cfg) {
    if (!cfg) return;
    $("nick").value = cfg.username || "";
    if ($("game-dir")) $("game-dir").value = cfg.game_dir || "";
    $("ram").value = cfg.ram_mb || 4096;
    if ($("auto-connect")) $("auto-connect").checked = cfg.auto_connect !== false;
    if ($("auth-nick") && cfg.username) $("auth-nick").value = cfg.username;
  }

  function cfgPayload() {
    return {
      username: $("nick").value.trim(),
      game_dir: $("game-dir") ? $("game-dir").value.trim() : "",
      auto_connect: $("auto-connect") ? $("auto-connect").checked : true,
      ram_mb: Number($("ram").value) || 4096,
    };
  }

  function appendLogs(logs) {
    if (!logs || !logs.length) return;
    const box = $("log");
    const stick = box.scrollTop + box.clientHeight >= box.scrollHeight - 40;
    for (const e of logs) {
      afterId = Math.max(afterId, e.id);
      const div = document.createElement("div");
      div.className = "line";
      div.innerHTML = `<span class="t">${e.t}</span><span class="${e.tag || "info"}"></span>`;
      div.querySelector("span:last-child").textContent = e.text;
      box.appendChild(div);
    }
    if (stick) box.scrollTop = box.scrollHeight;
  }

  function setBusy(on, playLabel) {
    busy = on;
    $("btn-play").disabled = on;
    $("btn-update").disabled = on;
    if (playLabel) $("btn-play").textContent = playLabel;
    else if (!on) $("btn-play").textContent = authed ? "Играть" : "Вход нужен";
  }

  function paintAccount(cfg) {
    const wrap = $("side-account");
    const nickEl = $("account-nick");
    if (!wrap || !nickEl) return;
    if (authed) {
      wrap.classList.remove("hidden");
      nickEl.textContent = cfg?.username || "игрок";
    } else {
      wrap.classList.add("hidden");
      nickEl.textContent = "—";
    }
  }

  function setAuthed(isAuthed, cfg) {
    authed = isAuthed;
    paintAccount(cfg);
    const lock = !isAuthed;
    if (lock && !busy) {
      $("btn-play").disabled = true;
      $("btn-update").disabled = true;
      $("btn-play").textContent = "Вход нужен";
    } else if (!lock && !busy) {
      $("btn-play").disabled = false;
      $("btn-update").disabled = false;
      $("btn-play").textContent = "Играть";
    }
  }

  function paintPackBanner(pack) {
    const el = $("update-banner");
    if (!el || !pack) return;
    if (pack.update_available && pack.remote) {
      el.classList.remove("hidden");
      const local = pack.local ? ` (сейчас ${pack.local})` : "";
      $("update-banner-text").textContent = `Доступно обновление сборки ${pack.remote}${local}`;
      $("btn-update").classList.add("highlight");
    } else {
      el.classList.add("hidden");
      $("btn-update").classList.remove("highlight");
    }
  }

  function paint(st) {
    document.body.classList.remove("state-ingame", "state-error");
    if (st.state === "ingame") document.body.classList.add("state-ingame");
    if (st.state === "error") document.body.classList.add("state-error");

    $("ver").textContent = "v" + (st.version || "?");
    paintPackBanner(st.pack);
    $("status").textContent = st.status || "Готов";
    const pct = Math.round(st.progress || 0);
    $("pct").textContent = pct + "%";
    $("bar").style.width = pct + "%";

    const sessionOk = Boolean(st?.cfg?.portal_session);
    if (sessionOk !== authed) {
      setAuthed(sessionOk, st.cfg);
      if (!sessionOk && !busy) showPage("auth");
    } else {
      paintAccount(st.cfg);
    }

    if (st.state === "busy") {
      setBusy(true, "Подготовка…");
    } else if (st.state === "ingame") {
      busy = false;
      setBusy(false);
      $("btn-play").textContent = "В игре";
    } else if (st.state === "error") {
      busy = false;
      setBusy(false);
      $("btn-play").textContent = "Ошибка — ещё раз";
    } else {
      busy = false;
      setBusy(false);
    }

    appendLogs(st.logs);
  }

  async function refreshStatus() {
    const r = await fetch("/api/status");
    return r.json();
  }

  async function ensureAuthed(cfg) {
    if (!cfg?.portal_session) return false;
    try {
      const r = await fetch("/api/portal_validate", { method: "POST" });
      const j = await r.json();
      return Boolean(j.ok);
    } catch (_) {
      return false;
    }
  }

  async function poll() {
    try {
      const r = await fetch("/api/status?after=" + afterId);
      paint(await r.json());
    } catch (_) { /* ignore */ }
    setTimeout(poll, 400);
  }

  async function afterLogin(st) {
    setAuthed(true, st.cfg);
    showPage("play");
    applyCfg(st.cfg);
    paint(st);
    setBusy(false);
  }

  async function boot() {
    try {
      let st = await refreshStatus();
      let ok = await ensureAuthed(st.cfg);
      if (st.cfg?.portal_session && !ok) {
        await fetch("/api/portal_logout", { method: "POST" });
        st = await refreshStatus();
      }
      authed = Boolean(st.cfg?.portal_session);
      setAuthed(authed, st.cfg);
      showPage(authed ? "play" : "auth");
      applyCfg(st.cfg);
      paint(st);
    } catch (_) {
      authed = false;
      setAuthed(false, {});
      showPage("auth");
    }
    poll();
  }

  $("btn-play").addEventListener("click", async () => {
    if (!authed) {
      showPage("auth");
      return;
    }
    showPage("log");
    setBusy(true, "Подготовка…");
    const r = await fetch("/api/play", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cfgPayload()),
    });
    const j = await r.json();
    if (!j.ok) {
      setBusy(false);
      alert(j.message || "Ошибка");
    }
  });

  $("btn-update").addEventListener("click", async () => {
    if (!authed) {
      showPage("auth");
      return;
    }
    showPage("log");
    setBusy(true, "Обновление…");
    const r = await fetch("/api/update", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cfgPayload()),
    });
    const j = await r.json();
    if (!j.ok) {
      setBusy(false);
      alert(j.message || "Ошибка");
    }
  });

  $("btn-update-banner")?.addEventListener("click", () => $("btn-update").click());

  $("btn-auth")?.addEventListener("click", async () => {
    const nick = $("auth-nick")?.value.trim() || "";
    const password = $("auth-pass")?.value || "";
    $("auth-error").textContent = "";
    if (!nick || !password) {
      $("auth-error").textContent = "Введи ник и пароль.";
      return;
    }

    setBusy(true, "Вход…");
    try {
      const r = await fetch("/api/portal_login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nick, password }),
      });
      const j = await r.json();
      if (!j.ok) {
        $("auth-error").textContent = j.message || "Ошибка входа";
        setBusy(false);
        return;
      }

      $("auth-pass").value = "";
      await afterLogin(await refreshStatus());
    } catch (_) {
      $("auth-error").textContent = "Не удалось выполнить вход.";
      setBusy(false);
    }
  });

  $("btn-auth-browser")?.addEventListener("click", async () => {
    $("auth-error").textContent = "";
    try {
      const r = await fetch("/api/portal_browser", { method: "POST" });
      const j = await r.json();
      if (!j.ok) {
        $("auth-error").textContent = j.message || "Не удалось открыть браузер";
        return;
      }
      $("auth-error").textContent = "Войди на сайте — лаунчер подхватит сессию.";
      const started = Date.now();
      const tick = async () => {
        const st = await refreshStatus();
        if (st.cfg?.portal_session) {
          const ok = await ensureAuthed(st.cfg);
          if (ok) {
            await afterLogin(st);
            $("auth-error").textContent = "";
            return;
          }
        }
        if (Date.now() - started < 120000) setTimeout(tick, 800);
      };
      tick();
    } catch (_) {
      $("auth-error").textContent = "Не удалось открыть браузер.";
    }
  });

  $("btn-logout")?.addEventListener("click", async () => {
    await fetch("/api/portal_logout", { method: "POST" });
    authed = false;
    setAuthed(false, {});
    showPage("auth");
    $("auth-pass").value = "";
    $("status").textContent = "Выход выполнен";
  });

  if ($("btn-browse-dir")) {
    $("btn-browse-dir").addEventListener("click", async () => {
      try {
        const r = await fetch("/api/browse_dir", { method: "POST" });
        const j = await r.json();
        if (j.ok && j.dir) $("game-dir").value = j.dir;
      } catch (_) {}
    });
  }

  $("btn-save").addEventListener("click", async () => {
    await fetch("/api/config", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cfgPayload()),
    });
    $("status").textContent = "Настройки сохранены";
  });

  boot();
})();
