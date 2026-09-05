#!/usr/bin/env python3
"""Upgrade build_hub_html.py to Apple Bento Minimal and visionOS Glassmorphism.
Adds official vector logo embedding, Bento Grid for Profile, and Sfx tab haptics.
"""
import os
import re

SCRIPT_PATH = "tools/build_hub_html.py"

with open(SCRIPT_PATH, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Add logo base64 loader near top
logo_helper = '''
def get_logo_base64():
    logo_file = 'dist/AquaTech-Client/config/fancymenu/assets/logo.png'
    if not os.path.exists(logo_file):
        logo_file = 'bootstrap/winres/icon.png'
    from PIL import Image
    import io, base64
    im = Image.open(logo_file).convert('RGBA').resize((96, 96), Image.LANCZOS)
    buf = io.BytesIO()
    im.save(buf, format='PNG', optimize=True)
    return base64.b64encode(buf.getvalue()).decode('ascii')

logo_b64 = get_logo_base64()
'''

if "def get_logo_base64" not in code:
    code = code.replace("case_icons_json = json.dumps(case_icons, ensure_ascii=False)\n",
                        "case_icons_json = json.dumps(case_icons, ensure_ascii=False)\n" + logo_helper)

# 2. Update .brand and .hub styles in CSS
old_brand_css = """.brand{display:flex;align-items:center;gap:10px;min-width:208px;font-size:17px;font-weight:760;letter-spacing:.01em}
    .brand-mark{width:28px;height:28px;display:grid;place-items:center;border-radius:10px;color:#06120f;background:linear-gradient(135deg,var(--accent),var(--accent2));box-shadow:0 0 25px color-mix(in srgb,var(--accent) 28%,transparent)}
    .brand-mark svg{width:18px;height:18px}"""

new_brand_css = """.brand{display:flex;align-items:center;gap:12px;min-width:216px}
    .brand-logo{width:36px;height:36px;object-fit:contain;filter:drop-shadow(0 0 12px rgba(47,224,192,.45));transition:transform .25s var(--ease)}
    .brand:hover .brand-logo{transform:scale(1.08) rotate(3deg)}
    .brand-title{font-size:17px;font-weight:800;letter-spacing:-.01em;color:#fff;line-height:1.1}
    .brand-sub{font-size:10px;color:var(--muted);font-weight:500;letter-spacing:.02em}"""

code = code.replace(old_brand_css, new_brand_css)

# 3. Update topbar HTML brand
old_brand_html = """      <div class="brand">
        <span class="brand-mark">
          <svg viewBox="0 0 24 24"><path fill="currentColor" d="M3 15c3-7 7-11 13-12-2 3-2 6 0 8 1 1 3 2 5 2-2 5-6 8-11 8-4 0-7-2-7-6Z"/></svg>
        </span>
        AquaLumen
      </div>"""

new_brand_html = """      <div class="brand">
        <img class="brand-logo" src="data:image/png;base64,__AQUATECH_LOGO_B64__" alt="AquaTech" />
        <div>
          <div class="brand-title">AquaTech</div>
          <div class="brand-sub">Ocean Skyblock · 1.20.1</div>
        </div>
      </div>"""

code = code.replace(old_brand_html, new_brand_html)

# 4. Add Apple Bento CSS
bento_css = """
    /* Apple Bento Grid for Profile View */
    .bento-view{height:100%;overflow-y:auto;padding-right:4px}
    .bento-grid{display:grid;grid-template-columns:1.2fr 1fr;grid-template-rows:auto auto;gap:12px;margin-bottom:12px}
    .bento-card{border:1px solid var(--line);border-radius:18px;background:rgba(255,255,255,.025);padding:18px;position:relative;overflow:hidden;box-shadow:inset 0 1px 0 rgba(255,255,255,.08);transition:border-color .2s var(--ease)}
    .bento-card:hover{border-color:rgba(255,255,255,.16)}
    
    .bento-hero{grid-row:span 2;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:24px 18px;background:radial-gradient(circle at 50% 28%,rgba(47,224,192,.14),transparent 68%),rgba(255,255,255,.022)}
    .hero-podium{position:relative;width:124px;height:124px;display:grid;place-items:center;margin-bottom:12px}
    .podium-halo{position:absolute;bottom:6px;width:104px;height:24px;border-radius:50%;background:radial-gradient(ellipse,color-mix(in srgb,var(--accent) 55%,transparent),transparent 75%);filter:blur(8px);animation:halo-pulse 3s ease-in-out infinite alternate}
    @keyframes halo-pulse{from{transform:scale(.9);opacity:.6}to{transform:scale(1.15);opacity:1}}
    .hero-skin{width:92px;height:92px;object-fit:cover;object-position:top center;transform:scale(1.36) translateY(5px);filter:drop-shadow(0 10px 22px rgba(0,0,0,.75));position:relative;z-index:2}
    .hero-avatar-fallback{font-size:32px;font-weight:800;color:var(--accent)}
    .hero-tag{display:inline-block;padding:3px 12px;border-radius:999px;font-size:10.5px;font-weight:800;letter-spacing:.12em;text-transform:uppercase;border:1px solid;margin-bottom:8px}
    .hero-name{margin:0 0 12px;font-size:24px;font-weight:800;letter-spacing:-.01em;color:#fff}
    .hero-lvl-box{width:100%;max-width:240px}
    .hero-lvl-row{display:flex;justify-content:space-between;margin-bottom:6px;font-size:11px;color:var(--muted);font-weight:600}
    
    .bento-finance{display:flex;flex-direction:column;justify-content:space-between;min-height:120px}
    .finance-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:10px}
    .finance-head b{font-size:12px;font-weight:700}
    .finance-tiles{display:grid;grid-template-columns:1fr 1fr;gap:10px}
    .coin-tile{display:flex;align-items:center;gap:10px;padding:10px 12px;border:1px solid var(--line);border-radius:14px;background:rgba(255,255,255,.02);transition:background .18s}
    .coin-tile:hover{background:rgba(255,255,255,.045)}
    .coin-tile small{display:block;font-size:9.5px;color:var(--muted);margin-bottom:2px}
    .tile-val{font-size:16px;font-weight:800;font-variant-numeric:tabular-nums}
    .tile-icon{width:34px;height:34px;display:grid;place-items:center;border-radius:10px;flex:0 0 auto}
    .gold-glow{background:rgba(245,194,91,.14);color:var(--gold);border:1px solid rgba(245,194,91,.3);box-shadow:0 0 16px rgba(245,194,91,.18)}
    .gem-glow{background:rgba(47,224,192,.14);color:var(--accent);border:1px solid rgba(47,224,192,.3);box-shadow:0 0 16px rgba(47,224,192,.18)}
    
    .bento-stats{display:grid;grid-template-columns:repeat(2,1fr);gap:8px}
    .stat-pill{padding:10px 12px;border:1px solid var(--line);border-radius:13px;background:rgba(255,255,255,.02);display:flex;flex-direction:column;justify-content:space-between}
    .stat-pill small{font-size:9.5px;color:var(--muted)}
    .stat-pill b{font-size:15px;font-weight:750;margin-top:4px;font-variant-numeric:tabular-nums}
    
    .bento-season{display:flex;flex-direction:column;justify-content:space-between}
"""

if "Apple Bento Grid for Profile View" not in code:
    code = code.replace(".hero-info{position:relative", bento_css + "    .hero-info{position:relative")

# 5. Replace profileView
old_profile_view = """  function profileView(s) {
    const p = s.profile, w = s.wallet;
    const rankCol = p.rankColor ? "#" + (p.rankColor & 0xffffff).toString(16).padStart(6, "0") : "var(--accent)";
    const playerName = p.name || "Player";
    const skinUrl = "https://mc-heads.net/body/" + encodeURIComponent(playerName) + "/128";
    const initial = esc(playerName[0].toUpperCase());
    return `<div class="view">
      <div class="grid two">
        <section class="card hero">
          <div class="avatar" style="overflow:hidden;position:relative;padding:0;background:radial-gradient(circle at 50% 30%,color-mix(in srgb,var(--accent) 30%,transparent),var(--raised));">
            <img src="${skinUrl}" alt="${esc(playerName)}" style="width:100%;height:100%;object-fit:cover;object-position:top center;transform:scale(1.26) translateY(5px);filter:drop-shadow(0 6px 14px rgba(0,0,0,0.6));" onerror="this.style.display='none';this.nextElementSibling.style.display='block';" />
            <span style="display:none;font-size:30px;font-weight:800;color:var(--accent);">${initial}</span>
          </div>
          <div class="hero-info">
            <h2>${esc(p.name)}</h2>
            <div class="rank" style="color:${rankCol}">${esc(p.rank)}</div>
            <div class="progress-label"><span>Уровень ${p.level}</span><span>${Math.round(p.levelProgress * 100)}%</span></div>
            <div class="progress"><i style="width:${Math.round(p.levelProgress * 100)}%"></i></div>
          </div>
        </section>
        <section class="stats">
          <div class="stat"><small>Время в игре</small><b>${formatHours(p.playtimeMinutes)}</b></div>
          <div class="stat"><small>Квестов выполнено</small><b>${p.quests}</b></div>
          <div class="stat"><small>Убийств / Смертей</small><b>${p.kills} / ${p.deaths}</b></div>
          <div class="stat"><small>Друзей онлайн</small><b>${p.friendsOnline}</b></div>
        </section>
      </div>
      <div class="section-title"><b>Сезонный Прогресс</b><span>Уровень ${s.season.tier} / ${s.season.maxTier}</span></div>
      <section class="card season">
        <div class="season-head">
          <div><h3>${esc(s.season.title)}</h3><p>${s.season.premium ? "Премиум пропуск активен" : "Базовый доступ"}</p></div>
          <b class="tier">T${s.season.tier}</b>
        </div>
        <div class="progress-label"><span>Прогресс уровня</span><span>${Math.round(s.season.tierProgress * 100)}%</span></div>
        <div class="progress"><i style="width:${Math.round(s.season.tierProgress * 100)}%"></i></div>
      </section>
    </div>`;
  }"""

new_profile_view = """  function profileView(s) {
    const p = s.profile, w = s.wallet;
    const rankCol = p.rankColor ? "#" + (p.rankColor & 0xffffff).toString(16).padStart(6, "0") : "var(--accent)";
    const playerName = p.name || "Player";
    const skinUrl = "https://mc-heads.net/body/" + encodeURIComponent(playerName) + "/160";
    const initial = esc(playerName[0].toUpperCase());

    return `<div class="view bento-view">
      <div class="bento-grid">
        <section class="card bento-card bento-hero">
          <div class="hero-podium">
            <div class="podium-halo"></div>
            <img src="${skinUrl}" alt="${esc(playerName)}" class="hero-skin" onerror="this.style.display='none';this.nextElementSibling.style.display='grid';" />
            <span class="hero-avatar-fallback" style="display:none;">${initial}</span>
          </div>
          <div class="hero-tag" style="color:${rankCol};border-color:${rankCol}55;background:${rankCol}16">${esc(p.rank)}</div>
          <h2 class="hero-name">${esc(p.name)}</h2>
          <div class="hero-lvl-box">
            <div class="hero-lvl-row"><span>Уровень ${p.level}</span><span>${Math.round(p.levelProgress * 100)}%</span></div>
            <div class="progress"><i style="width:${Math.round(p.levelProgress * 100)}%"></i></div>
          </div>
        </section>

        <section class="card bento-card bento-finance">
          <div class="finance-head">
            <b>Казна и Баланс</b>
            <button class="button" style="font-size:9.5px;padding:2px 8px;min-height:22px" onclick="document.querySelector('[data-tab=store]')?.click()">Магазин ➔</button>
          </div>
          <div class="finance-tiles">
            <div class="coin-tile">
              <div class="tile-icon gold-glow">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><ellipse cx="12" cy="7" rx="7" ry="3"/><path d="M5 7v4c0 1.7 3.1 3 7 3s7-1.3 7-3V7"/><path d="M5 11v4c0 1.7 3.1 3 7 3s7-1.3 7-3v-4"/></svg>
              </div>
              <div>
                <small>АкваМонеты</small>
                <div class="tile-val" style="color:var(--gold)">${num(w.coins)} ¤</div>
              </div>
            </div>
            <div class="coin-tile">
              <div class="tile-icon gem-glow">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2.5 18 8l-6 13L6 8Z"/><path d="M6 8h12M12 2.5 9.5 8l2.5 13M12 2.5 14.5 8 12 21" stroke="#000" stroke-width=".8" opacity=".5"/></svg>
              </div>
              <div>
                <small>Кристаллы</small>
                <div class="tile-val" style="color:var(--accent)">${num(w.gems)}</div>
              </div>
            </div>
          </div>
        </section>

        <section class="card bento-card bento-stats">
          <div class="stat-pill"><small>⏱ Время в игре</small><b>${formatHours(p.playtimeMinutes)}</b></div>
          <div class="stat-pill"><small>📜 Квестов закрыто</small><b>${p.quests}</b></div>
          <div class="stat-pill"><small>⚔ Убийств / Смертей</small><b>${p.kills} / ${p.deaths}</b></div>
          <div class="stat-pill"><small>👥 Друзей онлайн</small><b>${p.friendsOnline}</b></div>
        </section>
      </div>

      <section class="card bento-card bento-season">
        <div class="season-head">
          <div>
            <h3 style="margin:0 0 4px;font-size:14px;">${esc(s.season.title)}</h3>
            <p style="margin:0;color:var(--muted);font-size:11px;">${s.season.premium ? "★ Премиум Пропуск" : "Базовый доступ"} · Серия входов: <b style="color:var(--gold)">🔥 ${w.dailyStreak || 1} дн.</b></p>
          </div>
          <b class="tier">T${s.season.tier}</b>
        </div>
        <div class="progress-label" style="margin-top:10px;"><span>Прогресс сезона</span><span>${Math.round((s.season.tierProgress || 0) * 100)}%</span></div>
        <div class="progress"><i style="width:${Math.round((s.season.tierProgress || 0) * 100)}%"></i></div>
      </section>
    </div>`;
  }"""

code = code.replace(old_profile_view, new_profile_view)

# 6. Add Sfx.tab audio synthesis
sfx_tab_code = """    tab() {
      if (!this.ctx) return;
      try {
        const t = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = "sine";
        osc.frequency.setValueAtTime(1600, t);
        osc.frequency.exponentialRampToValueAtTime(340, t + 0.016);
        gain.gain.setValueAtTime(0.045, t);
        gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.016);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(t);
        osc.stop(t + 0.018);
      } catch (e) {}
    },
    tick(speedRatio) {"""

code = code.replace("    tick(speedRatio) {", sfx_tab_code)

# 7. Add Sfx.tab call to nav button click
old_nav_click = """      b.onclick = () => {
        if (state.tab === b.dataset.tab) return;
        state.tab = b.dataset.tab;
        renderNav();
        renderView(true, false);
      };"""

new_nav_click = """      b.onclick = () => {
        if (state.tab === b.dataset.tab) return;
        Sfx.ensure();
        Sfx.tab();
        state.tab = b.dataset.tab;
        renderNav();
        renderView(true, false);
      };"""

code = code.replace(old_nav_click, new_nav_click)

# 8. Replace __AQUATECH_LOGO_B64__ in hub_html_content
old_replace = 'hub_html_content = hub_html_raw.replace("__TEXTURES_JSON__", textures_json).replace("__CASE_ICONS_JSON__", case_icons_json)'
new_replace = 'hub_html_content = hub_html_raw.replace("__TEXTURES_JSON__", textures_json).replace("__CASE_ICONS_JSON__", case_icons_json).replace("__AQUATECH_LOGO_B64__", logo_b64)'

code = code.replace(old_replace, new_replace)

with open(SCRIPT_PATH, "w", encoding="utf-8") as f:
    f.write(code)

print("OK: build_hub_html.py upgraded successfully!")
