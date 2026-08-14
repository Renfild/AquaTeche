(() => {
  const CLOSE = { action: "CLOSE_GUI" };

  function bridgeSend(payload) {
    const s = typeof payload === "string" ? payload : JSON.stringify(payload);
    if (window.AquaTechBridge && typeof window.AquaTechBridge.send === "function") {
      window.AquaTechBridge.send(s);
      return;
    }
    location.hash = "aqipc=" + encodeURIComponent(s);
  }

  window.AquaTechBridge = window.AquaTechBridge || {
    send(msg) {
      location.hash = "aqipc=" + encodeURIComponent(typeof msg === "string" ? msg : JSON.stringify(msg));
    },
  };

  function esc(s) {
    return String(s ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  async function api(path) {
    const res = await fetch(path, { credentials: "include", headers: { "content-type": "application/json" } });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const err = new Error(data.error || "HTTP " + res.status);
      err.status = res.status;
      throw err;
    }
    return data;
  }

  function page() {
    return document.body.getAttribute("data-embed") || "";
  }

  function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param) || "";
  }

  function bindChrome() {
    document.getElementById("embed-close")?.addEventListener("click", () => bridgeSend(CLOSE));
    window.addEventListener("keydown", (e) => {
      if (e.key === "Escape") bridgeSend(CLOSE);
    });
  }

  const STORE_ITEMS = [
    {
      slug: "vip",
      title: "VIP",
      tier: "vip",
      price_rub: 149,
      badge: "СТАРТОВЫЙ",
      color: "#10B981",
      perks: [
        "Префикс [VIP] в чате и табе",
        "Цветной никнейм и сообщения",
        "Доступ к /kit vip (удочка T2 + ресурсы)",
        "1 дополнительный приват острова",
        "Приоритетный вход на сервер",
        "Сохранение 50% опыта при смерти"
      ]
    },
    {
      slug: "premium",
      title: "Premium",
      tier: "premium",
      price_rub: 299,
      badge: "ПОПУЛЯРНЫЙ",
      color: "#00E5FF",
      perks: [
        "Все возможности ранга VIP",
        "Префикс [PREMIUM] в табе",
        "Доступ к /kit premium (удочка T4 + кейс)",
        "3 точки дома (/sethome 3)",
        "Ускоренная авторыбалка (+15% к скорости)",
        "Бесплатный вход без очереди"
      ]
    },
    {
      slug: "deluxe",
      title: "Deluxe",
      tier: "deluxe",
      price_rub: 599,
      badge: "ПРОДВИНУТЫЙ",
      color: "#A855F7",
      perks: [
        "Все возможности Premium",
        "Префикс [DELUXE] с градиентом",
        "Доступ к /kit deluxe (удочка T6 + 3 кейса)",
        "5 точек дома (/sethome 5)",
        "Команда /feed и /heal раз в час",
        "Увеличенный лимит механизмов (+50%)"
      ]
    },
    {
      slug: "ultimate",
      title: "Ultimate",
      tier: "ultimate",
      price_rub: 1199,
      badge: "МАКСИМАЛЬНЫЙ",
      color: "#F59E0B",
      perks: [
        "Максимум привилегий на сервере",
        "Префикс [ULTIMATE] с неоновой подсветкой",
        "Доступ к /kit ultimate (удочка T8 + 5 кейсов)",
        "Безлимитные точки дома",
        "Полет /fly в границах своего острова",
        "Персональный менеджер и техподдержка"
      ]
    }
  ];

  async function renderDonate() {
    const root = document.getElementById("embed-root");
    if (!root) return;

    let items = STORE_ITEMS;
    try {
      const data = await api("/api/catalog?kind=store");
      if (data.items && data.items.length) {
        items = data.items.map((it, idx) => ({
          ...STORE_ITEMS[idx % STORE_ITEMS.length],
          ...it
        }));
      }
    } catch {}

    root.innerHTML = items
      .map((it) => {
        const perks = (it.perks || []).map((p) => `<li><span class="bullet">></span> ${esc(p)}</li>`).join("");
        return `<article class="embed-card tier-${it.tier || 'vip'}">
          <div class="card-header">
            <span class="card-badge" style="border-color: ${it.color}; color: ${it.color}">${esc(it.badge || "ТАРИФ")}</span>
            <h2 style="color: ${it.color}">${esc(it.title)}</h2>
            <div class="embed-price">${esc(it.price_rub)} <span class="rub">₽ / мес</span></div>
          </div>
          <ul class="card-perks">${perks}</ul>
          <div class="card-footer">
            <button type="button" class="embed-buy btn-tier" data-slug="${esc(it.slug)}" onclick="window.AquaTechBridge.send(JSON.stringify({action:'BUY_DONATE', slug:'${esc(it.slug)}'}))">
              Выбрать тариф
            </button>
          </div>
        </article>`;
      })
      .join("");
  }

  async function renderCabinet() {
    const root = document.getElementById("embed-root");
    if (!root) return;

    let nick = getQueryParam("nick") || "Renfild";
    let balance = 0;
    let rank = "Игрок";

    try {
      const me = await api("/api/me");
      if (me.user?.nick) nick = me.user.nick;
      if (me.user?.balance) balance = me.user.balance;
      if (me.user?.rank_id) rank = me.user.rank_id.toUpperCase();
    } catch {}

    let profile = { coins: balance, fish: 142, likes: 12, bio: "Покоритель океанических глубин AquaTech." };
    try {
      const p = await api("/api/profiles/" + encodeURIComponent(nick));
      if (p.profile) profile = { ...profile, ...p.profile };
    } catch {}

    root.innerHTML = `
      <div class="cabinet-grid">
        <div class="cabinet-sidebar">
          <div class="avatar-card">
            <div class="avatar-frame">
              <img src="https://mc-heads.net/body/${encodeURIComponent(nick)}" alt="${esc(nick)}" />
            </div>
            <h2 class="player-name">${esc(nick)}</h2>
            <div class="rank-pill">${esc(rank)}</div>
            <div class="online-indicator">В игре на сервере</div>
          </div>
        </div>

        <div class="cabinet-main">
          <div class="stats-cards-grid">
            <div class="stat-card">
              <div class="stat-value">${esc(profile.coins ?? balance)} <span class="stat-unit">монет</span></div>
              <div class="stat-label">Баланс монет</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">${esc(profile.fish ?? 0)} <span class="stat-unit">шт</span></div>
              <div class="stat-label">Рыбы выловлено</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">${esc(profile.quests_done ?? 0)} <span class="stat-unit">/ ${esc(profile.quests_total || 25)}</span></div>
              <div class="stat-label">Квестов выполнено</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">#${esc(profile.leaderboard_rank || 1)}</div>
              <div class="stat-label">Рейтинг рыбака</div>
            </div>
          </div>

          <div class="cabinet-actions">
            <button type="button" class="btn-action primary" onclick="window.AquaTechBridge.send(JSON.stringify({action:'NAVIGATE', to:'donate'}))">
              Пополнить баланс / Донат
            </button>
            <button type="button" class="btn-action secondary" onclick="window.AquaTechBridge.send(JSON.stringify({action:'OPEN_CASES'}))">
              Открыть кейсы
            </button>
          </div>
        </div>
      </div>`;
  }

  bindChrome();
  if (page() === "donate") renderDonate();
  if (page() === "cabinet") renderCabinet();
})();
