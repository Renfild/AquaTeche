(() => {
  const IP = "g-pl-3.apexnodes.xyz:21561";
  const DOWNLOAD = "/dl/AquaTech.exe";
  const DOWNLOAD_ZIP = "/dl/AquaTechLauncher.zip";
  const CLIENT_VERSION = "client-2.9.91";
  /* portal ui build: compact header + market lots */
  const CANONICAL = "https://aquateche.store";
  const DISCORD = "https://discord.gg/3Khzr5z4fQ";
  const STORAGE_USER = "aquatech_user";
  const STORAGE_SOUND = "aquatech_sound";
  const API_BASE = "";
  const VISA_MIN_RUB = 450;
  let headerOutsideClick = null;

  const NAV_PRIMARY = [
    { href: "guide.html", label: "Гайд", id: "guide" },
    { href: "store.html", label: "Магазин", id: "store" },
    { href: "cases.html", label: "Кейсы", id: "cases" },
    { href: "rods.html", label: "Удочки", id: "rods" },
  ];
  const NAV_MORE = [
    { href: "market.html", label: "Аукцион", id: "market" },
    { href: "top.html", label: "Топы", id: "top" },
    { href: "news.html", label: "Новости", id: "news" },
    { href: "events.html", label: "События", id: "events" },
    { href: "players.html", label: "Игроки", id: "players" },
    { href: "start.html", label: "Как начать", id: "start" },
    { href: "rules.html", label: "Правила", id: "rules" },
  ];

  const COPY_FIELDS = [
    { group: "Главная", key: "hero_eyebrow", label: "Hero · eyebrow", long: false },
    { group: "Главная", key: "hero_title", label: "Hero · заголовок", long: false },
    { group: "Главная", key: "hero_lead", label: "Hero · текст", long: true },
    { group: "Главная", key: "features_title", label: "Секция · заголовок", long: false },
    { group: "Главная", key: "features_lead", label: "Секция · подзаголовок", long: false },
    { group: "Главная", key: "tile_rods_tag", label: "Плитка удочки · тег", long: false },
    { group: "Главная", key: "tile_rods_title", label: "Плитка удочки · заголовок", long: false },
    { group: "Главная", key: "tile_rods_body", label: "Плитка удочки · текст", long: true },
    { group: "Главная", key: "tile_cases_tag", label: "Плитка кейсы · тег", long: false },
    { group: "Главная", key: "tile_cases_title", label: "Плитка кейсы · заголовок", long: false },
    { group: "Главная", key: "tile_cases_body", label: "Плитка кейсы · текст", long: true },
    { group: "Главная", key: "tile_top_tag", label: "Плитка топы · тег", long: false },
    { group: "Главная", key: "tile_top_title", label: "Плитка топы · заголовок", long: false },
    { group: "Главная", key: "tile_top_body", label: "Плитка топы · текст", long: true },
    { group: "Главная", key: "home_news_title", label: "Новости дома · заголовок", long: false },
    { group: "Главная", key: "home_news_lead", label: "Новости дома · lead", long: false },
    { group: "Главная", key: "join_title", label: "Join · заголовок", long: false },
    { group: "Главная", key: "join_body", label: "Join · текст", long: true },
    { group: "Главная", key: "footer_blurb", label: "Футер", long: true },
    { group: "Старт", key: "start_eyebrow", label: "Eyebrow", long: false },
    { group: "Старт", key: "start_title", label: "Заголовок", long: false },
    { group: "Старт", key: "start_lead", label: "Lead", long: true },
    { group: "Старт", key: "start_step1_title", label: "Шаг 1 · заголовок", long: false },
    { group: "Старт", key: "start_step1_body", label: "Шаг 1 · текст", long: true },
    { group: "Старт", key: "start_step2_title", label: "Шаг 2 · заголовок", long: false },
    { group: "Старт", key: "start_step2_1", label: "Шаг 2 · пункт 1", long: false },
    { group: "Старт", key: "start_step2_2", label: "Шаг 2 · пункт 2", long: false },
    { group: "Старт", key: "start_step2_3", label: "Шаг 2 · пункт 3", long: false },
    { group: "Старт", key: "start_step2_4", label: "Шаг 2 · пункт 4", long: false },
    { group: "Магазин", key: "store_eyebrow", label: "Eyebrow", long: false },
    { group: "Магазин", key: "store_title", label: "Заголовок", long: false },
    { group: "Магазин", key: "store_lead", label: "Lead", long: true },
    { group: "Магазин", key: "store_notice", label: "Баннер", long: true },
    { group: "Кейсы", key: "cases_eyebrow", label: "Eyebrow", long: false },
    { group: "Кейсы", key: "cases_title", label: "Заголовок", long: false },
    { group: "Кейсы", key: "cases_lead", label: "Lead", long: true },
    { group: "Кейсы", key: "cases_notice", label: "Баннер", long: true },
    { group: "Удочки", key: "rods_eyebrow", label: "Eyebrow", long: false },
    { group: "Удочки", key: "rods_title", label: "Заголовок", long: false },
    { group: "Удочки", key: "rods_lead", label: "Lead", long: true },
    { group: "Удочки", key: "rods_rules_title", label: "Правила улова · заголовок", long: false },
    { group: "Удочки", key: "rods_rule_1", label: "Правило 1", long: true },
    { group: "Удочки", key: "rods_rule_2", label: "Правило 2", long: true },
    { group: "Удочки", key: "rods_rule_3", label: "Правило 3", long: true },
    { group: "Удочки", key: "rods_rule_4", label: "Правило 4", long: true },
    { group: "Топы", key: "top_eyebrow", label: "Eyebrow", long: false },
    { group: "Топы", key: "top_title", label: "Заголовок", long: false },
    { group: "Топы", key: "top_lead", label: "Lead", long: true },
    { group: "Новости", key: "news_eyebrow", label: "Eyebrow", long: false },
    { group: "Новости", key: "news_title", label: "Заголовок", long: false },
    { group: "Новости", key: "news_page_lead", label: "Lead", long: true },
    { group: "Профиль", key: "profile_eyebrow", label: "Eyebrow", long: false },
    { group: "Профиль", key: "profile_title", label: "Заголовок", long: false },
    { group: "Профиль", key: "profile_lead", label: "Lead", long: true },
    { group: "Вход", key: "login_eyebrow", label: "Eyebrow", long: false },
    { group: "Вход", key: "login_title", label: "Заголовок", long: false },
    { group: "Вход", key: "login_lead", label: "Lead", long: false },
    { group: "Регистрация", key: "register_eyebrow", label: "Eyebrow", long: false },
    { group: "Регистрация", key: "register_title", label: "Заголовок", long: false },
    { group: "Регистрация", key: "register_lead", label: "Lead", long: false },
    { group: "Поиск", key: "players_eyebrow", label: "Eyebrow", long: false },
    { group: "Поиск", key: "players_title", label: "Заголовок", long: false },
    { group: "Поиск", key: "players_lead", label: "Lead", long: false },
    { group: "Правила", key: "rules_eyebrow", label: "Eyebrow", long: false },
    { group: "Правила", key: "rules_title", label: "Заголовок", long: false },
    { group: "Правила", key: "rules_1", label: "Пункт 1", long: true },
    { group: "Правила", key: "rules_2", label: "Пункт 2", long: true },
    { group: "Правила", key: "rules_3", label: "Пункт 3", long: true },
    { group: "Правила", key: "rules_4", label: "Пункт 4", long: true },
    { group: "Правила", key: "rules_5", label: "Пункт 5", long: true },
  ];

  const FALLBACK_NEWS = [
    {
      title: "Лаунчер 2.9.69",
      body: "Автоматическая загрузка сборки с зеркал, авторизация через портал и самообновление лаунчера.",
      published_at: "2026-08-17",
    },
    {
      title: "Подключение к серверу",
      body: "Заходи по IP с сайта. Отдельный туннель для модов больше не нужен.",
      published_at: "2026-08-01",
    },
    {
      title: "Авторыбалка AquaTech",
      body: "Удочки с кастомным лутом и авторыбалкой на сервере.",
      published_at: "2026-07-15",
    },
  ];

  const FALLBACK_PLAYERS = [
    { nick: "Renfild", privilege: "Создатель", playtime_hours: 340, playtime: "340 ч", coins: 854000, likes: 256, fish: 4890, badges: ["Создатель", "Мастер рыбалки", "Deep Ocean", "VIP"], bio: "Основатель проекта AquaTech. Покоритель Бездны.", theme: "ocean" },
    { nick: "AquaSmoke1", privilege: "Легенда", playtime_hours: 215, playtime: "215 ч", coins: 490000, likes: 142, fish: 3120, badges: ["Top Fisher", "Легенда"], bio: "Ловлю рыбу в лаве на Magma Rod.", theme: "deep" },
    { nick: "xietoru", privilege: "Адмирал", playtime_hours: 180, playtime: "180 ч", coins: 345000, likes: 98, fish: 2450, badges: ["Beta Tester", "Адмирал"], bio: "Исследователь биомов и кастомного лута.", theme: "storm" },
    { nick: "VortexHunter", privilege: "Капитан", playtime_hours: 120, playtime: "120 ч", coins: 180000, likes: 64, fish: 1780, badges: ["Капитан"], bio: "AquaTech Fishing Legend", theme: "abyss" },
    { nick: "Nautilus99", privilege: "Шкипер", playtime_hours: 95, playtime: "95 ч", coins: 120000, likes: 45, fish: 1340, badges: ["Шкипер", "Рыбак"], bio: "Изучаю таблицы T1-T13.", theme: "ocean" },
    { nick: "SeaDragon", privilege: "Моряк", playtime_hours: 80, playtime: "80 ч", coins: 95000, likes: 38, fish: 980, badges: ["Моряк"], bio: "Поймал Титановую руду на T2!", theme: "deep" }
  ];

  let apiAvailable = null;
  let audioCtx = null;
  let soundOn = localStorage.getItem(STORAGE_SOUND) === "1";
  let reduceMotion = false;

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
    try { updateAuthLinks(); } catch {}
  }

  function skinUrl(nick) {
    return `/api/skins/${encodeURIComponent(nick)}/avatar?v=look2`;
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
    playTone("ok");
    clearTimeout(toast._t);
    toast._t = setTimeout(() => el.classList.remove("show"), 2600);
  }

  function ensureAudio() {
    if (!soundOn || reduceMotion) return null;
    const AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return null;
    if (!audioCtx) audioCtx = new AC();
    if (audioCtx.state === "suspended") audioCtx.resume().catch(() => {});
    return audioCtx;
  }

  function playTone(kind = "click") {
    const ctx = ensureAudio();
    if (!ctx) return;
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    if (kind === "hover") {
      osc.type = "sine";
      osc.frequency.setValueAtTime(660, now);
      gain.gain.setValueAtTime(0.0001, now);
      gain.gain.exponentialRampToValueAtTime(0.015, now + 0.01);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.07);
      osc.start(now);
      osc.stop(now + 0.08);
      return;
    }
    if (kind === "tick") {
      osc.type = "sine";
      osc.frequency.setValueAtTime(800, now);
      osc.frequency.exponentialRampToValueAtTime(320, now + 0.02);
      gain.gain.setValueAtTime(0.0001, now);
      gain.gain.exponentialRampToValueAtTime(0.04, now + 0.003);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.025);
      osc.start(now);
      osc.stop(now + 0.03);
      return;
    }
    if (kind === "ok") {
      osc.type = "triangle";
      osc.frequency.setValueAtTime(520, now);
      osc.frequency.exponentialRampToValueAtTime(780, now + 0.08);
      gain.gain.setValueAtTime(0.0001, now);
      gain.gain.exponentialRampToValueAtTime(0.04, now + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.18);
      osc.start(now);
      osc.stop(now + 0.2);
      return;
    }
    osc.type = "sine";
    osc.frequency.setValueAtTime(420, now);
    osc.frequency.exponentialRampToValueAtTime(280, now + 0.06);
    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(0.035, now + 0.012);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.12);
    osc.start(now);
    osc.stop(now + 0.14);
  }

  function wireSounds() {
    reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    document.addEventListener(
      "pointerdown",
      (e) => {
        const t = e.target.closest(".btn, .ip-box, .tile, .news-item, .menu-btn, .sound-toggle, .tab");
        if (!t) return;
        playTone("click");
      },
      true
    );
    let hoverAt = 0;
    document.addEventListener(
      "pointerover",
      (e) => {
        const t = e.target.closest(".btn, .tile, .news-item");
        if (!t || reduceMotion) return;
        const now = performance.now();
        if (now - hoverAt < 80) return;
        hoverAt = now;
        playTone("hover");
      },
      true
    );
  }

  function formatNewsDate(raw) {
    const s = String(raw || "").trim();
    if (!s) return "";
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!m) return s;
    const months = [
      "января",
      "февраля",
      "марта",
      "апреля",
      "мая",
      "июня",
      "июля",
      "августа",
      "сентября",
      "октября",
      "ноября",
      "декабря",
    ];
    const day = Number(m[3]);
    const month = months[Number(m[2]) - 1] || m[2];
    return `${day} ${month} ${m[1]}`;
  }

  function renderNewsList(root, items, { link = false, limit = 40 } = {}) {
    if (!root) return;
    const rows = (items || []).slice(0, limit);
    if (!rows.length) {
      root.innerHTML = `<p class="muted-line">Пока пусто.</p>`;
      return;
    }
    root.innerHTML = rows
      .map((n, i) => {
        const inner = `<time>${esc(formatNewsDate(n.published_at))}</time>
          <h3>${esc(n.title)}</h3>
          <p>${esc(n.body)}</p>`;
        const delay = `style="--d:${(0.04 * i).toFixed(2)}s"`;
        if (link) {
          return `<a class="news-item reveal" href="news.html" ${delay}>${inner}</a>`;
        }
        return `<article class="news-item reveal" ${delay}>${inner}</article>`;
      })
      .join("");
    initReveal();
  }

  function applySiteCopy(copy) {
    if (!copy) return;
    document.querySelectorAll("[data-site]").forEach((el) => {
      const key = el.getAttribute("data-site");
      if (key && copy[key]) el.textContent = copy[key];
    });
  }

  async function loadSiteContent() {
    const homeNews = $("[data-news-home]");
    const pageNews = $("[data-news-page]");
    try {
      const data = await api("/api/site");
      applySiteCopy(data.copy || {});
      if (homeNews) renderNewsList(homeNews, data.news || [], { link: true, limit: 4 });
      if (pageNews) {
        const full = await api("/api/news");
        renderNewsList(pageNews, full.news || data.news || [], { link: false });
      }
    } catch {
      if (homeNews) renderNewsList(homeNews, FALLBACK_NEWS, { link: true, limit: 2 });
      if (pageNews) renderNewsList(pageNews, FALLBACK_NEWS, { link: false });
    }
  }

  function isMirrorHost() {
    const h = location.hostname || "";
    return h.includes("github.io") || h.includes("jsdelivr.net");
  }

  function isCanonicalHost() {
    const h = location.hostname || "";
    return (
      h === "aquateche.store" ||
      h === "www.aquateche.store" ||
      h.includes("santcrail.workers.dev") ||
      h.includes("pages.dev")
    );
  }

  const _apiCache = new Map();
  async function api(path, opts = {}) {
    const isGet = !opts.method || opts.method.toUpperCase() === "GET";
    if (isGet && _apiCache.has(path)) {
      const entry = _apiCache.get(path);
      if (Date.now() - entry.ts < 2500) {
        return entry.promise;
      }
    }
    const url = `${API_BASE}${path}`;
    const p = (async () => {
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
    })();
    if (isGet) {
      _apiCache.set(path, { ts: Date.now(), promise: p });
    }
    return p;
  }

  function copyIP(e) {
    navigator.clipboard?.writeText(IP).then(
      () => toast("IP скопирован: " + IP),
      () => toast(IP)
    );
    if (soundOn) playTone("ok");
    const target = e?.currentTarget || (e?.target ? e.target.closest("[data-copy-ip]") : null);
    if (target) {
      target.classList.add("copied");
      const copySpan = target.querySelector(".copy");
      if (copySpan) {
        const prev = copySpan.textContent;
        copySpan.textContent = "✓ Скопировано!";
        setTimeout(() => {
          target.classList.remove("copied");
          copySpan.textContent = prev;
        }, 2200);
      }
    }
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

  function navLink(n, active) {
    return `<a href="${n.href}" class="${n.id === active ? "active" : ""}">${n.label}</a>`;
  }

  function soundGlyph(on) {
    return on
      ? `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 10v4h3l5 4V6L7 10H4z"/><path d="M16 9.5a3.5 3.5 0 0 1 0 5"/><path d="M18.3 7a7 7 0 0 1 0 10"/></svg>`
      : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 10v4h3l5 4V6L7 10H4z"/><path d="M16 9l5 6M21 9l-5 6"/></svg>`;
  }

  function updateAuthLinks() {
    const user = getUser();
    const startNext = document.querySelector(".start-next");
    if (startNext) {
      if (user?.nick) {
        startNext.innerHTML = `<span style="color:#94a3b8;font-size:0.92rem">Вы вошли как <a href="profile.html" style="color:#2fe0c0;font-weight:600">${esc(user.nick)}</a> · <a href="profile.html">Личный кабинет</a></span>`;
      } else {
        startNext.innerHTML = `<a href="register.html">Создать аккаунт</a>`;
      }
    }
    if (user?.nick) {
      document.querySelectorAll('a[href="register.html"]').forEach((link) => {
        if (!link.closest("#mobile-nav") && !link.closest(".header-account-panel")) {
          link.href = "profile.html";
          link.textContent = "Личный кабинет";
        }
      });
    }
  }

  function renderHeader() {
    const mount = $("#site-header");
    if (!mount) return;
    const user = getUser();
    const active = pageId();
    const primary = NAV_PRIMARY.map((n) => navLink(n, active)).join("");
    const moreCurrent = NAV_MORE.some((n) => n.id === active);
    const moreLinks = NAV_MORE.map((n) => navLink(n, active)).join("");
    const allMobile = [...NAV_PRIMARY, ...NAV_MORE].map((n) => navLink(n, active)).join("");
    const coins =
      user
        ? `<div class="header-balances">
            <a href="profile.html" class="header-rubles" title="Рублёвый баланс: ${Number(user.rub_balance || 0).toLocaleString("ru-RU")} ₽">${Number(user.rub_balance || 0).toLocaleString("ru-RU")} ₽</a>
            <a href="profile.html#overview" class="header-coins" title="АкваМонеты: ${Number(user.coins || 0).toLocaleString("ru-RU")}"><span class="aqua-coin-badge">${Number(user.coins || 0).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" /></span></a>
          </div>`
        : "";
    const account = user
      ? `<details class="header-account">
            <summary aria-haspopup="menu">${esc(user.nick)}</summary>
            <div class="header-account-panel">
              <a href="profile.html">Кабинет</a>
              ${user.is_admin ? '<a href="admin.html">Админка</a>' : ""}
              <a href="${DISCORD}" target="_blank" rel="noopener noreferrer">Discord</a>
              <button type="button" data-sound-toggle>${soundOn ? "Звуки: вкл" : "Звуки: выкл"}</button>
              <button type="button" data-logout>Выйти</button>
            </div>
          </details>`
        : `<a class="header-signin" href="login.html">Войти</a>`;

    mount.innerHTML = `
      <header class="site-header">
        <div class="container header-inner">
          <a class="brand" href="index.html"><img src="assets/logo.png?v=2" alt="" width="28" height="28" /><span>AquaTech</span></a>
          <nav class="nav-desktop" aria-label="Основное">
            ${primary}
            <details class="nav-more">
              <summary aria-haspopup="menu"${moreCurrent ? ' class="is-current"' : ""}>Ещё</summary>
              <div class="nav-more-panel">${moreLinks}</div>
            </details>
          </nav>
          <div class="header-end">
            <span class="header-live" title="Онлайн на сервере"><span class="dot"></span><span data-online aria-live="polite">0 онлайн</span></span>
          ${coins}
            ${account}
            <a class="header-cta" data-download href="${DOWNLOAD}">Скачать</a>
            <button class="menu-btn" type="button" aria-label="Меню" aria-expanded="false" aria-controls="mobile-nav" data-menu>
              <span></span><span></span><span></span>
            </button>
          </div>
        </div>
        <div class="mobile-nav" id="mobile-nav" role="dialog" aria-modal="true" aria-label="Меню" aria-hidden="true">
          <div class="container">
            <div class="mobile-nav-status" title="Онлайн на сервере"><span class="dot"></span><span data-online aria-live="polite">…</span></div>
            <a href="index.html" class="${active === "home" ? "active" : ""}">Главная</a>
            ${allMobile}
            ${user?.is_admin ? '<a href="admin.html">Админка</a>' : ""}
            ${
              user
                ? `<a href="profile.html">Кабинет · ${Number(user.rub_balance || 0).toLocaleString("ru-RU")} ₽</a>
                   <button type="button" data-topup>Пополнить</button>
                   <button type="button" data-sound-toggle>${soundOn ? "Звуки: вкл" : "Звуки: выкл"}</button>
                   <button type="button" data-logout>Выйти</button>`
                : `<a href="login.html">Войти</a>
                   <a href="register.html">Регистрация</a>`
            }
            <a class="nav-cta" href="${DOWNLOAD}">Скачать лаунчер</a>
            <a href="${DISCORD}" target="_blank" rel="noopener noreferrer">Discord</a>
          </div>
        </div>
      </header>`;

    const menuBtn = $("[data-menu]", mount);
    const mobileNav = $("#mobile-nav", mount);
    menuBtn?.addEventListener("click", (e) => {
      e.stopPropagation();
      setMobileMenu(!mobileNav?.classList.contains("open"));
    });
    mobileNav?.querySelectorAll("a").forEach((a) => {
      a.addEventListener("click", () => setMobileMenu(false));
    });
    mount.querySelectorAll("details").forEach((d) => {
      d.addEventListener("toggle", () => {
        if (!d.open) return;
        mount.querySelectorAll("details").forEach((other) => {
          if (other !== d) other.removeAttribute("open");
        });
      });
    });
    headerOutsideClick?.abort();
    headerOutsideClick = new AbortController();
    document.addEventListener(
      "click",
      (e) => {
      if (!mount.contains(e.target)) {
        setMobileMenu(false);
          mount.querySelectorAll("details.nav-more, details.header-account").forEach((d) => {
          d.removeAttribute("open");
        });
      }
      },
      { signal: headerOutsideClick.signal }
    );
    mount.querySelectorAll("[data-sound-toggle]").forEach((btn) => {
      btn.addEventListener("click", () => {
      soundOn = !soundOn;
      localStorage.setItem(STORAGE_SOUND, soundOn ? "1" : "0");
        mount.querySelectorAll("[data-sound-toggle]").forEach((b) => {
          b.textContent = soundOn ? "Звуки: вкл" : "Звуки: выкл";
        });
      if (soundOn) playTone("ok");
      });
    });
    mount.querySelectorAll("[data-logout]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        try {
          await api("/api/logout", { method: "POST", body: "{}" });
        } catch {
          /* offline / mirror */
        }
        setUser(null);
        location.href = "index.html";
      });
    });
    mount.querySelectorAll("[data-topup]").forEach((btn) => {
      btn.addEventListener("click", () => {
        setMobileMenu(false);
        openTopupModal(100);
      });
    });
  }

  function setMobileMenu(open) {
    const btn = document.querySelector("[data-menu]");
    const nav = document.getElementById("mobile-nav");
    if (!btn || !nav) return;
    btn.classList.toggle("active", open);
    nav.classList.toggle("open", open);
    btn.setAttribute("aria-expanded", open ? "true" : "false");
    nav.setAttribute("aria-hidden", open ? "false" : "true");
    if (open) {
      nav.querySelector("a, button")?.focus();
    } else if (nav.contains(document.activeElement)) {
      btn.focus();
    }
  }

  function renderFooter() {
    const mount = $("#site-footer");
    if (!mount) return;
    mount.innerHTML = `
      <footer class="site-footer">
        <div class="container footer-grid">
          <div>
            <div class="brand" style="margin-bottom:0.8rem"><span class="brand-mark"></span>AquaTech</div>
            <p style="color:var(--muted);margin:0;max-width:28rem" data-site="footer_blurb">Океанский сервер. Скачай лаунчер и заходи.</p>
          </div>
          <div>
            <h4>Игра</h4>
            <a href="start.html">Скачать лаунчер</a>
            <a href="market.html">Аукцион</a>
            <a href="rods.html">Удочки AquaTech</a>
            <a href="cases.html">Кейсы</a>
            <a href="store.html">Донат</a>
          </div>
          <div>
            <h4>Сообщество</h4>
            <a href="top.html">Топы игроков</a>
            <a href="players.html">Поиск игроков</a>
            <a href="news.html">Новости</a>
            <a href="profile.html">Профили</a>
            <a href="${DISCORD}" target="_blank" rel="noopener noreferrer">Discord</a>
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
    document.querySelectorAll("[data-download-zip]").forEach((el) => {
      el.setAttribute("href", DOWNLOAD_ZIP);
    });
    updateAuthLinks();
    refreshOnlinePill();
    setInterval(refreshOnlinePill, 30000);

    // Back to Top button
    const btt = document.getElementById("backToTop");
    if (btt) {
      window.addEventListener(
        "scroll",
        () => {
          if (window.scrollY > 450) btt.classList.add("visible");
          else btt.classList.remove("visible");
        },
        { passive: true }
      );
      btt.addEventListener("click", () => {
        window.scrollTo({ top: 0, behavior: "smooth" });
      });
    }

    // Rods loot search
    const lootSearch = document.getElementById("loot-search");
    if (lootSearch) {
      lootSearch.addEventListener("input", () => {
        const query = lootSearch.value.trim().toLowerCase();
        const blocks = document.querySelectorAll(".loot-block");
        blocks.forEach((block) => {
          let hasMatch = false;
          const rows = block.querySelectorAll(".loot-table tbody tr");
          rows.forEach((tr) => {
            const text = tr.textContent.toLowerCase();
            if (!query || text.includes(query)) {
              tr.style.display = "";
              if (query && text.includes(query)) {
                tr.style.background = "rgba(92, 225, 255, 0.15)";
                hasMatch = true;
              } else {
                tr.style.background = "";
              }
            } else {
              tr.style.display = "none";
            }
          });
          if (query && !hasMatch && !block.textContent.toLowerCase().includes(query)) {
            block.style.opacity = "0.25";
          } else {
            block.style.opacity = "1";
          }
        });
      });
    }

    // Spotlight cursor tracking on cards
    document.querySelectorAll(".tile, .catalog-card, .join-panel, .loot-block, .card").forEach((card) => {
      card.addEventListener("pointermove", (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        card.style.setProperty("--mouse-x", `${x}px`);
        card.style.setProperty("--mouse-y", `${y}px`);
      });
    });

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") {
        document.querySelectorAll(".loot-modal-overlay.open").forEach((m) => m.classList.remove("open"));
        setMobileMenu(false);
        document.querySelectorAll("#site-header details[open]").forEach((d) => d.removeAttribute("open"));
      }
    });
  }

  async function refreshOnlinePill() {
    const pills = document.querySelectorAll("[data-online]");
    const heroCount = $("#heroOnlineCount");
    const bentoCount = $("#bentoOnlineText");
    const showcaseStatus = $("#showcaseStatusText");
    const showcasePing = $("#showcasePingText");
    try {
      const t0 = performance.now();
      const data = await api("/api/server-status");
      const latency = Math.max(12, Math.round(performance.now() - t0));
      const online = !!data.online;
      const n = Number(data.players_online || 0) || 0;
      const max = Number(data.players_max || 0) || 50;
      const label = online ? `${n} онлайн` : "оффлайн";
      pills.forEach((el) => {
        el.textContent = label;
      });
      if (heroCount) heroCount.textContent = `${n} онлайн`;
      if (bentoCount) bentoCount.textContent = `${n} онлайн`;
      if (showcaseStatus) {
        showcaseStatus.textContent = online ? `СЕРВЕР В СЕТИ · ${n}/${max} ИГРОКОВ` : "СЕРВЕР НА ТЕХОБСЛУЖИВАНИИ";
      }
      if (showcasePing) {
        if (online) {
          showcasePing.textContent = `${latency} ms`;
          showcasePing.style.color = latency < 80 ? "#22c55e" : latency < 180 ? "#f59e0b" : "#ef4444";
          showcasePing.style.borderColor = latency < 80 ? "rgba(34,197,94,0.3)" : "rgba(245,158,11,0.3)";
        } else {
          showcasePing.textContent = "ОФФЛАЙН";
          showcasePing.style.color = "#ef4444";
          showcasePing.style.borderColor = "rgba(239,68,68,0.3)";
        }
      }
      document.querySelectorAll(".online-pill, .hero-status-pill, .mobile-nav-status, #bentoOnlinePill, .header-live").forEach((el) => {
        el.classList.toggle("is-offline", !online);
        el.title = online
          ? `Онлайн на сервере: ${n}${data.players_max ? " / " + data.players_max : ""} · Пинг: ${latency} ms`
          : "Сервер сейчас недоступен";
      });
    } catch {
      pills.forEach((el) => {
        el.textContent = "0 онлайн";
      });
      if (heroCount) heroCount.textContent = "0 онлайн";
      if (bentoCount) bentoCount.textContent = "0 онлайн";
      if (showcaseStatus) showcaseStatus.textContent = "СЕРВЕР В СЕТИ · FORGE 1.20.1";
      if (showcasePing) {
        showcasePing.textContent = "24/7 ОНЛАЙН";
        showcasePing.style.color = "#22c55e";
      }
    }
  }

  function cleanPrivilege(priv) {
    if (!priv) return "Игрок";
    let s = String(priv)
      .replace(/[\uE000-\uF8FF\uD800-\uDFFF]/g, "")
      .replace(/§[0-9a-fk-or]/gi, "")
      .trim();
    s = s.replace(/^[\[\(<]+/, "").replace(/[\]\)>]+$/, "").trim();
    const low = s.toLowerCase();
    if (!s || low === "default" || low === "player" || low === "игрок" || low === "матрос" || low === "пролог") return "Игрок";
    if (low.includes("owner") || low.includes("создател") || low.includes("владел")) return "Владелец";
    if (low.includes("admin") || low.includes("админ")) return "Админ";
    if (low.includes("dev") || low.includes("разраб")) return "Разработчик";
    if (low.includes("mod") || low.includes("модер")) return "Модератор";
    if (low.includes("helper") || low.includes("хелпер")) return "Хелпер";
    if (low.includes("manager") || low.includes("куратор") || low.includes("менеджер")) return "Куратор";
    if (low.includes("staff") || low.includes("персонал")) return "Персонал";
    if (low.includes("vipplus") || low.includes("vip+")) return "VIP+";
    if (low.includes("vip")) return "VIP";
    if (low.includes("deluxe") || low.includes("делюкс")) return "Deluxe";
    if (low.includes("ultimate") || low.includes("ультимейт")) return "Ultimate";
    if (low.includes("legend") || low.includes("легенд")) return "Легенда";
    if (low.includes("admiral") || low.includes("адмирал")) return "Адмирал";
    if (low.includes("captain") || low.includes("капитан")) return "Капитан";
    if (low.includes("skipper") || low.includes("шкипер")) return "Шкипер";
    if (low.includes("sailor") || low.includes("моряк")) return "Моряк";
    if (low.includes("streamer") || low.includes("стример") || low.includes("twitch")) return "Стример";
    if (low.includes("youtuber") || low.includes("ютубер") || low.includes("youtube")) return "YouTuber";
    if (low.includes("artist") || low.includes("артист")) return "Артист";
    if (low.includes("builder") || low.includes("билдер") || low.includes("строитель")) return "Билдер";
    if (low.includes("friend") || low.includes("друг")) return "Друг";
    if (low.includes("trainee") || low.includes("стажер") || low.includes("стажёр")) return "Стажер";
    return s;
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
            ? `<span class="aqua-coin-badge">${Number(p.coins || 0).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" /></span>`
            : mode === "likes"
              ? `${p.likes || 0} ❤`
              : mode === "fish"
                ? `${Number(p.fish || 0).toLocaleString("ru-RU")} рыб`
                : playtime;
        const priv = cleanPrivilege(p.privilege);
        return `<a class="top-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
            <div class="rank">${i + 1}</div>
            <img src="${skinUrl(p.nick)}" alt="">
            <div class="meta"><strong>${p.nick}</strong><span>${priv}</span></div>
            <div class="stat">${stat}</div>
          </a>`;
      })
      .join("");
  }

  async function loadPlayers(sort = "likes", q = "") {
    const qs = new URLSearchParams({ sort, limit: "40" });
    if (q) qs.set("q", q);
    try {
      const data = await api(`/api/players?${qs}`);
      if (data.players && data.players.length > 0) return data.players;
    } catch {
      /* fallback to local database */
    }
    let list = [...FALLBACK_PLAYERS];
    if (q) {
      const query = q.toLowerCase();
      list = list.filter((p) => p.nick.toLowerCase().includes(query));
    }
    if (sort === "coins") list.sort((a, b) => b.coins - a.coins);
    else if (sort === "likes") list.sort((a, b) => b.likes - a.likes);
    else if (sort === "fish") list.sort((a, b) => b.fish - a.fish);
    else if (sort === "playtime" || sort === "playtime_hours")
      list.sort((a, b) => b.playtime_hours - a.playtime_hours);
    return list;
  }

  function initTop() {
    const podiumRoot = $("#top-podium-root");
    const bentoRoot = $("#top-bento-root");
    const legacyRoot = $("#top-root");
    if (!podiumRoot && !legacyRoot) return;

    let mode = "fish";

    const getTitles = (tab) => {
      if (tab === "fish") return { t1: "Гроза глубин", t2: "Мастер заброса", t3: "Ловец волн", unit: "рыб" };
      if (tab === "playtime") return { t1: "Вечный страж", t2: "Старожил архипелага", t3: "Морской волк", unit: "ч" };
      if (tab === "coins") return { t1: "Океанский магнат", t2: "Золотой картель", t3: "Богатый шкипер", unit: "" };
      return { t1: "Кумир сервера", t2: "Душа компании", t3: "Любимец публики", unit: "❤" };
    };

    const formatStat = (p, tab) => {
      if (tab === "fish") return `${Number(p.fish || 0).toLocaleString("ru-RU")} рыб`;
      if (tab === "playtime") return `${p.playtime_hours || parseInt(p.playtime || "0", 10) || 0} ч`;
      if (tab === "coins") return `${Number(p.coins || 0).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="">`;
      return `${p.likes || 0} ❤`;
    };

    const render = async () => {
      if (podiumRoot) podiumRoot.innerHTML = `<p class="muted-line" style="grid-column:1/-1;text-align:center">Загрузка топа…</p>`;
      if (bentoRoot) bentoRoot.innerHTML = `<p class="muted-line" style="text-align:center">Синхронизация с сервером…</p>`;
      if (legacyRoot) legacyRoot.innerHTML = `<p class="muted-line">Загрузка…</p>`;

      try {
        const players = await loadPlayers(mode === "playtime" ? "playtime" : mode);
        if (!players || !players.length) {
          if (podiumRoot) podiumRoot.innerHTML = `<p class="muted-line" style="grid-column:1/-1;text-align:center">Пока нет игроков в рейтинге.</p>`;
          if (bentoRoot) bentoRoot.innerHTML = `<p class="muted-line" style="text-align:center">Данные обновятся в полночь.</p>`;
          return;
        }

        const titles = getTitles(mode);
        const top3 = players.slice(0, 3);
        const rest = players.slice(3);

        if (podiumRoot) {
          const p1 = top3[0];
          const p2 = top3[1];
          const p3 = top3[2];

          podiumRoot.innerHTML = `
            ${p2 ? `
              <a class="podium-card podium-2" href="profile.html?u=${encodeURIComponent(p2.nick)}">
                <div class="podium-crown-badge">🥈 2 Место</div>
                <div class="podium-avatar-wrap">
                  <img class="podium-avatar-img" src="${skinUrl(p2.nick)}" alt="${esc(p2.nick)}" loading="lazy" />
                </div>
                <div class="podium-nick">${esc(p2.nick)}</div>
                <div class="podium-role">${cleanPrivilege(p2.privilege)} · ${titles.t2}</div>
                <div class="podium-stat-box">
                  <span class="podium-stat-val">${formatStat(p2, mode)}</span>
                  <span class="podium-stat-lbl">${titles.unit}</span>
                </div>
              </a>
            ` : ""}

            ${p1 ? `
              <a class="podium-card podium-1" href="profile.html?u=${encodeURIComponent(p1.nick)}">
                <div class="podium-crown-badge">🥇 1 Место</div>
                <div class="podium-avatar-wrap">
                  <img class="podium-avatar-img" src="${skinUrl(p1.nick)}" alt="${esc(p1.nick)}" loading="lazy" />
                </div>
                <div class="podium-nick">${esc(p1.nick)}</div>
                <div class="podium-role">${cleanPrivilege(p1.privilege)} · ${titles.t1}</div>
                <div class="podium-stat-box">
                  <span class="podium-stat-val">${formatStat(p1, mode)}</span>
                  <span class="podium-stat-lbl">${titles.unit}</span>
                </div>
              </a>
            ` : ""}

            ${p3 ? `
              <a class="podium-card podium-3" href="profile.html?u=${encodeURIComponent(p3.nick)}">
                <div class="podium-crown-badge">🥉 3 Место</div>
                <div class="podium-avatar-wrap">
                  <img class="podium-avatar-img" src="${skinUrl(p3.nick)}" alt="${esc(p3.nick)}" loading="lazy" />
                </div>
                <div class="podium-nick">${esc(p3.nick)}</div>
                <div class="podium-role">${cleanPrivilege(p3.privilege)} · ${titles.t3}</div>
                <div class="podium-stat-box">
                  <span class="podium-stat-val">${formatStat(p3, mode)}</span>
                  <span class="podium-stat-lbl">${titles.unit}</span>
                </div>
              </a>
            ` : ""}
          `;
        }

        if (bentoRoot) {
          if (rest.length) {
            bentoRoot.innerHTML = rest.map((p, idx) => `
              <a class="top-bento-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
                <div class="top-rank-num">#${idx + 4}</div>
                <img class="top-row-avatar" src="${skinUrl(p.nick)}" alt="${esc(p.nick)}" loading="lazy" />
                <div class="top-row-info">
                  <div class="top-row-nick">${esc(p.nick)}</div>
                  <div class="top-row-meta">${cleanPrivilege(p.privilege)}</div>
                </div>
                <div class="top-row-stat">${formatStat(p, mode)}</div>
              </a>
            `).join("");
          } else {
            bentoRoot.innerHTML = `<p class="muted-line" style="text-align:center;padding:1rem">Здесь появятся следующие участники рейтинга.</p>`;
          }
        }

        if (legacyRoot) {
          legacyRoot.innerHTML = playerRows(players, mode) || `<p class="muted-line">Пока нет игроков в базе.</p>`;
        }
      } catch (e) {
        console.error("Failed to load leaderboard:", e);
        if (podiumRoot) podiumRoot.innerHTML = `<p class="muted-line" style="grid-column:1/-1;text-align:center">Не удалось загрузить топ. Попробуй позже.</p>`;
        if (bentoRoot) bentoRoot.innerHTML = `<p class="muted-line" style="text-align:center">Ошибка сети при получении данных.</p>`;
      }
    };

    document.querySelectorAll("[data-top-tab]").forEach((btn) => {
      btn.addEventListener("click", () => {
        mode = btn.dataset.topTab;
        document.querySelectorAll("[data-top-tab]").forEach((b) => b.classList.toggle("active", b === btn));
        playTone("click");
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
            <div class="meta"><strong>${p.nick}</strong><span>${cleanPrivilege(p.privilege)} · ${p.playtime || (p.playtime_hours || 0) + " ч"}</span></div>
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

  const THEMES = [
    { id: "ocean", label: "Океан" },
    { id: "deep", label: "Глубина" },
    { id: "storm", label: "Шторм" },
    { id: "abyss", label: "Бездна" },
    { id: "magma", label: "Магма" },
    { id: "celestial", label: "Небесный" },
    { id: "cyber", label: "Кибер" },
    { id: "aurora", label: "Аврора" },
  ];

  function heartSvg(filled) {
    return `<svg class="heart-icon" width="16" height="16" viewBox="0 0 24 24" fill="${filled ? "currentColor" : "none"}" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>`;
  }

  const LK_TABS = [
    { id: "overview", label: "Обзор" },
    { id: "skin", label: "Скин и плащ" },
    { id: "theme", label: "Тема профиля" },
    { id: "password", label: "Пароль" },
    { id: "about", label: "О себе" },
  ];

  function avatarSrc(nick, ver) {
    const v = ver ? encodeURIComponent(ver) : "look2";
    return `/api/skins/${encodeURIComponent(nick)}/avatar?v=${v}`;
  }

  async function apiUpload(path, form) {
    const res = await fetch(`${API_BASE}${path}`, { method: "POST", credentials: "include", body: form });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
    return data;
  }

  function blitSkin(ctx, img, sx, sy, sw, sh, dx, dy, scale) {
    ctx.drawImage(img, sx, sy, sw, sh, dx * scale, dy * scale, sw * scale, sh * scale);
  }

  function drawSkinFront(canvas, img) {
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    const scale = Math.max(4, Math.floor(canvas.width / 16));
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const hd = img.width >= 128 ? img.width / 64 : 1;
    const s = (sx, sy, sw, sh, dx, dy) => blitSkin(ctx, img, sx * hd, sy * hd, sw * hd, sh * hd, dx, dy, scale);
    s(8, 8, 8, 8, 4, 0);
    s(40, 8, 8, 8, 4, 0);
    s(44, 20, 4, 12, 0, 8);
    s(20, 20, 8, 12, 4, 8);
    if (img.height >= 64) s(36, 52, 4, 12, 12, 8);
    else s(44, 20, 4, 12, 12, 8);
    s(4, 20, 4, 12, 4, 20);
    if (img.height >= 64) s(20, 52, 4, 12, 8, 20);
    else s(4, 20, 4, 12, 8, 20);
  }

  function paintSkinCanvas(canvas, url) {
    if (!canvas || !url) return;
    const img = new Image();
    img.onload = () => drawSkinFront(canvas, img);
    img.src = url;
  }

  function lkTab() {
    const h = (location.hash || "#overview").replace("#", "");
    return LK_TABS.some((t) => t.id === h) ? h : "overview";
  }

  function lookCard(kind, title, hint, editable) {
    const preview =
      kind === "cape"
        ? `<img class="look-cape" alt="" data-look-img="cape">`
        : kind === "avatar"
          ? `<img class="look-avatar" alt="" data-look-img="avatar">`
          : `<canvas class="skin-canvas" width="80" height="160" data-look-canvas="skin" aria-label="Превью скина"></canvas>`;
    const accept = kind === "avatar" ? "image/png,image/jpeg" : "image/png";
    const dropAttrs = editable
      ? `data-look-drop="${kind}" tabindex="0" role="button" aria-label="${title}: выбрать файл"`
      : "";
    const actions = editable
      ? `<div class="look-actions">
        <button type="button" class="btn btn-primary" data-look-upload="${kind}">Загрузить</button>
        <button type="button" class="btn btn-ghost" data-look-delete="${kind}">Удалить</button>
        <input type="file" accept="${accept}" hidden data-look-file="${kind}">
      </div>
      <p class="look-status" data-look-status="${kind}" aria-live="polite"></p>`
      : "";
    return `<article class="look-card ${kind === "avatar" ? "look-avatar-card" : ""}">
      <h3>${title}</h3>
      <div class="look-preview" ${dropAttrs}>
        ${preview}
      </div>
      <p class="look-hint">${hint}</p>
      ${actions}
    </article>`;
  }

  function skinLookCard(title, hint, editable) {
    const dropAttrs = editable
      ? `data-look-drop="skin" tabindex="0" role="button" aria-label="${title}: выбрать файл"`
      : "";
    const actions = editable
      ? `<div class="look-actions">
        <button type="button" class="btn btn-primary" data-look-upload="skin">Выбрать файл</button>
        <button type="button" class="btn btn-ghost" data-look-delete="skin">Удалить</button>
        <input type="file" accept="image/png" hidden data-look-file="skin">
      </div>
      <div class="skin-import-box" style="margin-top:0.6rem;display:flex;gap:0.4rem;width:100%">
        <input type="text" data-skin-import-input placeholder="Ник Mojang или URL скина..." style="flex:1;min-width:0;padding:0.45rem 0.65rem;border-radius:8px;background:rgba(0,0,0,0.3);border:1px solid rgba(47,224,192,0.25);color:#fff;font-size:0.82rem">
        <button type="button" class="btn btn-secondary" data-skin-import-btn style="padding:0.45rem 0.75rem;font-size:0.82rem;white-space:nowrap">Импорт</button>
      </div>
      <p class="look-status" data-look-status="skin" aria-live="polite"></p>`
      : "";
    return `<article class="look-card look-skin-card">
      <h3>${title}</h3>
      <div class="look-preview look-preview-3d" data-look-host="skin" ${dropAttrs}>
        <canvas class="skin-canvas" width="80" height="160" data-look-canvas="skin" aria-label="Превью скина"></canvas>
      </div>
      <p class="look-hint">${hint}</p>
      ${actions}
    </article>`;
  }

  const REDUCED_MOTION = window.matchMedia("(prefers-reduced-motion: reduce)");
  const LOOK_RULES = {
    skin: { dims: [[64, 64], [64, 32], [128, 128], [128, 64]], max: 131072 },
    cape: { dims: [[64, 32], [128, 64]], max: 65536 },
    avatar: { dims: null, max: 262144 },
  };
  let skinview3dPromise = null;

  function loadSkinview3d() {
    if (window.skinview3d) return Promise.resolve(window.skinview3d);
    if (!skinview3dPromise) {
      skinview3dPromise = new Promise((resolve, reject) => {
        const s = document.createElement("script");
        s.src = "/assets/js/vendor/skinview3d.bundle.js";
        s.onload = () => (window.skinview3d ? resolve(window.skinview3d) : reject(new Error("skinview3d")));
        s.onerror = () => reject(new Error("skinview3d"));
        document.head.appendChild(s);
      }).catch((err) => {
        skinview3dPromise = null;
        throw err;
      });
    }
    return skinview3dPromise;
  }

  function setLookStatus(root, kind, text, tone) {
    const el = root.querySelector(`[data-look-status="${kind}"]`);
    if (!el) return;
    el.textContent = text;
    el.classList.toggle("ok", tone === "ok");
    el.classList.toggle("err", tone === "err");
  }

  function fileImageSize(file) {
    return new Promise((resolve) => {
      if (!file.type || !file.type.startsWith("image/")) return resolve(null);
      const url = URL.createObjectURL(file);
      const img = new Image();
      img.onload = () => {
        const size = { w: img.naturalWidth, h: img.naturalHeight };
        URL.revokeObjectURL(url);
        resolve(size);
      };
      img.onerror = () => {
        URL.revokeObjectURL(url);
        resolve(null);
      };
      img.src = url;
    });
  }

  async function renderOwnCabinet(root, profile, user, theme, socials, mine, rubBalance) {
    rubBalance = Number(rubBalance || 0);
    const tab = lkTab();
    const nav = LK_TABS.map(
      (t) =>
        `<a href="#${t.id}" ${t.id === tab ? 'aria-current="page"' : ""}>${t.label}</a>`
    ).join("");
    const badges = (profile.badges || [])
      .map((b) => {
        if (typeof b === "string") return `<div class="badge-card common"><span class="badge-title">${esc(b)}</span></div>`;
        return `<div class="badge-card ${esc(b.rarity || "common")}"><span class="badge-title">${esc(b.title)}</span>${b.desc ? `<span class="badge-desc">${esc(b.desc)}</span>` : ""}</div>`;
      })
      .join("") || '<span class="muted-line">Пока пусто</span>';

    root.innerHTML = `
      <div class="lk-page">
        <div class="lk-hero profile-cover ${esc(theme)}">
          <div class="lk-hero-id">
            <img src="${avatarSrc(profile.nick)}" alt="" width="64" height="64" onerror="this.onerror=null;this.src='/assets/images/avatar_default.png'">
            <div>
              <h1>${esc(profile.nick)}</h1>
              <span class="tag ${profile.privilege === "Создатель" || profile.privilege === "Владелец" ? "gold" : ""}">${esc(cleanPrivilege(profile.privilege))}</span>
              ${
                mine
                  ? ""
                  : `<button class="btn-like ${profile.has_liked ? "liked" : ""}" type="button" id="btn-like-profile">
                      <span class="heart">${heartSvg(profile.has_liked)}</span>
                      <span id="like-count">${profile.likes || 0}</span>
                      <span style="font-size:0.82rem;font-weight:600;opacity:0.85">${profile.has_liked ? "Нравится" : "Похвалить"}</span>
                    </button>`
              }
            </div>
          </div>
          <div class="lk-balance">
            <div class="lk-balance-card card-rubles">
              <div class="card-top-row">
                <span class="currency-label">Рубли · Баланс</span>
                <span style="font-size:1.15rem">💳</span>
              </div>
              <strong>${rubBalance.toLocaleString("ru-RU")} ₽</strong>
              ${mine ? `<div class="card-actions"><a class="btn btn-primary" href="store.html" style="padding:0.35rem 0.75rem;font-size:0.82rem;font-weight:600">Магазин</a><button class="btn btn-secondary" type="button" id="lkTopupBtn" style="padding:0.35rem 0.75rem;font-size:0.82rem">Пополнить</button></div>` : ""}
            </div>
            <div class="lk-balance-card card-coins">
              <div class="card-top-row">
                <span class="currency-label">АкваМонеты</span>
                <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" />
              </div>
              <strong class="aqua-coin-badge">${Number(profile.coins || 0).toLocaleString("ru-RU")}</strong>
              ${mine ? `<div class="card-actions"><a class="btn btn-primary" href="cases.html" style="background:linear-gradient(135deg,#ffd875,#f59e0b);color:#091924;border:none;padding:0.35rem 0.75rem;font-size:0.82rem;font-weight:700">Кейсы F4</a><button class="btn btn-secondary" type="button" id="lkBuyCoinsBtn" style="padding:0.35rem 0.75rem;font-size:0.82rem">Купить</button></div>` : `<div class="card-actions"><a class="btn btn-secondary" href="cases.html" style="padding:0.35rem 0.75rem;font-size:0.82rem">Кейсы</a></div>`}
            </div>
            <div class="lk-balance-card card-fish">
              <div class="card-top-row">
                <span class="currency-label">Рейтинг рыбалки</span>
                <span style="font-size:1.15rem">🎣</span>
              </div>
              <strong>${Number(profile.fish || 0).toLocaleString("ru-RU")} рыб</strong>
              <div class="card-actions"><a class="btn btn-secondary" href="top.html" style="padding:0.35rem 0.75rem;font-size:0.82rem">Топы</a></div>
            </div>
          </div>
        </div>
        <div class="lk-shell">
          <nav class="lk-nav" aria-label="Кабинет">${nav}</nav>
          <div class="lk-main">
            <section class="lk-pane" data-lk-pane="overview" ${tab === "overview" ? "" : "hidden"}>
              <h2>Обзор</h2>
              <div class="stats-row">
                <div class="stat-card"><strong>${rubBalance.toLocaleString("ru-RU")} ₽</strong><span>рублёвый баланс</span></div>
                <div class="stat-card"><strong class="aqua-coin-badge">${Number(profile.coins || 0).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" /></strong><span>аква-монет в игре</span></div>
                <div class="stat-card"><strong>${Number(profile.fish || 0).toLocaleString("ru-RU")}</strong><span>рыбы поймано</span></div>
                <div class="stat-card"><strong>${esc(profile.playtime || (profile.playtime_hours || 0) + " ч")}</strong><span>время в игре</span></div>
                <div class="stat-card"><strong>${profile.views || 0}</strong><span>просмотры</span></div>
              </div>
              ${
                mine
                  ? `<div class="panel" style="margin-top:1.25rem;background:linear-gradient(135deg,rgba(47,224,192,0.12),rgba(8,28,44,0.6));border:1px solid rgba(47,224,192,0.3);display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:1rem;padding:1.25rem 1.5rem;border-radius:16px">
                <div style="display:flex;align-items:center;gap:1rem">
                  <img src="${avatarSrc(profile.nick)}" alt="" width="52" height="52" style="border-radius:12px;border:1px solid rgba(47,224,192,0.4);object-fit:cover;image-rendering:pixelated" onerror="this.onerror=null;this.src='/assets/images/avatar_default.png'">
                  <div>
                    <h3 style="margin:0 0 0.25rem;font-size:1.1rem;color:#f8fafc">Твой скин и аватарка</h3>
                    <p class="muted-line" style="margin:0;font-size:0.88rem">Загрузи PNG скин (64×64/128×128) или аватарку — они мгновенно появятся на сайте и в игре на сервере.</p>
                  </div>
                </div>
                <a class="btn btn-primary" href="#skin">Настроить скин и аватар</a>
              </div>`
                  : ""
              }
              <div class="panel" style="margin-top:1.25rem">
                <h3>Бейджи</h3>
                <div class="badge-grid">${badges}</div>
              </div>
            </section>
            <section class="lk-pane" data-lk-pane="skin" ${tab === "skin" ? "" : "hidden"}>
              <h2>Скин и плащ</h2>
              <div class="look-grid">
                ${skinLookCard("Скин", mine ? "PNG 64×64, 64×32 или 128×128. Модель крутится мышкой. Загруженный скин появится в игре." : "Скин игрока в игре.", mine)}
                ${lookCard("avatar", "Аватар", mine ? "Картинка на сайте. Можно перетащить файл в рамку." : "Аватар игрока на сайте.", mine)}
                ${lookCard("cape", "Плащ", mine ? "PNG 64×32 или 128×64. Появится на сайте и на модели скина." : "Плащ на сайте.", mine)}
              </div>
            </section>
            <section class="lk-pane" data-lk-pane="theme" ${tab === "theme" ? "" : "hidden"}>
              <h2>Тема профиля</h2>
              ${
                mine
                  ? `<form class="panel form" id="profile-edit-theme">
                <div class="theme-selector-grid">
                  ${THEMES.map(
                    (t) => `<label class="theme-pill"><input type="radio" name="theme" value="${t.id}" ${t.id === theme ? "checked" : ""}><div class="theme-pill-content">${t.label}</div></label>`
                  ).join("")}
                </div>
                <button class="btn btn-primary" type="submit" style="margin-top:1rem">Сохранить тему</button>
              </form>`
                  : `<p class="muted-line">Тема: ${esc((THEMES.find((t) => t.id === theme) || {}).label || theme)}</p>`
              }
            </section>
            <section class="lk-pane" data-lk-pane="password" ${tab === "password" ? "" : "hidden"}>
              <h2>Смена пароля</h2>
              ${
                mine
                  ? `<form class="panel form" id="password-change">
                <div class="field"><label for="pw-old">Текущий пароль</label><input id="pw-old" name="old" type="password" autocomplete="current-password" required></div>
                <div class="field"><label for="pw-next">Новый пароль (от 8 символов)</label><input id="pw-next" name="next" type="password" minlength="8" autocomplete="new-password" required></div>
                <div class="field"><label for="pw-next2">Повтори новый пароль</label><input id="pw-next2" name="next2" type="password" autocomplete="new-password" required></div>
                <p class="field-error" data-pw-error role="alert" aria-live="polite"></p>
                <button class="btn btn-primary" type="submit">Сменить пароль</button>
              </form>`
                  : `<p class="muted-line">Только для своего профиля.</p>`
              }
            </section>
            <section class="lk-pane" data-lk-pane="about" ${tab === "about" ? "" : "hidden"}>
              <h2>О себе</h2>
              ${
                mine
                  ? `<form class="panel form" id="profile-edit">
                ${socials.length ? `<div class="profile-socials">${socials.join("")}</div>` : ""}
                <div class="form-grid-2">
                  <div class="field"><label for="status_message">Статус</label><input id="status_message" type="text" name="status_message" maxlength="80" value="${esc(profile.status_message || "")}"></div>
                  <div class="field"><label for="fav_rod">Любимая удочка</label><input id="fav_rod" type="text" name="fav_rod" maxlength="50" value="${esc(profile.fav_rod || "")}"></div>
                </div>
                <div class="form-grid-2" style="margin-top:0.5rem">
                  <div class="field"><label for="social_tg">Telegram</label><input id="social_tg" type="text" name="social_tg" value="${esc(profile.social_tg || "")}"></div>
                  <div class="field"><label for="social_vk">VK</label><input id="social_vk" type="text" name="social_vk" value="${esc(profile.social_vk || "")}"></div>
                </div>
                <div class="field" style="margin-top:0.5rem"><label for="social_discord">Discord</label><input id="social_discord" type="text" name="social_discord" value="${esc(profile.social_discord || "")}"></div>
                <div class="field" style="margin-top:0.5rem"><label for="bio">Био</label><textarea id="bio" name="bio" rows="3" maxlength="300">${esc(profile.bio || "")}</textarea></div>
                <input type="hidden" name="theme" value="${esc(theme)}">
                <button class="btn btn-primary" type="submit" style="margin-top:0.75rem">Сохранить</button>
              </form>`
                  : `<div class="panel">
                ${profile.status_message ? `<p>${esc(profile.status_message)}</p>` : ""}
                ${profile.fav_rod ? `<p>Любимая удочка: <strong>${esc(profile.fav_rod)}</strong></p>` : ""}
                ${socials.length ? `<div class="profile-socials">${socials.join("")}</div>` : ""}
                <p class="profile-bio">${esc(profile.bio || "Пока пусто.")}</p>
              </div>`
              }
            </section>
          </div>
        </div>
      </div>`;

    function showTab(id) {
      root.querySelectorAll("[data-lk-pane]").forEach((p) => {
        p.hidden = p.dataset.lkPane !== id;
      });
      root.querySelectorAll(".lk-nav a").forEach((a) => {
        if (a.getAttribute("href") === "#" + id) a.setAttribute("aria-current", "page");
        else a.removeAttribute("aria-current");
      });
      if (id === "skin") maybeSkinMount();
      else pauseSkinViewer();
    }

    root.addEventListener("click", (e) => {
      const topupBtn = e.target.closest("#lkTopupBtn");
      if (topupBtn) {
        e.preventDefault();
        openTopupModal(100);
        return;
      }
      const buyCoinsBtn = e.target.closest("#lkBuyCoinsBtn");
      if (buyCoinsBtn) {
        e.preventDefault();
        openBuyCoinsModal();
        return;
      }
      const a = e.target.closest("a[href^='#']");
      if (!a) return;
      const id = a.getAttribute("href").slice(1);
      if (!LK_TABS.some((t) => t.id === id)) return;
      e.preventDefault();
      history.replaceState(null, "", "#" + id);
      showTab(id);
    });

    window.addEventListener("hashchange", () => showTab(lkTab()));

    async function saveProfile(form) {
      const fd = new FormData(form);
      await api(`/api/profiles/${encodeURIComponent(profile.nick)}`, {
        method: "PATCH",
        body: JSON.stringify({
          bio: fd.get("bio") ?? profile.bio,
          theme: fd.get("theme") ?? theme,
          status_message: fd.get("status_message") ?? profile.status_message,
          fav_rod: fd.get("fav_rod") ?? profile.fav_rod,
          social_tg: fd.get("social_tg") ?? profile.social_tg,
          social_vk: fd.get("social_vk") ?? profile.social_vk,
          social_discord: fd.get("social_discord") ?? profile.social_discord,
        }),
      });
      toast("Сохранено");
      if (soundOn) playTone("ok");
    }

    $("#profile-edit")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        await saveProfile(e.currentTarget);
      } catch (err) {
        toast(err.message || "Не удалось сохранить");
      }
    });
    $("#profile-edit-theme")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        const fd = new FormData(e.currentTarget);
        await api(`/api/profiles/${encodeURIComponent(profile.nick)}`, {
          method: "PATCH",
          body: JSON.stringify({
            bio: profile.bio,
            theme: fd.get("theme"),
            status_message: profile.status_message,
            fav_rod: profile.fav_rod,
            social_tg: profile.social_tg,
            social_vk: profile.social_vk,
            social_discord: profile.social_discord,
          }),
        });
        toast("Тема сохранена");
        if (soundOn) playTone("ok");
        root.querySelector(".lk-hero")?.classList.remove(...THEMES.map((t) => t.id));
        root.querySelector(".lk-hero")?.classList.add("profile-cover", fd.get("theme"));
      } catch (err) {
        toast(err.message || "Не удалось сохранить");
      }
    });

    $("#password-change")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const errEl = root.querySelector("[data-pw-error]");
      if (errEl) errEl.textContent = "";
      const fd = new FormData(e.currentTarget);
      if (fd.get("next") !== fd.get("next2")) {
        if (errEl) errEl.textContent = "Пароли не совпадают";
        return;
      }
      try {
        await api("/api/password", {
          method: "POST",
          body: JSON.stringify({ old: fd.get("old"), next: fd.get("next") }),
        });
        toast("Пароль изменён");
        if (soundOn) playTone("ok");
        e.currentTarget.reset();
      } catch (err) {
        if (errEl) errEl.textContent = err.message || "Не удалось сменить пароль";
        toast(err.message || "Не удалось сменить пароль");
      }
    });

    let look = { urls: {} };
    try {
      look = await api(`/api/skins/${encodeURIComponent(profile.nick)}`);
    } catch {
      look = { urls: {} };
    }
    const skinUrlNow = look.urls?.skin;
    if (skinUrlNow) paintSkinCanvas(root.querySelector("[data-look-canvas='skin']"), skinUrlNow);
    const capeImg = root.querySelector("[data-look-img='cape']");
    if (capeImg && look.urls?.cape) capeImg.src = look.urls.cape;
    const avImg = root.querySelector("[data-look-img='avatar']");
    if (avImg) {
      avImg.src = look.urls?.avatar || avatarSrc(profile.nick);
      avImg.onerror = () => {
        avImg.onerror = null;
        avImg.src = "/assets/images/avatar_default.png";
      };
    }

    const fallbackSkinUrl = `/api/skins/${encodeURIComponent(profile.nick)}/skin`;
    let skinState = null;

    function pauseSkinViewer() {
      if (skinState) skinState.viewer.renderPaused = true;
    }

    function maybeSkinMount() {
      const pane = root.querySelector("[data-lk-pane='skin']");
      const host = root.querySelector("[data-look-host='skin']");
      if (!pane || !host) return;
      if (pane.hidden) {
        pauseSkinViewer();
        return;
      }
      if (skinState) {
        skinState.viewer.renderPaused = false;
        return;
      }
      const canvas = document.createElement("canvas");
      canvas.className = "skin-viewer";
      canvas.setAttribute("role", "img");
      canvas.setAttribute("aria-label", "3D-превью скина");
      loadSkinview3d()
        .then((sv) => {
          if (!host.isConnected) return;
          const viewer = new sv.SkinViewer({
            canvas,
            width: Math.max(280, host.clientWidth - 24),
            height: 400,
          });
          viewer.controls.enableZoom = true;
          viewer.controls.enablePan = false;
          viewer.zoom = 1.25;
          if (!REDUCED_MOTION.matches) {
            viewer.autoRotate = true;
            viewer.autoRotateSpeed = 1.4;
            viewer.animation = new sv.WalkingAnimation();
            viewer.animation.speed = 0.7;
          }
          viewer.loadSkin(skinUrlNow || fallbackSkinUrl).catch(() => {});
          if (look.urls?.cape) viewer.loadCape(look.urls.cape).catch(() => {});
          host.classList.add("has-3d");
          host.appendChild(canvas);
          skinState = { viewer, host };
          const zoomBox = document.createElement("div");
          zoomBox.className = "skin-zoom";
          zoomBox.innerHTML = `
            <button type="button" class="skin-zoom-btn" data-zoom="in" aria-label="Приблизить">+</button>
            <button type="button" class="skin-zoom-btn" data-zoom="out" aria-label="Отдалить">−</button>
            <button type="button" class="skin-zoom-btn" data-zoom="reset" aria-label="Сбросить масштаб">⟲</button>`;
          zoomBox.querySelectorAll("[data-zoom]").forEach((b) => {
            b.addEventListener("pointerdown", (e) => e.stopPropagation());
            b.addEventListener("click", (e) => {
              e.stopPropagation();
              const dir = b.getAttribute("data-zoom");
              if (dir === "reset") {
                viewer.zoom = 1.25;
                return;
              }
              const z = viewer.zoom * (dir === "in" ? 1.25 : 0.8);
              viewer.zoom = Math.min(4, Math.max(0.4, z));
            });
          });
          host.appendChild(zoomBox);
          let downX = 0;
          let downY = 0;
          let moved = false;
          host.addEventListener("pointerdown", (e) => {
            downX = e.clientX;
            downY = e.clientY;
            moved = false;
          });
          host.addEventListener("pointermove", (e) => {
            if (e.buttons && (Math.abs(e.clientX - downX) > 6 || Math.abs(e.clientY - downY) > 6)) moved = true;
          });
          host.addEventListener("pointerup", () => {
            host._dragMoved = moved;
          });
          // Буфер канваса должен совпадать с отображаемым размером, иначе картинка плывёт.
          const fit = () => {
            const w = canvas.clientWidth;
            const h = canvas.clientHeight;
            if (w > 80 && h > 80) {
              viewer.width = w;
              viewer.height = h;
            }
          };
          if (window.ResizeObserver) new ResizeObserver(fit).observe(canvas);
          fit();
        })
        .catch(() => {
          /* WebGL или скрипт недоступны — остаётся плоское 2D-превью */
        });
    }
    maybeSkinMount();

    async function sendLook(kind, file) {
      const rule = LOOK_RULES[kind];
      const uploadBtn = root.querySelector(`[data-look-upload="${kind}"]`);
      const btnLabel = uploadBtn ? uploadBtn.textContent : null;
      setLookStatus(root, kind, "Загрузка…");
      if (uploadBtn) {
        uploadBtn.disabled = true;
        uploadBtn.textContent = "Загрузка…";
      }
      try {
        if (file.size > rule.max) throw new Error("Файл слишком большой");
        if (kind === "avatar" && !/^image\/(png|jpeg)$/.test(file.type)) throw new Error("Нужен PNG или JPEG");
        if (kind !== "avatar" && file.type !== "image/png") throw new Error("Нужен PNG");
        if (rule.dims) {
          const dims = await fileImageSize(file);
          if (!dims || !rule.dims.some(([w, h]) => w === dims.w && h === dims.h)) {
            throw new Error(kind === "skin" ? "Скин: PNG 64×64, 64×32 или 128×128" : "Плащ: PNG 64×32 или 128×64");
          }
        }
        const form = new FormData();
        form.append("kind", kind);
        form.append("file", file);
        const res = await apiUpload("/api/skins", form);
        if (kind === "skin") {
          paintSkinCanvas(root.querySelector("[data-look-canvas='skin']"), res.url);
          if (skinState) skinState.viewer.loadSkin(res.url).catch(() => {});
          setLookStatus(root, kind, "Готово! Скин сохранён и синхронизируется с игрой на сервере.", "ok");
        } else {
          setLookStatus(root, kind, "Сохранено", "ok");
        }
        if (kind === "cape") {
          if (capeImg) capeImg.src = res.url;
          if (skinState) skinState.viewer.loadCape(res.url).catch(() => {});
        }
        if (kind === "avatar") {
          const bust = res.url || avatarSrc(profile.nick, String(Date.now()));
          const hero = root.querySelector(".lk-hero-id img");
          if (hero) hero.src = bust;
          if (avImg) avImg.src = bust;
          document.querySelectorAll(".lead-avatar, .lot-seller-avatar").forEach((img) => {
            if (img.alt === profile.nick) img.src = bust;
          });
          setLookStatus(root, kind, "Аватарка успешно сохранена!", "ok");
        }
        toast("Загружено");
        if (soundOn) playTone("ok");
      } catch (err) {
        setLookStatus(root, kind, err.message || "Не удалось загрузить", "err");
        toast(err.message || "Не удалось загрузить");
      } finally {
        if (uploadBtn) {
          uploadBtn.disabled = false;
          uploadBtn.textContent = btnLabel || "Загрузить";
        }
      }
    }

    function bindKind(kind) {
      const fileInput = root.querySelector(`[data-look-file="${kind}"]`);
      const drop = root.querySelector(`[data-look-drop="${kind}"]`);
      root.querySelector(`[data-look-upload="${kind}"]`)?.addEventListener("click", () => fileInput?.click());
      drop?.addEventListener("click", () => {
        if (drop._dragMoved) {
          drop._dragMoved = false;
          return;
        }
        fileInput?.click();
      });
      drop?.addEventListener("keydown", (e) => {
        if (e.target.closest(".skin-zoom")) return;
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          fileInput?.click();
        }
      });
      fileInput?.addEventListener("change", () => {
        const f = fileInput.files?.[0];
        if (f) sendLook(kind, f);
        fileInput.value = "";
      });
      ["dragenter", "dragover"].forEach((ev) => {
        drop?.addEventListener(ev, (e) => {
          e.preventDefault();
          drop.classList.add("is-over");
        });
      });
      ["dragleave", "drop"].forEach((ev) => {
        drop?.addEventListener(ev, (e) => {
          e.preventDefault();
          drop.classList.remove("is-over");
        });
      });
      drop?.addEventListener("drop", (e) => {
        const f = e.dataTransfer?.files?.[0];
        if (f) sendLook(kind, f);
      });
      root.querySelector(`[data-look-delete="${kind}"]`)?.addEventListener("click", async () => {
        if (!confirm(kind === "skin" ? "Снять скин с сайта и в игре?" : "Удалить файл?")) return;
        try {
          await api("/api/skins", { method: "DELETE", body: JSON.stringify({ kind }) });
          toast("Удалено");
          if (kind === "skin") {
            const c = root.querySelector("[data-look-canvas='skin']");
            c?.getContext("2d")?.clearRect(0, 0, c.width, c.height);
            if (skinState) skinState.viewer.loadSkin(fallbackSkinUrl).catch(() => {});
          }
          if (kind === "cape") {
            if (capeImg) capeImg.removeAttribute("src");
            if (skinState) skinState.viewer.loadCape(null).catch(() => {});
          }
          if (kind === "avatar" && avImg) avImg.src = skinUrl(profile.nick);
          setLookStatus(root, kind, kind === "skin" ? "Скин снят. В игре вернётся прежний после входа." : "Удалено", "ok");
        } catch (err) {
          toast(err.message || "Не удалось удалить");
        }
      });
    }
    if (mine) {
      ["avatar", "skin", "cape"].forEach(bindKind);

      const importInput = root.querySelector("[data-skin-import-input]");
      const importBtn = root.querySelector("[data-skin-import-btn]");
      if (importBtn && importInput) {
        const doImport = async () => {
          const val = importInput.value.trim();
          if (!val) return;
          setLookStatus(root, "skin", "Получение скина…");
          importBtn.disabled = true;
          try {
            let skinFetchUrl = val;
            if (!/^https?:\/\//i.test(val)) {
              setLookStatus(root, "skin", "Нужна ссылка на PNG или файл с компьютера", "err");
              importBtn.disabled = false;
              return;
            }
            const resp = await fetch(skinFetchUrl);
            if (!resp.ok) throw new Error("Не удалось скачать скин по указанному источнику");
            const blob = await resp.blob();
            const file = new File([blob], "skin.png", { type: "image/png" });
            await sendLook("skin", file);
            importInput.value = "";
          } catch (err) {
            setLookStatus(root, "skin", err.message || "Ошибка импорта скина", "err");
            toast(err.message || "Ошибка импорта скина");
          } finally {
            importBtn.disabled = false;
          }
        };
        importBtn.addEventListener("click", doImport);
        importInput.addEventListener("keydown", (e) => {
          if (e.key === "Enter") {
            e.preventDefault();
            doImport();
          }
        });
      }
    }

    const likeBtn = $("#btn-like-profile", root);
    likeBtn?.addEventListener("click", async () => {
      if (!user) {
        toast("Войди, чтобы похвалить");
        return;
      }
      try {
        const res = await api(`/api/profiles/${encodeURIComponent(profile.nick)}/like`, {
          method: "POST",
          body: "{}",
        });
        if (res.ok) {
          likeBtn.classList.toggle("liked", res.liked);
          likeBtn.querySelector(".heart").innerHTML = heartSvg(res.liked);
          $("#like-count", root).textContent = res.likes;
          toast(res.liked ? "Похвалили" : "Лайк убран");
        }
      } catch (err) {
        toast(err.message || "Не удалось поставить лайк");
      }
    });
  }

  async function initProfile() {
    const root = $("#profile-root");
    if (!root) return;
    const params = new URLSearchParams(location.search);
    const user = getUser();
    const nick = params.get("nick") || params.get("u") || user?.nick;
    if (!nick) {
      root.innerHTML = `
        <div class="panel" style="max-width:560px;margin:2rem auto;text-align:center;padding:2.5rem 2rem;background:linear-gradient(180deg,rgba(8,28,44,0.85),rgba(4,16,26,0.95));border:1px solid rgba(47,224,192,0.3);border-radius:24px">
          <div style="font-size:2.8rem;margin-bottom:0.75rem">👤</div>
          <h2 style="margin:0 0 0.5rem;color:#f8fafc">Личный кабинет AquaTech</h2>
          <p class="muted-line" style="margin-bottom:1.5rem;font-size:0.95rem">Войди в свой аккаунт, чтобы загрузить скин, плащ и аватарку для игры на сервере, либо найди игрока по нику.</p>
          <div style="display:flex;gap:0.75rem;justify-content:center;flex-wrap:wrap">
            <a class="btn btn-primary" href="login.html">Войти в аккаунт</a>
            <a class="btn btn-secondary" href="register.html">Создать аккаунт</a>
          </div>
          <div style="margin-top:1.5rem;padding-top:1.25rem;border-top:1px solid rgba(255,255,255,0.08)">
            <form id="profile-search-form" style="display:flex;gap:0.5rem;max-width:320px;margin:0 auto">
              <input type="text" id="profile-search-nick" placeholder="Ник игрока..." style="flex:1;padding:0.5rem 0.75rem;border-radius:8px;background:rgba(0,0,0,0.3);border:1px solid rgba(255,255,255,0.15);color:#fff" required>
              <button class="btn btn-ghost" type="submit">Открыть</button>
            </form>
          </div>
        </div>`;
      root.querySelector("#profile-search-form")?.addEventListener("submit", (e) => {
        e.preventDefault();
        const n = root.querySelector("#profile-search-nick")?.value.trim();
        if (n) location.href = `profile.html?nick=${encodeURIComponent(n)}`;
      });
      return;
    }

    root.innerHTML = `<p class="muted-line">Загрузка профиля…</p>`;
    try {
    let profile = null;
    try {
      const data = await api(`/api/profiles/${encodeURIComponent(nick)}`);
      profile = data.profile;
    } catch {
      profile = null;
    }

    if (!profile) {
      const found = FALLBACK_PLAYERS.find((p) => p.nick.toLowerCase() === nick.toLowerCase());
      if (found) {
        profile = { ...found, views: 240, has_liked: false };
      } else {
        profile = {
          nick: nick,
          privilege: "Игрок AquaTech",
          bio: "Исследователь океанских глубин и кастомной рыбалки AquaTech.",
          theme: "ocean",
            likes: 0,
            fish: 0,
            coins: 0,
            views: 1,
          has_liked: false,
            badges: ["Новичок"],
        };
      }
    }

    const mine = user && user.nick.toLowerCase() === profile.nick.toLowerCase();
    const theme = profile.theme || "ocean";
      const rubBalance = Number(user?.rub_balance || 0);
      if (mine) {
        api("/api/me")
          .then((me) => {
            const rub = Number(me.rub_balance || 0);
            if (me.user) setUser({ ...getUser(), ...me.user, rub_balance: rub });
            renderHeader();
          })
          .catch(() => {});
      }

    const socials = [];
    if (profile.social_tg) {
      const tg = profile.social_tg.replace(/^@/, "");
      socials.push(`<a class="social-chip" href="https://t.me/${encodeURIComponent(tg)}" target="_blank" rel="noopener">Telegram: @${tg}</a>`);
    }
    if (profile.social_vk) {
      const vk = profile.social_vk.replace(/^(https?:\/\/)?(vk\.com\/)?/, "");
      socials.push(`<a class="social-chip" href="https://vk.com/${encodeURIComponent(vk)}" target="_blank" rel="noopener">VK: ${vk}</a>`);
    }
    if (profile.social_discord) {
      socials.push(`<span class="social-chip">Discord: ${profile.social_discord}</span>`);
    }

      await renderOwnCabinet(root, profile, user, theme, socials, mine, rubBalance);
      document.body.classList.add("profile-ready");
    } catch (err) {
    root.innerHTML = `
        <div class="panel" style="max-width:32rem;margin:0 auto;text-align:center;padding:1.5rem">
          <h2 style="margin:0 0 0.5rem">Не удалось открыть профиль</h2>
          <p class="muted-line">${esc(err.message || "Ошибка загрузки")}</p>
          <button class="btn btn-primary" type="button" id="profile-retry">Повторить</button>
        </div>`;
      root.querySelector("#profile-retry")?.addEventListener("click", () => location.reload());
    }
  }

  function initReset() {
    const form = $("#reset-form");
    if (!form) return;
    const emailField = $("#reset-email-field");
    const codeForm = $("#reset-code-form");
    const claimForm = $("#reset-claim-form");
    const supportBox = $("#reset-step-support");
    const backBtn = $("#reset-back-btn");
    let nick = "";

    function resetError(text) {
      document.querySelectorAll("[data-reset-error]").forEach((el) => {
        el.textContent = text || "";
      });
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      nick = ($("#reset-nick")?.value || "").trim();
      const email = ($("#reset-email")?.value || "").trim();
      resetError("");

      try {
        const data = await api(`/api/auth/nick?nick=${encodeURIComponent(nick)}`);
        if (data.unclaimed || data.exists === false) {
          form.hidden = true;
          claimForm.hidden = false;
          $("#reset-claim-title").textContent = `Задай пароль для ${nick}`;
          $("#reset-password")?.focus();
          return;
        }

        // Account is registered/claimed: trigger forgot-password
        if (emailField.hidden && !email) {
          emailField.hidden = false;
          $("#reset-email")?.focus();
          toast("Введи Email, привязанный к аккаунту");
          return;
        }

        const res = await api("/api/auth/forgot-password", {
          method: "POST",
          body: JSON.stringify({ nick, email }),
        });

        form.hidden = true;
        codeForm.hidden = false;
        supportBox.hidden = false;
        $("#reset-code-title").textContent = `Сброс пароля для ${nick}`;
        $("#reset-code-subtitle").textContent = res.emailMasked
          ? `Код отправлен на ${res.emailMasked}`
          : "Код подтверждения отправлен на почту.";
        $("#reset-verify-code")?.focus();
      } catch (err) {
        resetError(err.message || "Не удалось отправить запрос на сброс");
        if (emailField.hidden) emailField.hidden = false;
      }
    });

    backBtn?.addEventListener("click", () => {
      codeForm.hidden = true;
      supportBox.hidden = true;
      form.hidden = false;
      resetError("");
    });

    codeForm?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.currentTarget);
      const code = String(fd.get("code") || "").trim();
      const p1 = String(fd.get("password") || "");
      const p2 = String(fd.get("password2") || "");

      if (p1 !== p2) {
        resetError("Пароли не совпадают");
        return;
      }

      resetError("");
      try {
        await api("/api/auth/reset-password", {
          method: "POST",
          body: JSON.stringify({
            code,
            password: p1,
          }),
        });
        toast("Пароль успешно изменён! Теперь войди.");
        setTimeout(() => {
          location.href = "login.html";
        }, 800);
      } catch (err) {
        resetError(err.message || "Неверный код подтверждения");
      }
    });

    claimForm?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.currentTarget);
      if (fd.get("password") !== fd.get("password2")) {
        resetError("Пароли не совпадают");
        return;
      }
      try {
        await api("/api/register", {
          method: "POST",
          body: JSON.stringify({ nick, password: fd.get("password") }),
        });
        toast("Пароль задан! Теперь войди.");
        setTimeout(() => {
          location.href = "login.html";
        }, 600);
      } catch (err) {
        resetError(err.message || "Не удалось задать пароль");
      }
    });
  }

  function initAuth() {
    const login = $("#login-form");
    const reg = $("#register-form");
    const params = new URLSearchParams(location.search);
    const launcherPort = params.get("port") || "12450";
    const fromLauncher = params.get("launcher") === "1";

    async function finishLauncherLogin(userNick) {
      if (!fromLauncher) return false;
      try {
        const data = await api("/api/launcher/session", {
          headers: { "x-aquatech-launcher": "1" },
        });
        if (!data.session) return false;
        const nick = encodeURIComponent(data.user?.nick || userNick || "");
        location.href = `http://127.0.0.1:${launcherPort}/api/portal_callback?session=${encodeURIComponent(data.session)}&nick=${nick}`;
        return true;
      } catch (_) {
        return false;
      }
    }

    if (fromLauncher && getUser()) {
      finishLauncherLogin(getUser().nick);
    }

    login?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(login);
      const nick = String(fd.get("nick") || "").trim();
      const password = String(fd.get("password") || "");
      const errBox = login.querySelector("[data-auth-error]");
      const submitBtn = login.querySelector('button[type="submit"]');
      if (errBox) errBox.textContent = "";
      if (submitBtn) submitBtn.disabled = true;
      try {
        const data = await api("/api/login", {
          method: "POST",
          body: JSON.stringify({ nick, password }),
          headers: fromLauncher ? { "x-aquatech-launcher": "1" } : {},
        });
        setUser(data.user);
        toast("Вход выполнен");
        if (fromLauncher) {
          if (data.session) {
            location.href = `http://127.0.0.1:${launcherPort}/api/portal_callback?session=${encodeURIComponent(data.session)}&nick=${encodeURIComponent(data.user.nick)}`;
            return;
          }
          if (await finishLauncherLogin(data.user.nick)) return;
        }
        location.href = `profile.html?u=${encodeURIComponent(data.user.nick)}`;
      } catch (err) {
        if (submitBtn) submitBtn.disabled = false;
        if (apiAvailable === false || isMirrorHost()) {
          location.href = `${CANONICAL}/login.html`;
          return;
        }
        const msg = err.message || "Ошибка входа";
        if (errBox) errBox.textContent = msg;
        toast(msg);
      }
    });

    reg?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(reg);
      const nick = String(fd.get("nick") || "").trim();
      const password = String(fd.get("password") || "");
      const errBox = reg.querySelector("[data-auth-error]");
      const submitBtn = reg.querySelector('button[type="submit"]');
      if (errBox) errBox.textContent = "";
      if (submitBtn) submitBtn.disabled = true;
      try {
        const data = await api("/api/register", {
          method: "POST",
          body: JSON.stringify({ nick, password }),
        });
        setUser(data.user);
        toast("Аккаунт создан");
        location.href = `profile.html?u=${encodeURIComponent(data.user.nick)}`;
      } catch (err) {
        if (submitBtn) submitBtn.disabled = false;
        if (apiAvailable === false || isMirrorHost()) {
          location.href = `${CANONICAL}/register.html`;
          return;
        }
        const msg = err.message || "Ошибка регистрации";
        if (errBox) errBox.textContent = msg;
        toast(msg);
      }
    });
  }

  const CASE_LOOT_TABLES = {
    ocean: [
      { name: "AquaCoins ×60–120", rarity: "common", rarityLabel: "Обычный", chance: "20%" },
      { name: "Железная руда ×4–8", rarity: "common", rarityLabel: "Обычный", chance: "15%" },
      { name: "Оловянная руда ×3–6", rarity: "common", rarityLabel: "Обычный", chance: "14%" },
      { name: "Редстоун ×4–8", rarity: "common", rarityLabel: "Обычный", chance: "12%" },
      { name: "Медные слитки ×4–8", rarity: "common", rarityLabel: "Обычный", chance: "12%" },
      { name: "Лазурит ×3–6", rarity: "common", rarityLabel: "Обычный", chance: "10%" },
      { name: "Слизкие шары ×4–9", rarity: "common", rarityLabel: "Обычный", chance: "9%" },
      { name: "Нить ×4–8", rarity: "common", rarityLabel: "Обычный", chance: "4%" },
      { name: "Бутыльки опыта ×4–8", rarity: "rare", rarityLabel: "Редкий", chance: "4%" },
    ],
    fisher: [
      { name: "Серебряная руда ×2–4", rarity: "common", rarityLabel: "Обычный", chance: "15%" },
      { name: "Железная руда ×6–12", rarity: "common", rarityLabel: "Обычный", chance: "13%" },
      { name: "Алюминиевая руда ×2–4", rarity: "common", rarityLabel: "Обычный", chance: "13%" },
      { name: "Сапфир ×1–2", rarity: "rare", rarityLabel: "Редкий", chance: "12%" },
      { name: "Вольфрам ×1–3", rarity: "rare", rarityLabel: "Редкий", chance: "11%" },
      { name: "Хром ×1–3", rarity: "rare", rarityLabel: "Редкий", chance: "11%" },
      { name: "Кобальт ×1–2", rarity: "rare", rarityLabel: "Редкий", chance: "8%" },
      { name: "Бутыльки опыта ×8–16", rarity: "common", rarityLabel: "Обычный", chance: "6%" },
      { name: "Ледяная удочка [T6]", rarity: "epic", rarityLabel: "Эпический", chance: "4%" },
      { name: "Удочка Ловца Звёзд [T7]", rarity: "epic", rarityLabel: "Эпический", chance: "3%" },
      { name: "Лазурный кристалл [T8]", rarity: "epic", rarityLabel: "Эпический", chance: "2%" },
      { name: "Акулий клык [T9]", rarity: "legendary", rarityLabel: "Легендарный", chance: "2%" },
    ],
    depth: [
      { name: "Платина ×2–4", rarity: "rare", rarityLabel: "Редкий", chance: "13%" },
      { name: "Алмазы ×2–4", rarity: "rare", rarityLabel: "Редкий", chance: "12%" },
      { name: "Дроблёный уран ×1–3", rarity: "rare", rarityLabel: "Редкий", chance: "11%" },
      { name: "Инконель ×1–2", rarity: "rare", rarityLabel: "Редкий", chance: "11%" },
      { name: "Гемы ×2–3", rarity: "rare", rarityLabel: "Редкий", chance: "9%" },
      { name: "Сердце моря ×1", rarity: "epic", rarityLabel: "Эпический", chance: "8%" },
      { name: "Осмиридий ×1–2", rarity: "epic", rarityLabel: "Эпический", chance: "8%" },
      { name: "Адамантиевая руда ×1–2", rarity: "epic", rarityLabel: "Эпический", chance: "7%" },
      { name: "Светящаяся ягода [T11]", rarity: "epic", rarityLabel: "Эпический", chance: "6%" },
      { name: "Обсидиановая [T10]", rarity: "epic", rarityLabel: "Эпический", chance: "5%" },
      { name: "Магматическая [T12]", rarity: "legendary", rarityLabel: "Легендарный", chance: "4%" },
      { name: "Альфа [T13]", rarity: "legendary", rarityLabel: "Легендарный", chance: "3%" },
      { name: "Звезда Незера ×1", rarity: "legendary", rarityLabel: "Легендарный", chance: "3%" },
    ],
  };

  const CASE_ICONS = {
    starter: "assets/images/cases/starter.png",
    smeltery: "assets/images/cases/smeltery.png",
    steam: "assets/images/cases/steam.png",
    flora: "assets/images/cases/flora.png",
    applied: "assets/images/cases/applied.png",
    abyss: "assets/images/cases/abyss.png",
    superconductor: "assets/images/cases/superconductor.png",
    singularity: "assets/images/cases/singularity.png",
    draconic: "assets/images/cases/draconic.png",
    infinity: "assets/images/cases/infinity.png",
    metallurgy: "assets/images/cases/metallurgy.png",
  };

  function getItemIconUrl(itemId) {
    if (!itemId) return "";
    const safe = String(itemId).replace(/[^a-zA-Z0-9_]/g, "_");
    return `assets/images/items/${safe}.png`;
  }

  const RARITY_LABEL = {
    common: "Обычный",
    uncommon: "Необычный",
    rare: "Редкий",
    epic: "Эпический",
    legendary: "Легендарный",
    mythic: "Мифический",
    exotic: "Экзотический",
  };

  function openLootModal(slug) {
    const live = (window.__liveCases || []).find((c) => c.slug === slug);
    if (live) return openLiveLootModal(live);
    const item = (FALLBACK_CATALOG.case || []).find((c) => c.slug === slug);
    const loot = CASE_LOOT_TABLES[slug] || [];
    if (!item) return;

    let modal = document.getElementById("loot-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "loot-modal";
      modal.className = "loot-modal-overlay";
      document.body.appendChild(modal);
    }

    modal.innerHTML = `
      <div class="loot-modal-card">
        <div class="loot-modal-header">
          <div>
            <span class="tag ${slug === 'depth' ? 'gold' : ''}">Дроп кейса</span>
            <h3>${item.title}</h3>
          </div>
          <button class="loot-modal-close" type="button" aria-label="Закрыть">✕</button>
        </div>
        <p style="color:var(--muted);margin-bottom:1.25rem">${item.description}</p>
        <div class="loot-items-list">
          ${loot
            .map(
              (l) => `
            <div class="loot-item-row">
              <div class="loot-item-info">
                <span class="rarity-badge rarity-${l.rarity}">${l.rarityLabel}</span>
                <span class="loot-item-name">${l.name}</span>
              </div>
              <div class="loot-item-chance">${l.chance}</div>
            </div>`
            )
            .join("")}
        </div>
        <div style="margin-top:1.5rem;text-align:center">
          <small style="color:var(--muted)">Открытие кейсов происходит в игре на сервере AquaTech (клавиша F4)</small>
        </div>
      </div>
    `;

    modal.classList.add("open");
    modal.querySelector(".loot-modal-close")?.addEventListener("click", () => modal.classList.remove("open"));
    modal.addEventListener("click", (e) => {
      if (e.target === modal) modal.classList.remove("open");
    });
  }

  function openLiveLootModal(c) {
    let modal = document.getElementById("loot-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "loot-modal";
      modal.className = "loot-modal-overlay";
      document.body.appendChild(modal);
    }
    const iconSrc = CASE_ICONS[c.slug] || CASE_ICONS.starter || "assets/logo.png";
    const rows = [...c.loot].sort((a, b) => b.weight - a.weight);
    const coinSvg = '<img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt="">';
    const spinSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 1 10 10"/><path d="M12 6v6l4 2"/></svg>';

    modal.innerHTML = `
      <div class="loot-modal-card apple-modal-box">
        <div class="loot-modal-header" style="border-bottom:1px solid rgba(255,255,255,0.08);padding-bottom:1.15rem;margin-bottom:1.15rem;display:flex;align-items:center;justify-content:space-between">
          <div style="display:flex;align-items:center;gap:1.1rem">
            <div style="width:54px;height:54px;border-radius:14px;background:radial-gradient(circle, rgba(47,224,192,0.22) 0%, rgba(255,255,255,0.03) 70%);border:1px solid rgba(47,224,192,0.28);display:flex;align-items:center;justify-content:center;flex-shrink:0">
              <img src="${iconSrc}" alt="${esc(c.title)}" style="width:42px;height:42px;object-fit:contain;filter:drop-shadow(0 4px 8px rgba(0,0,0,0.5))" />
            </div>
            <div>
              <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.25rem">
                <span class="rarity-badge rarity-${esc(c.rarity)}">${RARITY_LABEL[c.rarity] || esc(c.rarity)}</span>
              </div>
              <h3 style="font-size:1.3rem;font-weight:800;color:#f8fafc;margin:0;letter-spacing:-0.01em">${esc(c.title)}</h3>
            </div>
          </div>
          <button class="apple-modal-close" type="button" aria-label="Закрыть">✕</button>
        </div>

        <div style="display:flex;justify-content:space-between;align-items:center;background:rgba(255,255,255,0.04);border:1px solid rgba(255,255,255,0.08);border-radius:14px;padding:0.75rem 1.1rem;margin-bottom:1.15rem">
          <span style="font-size:0.88rem;color:#94a3b8;font-weight:500">Стоимость открытия:</span>
          <span style="font-size:1.1rem;font-weight:800;color:#ffd875;display:inline-flex;align-items:center;gap:0.4rem">
            ${coinSvg}
            <span>${Number(c.cost).toLocaleString("ru-RU")}</span>
          </span>
        </div>

        <div class="loot-items-list">
          ${(() => {
            const maxChance = Math.max(...rows.map((r) => Number(r.chance || 1)), 1);
            return rows
              .map((l) => {
                const iconUrl = getItemIconUrl(l.item);
                const chanceVal = Number(l.chance || 0);
                const progWidth = Math.min(100, Math.max(8, Math.round((chanceVal / maxChance) * 100)));
                const countText = l.min < l.max ? `${l.min}–${l.max} шт` : `${l.min} шт`;
                return `
                <div class="loot-item-row-apple">
                  <div class="loot-item-icon-box">
                    ${iconUrl
                      ? `<img class="loot-item-pixel-ico" src="${iconUrl}" alt="${esc(l.name)}" onerror="this.style.display='none';if(this.nextElementSibling)this.nextElementSibling.style.display='flex';" /><div class="loot-item-fallback" style="display:none;font-size:0.9rem;color:#2fe0c0">◆</div>`
                      : `<div class="loot-item-fallback" style="font-size:0.9rem;color:#2fe0c0">◆</div>`
                    }
                  </div>
                  <div style="flex:1;min-width:0">
                    <div style="display:flex;align-items:center;justify-content:space-between;gap:0.5rem">
                      <span style="font-weight:600;color:#f8fafc;font-size:0.9rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" title="${esc(l.name)}">${esc(l.name)}</span>
                      <span class="loot-count-pill">${countText}</span>
                    </div>
                    <div class="loot-item-prog-track">
                      <div class="loot-item-prog-fill" style="width:${progWidth}%"></div>
                    </div>
                  </div>
                  <div style="font-weight:800;font-size:0.95rem;color:#2fe0c0;min-width:44px;text-align:right">${l.chance}%</div>
                </div>
              `;
            })
            .join("");
          })()}
        </div>

        <div style="margin-top:1.25rem;display:flex;flex-direction:column;gap:0.6rem">
          <button class="btn-loot-modal-spin" id="lootModalSpinBtn" type="button">
            ${spinSvg}
            <span>Крутить барабан (${Number(c.cost).toLocaleString("ru-RU")} монет)</span>
          </button>
          <div style="text-align:center;font-size:0.78rem;color:#94a3b8">
            Открытие доступно на сайте и в игре на сервере AquaTech (клавиша <b>F4 → Кейсы</b>)
          </div>
        </div>
      </div>
    `;
    modal.classList.add("open");
    modal.querySelector(".apple-modal-close")?.addEventListener("click", () => modal.classList.remove("open"));
    modal.addEventListener("click", (e) => {
      if (e.target === modal) modal.classList.remove("open");
    });

    const spinBtn = modal.querySelector("#lootModalSpinBtn");
    if (spinBtn) {
      spinBtn.addEventListener("click", () => {
        modal.classList.remove("open");
        openRouletteModal(c.slug, c.title, c.cost);
      });
    }
  }

  function caseCard(c) {
    const top = [...c.loot].sort((a, b) => b.weight - a.weight).slice(0, 3);
    return `<article class="case-card reveal">
      <div class="case-card-head">
        <span class="rarity-badge rarity-${esc(c.rarity)}">${RARITY_LABEL[c.rarity] || esc(c.rarity)}</span>
        <span class="case-cost">${Number(c.cost).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt=""></span>
      </div>
      <h3>${esc(c.title)}</h3>
      <ul class="case-top">
        ${top
          .map(
            (l) => `<li><span>${esc(l.name)}</span><b>${l.chance}%</b></li>`
          )
          .join("")}
      </ul>
      <div style="display:flex;flex-direction:column;gap:0.5rem;margin-top:auto">
        <button class="btn btn-secondary" style="width:100%" type="button" data-view-loot="${esc(c.slug)}">Просмотр содержимого</button>
        <div style="text-align:center;font-size:0.75rem;color:var(--muted)">Открытие доступно в игре (клавиша F4)</div>
      </div>
    </article>`;
  }

  async function openCaseWeb(slug, btn) {
    const rawUser = localStorage.getItem("at_user");
    if (!rawUser) {
      toast("Войди в аккаунт, чтобы открывать кейсы за монеты");
      setTimeout(() => (location.href = "login.html"), 700);
      return;
    }
    const origText = btn.textContent;
    btn.disabled = true;
    btn.textContent = "Крутим…";

    const c = (window.__liveCases || []).find((x) => x.slug === slug);
    if (!c) {
      toast("Кейс не найден");
      btn.disabled = false;
      btn.textContent = origText;
      return;
    }

    let modal = document.getElementById("roulette-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "roulette-modal";
      modal.className = "roulette-modal-overlay";
      document.body.appendChild(modal);
    }

    const RARITY_COLORS = {
      common: "#94a3b8",
      uncommon: "#4ade80",
      rare: "#38bdf8",
      epic: "#c084fc",
      legendary: "#facc15",
      mythic: "#f43f5e",
      exotic: "#2fe0c0",
    };

    // Pre-populate 48 random tape tiles from the loot pool
    const lootPool = c.loot || [];
    const targetIdx = 34;
    const tiles = [];
    for (let i = 0; i < 46; i++) {
      const pick = lootPool[Math.floor(Math.random() * lootPool.length)] || {
        name: "Предмет",
        rarity: "common",
        min: 1,
      };
      tiles.push(pick);
    }

    modal.innerHTML = `
      <div class="roulette-box">
        <h3 style="margin:0 0 0.35rem;font-size:1.35rem;color:#F8FAFC">${esc(c.title)}</h3>
        <p style="color:var(--muted);font-size:0.9rem;margin:0" id="roulette-status">Крутим барабан…</p>
        <div class="roulette-reel" id="roulette-reel">
          <div class="roulette-marker"></div>
          <div class="roulette-strip" id="roulette-strip">
            ${tiles
              .map(
                (l) => `
              <div class="roulette-tile" style="border-color:${RARITY_COLORS[l.rarity || 'common']}33">
                <span class="rarity-badge rarity-${esc(l.rarity || 'common')}" style="transform:scale(0.85)">${RARITY_LABEL[l.rarity] || 'Лут'}</span>
                <span class="roulette-tile-name">${esc(l.name)}</span>
                <span class="roulette-tile-count">${l.min || 1} шт.</span>
              </div>`
              )
              .join("")}
          </div>
        </div>
        <div id="roulette-result-box" style="display:none;margin-top:1.2rem">
          <div style="background:rgba(255,255,255,0.04);border:1px solid rgba(255,255,255,0.1);border-radius:12px;padding:1.1rem;margin-bottom:1.2rem">
            <span class="rarity-badge" id="roulette-win-rarity"></span>
            <div id="roulette-win-name" style="font-size:1.7rem;font-weight:bold;color:var(--gold);margin-top:0.4rem"></div>
            <div id="roulette-win-amount" style="font-size:1.05rem;color:#E2E8F0;font-weight:600"></div>
          </div>
          <p style="color:#4ade80;font-weight:500;font-size:0.92rem;margin-bottom:1.2rem">
            ✓ Предмет автоматически отправлен в твой инвентарь на сервере!
          </p>
          <div style="display:flex;gap:0.75rem;justify-content:center">
            <button class="btn btn-primary" type="button" id="roulette-close-btn" style="min-width:140px">Забрать</button>
          </div>
        </div>
      </div>
    `;

    modal.classList.add("open");

    const stripEl = modal.querySelector("#roulette-strip");
    const reelEl = modal.querySelector("#roulette-reel");

    try {
      const res = await api("/api/cases/open", {
        method: "POST",
        body: JSON.stringify({ slug }),
      });

      if (!res.ok || !res.loot) {
        throw new Error(res.error || "Ошибка открытия");
      }

      // Embed winning tile into index 34
      tiles[targetIdx] = {
        name: res.loot.name,
        rarity: res.loot.rarity || c.rarity,
        min: res.loot.amount,
      };

      stripEl.innerHTML = tiles
        .map(
          (l, i) => `
        <div class="roulette-tile" id="tile-${i}" style="border-bottom-color:${RARITY_COLORS[l.rarity || 'common']}">
          <span class="rarity-badge rarity-${esc(l.rarity || 'common')}" style="transform:scale(0.85)">${RARITY_LABEL[l.rarity] || 'Лут'}</span>
          <span class="roulette-tile-name">${esc(l.name)}</span>
          <span class="roulette-tile-count">×${l.min || 1}</span>
        </div>`
        )
        .join("");

      // Calculate translation distance
      const tileWidth = 130;
      const markerX = reelEl.clientWidth / 2;
      const jitter = (Math.random() * 0.3 - 0.15) * tileWidth;
      const landDist = targetIdx * tileWidth + tileWidth / 2 - markerX + jitter;
      const landT0 = performance.now();
      const landDur = 3900;
      let lastTileIdx = -1;

      const loop = (now) => {
        const elapsed = now - landT0;
        const u = Math.min(1.0, elapsed / landDur);
        // Quintic smooth ease-out (authentic 60fps decelerating reel)
        const eased = 1.0 - Math.pow(1.0 - u, 5);
        const pos = landDist * eased;
        stripEl.style.transform = `translate3d(${-pos.toFixed(2)}px, 0, 0)`;

        const currentTileIdx = Math.floor((pos + markerX) / tileWidth);
        if (currentTileIdx !== lastTileIdx) {
          lastTileIdx = currentTileIdx;
          playTone("tick");
        }

        if (u < 1.0) {
          requestAnimationFrame(loop);
        } else {
          // Finished spin
          playTone("ok");
          const winTile = document.getElementById(`tile-${targetIdx}`);
          const winCol = RARITY_COLORS[res.loot.rarity || 'common'] || "#2fe0c0";
          if (winTile) {
            winTile.classList.add("win");
            winTile.style.borderColor = winCol;
            winTile.style.boxShadow = `0 0 35px ${winCol}aa, inset 0 0 20px ${winCol}33`;
          }

          modal.querySelector("#roulette-status").textContent = "Выигрыш получен!";
          modal.querySelector("#roulette-win-rarity").className = `rarity-badge rarity-${esc(res.loot.rarity || 'common')}`;
          modal.querySelector("#roulette-win-rarity").textContent = RARITY_LABEL[res.loot.rarity] || 'Выигрыш';
          modal.querySelector("#roulette-win-name").textContent = res.loot.name;
          modal.querySelector("#roulette-win-amount").textContent = `×${res.loot.amount} шт.`;
          modal.querySelector("#roulette-result-box").style.display = "block";
          toast(`Выигрыш: ${res.loot.name} ×${res.loot.amount}`);

          modal.querySelector("#roulette-close-btn").onclick = () => {
            modal.classList.remove("open");
          };
        }
      };

      requestAnimationFrame(loop);
    } catch (err) {
      modal.classList.remove("open");
      toast(err.message || "Ошибка открытия кейса");
    } finally {
      btn.disabled = false;
      btn.textContent = origText;
    }
  }

  async function initCasesLive() {
    const root = $("#cases-root");
    if (!root) return;
    root.innerHTML = `<p class="muted-line">Загрузка кейсов…</p>`;
    let cases = [];
    try {
      const data = await api("/data/cases.json");
      cases = data.cases || [];
    } catch {
      cases = [];
    }
    if (!cases.length) {
      await initCatalog("case");
      return;
    }
    window.__liveCases = cases;
    root.innerHTML = cases.map(caseCard).join("");
    root.querySelectorAll("[data-view-loot]").forEach((btn) => {
      btn.addEventListener("click", () => openLootModal(btn.getAttribute("data-view-loot")));
    });
    revealScan(root);
  }

  function catalogCard(item, kind) {
    const perks = (item.perks || [])
      .map((p) => `<li>${p}</li>`)
      .join("");
    const price =
      kind === "store"
        ? `<div class="price">${item.price_rub} ₽ <small>/ мес</small></div>`
        : `<div class="price" style="color:var(--muted);font-size:1rem">Только на сервере</div>`;
    const btnLabel = kind === "store" ? "Купить — скоро" : "Открыть — в игре (F4)";
    const lootBtn =
      kind === "case"
        ? `<button class="btn btn-aqua" style="margin-top:0.85rem;width:100%" type="button" data-view-loot="${item.slug}">Состав кейса</button>`
        : "";
    return `<div class="card catalog-card">
      <span class="tag ${item.slug === "deluxe" || item.slug === "ultimate" || item.slug === "depth" ? "gold" : ""}">${item.title}</span>
      <h3>${item.title}</h3>
      <p style="color:var(--muted);margin:.55rem 0 0">${item.description}</p>
      <ul class="perk-list">${perks}</ul>
      ${price}
      ${lootBtn}
      <button class="btn btn-secondary btn-disabled" style="margin-top:0.65rem" type="button" disabled title="Действие на сервере">${btnLabel}</button>
    </div>`;
  }

  const FALLBACK_CATALOG = {
    store: [
      {
        slug: "sailor",
        title: "Моряк",
        price_rub: 99,
        description: "Стартовая морская привилегия. Префикс [МОРЯК], 2 точки дома (/sethome), доступ к базовым удобствам.",
        perks: ["Префикс [МОРЯК] в чате", "2 точки дома /sethome", "Цветной ник", "Базовый морской набор"],
      },
      {
        slug: "skipper",
        title: "Шкипер",
        price_rub: 249,
        description: "Продвинутый мореплаватель. Префикс [ШКИПЕР], 3 точки дома, приоритетный вход на сервер.",
        perks: ["Префикс [ШКИПЕР] в чате", "3 точки дома /sethome", "Приоритетный вход на сервер", "Кит Шкипера в меню F4"],
      },
      {
        slug: "captain",
        title: "Капитан",
        price_rub: 499,
        description: "Командир корабля. Префикс [КАПИТАН], 5 точек дома, режим полёта /fly на приватах.",
        perks: ["Префикс [КАПИТАН] в чате", "Режим полёта /fly", "5 точек дома /sethome", "Множитель удачи x2", "Кит Капитана"],
      },
      {
        slug: "admiral",
        title: "Адмирал",
        price_rub: 899,
        description: "Верховный главнокомандующий флота. Префикс [АДМИРАЛ], 10 точек дома, /fly, /nick.",
        perks: ["Префикс [АДМИРАЛ] в чате", "Режим полёта /fly", "Смена ника /nick", "10 точек дома /sethome", "Множитель удачи x4", "Кит Адмирала"],
      },
      {
        slug: "legend",
        title: "Легенда",
        price_rub: 1499,
        description: "Высший статус на сервере AquaTech. Префикс [ЛЕГЕНДА], неограниченные дома, /fly, /hat, /nick.",
        perks: ["Префикс [ЛЕГЕНДА] в чате", "Режим полёта /fly везде", "Блок на голове /hat", "Смена ника /nick", "15 точек дома /sethome", "Максимальный множитель x8", "Эксклюзивный кейс Легенды"],
      },
      {
        slug: "vip",
        title: "VIP",
        price_rub: 199,
        description: "Классическая VIP-привилегия. Префикс [VIP], /fly, /wb, /ec, косметические эффекты.",
        perks: ["Префикс [VIP] в чате", "Виртуальный верстак /wb", "Эндер-сундук /ec", "Режим полёта /fly", "Косметика AquaLumen"],
      },
    ],
    case: [
      {
        slug: "ocean",
        title: "Океанский кейс",
        price_rub: 0,
        description: "Стартовые материалы прогрессии за 500 внутриигровых монет. Открывается в игре (F4).",
        perks: ["Железо, олово, медь", "Редстоун и лазурит", "Слизкие шары", "Откат 60–120 монет"],
      },
      {
        slug: "fisher",
        title: "Кейс рыбака",
        price_rub: 0,
        description: "Материалы середины прогрессии за 1500 монет и редкие удочки T6–T9.",
        perks: ["Серебро, алюминий, кобальт", "Сапфир, вольфрам, хром", "Удочка T6 — 4%", "Удочка T9 — 2%"],
      },
      {
        slug: "depth",
        title: "Кейс Бездны",
        price_rub: 0,
        description: "Поздние материалы за 5000 монет и топовые удочки T10–T13.",
        perks: ["Платина, уран, инконель", "Осмиридий и адамантит", "Сердце моря и звезда Незера", "Удочка T13 Альфа — 3%"],
      },
    ],
  };




  // ─── HOLOGRAPHIC OCEAN TERMINAL (HERO SHOWCASE) ───────────────────────────
  function initHeroHoloTerminal() {
    const terminal = $("#heroHoloTerminal");
    if (!terminal) return;
    const glass = terminal.querySelector(".holo-terminal-glass") || terminal;

    const data = {
      dredger: {
        imgId: "holoImgDredger",
        l1: "Глубина", v1: "-160м (Абиссаль)",
        l2: "Добыча", v2: "Титан & Незерит",
        l3: "КПД станции", v3: "120 RF/t · 100%",
        desc: "Дноуглубительный бур автоматической добычи руд и титана со дна океана."
      },
      fisher: {
        imgId: "holoImgFisher",
        l1: "Скорость", v1: "18 сек / цикл",
        l2: "Фильтрация", v2: "Industrial Upgrade",
        l3: "Наживка", v3: "Глубинный червь (+40%)",
        desc: "Автоматическая станция фильтрации и вылавливания ресурсов без вашего участия."
      },
      rod: {
        imgId: "holoImgRod",
        l1: "Тир", v1: "T13 Квантовый Капстоун",
        l2: "Улов", v2: "Осмиридий · Адамантит",
        l3: "Прочность", v3: "320 уловов (Усиленная)",
        desc: "Легендарная удочка XIII уровня StarCatcher для добычи редчайших элементов космоса."
      }
    };

    // 1. Tab Switching
    const tabs = terminal.querySelectorAll(".holo-tab");
    tabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        const key = tab.dataset.holoTab;
        if (!key || !data[key]) return;

        tabs.forEach((t) => t.classList.remove("active"));
        tab.classList.add("active");

        // Switch active image
        terminal.querySelectorAll(".holo-img").forEach((img) => img.classList.remove("active"));
        const targetImg = $(`#${data[key].imgId}`);
        if (targetImg) targetImg.classList.add("active");

        // Update telemetry
        const b1Label = $("#holoBadge1 .holo-badge-label");
        const b1Val = $("#holoVal1");
        const b2Label = $("#holoBadge2 .holo-badge-label");
        const b2Val = $("#holoVal2");
        const b3Label = $("#holoBadge3 .holo-badge-label");
        const b3Val = $("#holoVal3");
        const desc = $("#holoDesc");

        if (b1Label) b1Label.textContent = data[key].l1;
        if (b1Val) b1Val.textContent = data[key].v1;
        if (b2Label) b2Label.textContent = data[key].l2;
        if (b2Val) b2Val.textContent = data[key].v2;
        if (b3Label) b3Label.textContent = data[key].l3;
        if (b3Val) b3Val.textContent = data[key].v3;
        if (desc) desc.textContent = data[key].desc;
      });
    });

    // 2. 3D Parallax Tilt
    terminal.addEventListener("mousemove", (e) => {
      const rect = terminal.getBoundingClientRect();
      const cx = rect.left + rect.width / 2;
      const cy = rect.top + rect.height / 2;
      const dx = (e.clientX - cx) / (rect.width / 2);
      const dy = (e.clientY - cy) / (rect.height / 2);
      const rotX = -Math.max(-1, Math.min(1, dy)) * 10;
      const rotY = Math.max(-1, Math.min(1, dx)) * 12;
      glass.style.transform = `rotateX(${rotX.toFixed(2)}deg) rotateY(${rotY.toFixed(2)}deg)`;
    }, { passive: true });

    terminal.addEventListener("mouseleave", () => {
      glass.style.transform = "rotateX(0deg) rotateY(0deg)";
    }, { passive: true });

    // 3. Sonar Pulse Action with Web Audio Submarine Ping
    const sonarBtn = $("#btnSonarPulse");
    const ripple = $("#sonarWaveRipple");

    const discoveries = [
      "Сонар: Обнаружена богатая жила Титана на отметке -164м!",
      "Сонар: Засечен косяк Золотой Рыбы в квадрате Омега!",
      "Сонар: Дноуглубительный бур поднял древний адамантит!",
      "Сонар: Зафиксирован метеорит бездны с осколками звезд!"
    ];

    function playSonarSound() {
      if (!soundOn || reduceMotion) return;
      try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        const ctx = new AudioCtx();
        const now = ctx.currentTime;
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.type = "sine";
        osc.frequency.setValueAtTime(440, now);
        osc.frequency.exponentialRampToValueAtTime(880, now + 0.12);
        osc.frequency.exponentialRampToValueAtTime(220, now + 0.45);

        gain.gain.setValueAtTime(0.12, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.55);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(now);
        osc.stop(now + 0.6);
      } catch (e) {}
    }

    if (sonarBtn && ripple) {
      sonarBtn.addEventListener("click", () => {
        playSonarSound();
        ripple.classList.remove("pulse-active");
        void ripple.offsetWidth; // force reflow
        ripple.classList.add("pulse-active");

        const msg = discoveries[Math.floor(Math.random() * discoveries.length)];
        toast(msg);
      });
    }
  }

  // ─── QUICK CONNECT (NO IP, AUTO OS DETECTION) ──────────────────────────────
  function initQuickConnect() {
    const osIcon = $("#qcOsIcon");
    const osName = $("#qcOsName");

    if (osName) {
      const ua = navigator.userAgent || "";
      const plat = (navigator.userAgentData?.platform || navigator.platform || "").toLowerCase();
      if (plat.includes("win") || ua.includes("Windows")) {
        if (osIcon) osIcon.innerHTML = '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M0 3.449L9.75 2.1v9.451H0m10.949-9.602L24 0v11.551H10.949M0 12.6h9.75v9.451L0 20.699M10.949 12.6H24V24l-12.951-1.801"/></svg>';
        osName.textContent = "Windows 10/11 x64";
      } else if (plat.includes("mac") || ua.includes("Macintosh")) {
        if (osIcon) osIcon.innerHTML = '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 6.38c.62-.75 1.04-1.8 1.01-2.85-.92.04-2.04.62-2.69 1.37-.57.65-1.07 1.71-.94 2.73 1.03.08 2-.5 2.62-1.25z"/></svg>';
        osName.textContent = "macOS (Apple Silicon / Intel)";
      } else if (plat.includes("linux") || ua.includes("Linux")) {
        if (osIcon) osIcon.innerHTML = '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M12.002 0c-4.417 0-8 3.582-8 8 0 2.052.784 3.924 2.072 5.342-.046.223-.072.452-.072.688 0 1.656 1.344 3 3 3 .152 0 .301-.013.447-.035.795.632 1.797 1.005 2.893 1.005 1.096 0 2.098-.373 2.893-1.005.146.022.295.035.447.035 1.656 0 3-1.344 3-3 0-.236-.026-.465-.072-.688 1.288-1.418 2.072-3.29 2.072-5.342 0-4.418-3.583-8-8-8z"/></svg>';
        osName.textContent = "Linux x64";
      } else {
        if (osIcon) osIcon.innerHTML = '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M0 3.449L9.75 2.1v9.451H0m10.949-9.602L24 0v11.551H10.949M0 12.6h9.75v9.451L0 20.699M10.949 12.6H24V24l-12.951-1.801"/></svg>';
        osName.textContent = "ПК / Ноутбук";
      }
    }

    // Spotlight cursor tracking on bento cards
    document.querySelectorAll(".bento-card").forEach((card) => {
      card.addEventListener("mousemove", (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        card.style.setProperty("--mouse-x", `${x}px`);
        card.style.setProperty("--mouse-y", `${y}px`);
      }, { passive: true });
    });
  }

  // ─── BENTO DASHBOARD «ЖИВОЙ ОКЕАН» (NO EMOJIS, CLEAN SVGS) ─────────────────
  async function initBentoDashboard() {
    const coinSvg = '<img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt="">';
    const fishSvg = '<svg class="trend-fish-ico" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6.5 12c3.5-3.5 7.5-3.5 11.5 0-4 3.5-8 3.5-11.5 0z"/><circle cx="14.5" cy="11" r="1" fill="currentColor"/><path d="M18 12l3-2.5v5l-3-2.5z"/></svg>';

    // Parallel fetch
    const [trendsRes, marketRes, playersRes] = await Promise.allSettled([
      api("/api/trends"),
      api("/api/market/public?limit=3"),
      loadPlayers("fish"),
    ]);

    // 1. Bento Radar Top Fishermen (Numeric rank pills, no emojis)
    const fishList = $("#bentoTopFishList");
    if (fishList) {
      if (playersRes.status === "fulfilled" && Array.isArray(playersRes.value) && playersRes.value.length) {
        const top3 = playersRes.value.slice(0, 3);
        fishList.innerHTML = top3.map((p, idx) => `
          <a class="radar-lead-item" href="profile.html?nick=${encodeURIComponent(p.nick)}">
            <div class="lead-user">
              <span class="rank-pill rank-pill-${idx + 1}">${idx + 1}</span>
              <img class="lead-avatar" src="/api/skins/${encodeURIComponent(p.nick)}/avatar?v=look2" alt="${esc(p.nick)}" onerror="this.onerror=null;this.src='/assets/images/avatar_default.png'" />
              <div>
                <span class="lead-name">${esc(p.nick)}</span>
                ${p.privilege ? `<span class="lead-rank rank-${esc(p.privilege)}">${esc(p.privilege).toUpperCase()}</span>` : ""}
              </div>
            </div>
            <span class="lead-score">${Number(p.fish || 0).toLocaleString("ru-RU")} <small class="score-unit">уловов</small></span>
          </a>
        `).join("");
      } else {
        fishList.innerHTML = '<p class="muted-line">Лидеры рыболовства появятся в первом турнире</p>';
      }
    }

    // 2. Bento Trends Ticker (Clean SVG, sanitize ??? name)
    const trendsList = $("#bentoTrendsList");
    if (trendsList) {
      if (trendsRes.status === "fulfilled" && trendsRes.value?.trends?.length) {
        trendsList.innerHTML = trendsRes.value.trends.slice(0, 3).map((f) => {
          const rawName = f.name || "";
          const cleanName = (rawName === "???" || rawName.includes("?") || f.id === "starcatcher:missingno") ? "Глитч-рыба" : rawName;
          return `
            <div class="bento-trend-item">
              <span class="trend-fish-name">${esc(cleanName)}</span>
              <span class="trend-badge-mult">×${f.mult}</span>
            </div>
          `;
        }).join("");
      } else {
        trendsList.innerHTML = '<p class="muted-line">Спрос обновляется каждый день в полночь</p>';
      }
    }

    // 3. Bento Fresh Market Lot
    const marketFresh = $("#bentoMarketFresh");
    if (marketFresh) {
      if (marketRes.status === "fulfilled" && marketRes.value?.lots?.length) {
        const lot = marketRes.value.lots[0];
        marketFresh.innerHTML = `
          <div class="market-lot-card">
            <div class="lot-title-row">
              <span class="lot-item-name">${esc(lot.label)}${lot.count > 1 ? ` ×${lot.count}` : ""}</span>
              <span style="font-weight:700;color:var(--gold);font-size:0.92rem">
                ${Number(lot.price).toLocaleString("ru-RU")} ${coinSvg}
              </span>
            </div>
            <div class="lot-seller-row">
              <span>Продавец: <strong style="color:#f1f5f9">${esc(lot.seller)}</strong></span>
              <span class="qc-ver-chip" style="background:rgba(47,224,192,0.12);color:var(--accent)">/ah в игре</span>
            </div>
          </div>
        `;
      } else {
        marketFresh.innerHTML = `
          <div class="market-lot-card" style="text-align:center;padding:1rem 0.5rem">
            <p style="color:var(--muted);font-size:0.85rem;margin:0">На рынке пока нет активных лотов.</p>
            <small style="color:var(--accent);display:block;margin-top:0.3rem">Выстави первым через <code>/ah sell</code> в игре!</small>
          </div>
        `;
      }
    }
  }

  async function initLive() {
    try {
      const t = await api("/api/trends");
      const el = $("#live-trends");
      if (el && t.trends && t.trends.length) {
        el.innerHTML = t.trends.map((f) =>
          `<div style="display:flex;justify-content:space-between;align-items:center;padding:0.45rem 0;border-bottom:1px solid rgba(255,255,255,0.07)">
            <span style="font-size:0.92rem">${esc(f.name)}</span>
            <b style="color:var(--gold);font-size:0.92rem">×${f.mult}</b>
          </div>`).join("");
      }
    } catch {}
    try {
      const m = await api("/api/market/public?limit=5");
      const el = $("#live-market");
      if (el && m.lots && m.lots.length) {
        el.innerHTML = m.lots.map((l) =>
          `<div style="display:flex;justify-content:space-between;align-items:center;gap:0.5rem;padding:0.45rem 0;border-bottom:1px solid rgba(255,255,255,0.07)">
            <span style="font-size:0.92rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${esc(l.label)}${l.count > 1 ? ` ×${l.count}` : ""}</span>
            <span style="display:flex;align-items:center;gap:0.6rem;flex-shrink:0">
              <b style="color:var(--gold);font-size:0.92rem">${Number(l.price).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt=""></b>
              <small style="color:var(--muted)">${esc(l.seller)}</small>
            </span>
          </div>`).join("");
      } else if (el) {
        el.innerHTML = '<p style="color:var(--muted)">Лотов пока нет — будь первым: /ah sell в игре</p>';
      }
    } catch {}
  }

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
    
    // Wire loot preview modal buttons
    root.querySelectorAll("[data-view-loot]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const slug = btn.getAttribute("data-view-loot");
        openLootModal(slug);
      });
    });

    root.querySelectorAll("button[disabled]").forEach((btn) => {
      btn.addEventListener("click", (e) => {
        e.preventDefault();
        toast("Покупки / открытие на сервере");
      });
    });
  }

  function renderMarketLots(root, lots, { empty = "Пока нет лотов. Выставь предмет командой /ah sell в игре." } = {}) {
    if (!root) return;
    const rows = lots || [];
    if (!rows.length) {
      root.innerHTML = `<p class="muted-line">${empty}</p>`;
      return;
    }
    root.innerHTML = `<table class="market-table">
      <thead><tr><th>Предмет</th><th>Кол-во</th><th>Продавец</th><th>Цена</th></tr></thead>
      <tbody>
        ${rows
          .map(
            (lot) => `<tr>
              <td>${esc(lot.label || lot.item_id || "предмет")}</td>
              <td>${esc(lot.count || 1)}</td>
              <td>${esc(lot.seller || "—")}</td>
              <td class="price">${Number(lot.price || 0).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt=""></td>
            </tr>`
          )
          .join("")}
      </tbody>
    </table>`;
  }

  async function initTrends() {
    const root = $("[data-trends-home]");
    if (!root) return;
    const emptyCopy = "Тренд обновится после полуночи на сервере";
    try {
      const data = await api("/api/trends");
      const rows = Array.isArray(data.trends) ? data.trends : [];
      if (!rows.length) {
        root.innerHTML = `<p class="muted-line">${emptyCopy}</p>`;
        return;
      }
      root.innerHTML = `<ul class="trend-list">${rows
        .map((row) => {
          const label = esc(row.name || row.id || "рыба");
          const mult = Number(row.mult) || 1;
          const shown = Number.isInteger(mult) ? String(mult) : String(mult);
          return `<li class="trend-row"><span class="trend-name">${label}</span><span class="trend-mult">×${esc(shown)}</span></li>`;
        })
        .join("")}</ul>`;
    } catch {
      root.innerHTML = `<p class="muted-line">${emptyCopy}</p>`;
    }
  }

  async function initHomeFishTop() {
    const root = $("[data-fish-home]");
    if (!root) return;
    try {
      const players = (await loadPlayers("fish")).slice(0, 3);
      root.innerHTML = playerRows(players, "fish") || `<p class="muted-line">Пока нет улова в базе.</p>`;
    } catch {
      root.innerHTML = `<p class="muted-line">Не удалось загрузить топ.</p>`;
    }
  }

  async function initMarket() {
    const home = $("[data-market-home]");
    const page = $("[data-market-page]");
    if (!home && !page) return;
    const limit = page ? 40 : 6;
    try {
      const data = await api(`/api/market/public?limit=${limit}`);
      const lots = data.lots || [];
      renderMarketLots(home, lots.slice(0, 6), { empty: "Аукцион пуст. Лоты появляются из игры." });
      renderMarketLots(page, lots);
    } catch {
      renderMarketLots(home, [], { empty: "Аукцион сейчас недоступен." });
      renderMarketLots(page, [], { empty: "Не удалось загрузить лоты." });
    }
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
    nodes.forEach((n) => n.classList.contains("in") || io.observe(n));
    document.querySelectorAll(".hero .reveal").forEach((n) => n.classList.add("in"));
  }

  /** Re-run reveal for dynamically rendered containers (grids get a small stagger). */
  function revealScan(scope) {
    const nodes = (scope || document).querySelectorAll(".reveal:not(.in)");
    if (!nodes.length) return;
    if (reduceMotion) {
      nodes.forEach((n) => n.classList.add("in"));
      return;
    }
    revealScan._io =
      revealScan._io ||
      new IntersectionObserver(
        (entries) => {
          entries.forEach((e) => {
            if (e.isIntersecting) {
              e.target.classList.add("in");
              revealScan._io.unobserve(e.target);
            }
          });
        },
        { threshold: 0.12, rootMargin: "0px 0px -40px 0px" }
      );
    nodes.forEach((n, i) => {
      if (!n.style.transitionDelay && i < 6) n.style.transitionDelay = `${Math.min(i * 45, 240)}ms`;
      revealScan._io.observe(n);
    });
  }

  async function refreshSession() {
    try {
      const data = await api("/api/me");
      if (data.user) {
        setUser({
          ...data.user,
          coins: data.profile && data.profile.coins != null ? data.profile.coins : data.user.coins,
          rub_balance: Number(data.rub_balance || 0),
        });
      }
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

    gate.textContent = "Доступ есть. Тексты, новости, каталог и игроки ниже.";
    root.hidden = false;

    const purchases = $("#admin-purchases");
    let siteCopy = {};
    try {
      const st = await api("/api/admin/settings");
      if (purchases) purchases.checked = !!st.settings?.purchases_enabled;
      siteCopy = st.copy || {};
    } catch {
      /* settings optional */
    }

    const copyBox = $("#admin-copy");
    if (copyBox) {
      let lastGroup = "";
      copyBox.innerHTML = COPY_FIELDS.map((f) => {
        const val = esc(siteCopy[f.key] || "");
        const field = f.long
          ? `<div class="field"><label>${esc(f.label)}</label><textarea data-copy="${f.key}" rows="3">${val}</textarea></div>`
          : `<div class="field"><label>${esc(f.label)}</label><input data-copy="${f.key}" value="${val}" /></div>`;
        if (f.group && f.group !== lastGroup) {
          lastGroup = f.group;
          return `<h4 class="admin-copy-group">${esc(f.group)}</h4>${field}`;
        }
        return field;
      }).join("");
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

    $("#admin-save-copy")?.addEventListener("click", async () => {
      const copy = {};
      document.querySelectorAll("[data-copy]").forEach((el) => {
        copy[el.getAttribute("data-copy")] = el.value;
      });
      try {
        await api("/api/admin/settings", {
          method: "PATCH",
          body: JSON.stringify({ copy }),
        });
        toast("Тексты сайта сохранены");
      } catch (err) {
        toast(err.message || "Не удалось сохранить тексты");
      }
    });

    async function loadNewsAdmin() {
      const box = $("#admin-news");
      if (!box) return;
      box.innerHTML = `<p class="muted-line">Загрузка…</p>`;
      try {
        const data = await api("/api/admin/news");
        const rows = data.news || [];
        if (!rows.length) {
          box.innerHTML = `<p class="muted-line">Новостей нет.</p>`;
          return;
        }
        box.innerHTML = `<table class="admin-table"><thead><tr>
          <th>Дата</th><th>Заголовок</th><th>Текст</th><th>Вкл</th><th></th>
        </tr></thead><tbody>
        ${rows
          .map(
            (n) => `<tr data-id="${n.id}">
          <td><input data-f="published_at" type="date" value="${esc(String(n.published_at || "").slice(0, 10))}" /></td>
          <td><input data-f="title" value="${esc(n.title)}" /></td>
          <td><textarea data-f="body" rows="3">${esc(n.body)}</textarea></td>
          <td><input data-f="published" type="checkbox" ${n.published ? "checked" : ""} /></td>
          <td style="white-space:nowrap">
            <button class="btn btn-secondary" type="button" data-save-news>OK</button>
            <button class="btn btn-ghost" type="button" data-del-news>Удалить</button>
          </td>
        </tr>`
          )
          .join("")}
        </tbody></table>`;

        box.querySelectorAll("[data-save-news]").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const tr = btn.closest("tr");
            const id = tr?.dataset.id;
            if (!id) return;
            const body = {};
            tr.querySelectorAll("[data-f]").forEach((inp) => {
              const key = inp.getAttribute("data-f");
              if (key === "published") body.published = inp.checked;
              else body[key] = inp.value;
            });
            try {
              await api(`/api/admin/news/${id}`, {
                method: "PATCH",
                body: JSON.stringify(body),
              });
              toast("Новость сохранена");
            } catch (err) {
              toast(err.message || "Ошибка");
            }
          });
        });
        box.querySelectorAll("[data-del-news]").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const tr = btn.closest("tr");
            const id = tr?.dataset.id;
            if (!id || !confirm("Удалить новость?")) return;
            try {
              await api(`/api/admin/news/${id}`, { method: "DELETE" });
              toast("Удалено");
              await loadNewsAdmin();
            } catch (err) {
              toast(err.message || "Ошибка");
            }
          });
        });
      } catch (err) {
        box.innerHTML = `<p class="muted-line">${esc(err.message || "Ошибка загрузки")}</p>`;
      }
    }

    const newsForm = $("#admin-news-form");
    if (newsForm) {
      const dateInp = newsForm.querySelector('[name="published_at"]');
      if (dateInp && !dateInp.value) dateInp.value = new Date().toISOString().slice(0, 10);
      newsForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(newsForm);
        try {
          await api("/api/admin/news", {
            method: "POST",
            body: JSON.stringify({
              title: fd.get("title"),
              body: fd.get("body"),
              published_at: fd.get("published_at"),
              published: !!fd.get("published"),
            }),
          });
          newsForm.reset();
          if (dateInp) dateInp.value = new Date().toISOString().slice(0, 10);
          const pub = newsForm.querySelector('[name="published"]');
          if (pub) pub.checked = true;
          toast("Новость добавлена");
          await loadNewsAdmin();
        } catch (err) {
          toast(err.message || "Не удалось добавить");
        }
      });
    }
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

    await loadNewsAdmin();
    await loadUsers();
    await loadCatalog();
  }

  document.addEventListener("DOMContentLoaded", () => {
    reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const page = pageId();

    // 1. Instant synchronous layout & UI wiring
    renderHeader();
    renderFooter();
    wireCommon();
    wireSounds();
    showApiBanner();
    lockAuthForms();

    // 2. Instant reveal so content is immediately visible with zero delay!
    initReveal();

    // 3. Background session refresh (never blocks initial render)
    if (isCanonicalHost()) {
      refreshSession().then(() => renderHeader()).catch(() => {});
    }

    // 4. Targeted asynchronous data hydration by page
    
    if (page === "home") {
      initQuickConnect();
      initBentoDashboard();
      Promise.allSettled([
        loadSiteContent(),
      ]);
    } else if (page === "store") {
      initStorePage();
    } else if (page === "cases") {
      initCasesShowcasePage();
    } else if (page === "market") {
      initMarketPage();
    } else if (page === "profile") {
      initProfile();
    } else if (page === "top") {
    initTop();
    } else if (page === "players") {
    initPlayers();
    } else if (page === "login" || page === "register") {
    initAuth();
    } else if (page === "reset") {
      initReset();
    } else if (page === "admin") {
      initAdmin();
    } else if (page === "news") {
      loadSiteContent();
    }

    if ("serviceWorker" in navigator && location.protocol === "https:") {
      navigator.serviceWorker.register("/sw.js").catch(() => {});
    }
  });


  // ==========================================================================
  // AQUATECH SUBPAGE ENHANCEMENTS: STORE, CASES (ROULETTE), MARKET & PROFILE
  // ==========================================================================


  // 1. STORE PAGE
  function initStorePage() {
    const root = $("#store-root");
    if (!root) return;

    let currentPeriod = "month"; // "month" | "forever"

    const ranks = [
      {
        slug: "sailor",
        title: "Моряк",
        monthPrice: 99,
        foreverPrice: 299,
        badge: "badge-sailor",
        badgeLabel: "МОРЯК",
        desc: "Стартовая морская привилегия. Префикс в чате, 2 точки дома (/sethome) и базовые удобства.",
        perks: [
          "Префикс [МОРЯК] в чате",
          "2 точки дома /sethome",
          "Цветной ник в чате и Tab",
          "Базовый морской набор в F4",
          "Сохранение 20% опыта при гибели"
        ]
      },
      {
        slug: "skipper",
        title: "Шкипер",
        monthPrice: 249,
        foreverPrice: 699,
        badge: "badge-skipper",
        badgeLabel: "ШКИПЕР",
        desc: "Продвинутый мореплаватель. Приоритетный слот входа, 3 точки дома и набор Шкипера.",
        perks: [
          "Префикс [ШКИПЕР] в чате",
          "3 точки дома /sethome",
          "Приоритетный вход на сервер 24/7",
          "Набор Шкипера в меню F4",
          "Множитель удачи рыбалки x1.5"
        ]
      },
      {
        slug: "captain",
        title: "Капитан",
        monthPrice: 499,
        foreverPrice: 1299,
        badge: "badge-captain",
        badgeLabel: "ХИТ · КАПИТАН",
        popular: true,
        desc: "Командир корабля. Режим полета /fly на приватах, 5 точек дома и повышенная удача.",
        perks: [
          "Префикс [КАПИТАН] в чате",
          "Режим полёта /fly на приватах",
          "5 точек дома /sethome",
          "Множитель удачи улова x2.0",
          "Кит Капитана в меню F4"
        ]
      },
      {
        slug: "admiral",
        title: "Адмирал",
        monthPrice: 899,
        foreverPrice: 2199,
        badge: "badge-admiral",
        badgeLabel: "АДМИРАЛ",
        desc: "Верховный флагман флота. Полет /fly везде, смена ника /nick, 10 точек дома.",
        perks: [
          "Префикс [АДМИРАЛ] в чате",
          "Режим полёта /fly по всему миру",
          "Смена ника командой /nick",
          "10 точек дома /sethome",
          "Множитель удачи улова x4.0",
          "Увеличенный Кит Адмирала"
        ]
      },
      {
        slug: "legend",
        title: "Легенда",
        monthPrice: 1499,
        foreverPrice: 3499,
        badge: "badge-legend",
        badgeLabel: "ЛЕГЕНДА ОКЕАНА",
        legend: true,
        desc: "Высший статус на сервере AquaTech. Неограниченные дома, /hat, максимальная удача x8.",
        perks: [
          "Префикс [ЛЕГЕНДА] в чате и Tab",
          "Неограниченные дома /sethome",
          "Режим полёта /fly + надеть блок /hat",
          "Максимальная удача улова x8.0",
          "Эксклюзивный Кит Легенды в F4",
          "Доступ к закрытым ивентам"
        ]
      }
    ];

    const checkSvg = '<svg class="perk-check-ico" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>';

    function renderCards() {
      const isForever = currentPeriod === "forever";
      root.innerHTML = ranks.map((r) => {
        const price = isForever ? r.foreverPrice : r.monthPrice;
        const unit = isForever ? "навсегда" : "мес";
        return `
          <div class="store-rank-card ${r.popular ? 'is-popular' : ''} ${r.legend ? 'is-legend' : ''}">
            <span class="rank-badge-pill ${r.badge}">${r.badgeLabel}</span>
            <h3 class="store-rank-title">${esc(r.title)}</h3>
            <p class="store-rank-desc">${esc(r.desc)}</p>
            <div class="store-price-row">
              <span class="store-price-num">${price} ₽</span>
              <span class="store-price-unit">/ ${unit}</span>
            </div>
            <ul class="perk-list-v2">
              ${r.perks.map((p) => `<li class="perk-item-v2">${checkSvg}<span>${esc(p)}</span></li>`).join("")}
            </ul>
            <button class="store-buy-btn" type="button" data-buy-rank="${r.slug}" data-rank-title="${esc(r.title)}" data-rank-price="${price}" data-rank-period="${unit}">
              <span>Купить за ${price} ₽</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
          </div>
        `;
      }).join("");

      root.querySelectorAll("[data-buy-rank]").forEach((btn) => {
        btn.addEventListener("click", () => {
          const slug = btn.getAttribute("data-buy-rank");
          const title = btn.getAttribute("data-rank-title");
          const price = btn.getAttribute("data-rank-price");
          const period = btn.getAttribute("data-rank-period");
          openStoreModal(title, price, period, slug);
        });
      });
    }

    // Segmented duration toggle handlers (Apple HIG)
    document.querySelectorAll(".apple-segment-btn[data-period]").forEach((btn) => {
      btn.addEventListener("click", () => {
        document.querySelectorAll(".apple-segment-btn[data-period]").forEach((b) => b.classList.remove("active"));
        btn.classList.add("active");
        currentPeriod = btn.getAttribute("data-period");
        playTone("click");
        renderCards();
      });
    });

    document.querySelectorAll("[data-buy-coins]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const slug = btn.getAttribute("data-buy-coins");
        openBuyCoinsModal(slug);
      });
    });

    renderCards();
  }

  async function openStoreModal(title, price, period, slug) {
    let modal = document.getElementById("store-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "store-modal";
      modal.className = "store-modal-overlay";
      document.body.appendChild(modal);
    }

    const user = getUser();
    const defaultNick = user ? user.nick : "";
    const periodStr = period === "навсегда" ? "Навсегда" : "1 Месяц";
    const priceRub = Math.max(1, Number(price || 99));
    let userRubles = 0;

    if (user?.nick) {
      try {
        const me = await api("/api/me");
        userRubles = Number(me.rub_balance || 0);
        if (me.user) setUser({ ...getUser(), ...me.user, rub_balance: userRubles });
        renderHeader();
      } catch {
        userRubles = Number(getUser()?.rub_balance || 0);
      }
    }

    const hasEnough = user && userRubles >= priceRub;
    let selectedMethod = "sbp";

    modal.innerHTML = `
      <div class="apple-modal-box">
        <div class="apple-modal-header">
          <div>
            <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.35rem">
              <h3>Покупка: «${esc(title)}»</h3>
              <span class="apple-badge-save" style="background:rgba(47,224,192,0.18);color:#2fe0c0;border:1px solid rgba(47,224,192,0.4)">${esc(periodStr)}</span>
            </div>
            <p class="apple-modal-subtitle">Моментальная выдача привилегии на сервере AquaTech</p>
          </div>
          <button class="apple-modal-close" type="button" aria-label="Закрыть">✕</button>
        </div>

        <div class="apple-modal-field">
          <label for="storeModalNick">Игровой никнейм на сервере</label>
          <input type="text" id="storeModalNick" class="apple-modal-input" value="${esc(defaultNick)}" placeholder="Введи ник в игре (например: Renfild)" autocomplete="off" />
        </div>

        <div class="apple-modal-field">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem">
            <label style="margin:0">Способ оплаты</label>
            ${user ? `<span style="font-size:0.85rem;color:#2fe0c0">Твой баланс: <b>${userRubles.toLocaleString("ru-RU")} ₽</b></span>` : ""}
          </div>
          <div class="apple-pay-methods-list" role="radiogroup" aria-label="Способ оплаты">
            <!-- С баланса -->
            <div class="apple-pay-method-card ${selectedMethod === 'balance' ? 'active' : ''} ${hasEnough ? '' : 'is-dimmed'}" data-method="balance" role="radio" tabindex="0" aria-checked="${selectedMethod === 'balance' ? 'true' : 'false'}" aria-label="С баланса аккаунта">
              <div class="apple-pay-method-left">
                <div class="apple-pay-icon-box balance-icon-box">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ffd875" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="5" width="20" height="14" rx="3"></rect>
                    <line x1="2" y1="10" x2="22" y2="10"></line>
                  </svg>
                </div>
                <div class="apple-pay-method-info">
                  <div class="apple-pay-method-title-row">
                    <span class="apple-pay-method-title">С баланса аккаунта</span>
                    <span class="apple-pay-badge ${hasEnough ? 'badge-green' : 'badge-muted'}">${hasEnough ? 'Хватает' : 'Недостаточно'}</span>
                  </div>
                  <span class="apple-pay-method-sub">${user ? userRubles.toLocaleString('ru-RU') + ' ₽ доступно' : 'Нужен вход в аккаунт'}</span>
                </div>
              </div>
              <div class="apple-pay-method-radio">
                <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
              </div>
            </div>

            <!-- СБП -->
            <div class="apple-pay-method-card ${selectedMethod === 'sbp' ? 'active' : ''}" data-method="sbp" role="radio" tabindex="0" aria-checked="${selectedMethod === 'sbp' ? 'true' : 'false'}" aria-label="СБП — 0% комиссия">
              <div class="apple-pay-method-left">
                <div class="apple-pay-icon-box sbp-icon-box">
                  <svg width="26" height="26" viewBox="0 0 28 28" fill="none">
                    <path d="M14 3L18 9H10L14 3Z" fill="#F4B400"/>
                    <path d="M25 14L19 18V10L25 14Z" fill="#0077FF"/>
                    <path d="M14 25L10 19H18L14 25Z" fill="#00A859"/>
                    <path d="M3 14L9 10V18L3 14Z" fill="#E84135"/>
                    <circle cx="14" cy="14" r="3.5" fill="#FFFFFF"/>
                  </svg>
                </div>
                <div class="apple-pay-method-info">
                  <div class="apple-pay-method-title-row">
                    <span class="apple-pay-method-title">СБП</span>
                    <span class="apple-pay-badge badge-green">0% комиссия</span>
                  </div>
                  <span class="apple-pay-method-sub">QR-код в приложении любого банка РФ</span>
                </div>
              </div>
              <div class="apple-pay-method-radio">
                <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
              </div>
            </div>

            <!-- МИР -->
            <div class="apple-pay-method-card ${selectedMethod === 'card' ? 'active' : ''}" data-method="card" role="radio" tabindex="0" aria-checked="${selectedMethod === 'card' ? 'true' : 'false'}" aria-label="Карты МИР — карты РФ">
              <div class="apple-pay-method-left">
                <div class="apple-pay-icon-box mir-icon-box">
                  <svg width="30" height="20" viewBox="0 0 30 20" fill="none">
                    <rect width="30" height="20" rx="4" fill="#0d281e" stroke="#0ecb81" stroke-width="1.2"/>
                    <text x="15" y="14" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-weight="900" font-size="10" fill="#0ecb81" letter-spacing="1">МИР</text>
                  </svg>
                </div>
                <div class="apple-pay-method-info">
                  <div class="apple-pay-method-title-row">
                    <span class="apple-pay-method-title">Карты МИР</span>
                    <span class="apple-pay-badge badge-cyan">Карты РФ</span>
                  </div>
                  <span class="apple-pay-method-sub">Сбер, Т-Банк, ВТБ, Альфа и другие банки</span>
                </div>
              </div>
              <div class="apple-pay-method-radio">
                <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
              </div>
            </div>

            <!-- Visa / Mastercard -->
            <div class="apple-pay-method-card ${selectedMethod === 'visa' ? 'active' : ''} ${priceRub >= VISA_MIN_RUB ? '' : 'is-dimmed'}" data-method="visa" role="radio" tabindex="0" aria-checked="${selectedMethod === 'visa' ? 'true' : 'false'}" aria-label="Visa / Mastercard — Международные карты">
              <div class="apple-pay-method-left">
                <div class="apple-pay-icon-box visa-icon-box">
                  <svg width="32" height="20" viewBox="0 0 32 20" fill="none">
                    <circle cx="12" cy="10" r="7" fill="#EB001B" fill-opacity="0.9"/>
                    <circle cx="20" cy="10" r="7" fill="#F79E1B" fill-opacity="0.85"/>
                    <path d="M16 5.8A7 7 0 0 1 16 14.2A7 7 0 0 1 16 5.8Z" fill="#FF5F00"/>
                  </svg>
                </div>
                <div class="apple-pay-method-info">
                  <div class="apple-pay-method-title-row">
                    <span class="apple-pay-method-title">Visa / Mastercard</span>
                    <span class="apple-pay-badge ${priceRub >= VISA_MIN_RUB ? 'badge-amber' : 'badge-muted'} visa-badge">${priceRub >= VISA_MIN_RUB ? 'Международные' : 'от ' + VISA_MIN_RUB + ' ₽'}</span>
                  </div>
                  <span class="apple-pay-method-sub">Карты СНГ, Европы и зарубежных банков</span>
                </div>
              </div>
              <div class="apple-pay-method-radio">
                <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
              </div>
            </div>
          </div>
        </div>

        <div class="apple-modal-total-card">
          <div>
            <div class="apple-modal-total-label">Стоимость привилегии:</div>
            <small style="color:#94a3b8;font-size:0.78rem">Срок: ${esc(periodStr)}</small>
          </div>
          <div class="apple-modal-total-price" id="storeModalTotalPrice">${priceRub} ₽</div>
        </div>

        <div style="display:flex;flex-direction:column;gap:0.6rem">
          <button class="apple-modal-submit-btn" id="storeConfirmBtn" type="button">
            <span>${selectedMethod === 'balance' ? `Купить за ${priceRub} ₽ (с баланса)` : selectedMethod === 'card' ? `Оплатить картой МИР ${priceRub} ₽` : selectedMethod === 'visa' ? `Оплатить Visa / MC ${priceRub} ₽` : `Оплатить через СБП ${priceRub} ₽`}</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>

        <div id="storeModalMsg" class="apple-modal-status-msg" style="display:none;margin-top:1rem"></div>
      </div>
    `;

    modal.style.display = "flex";
    requestAnimationFrame(() => modal.classList.add("open"));

    function closeModal() {
      modal.classList.remove("open");
      setTimeout(() => { modal.style.display = "none"; }, 250);
    }

    modal.querySelector(".apple-modal-close").onclick = closeModal;
    modal.onclick = (e) => { if (e.target === modal) closeModal(); };

    const methodCards = modal.querySelectorAll(".apple-pay-method-card");
    const confirmBtn = modal.querySelector("#storeConfirmBtn");

    methodCards.forEach((card) => {
      const handleSelect = () => {
        const next = card.getAttribute("data-method");
        if (next === "balance" && !hasEnough) {
          playTone("warn");
          toast("Недостаточно средств на балансе. Пополни баланс или выбери СБП / Карту.");
          return;
        }
        if (next === "visa" && priceRub < VISA_MIN_RUB) {
          playTone("warn");
          toast("Visa / Mastercard от " + VISA_MIN_RUB + " ₽. Для этой суммы — СБП или МИР.");
          return;
        }
        methodCards.forEach((c) => {
          c.classList.remove("active");
          c.setAttribute("aria-checked", "false");
        });
        card.classList.add("active");
        card.setAttribute("aria-checked", "true");
        selectedMethod = next;
        playTone("click");
        if (selectedMethod === "balance") {
          confirmBtn.querySelector("span").textContent = `Купить за ${priceRub} ₽ (с баланса)`;
        } else if (selectedMethod === "card") {
          confirmBtn.querySelector("span").textContent = `Оплатить картой МИР ${priceRub} ₽`;
        } else if (selectedMethod === "visa") {
          confirmBtn.querySelector("span").textContent = `Оплатить Visa / MC ${priceRub} ₽`;
        } else {
          confirmBtn.querySelector("span").textContent = `Оплатить через СБП ${priceRub} ₽`;
        }
      };
      card.addEventListener("click", handleSelect);
      card.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          handleSelect();
        }
      });
    });

    const nickInput = modal.querySelector("#storeModalNick");
    const msg = modal.querySelector("#storeModalMsg");

    confirmBtn.onclick = async () => {
      const nick = (nickInput.value || "").trim();
      if (!nick) {
        nickInput.style.borderColor = "#f87171";
        nickInput.focus();
        playTone("warn");
        return;
      }
      confirmBtn.disabled = true;
      confirmBtn.querySelector("span").textContent = "Обработка платежа…";
      msg.style.display = "none";

      try {
        const isForever = String(period || "").toLowerCase().includes("навсегда") || String(period || "").toLowerCase().includes("forever");
        const targetSlug = isForever && !slug.endsWith("_forever") ? `${slug}_forever` : (slug || "sailor");
        const res = await api("/api/purchase", {
          method: "POST",
          body: JSON.stringify({ nick, slug: targetSlug, period: isForever ? "forever" : "month", method: selectedMethod })
        });

        if (selectedMethod !== "balance" && (res.confirmation_url || res.url)) {
          const dest = res.host || "";
          const rail = res.rail || selectedMethod;
          msg.style.display = "block";
          msg.innerHTML = `<span style="color:#2fe0c0">Открываем ${rail === "SBP" || selectedMethod === "sbp" ? "СБП" : selectedMethod === "visa" ? "Visa / MC" : "МИР"} (${esc(dest || "шлюз")})…</span>`;
          if ((selectedMethod === "sbp" || selectedMethod === "visa") && /lava\.top$/i.test(dest)) {
            throw new Error("Сервер отдал карточную страницу Lava вместо " + (selectedMethod === "sbp" ? "СБП" : "Visa") + ".");
          }
          window.location.href = res.confirmation_url || res.url;
          return;
        }

        playTone("ok");
        msg.style.display = "block";
        msg.innerHTML = `
          <div style="background:rgba(47,224,192,0.12);border:1px solid #2fe0c0;border-radius:14px;padding:1.1rem;color:#2fe0c0;text-align:center">
            <h4 style="margin:0 0 0.3rem;font-size:1.15rem">✅ Покупка успешна!</h4>
            <p style="margin:0 0 0.5rem;color:#f8fafc;font-size:0.92rem">${esc(res.message || "Привилегия успешно выдана на сервере!")}</p>
            <small style="color:#94a3b8">Остаток на балансе: <b>${Number(res.balance_rub || 0).toLocaleString("ru-RU")} ₽</b></small>
          </div>
        `;
        toast("Привилегия куплена!");
        if (typeof res.balance_rub === "number") {
          const u = getUser();
          if (u) {
            setUser({ ...u, rub_balance: Number(res.balance_rub) });
            renderHeader();
          }
        }
      } catch (err) {
        playTone("warn");
        msg.style.display = "block";
        msg.innerHTML = `
          <div style="background:rgba(239,68,68,0.12);border:1px solid #ef4444;border-radius:14px;padding:1rem;color:#f87171;text-align:center">
            <strong>Ошибка оплаты:</strong><br/>
            ${esc(err.message || "Не удалось завершить покупку.")}
          </div>
        `;
        confirmBtn.disabled = false;
        confirmBtn.querySelector("span").textContent =
          selectedMethod === "balance"
            ? `Купить за ${priceRub} ₽ (с баланса)`
            : selectedMethod === "card"
              ? `Оплатить МИР ${priceRub} ₽`
              : selectedMethod === "visa"
                ? `Оплатить Visa / MC ${priceRub} ₽`
                : `Оплатить СБП ${priceRub} ₽`;
      }
    };
  }

  function openBuyCoinsModal(preselectSlug = "coins_30k") {
    window.openBuyCoinsModal = openBuyCoinsModal;
    if (!getUser()) {
      toast("Сначала войди в аккаунт");
      location.href = "login.html";
      return;
    }
    let modal = document.getElementById("buy-coins-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "buy-coins-modal";
      modal.className = "store-modal-overlay";
      document.body.appendChild(modal);
    }
    const user = getUser();
    const userRub = Number(user.rub_balance || 0);

    const PACKS = [
      { slug: "coins_10k", title: "10 000 Монет", coins: 10000, price: 99, badge: "Старт", desc: "Хватит на 20 стартовых кейсов" },
      { slug: "coins_30k", title: "30 000 Монет", coins: 30000, price: 249, badge: "Популярно", desc: "Для редких удочек и кейсов F4" },
      { slug: "coins_75k", title: "75 000 Монет", coins: 75000, price: 499, badge: "Выгодно +15%", desc: "Хватит на кейсы Бездны и аукцион" },
      { slug: "coins_200k", title: "200 000 Монет", coins: 200000, price: 999, badge: "Максимум", desc: "Океанский магнат: топ-снасти и F4" },
    ];

    let selectedSlug = preselectSlug || "coins_30k";
    let selectedMethod = (userRub >= 249) ? "balance" : "sbp";

    function render() {
      const pack = PACKS.find((p) => p.slug === selectedSlug) || PACKS[1];
      const canBalance = userRub >= pack.price;
      if (selectedMethod === "balance" && !canBalance) {
        selectedMethod = "sbp";
      }
      if (selectedMethod === "visa" && pack.price < VISA_MIN_RUB) {
        selectedMethod = "sbp";
      }

      modal.innerHTML = `
        <div class="apple-modal-box">
          <div class="apple-modal-header">
            <div>
              <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.35rem">
                <h3>Покупка АкваМонет</h3>
                <span class="apple-badge-save" style="background:rgba(245,194,91,0.18);color:#ffd875;border:1px solid rgba(245,194,91,0.4)">Игровая валюта F4</span>
              </div>
              <p class="apple-modal-subtitle">Моментальное зачисление на аккаунт на сервере и сайте</p>
            </div>
            <button class="apple-modal-close" type="button" aria-label="Закрыть">✕</button>
          </div>

          <div style="display:grid;grid-template-columns:repeat(2, 1fr);gap:0.6rem;margin:1rem 0">
            ${PACKS.map((p) => `
              <div class="apple-pay-method-card ${p.slug === selectedSlug ? "active" : ""}" data-coin-slug="${p.slug}" style="padding:0.75rem 0.85rem;cursor:pointer;display:flex;flex-direction:column;align-items:flex-start;justify-content:space-between">
                <div style="width:100%;display:flex;justify-content:space-between;align-items:center;margin-bottom:0.35rem">
                  <span class="apple-badge-save" style="font-size:0.62rem;padding:0.1rem 0.38rem">${p.badge}</span>
                  <span style="font-size:0.85rem;font-weight:700;color:#f8fafc;white-space:nowrap">${p.price} ₽</span>
                </div>
                <strong class="aqua-coin-badge" style="color:#ffd875;font-size:1.05rem;display:inline-flex;align-items:center;gap:0.3rem">${Number(p.coins).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" /></strong>
              </div>
            `).join("")}
          </div>

          <div class="apple-modal-field">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem">
              <label style="margin:0">Способ оплаты</label>
              <span style="font-size:0.85rem;color:#2fe0c0">Баланс: <b>${userRub.toLocaleString("ru-RU")} ₽</b></span>
            </div>
            <div class="apple-pay-methods-list" role="radiogroup" aria-label="Способ оплаты">
              <!-- С баланса аккаунта -->
              <div class="apple-pay-method-card ${selectedMethod === 'balance' ? 'active' : ''} ${canBalance ? '' : 'is-dimmed'}" data-pay="balance" role="radio" tabindex="0" aria-checked="${selectedMethod === 'balance' ? 'true' : 'false'}" aria-label="С баланса аккаунта">
                <div class="apple-pay-method-left">
                  <div class="apple-pay-icon-box balance-icon-box">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ffd875" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="2" y="5" width="20" height="14" rx="3"></rect>
                      <line x1="2" y1="10" x2="22" y2="10"></line>
                    </svg>
                  </div>
                  <div class="apple-pay-method-info">
                    <div class="apple-pay-method-title-row">
                      <span class="apple-pay-method-title">С баланса аккаунта</span>
                      <span class="apple-pay-badge ${canBalance ? 'badge-green' : 'badge-muted'}">${canBalance ? 'Хватает' : 'Недостаточно'}</span>
                    </div>
                    <span class="apple-pay-method-sub">${userRub.toLocaleString('ru-RU')} ₽ доступно на балансе</span>
                  </div>
                </div>
                <div class="apple-pay-method-radio">
                  <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
                </div>
              </div>

              <!-- СБП -->
              <div class="apple-pay-method-card ${selectedMethod === 'sbp' ? 'active' : ''}" data-pay="sbp" role="radio" tabindex="0" aria-checked="${selectedMethod === 'sbp' ? 'true' : 'false'}" aria-label="СБП — 0% комиссия">
                <div class="apple-pay-method-left">
                  <div class="apple-pay-icon-box sbp-icon-box">
                    <svg width="26" height="26" viewBox="0 0 28 28" fill="none">
                      <path d="M14 3L18 9H10L14 3Z" fill="#F4B400"/>
                      <path d="M25 14L19 18V10L25 14Z" fill="#0077FF"/>
                      <path d="M14 25L10 19H18L14 25Z" fill="#00A859"/>
                      <path d="M3 14L9 10V18L3 14Z" fill="#E84135"/>
                      <circle cx="14" cy="14" r="3.5" fill="#FFFFFF"/>
                    </svg>
                  </div>
                  <div class="apple-pay-method-info">
                    <div class="apple-pay-method-title-row">
                      <span class="apple-pay-method-title">СБП</span>
                      <span class="apple-pay-badge badge-green">0% комиссия</span>
                    </div>
                    <span class="apple-pay-method-sub">QR-код в приложении любого банка РФ</span>
                  </div>
                </div>
                <div class="apple-pay-method-radio">
                  <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
                </div>
              </div>

              <!-- Карты МИР -->
              <div class="apple-pay-method-card ${selectedMethod === 'card' ? 'active' : ''}" data-pay="card" role="radio" tabindex="0" aria-checked="${selectedMethod === 'card' ? 'true' : 'false'}" aria-label="Карты МИР — карты РФ">
                <div class="apple-pay-method-left">
                  <div class="apple-pay-icon-box mir-icon-box">
                    <svg width="30" height="20" viewBox="0 0 30 20" fill="none">
                      <rect width="30" height="20" rx="4" fill="#0d281e" stroke="#0ecb81" stroke-width="1.2"/>
                      <text x="15" y="14" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-weight="900" font-size="10" fill="#0ecb81" letter-spacing="1">МИР</text>
                    </svg>
                  </div>
                  <div class="apple-pay-method-info">
                    <div class="apple-pay-method-title-row">
                      <span class="apple-pay-method-title">Карты МИР</span>
                      <span class="apple-pay-badge badge-cyan">Карты РФ</span>
                    </div>
                    <span class="apple-pay-method-sub">Сбер, Т-Банк, ВТБ, Альфа и другие банки</span>
                  </div>
                </div>
                <div class="apple-pay-method-radio">
                  <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
                </div>
              </div>

              <!-- Visa / Mastercard -->
              <div class="apple-pay-method-card ${selectedMethod === 'visa' ? 'active' : ''} ${pack.price >= VISA_MIN_RUB ? '' : 'is-dimmed'}" data-pay="visa" role="radio" tabindex="0" aria-checked="${selectedMethod === 'visa' ? 'true' : 'false'}" aria-label="Visa / Mastercard — Международные карты">
                <div class="apple-pay-method-left">
                  <div class="apple-pay-icon-box visa-icon-box">
                    <svg width="32" height="20" viewBox="0 0 32 20" fill="none">
                      <circle cx="12" cy="10" r="7" fill="#EB001B" fill-opacity="0.9"/>
                      <circle cx="20" cy="10" r="7" fill="#F79E1B" fill-opacity="0.85"/>
                      <path d="M16 5.8A7 7 0 0 1 16 14.2A7 7 0 0 1 16 5.8Z" fill="#FF5F00"/>
                    </svg>
                  </div>
                  <div class="apple-pay-method-info">
                    <div class="apple-pay-method-title-row">
                      <span class="apple-pay-method-title">Visa / Mastercard</span>
                      <span class="apple-pay-badge ${pack.price >= VISA_MIN_RUB ? 'badge-amber' : 'badge-muted'} visa-badge">${pack.price >= VISA_MIN_RUB ? 'Международные' : 'от ' + VISA_MIN_RUB + ' ₽'}</span>
                    </div>
                    <span class="apple-pay-method-sub">Карты СНГ, Европы и зарубежных банков</span>
                  </div>
                </div>
                <div class="apple-pay-method-radio">
                  <svg class="apple-pay-radio-check" width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
                </div>
              </div>
            </div>
          </div>

          <div class="apple-modal-total-card">
            <div>
              <div class="apple-modal-total-label">К зачислению:</div>
              <small class="aqua-coin-badge" style="color:#ffd875;font-weight:700;display:inline-flex;align-items:center;gap:0.25rem">${Number(pack.coins).toLocaleString("ru-RU")} <img src="assets/logo.png" class="aqua-coin-icon" alt="AquaCoins" /></small>
            </div>
            <div class="apple-modal-total-price">${pack.price} ₽</div>
          </div>

          <div style="margin-top:0.75rem">
            <button class="apple-modal-submit-btn" id="coinBuyConfirmBtn" type="button" style="background:linear-gradient(135deg,#ffd875,#f59e0b);color:#091924;font-weight:800;border:none">
              <span>${selectedMethod === "balance" ? `Купить за ${pack.price} ₽ (с баланса)` : selectedMethod === "card" ? `Оплатить ${pack.price} ₽ (картой МИР)` : selectedMethod === "visa" ? `Оплатить ${pack.price} ₽ (картой Visa / MC)` : `Оплатить ${pack.price} ₽ (через СБП)`}</span>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
          </div>

          <div id="coinModalMsg" class="apple-modal-status-msg" style="display:none;margin-top:1rem"></div>
        </div>
      `;

      modal.style.display = "flex";
      requestAnimationFrame(() => modal.classList.add("open"));

      modal.querySelector(".apple-modal-close").onclick = () => {
        modal.classList.remove("open");
        setTimeout(() => { modal.style.display = "none"; }, 250);
      };

      modal.querySelectorAll("[data-coin-slug]").forEach((c) => {
        c.onclick = () => {
          selectedSlug = c.getAttribute("data-coin-slug");
          playTone("click");
          render();
        };
      });

      function updateButton() {
        const btnSpan = modal.querySelector("#coinBuyConfirmBtn span");
        if (!btnSpan) return;
        if (selectedMethod === "balance") {
          btnSpan.textContent = `Купить за ${pack.price} ₽ (с баланса)`;
        } else if (selectedMethod === "card") {
          btnSpan.textContent = `Оплатить ${pack.price} ₽ (картой МИР)`;
        } else if (selectedMethod === "visa") {
          btnSpan.textContent = `Оплатить ${pack.price} ₽ (картой Visa / MC)`;
        } else {
          btnSpan.textContent = `Оплатить ${pack.price} ₽ (через СБП)`;
        }
      }

      modal.querySelectorAll("[data-pay]").forEach((m) => {
        const handleSelect = () => {
          const method = m.getAttribute("data-pay");
          if (method === "balance" && !canBalance) {
            playTone("warn");
            toast(`Недостаточно средств на балансе (${userRub} ₽). Пополни баланс или выбери СБП / Карту.`);
            return;
          }
          if (method === "visa" && pack.price < VISA_MIN_RUB) {
            playTone("warn");
            toast(`Visa / Mastercard доступна для сумм от ${VISA_MIN_RUB} ₽. Для ${pack.price} ₽ используй СБП или МИР.`);
            return;
          }
          selectedMethod = method;
          playTone("click");
          modal.querySelectorAll("[data-pay]").forEach((c) => {
            c.classList.remove("active");
            c.setAttribute("aria-checked", "false");
          });
          m.classList.add("active");
          m.setAttribute("aria-checked", "true");
          updateButton();
        };
        m.onclick = handleSelect;
        m.onkeydown = (e) => {
          if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            handleSelect();
          }
        };
      });

      const confirmBtn = modal.querySelector("#coinBuyConfirmBtn");
      const msg = modal.querySelector("#coinModalMsg");

      confirmBtn.onclick = async () => {
        confirmBtn.disabled = true;
        confirmBtn.querySelector("span").textContent = "Обработка…";
        msg.style.display = "none";

        try {
          const res = await api("/api/purchase", {
            method: "POST",
            body: JSON.stringify({ slug: selectedSlug, method: selectedMethod })
          });

          if (res.type === "redirect" && res.url) {
            window.location.href = res.url;
            return;
          }

          playTone("ok");
          msg.style.display = "block";
          msg.innerHTML = `
            <div style="background:rgba(245,194,91,0.15);border:1px solid #ffd875;border-radius:14px;padding:1rem;color:#ffd875;text-align:center">
              <h4 style="margin:0 0 0.25rem">✅ Монеты начислены!</h4>
              <p style="margin:0;color:#f8fafc;font-size:0.9rem">${esc(res.message || "Монеты успешно добавлены на аккаунт!")}</p>
            </div>
          `;
          toast("Монеты зачислены!");
          setTimeout(() => { location.reload(); }, 1200);
        } catch (err) {
          playTone("warn");
          msg.style.display = "block";
          msg.innerHTML = `
            <div style="background:rgba(239,68,68,0.12);border:1px solid #ef4444;border-radius:14px;padding:1rem;color:#f87171;text-align:center">
              ${esc(err.message || "Ошибка покупки монет.")}
            </div>
          `;
          confirmBtn.disabled = false;
          updateButton();
        }
      };
    }

    render();
  }

  function openTopupModal(defaultRub = 100) {
    window.openTopupModal = openTopupModal;
    if (!getUser()) {
      toast("Сначала войди в аккаунт");
      location.href = "login.html";
      return;
    }
    let modal = document.getElementById("topup-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "topup-modal";
      modal.className = "store-modal-overlay";
      document.body.appendChild(modal);
    }

    const user = getUser();
    const defaultNick = user ? user.nick : "";

    modal.innerHTML = `
      <div class="apple-modal-box" style="max-width: 440px">
        <div class="apple-grabber"></div>
        <div class="apple-modal-header">
          <div>
            <h3 style="margin:0 0 0.25rem;font-size:1.35rem;font-weight:700">Пополнение счёта</h3>
            <p class="apple-modal-subtitle">Зачисление на аккаунт <b style="color:#ffffff">${esc(defaultNick || 'игрока')}</b></p>
          </div>
          <button class="apple-modal-close" type="button" aria-label="Закрыть">✕</button>
        </div>

        <div class="apple-segmented-pills">
          <button type="button" class="apple-pill-btn ${defaultRub === 100 ? 'active' : ''}" data-amount="100">100 ₽</button>
          <button type="button" class="apple-pill-btn ${defaultRub === 250 ? 'active' : ''}" data-amount="250">250 ₽</button>
          <button type="button" class="apple-pill-btn ${defaultRub === 500 ? 'active' : ''}" data-amount="500">500 ₽</button>
          <button type="button" class="apple-pill-btn ${defaultRub === 1000 ? 'active' : ''}" data-amount="1000">1000 ₽</button>
        </div>

        <div class="apple-amount-card">
          <div class="apple-amount-top">
            <span class="apple-amount-label">Сумма пополнения</span>
            <span class="apple-amount-hint">от 10 до 50 000 ₽</span>
          </div>
          <div class="apple-amount-input-row">
            <span class="apple-currency-symbol">₽</span>
            <input type="number" id="topupAmountInput" class="apple-amount-number-input" value="${defaultRub}" min="10" max="50000" placeholder="0" inputmode="numeric" />
            <span class="apple-amount-badge">RUB</span>
          </div>
        </div>

        <div style="font-size:0.75rem;font-weight:600;text-transform:uppercase;letter-spacing:0.06em;color:rgba(255,255,255,0.45);margin-bottom:0.4rem;padding-left:0.2rem">
          Способ оплаты
        </div>
        <div class="apple-inset-group" role="radiogroup" aria-label="Способ оплаты">
          <!-- СБП Row -->
          <div class="apple-inset-row active" data-pay="sbp" role="radio" tabindex="0" aria-checked="true" aria-label="СБП — 0% комиссия">
            <div class="apple-row-left">
              <div class="apple-row-icon">
                <svg width="22" height="22" viewBox="0 0 28 28" fill="none">
                  <path d="M14 3L18 9H10L14 3Z" fill="#F4B400"/>
                  <path d="M25 14L19 18V10L25 14Z" fill="#0077FF"/>
                  <path d="M14 25L10 19H18L14 25Z" fill="#00A859"/>
                  <path d="M3 14L9 10V18L3 14Z" fill="#E84135"/>
                  <circle cx="14" cy="14" r="3" fill="#FFFFFF"/>
                </svg>
              </div>
              <div class="apple-row-text">
                <div class="apple-row-title">СБП <span class="apple-pay-badge badge-green" style="font-size:0.65rem;padding:0.1rem 0.35rem">0%</span></div>
                <div class="apple-row-sub">Быстрый перевод по QR в любом банке</div>
              </div>
            </div>
            <div class="apple-check-circle">
              <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
            </div>
          </div>

          <!-- МИР Row -->
          <div class="apple-inset-row" data-pay="card" role="radio" tabindex="0" aria-checked="false" aria-label="Карты МИР — карты РФ">
            <div class="apple-row-left">
              <div class="apple-row-icon">
                <svg width="24" height="16" viewBox="0 0 30 20" fill="none">
                  <rect width="30" height="20" rx="4" fill="#0d281e" stroke="#0ecb81" stroke-width="1.2"/>
                  <text x="15" y="14" text-anchor="middle" font-family="-apple-system, sans-serif" font-weight="900" font-size="10" fill="#0ecb81">МИР</text>
                </svg>
              </div>
              <div class="apple-row-text">
                <div class="apple-row-title">Карты МИР <span class="apple-pay-badge badge-cyan" style="font-size:0.65rem;padding:0.1rem 0.35rem">РФ</span></div>
                <div class="apple-row-sub">Сбер, Т-Банк, ВТБ, Альфа и др.</div>
              </div>
            </div>
            <div class="apple-check-circle">
              <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
            </div>
          </div>

          <!-- Visa Row -->
          <div class="apple-inset-row ${defaultRub >= VISA_MIN_RUB ? '' : 'is-dimmed'}" data-pay="visa" role="radio" tabindex="0" aria-checked="false" aria-label="Visa / Mastercard — Международные карты">
            <div class="apple-row-left">
              <div class="apple-row-icon">
                <svg width="24" height="16" viewBox="0 0 32 20" fill="none">
                  <circle cx="12" cy="10" r="7" fill="#EB001B"/>
                  <circle cx="20" cy="10" r="7" fill="#F79E1B" fill-opacity="0.85"/>
                </svg>
              </div>
              <div class="apple-row-text">
                <div class="apple-row-title">Visa / Mastercard <span class="apple-pay-badge ${defaultRub >= VISA_MIN_RUB ? 'badge-amber' : 'badge-muted'} visa-badge" style="font-size:0.65rem;padding:0.1rem 0.35rem">${defaultRub >= VISA_MIN_RUB ? 'Международные' : 'от ' + VISA_MIN_RUB + ' ₽'}</span></div>
                <div class="apple-row-sub">Зарубежные карты и СНГ</div>
              </div>
            </div>
            <div class="apple-check-circle">
              <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>
            </div>
          </div>
        </div>

        <!-- Summary Line Items -->
        <div class="apple-line-item">
          <span>Сумма к зачислению</span>
          <span id="topupLineAmount">${Number(defaultRub).toLocaleString("ru-RU")} ₽</span>
        </div>
        <div class="apple-line-item">
          <span>Комиссия шлюза</span>
          <span style="color:#2fe0c0">0 ₽ (0%)</span>
        </div>
        <div class="apple-line-item total">
          <span>Итого к оплате:</span>
          <span id="topupLineTotal">${Number(defaultRub).toLocaleString("ru-RU")} ₽</span>
        </div>

        <button class="apple-primary-btn" id="topupSubmitBtn" type="button">
          <span>Оплатить ${Number(defaultRub).toLocaleString("ru-RU")} ₽ через СБП</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
        <div class="apple-notice-text">Безопасный платёжный шлюз Lava · Моментальное зачисление</div>

        <div id="topupModalMsg" class="apple-modal-status-msg" style="display:none;margin-top:1rem"></div>
      </div>
    `;

    modal.style.display = "flex";
    requestAnimationFrame(() => modal.classList.add("open"));

    function closeTopup() {
      modal.classList.remove("open");
      setTimeout(() => { modal.style.display = "none"; }, 250);
    }

    modal.querySelector(".apple-modal-close")?.addEventListener("click", closeTopup);
    modal.onclick = (e) => { if (e.target === modal) closeTopup(); };

    const amountInput = modal.querySelector("#topupAmountInput");
    const lineAmount = modal.querySelector("#topupLineAmount");
    const lineTotal = modal.querySelector("#topupLineTotal");
    const submitBtn = modal.querySelector("#topupSubmitBtn");
    const topupMsg = modal.querySelector("#topupModalMsg");
    let topupPay = "sbp";

    function updateSubmitLabel() {
      const val = Math.max(10, Number(amountInput.value || 0));
      const payLabel = topupPay === "sbp" ? "через СБП" : topupPay === "visa" ? "картой Visa / MC" : "картой МИР";
      submitBtn.querySelector("span").textContent = `Оплатить ${val.toLocaleString("ru-RU")} ₽ ${payLabel}`;
    }

    const visaRow = modal.querySelector('[data-pay="visa"]');
    function syncVisaAvail(val) {
      if (!visaRow) return;
      const ok = val >= VISA_MIN_RUB;
      visaRow.classList.toggle("is-dimmed", !ok);
      const badge = visaRow.querySelector(".visa-badge");
      if (badge) {
        badge.textContent = ok ? "Международные" : "от " + VISA_MIN_RUB + " ₽";
        badge.className = `apple-pay-badge ${ok ? 'badge-amber' : 'badge-muted'} visa-badge`;
      }
      updateSubmitLabel();
    }

    function updateCoins() {
      const val = Math.max(10, Number(amountInput.value || 0));
      if (lineAmount) lineAmount.textContent = `${val.toLocaleString("ru-RU")} ₽`;
      if (lineTotal) lineTotal.textContent = `${val.toLocaleString("ru-RU")} ₽`;
      updateSubmitLabel();
      syncVisaAvail(val);
    }

    amountInput.addEventListener("input", () => {
      const val = Number(amountInput.value || 0);
      modal.querySelectorAll(".apple-pill-btn").forEach((b) => {
        b.classList.toggle("active", Number(b.getAttribute("data-amount")) === val);
      });
      updateCoins();
    });

    modal.querySelectorAll(".apple-pill-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        modal.querySelectorAll(".apple-pill-btn").forEach((b) => b.classList.remove("active"));
        btn.classList.add("active");
        amountInput.value = btn.getAttribute("data-amount");
        updateCoins();
        playTone("click");
      });
    });

    modal.querySelectorAll(".apple-inset-row").forEach((card) => {
      const selectCard = () => {
        const next = card.getAttribute("data-pay") || "sbp";
        const val = Math.max(10, Number(amountInput.value || 0));
        if (next === "visa" && val < VISA_MIN_RUB) {
          amountInput.value = VISA_MIN_RUB;
          modal.querySelectorAll(".apple-pill-btn").forEach((b) => b.classList.remove("active"));
          updateCoins();
          toast(`Сумма обновлена до ${VISA_MIN_RUB} ₽ (минимум для Visa/Mastercard)`);
        }
        modal.querySelectorAll(".apple-inset-row").forEach((c) => {
          c.classList.remove("active");
          c.setAttribute("aria-checked", "false");
          const check = c.querySelector(".apple-check-circle");
          if (check) check.innerHTML = "";
        });
        card.classList.add("active");
        card.setAttribute("aria-checked", "true");
        const check = card.querySelector(".apple-check-circle");
        if (check) {
          check.innerHTML = '<svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="2.5 7 5.5 10 11.5 4"/></svg>';
        }
        topupPay = next;
        updateSubmitLabel();
        playTone("click");
      };

      card.addEventListener("click", selectCard);
      card.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          selectCard();
        }
      });
    });

    modal.querySelector(".apple-amount-card")?.addEventListener("click", (e) => {
      if (e.target !== amountInput) {
        amountInput.focus();
      }
    });

    submitBtn.onclick = async () => {
      const nick = (modal.querySelector("#topupNick")?.value || defaultNick || "").trim();
      const amount = Number(amountInput.value || 100);
      const promo = (modal.querySelector("#topupPromo")?.value || "").trim();

      if (!nick) {
        toast("Введи ник игрока");
        return;
      }
      if (topupPay === "visa" && amount < VISA_MIN_RUB) {
        toast("Visa / Mastercard от " + VISA_MIN_RUB + " ₽");
        amountInput.value = VISA_MIN_RUB;
        updateCoins();
        return;
      }
      submitBtn.disabled = true;
      submitBtn.querySelector("span").textContent = "Создание счёта…";
      topupMsg.style.display = "none";

      try {
        const res = await api("/api/purchase", {
          method: "POST",
          body: JSON.stringify({ action: "topup", nick, amount, promo, method: topupPay })
        });

        if (res.type === "redirect" && res.url) {
          topupMsg.style.display = "block";
          topupMsg.innerHTML = `<span style="color:#2fe0c0">Открываем ${topupPay === "sbp" ? "СБП" : topupPay === "visa" ? "Visa / MC" : "МИР"}${res.host ? " (" + esc(res.host) + ")" : ""}…</span>`;
          window.location.href = res.url;
          return;
        }

        playTone("ok");
        topupMsg.style.display = "block";
        topupMsg.innerHTML = `
          <div style="background:rgba(47,224,192,0.12);border:1px solid #2fe0c0;border-radius:14px;padding:1.1rem;color:#2fe0c0;text-align:center">
            <h4 style="margin:0 0 0.3rem">✅ Баланс пополнен!</h4>
            <p style="margin:0 0 0.4rem;color:#f8fafc">+${Number(res.rubles_added || amount).toLocaleString("ru-RU")} ₽ зачислено на аккаунт <b>${esc(nick)}</b></p>
            <small style="color:#2fe0c0">Текущий баланс: <b>${Number(res.balance_rub || 0).toLocaleString("ru-RU")} ₽</b></small>
          </div>
        `;
        toast("Баланс пополнен!");
        if (typeof res.balance_rub === "number") {
          const u = getUser();
          if (u) {
            setUser({ ...u, rub_balance: Number(res.balance_rub) });
            renderHeader();
          }
        }
      } catch (err) {
        playTone("warn");
        topupMsg.style.display = "block";
        topupMsg.innerHTML = `
          <div style="background:rgba(239,68,68,0.12);border:1px solid #ef4444;border-radius:14px;padding:1rem;color:#f87171;text-align:center">
            ${esc(err.message || "Не удалось пополнить баланс.")}
          </div>
        `;
        submitBtn.disabled = false;
        submitBtn.querySelector("span").textContent = `Пополнить на ${amount} ₽`;
      }
    };
  }

  // 2. CASES SHOWCASE (APPLE HIG BENTO SYSTEM)
  async function initCasesShowcasePage() {
    const root = $("#cases-showcase-root");
    if (!root) return;

    let casesList = [];
    try {
      const res = await fetch("/data/cases.json");
      if (res.ok) {
        const data = await res.json();
        casesList = data && data.cases ? data.cases : [];
      }
    } catch (e) {
      console.warn("Could not fetch /data/cases.json, using fallback", e);
    }

    if (!casesList.length) {
      casesList = [
        { slug: "starter", title: "Кейс I: Первопроходец Океана", cost: 2500, rarity: "common", desc: "Стартовые руды, медные слитки, редстоун и базовые инструменты выживания." },
        { slug: "smeltery", title: "Кейс II: Инженер Плавильни", cost: 10000, rarity: "uncommon", desc: "Бронза, инвар, сплавы плавильни, термоэлектрические пластины и редкие кристаллы." },
        { slug: "steam", title: "Кейс III: Паровая Энергия", cost: 45000, rarity: "rare", desc: "Паровые котлы, стальные механизмы, поршни высокого давления и шестерни." },
        { slug: "flora", title: "Кейс IV: Ботаническая Флора", cost: 150000, rarity: "epic", desc: "Магические лепестки, жизнедерево, руны стихий и мистические цветы Botania." },
        { slug: "applied", title: "Кейс V: Цифровая МЭ-Сеть", cost: 450000, rarity: "epic", desc: "МЭ-процессоры, логические и инженерные прессы, флюикс-кристаллы и кабели." },
        { slug: "abyss", title: "Кейс VI: Глубины и Радиация", cost: 1200000, rarity: "legendary", desc: "Глубоководный титан, уран, защитные сплавы и редчайшие артефакты впадины." },
        { slug: "superconductor", title: "Кейс VII: Сверхпроводники", cost: 2500000, rarity: "legendary", desc: "Криогенные кабели, сверхпроводящие катушки, платина и квантовые микрочипы." },
        { slug: "singularity", title: "Кейс VIII: Матрица Сингулярности", cost: 4500000, rarity: "mythic", desc: "Сжатые сингулярности, нейтроний, темная материя и квантовые конденсаторы." },
        { slug: "draconic", title: "Кейс IX: Дракониевое Слияние", cost: 6500000, rarity: "mythic", desc: "Ядра дракона, пробужденный драконий, виверн-аккумуляторы и сердца Края." },
        { slug: "infinity", title: "Кейс X: Абсолютная Бесконечность", cost: 8000000, rarity: "exotic", desc: "Катализаторы бесконечности Avaritia, слитки космоса и высшие механизмы." }
      ];
    }

    window.__liveCases = casesList;

    const coinSvg = '<img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt="">';

    let activeFilter = "all";

    function renderCases() {
      const filtered = casesList.filter((c, idx) => {
        const tier = idx + 1;
        if (activeFilter === "early") return tier >= 1 && tier <= 3;
        if (activeFilter === "mid") return tier >= 4 && tier <= 6;
        if (activeFilter === "late") return tier >= 7 && tier <= 10;
        return true;
      });

      root.innerHTML = filtered.map((c) => {
        const actualIndex = casesList.findIndex((item) => item.slug === c.slug);
        const tierNum = actualIndex + 1;
        const iconSrc = CASE_ICONS[c.slug] || CASE_ICONS.starter || "assets/logo.png";
        
        // Extract top 3 loot rewards for preview chips
        const topLoot = c.loot && c.loot.length
          ? [...c.loot].sort((a, b) => b.weight - a.weight).slice(0, 3)
          : [];

        return `
          <div class="case-apple-card">
            <div class="case-apple-top-row">
              <span class="case-tier-pill">ТИР ${tierNum}</span>
              <span class="rarity-badge rarity-${esc(c.rarity)}">${RARITY_LABEL[c.rarity] || esc(c.rarity)}</span>
            </div>
            
            <div class="case-apple-orb rarity-${esc(c.rarity)}">
              <img class="case-apple-img" src="${iconSrc}" alt="${esc(c.title)}" loading="lazy" />
            </div>

            <h3 class="case-apple-title">${esc(c.title)}</h3>
            <p class="case-apple-desc">${esc(c.description || c.desc || "Награды и материалы прогрессии сервера.")}</p>

            ${topLoot.length ? `
              <div class="case-loot-chips">
                ${topLoot.map((item) => `<span class="loot-chip" title="${esc(item.name)}">${esc(item.name)}</span>`).join("")}
              </div>
            ` : ""}

            <div class="case-apple-footer">
              <div class="case-apple-cost-row">
                <span class="case-cost-label">Открытие:</span>
                <span class="case-cost-value">
                  ${coinSvg}
                  <span>${Number(c.cost).toLocaleString("ru-RU")}</span>
                </span>
              </div>
              <div class="case-apple-actions">
                <button class="btn-case-apple-spin" type="button" data-spin-case="${esc(c.slug)}">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 1 10 10"/><path d="M12 6v6l4 2"/></svg>
                  <span>Крутить барабан</span>
                </button>
                <button class="btn-case-apple-view" type="button" data-view-loot="${esc(c.slug)}" title="Содержимое кейса" aria-label="Содержимое кейса">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
                </button>
              </div>
            </div>
          </div>
        `;
      }).join("");

      root.querySelectorAll("[data-spin-case]").forEach((btn) => {
        btn.addEventListener("click", () => {
          const slug = btn.getAttribute("data-spin-case");
          const caseObj = (window.__liveCases || []).find((item) => item.slug === slug) || {};
          openRouletteModal(slug, caseObj.title || "Кейс", caseObj.cost || 0);
        });
      });

      root.querySelectorAll("[data-view-loot]").forEach((btn) => {
        btn.addEventListener("click", () => {
          const slug = btn.getAttribute("data-view-loot");
          const caseObj = (window.__liveCases || []).find((item) => item.slug === slug);
          if (caseObj && caseObj.loot) {
            openLiveLootModal(caseObj);
          } else {
            openLootModal(slug);
          }
        });
      });
    }

    // Segmented filter button handlers
    document.querySelectorAll(".apple-segment-btn[data-filter]").forEach((btn) => {
      btn.addEventListener("click", () => {
        document.querySelectorAll(".apple-segment-btn[data-filter]").forEach((b) => b.classList.remove("active"));
        btn.classList.add("active");
        activeFilter = btn.getAttribute("data-filter");
        playTone("click");
        renderCases();
      });
    });

    renderCases();
  }

  // INTERACTIVE 3D ROULETTE DRUM CONNECTED TO LIVE /api/cases/open (APPLE HIG)
  function openRouletteModal(slug, caseTitle, caseCost = 0) {
    let modal = document.getElementById("roulette-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "roulette-modal";
      modal.className = "roulette-modal-overlay open";
      document.body.appendChild(modal);
    } else {
      modal.className = "roulette-modal-overlay open";
    }

    const liveCase = (window.__liveCases || []).find((c) => c.slug === slug);
    const loot = (liveCase && liveCase.loot && liveCase.loot.length)
      ? liveCase.loot
      : (CASE_LOOT_TABLES[slug] || [
          { name: "Железные слитки", rarity: "common", item: "minecraft:iron_ingot", min: 16 },
          { name: "Медные слитки", rarity: "common", item: "minecraft:copper_ingot", min: 24 },
          { name: "Редстоун", rarity: "uncommon", item: "minecraft:redstone", min: 32 },
          { name: "Алмазы", rarity: "rare", item: "minecraft:diamond", min: 3 },
          { name: "Титановый слиток", rarity: "epic", item: "aquatech_ui:titanium_ingot", min: 2 },
          { name: "Альфа-удочка T13", rarity: "legendary", item: "aquatech_ui:alpha_rod", min: 1 }
        ]);

    const actualIdx = (window.__liveCases || []).findIndex((c) => c.slug === slug);
    const tierNum = actualIdx >= 0 ? actualIdx + 1 : 1;
    const caseIconSrc = CASE_ICONS[slug] || "assets/images/cases/starter.png";

    const u = getUser();
    const isAuthed = !!(u && u.nick);
    const userCoins = u && u.coins != null ? Number(u.coins) : 0;
    const coinSvg = '<img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt="">';
    const spinSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 1 10 10"/><path d="M12 6v6l4 2"/></svg>';

    // Build 60 tiles for initial drum display
    const tiles = [];
    for (let i = 0; i < 60; i++) {
      const item = loot[Math.floor(Math.random() * loot.length)];
      tiles.push({
        name: item.name,
        rarity: item.rarity || "rare",
        item: item.item || "",
        amount: item.min || 1,
      });
    }

    const winIndex = 45;

    function renderStripTiles(items) {
      return items.map((t, idx) => {
        const iconUrl = getItemIconUrl(t.item);
        const rarity = t.rarity || "rare";
        const count = t.amount || t.min || 1;
        return `
          <div class="roulette-item-tile rarity-${rarity}" id="tile-${idx}">
            ${iconUrl
              ? `<img class="roulette-item-ico" src="${iconUrl}" alt="${esc(t.name)}" onerror="this.style.display='none';if(this.nextElementSibling)this.nextElementSibling.style.display='flex';" /><div class="roulette-item-fallback" style="display:none">◆</div>`
              : `<div class="roulette-item-fallback">◆</div>`
            }
            ${count > 1 ? `<span class="roulette-item-count">×${count}</span>` : ""}
            <div class="roulette-item-name" title="${esc(t.name)}">${esc(t.name)}</div>
          </div>
        `;
      }).join("");
    }

    modal.innerHTML = `
      <div class="roulette-modal-box">
        <div class="roulette-header">
          <div class="roulette-title-group">
            <div class="roulette-eyebrow">
              <span class="eyebrow-dot"></span>
              <span>ТИР ${tierNum} · ВИТРИНА КЕЙСА</span>
            </div>
            <h3>
              <img class="roulette-case-ico" src="${caseIconSrc}" alt="${esc(caseTitle)}" />
              <span>${esc(caseTitle)}</span>
            </h3>
          </div>
          <button class="roulette-close-btn" type="button" aria-label="Закрыть">✕</button>
        </div>

        <div class="roulette-info-bar">
          <div class="info-pill cost-pill">
            <span class="pill-label">Стоимость:</span>
            <span class="pill-value gold-text">${Number(caseCost).toLocaleString("ru-RU")} ${coinSvg}</span>
          </div>
          ${isAuthed ? `
            <div class="info-pill balance-pill">
              <span class="pill-label">Баланс:</span>
              <span class="pill-value cyan-text" id="rouletteUserBalance">${userCoins.toLocaleString("ru-RU")} ${coinSvg}</span>
            </div>
          ` : `
            <div class="info-pill guest-pill">
              <span style="color:#f5c25b">★</span>
              <span>Демо-режим · <a href="login.html" class="guest-login-link">Войти для выдачи</a></span>
            </div>
          `}
        </div>

        <div class="roulette-stage">
          <div class="roulette-viewport" id="rouletteViewport">
            <div class="roulette-edge-vignette vignette-left"></div>
            <div class="roulette-edge-vignette vignette-right"></div>
            <div class="roulette-needle"></div>
            <div class="roulette-strip" id="rouletteStrip">
              ${renderStripTiles(tiles)}
            </div>
          </div>
        </div>

        <div class="roulette-spin-action">
          <button class="btn-spin-now" id="btnSpinNow" type="button">
            ${spinSvg}
            <span>${isAuthed ? "КРУТИТЬ БАРАБАН" : "ДЕМО-КРУТКА"}</span>
          </button>
          <p class="roulette-hint" id="rouletteHint">
            ${isAuthed
              ? "Предмет моментально выдается в инвентарь на сервере (меню F4) и заносится в хранилище профиля."
              : "Демонстрационный режим. Войдите в аккаунт, чтобы крутить за монеты и получать дроп на сервере."}
          </p>
        </div>

        <div id="rouletteWinContainer"></div>
      </div>
    `;

    function closeModal() {
      modal.classList.remove("open");
      document.removeEventListener("keydown", onKeyEsc);
      setTimeout(() => modal.remove(), 250);
    }

    function onKeyEsc(e) {
      if (e.key === "Escape") closeModal();
    }

    modal.querySelector(".roulette-close-btn").onclick = closeModal;
    modal.onclick = (e) => { if (e.target === modal) closeModal(); };
    document.addEventListener("keydown", onKeyEsc);

    const strip = modal.querySelector("#rouletteStrip");
    const spinBtn = modal.querySelector("#btnSpinNow");
    const winContainer = modal.querySelector("#rouletteWinContainer");
    const btnTextEl = spinBtn.querySelector("span");

    spinBtn.onclick = async () => {
      spinBtn.disabled = true;
      winContainer.innerHTML = "";
      modal.querySelectorAll(".roulette-item-tile.is-winner").forEach((el) => el.classList.remove("is-winner"));

      let winItem = null;

      if (isAuthed) {
        btnTextEl.textContent = "СВЯЗЬ С СЕРВЕРОМ…";
        try {
          const res = await api("/api/cases/open", {
            method: "POST",
            body: JSON.stringify({ slug }),
          });

          if (!res || !res.ok || !res.loot) {
            throw new Error(res.error || "Не удалось открыть кейс");
          }

          // Atomically update local user balance
          if (res.remainingCoins != null) {
            u.coins = res.remainingCoins;
            setUser(u);
            const balEl = modal.querySelector("#rouletteUserBalance");
            if (balEl) balEl.innerHTML = `${Number(u.coins).toLocaleString("ru-RU")} ${coinSvg}`;
            const hdrCoins = document.querySelector("#hdrUserCoins");
            if (hdrCoins) hdrCoins.textContent = Number(u.coins).toLocaleString("ru-RU");
          }

          winItem = {
            name: res.loot.name,
            rarity: res.loot.rarity || liveCase?.rarity || "rare",
            item: res.loot.item || "",
            amount: res.loot.amount || 1,
            isReal: true,
          };
        } catch (err) {
          winContainer.innerHTML = `
            <div class="roulette-error-card">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <div>
                <strong>${esc(err.message || "Ошибка открытия кейса")}</strong>
                <div style="font-size:0.8rem;color:#fca5a5;margin-top:0.2rem">Пополните баланс монет в магазине или откройте кейс в игре (клавиша F4).</div>
              </div>
            </div>
          `;
          spinBtn.disabled = false;
          btnTextEl.textContent = isAuthed ? "КРУТИТЬ БАРАБАН" : "ДЕМО-КРУТКА";
          return;
        }
      } else {
        // Free Interactive Demo spin
        const pick = loot[Math.floor(Math.random() * loot.length)];
        winItem = {
          name: pick.name,
          rarity: pick.rarity || "rare",
          item: pick.item || "",
          amount: pick.min || 1,
          isReal: false,
        };
      }

      btnTextEl.textContent = "КРУТИМ БАРАБАН…";

      // Rebuild tiles with the guaranteed winning item at winIndex
      tiles[winIndex] = winItem;
      strip.innerHTML = renderStripTiles(tiles);

      playTone("ok");

      // Tile width: 116px width + 10px gap = 126px
      const tileWidth = 126;
      const viewport = modal.querySelector(".roulette-viewport");
      const viewportW = viewport ? viewport.clientWidth : 700;
      const targetOffset = (winIndex * tileWidth) - (viewportW / 2) + (tileWidth / 2) + (Math.random() * 24 - 12);

      strip.style.transition = "none";
      strip.style.transform = "translateX(0px)";

      // Force layout flush
      void strip.offsetWidth;

      // Realistic mechanical deceleration ticks
      let startTime = performance.now();
      const spinDuration = 4500;
      let tickTimer = null;

      function scheduleTick() {
        const elapsed = performance.now() - startTime;
        if (elapsed >= spinDuration - 120) return;
        playTone("tick");
        const progress = Math.min(1, elapsed / spinDuration);
        const nextDelay = 55 + Math.pow(progress, 2.6) * 350;
        tickTimer = setTimeout(scheduleTick, nextDelay);
      }
      setTimeout(scheduleTick, 55);

      requestAnimationFrame(() => {
        strip.style.transition = "transform 4.5s cubic-bezier(0.12, 0.8, 0.2, 1)";
        strip.style.transform = `translateX(-${targetOffset}px)`;
      });

      setTimeout(() => {
        clearTimeout(tickTimer);
        playTone("ok");
        spinBtn.disabled = false;
        btnTextEl.textContent = "КРУТИТЬ ЕЩЁ РАЗ";

        const winningTile = document.getElementById(`tile-${winIndex}`);
        if (winningTile) winningTile.classList.add("is-winner");

        const winIconUrl = getItemIconUrl(winItem.item);
        const rarityLabel = RARITY_LABEL[winItem.rarity] || "Выигрыш";

        if (winItem.isReal) {
          winContainer.innerHTML = `
            <div class="roulette-win-card">
              <div class="roulette-win-ico-box">
                ${winIconUrl
                  ? `<img class="roulette-win-ico" src="${winIconUrl}" alt="${esc(winItem.name)}" onerror="this.style.display='none';this.nextElementSibling.style.display='block';" /><span style="display:none;font-size:1.8rem;color:#2fe0c0">✦</span>`
                  : `<span style="font-size:1.8rem;color:#2fe0c0">✦</span>`
                }
              </div>
              <div class="roulette-win-content">
                <div class="roulette-win-badge badge-real">✓ ${rarityLabel} · Доставлено в игру</div>
                <div class="roulette-win-name">${esc(winItem.name)} ${winItem.amount > 1 ? `×${winItem.amount}` : ""}</div>
                <div class="roulette-win-desc">Предмет моментально выдан в твой инвентарь на сервере (меню F4) и зачислен в хранилище профиля.</div>
              </div>
            </div>
          `;
        } else {
          winContainer.innerHTML = `
            <div class="roulette-win-card">
              <div class="roulette-win-ico-box" style="border-color:rgba(245,194,91,0.35);background:rgba(245,194,91,0.12)">
                ${winIconUrl
                  ? `<img class="roulette-win-ico" src="${winIconUrl}" alt="${esc(winItem.name)}" onerror="this.style.display='none';this.nextElementSibling.style.display='block';" /><span style="display:none;font-size:1.8rem;color:#f5c25b">★</span>`
                  : `<span style="font-size:1.8rem;color:#f5c25b">★</span>`
                }
              </div>
              <div class="roulette-win-content">
                <div class="roulette-win-badge badge-demo">★ ${rarityLabel} · Демо-крутка</div>
                <div class="roulette-win-name">${esc(winItem.name)} ${winItem.amount > 1 ? `×${winItem.amount}` : ""}</div>
                <div class="roulette-win-desc"><a href="login.html" class="guest-login-link">Войдите в аккаунт</a>, чтобы крутить кейсы за монеты и получать дроп в игре (клавиша F4).</div>
              </div>
            </div>
          `;
        }
      }, 4700);
    };
  }

  // 3. MARKET PAGE WITH SEARCH & FILTER
  async function initMarketPage() {
    const grid = $("#marketLotsGrid");
    const countEl = $("#marketLotCounter");
    const searchInput = $("#marketSearchInput");
    const pills = $("#marketFilterPills");
    if (!grid) return;

    let allLots = [];
    try {
      const res = await api("/api/market/public?limit=30");
      allLots = res && res.items ? res.items : [];
    } catch {
      allLots = [];
    }

    if (!allLots.length) {
      allLots = [
        { item_name: "StarCatcher Magma Rod T9", seller_nick: "AquaSmoke1", price: 4500, amount: 1, expires_in: "через 14 ч", category: "rods" },
        { item_name: "Титановый слиток x64", seller_nick: "Renfild", price: 1200, amount: 64, expires_in: "через 22 ч", category: "ores" },
        { item_name: "Дноуглубительный бур", seller_nick: "xietoru", price: 8900, amount: 1, expires_in: "через 8 ч", category: "tech" },
        { item_name: "Кейс Бездны x3", seller_nick: "VortexHunter", price: 14000, amount: 3, expires_in: "через 18 ч", category: "cases" },
        { item_name: "Авторыболов MK3", seller_nick: "SeaDragon", price: 6500, amount: 1, expires_in: "через 5 ч", category: "tech" },
        { item_name: "Небесный камень AE2 x128", seller_nick: "Nautilus99", price: 800, amount: 128, expires_in: "через 31 ч", category: "ores" },
      ];
    }

    let activeFilter = "all";
    let query = "";

    function renderFiltered() {
      const filtered = allLots.filter((lot) => {
        const matchesQuery = !query || lot.item_name.toLowerCase().includes(query) || lot.seller_nick.toLowerCase().includes(query);
        const matchesFilter = activeFilter === "all" || (lot.category && lot.category === activeFilter) || (activeFilter === "rods" && lot.item_name.toLowerCase().includes("rod")) || (activeFilter === "ores" && lot.item_name.toLowerCase().includes("слиток"));
        return matchesQuery && matchesFilter;
      });

      if (countEl) countEl.textContent = `${filtered.length} лотов`;

      const coinSvg = '<img src="assets/logo.png" class="aqua-coin-icon coin-ico" alt="">';

      if (!filtered.length) {
        grid.innerHTML = '<p class="muted-line" style="grid-column:1/-1;text-align:center;padding:3rem">По заданным фильтрам лотов не найдено. Выстави свой предмет командой <code>/ah sell</code> в игре!</p>';
        return;
      }

      grid.innerHTML = filtered.map((l) => `
        <div class="lot-v2-card">
          <div class="lot-header">
            <div class="lot-seller">
              <img class="lot-seller-avatar" src="/api/skins/${encodeURIComponent(l.seller_nick)}/avatar?v=look2" alt="${esc(l.seller_nick)}" onerror="this.onerror=null;this.src='/assets/images/avatar_default.png'" />
              <span class="lot-seller-name">${esc(l.seller_nick)}</span>
            </div>
            <span class="lot-timer">${esc(l.expires_in || "активен")}</span>
          </div>
          <div class="lot-item-info">
            <div class="lot-item-ico">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
            </div>
            <div>
              <div class="lot-item-name">${esc(l.item_name)}</div>
              <small style="color:#94a3b8">Количество: ×${l.amount || 1}</small>
            </div>
          </div>
          <div class="lot-footer">
            <span class="lot-price-tag">${coinSvg}<span>${Number(l.price).toLocaleString("ru-RU")}</span></span>
            <span style="font-size:0.8rem;color:#94a3b8">Купить: /ah</span>
          </div>
        </div>
      `).join("");
    }

    if (searchInput) {
      searchInput.addEventListener("input", (e) => {
        query = e.target.value.toLowerCase().trim();
        renderFiltered();
      });
    }

    if (pills) {
      pills.querySelectorAll(".market-pill-btn").forEach((btn) => {
        btn.addEventListener("click", () => {
          pills.querySelectorAll(".market-pill-btn").forEach((b) => b.classList.remove("active"));
          btn.classList.add("active");
          activeFilter = btn.getAttribute("data-filter");
          renderFiltered();
        });
      });
    }

    renderFiltered();
  }

  // 4. PROFILE PAGE WITH 3D SKIN & BENTO STATS
  async function initProfilePage() {
    return initProfile();
  }

  const openStoreBuyModal = typeof openBuyCoinsModal === "function" ? openBuyCoinsModal : () => {};
  window.openTopupModal = typeof openTopupModal === "function" ? openTopupModal : () => {};
  window.openBuyCoinsModal = typeof openBuyCoinsModal === "function" ? openBuyCoinsModal : () => {};
  window.openStoreBuyModal = openStoreBuyModal;
  window.openRouletteModal = typeof openRouletteModal === "function" ? openRouletteModal : () => {};
  window.AquaTechSite = { 
    IP, DOWNLOAD, DOWNLOAD_ZIP, DISCORD, CANONICAL, toast, copyIP, api, 
    openTopupModal: window.openTopupModal, 
    openBuyCoinsModal: window.openBuyCoinsModal, 
    openStoreBuyModal, 
    openRouletteModal: window.openRouletteModal 
  };
})();

