(() => {
  const IP = "g-pl-3.apexnodes.xyz:21561";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.69/AquaTech.exe";
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

  const FALLBACK_PLAYERS = [
    { nick: "Renfild", privilege: "Создатель", playtime_hours: 340, playtime: "340 ч", coins: 854000, likes: 256, fish: 4890, badges: ["Создатель", "StarCatcher Master", "Deep Ocean", "VIP"], bio: "Основатель проекта AquaTech. Покоритель Бездны.", theme: "ocean" },
    { nick: "AquaSmoke1", privilege: "Ultimate", playtime_hours: 215, playtime: "215 ч", coins: 490000, likes: 142, fish: 3120, badges: ["Top Fisher", "Ultimate"], bio: "Ловлю рыбу в лаве на Magma Rod.", theme: "deep" },
    { nick: "xietoru", privilege: "Deluxe", playtime_hours: 180, playtime: "180 ч", coins: 345000, likes: 98, fish: 2450, badges: ["Beta Tester", "Deluxe"], bio: "Исследователь биомов и кастомного лута.", theme: "storm" },
    { nick: "VortexHunter", privilege: "VIP", playtime_hours: 120, playtime: "120 ч", coins: 180000, likes: 64, fish: 1780, badges: ["VIP"], bio: "AquaTech Fishing Legend", theme: "abyss" },
    { nick: "Nautilus99", privilege: "Игрок", playtime_hours: 95, playtime: "95 ч", coins: 120000, likes: 45, fish: 1340, badges: ["Рыбак"], bio: "Изучаю таблицы T1-T13.", theme: "ocean" },
    { nick: "SeaDragon", privilege: "VIP", playtime_hours: 80, playtime: "80 ч", coins: 95000, likes: 38, fish: 980, badges: ["VIP"], bio: "Поймал Титановую руду на T2!", theme: "deep" }
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

    const menuBtn = $("[data-menu]", mount);
    const mobileNav = $("#mobile-nav", mount);
    menuBtn?.addEventListener("click", (e) => {
      e.stopPropagation();
      menuBtn.classList.toggle("active");
      mobileNav?.classList.toggle("open");
    });
    mobileNav?.querySelectorAll("a").forEach((a) => {
      a.addEventListener("click", () => {
        menuBtn?.classList.remove("active");
        mobileNav?.classList.remove("open");
      });
    });
    document.addEventListener("click", (e) => {
      if (!mount.contains(e.target)) {
        menuBtn?.classList.remove("active");
        mobileNav?.classList.remove("open");
      }
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

    // Global escape key to close modals & menu
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") {
        document.querySelectorAll(".loot-modal-overlay.open").forEach((m) => m.classList.remove("open"));
        document.querySelector("[data-menu]")?.classList.remove("active");
        document.getElementById("mobile-nav")?.classList.remove("open");
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

    root.innerHTML = `
      <div class="profile-cover ${theme}">
        <div class="profile-identity">
          <img class="profile-avatar" src="${skinUrl(profile.nick)}" alt="${profile.nick}">
          <div class="profile-meta">
            <h1>${profile.nick}</h1>
            <div class="profile-status-line">
              <span class="tag ${profile.slug === "deluxe" || profile.slug === "ultimate" || profile.privilege === "Создатель" ? "gold" : ""}">${profile.privilege || "Игрок"}</span>
              ${profile.status_message ? `<span class="profile-status-badge">${profile.status_message}</span>` : ""}
            </div>
            ${profile.fav_rod ? `<div class="fav-rod-badge">Любимая удочка: <strong>${profile.fav_rod}</strong></div>` : ""}
            ${socials.length ? `<div class="profile-socials">${socials.join("")}</div>` : ""}
            <p style="margin-top:0.75rem;color:rgba(255,255,255,0.85);max-width:38rem">${profile.bio || ""}</p>
          </div>
        </div>
        <div class="profile-actions">
          <button class="btn-like ${profile.has_liked ? "liked" : ""}" type="button" id="btn-like-profile" ${mine ? 'disabled title="Нельзя ставить лайк своему профилю"' : ""}>
            <span class="heart">${heartSvg(profile.has_liked)}</span>
            <span id="like-count">${profile.likes || 0}</span>
            <span style="font-size:0.82rem;font-weight:600;opacity:0.85">${profile.has_liked ? "Вам нравится" : "Похвалить"}</span>
          </button>
        </div>
      </div>

      <div class="stats-row" style="margin-top:1.25rem">
        <div class="stat-card"><strong>${Number(profile.coins || 0).toLocaleString("ru-RU")} ¤</strong><span>AquaCoins</span></div>
        <div class="stat-card"><strong>${Number(profile.fish || 0).toLocaleString("ru-RU")}</strong><span>рыбы поймано</span></div>
        <div class="stat-card"><strong>${profile.playtime || (profile.playtime_hours || 0) + " ч"}</strong><span>в игре</span></div>
        <div class="stat-card"><strong>${profile.views || 0}</strong><span>просмотры</span></div>
      </div>

      <div class="panel" style="margin-top:1.25rem">
        <h3>Бейджи и титулы</h3>
        <div class="badge-grid" style="margin-top:.6rem">
          ${(profile.badges || []).map((b) => `<span class="badge">${b}</span>`).join("") || '<span class="muted-line">Пока пусто</span>'}
        </div>
      </div>

      ${
        mine
          ? `<form class="panel form" id="profile-edit" style="margin-top:1.25rem">
              <h3>Кастомизация профиля</h3>
              <p style="color:var(--muted);font-size:0.88rem;margin-top:0.25rem">Настройте внешний вид своей карточки игрока на сайте.</p>
              
              <div class="field" style="margin-top:1rem">
                <label>Тема оформления профиля</label>
                <div class="theme-selector-grid">
                  ${THEMES.map(
                    (t) => `
                    <label class="theme-pill">
                      <input type="radio" name="theme" value="${t.id}" ${t.id === theme ? "checked" : ""}>
                      <div class="theme-pill-content">${t.label}</div>
                    </label>`
                  ).join("")}
                </div>
              </div>

              <div class="form-grid-2" style="margin-top:0.5rem">
                <div class="field">
                  <label>Статус (до 80 символов)</label>
                  <input type="text" name="status_message" maxlength="80" placeholder="Например: Ловлю на T10 Магматическую" value="${profile.status_message || ""}">
                </div>
                <div class="field">
                  <label>Любимая удочка</label>
                  <input type="text" name="fav_rod" maxlength="50" placeholder="Например: Алмазная удочка [T7]" value="${profile.fav_rod || ""}">
                </div>
              </div>

              <div class="form-grid-2" style="margin-top:0.5rem">
                <div class="field">
                  <label>Telegram</label>
                  <input type="text" name="social_tg" placeholder="@username" value="${profile.social_tg || ""}">
                </div>
                <div class="field">
                  <label>VK</label>
                  <input type="text" name="social_vk" placeholder="id или ник" value="${profile.social_vk || ""}">
                </div>
              </div>

              <div class="field" style="margin-top:0.5rem">
                <label>Discord</label>
                <input type="text" name="social_discord" placeholder="username" value="${profile.social_discord || ""}">
              </div>

              <div class="field" style="margin-top:0.5rem">
                <label>О себе (био)</label>
                <textarea name="bio" rows="3" maxlength="300" placeholder="Расскажите о себе, своих рекордах в рыбалке или острове...">${profile.bio || ""}</textarea>
              </div>

              <button class="btn btn-primary" type="submit" style="margin-top:0.5rem">Сохранить профиль</button>
            </form>`
          : ""
      }`;

    // Wire Like Button
    const likeBtn = $("#btn-like-profile");
    likeBtn?.addEventListener("click", async () => {
      if (!user) {
        toast("Войдите в аккаунт, чтобы похвалить игрока");
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
          $("#like-count").textContent = res.likes;
          toast(res.liked ? "Вы похвалили игрока!" : "Лайк убран");
          if (soundOn) playTone("ok");
        }
      } catch (err) {
        toast(err.message || "Не удалось поставить лайк");
      }
    });

    // Wire Edit Form
    const form = $("#profile-edit");
    form?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(form);
      try {
        await api(`/api/profiles/${encodeURIComponent(profile.nick)}`, {
          method: "PATCH",
          body: JSON.stringify({
            bio: fd.get("bio"),
            theme: fd.get("theme"),
            status_message: fd.get("status_message"),
            fav_rod: fd.get("fav_rod"),
            social_tg: fd.get("social_tg"),
            social_vk: fd.get("social_vk"),
            social_discord: fd.get("social_discord"),
          }),
        });
        toast("Профиль успешно обновлён!");
        if (soundOn) playTone("ok");
        setTimeout(() => location.reload(), 600);
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

  const CASE_LOOT_TABLES = {
    ocean: [
      { name: "AquaCoins (10 000 — 50 000)", rarity: "common", rarityLabel: "Обычный", chance: "45%" },
      { name: "Наживка глубоководная ×16", rarity: "common", rarityLabel: "Обычный", chance: "30%" },
      { name: "Магнитный поплавок ×1", rarity: "rare", rarityLabel: "Редкий", chance: "15%" },
      { name: "Бустер опыта рыбалки (2ч) ×2", rarity: "epic", rarityLabel: "Эпический", chance: "10%" },
    ],
    fisher: [
      { name: "Коралловая удочка [T6]", rarity: "rare", rarityLabel: "Редкий", chance: "35%" },
      { name: "Алмазная удочка [T7]", rarity: "rare", rarityLabel: "Редкий", chance: "25%" },
      { name: "Аметистовая удочка [T8]", rarity: "epic", rarityLabel: "Эпический", chance: "20%" },
      { name: "Золотая удочка [T9]", rarity: "epic", rarityLabel: "Эпический", chance: "12%" },
      { name: "Магматическая удочка [T10]", rarity: "legendary", rarityLabel: "Легендарный", chance: "8%" },
    ],
    depth: [
      { name: "Рамка профиля «Глубинная Бездна»", rarity: "rare", rarityLabel: "Редкий", chance: "40%" },
      { name: "250 000 AquaCoins", rarity: "epic", rarityLabel: "Эпический", chance: "30%" },
      { name: "Привилегия Deluxe на 14 дней", rarity: "epic", rarityLabel: "Эпический", chance: "20%" },
      { name: "Привилегия Ultimate на 30 дней", rarity: "legendary", rarityLabel: "Легендарный", chance: "10%" },
    ],
  };

  function openLootModal(slug) {
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
        ? `<button class="btn btn-aqua" style="margin-top:0.85rem;width:100%" type="button" data-view-loot="${item.slug}">🔍 Содержимое кейса</button>`
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
