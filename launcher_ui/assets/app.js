(() => {
  const $ = (id) => document.getElementById(id);
  let afterId = 0;
  let busy = false;

  const pages = {
    play: $("page-play"),
    settings: $("page-settings"),
    log: $("page-log"),
  };

  function showPage(name) {
    Object.entries(pages).forEach(([k, el]) => el.classList.toggle("active", k === name));
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
    $("game-dir").value = cfg.game_dir || "";
    $("update-url").value = cfg.update_url || "";
    $("ram").value = cfg.ram_mb || 4096;
  }

  function cfgPayload() {
    return {
      username: $("nick").value.trim(),
      game_dir: $("game-dir").value.trim(),
      update_url: $("update-url").value.trim(),
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
    else if (!on) $("btn-play").textContent = "Играть";
  }

  function paint(st) {
    document.body.classList.remove("state-ingame", "state-error");
    if (st.state === "ingame") document.body.classList.add("state-ingame");
    if (st.state === "error") document.body.classList.add("state-error");

    $("ver").textContent = "v" + (st.version || "?");
    $("server").textContent = st.server || "";
    $("status").textContent = st.status || "Готов";
    const pct = Math.round(st.progress || 0);
    $("pct").textContent = pct + "%";
    $("bar").style.width = pct + "%";

    if (st.state === "busy") {
      setBusy(true, "Подготовка…");
    } else if (st.state === "ingame") {
      setBusy(false);
      $("btn-play").textContent = "В игре";
    } else if (st.state === "error") {
      setBusy(false);
      $("btn-play").textContent = "Ошибка — ещё раз";
    } else if (!busy) {
      setBusy(false);
    }

    appendLogs(st.logs);
  }

  async function poll() {
    try {
      const r = await fetch("/api/status?after=" + afterId);
      const st = await r.json();
      paint(st);
    } catch (_) { /* ignore */ }
    setTimeout(poll, 400);
  }

  async function boot() {
    const r = await fetch("/api/status");
    const st = await r.json();
    applyCfg(st.cfg);
    paint(st);
    poll();
  }

  $("btn-play").addEventListener("click", async () => {
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

  $("btn-save").addEventListener("click", async () => {
    await fetch("/api/config", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cfgPayload()),
    });
    $("status").textContent = "Настройки сохранены";
  });

  if ($("btn-browse-dir")) {
    $("btn-browse-dir").addEventListener("click", async () => {
      try {
        const r = await fetch("/api/browse_dir", { method: "POST" });
        const j = await r.json();
        if (j.ok && j.dir) {
          $("game-dir").value = j.dir;
        }
      } catch (_) {}
    });
  }

  boot();
})();
