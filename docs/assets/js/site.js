(() => {
  const IP = "katherine-hydro.tun.ply.gg:31279";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.9/AquaTech.exe";
  /** Primary site: workers.dev (pages.dev is blocked in some networks, e.g. BY). */
  const CANONICAL = "https://aquatech.santcrail.workers.dev";
  const PAGES_MIRROR = "https://aquatech-7gs.pages.dev";
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

  const DEMO_PLAYERS = [
    { nick: "WebTest", privilege: "Deluxe", playtime: "128 ч", playtime_hours: 128, coins: 4200, likes: 86, fish: 1840 },
    { nick: "OceanKing", privilege: "Ultimate", playtime: "210 ч", playtime_hours: 210, coins: 9800, likes: 214, fish: 4021 },
    { nick: "StarCatcher", privilege: "Premium", playtime: "96 ч", playtime_hours: 96, coins: 2100, likes: 61, fish: 990 },
    { nick: "HydroForge", privilege: "VIP", playtime: "74 ч", playtime_hours: 74, coins: 900, likes: 33, fish: 640 },
    { nick: "DepthWalker", privilege: "Игрок", playtime: "58 ч", playtime_hours: 58, coins: 350, likes: 18, fish: 410 },
    { nick: "Renfild", privilege: "Ultimate", playtime: "301 ч", playtime_hours: 301, coins: 15000, likes: 502, fish: 8120 },
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
    if ($("#api-mirror-banner")) return;
    if (isMirrorHost()) {
      const el = document.createElement("div");
      el.id = "api-mirror-banner";
      el.className = "notice-banner";
      el.innerHTML = `Зеркало только для чтения. Регистрация, вход и профили: <a href="${CANONICAL}/">aquatech.santcrail.workers.dev</a>`;
      document.body.prepend(el);
      return;
    }
    if (location.hostname.includes("pages.dev")) {
      const el = document.createElement("div");
      el.id = "api-mirror-banner";
      el.className = "notice-banner";
      el.innerHTML = `Если сайт не открывается у друзей — используй <a href="${CANONICAL}/">workers.dev</a> (pages.dev часто режется провайдером).`;
      document.body.prepend(el);
    }
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
      note.innerHTML = `На этом зеркале аккаунты не работают. Открой <a href="${CANONICAL}/${form.id === "register-form" ? "register.html" : "login.html"}">основной сайт</a>.`;
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
          <div class="online-pill" title="Онлайн на сервере"><span class="dot"></span><span data-online>42</span> онлайн</div>
          <div class="header-actions">
            ${
              user
                ? `<a class="btn btn-secondary" href="profile.html?u=${encodeURIComponent(user.nick)}">${user.nick}</a>
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
            <p style="color:var(--muted);margin:0;max-width:28rem">Океанский модпак Minecraft 1.20.1 Forge + Mohist. Лаунчер, сборка и сервер — в одном проекте.</p>
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
            <a href="${CANONICAL}/">Основной сайт</a>
            <a href="https://renfild.github.io/AquaTeche/">Зеркало (GH Pages)</a>
            <a href="${PAGES_MIRROR}/">Pages.dev</a>
            <a href="${DOWNLOAD}">AquaTech.exe</a>
          </div>
        </div>
        <div class="container footer-copy">© 2026 AquaTech · Minecraft Forge 1.20.1 + Mohist</div>
      </footer>`;
  }

  function wireCommon() {
    document.querySelectorAll("[data-copy-ip]").forEach((el) => {
      el.addEventListener("click", copyIP);
    });
    document.querySelectorAll("[data-download]").forEach((el) => {
      el.setAttribute("href", DOWNLOAD);
    });
    const online = Math.floor(28 + Math.random() * 40);
    document.querySelectorAll("[data-online]").forEach((el) => {
      el.textContent = String(online);
    });
  }

  function playerRows(players, mode) {
    return players
      .map((p, i) => {
        const hours = p.playtime_hours ?? parseInt(String(p.playtime || "0"), 10) || 0;
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
    try {
      const qs = new URLSearchParams({ sort, limit: "40" });
      if (q) qs.set("q", q);
      const data = await api(`/api/players?${qs}`);
      return data.players || [];
    } catch {
      let list = [...DEMO_PLAYERS];
      if (q) {
        const qq = q.toLowerCase();
        list = list.filter((p) => p.nick.toLowerCase().includes(qq));
      }
      list.sort((a, b) => {
        if (sort === "coins") return b.coins - a.coins;
        if (sort === "fish") return b.fish - a.fish;
        if (sort === "playtime") return b.playtime_hours - a.playtime_hours;
        return b.likes - a.likes;
      });
      return list;
    }
  }

  function initTop() {
    const root = $("#top-root");
    if (!root) return;
    let mode = "likes";
    const render = async () => {
      root.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      const players = await loadPlayers(mode === "playtime" ? "playtime" : mode);
      root.innerHTML = playerRows(players, mode) || `<p class="muted-line">Пока нет игроков в базе.</p>`;
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
      const demo = DEMO_PLAYERS.find((p) => p.nick.toLowerCase() === nick.toLowerCase());
      profile = demo
        ? {
            nick: demo.nick,
            bio: "Демо-профиль (API недоступен).",
            theme: "ocean",
            privilege: demo.privilege,
            coins: demo.coins,
            likes: demo.likes,
            fish: demo.fish,
            playtime: demo.playtime,
            views: 0,
            badges: ["Демо"],
          }
        : null;
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
          toast("Открой основной сайт для входа");
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
          toast("Открой основной сайт для регистрации");
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
        description:
          "Базовая поддержка сервера. Префикс и удобства на AquaTech; покупка на сайте временно закрыта.",
        perks: ["Префикс VIP в чате", "+1 дом /sethome", "Цветной ник", "Приоритет в очереди входа"],
      },
      {
        slug: "premium",
        title: "Premium",
        price_rub: 299,
        description: "Всё из VIP плюс ежедневный кейс на сервере. Оплата на сайте пока недоступна.",
        perks: ["Всё из VIP", "Кейс в день (на сервере)", "Приоритет входа", "Доп. слот варпа"],
      },
      {
        slug: "deluxe",
        title: "Deluxe",
        price_rub: 599,
        description: "Бонусы к улову и рамка профиля. Покупка через сайт отключена до оплаты.",
        perks: ["Всё из Premium", "Рамка профиля", "Бонус к улову", "Бейдж Deluxe"],
      },
      {
        slug: "ultimate",
        title: "Ultimate",
        price_rub: 1199,
        description: "Максимальный ранг. Онлайн-оплата будет позже — сейчас только витрина.",
        perks: ["Всё из Deluxe", "Бейдж Ultimate", "Максимум домов", "Приоритетная поддержка"],
      },
    ],
    case: [
      {
        slug: "ocean",
        title: "Океанский кейс",
        price_rub: 0,
        description: "Базовый кейс с монетами. Открытие на сайте отключено — крутится в игре (F4 / casesmod).",
        perks: ["AquaCoins", "Расходники", "Шанс на мелкий буст"],
      },
      {
        slug: "fisher",
        title: "Кейс рыбака",
        price_rub: 0,
        description: "Награды под рыбалку StarCatcher. Сайтовая рулетка выключена.",
        perks: ["Ресурсы улова", "Шанс на буст удочки", "Монеты"],
      },
      {
        slug: "depth",
        title: "Глубинный кейс",
        price_rub: 0,
        description: "Редкая косметика и пробные привилегии. Только на сервере.",
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
        toast("Покупки временно отключены");
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
    initReveal();
  });

  window.AquaTechSite = { IP, DOWNLOAD, CANONICAL, toast, copyIP, api };
})();
