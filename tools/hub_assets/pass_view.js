  /* Premium track: mirror of PremiumPassRewards.java - keep both in sync. */
  const PASS_PREMIUM_COINS = [2500, 3000, 3500, 4000, 5000, 6000, 7000, 8000, 9000, 12000, 14000, 16000, 18000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000, 70000, 80000, 100000];
  const PASS_PREMIUM_EXTRA = {
    3: { kind: "booster", id: "small", label: "Бустер" },
    5: { kind: "case", id: "smeltery" },
    7: { kind: "booster", id: "small", label: "Бустер" },
    10: { kind: "case", id: "steam" },
    13: { kind: "item", id: "aquatech_ui:rate_x8", label: "Множитель ×8" },
    14: { kind: "booster", id: "large", label: "Бустер большой" },
    15: { kind: "case", id: "flora" },
    18: { kind: "booster", id: "large", label: "Бустер большой" },
    20: { kind: "case", id: "abyss" },
    23: { kind: "item", id: "aquatech_ui:rate_x32", label: "Множитель ×32" },
    25: { kind: "case", id: "singularity" },
  };

  function passView(s) {
    const season = s.season || { tier: 1, maxTier: 25 };
    const cur = Number(season.tier) || 1;
    const claimed = (season.claimedTiers || []).map(Number);
    const claimedP = (season.claimedPremiumTiers || []).map(Number);
    const hasPrem = Boolean(season.premium);
    const claimableCount = Number(season.claimable) || 0;
    const maxT = 25;
    const N = 6;
    if (state.passStart == null) state.passStart = Math.max(1, Math.min(maxT - N + 1, cur - 1));
    const start = Math.max(1, Math.min(maxT - N + 1, state.passStart));
    const prog = Math.round((season.tierProgress || 0) * 100);
    const short = (n) => n >= 1000 ? String(n / 1000).replace(".", ",") + "к" : String(n);
    const caseTitle = (id) => {
      const c = (s.cases || []).find((x) => x.id === id);
      return String((c && c.title) || id).replace(/^Кейс [IVX]+: ?/, "");
    };
    const coinSprite = (n) => n < 10000 ? "coin-1" : n < 30000 ? "coin-3" : n < 70000 ? "bag" : "chest";
    const iconOf = (r) => {
      if (r.type === "case" && CASE_ICONS[r.item]) return `<img class="np-ic" src="${CASE_ICONS[r.item]}" alt="">`;
      const mult = /rate_x(\d+)$/.exec(r.item || "");
      if (mult) return [2, 4, 8, 16, 32, 64].includes(Number(mult[1])) ? `<i class="np-rate" style="background-image:var(--s-rate-${mult[1]})"></i>` : `<span class="np-mult m${mult[1]}">×${mult[1]}</span>`;
      if (r.item) return getItemIconHtml(r.label, r.item, "np-ic", r.type);
      return `<i class="np-coin ${coinSprite(r.coins)}"></i>`;
    };
    const premiumReward = (t) => {
      const ex = PASS_PREMIUM_EXTRA[t] || {};
      const base = { tier: t, coins: PASS_PREMIUM_COINS[t - 1] || 0 };
      if (ex.kind === "case") return Object.assign(base, { type: "case", item: ex.id, label: caseTitle(ex.id), name: caseTitle(ex.id) });
      if (ex.kind === "item") return Object.assign(base, { item: ex.id, label: ex.label, name: ex.label });
      if (ex.kind === "booster") return Object.assign(base, { booster: ex.id, label: ex.label, name: ex.label });
      return Object.assign(base, { label: "Монеты", name: "Монеты" });
    };
    const premIcon = (r) => r.booster ? `<i class="np-coin booster${r.booster === "large" ? " big" : ""}"></i>` : iconOf(r);

    let free = "", prem = "", line = "";
    for (let t = start; t < start + N; t++) {
      const r = PASS_REWARDS[t - 1] || { tier: t, label: "Монеты", coins: 2500 + t * 1500, rarity: "common" };
      const isClaimed = claimed.includes(t);
      const ready = !isClaimed && cur >= t;
      const st = isClaimed ? "done" : ready ? "ready" : (t === cur + 1 ? "next" : "lock");
      const name = (r.type === "case" || r.item) ? String(r.label).replace(/^Кейс [IVX]+: ?/, "") : "Монеты";
      free += `<div class="np-col" style="--i:${t - start}"><div class="np-slot ${st}${ready ? " claim-pass" : ""}" ${ready ? `data-level="${t}"` : ""} title="${esc(r.label)}">
        ${ready ? '<span class="np-take">ЗАБРАТЬ!</span>' : ""}
        ${iconOf(r)}${isClaimed ? '<i class="np-ck"></i>' : ""}
        <span class="np-cnt">${short(r.coins)}</span></div>
        <div class="np-name">${esc(name)}</div></div>`;
      const pr = premiumReward(t);
      const pClaimed = claimedP.includes(t);
      const pReady = hasPrem && !pClaimed && cur >= t;
      const pst = !hasPrem ? "plock" : pClaimed ? "done" : pReady ? "ready" : (t === cur + 1 ? "next" : "lock");
      prem += `<div class="np-col" style="--i:${t - start}"><div class="np-slot prem ${pst}${pReady ? " claim-prem" : ""}" ${pReady ? `data-plevel="${t}"` : ""} title="${esc(pr.label)}">
        ${pReady ? '<span class="np-take">ЗАБРАТЬ!</span>' : ""}
        ${premIcon(pr)}${pClaimed ? '<i class="np-ck"></i>' : ""}${!hasPrem ? '<i class="np-lk"></i>' : ""}
        <span class="np-cnt">${short(pr.coins)}</span></div>
        <div class="np-name">${esc(pr.name)}</div></div>`;
      line += `<div class="np-col"><div class="np-dia ${t === cur ? "now" : t < cur ? "past" : ""}"><span>${t}</span></div></div>`;
    }
    const earnedFree = PASS_REWARDS.reduce((a, r) => a + (claimed.includes(r.tier) ? r.coins : 0), 0);
    const earnedPrem = claimedP.reduce((a, t) => a + (PASS_PREMIUM_COINS[t - 1] || 0), 0);
    const top = PASS_REWARDS[maxT - 1] || {};
    const topIcon = top.type === "case" && CASE_ICONS[top.item] ? `<img class="np-bigic" src="${CASE_ICONS[top.item]}" alt="">` : "";
    const offer = (s.store || []).find((o) => o.id === "pass.premium");
    const price = offer ? Number(offer.price) : 100;
    const buy = hasPrem
      ? '<div class="np-emb np-on">ПРЕМИУМ АКТИВЕН</div>'
      : `<button class="np-buy button primary" type="button" data-price="${price}">Премиум · ${price} кр.</button>`;
    const total = hasPrem ? maxT * 2 : maxT;
    return `<div class="view np">
      <div class="np-head"><div><h2>Сезонный пропуск</h2><p>${esc(season.title || "Сезон 1")} · ${maxT} уровней</p></div>
        <div class="np-headr">${buy}<div class="np-emb">Получено <b>${claimed.length + claimedP.length}</b> из ${total}</div></div></div>
      <section class="np-board">
        <aside class="np-prem">
          <span class="np-ribbon">ГЛАВНАЯ НАГРАДА</span>
          ${topIcon}
          <h3>${esc(String(top.label || "").replace(/^Кейс [IVX]+: ?/, "") || "Финал")}</h3>
          <ul><li>Уровень ${maxT}</li><li>Доступно: ${claimableCount}</li></ul>
        </aside>
        <div class="np-trackwrap">
          <button class="np-arr l" data-ps="-1" type="button" aria-label="Назад">&#9664;</button>
          <div class="np-cap">Бесплатная дорожка</div>
          <div class="np-grid">${free}</div>
          <div class="np-grid np-lineg">${line}</div>
          <div class="np-cap pr">Премиум дорожка${hasPrem ? "" : " · нужен пропуск"}</div>
          <div class="np-grid">${prem}</div>
          <button class="np-arr r" data-ps="1" type="button" aria-label="Дальше">&#9654;</button>
        </div>
      </section>
      <div class="np-xp">
        <div class="np-xpbox"><b>${cur} LVL</b><div class="np-bar"><i style="width:${prog}%"></i><em>${prog}%</em></div><b class="pc">${cur >= maxT ? "МАКС" : "→ " + (cur + 1)}</b></div>
      </div>
      <div class="np-stats">
        <div class="np-st"><small>Получено наград</small><b>${claimed.length + claimedP.length}<i> / ${total}</i></b></div>
        <div class="np-st"><small>Монет за сезон</small><b>${coins(earnedFree + earnedPrem)}</b></div>
        <div class="np-st${claimableCount > 0 ? " hot" : ""}"><small>Ждут получения</small><b>${claimableCount}</b></div>
        <div class="np-st"><small>До финала</small><b>${Math.max(0, maxT - cur)}<i> ур.</i></b></div>
      </div>
    </div>`;
  }

  document.addEventListener("click", function (e) {
    const arr = e.target.closest(".np-arr");
    if (arr) {
      state.passStart = (state.passStart == null ? 1 : state.passStart) + Number(arr.dataset.ps);
      renderView(false, true);
      return;
    }
    const claim = e.target.closest(".claim-prem");
    if (claim) {
      action("pass.claim_premium", claim.dataset.plevel);
      return;
    }
    const buy = e.target.closest(".np-buy");
    if (buy) {
      confirmAction("Боевой Пропуск", `Открыть премиум-дорожку за <b>${esc(buy.dataset.price)} кр.</b>?`, "store.buy", "pass.premium");
    }
  });

