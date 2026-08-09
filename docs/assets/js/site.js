(() => {
  const IP = "katherine-hydro.tun.ply.gg:31279";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.18/AquaTech.exe";
  const CANONICAL = "https://aquatech.santcrail.workers.dev";
  const STORAGE_USER = "aquatech_user";
  const API_BASE = "";

  const NAV = [
    { href: "index.html", label: "Главная", id: "home" },
    { href: "start.html", label: "Начать игру", id: "start" },
    { href: "store.html", label: "Магазин", id: "store" },
    { href: "cases.html", label: "Кейсы", id: "cases" },
    { href: "rods.html", label: "Удочки", id: "rods" },
    { href: "top.html", label: "Топы", id: "top" },
    { href: "news.html", label: "Новости", id: "news" },
  ];

  let apiAvailable = null;

  function $(sel, root = document) {
    return root.querySelector(sel);
  }

  function pageId() {
    return document.body.dataset.page || "home";
  }

  function getUser() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_USER) || "null");
    } catch {
      return null;
    }
  }

  function setUser(user) {
    if (!user) localStorage.removeItem(STORAGE_USER);
    else localStorage.setItem(STORAGE_USER, JSON.stringify(user));
  }

  function skinUrl(nick) {
    return `https://mc-heads.net/avatar/${encodeURIComponent(nick)}/64`;
  }

  function toast(msg) {
    let el = $("#toast");
    if (!el) {
      el = document.createElement("div");
      el.id = "toast";
      el.className = "toast";
      document.body.appendChild(el);
    }
    el.textContent = msg;
    el.classList.add("show");
    clearTimeout(toast._t);
    toast._t = setTimeout(() => el.classList.remove("show"), 2600);
  }

  function isMirrorHost() {
    const h = location.hostname || "";
    return h.includes("github.io") || h.includes("jsdelivr.net");
  }

  function isCanonicalHost() {
    const h = location.hostname || "";
    return h.includes("santcrail.workers.dev") || h.includes("pages.dev");
  }

  async function api(path, opts = {}) {
    const url = `${API_BASE}${path}`;
    try {
      const res = await fetch(url, {
        credentials: "include",
        headers: { "content-type": "application/json", ...(opts.headers || {}) },
        ...opts,
      });
      const data = await res.json().catch(() => ({}));
      apiAvailable = true;
      if (!res.ok) {
        const err = new Error(data.error || `HTTP ${res.status}`);
        err.status = res.status;
        err.data = data;
        throw err;
      }
      return data;
    } catch (e) {
      if (e.status) throw e;
      apiAvailable = false;
      throw e;
    }
  }

  function copyIP() {
    navigator.clipboard?.writeText(IP).then(
      () => toast("IP скопирован"),
      () => toast(IP)
    );
  }

  function showApiBanner() {
    /* no player-facing infra banners */
  }

  function lockAuthForms() {
    if (!isMirrorHost()) return;
    ["login-form", "register-form"].forEach((id) => {
      const form = document.getElementById(id);
      if (!form) return;
      form.querySelectorAll("input,button").forEach((el) => {
        el.disabled = true;
      });
      const note = document.createElement("div");
      note.className = "notice-banner inline";
      const page = form.id === "register-form" ? "register.html" : "login.html";
      note.innerHTML = `<a href="${CANONICAL}/${page}">Войти или зарегистрироваться</a>`;
      form.before(note);
    });
  }

  function renderHeader() {
    const mount = $("#site-header");
    if (!mount) return;
    const user = getUser();
    const active = pageId();
    const nav = NAV.map(
      (n) =>
        `<a href="${n.href}" class="${n.id === active ? "active" : ""}">${n.label}</a>`
    ).join("");

    mount.innerHTML = `
      <div class="site-header">
        <div class="container header-inner">
          <a class="brand" href="index.html"><span class="brand-mark"></span>AquaTech</a>
          <nav class="nav-desktop">${nav}</nav>
          <div class="header-spacer"></div>
          <div class="online-pill" title="Онлайн на сервере"><span class="dot"></span><span data-online>—</span> онлайн</div>
          <div class="header-actions">
            ${
              user
                ? `${user.is_admin ? '<a class="btn btn-ghost" href="admin.html">Админка</a>' : ""}
                   <a class="btn btn-secondary" href="profile.html?u=${encodeURIComponent(user.nick)}">${user.nick}</a>
                   <button class="btn btn-ghost" type="button" data-logout>Выйти</button>`
                : `<a class="btn btn-ghost btn-hide-desktop" href="login.html">Войти</a>
                   <a class="btn btn-secondary" href="login.html">Войти</a>
                   <a class="btn btn-primary" href="start.html">Начать игру</a>`
            }
            <button class="menu-btn" type="button" aria-label="Меню" data-menu>
              <span></span><span></span><span></span>
            </button>
          </div>
        </div>
        <div class="mobile-nav" id="mobile-nav">
          <div class="container">
            ${nav}
            <a href="players.html">Поиск игроков</a>
            <a href="rules.html">Правила</a>
            ${user?.is_admin ? '<a href="admin.html">Админка</a>' : ""}
            ${user ? "" : '<a href="register.html">Регистрация</a>'}
          </div>
        </div>
      </div>`;

    $("[data-menu]", mount)?.addEventListener("click", () => {
      $("#mobile-nav", mount)?.classList.toggle("open");
    });
    $("[data-logout]", mount)?.addEventListener("click", async () => {
      try {
        await api("/api/logout", { method: "POST", body: "{}" });
      } catch {
        /* offline / mirror */
      }
      setUser(null);
      location.href = "index.html";
    });
  }

  function renderFooter() {
    const mount = $("#site-footer");
    if (!mount) return;
    mount.innerHTML = `
      <footer class="site-footer">
        <div class="container footer-grid">
          <div>
            <div class="brand" style="margin-bottom:0.8rem"><span class="brand-mark"></span>AquaTech</div>
            <p style="color:var(--muted);margin:0;max-width:28rem">Океанский сервер. Скачай лаунчер и заходи.</p>
          </div>
          <div>
            <h4>Игра</h4>
            <a href="start.html">Скачать лаунчер</a>
            <a href="rods.html">StarCatcher удочки</a>
            <a href="cases.html">Кейсы</a>
            <a href="store.html">Донат</a>
          </div>
          <div>
            <h4>Сообщество</h4>
            <a href="top.html">Топы игроков</a>
            <a href="players.html">Поиск игроков</a>
            <a href="news.html">Новости</a>
            <a href="profile.html">Профили</a>
          </div>
          <div>
            <h4>Проект</h4>
            <a href="rules.html">Правила</a>
            <a href="${DOWNLOAD}">Скачать лаунчер</a>
          </div>
        </div>
        <div class="container footer-copy">© 2026 AquaTech</div>
      </footer>`;
  }

  function wireCommon() {
    document.querySelectorAll("[data-copy-ip]").forEach((el) => {
      el.addEventListener("click", copyIP);
    });
    document.querySelectorAll("[data-download]").forEach((el) => {
      el.setAttribute("href", DOWNLOAD);
    });
    refreshOnlinePill();
    setInterval(refreshOnlinePill, 60000);
  }

  async function refreshOnlinePill() {
    const pills = document.querySelectorAll("[data-online]");
    if (!pills.length) return;
    try {
      const data = await api("/api/server-status");
      const online = !!data.online;
      const n = Number(data.players_online || 0) || 0;
      pills.forEach((el) => {
        el.textContent = String(n);
      });
      document.querySelectorAll(".online-pill").forEach((el) => {
        el.classList.toggle("is-offline", !online);
        el.title = online
          ? `Онлайн на сервере: ${n}${data.players_max ? " / " + data.players_max : ""}`
          : "Сервер сейчас недоступен";
      });
    } catch {
      pills.forEach((el) => {
        el.textContent = "—";
      });
    }
  }

  function playerRows(players, mode) {
    return players
      .map((p, i) => {
        const hours = Number(
          p.playtime_hours != null ? p.playtime_hours : parseInt(String(p.playtime || "0"), 10) || 0
        );
        const playtime = p.playtime || `${hours} ч`;
        const stat =
          mode === "coins"
            ? `${Number(p.coins || 0).toLocaleString("ru-RU")} ¤`
            : mode === "likes"
              ? `${p.likes || 0} ❤`
              : mode === "fish"
                ? `${p.fish || 0} 🐟`
                : playtime;
        return `<a class="top-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
            <div class="rank">${i + 1}</div>
            <img src="${skinUrl(p.nick)}" alt="">
            <div class="meta"><strong>${p.nick}</strong><span>${p.privilege || "Игрок"}</span></div>
            <div class="stat">${stat}</div>
          </a>`;
      })
      .join("");
  }

  async function loadPlayers(sort = "likes", q = "") {
    const qs = new URLSearchParams({ sort, limit: "40" });
    if (q) qs.set("q", q);
    const data = await api(`/api/players?${qs}`);
    return data.players || [];
  }

  function initTop() {
    const root = $("#top-root");
    if (!root) return;
    let mode = "likes";
    const render = async () => {
      root.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      try {
        const players = await loadPlayers(mode === "playtime" ? "playtime" : mode);
        root.innerHTML = playerRows(players, mode) || `<p class="muted-line">Пока нет игроков в базе.</p>`;
      } catch {
        root.innerHTML = `<p class="muted-line">Не удалось загрузить топ. Обнови страницу.</p>`;
      }
    };
    document.querySelectorAll("[data-top-tab]").forEach((btn) => {
      btn.addEventListener("click", () => {
        mode = btn.dataset.topTab;
        document.querySelectorAll("[data-top-tab]").forEach((b) => b.classList.toggle("active", b === btn));
        render();
      });
    });
    render();
  }

  function initPlayers() {
    const input = $("#player-search");
    const list = $("#player-results");
    if (!input || !list) return;
    let t = 0;
    const draw = async () => {
      list.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      try {
        const players = await loadPlayers("likes", input.value.trim());
        list.innerHTML =
          players
            .map(
              (p) => `<a class="top-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
            <div class="rank">·</div>
            <img src="${skinUrl(p.nick)}" alt="">
            <div class="meta"><strong>${p.nick}</strong><span>${p.privilege || "Игрок"} · ${p.playtime || (p.playtime_hours || 0) + " ч"}</span></div>
            <div class="stat">${p.likes || 0} ❤</div>
          </a>`
            )
            .join("") || `<p class="muted-line">Никого не найдено.</p>`;
      } catch {
        list.innerHTML = `<p class="muted-line">Не удалось загрузить список.</p>`;
      }
    };
    input.addEventListener("input", () => {
      clearTimeout(t);
      t = setTimeout(draw, 200);
    });
    draw();
  }

  async function initProfile() {
    const root = $("#profile-root");
    if (!root) return;
    const params = new URLSearchParams(location.search);
    const user = getUser();
    const nick = params.get("u") || user?.nick;
    if (!nick) {
      root.innerHTML = `<div class="panel"><p class="muted-line">Укажи ник в адресе или <a href="login.html">войди</a>.</p></div>`;
      return;
    }

    root.innerHTML = `<p class="muted-line">Загрузка профиля…</p>`;
    let profile = null;
    try {
      const data = await api(`/api/profiles/${encodeURIComponent(nick)}`);
      profile = data.profile;
    } catch {
      profile = null;
    }

    if (!profile) {
      root.innerHTML = `<div class="panel"><p class="muted-line">Игрок не найден.</p></div>`;
      return;
    }

    const mine = user && user.nick.toLowerCase() === profile.nick.toLowerCase();
    const theme = profile.theme || "ocean";
    root.innerHTML = `
      <div class="profile-cover ${theme}">
        <div class="profile-identity">
          <img class="profile-avatar" src="${skinUrl(profile.nick)}" alt="">
          <div>
            <h1>${profile.nick}</h1>
            <p>${profile.privilege || "Игрок"} · ${profile.bio || ""}</p>
          </div>
        </div>
      </div>
      <div class="stats-row" style="margin-top:1rem">
        <div class="stat-card"><strong>${profile.likes || 0}</strong><span>лайки</span></div>
        <div class="stat-card"><strong>${profile.fish || 0}</strong><span>улов</span></div>
        <div class="stat-card"><strong>${Number(profile.coins || 0).toLocaleString("ru-RU")}</strong><span>монеты</span></div>
        <div class="stat-card"><strong>${profile.views || 0}</strong><span>просмотры</span></div>
      </div>
      <div class="panel" style="margin-top:1rem">
        <h3>Бейджи</h3>
        <div class="badge-grid" style="margin-top:.6rem">
          ${(profile.badges || []).map((b) => `<span class="badge">${b}</span>`).join("") || '<span class="muted-line">Пока пусто</span>'}
        </div>
      </div>
      ${
        mine
          ? `<form class="panel form" id="profile-edit" style="margin-top:1rem">
              <h3>Редактировать</h3>
              <div class="field"><label>О себе</label><textarea name="bio" rows="3">${profile.bio || ""}</textarea></div>
              <div class="field"><label>Тема</label>
                <select name="theme">
                  ${["ocean", "deep", "storm", "abyss"]
                    .map((t) => `<option value="${t}" ${t === theme ? "selected" : ""}>${t}</option>`)
                    .join("")}
                </select>
              </div>
              <button class="btn btn-primary" type="submit">Сохранить</button>
            </form>`
          : ""
      }`;

    const form = $("#profile-edit");
    form?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(form);
      try {
        await api(`/api/profiles/${encodeURIComponent(profile.nick)}`, {
          method: "PATCH",
          body: JSON.stringify({ bio: fd.get("bio"), theme: fd.get("theme") }),
        });
        toast("Профиль сохранён");
        location.reload();
      } catch (err) {
        toast(err.message || "Не удалось сохранить");
      }
    });
  }

  function initAuth() {
    const login = $("#login-form");
    const reg = $("#register-form");

    login?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(login);
      const nick = String(fd.get("nick") || "").trim();
      const password = String(fd.get("password") || "");
      try {
        const data = await api("/api/login", {
          method: "POST",
          body: JSON.stringify({ nick, password }),
        });
        setUser(data.user);
        toast("Вход выполнен");
        location.href = `profile.html?u=${encodeURIComponent(data.user.nick)}`;
      } catch (err) {
        if (apiAvailable === false || isMirrorHost()) {
          location.href = `${CANONICAL}/login.html`;
          return;
        }
        toast(err.message || "Ошибка входа");
      }
    });

    reg?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(reg);
      const nick = String(fd.get("nick") || "").trim();
      const password = String(fd.get("password") || "");
      try {
        const data = await api("/api/register", {
          method: "POST",
          body: JSON.stringify({ nick, password }),
        });
        setUser(data.user);
        toast("Аккаунт создан");
        location.href = `profile.html?u=${encodeURIComponent(data.user.nick)}`;
      } catch (err) {
        if (apiAvailable === false || isMirrorHost()) {
          location.href = `${CANONICAL}/register.html`;
          return;
        }
        toast(err.message || "Ошибка регистрации");
      }
    });
  }

  function catalogCard(item, kind) {
    const perks = (item.perks || [])
      .map((p) => `<li>${p}</li>`)
      .join("");
    const price =
      kind === "store"
        ? `<div class="price">${item.price_rub} ₽ <small>/ мес</small></div>`
        : `<div class="price" style="color:var(--muted);font-size:1rem">Только на сервере</div>`;
    const btnLabel = kind === "store" ? "Купить — скоро" : "Открыть — скоро";
    return `<div class="card catalog-card">
      <span class="tag ${item.slug === "deluxe" || item.slug === "ultimate" || item.slug === "depth" ? "gold" : ""}">${item.title}</span>
      <h3>${item.title}</h3>
      <p style="color:var(--muted);margin:.55rem 0 0">${item.description}</p>
      <ul class="perk-list">${perks}</ul>
      ${price}
      <button class="btn btn-secondary btn-disabled" style="margin-top:1rem" type="button" disabled title="Покупки отключены">${btnLabel}</button>
    </div>`;
  }

  const FALLBACK_CATALOG = {
    store: [
      {
        slug: "vip",
        title: "VIP",
        price_rub: 149,
        description: "Префикс, цветной ник, +1 дом. Купить на сайте пока нельзя.",
        perks: ["Префикс VIP в чате", "+1 дом /sethome", "Цветной ник", "Приоритет в очереди"],
      },
      {
        slug: "premium",
        title: "Premium",
        price_rub: 299,
        description: "Всё из VIP, кейс в день на сервере, приоритет входа.",
        perks: ["Всё из VIP", "Кейс в день (в игре)", "Приоритет входа", "Доп. слот варпа"],
      },
      {
        slug: "deluxe",
        title: "Deluxe",
        price_rub: 599,
        description: "Бонус к улову и рамка профиля. Оплата на сайте выключена.",
        perks: ["Всё из Premium", "Рамка профиля", "Бонус к улову", "Бейдж Deluxe"],
      },
      {
        slug: "ultimate",
        title: "Ultimate",
        price_rub: 1199,
        description: "Максимум привилегий на сервере. Оплата на сайте позже.",
        perks: ["Всё из Deluxe", "Бейдж Ultimate", "Максимум домов", "Приоритет в поддержке"],
      },
    ],
    case: [
      {
        slug: "ocean",
        title: "Океанский кейс",
        price_rub: 0,
        description: "Монеты и расходники. Открывается в игре (F4).",
        perks: ["AquaCoins", "Расходники", "Мелкий буст"],
      },
      {
        slug: "fisher",
        title: "Кейс рыбака",
        price_rub: 0,
        description: "Лут под StarCatcher. Рулетки на сайте нет.",
        perks: ["Ресурсы улова", "Буст удочки", "Монеты"],
      },
      {
        slug: "depth",
        title: "Глубинный кейс",
        price_rub: 0,
        description: "Редкая косметика и пробные привилегии. Только сервер.",
        perks: ["Рамка профиля", "Пробная привилегия", "Крупный запас монет"],
      },
    ],
  };

  async function initCatalog(kind) {
    const root = kind === "store" ? $("#store-root") : $("#cases-root");
    if (!root) return;
    root.innerHTML = `<p class="muted-line">Загрузка каталога…</p>`;
    let items = [];
    try {
      const data = await api(`/api/catalog?kind=${kind}`);
      items = data.items || [];
    } catch {
      items = FALLBACK_CATALOG[kind] || [];
    }
    root.innerHTML = items.map((it) => catalogCard(it, kind)).join("");
    root.querySelectorAll("button[disabled]").forEach((btn) => {
      btn.addEventListener("click", (e) => {
        e.preventDefault();
        toast("Покупки выключены");
      });
    });
  }

  function initReveal() {
    const nodes = document.querySelectorAll(".reveal");
    if (!nodes.length) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      nodes.forEach((n) => n.classList.add("in"));
      return;
    }
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            e.target.classList.add("in");
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -40px 0px" }
    );
    nodes.forEach((n) => io.observe(n));
    document.querySelectorAll(".hero .reveal").forEach((n) => n.classList.add("in"));
  }

  async function refreshSession() {
    try {
      const data = await api("/api/me");
      if (data.user) setUser(data.user);
    } catch {
      /* not logged in or no API */
    }
  }

  function esc(s) {
    return String(s ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  async function initAdmin() {
    if (pageId() !== "admin") return;
    const gate = $("#admin-gate");
    const root = $("#admin-root");
    if (!gate || !root) return;

    try {
      await api("/api/admin/me");
    } catch (err) {
      gate.innerHTML =
        err.status === 401 || !getUser()
          ? `Нужен <a href="login.html">вход</a> под админ-ником.`
          : "Нет доступа к админке.";
      return;
    }

    gate.textContent = "Доступ есть. Правь каталог и игроков ниже.";
    root.hidden = false;

    const purchases = $("#admin-purchases");
    try {
      const st = await api("/api/admin/settings");
      if (purchases) purchases.checked = !!st.settings?.purchases_enabled;
    } catch {
      /* settings optional */
    }

    $("#admin-save-settings")?.addEventListener("click", async () => {
      try {
        await api("/api/admin/settings", {
          method: "PATCH",
          body: JSON.stringify({ purchases_enabled: !!purchases?.checked }),
        });
        toast("Настройки сохранены");
      } catch (err) {
        toast(err.message || "Не удалось сохранить");
      }
    });

    async function loadUsers() {
      const box = $("#admin-users");
      if (!box) return;
      const q = ($("#admin-user-q")?.value || "").trim();
      box.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      try {
        const data = await api(`/api/admin/users?q=${encodeURIComponent(q)}`);
        const rows = data.users || [];
        if (!rows.length) {
          box.innerHTML = `<p class="muted-line">Никого не нашли.</p>`;
          return;
        }
        box.innerHTML = `<table class="admin-table"><thead><tr>
          <th>Ник</th><th>Ранг</th><th>Монеты</th><th>Лайки</th><th>Рыба</th><th>Часы</th><th></th>
        </tr></thead><tbody>
        ${rows
          .map(
            (u) => `<tr data-nick="${esc(u.nick)}">
          <td><strong>${esc(u.nick)}</strong>${u.is_admin ? ' <span class="tag">admin</span>' : ""}</td>
          <td><input data-f="privilege" value="${esc(u.privilege)}" /></td>
          <td><input data-f="coins" type="number" min="0" value="${esc(u.coins)}" /></td>
          <td><input data-f="likes" type="number" min="0" value="${esc(u.likes)}" /></td>
          <td><input data-f="fish" type="number" min="0" value="${esc(u.fish)}" /></td>
          <td><input data-f="playtime_hours" type="number" min="0" value="${esc(u.playtime_hours)}" /></td>
          <td><button class="btn btn-secondary" type="button" data-save-user>OK</button></td>
        </tr>`
          )
          .join("")}
        </tbody></table>`;
        box.querySelectorAll("[data-save-user]").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const tr = btn.closest("tr");
            const nick = tr?.dataset.nick;
            if (!nick) return;
            const body = {};
            tr.querySelectorAll("[data-f]").forEach((inp) => {
              const key = inp.getAttribute("data-f");
              body[key] = inp.type === "number" ? Number(inp.value) : inp.value;
            });
            try {
              await api(`/api/admin/users/${encodeURIComponent(nick)}`, {
                method: "PATCH",
                body: JSON.stringify(body),
              });
              toast(`Сохранено: ${nick}`);
            } catch (err) {
              toast(err.message || "Ошибка");
            }
          });
        });
      } catch (err) {
        box.innerHTML = `<p class="muted-line">${esc(err.message || "Ошибка загрузки")}</p>`;
      }
    }

    async function loadCatalog() {
      const box = $("#admin-catalog");
      if (!box) return;
      box.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      try {
        const data = await api("/api/admin/catalog");
        const rows = data.items || [];
        box.innerHTML = `<table class="admin-table"><thead><tr>
          <th>Slug</th><th>Название</th><th>Цена</th><th>Описание</th><th>Perks (\\n)</th><th>Вкл</th><th></th>
        </tr></thead><tbody>
        ${rows
          .map(
            (it) => `<tr data-id="${it.id}">
          <td>${esc(it.slug)}<div class="muted-line">${esc(it.kind)}</div></td>
          <td><input data-f="title" value="${esc(it.title)}" /></td>
          <td><input data-f="price_rub" type="number" min="0" value="${esc(it.price_rub)}" /></td>
          <td><textarea data-f="description" rows="3">${esc(it.description)}</textarea></td>
          <td><textarea data-f="perks" rows="3">${esc((it.perks || []).join("\n"))}</textarea></td>
          <td><input data-f="enabled" type="checkbox" ${it.enabled ? "checked" : ""} /></td>
          <td><button class="btn btn-secondary" type="button" data-save-item>OK</button></td>
        </tr>`
          )
          .join("")}
        </tbody></table>`;
        box.querySelectorAll("[data-save-item]").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const tr = btn.closest("tr");
            const id = tr?.dataset.id;
            if (!id) return;
            const body = {};
            tr.querySelectorAll("[data-f]").forEach((inp) => {
              const key = inp.getAttribute("data-f");
              if (key === "enabled") body.enabled = inp.checked;
              else if (key === "perks")
                body.perks = String(inp.value)
                  .split(/\r?\n/)
                  .map((s) => s.trim())
                  .filter(Boolean);
              else if (key === "price_rub") body.price_rub = Number(inp.value);
              else body[key] = inp.value;
            });
            try {
              await api(`/api/admin/catalog/${id}`, {
                method: "PATCH",
                body: JSON.stringify(body),
              });
              toast("Каталог сохранён");
            } catch (err) {
              toast(err.message || "Ошибка");
            }
          });
        });
      } catch (err) {
        box.innerHTML = `<p class="muted-line">${esc(err.message || "Ошибка загрузки")}</p>`;
      }
    }

    let userTimer;
    $("#admin-user-q")?.addEventListener("input", () => {
      clearTimeout(userTimer);
      userTimer = setTimeout(loadUsers, 280);
    });
    $("#admin-short-copy")?.addEventListener("click", async () => {
      try {
        await api("/api/admin/catalog", {
          method: "POST",
          body: JSON.stringify({ action: "short_copy" }),
        });
        toast("Короткие тексты записаны");
        await loadCatalog();
      } catch (err) {
        toast(err.message || "Ошибка");
      }
    });

    await loadUsers();
    await loadCatalog();
  }

  document.addEventListener("DOMContentLoaded", async () => {
    showApiBanner();
    lockAuthForms();
    if (isCanonicalHost()) await refreshSession();
    renderHeader();
    renderFooter();
    wireCommon();
    initTop();
    initPlayers();
    initProfile();
    initAuth();
    initCatalog("store");
    initCatalog("case");
    await initAdmin();
    initReveal();
  });

  window.AquaTechSite = { IP, DOWNLOAD, CANONICAL, toast, copyIP, api };
})();
