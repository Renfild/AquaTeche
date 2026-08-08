(() => {
  const IP = "katherine-hydro.tun.ply.gg:31279";
  const DOWNLOAD =
    "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.9/AquaTech.exe";
  const STORAGE_USER = "aquatech_user";
  const STORAGE_PROFILES = "aquatech_profiles";

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
    { nick: "WebTest", privilege: "Deluxe", playtime: "128 ч", coins: 4200, likes: 86, fish: 1840 },
    { nick: "OceanKing", privilege: "Ultimate", playtime: "210 ч", coins: 9800, likes: 214, fish: 4021 },
    { nick: "StarCatcher", privilege: "Premium", playtime: "96 ч", coins: 2100, likes: 61, fish: 990 },
    { nick: "HydroForge", privilege: "VIP", playtime: "74 ч", coins: 900, likes: 33, fish: 640 },
    { nick: "DepthWalker", privilege: "Игрок", playtime: "58 ч", coins: 350, likes: 18, fish: 410 },
    { nick: "Renfild", privilege: "Ultimate", playtime: "301 ч", coins: 15000, likes: 502, fish: 8120 },
    { nick: "AquaNova", privilege: "Premium", playtime: "112 ч", coins: 2600, likes: 77, fish: 1204 },
    { nick: "TideBaron", privilege: "Deluxe", playtime: "143 ч", coins: 5100, likes: 95, fish: 2011 },
    { nick: "KelpCraft", privilege: "VIP", playtime: "41 ч", coins: 420, likes: 12, fish: 280 },
    { nick: "SonarFox", privilege: "Игрок", playtime: "27 ч", coins: 120, likes: 5, fish: 150 },
  ];

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

  function profiles() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_PROFILES) || "{}");
    } catch {
      return {};
    }
  }

  function saveProfiles(map) {
    localStorage.setItem(STORAGE_PROFILES, JSON.stringify(map));
  }

  function ensureProfile(nick) {
    const map = profiles();
    if (!map[nick]) {
      map[nick] = {
        nick,
        bio: "Исследователь глубин AquaTech.",
        theme: "ocean",
        glow: "#2de2e6",
        views: Math.floor(40 + Math.random() * 400),
        likes: Math.floor(5 + Math.random() * 120),
        playtime: `${Math.floor(10 + Math.random() * 200)} ч`,
        fish: Math.floor(50 + Math.random() * 3000),
        coins: Math.floor(100 + Math.random() * 5000),
        privilege: "Игрок",
        badges: ["Новичок глубин"],
      };
      saveProfiles(map);
    }
    return map[nick];
  }

  function skinUrl(nick) {
    return `https://mc-heads.net/avatar/${encodeURIComponent(nick)}/64`;
  }

  function toast(text) {
    let el = $(".toast");
    if (!el) {
      el = document.createElement("div");
      el.className = "toast";
      document.body.appendChild(el);
    }
    el.textContent = text;
    el.classList.add("show");
    clearTimeout(toast._t);
    toast._t = setTimeout(() => el.classList.remove("show"), 2200);
  }

  function copyIP() {
    navigator.clipboard?.writeText(IP).then(
      () => toast("IP скопирован"),
      () => toast(IP)
    );
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
    $("[data-logout]", mount)?.addEventListener("click", () => {
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
            <a href="https://renfild.github.io/AquaTeche/">Зеркало сайта (GH Pages)</a>
            <a href="login.html">Войти</a>
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

  function initTop() {
    const root = $("#top-root");
    if (!root) return;
    let mode = "playtime";
    const render = () => {
      const sorted = [...DEMO_PLAYERS].sort((a, b) => {
        if (mode === "coins") return b.coins - a.coins;
        if (mode === "likes") return b.likes - a.likes;
        return parseInt(b.playtime) - parseInt(a.playtime);
      });
      root.innerHTML = sorted
        .map((p, i) => {
          const stat =
            mode === "coins"
              ? `${p.coins.toLocaleString("ru-RU")} ¤`
              : mode === "likes"
                ? `${p.likes} ❤`
                : p.playtime;
          return `<a class="top-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
            <div class="rank">${i + 1}</div>
            <img src="${skinUrl(p.nick)}" alt="">
            <div class="meta"><strong>${p.nick}</strong><span>${p.privilege}</span></div>
            <div class="stat">${stat}</div>
          </a>`;
        })
        .join("");
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
    const draw = () => {
      const q = input.value.trim().toLowerCase();
      const items = DEMO_PLAYERS.filter((p) => !q || p.nick.toLowerCase().includes(q));
      list.innerHTML = items
        .map(
          (p) => `<a class="top-row" href="profile.html?u=${encodeURIComponent(p.nick)}">
            <div class="rank">·</div>
            <img src="${skinUrl(p.nick)}" alt="">
            <div class="meta"><strong>${p.nick}</strong><span>${p.privilege} · ${p.playtime}</span></div>
            <div class="stat">${p.likes} ❤</div>
          </a>`
        )
        .join("");
    };
    input.addEventListener("input", draw);
    draw();
  }

  function initProfile() {
    const root = $("#profile-root");
    if (!root) return;
    const params = new URLSearchParams(location.search);
    const user = getUser();
    const nick = params.get("u") || user?.nick || "WebTest";
    const demo = DEMO_PLAYERS.find((p) => p.nick.toLowerCase() === nick.toLowerCase());
    const profile = Object.assign(
      ensureProfile(nick),
      demo
        ? {
            privilege: demo.privilege,
            playtime: demo.playtime,
            coins: demo.coins,
            likes: demo.likes,
            fish: demo.fish,
          }
        : {}
    );
    profile.views = (profile.views || 0) + 1;
    const map = profiles();
    map[nick] = profile;
    saveProfiles(map);

    const isOwner = user && user.nick.toLowerCase() === nick.toLowerCase();
    root.innerHTML = `
      <div class="profile-cover ${profile.theme || "ocean"}" style="box-shadow:0 0 0 1px ${profile.glow || "var(--aqua)"}, 0 20px 60px rgba(0,0,0,.45)">
        <div class="profile-identity">
          <img class="profile-avatar" src="${skinUrl(nick)}" alt="">
          <div>
            <h1>${nick}</h1>
            <p>${profile.privilege || "Игрок"} · ${profile.views} просмотров · ${profile.likes} лайков</p>
          </div>
        </div>
      </div>
      <div class="stats-row" style="margin-top:1rem">
        <div class="stat-card"><strong>${profile.playtime}</strong><span>Онлайн</span></div>
        <div class="stat-card"><strong>${profile.fish}</strong><span>Улов</span></div>
        <div class="stat-card"><strong>${Number(profile.coins).toLocaleString("ru-RU")}</strong><span>Монеты</span></div>
        <div class="stat-card"><strong>${profile.likes}</strong><span>Лайки</span></div>
      </div>
      <div class="card" style="margin-top:1rem">
        <h3>О игроке</h3>
        <p style="margin:.5rem 0 0;color:var(--muted)">${profile.bio || ""}</p>
        <div class="badge-grid" style="margin-top:1rem">
          ${(profile.badges || []).map((b) => `<span class="badge">${b}</span>`).join("")}
          ${profile.privilege && profile.privilege !== "Игрок" ? `<span class="badge">${profile.privilege}</span>` : ""}
        </div>
        <div style="margin-top:1rem;display:flex;gap:.5rem;flex-wrap:wrap">
          <button class="btn btn-aqua" type="button" data-like>Лайк профилю</button>
          ${isOwner ? "" : `<a class="btn btn-secondary" href="login.html">Это не вы? Войти</a>`}
        </div>
      </div>
      ${
        isOwner
          ? `<div class="card" style="margin-top:1rem">
              <h3>Кастомизация профиля</h3>
              <form class="form" id="profile-edit">
                <div class="field"><label>Био</label><textarea name="bio" rows="3">${profile.bio || ""}</textarea></div>
                <div class="field"><label>Обложка</label>
                  <select name="theme">
                    <option value="ocean">Океан</option>
                    <option value="deep">Глубина</option>
                    <option value="storm">Шторм</option>
                    <option value="abyss">Бездна</option>
                  </select>
                </div>
                <div class="field"><label>Свечение</label><input name="glow" type="color" value="${profile.glow || "#2de2e6"}"></div>
                <button class="btn btn-primary" type="submit">Сохранить</button>
              </form>
            </div>`
          : ""
      }`;

    const themeSelect = root.querySelector('select[name="theme"]');
    if (themeSelect) themeSelect.value = profile.theme || "ocean";

    root.querySelector("[data-like]")?.addEventListener("click", () => {
      profile.likes += 1;
      map[nick] = profile;
      saveProfiles(map);
      toast("Лайк отправлен");
      initProfile();
    });

    $("#profile-edit")?.addEventListener("submit", (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      profile.bio = String(fd.get("bio") || "");
      profile.theme = String(fd.get("theme") || "ocean");
      profile.glow = String(fd.get("glow") || "#2de2e6");
      if (!profile.badges.includes("Кастомизатор")) profile.badges.push("Кастомизатор");
      map[nick] = profile;
      saveProfiles(map);
      toast("Профиль сохранён");
      initProfile();
    });
  }

  function initAuth() {
    const login = $("#login-form");
    const reg = $("#register-form");
    login?.addEventListener("submit", (e) => {
      e.preventDefault();
      const fd = new FormData(login);
      const nick = String(fd.get("nick") || "").trim();
      if (!nick || nick.length < 3) return toast("Введите ник (от 3 символов)");
      ensureProfile(nick);
      setUser({ nick });
      toast(`Добро пожаловать, ${nick}`);
      location.href = `profile.html?u=${encodeURIComponent(nick)}`;
    });
    reg?.addEventListener("submit", (e) => {
      e.preventDefault();
      const fd = new FormData(reg);
      const nick = String(fd.get("nick") || "").trim();
      const pass = String(fd.get("password") || "");
      if (!nick || nick.length < 3) return toast("Ник от 3 символов");
      if (pass.length < 4) return toast("Пароль от 4 символов");
      const p = ensureProfile(nick);
      p.badges = ["Новичок глубин", "Зарегистрирован"];
      const map = profiles();
      map[nick] = p;
      saveProfiles(map);
      setUser({ nick });
      toast("Аккаунт создан");
      location.href = `profile.html?u=${encodeURIComponent(nick)}`;
    });
  }

  function initCases() {
    const modal = $("#case-modal");
    const result = $("#case-result");
    if (!modal) return;
    const loot = [
      "VIP на 7 дней",
      "500 AquaCoins",
      "Ключ от кейса ×2",
      "Premium на 3 дня",
      "Скин-рамка Ocean",
      "1000 AquaCoins",
      "Deluxe пробный день",
    ];
    document.querySelectorAll("[data-open-case]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const name = btn.dataset.openCase || "Кейс";
        modal.classList.add("open");
        result.textContent = "Крутим…";
        setTimeout(() => {
          const item = loot[Math.floor(Math.random() * loot.length)];
          result.textContent = `${name}: ${item}`;
        }, 700);
      });
    });
    modal.addEventListener("click", (e) => {
      if (e.target === modal || e.target.closest("[data-close-modal]")) modal.classList.remove("open");
    });
  }

  function initStore() {
    document.querySelectorAll("[data-buy]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const user = getUser();
        if (!user) {
          toast("Сначала войдите в аккаунт");
          location.href = "login.html";
          return;
        }
        toast(`Заявка на ${btn.dataset.buy} для ${user.nick} принята (демо)`);
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
    // hero is above fold — show immediately
    document.querySelectorAll(".hero .reveal").forEach((n) => n.classList.add("in"));
  }

  document.addEventListener("DOMContentLoaded", () => {
    renderHeader();
    renderFooter();
    wireCommon();
    initTop();
    initPlayers();
    initProfile();
    initAuth();
    initCases();
    initStore();
    initReveal();
  });

  window.AquaTechSite = { IP, DOWNLOAD, toast, copyIP };
})();
