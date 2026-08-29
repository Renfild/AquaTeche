(() => {
  const IP = "g-pl-3.apexnodes.xyz:21561";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.83/AquaTech.exe";
  /* portal ui build: compact header + market lots */
  const CANONICAL = "https://aquateche.store";
  const DISCORD = "https://discord.gg/3Khzr5z4fQ";
  const STORAGE_USER = "aquatech_user";
  const STORAGE_SOUND = "aquatech_sound";
  const API_BASE = "";

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
  let soundOn = localStorage.getItem(STORAGE_SOUND) !== "0";
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

  function renderHeader() {
    const mount = $("#site-header");
    if (!mount) return;
    const user = getUser();
    const active = pageId();
    const primary = NAV_PRIMARY.map((n) => navLink(n, active)).join("");
    const moreOpen = NAV_MORE.some((n) => n.id === active);
    const moreLinks = NAV_MORE.map((n) => navLink(n, active)).join("");
    const coins =
      user && user.coins != null
        ? `<span class="header-coins" title="Монеты">${Number(user.coins).toLocaleString("ru-RU")} ¤</span>`
        : "";
    const allMobile = [...NAV_PRIMARY, ...NAV_MORE]
      .map((n) => navLink(n, active))
      .join("");

    mount.innerHTML = `
      <header class="site-header">
        <div class="container header-inner">
          <a class="brand" href="index.html"><img src="assets/logo.png" alt="" width="28" height="28" /><span>AquaTech</span></a>
          <nav class="nav-desktop" aria-label="Основное">
            ${primary}
            <details class="nav-more"${moreOpen ? " open" : ""}>
              <summary>Ещё</summary>
              <div class="nav-more-panel">${moreLinks}</div>
            </details>
          </nav>
          <div class="header-spacer"></div>
          <div class="online-pill" title="Онлайн на сервере"><span class="dot"></span><span data-online aria-live="polite">…</span></div>
          ${coins}
          <button class="sound-toggle" type="button" data-sound-toggle aria-pressed="${soundOn ? "true" : "false"}" aria-label="${soundOn ? "Выключить звуки" : "Включить звуки"}" title="Звуки интерфейса">${soundGlyph(soundOn)}</button>
          <div class="header-actions">
            <a class="btn btn-ghost" href="${DISCORD}" target="_blank" rel="noopener noreferrer">Discord</a>
            ${
              user
                ? `${user.is_admin ? '<a class="btn btn-ghost" href="admin.html">Админка</a>' : ""}
                   <a class="btn btn-secondary" href="profile.html">${esc(user.nick)}</a>
                   <button class="btn btn-ghost" type="button" data-logout>Выйти</button>`
                : `<a class="btn btn-secondary" href="login.html">Войти</a>`
            }
            <a class="btn btn-primary header-download" data-download href="start.html">Скачать</a>
            <button class="menu-btn" type="button" aria-label="Меню" aria-expanded="false" aria-controls="mobile-nav" data-menu>
              <span></span><span></span><span></span>
            </button>
          </div>
        </div>
        <div class="mobile-nav" id="mobile-nav" role="dialog" aria-modal="true" aria-label="Меню" aria-hidden="true">
          <div class="container">
            <a href="index.html" class="${active === "home" ? "active" : ""}">Главная</a>
            ${allMobile}
            ${user?.is_admin ? '<a href="admin.html">Админка</a>' : ""}
            ${
              user
                ? `<a href="profile.html">Кабинет</a>
                   <button type="button" data-logout>Выйти</button>`
                : '<a href="register.html">Регистрация</a>'
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
    document.addEventListener("click", (e) => {
      if (!mount.contains(e.target)) {
        setMobileMenu(false);
        mount.querySelectorAll("details.nav-more").forEach((d) => {
          d.removeAttribute("open");
        });
      }
    });
    $("[data-sound-toggle]", mount)?.addEventListener("click", () => {
      soundOn = !soundOn;
      localStorage.setItem(STORAGE_SOUND, soundOn ? "1" : "0");
      const btn = $("[data-sound-toggle]", mount);
      if (btn) {
        btn.setAttribute("aria-pressed", soundOn ? "true" : "false");
        btn.setAttribute("aria-label", soundOn ? "Выключить звуки" : "Включить звуки");
        btn.innerHTML = soundGlyph(soundOn);
      }
      if (soundOn) playTone("ok");
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
    refreshOnlinePill();
    setInterval(refreshOnlinePill, 60000);

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
      }
    });
  }

  async function refreshOnlinePill() {
    const pills = document.querySelectorAll("[data-online]");
    if (!pills.length) return;
    try {
      const data = await api("/api/server-status");
      const online = !!data.online;
      const n = Number(data.players_online || 0) || 0;
      pills.forEach((el) => {
        el.textContent = online ? `${n} онлайн` : "оффлайн";
      });
      document.querySelectorAll(".online-pill, .hero-status-pill").forEach((el) => {
        el.classList.toggle("is-offline", !online);
        el.title = online
          ? `Онлайн на сервере: ${n}${data.players_max ? " / " + data.players_max : ""}`
          : "Сервер сейчас недоступен";
      });
    } catch {
      pills.forEach((el) => {
        el.textContent = "нет данных";
      });
      document.querySelectorAll(".online-pill, .hero-status-pill").forEach((el) => {
        el.classList.add("is-offline");
      });
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
            ? `${Number(p.coins || 0).toLocaleString("ru-RU")} ¤`
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
    const root = $("#top-root");
    if (!root) return;
    let mode = "fish";
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

  function avatarSrc(nick) {
    return `/api/skins/${encodeURIComponent(nick)}/avatar`;
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
        <button type="button" class="btn btn-primary" data-look-upload="skin">Загрузить</button>
        <button type="button" class="btn btn-ghost" data-look-delete="skin">Удалить</button>
        <input type="file" accept="image/png" hidden data-look-file="skin">
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

  async function renderOwnCabinet(root, profile, user, theme, socials, mine) {
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
            <img src="${avatarSrc(profile.nick)}" alt="" width="64" height="64" onerror="this.onerror=null;this.src='${skinUrl(profile.nick)}'">
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
            <div class="lk-balance-card">
              <div><span>Монеты</span><strong>${Number(profile.coins || 0).toLocaleString("ru-RU")} ¤</strong></div>
              ${mine ? `<a class="btn btn-primary" href="store.html">Магазин</a>` : ""}
            </div>
            <div class="lk-balance-card">
              <div><span>Рыба</span><strong>${Number(profile.fish || 0).toLocaleString("ru-RU")}</strong></div>
              <a class="btn btn-secondary" href="top.html">Топы</a>
            </div>
          </div>
        </div>
        <div class="lk-shell">
          <nav class="lk-nav" aria-label="Кабинет">${nav}</nav>
          <div class="lk-main">
            <section class="lk-pane" data-lk-pane="overview" ${tab === "overview" ? "" : "hidden"}>
              <h2>Обзор</h2>
              <div class="stats-row">
                <div class="stat-card"><strong>${Number(profile.coins || 0).toLocaleString("ru-RU")} ¤</strong><span>AquaCoins</span></div>
                <div class="stat-card"><strong>${Number(profile.fish || 0).toLocaleString("ru-RU")}</strong><span>рыбы поймано</span></div>
                <div class="stat-card"><strong>${esc(profile.playtime || (profile.playtime_hours || 0) + " ч")}</strong><span>в игре</span></div>
                <div class="stat-card"><strong>${profile.views || 0}</strong><span>просмотры</span></div>
              </div>
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

    root.querySelector(".lk-nav")?.addEventListener("click", (e) => {
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
        avImg.src = skinUrl(profile.nick);
      };
    }

    const fallbackSkinUrl = `https://mc-heads.net/skin/${encodeURIComponent(profile.nick)}`;
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
          setLookStatus(root, kind, "Готово! Если ты на сервере — скин обновится через несколько секунд, иначе при следующем входе.", "ok");
        } else {
          setLookStatus(root, kind, "Сохранено", "ok");
        }
        if (kind === "cape") {
          if (capeImg) capeImg.src = res.url;
          if (skinState) skinState.viewer.loadCape(res.url).catch(() => {});
        }
        if (kind === "avatar") {
          const hero = root.querySelector(".lk-hero-id img");
          if (hero) hero.src = res.url;
          if (avImg) avImg.src = res.url;
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
    if (mine) ["avatar", "skin", "cape"].forEach(bindKind);

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
      const found = FALLBACK_PLAYERS.find((p) => p.nick.toLowerCase() === nick.toLowerCase());
      if (found) {
        profile = { ...found, views: 240, has_liked: false };
      } else {
        profile = {
          nick: nick,
          privilege: "Игрок AquaTech",
          bio: "Исследователь океанских глубин и кастомной рыбалки AquaTech.",
          theme: "ocean",
          likes: 16,
          fish: 120,
          coins: 24500,
          views: 85,
          has_liked: false,
          badges: ["Рыбак", "AquaTech 2026"],
        };
      }
    }

    const mine = user && user.nick.toLowerCase() === profile.nick.toLowerCase();
    const theme = profile.theme || "ocean";

    // Social chips HTML
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

    await renderOwnCabinet(root, profile, user, theme, socials, mine);
  }

  function initReset() {
    const form = $("#reset-form");
    if (!form) return;
    const claimForm = $("#reset-claim-form");
    const supportBox = $("#reset-step-support");
    let nick = "";

    function resetError(text) {
      document.querySelectorAll("[data-reset-error]").forEach((el) => {
        el.textContent = text || "";
      });
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      nick = ($("#reset-nick")?.value || "").trim();
      resetError("");
      try {
        const data = await api(`/api/auth/nick?nick=${encodeURIComponent(nick)}`);
        if (data.unclaimed || data.exists === false) {
          form.hidden = true;
          claimForm.hidden = false;
          $("#reset-claim-title").textContent = `Задай пароль для ${nick}`;
          $("#reset-password")?.focus();
        } else {
          form.hidden = true;
          supportBox.hidden = false;
        }
      } catch (err) {
        resetError(err.message || "Не удалось проверить ник");
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
    const rows = [...c.loot].sort((a, b) => b.weight - a.weight);
    modal.innerHTML = `
      <div class="loot-modal-card">
        <div class="loot-modal-header">
          <div>
            <span class="rarity-badge rarity-${esc(c.rarity)}">${RARITY_LABEL[c.rarity] || esc(c.rarity)}</span>
            <h3>${esc(c.title)}</h3>
          </div>
          <button class="loot-modal-close" type="button" aria-label="Закрыть">✕</button>
        </div>
        <p style="color:var(--muted);margin-bottom:1.25rem">Стоимость открытия: <b style="color:var(--gold)">${Number(c.cost).toLocaleString("ru-RU")} ¤</b>. Шансы считаются по весам из конфига сервера.</p>
        <div class="loot-items-list">
          ${rows
            .map(
              (l) => `
            <div class="loot-item-row">
              <div class="loot-item-info">
                <span class="loot-item-name">${esc(l.name)}</span>
                <span class="loot-item-count">${l.min < l.max ? `${l.min}–${l.max}` : l.min} шт</span>
              </div>
              <div class="loot-item-chance">${l.chance}%</div>
            </div>`
            )
            .join("")}
        </div>
        <div style="margin-top:1.5rem;text-align:center">
          <small style="color:var(--muted)">Открывается в игре: меню F4</small>
        </div>
      </div>
    `;
    modal.classList.add("open");
    modal.querySelector(".loot-modal-close")?.addEventListener("click", () => modal.classList.remove("open"));
    modal.addEventListener("click", (e) => {
      if (e.target === modal) modal.classList.remove("open");
    });
  }

  function caseCard(c) {
    const top = [...c.loot].sort((a, b) => b.weight - a.weight).slice(0, 3);
    return `<article class="case-card reveal">
      <div class="case-card-head">
        <span class="rarity-badge rarity-${esc(c.rarity)}">${RARITY_LABEL[c.rarity] || esc(c.rarity)}</span>
        <span class="case-cost">${Number(c.cost).toLocaleString("ru-RU")} ¤</span>
      </div>
      <h3>${esc(c.title)}</h3>
      <ul class="case-top">
        ${top
          .map(
            (l) => `<li><span>${esc(l.name)}</span><b>${l.chance}%</b></li>`
          )
          .join("")}
      </ul>
      <button class="btn btn-secondary" style="margin-top:auto;width:100%" type="button" data-view-loot="${esc(c.slug)}">Состав и шансы</button>
    </article>`;
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


  async function initLive() {
    try {
      const t = await api("/api/trends");
      const el = $("#live-trends");
      if (el && t.trends && t.trends.length) {
        el.innerHTML = t.trends.map((f) =>
          `<div style="display:flex;justify-content:space-between;align-items:center;padding:0.45rem 0;border-bottom:1px solid rgba(255,255,255,0.07)">
            <span style="font-size:0.92rem">${f.name}</span>
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
            <span style="font-size:0.92rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${l.label}${l.count > 1 ? ` ×${l.count}` : ""}</span>
            <span style="display:flex;align-items:center;gap:0.6rem;flex-shrink:0">
              <b style="color:var(--gold);font-size:0.92rem">${Number(l.price).toLocaleString("ru-RU")} ¤</b>
              <small style="color:var(--muted)">${l.seller}</small>
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
              <td class="price">${Number(lot.price || 0).toLocaleString("ru-RU")} ¤</td>
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
        const coins = data.profile && data.profile.coins != null ? data.profile.coins : data.user.coins;
        setUser({ ...data.user, coins });
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

  document.addEventListener("DOMContentLoaded", async () => {
    reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    showApiBanner();
    lockAuthForms();
    if (isCanonicalHost()) await refreshSession();
    renderHeader();
    renderFooter();
    wireCommon();
    wireSounds();
    initTop();
    initPlayers();
    initProfile();
    initAuth();
    initReset();
    initLive();
    initCatalog("store");
    initCasesLive();
    await initTrends();
    await initHomeFishTop();
    await initMarket();
    await loadSiteContent();
    await initAdmin();
    initReveal();
  });

  window.AquaTechSite = { IP, DOWNLOAD, DISCORD, CANONICAL, toast, copyIP, api };
})();
