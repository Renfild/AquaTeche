(() => {
  const IP = "g-pl-3.apexnodes.xyz:21561";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.35/AquaTech.exe";
  /* portal ui build: nav-cta + online nowrap */
  const CANONICAL = "https://aquateche.store";
  const STORAGE_USER = "aquatech_user";
  const STORAGE_SOUND = "aquatech_sound";
  const API_BASE = "";

  const NAV = [
    { href: "index.html", label: "Главная", id: "home" },
    { href: "start.html", label: "Начать игру", id: "start", cta: true },
    { href: "store.html", label: "Магазин", id: "store" },
    { href: "cases.html", label: "Кейсы", id: "cases" },
    { href: "rods.html", label: "Удочки", id: "rods" },
    { href: "top.html", label: "Топы", id: "top" },
    { href: "news.html", label: "Новости", id: "news" },
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
      title: "Лаунчер 2.9.20",
      body: "Полноэкранный вход, палитра v2, анимации кнопок и мягкие звуки клика.",
      published_at: "2026-08-08",
    },
    {
      title: "Подключение к серверу",
      body: "Заходи по IP с сайта. Отдельный туннель для модов больше не нужен.",
      published_at: "2026-08-01",
    },
    {
      title: "Авторыбалка + StarCatcher",
      body: "Удочки с кастомным лутом и авторыбалкой на сервере.",
      published_at: "2026-07-15",
    },
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
    const nav = NAV.map((n) => {
      const classes = [
        n.id === active ? "active" : "",
        n.cta ? "nav-cta" : "",
      ]
        .filter(Boolean)
        .join(" ");
      return `<a href="${n.href}" class="${classes}">${n.label}</a>`;
    }).join("");

    mount.innerHTML = `
      <div class="site-header">
        <div class="container header-inner">
          <a class="brand" href="index.html"><span class="brand-mark"></span>AquaTech</a>
          <nav class="nav-desktop">${nav}</nav>
          <div class="header-spacer"></div>
          <div class="online-pill" title="Онлайн на сервере"><span class="dot"></span><span data-online aria-live="polite">…</span></div>
          <button class="sound-toggle" type="button" data-sound-toggle aria-pressed="${soundOn ? "true" : "false"}" title="Звуки интерфейса">${soundOn ? "Звук" : "Без звука"}</button>
          <div class="header-actions">
            ${
              user
                ? `${user.is_admin ? '<a class="btn btn-ghost" href="admin.html">Админка</a>' : ""}
                   <a class="btn btn-secondary" href="profile.html?u=${encodeURIComponent(user.nick)}">${user.nick}</a>
                   <button class="btn btn-ghost" type="button" data-logout>Выйти</button>`
                : `<a class="btn btn-ghost btn-hide-desktop" href="login.html">Войти</a>
                   <a class="btn btn-secondary" href="login.html">Войти</a>
                   <a class="btn btn-primary btn-show-mobile" href="start.html">Начать игру</a>`
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
    $("[data-sound-toggle]", mount)?.addEventListener("click", () => {
      soundOn = !soundOn;
      localStorage.setItem(STORAGE_SOUND, soundOn ? "1" : "0");
      const btn = $("[data-sound-toggle]", mount);
      if (btn) {
        btn.setAttribute("aria-pressed", soundOn ? "true" : "false");
        btn.textContent = soundOn ? "Звук" : "Без звука";
      }
      if (soundOn) playTone("ok");
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
            <p style="color:var(--muted);margin:0;max-width:28rem" data-site="footer_blurb">Океанский сервер. Скачай лаунчер и заходи.</p>
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
        el.textContent = online ? `${n} онлайн` : "оффлайн";
      });
      document.querySelectorAll(".online-pill").forEach((el) => {
        el.classList.toggle("is-offline", !online);
        el.title = online
          ? `Онлайн на сервере: ${n}${data.players_max ? " / " + data.players_max : ""}`
          : "Сервер сейчас недоступен";
      });
    } catch {
      pills.forEach((el) => {
        el.textContent = "нет данных";
      });
      document.querySelectorAll(".online-pill").forEach((el) => {
        el.classList.add("is-offline");
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
    initCatalog("store");
    initCatalog("case");
    await loadSiteContent();
    await initAdmin();
    initReveal();
  });

  window.AquaTechSite = { IP, DOWNLOAD, CANONICAL, toast, copyIP, api };
})();
