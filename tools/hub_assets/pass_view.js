  function passView(s) {
    const season = s.season || { tier: 1, maxTier: 25 };
    const cur = Number(season.tier) || 1;
    const claimed = (season.claimedTiers || []).map(Number);
    const claimableCount = Number(season.claimable) || 0;
    const maxT = 25;
    const N = 6;
    if (state.passStart == null) state.passStart = Math.max(1, Math.min(maxT - N + 1, cur - 1));
    const start = Math.max(1, Math.min(maxT - N + 1, state.passStart));
    const prog = Math.round((season.tierProgress || 0) * 100);
    const short = (n) => n >= 1000 ? String(n / 1000).replace(".", ",") + "к" : String(n);
    const iconOf = (r) => {
      if (r.type === "case" && CASE_ICONS[r.item]) return `<img class="np-ic" src="${CASE_ICONS[r.item]}" alt="">`;
      const mult = /rate_x(\d+)$/.exec(r.item || "");
      if (mult) return [2, 4, 8, 16, 32, 64].includes(Number(mult[1])) ? `<i class="np-rate" style="background-image:var(--s-rate-${mult[1]})"></i>` : `<span class="np-mult m${mult[1]}">×${mult[1]}</span>`;
      if (r.item) return getItemIconHtml(r.label, r.item, "np-ic", r.type);
      const spr = r.coins < 10000 ? "coin-1" : r.coins < 30000 ? "coin-3" : r.coins < 70000 ? "bag" : "chest";
      return `<i class="np-coin ${spr}"></i>`;
    };
    let free = "", line = "";
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
      line += `<div class="np-col"><div class="np-dia ${t === cur ? "now" : t < cur ? "past" : ""}"><span>${t}</span></div></div>`;
    }
    const marks = PASS_REWARDS.filter((r) => r.type === "case" || /rate_x/.test(r.item || "")).map((r) => {
      const t = r.tier, isClaimed = claimed.includes(t), ready = !isClaimed && cur >= t;
      const st = isClaimed ? "done" : ready ? "ready" : "lock";
      const nm = r.type === "case" ? (String(r.label).match(/^Кейс [IVX]+/) || ["Кейс"])[0] : "Бафф ×" + (/rate_x(.*)$/.exec(r.item) || [0, ""])[1];
      const tail = isClaimed ? "ПОЛУЧЕНО" : ready ? "ЗАБРАТЬ!" : "ЕЩЁ " + (t - cur) + " УР.";
      return `<div class="np-ms ${st}${ready ? " claim-pass" : ""}" ${ready ? `data-level="${t}"` : ""}><div class="np-msic">${iconOf(r)}</div><b>${esc(nm)}</b><span>УР. ${t}</span><em>${tail}</em></div>`;
    }).join("");
    const earned = PASS_REWARDS.reduce((a, r) => a + (claimed.includes(r.tier) ? r.coins : 0), 0);
    const top = PASS_REWARDS[maxT - 1] || {};
    const topIcon = top.type === "case" && CASE_ICONS[top.item] ? `<img class="np-bigic" src="${CASE_ICONS[top.item]}" alt="">` : "";
    return `<div class="view np">
      <div class="np-head"><div><h2>Сезонный пропуск</h2><p>${esc(season.title || "Сезон 1")} · ${maxT} уровней</p></div>
        <div class="np-emb">Получено <b>${claimed.length}</b> из ${maxT}</div></div>
      <section class="np-board">
        <aside class="np-prem">
          <span class="np-ribbon">ГЛАВНАЯ НАГРАДА</span>
          ${topIcon}
          <h3>${esc(String(top.label || "").replace(/^Кейс [IVX]+: ?/, "") || "Финал")}</h3>
          <ul><li>Уровень ${maxT}</li><li>Доступно: ${claimableCount}</li></ul>
        </aside>
        <div class="np-trackwrap">
          <button class="np-arr l" data-ps="-1" type="button" aria-label="Назад">&#9664;</button>
          <div class="np-grid">${free}</div>
          <div class="np-grid np-lineg">${line}</div>
          <button class="np-arr r" data-ps="1" type="button" aria-label="Дальше">&#9654;</button>
        </div>
      </section>
      <div class="np-xp">
        <div class="np-xpbox"><b>${cur} LVL</b><div class="np-bar"><i style="width:${prog}%"></i><em>${prog}%</em></div><b class="pc">${cur >= maxT ? "МАКС" : "→ " + (cur + 1)}</b></div>
      </div>
      <div class="section-title"><b>Главные награды</b><span>кейсы и баффы сезона</span></div>
      <div class="np-msg">${marks}</div>
      <div class="np-stats">
        <div class="np-st"><small>Получено наград</small><b>${claimed.length}<i> / ${maxT}</i></b></div>
        <div class="np-st"><small>Монет за сезон</small><b>${coins(earned)}</b></div>
        <div class="np-st${claimableCount > 0 ? " hot" : ""}"><small>Ждут получения</small><b>${claimableCount}</b></div>
        <div class="np-st"><small>До финала</small><b>${Math.max(0, maxT - cur)}<i> ур.</i></b></div>
      </div>
    </div>`;
  }

  document.addEventListener("click", function (e) {
    const b = e.target.closest(".np-arr");
    if (!b) return;
    state.passStart = (state.passStart == null ? 1 : state.passStart) + Number(b.dataset.ps);
    renderView(false, true);
  });

