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

  function bindChrome() {
    document.getElementById("embed-close")?.addEventListener("click", () => bridgeSend(CLOSE));
  }

  async function renderDonate() {
    const root = document.getElementById("embed-root");
    if (!root) return;
    root.innerHTML = `<p class="embed-muted">Каталог…</p>`;
    try {
      const data = await api("/api/catalog?kind=store");
      const items = data.items || [];
      if (!items.length) {
        root.innerHTML = `<p class="embed-muted">Каталог пуст.</p>`;
        return;
      }
      root.innerHTML = items
        .map((it) => {
          const perks = (it.perks || []).map((p) => `<li>${esc(p)}</li>`).join("");
          return `<article class="embed-card">
            <h2>${esc(it.title)}</h2>
            <p>${esc(it.description || "")}</p>
            <ul>${perks}</ul>
            <div class="embed-price">${esc(it.price_rub)} ₽</div>
            <button type="button" class="embed-buy" data-slug="${esc(it.slug)}" disabled>Купить — скоро</button>
          </article>`;
        })
        .join("");
    } catch (e) {
      root.innerHTML = `<p class="embed-muted">${e.status === 401 ? "Нет сессии. Зайди через лаунчер." : "Не удалось загрузить магазин."}</p>`;
    }
  }

  async function renderCabinet() {
    const root = document.getElementById("embed-root");
    if (!root) return;
    root.innerHTML = `<p class="embed-muted">Кабинет…</p>`;
    try {
      const me = await api("/api/me");
      const nick = me.user?.nick;
      if (!nick) throw Object.assign(new Error("no nick"), { status: 401 });
      let profile = {};
      try {
        const p = await api("/api/profiles/" + encodeURIComponent(nick));
        profile = p.profile || {};
      } catch {
        profile = {};
      }
      root.innerHTML = `
        <div class="embed-id">
          <img src="https://mc-heads.net/avatar/${encodeURIComponent(nick)}/48" alt="" width="48" height="48">
          <div>
            <h2>${esc(nick)}</h2>
            <p>${esc(profile.privilege || "Игрок")}</p>
          </div>
        </div>
        <div class="embed-stats">
          <div><strong>${esc(profile.coins ?? 0)}</strong><span>монеты</span></div>
          <div><strong>${esc(profile.fish ?? 0)}</strong><span>улов</span></div>
          <div><strong>${esc(profile.likes ?? 0)}</strong><span>лайки</span></div>
        </div>
        <p class="embed-bio">${esc(profile.bio || "Био пока пустое.")}</p>`;
    } catch (e) {
      root.innerHTML = `<p class="embed-muted">${e.status === 401 ? "Нет сессии портала. Зайди через лаунчер AquaTech." : "Кабинет недоступен."}</p>`;
    }
  }

  bindChrome();
  if (page() === "donate") renderDonate();
  if (page() === "cabinet") renderCabinet();
})();
