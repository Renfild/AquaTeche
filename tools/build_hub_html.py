import json
import os

with open('tools/label_texture_map.json', 'r', encoding='utf-8') as f:
    label_textures = json.load(f)

with open('tools/extracted_case_textures.json', 'r', encoding='utf-8') as f:
    item_textures = json.load(f)

with open('tools/case_icon_map.json', 'r', encoding='utf-8') as f:
    case_icons = json.load(f)

all_textures = {**item_textures, **label_textures}
textures_json = json.dumps(all_textures, ensure_ascii=False)
case_icons_json = json.dumps(case_icons, ensure_ascii=False)

hub_html_raw = r'''<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>AquaLumen</title>
  <style>
    head, title, style, script { display: none !important; }
    :root{
      color-scheme:dark;--bg:#070c12;--panel:rgba(14,21,30,.94);--raised:#16202c;--line:rgba(255,255,255,.10);
      --text:#f2f7fa;--muted:#9db2c4;--accent:#2fe0c0;--accent2:#3b9dff;--gold:#f5c25b;
      --danger:#ff6b6b;--success:#4cd08a;--radius:20px;--ease:cubic-bezier(.22,.8,.26,1);
    }
    *{box-sizing:border-box}
    html,body{width:100%;height:100%;margin:0;overflow:hidden;background:transparent;color:var(--text);font-family:"Segoe UI",Arial,sans-serif;-webkit-font-smoothing:antialiased}
    button{font:inherit;color:inherit}
    button:focus-visible{outline:2px solid var(--accent);outline-offset:2px}
    .stage{width:100%;height:100%;display:grid;place-items:center;padding:22px}
    .hub{
      width:min(1180px,96vw);height:min(680px,92vh);min-height:540px;display:grid;
      grid-template:58px 1fr 38px / 218px 1fr;overflow:hidden;border:1px solid var(--line);
      border-radius:var(--radius);background:linear-gradient(145deg,var(--panel),rgba(7,12,18,.97));
      box-shadow:none;
      animation:hub-in .38s var(--ease) both;
    }
    .reduce-motion *{animation:none!important;transition:none!important}
    @keyframes hub-in{from{opacity:0;transform:translateY(12px) scale(.985)}to{opacity:1;transform:none}}
    .topbar{grid-column:1/-1;display:flex;align-items:center;gap:14px;padding:0 18px;border-bottom:1px solid var(--line)}
    .brand{display:flex;align-items:center;gap:10px;min-width:208px;font-size:17px;font-weight:760;letter-spacing:.01em}
    .brand-mark{width:28px;height:28px;display:grid;place-items:center;border-radius:10px;color:#06120f;background:linear-gradient(135deg,var(--accent),var(--accent2));box-shadow:0 0 25px color-mix(in srgb,var(--accent) 28%,transparent)}
    .brand-mark svg{width:18px;height:18px}
    .server-title{min-width:0;flex:1;color:var(--muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    .chips{display:flex;gap:8px}
    .chip{display:flex;align-items:center;gap:7px;height:30px;padding:0 11px;border:1px solid var(--line);border-radius:999px;background:rgba(255,255,255,.035);font-size:12px;font-variant-numeric:tabular-nums}
    .chip-dot{width:7px;height:7px;border-radius:50%;background:var(--success);box-shadow:0 0 10px var(--success)}
    .icon-button{width:32px;height:32px;display:grid;place-items:center;border:1px solid var(--line);border-radius:10px;background:transparent;cursor:pointer;transition:.18s var(--ease)}
    .icon-button:hover{background:rgba(255,255,255,.07);border-color:color-mix(in srgb,var(--accent) 45%,var(--line))}
    .icon-button svg{width:16px;height:16px;fill:none;stroke:currentColor;stroke-width:1.8}
    .sidebar{grid-row:2;display:flex;flex-direction:column;padding:14px 10px;border-right:1px solid var(--line);background:rgba(0,0,0,.08)}
    .nav{display:grid;gap:5px}
    .nav-button{height:42px;display:flex;align-items:center;gap:11px;padding:0 12px;border:1px solid transparent;border-radius:12px;background:transparent;color:var(--muted);cursor:pointer;text-align:left;transition:.18s var(--ease)}
    .nav-button:hover{color:var(--text);background:rgba(255,255,255,.045)}
    .nav-button.active{color:var(--text);border-color:color-mix(in srgb,var(--accent) 36%,transparent);background:linear-gradient(90deg,color-mix(in srgb,var(--accent) 14%,transparent),transparent)}
    .nav-button svg{width:18px;height:18px;fill:none;stroke:currentColor;stroke-width:1.7}
    .nav-button.active svg{color:var(--accent);filter:drop-shadow(0 0 6px color-mix(in srgb,var(--accent) 45%,transparent))}
    .nav-label{flex:1;font-size:13px}
    .badge{min-width:19px;padding:2px 6px;border-radius:999px;background:color-mix(in srgb,var(--accent) 18%,transparent);color:var(--accent);font-size:10px;text-align:center}
    .daily{margin-top:auto;padding:13px;border:1px solid var(--line);border-radius:14px;background:linear-gradient(145deg,rgba(255,255,255,.04),rgba(255,255,255,.015))}
    .daily strong{display:block;font-size:12px}
    .daily span{display:block;margin:4px 0 10px;color:var(--muted);font-size:10px;line-height:1.45}
    .content{grid-row:2;overflow:hidden;position:relative;padding:20px 22px}
    .view{height:100%;overflow-y:auto;padding-right:5px}.view.view-enter{animation:view-in .22s var(--ease) both}
    .view::-webkit-scrollbar{width:4px}.view::-webkit-scrollbar-thumb{border-radius:4px;background:rgba(255,255,255,.16)}
    @keyframes view-in{from{opacity:0;transform:translateX(7px)}to{opacity:1;transform:none}}
    .view-title{display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:15px}
    .view-title h1{margin:0;font-size:22px;font-weight:760}.view-title p{margin:5px 0 0;color:var(--muted);font-size:11px}
    .grid{display:grid;gap:12px}.grid.two{grid-template-columns:1.25fr .75fr}.grid.three{grid-template-columns:repeat(3,1fr)}
    .card{border:1px solid var(--line);border-radius:15px;background:rgba(255,255,255,.028);padding:15px;overflow:hidden}
    .hero{min-height:180px;position:relative;display:flex;gap:18px;align-items:center;background:
      radial-gradient(circle at 82% 18%,color-mix(in srgb,var(--accent2) 17%,transparent),transparent 36%),
      radial-gradient(circle at 20% 100%,color-mix(in srgb,var(--accent) 12%,transparent),transparent 40%),rgba(255,255,255,.025)}
    .hero:after{content:"";position:absolute;inset:0;background-image:linear-gradient(var(--line) 1px,transparent 1px),linear-gradient(90deg,var(--line) 1px,transparent 1px);background-size:28px 28px;opacity:.12;pointer-events:none}
    .avatar{position:relative;z-index:1;width:84px;height:84px;display:grid;place-items:center;flex:0 0 auto;border:1px solid color-mix(in srgb,var(--accent) 55%,transparent);border-radius:25px;background:linear-gradient(145deg,color-mix(in srgb,var(--accent) 24%,var(--raised)),var(--raised));font-size:30px;font-weight:800;color:var(--accent);box-shadow:0 0 32px color-mix(in srgb,var(--accent) 18%,transparent)}
    .hero-info{position:relative;z-index:1;min-width:0;flex:1}.hero-info h2{margin:0 0 5px;font-size:25px}.rank{color:var(--accent);font-size:12px}
    .progress-label{display:flex;justify-content:space-between;margin:15px 0 6px;color:var(--muted);font-size:10px}
    .progress{height:7px;overflow:hidden;border-radius:6px;background:rgba(255,255,255,.08)}.progress>i{display:block;height:100%;border-radius:inherit;background:linear-gradient(90deg,var(--accent),var(--accent2));box-shadow:0 0 12px var(--accent);transition:width .5s var(--ease)}
    .stats{display:grid;grid-template-columns:repeat(2,1fr);gap:8px}.stat{min-height:76px;padding:12px;border:1px solid var(--line);border-radius:13px;background:rgba(255,255,255,.025)}
    .stat b{display:block;margin-top:7px;font-size:18px;font-variant-numeric:tabular-nums}.stat small{color:var(--muted);font-size:10px}
    .section-title{display:flex;justify-content:space-between;align-items:center;margin:15px 2px 9px;font-size:12px}.section-title span{color:var(--muted);font-size:10px}
    .season{position:relative}.season-head{display:flex;justify-content:space-between;gap:15px}.season h3{margin:0 0 4px;font-size:14px}.season p{margin:0;color:var(--muted);font-size:10px}.tier{font-size:24px;color:var(--gold)}
    .rows{display:grid}.row{display:flex;align-items:center;gap:11px;min-height:42px;border-bottom:1px solid var(--line);font-size:11px}.row:last-child{border:0}.place{width:24px;color:var(--muted)}.row.self{color:var(--accent)}.row-value{margin-left:auto;color:var(--muted)}
    .store-grid,.case-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:12px}
    .offer{min-height:172px;display:flex;flex-direction:column;position:relative}.offer-badge{position:absolute;right:11px;top:11px;padding:3px 7px;border-radius:8px;background:color-mix(in srgb,var(--gold) 16%,transparent);color:var(--gold);font-size:9px}
    .offer-art{height:54px;width:54px;display:grid;place-items:center;margin-bottom:14px;border-radius:16px;background:linear-gradient(135deg,color-mix(in srgb,var(--accent) 20%,transparent),color-mix(in srgb,var(--accent2) 12%,transparent));color:var(--accent);font-size:22px}
    .offer h3{margin:0;font-size:13px}.offer p{margin:5px 0 13px;color:var(--muted);font-size:10px;line-height:1.4}.offer-foot{margin-top:auto;display:flex;align-items:center;justify-content:space-between}.price{font-size:12px;color:var(--gold)}
    .button{min-height:30px;padding:0 13px;border:1px solid color-mix(in srgb,var(--accent) 36%,transparent);border-radius:10px;background:color-mix(in srgb,var(--accent) 11%,transparent);color:var(--accent);font-size:10px;cursor:pointer;transition:.18s var(--ease)}
    .button:hover{background:color-mix(in srgb,var(--accent) 20%,transparent);transform:translateY(-1px)}.button:disabled{opacity:.45;cursor:default;transform:none}
    .button.primary{border:0;background:linear-gradient(135deg,var(--accent),var(--accent2));color:#06110f;font-weight:750}
    
    /* Premium Case Cards */
    .case{min-height:245px;text-align:center;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:16px 14px;position:relative;transition:transform .22s var(--ease), border-color .22s}
    .case:hover{transform:translateY(-3px);box-shadow:0 12px 30px rgba(0,0,0,.4)}
    .case-card-img{width:96px;height:96px;object-fit:contain;margin:4px 0 10px;filter:drop-shadow(0 8px 18px rgba(0,0,0,.65));transition:transform .26s var(--ease)}
    .case:hover .case-card-img{transform:scale(1.08) translateY(-2px)}
    .case h3{margin:0 0 3px;font-size:14px;font-weight:780}
    .case-rarity{display:inline-block;margin:0 0 12px;font-size:9.5px;letter-spacing:.14em;text-transform:uppercase;font-weight:800;padding:2px 8px;border-radius:999px;border:1px solid}
    .case-actions-row{display:flex;gap:6px;width:100%;margin-top:auto}
    .case-actions-row .button{flex:1;font-size:9.5px;padding:0 6px}

    /* Drop Table Preview Modal */
    .case-preview-layer{position:fixed;inset:0;z-index:24;display:none;place-items:center;background:rgba(2,5,9,.84);backdrop-filter:blur(10px)}
    .case-preview-layer.open{display:grid}
    .case-preview-modal{position:relative;width:min(640px,94vw);max-height:86vh;padding:24px;border:1px solid var(--line);border-radius:22px;background:#0d151d;box-shadow:0 30px 90px rgba(0,0,0,.85);display:flex;flex-direction:column;animation:hub-in .25s var(--ease) both}
    .close-preview{position:absolute;right:16px;top:16px;z-index:2}
    .case-drop-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(128px,1fr));gap:10px;overflow-y:auto;max-height:360px;padding-right:4px;margin-top:10px}
    .case-drop-grid::-webkit-scrollbar{width:4px}.case-drop-grid::-webkit-scrollbar-thumb{border-radius:4px;background:rgba(255,255,255,.16)}
    .drop-item-card{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;padding:12px 8px;border:1px solid rgba(255,255,255,.08);border-radius:14px;background:rgba(255,255,255,.025);text-align:center;position:relative}
    .drop-item-card b{font-size:10px;font-weight:600;margin-top:2px;line-height:1.2;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
    .drop-item-chance{font-size:8.5px;padding:2px 6px;border-radius:6px;border:1px solid;font-weight:700;letter-spacing:.05em}

    .pass-track{display:grid;grid-template-columns:repeat(5,1fr);gap:10px;margin-top:12px}.reward{text-align:center}.reward-level{color:var(--muted);font-size:9px}.reward-icon{height:62px;display:grid;place-items:center;margin:6px 0;border:1px solid var(--line);border-radius:14px;background:rgba(255,255,255,.025);font-size:22px}.reward.claimable .reward-icon{border-color:color-mix(in srgb,var(--accent) 45%,transparent);box-shadow:0 0 18px color-mix(in srgb,var(--accent) 13%,transparent)}
    .settings{max-width:650px}.setting{display:flex;align-items:center;gap:16px;padding:14px 0;border-bottom:1px solid var(--line)}.setting:last-child{border:0}.setting-info{flex:1}.setting-info b{display:block;font-size:12px}.setting-info span{color:var(--muted);font-size:10px}
    .theme-picker{display:flex;gap:7px}.swatch{width:30px;height:30px;border:2px solid transparent;border-radius:10px;cursor:pointer}.swatch.active{border-color:white}.swatch[data-theme=aqua_lumen]{background:linear-gradient(135deg,#2fe0c0,#3b9dff)}.swatch[data-theme=violet_lumen]{background:linear-gradient(135deg,#b072ff,#ff6bc1)}.swatch[data-theme=midnight_rose]{background:linear-gradient(135deg,#ff7a9c,#ffb27a)}
    .toggle{width:42px;height:23px;padding:2px;border:0;border-radius:20px;background:rgba(255,255,255,.13);cursor:pointer}.toggle i{display:block;width:19px;height:19px;border-radius:50%;background:var(--muted);transition:.2s var(--ease)}.toggle.on{background:color-mix(in srgb,var(--accent) 26%,transparent)}.toggle.on i{transform:translateX(19px);background:var(--accent)}
    .footer{grid-column:1/-1;display:flex;align-items:center;gap:12px;padding:0 15px;border-top:1px solid var(--line);color:var(--muted);font-size:9px}.footer-build{margin-right:auto}.key{padding:2px 6px;border:1px solid var(--line);border-radius:6px;background:rgba(255,255,255,.04);color:var(--text)}
    .toast{position:fixed;left:50%;bottom:32px;z-index:20;min-width:230px;padding:11px 14px;border:1px solid color-mix(in srgb,var(--accent) 40%,var(--line));border-radius:12px;background:#111a23;color:var(--text);font-size:11px;box-shadow:0 15px 45px rgba(0,0,0,.45);transform:translate(-50%,20px);opacity:0;pointer-events:none;transition:.22s var(--ease)}.toast.show{transform:translate(-50%,0);opacity:1}
    .modal-layer{position:fixed;inset:0;z-index:15;display:none;place-items:center;background:rgba(2,5,9,.72);backdrop-filter:blur(8px)}.modal-layer.open{display:grid}
    .modal{width:min(390px,88vw);padding:20px;border:1px solid var(--line);border-radius:17px;background:#101820;box-shadow:0 25px 80px rgba(0,0,0,.6)}.modal h2{margin:0 0 7px;font-size:17px}.modal p{margin:0 0 18px;color:var(--muted);font-size:11px;line-height:1.55}.modal-actions{display:flex;justify-content:flex-end;gap:8px}
    .empty{height:100%;display:grid;place-items:center;color:var(--muted);font-size:12px}
    
    /* Case Opening Modal & Roulette */
    .case-layer{position:fixed;inset:0;z-index:25;display:none;place-items:center;background:rgba(2,5,9,.88);backdrop-filter:blur(12px)}
    .case-layer.open{display:grid}
    .case-modal{width:min(660px,94vw);padding:24px 24px 20px;border:1px solid var(--line);border-radius:22px;background:#0d151d;box-shadow:0 30px 100px rgba(0,0,0,.85);text-align:center;animation:hub-in .3s var(--ease) both}
    .case-modal h2{margin:8px 0 3px;font-size:20px;font-weight:780}
    .case-modal .case-sub{margin:0 0 16px;color:var(--muted);font-size:11px}
    .reel{position:relative;margin:0 auto 16px;width:min(580px,100%);height:124px;overflow:hidden;border:1px solid var(--line);border-radius:16px;background:rgba(4,8,14,.8);box-shadow:inset 0 0 30px rgba(0,0,0,.6)}
    .reel:before,.reel:after{content:"";position:absolute;top:0;bottom:0;width:80px;z-index:4;pointer-events:none}
    .reel:before{left:0;background:linear-gradient(90deg,#0d151d 15%,transparent)}
    .reel:after{right:0;background:linear-gradient(-90deg,#0d151d 15%,transparent)}
    
    .reel-marker{
      position:absolute;left:50%;top:0;bottom:0;width:2px;margin-left:-1px;z-index:6;
      background:linear-gradient(180deg,var(--accent),var(--accent2));
      box-shadow:0 0 16px var(--accent), 0 0 4px #fff;
      pointer-events:none;
    }
    .reel-marker:before{
      content:"";position:absolute;top:0;left:50%;transform:translateX(-50%);
      border-left:8px solid transparent;border-right:8px solid transparent;
      border-top:10px solid var(--accent);filter:drop-shadow(0 2px 5px rgba(0,0,0,.8));
    }
    .reel-marker:after{
      content:"";position:absolute;bottom:0;left:50%;transform:translateX(-50%);
      border-left:8px solid transparent;border-right:8px solid transparent;
      border-bottom:10px solid var(--accent);filter:drop-shadow(0 -2px 5px rgba(0,0,0,.8));
    }

    .reel-strip{position:absolute;top:10px;bottom:10px;left:0;display:flex;gap:8px;will-change:transform;-webkit-transform:translate3d(0,0,0);transform:translate3d(0,0,0);backface-visibility:hidden;perspective:1000px;contain:layout style paint}
    .reel-tile{
      flex:0 0 108px;width:108px;display:flex;flex-direction:column;align-items:center;justify-content:center;
      gap:6px;padding:6px 6px;border:1px solid rgba(255,255,255,.1);border-radius:14px;
      background:rgba(255,255,255,.03);font-size:9.5px;line-height:1.2;text-align:center;overflow:hidden;
      box-shadow:inset 0 0 16px rgba(0,0,0,.4);transition:border-color .2s, box-shadow .2s;
    }
    .reel-tile b{font-size:10px;line-height:1.15;font-weight:600;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
    .reel-tile.win{
      animation:tile-win .6s cubic-bezier(.17,.89,.32,1.28) both;
      z-index:2;
    }
    @keyframes tile-win{
      0%{transform:scale(1)}
      50%{transform:scale(1.14)}
      100%{transform:scale(1.08)}
    }
    
    /* Authentic Minecraft Pixel Item & 3D Block Icons */
    .mc-icon{
      width:44px;height:44px;
      image-rendering:pixelated;
      image-rendering:-moz-crisp-edges;
      image-rendering:crisp-edges;
      filter:drop-shadow(0 4px 8px rgba(0,0,0,.7));
      object-fit:contain;
      pointer-events:none;
    }
    .mc-icon-sm{
      width:16px;height:16px;
      image-rendering:pixelated;
      image-rendering:crisp-edges;
      vertical-align:middle;
    }
    .mc-icon-lg{
      width:52px;height:52px;
      image-rendering:pixelated;
      image-rendering:crisp-edges;
      filter:drop-shadow(0 6px 14px rgba(0,0,0,.8));
    }

    .case-reveal{min-height:108px;display:grid;place-items:center;align-content:center;gap:7px}
    .case-reveal .win-title{font-size:10px;letter-spacing:.3em;color:var(--muted);font-weight:700}
    .case-reveal .win-item{font-size:22px;font-weight:800}
    .case-reveal .win-rarity{padding:3px 12px;border:1px solid;border-radius:999px;font-size:10px;letter-spacing:.14em;font-weight:700}
    .case-reveal .win-amount{color:var(--gold);font-size:13px;font-weight:700}
    .case-wait{min-height:108px;display:grid;place-items:center;color:var(--muted);font-size:12px}
    .case-actions{display:flex;justify-content:center;gap:10px;margin-top:6px;min-height:32px}
    .confetti{position:fixed;left:50%;top:42%;z-index:30;width:8px;height:14px;border-radius:2px;pointer-events:none;animation:confetti-fly 1.35s ease-out forwards}
    @keyframes confetti-fly{0%{opacity:1;transform:translate(0,0) rotate(0)}100%{opacity:0;transform:translate(var(--cx),var(--cy)) rotate(var(--cr))}}
    @media(max-width:900px){.stage{padding:10px}.hub{width:98vw;height:96vh;grid-template-columns:166px 1fr}.brand{min-width:156px}.chips .chip:nth-child(2){display:none}.content{padding:15px}.grid.two{grid-template-columns:1fr}.stats{grid-template-columns:repeat(4,1fr)}.store-grid,.case-grid{grid-template-columns:repeat(2,1fr)}}
    @media(max-height:610px){.hub{min-height:0;height:96vh}.topbar{height:48px}.hub{grid-template-rows:48px 1fr 32px}.daily{display:none}.content{padding-top:12px;padding-bottom:12px}.hero{min-height:150px}}
  </style>
</head>
<body>
<div class="stage">
  <main class="hub" id="hub">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">
          <svg viewBox="0 0 24 24"><path fill="currentColor" d="M3 15c3-7 7-11 13-12-2 3-2 6 0 8 1 1 3 2 5 2-2 5-6 8-11 8-4 0-7-2-7-6Z"/></svg>
        </span>
        AquaLumen
      </div>
      <div class="server-title" id="serverName">AquaTech Network</div>
      <div class="chips">
        <span class="chip"><i class="chip-dot"></i><b id="online">—/—</b></span>
        <span class="chip"><b id="tps">— TPS</b></span>
        <span class="chip" style="color:var(--gold);"><b id="coins">0</b>&nbsp;монет</span>
        <span class="chip" style="color:var(--accent);"><b id="gems">0</b>&nbsp;крист</span>
      </div>
      <button class="icon-button" id="refresh" aria-label="Обновить">
        <svg viewBox="0 0 24 24"><path d="M20 7v5h-5M4 17v-5h5"/><path d="M6.1 8A7 7 0 0 1 18 6l2 2M17.9 16A7 7 0 0 1 6 18l-2-2"/></svg>
      </button>
      <button class="icon-button" id="close" aria-label="Закрыть">
        <svg viewBox="0 0 24 24"><path d="m6 6 12 12M18 6 6 18"/></svg>
      </button>
    </header>
    <aside class="sidebar">
      <nav class="nav" id="nav"></nav>
      <section class="daily">
        <strong>Ежедневная награда</strong>
        <span id="dailyText">Данные загружаются</span>
        <button class="button primary" id="dailyClaim">Забрать</button>
      </section>
    </aside>
    <section class="content" id="content"><div class="empty">Получаем профиль с сервера…</div></section>
    <footer class="footer">
      <span class="footer-build" id="build">AquaLumen UI</span>
      <span><span class="key" id="openKey">F4</span> открыть</span>
      <span><span class="key">ESC</span> закрыть</span>
    </footer>
  </main>
</div>
<div class="modal-layer" id="modalLayer">
  <section class="modal" role="dialog" aria-modal="true">
    <h2 id="modalTitle">Подтверждение</h2>
    <p id="modalText"></p>
    <div class="modal-actions">
      <button class="button" id="modalCancel">Отмена</button>
      <button class="button primary" id="modalConfirm">Продолжить</button>
    </div>
  </section>
</div>
<div class="case-preview-layer" id="casePreviewLayer">
  <section class="case-preview-modal" role="dialog" aria-modal="true">
    <button class="icon-button close-preview" id="closeCasePreview"><svg viewBox="0 0 24 24"><path d="m6 6 12 12M18 6 6 18"/></svg></button>
    <div style="display:flex;align-items:center;gap:16px;margin-bottom:12px;text-align:left;">
      <img id="previewCaseImg" class="case-card-img" style="width:72px;height:72px;margin:0;flex:0 0 auto;" src="" alt="" />
      <div>
        <h2 id="previewCaseTitle" style="margin:0 0 4px;font-size:20px;">Кейс</h2>
        <p id="previewCaseSub" style="margin:0;color:var(--muted);font-size:11px;"></p>
      </div>
    </div>
    <div class="section-title" style="margin:0 0 6px;"><b>Содержимое кейса</b><span id="previewLootCount"></span></div>
    <div class="case-drop-grid" id="previewDropGrid"></div>
    <div style="margin-top:16px;display:flex;justify-content:flex-end;align-items:center;gap:10px;flex-wrap:wrap;">
      <button class="button" id="previewCloseBtn">Закрыть</button>
      <div id="previewActions" style="display:flex;gap:8px;flex-wrap:wrap;"></div>
    </div>
  </section>
</div>
<div class="case-layer" id="caseLayer">
  <section class="case-modal" role="dialog" aria-modal="true">
    <div class="case-orb" id="caseOrb" style="margin:0 auto;width:54px;height:54px;border-radius:16px;font-size:20px;display:grid;place-items:center;border:1px solid var(--line);background:radial-gradient(circle,color-mix(in srgb,var(--accent) 26%,transparent),transparent 72%)">
      <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M3 8h18v4H3zM3 12v8h18v-8M3 8l2-4h6l2 4"/><circle cx="12" cy="16" r="1.4"/></svg>
    </div>
    <h2 id="caseTitle">Кейс</h2>
    <p class="case-sub" id="caseSub"></p>
    <div class="reel" id="caseReel">
      <div class="reel-marker" id="caseMarker"></div>
      <div class="reel-strip" id="caseStrip"></div>
    </div>
    <div class="case-wait" id="caseReveal"><span>Крутим рулетку…</span></div>
    <div class="case-actions" id="caseActions"></div>
  </section>
</div>
<div class="toast" id="toast"></div>
<script>
(() => {
  "use strict";

  const ITEM_TEXTURES = __TEXTURES_JSON__;
  const CASE_ICONS = __CASE_ICONS_JSON__;

  const tabMeta = {
    profile:["Профиль",'<svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="3.6"/><path d="M5 20c1.4-3.6 4-5.2 7-5.2s5.6 1.6 7 5.2"/></svg>'],
    store:["Магазин",'<svg viewBox="0 0 24 24"><path d="M4 8h16l-1.2 12H5.2L4 8Z"/><path d="M8.5 8V6.2a3.5 3.5 0 0 1 7 0V8"/></svg>'],
    cases:["Кейсы",'<svg viewBox="0 0 24 24"><path d="M3 8h18v4H3zM3 12v8h18v-8M3 8l2-4h6l2 4"/><circle cx="12" cy="16" r="1.4"/></svg>'],
    pass:["Пропуск",'<svg viewBox="0 0 24 24"><path d="M12 3l2.7 5.6 6.1.8-4.5 4.2 1.1 6-5.4-3-5.4 3 1.1-6L3.2 9.4l6.1-.8L12 3Z"/></svg>'],
    fishing:["Рыбалка",'<svg viewBox="0 0 24 24"><path d="M3 12c3-4 6-4 9 0s6 4 9 0"/><circle cx="18" cy="8" r="1.6"/></svg>'],
    auction:["Аукцион",'<svg viewBox="0 0 24 24"><path d="M4 8h16l-1.2 12H5.2L4 8Z"/><path d="M8.5 8V6.2a3.5 3.5 0 0 1 7 0V8"/></svg>'],
    kits:["Киты",'<svg viewBox="0 0 24 24"><rect x="4" y="9" width="16" height="11" rx="2.5"/><path d="M9 9V6.5A2.5 2.5 0 0 1 11.5 4h1A2.5 2.5 0 0 1 15 6.5V9M4 13.5h16"/></svg>'],
    warps:["Варпы",'<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="m15.5 8.5-2 5-5 2 2-5 5-2Z"/></svg>'],
    tops:["Топ",'<svg viewBox="0 0 24 24"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>'],
    settings:["Настройки",'<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="3.2"/><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 0 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-1.8-.3 1.6 1.6 0 0 0-1 1.5V21a2 2 0 0 1-4 0v-.2a1.6 1.6 0 0 0-1-1.5 1.6 1.6 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.6 1.6 0 0 0 .3-1.8 1.6 1.6 0 0 0-1.5-1H3a2 2 0 0 1 0-4h.2a1.6 1.6 0 0 0 1.5-1 1.6 1.6 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.6 1.6 0 0 0 1.8.3 1.6 1.6 0 0 0 1-1.5V3a2 2 0 0 1 4 0v.2a1.6 1.6 0 0 0 1 1.5 1.6 1.6 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l.1.1a1.6 1.6 0 0 0-.3 1.8 1.6 1.6 0 0 0 1.5 1H21a2 2 0 0 1 0 4h-.2a1.6 1.6 0 0 0-1.4 1Z"/></svg>']
  };

  const state = {
    tab:"profile",
    payload:{
      snapshot:{
        profile:{name:"Player",rank:"Игрок",rankColor:0x8fa6b8,level:1,levelProgress:0,playtimeMinutes:0,kills:0,deaths:0,quests:0,friendsOnline:0},
        wallet:{coins:0,gems:0,dailyStreak:1,dailyAvailable:false},
        season:{title:"Сезон 1",tier:1,maxTier:10,tierProgress:0,premium:false,claimable:0,claimedTiers:[]},
        tops:[],store:[],cases:[],kits:[],warps:[],fishes:[],
        server:{name:"AquaTech Network",online:1,slots:100,tps:20.0,build:"AquaLumen UI"},
        caseResult:null
      },
      enabledTabs:["profile","store","cases","pass","fishing","auction","kits","warps","tops","settings"],
      appearance:{theme:"aqua_lumen",animations:true,compact:false,panelOpacity:0.94},
      openKey:"F4"
    }
  };

  function $(id){return document.getElementById(id)}
  function esc(s){return String(s||"").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;")}
  function compact(n){n=Number(n)||0;if(n>=1e6)return(n/1e6).toFixed(1)+"M";if(n>=1e3)return(n/1e3).toFixed(1)+"k";return String(n)}
  function num(n){return new Intl.NumberFormat("ru-RU").format(Number(n)||0)}
  function formatHours(m){m=Number(m)||0;const h=Math.floor(m/60),rem=m%60;return h>0?`${h} ч ${rem} м`:`${rem} мин`}
  function send(msg){if(window.AquaLumenBridge)window.AquaLumenBridge.send(msg)}
  function action(a,arg){send({type:"action",action:a,argument:arg||""})}

  function toast(msg){
    const t=$("toast");t.textContent=msg;t.classList.add("show");
    clearTimeout(toast.timer);toast.timer=setTimeout(()=>t.classList.remove("show"),2400);
  }

  function confirmAction(titleText,msgText,act,arg){
    $("modalTitle").textContent=titleText;
    $("modalText").textContent=msgText;
    $("modalConfirm").onclick=()=>{$("modalLayer").classList.remove("open");send({type:"modal",open:false});action(act,arg)};
    $("modalCancel").onclick=()=>{$("modalLayer").classList.remove("open");send({type:"modal",open:false})};
    $("modalLayer").classList.add("open");send({type:"modal",open:true});
  }

  function resolveItemIcon(label, itemId) {
    if (itemId && ITEM_TEXTURES[itemId]) return ITEM_TEXTURES[itemId];
    const cleanLabel = String(label || "").toLowerCase().trim();
    if (ITEM_TEXTURES[cleanLabel]) return ITEM_TEXTURES[cleanLabel];
    for (const [key, tex] of Object.entries(ITEM_TEXTURES)) {
      if (key.length > 2 && !key.endsWith('__side') && cleanLabel.includes(key.toLowerCase())) {
        return tex;
      }
    }
    return null;
  }

  function getItemIconHtml(label, itemId, extraClass, itemType) {
    const cls = extraClass || "mc-icon";
    const low = String(label || "").toLowerCase();
    const itype = itemType || '';

    /* coins / gems virtual rewards — no item ID */
    if (itype === 'coins' || (!itemId && (low.includes('coin') || low.includes('монет') || low.includes('aquacoin')))) {
      return `<svg viewBox="0 0 24 24" class="${cls}" style="color:var(--gold);filter:drop-shadow(0 0 8px #f5c25b)"><ellipse cx="12" cy="7" rx="7" ry="3" fill="currentColor"/><path d="M5 7v4c0 1.7 3.1 3 7 3s7-1.3 7-3V7" fill="currentColor" opacity=".8"/><path d="M5 11v4c0 1.7 3.1 3 7 3s7-1.3 7-3v-4" fill="currentColor" opacity=".6"/></svg>`;
    }
    if (itype === 'gems' || (!itemId && (low.includes('гем') || low.includes('крист')))) {
      return `<svg viewBox="0 0 24 24" class="${cls}" style="color:var(--accent);filter:drop-shadow(0 0 10px #2fe0c0)"><path d="M12 2.5 18 8l-6 13L6 8Z" fill="currentColor"/><path d="M6 8h12M12 2.5 9.5 8l2.5 13M12 2.5 14.5 8 12 21" stroke="#000" stroke-width=".8" opacity=".5"/></svg>`;
    }

    const tex = resolveItemIcon(label, itemId);
    if (tex) {
      return `<img src="${tex}" class="${cls}" alt="${esc(label)}" />`;
    }

    /* final fallback SVG box */
    return `<svg viewBox="0 0 24 24" class="${cls}" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 8.5 6 5h12l2 3.5v3H4Z"/><path d="M4 11.5V20h16v-8.5"/><path d="M10 5v3.5M14 5v3.5" opacity=".5"/><rect x="10.7" y="14" width="2.6" height="3" rx=".6" fill="currentColor"/></svg>`;
  }

  /* High Performance Audio Synthesizer */
  /* High Performance Audio Synthesizer */
  const Sfx = {
    ctx: null, lastTick: 0,
    ensure() {
      try {
        this.ctx = this.ctx || new (window.AudioContext || window.webkitAudioContext)();
        if (this.ctx && this.ctx.state === "suspended") this.ctx.resume();
      } catch (e) { this.ctx = null; }
    },
    tick(speedRatio) {
      if (!this.ctx) return;
      const now = performance.now();
      if (now - this.lastTick < 45) return;
      this.lastTick = now;
      try {
        const t = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = "sine";
        const f = 1200 + (speedRatio || 1.0) * 900;
        osc.frequency.setValueAtTime(f, t);
        osc.frequency.exponentialRampToValueAtTime(280, t + 0.02);
        gain.gain.setValueAtTime(0.035, t);
        gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.02);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(t);
        osc.stop(t + 0.022);
      } catch (e) {}
    },
    win(big) {
      if (!this.ctx) return;
      try {
        const t = this.ctx.currentTime;
        const notes = big ? [523.25, 659.25, 783.99, 1046.50, 1318.51] : [523.25, 659.25, 783.99, 1046.50];
        notes.forEach((f, i) => {
          const osc = this.ctx.createOscillator();
          const gain = this.ctx.createGain();
          osc.type = "triangle";
          osc.frequency.setValueAtTime(f, t + i * 0.08);
          gain.gain.setValueAtTime(0, t + i * 0.08);
          gain.gain.linearRampToValueAtTime(big ? 0.09 : 0.06, t + i * 0.08 + 0.02);
          gain.gain.exponentialRampToValueAtTime(0.0001, t + i * 0.08 + (big ? 0.6 : 0.4));
          osc.connect(gain);
          gain.connect(this.ctx.destination);
          osc.start(t + i * 0.08);
          osc.stop(t + i * 0.08 + 0.65);
        });
      } catch (e) {}
    }
  };

  /* Smooth GPU 60 FPS Roulette Spin Engine */
  const CaseSpin = {
    active: false,
    def: null,
    raf: 0,
    timeout: 0,
    W: 116,
    pos: 0,
    tiles: [],
    resultIndex: -1,
    landFrom: 0,
    landDist: 0,
    landT0: 0,
    landDur: 4100,
    lastIdx: -1,
    colors: { common: "#9db2c4", uncommon: "#4cd08a", rare: "#3b9dff", epic: "#b072ff", legendary: "#f5c25b", mythic: "#ff4f79", exotic: "#ffe066" },
    ru: { common: "Обычный", uncommon: "Необычный", rare: "Редкий", epic: "Эпический", legendary: "Легендарный", mythic: "Мифический", exotic: "Экзотический" },
    
    color(r) { return this.colors[r] || this.colors.common; },
    label(r) { return this.ru[r] || r; },
    clean(l) { return String(l || "").replace(/\\s*[×x]\\s*[\\d\\u2013-]+\\s*$/, ""); },
    
    tileHtml(l) {
      const col = this.color(l.rarity);
      const icon = getItemIconHtml(l.label, l.item, "mc-icon", l.type);
      return `<div class="reel-tile" style="border-color:${col}44;box-shadow:inset 0 0 16px ${col}14">
        ${icon}
        <b>${esc(this.clean(l.label))}</b>
      </div>`;
    },

    pick(loot) {
      let t = 0;
      for (const l of loot) t += Math.max(1, l.weight);
      let r = Math.random() * t;
      for (const l of loot) {
        r -= Math.max(1, l.weight);
        if (r <= 0) return l;
      }
      return loot[loot.length - 1];
    },

    randTile() {
      return this.pick(this.def.loot && this.def.loot.length ? this.def.loot : [{ label: "?", rarity: "common", weight: 1 }]);
    },

    open(def) {
      this.close(true);
      this.def = def;
      this.active = true;
      this.pos = 0;
      this.tiles = [];
      this.resultIndex = -1;
      this.lastIdx = -1;
      Sfx.ensure();

      const col = this.color(def.rarity);
      $("caseOrb").style.borderColor = col + "66";
      $("caseOrb").style.color = col;
      $("caseOrb").style.boxShadow = `inset 0 0 22px ${col}22, 0 0 26px ${col}1f`;
      $("caseTitle").textContent = def.title;
      $("caseSub").textContent = `Стоимость: ${num(def.cost)} монет · ${this.label(def.rarity)}`;
      $("caseReveal").className = "case-wait";
      $("caseReveal").innerHTML = "<span>Крутим рулетку…</span>";
      $("caseActions").innerHTML = "";
      $("caseLayer").classList.add("open");
      send({ type: "modal", open: true });

      /* Build reel strip with diversity — no same item twice in a row,
         max ~30% slots for any single item so the reel feels varied */
      const lootPool = this.def.loot && this.def.loot.length ? this.def.loot : [{ label: "?", rarity: "common", weight: 1 }];
      const maxPerItem = Math.ceil(55 * 0.28);
      const counts = {};
      let lastId = null;
      for (let i = 0; i < 55; i++) {
        let picked = null;
        for (let attempt = 0; attempt < 20; attempt++) {
          const candidate = this.pick(lootPool);
          const cid = candidate.item || candidate.label;
          const cnt = counts[cid] || 0;
          if (cid !== lastId && cnt < maxPerItem) {
            picked = candidate;
            break;
}
}
        if (!picked) picked = this.pick(lootPool);
        const pid = picked.item || picked.label;
        counts[pid] = (counts[pid] || 0) + 1;
        lastId = pid;
        this.tiles.push(picked);
      }
      $("caseStrip").innerHTML = this.tiles.map(l => this.tileHtml(l)).join("");
      $("caseStrip").style.transform = "translate3d(0, 0, 0)";

      this.timeout = setTimeout(() => this.fail(), 7000);
    },

    deliver(result) {
      if (!this.active) {
        const amt = (result.amount > 1) ? (' x' + result.amount) : '';
        toast('Кейс: ' + this.clean(result.label) + amt);
        return;
      }
      clearTimeout(this.timeout);

      const animations = state.payload.appearance ? state.payload.appearance.animations : true;
      if (!animations) {
        this.reveal(result);
        return;
      }

      const targetIdx = 38;
      this.tiles[targetIdx] = { label: result.label, rarity: result.rarity, weight: 1, item: result.item, type: result.type };
      this.resultIndex = targetIdx;
      $("caseStrip").innerHTML = this.tiles.map(l => this.tileHtml(l)).join("");

      const markerX = $("caseReel").clientWidth / 2;
      const jitter = (Math.random() * 0.3 - 0.15) * this.W;
      this.landFrom = 0;
      this.landDist = targetIdx * this.W + (this.W / 2) - markerX + jitter;
      this.landT0 = performance.now();
      this.landDur = 4100;

      const stripEl = $("caseStrip");

      const loop = (now) => {
        if (!this.active) return;
        const elapsed = now - this.landT0;
        const u = Math.min(1.0, elapsed / this.landDur);

        // Quintic Smooth Ease-Out for 60 FPS
        const eased = 1.0 - Math.pow(1.0 - u, 5);
        this.pos = this.landFrom + this.landDist * eased;

        stripEl.style.transform = `translate3d(${-this.pos.toFixed(2)}px, 0, 0)`;

        const idx = Math.floor((this.pos + markerX) / this.W);
        if (idx !== this.lastIdx) {
          this.lastIdx = idx;
          Sfx.tick(1.0 - u);
        }

        if (u >= 1.0) {
          cancelAnimationFrame(this.raf);
          this.raf = 0;
          this.reveal(result);
          return;
        }

        this.raf = requestAnimationFrame(loop);
      };

      this.raf = requestAnimationFrame(loop);
    },

    reveal(result) {
      if (!result && this.resultIndex >= 0) result = this.tiles[this.resultIndex];
      if (!result) { this.fail(); return; }

      action("case.claim", "");

      const col = this.color(result.rarity);
      const strip = $("caseStrip");
      if (this.resultIndex >= 0 && strip.children[this.resultIndex]) {
        const winTile = strip.children[this.resultIndex];
        winTile.classList.add("win");
        winTile.style.borderColor = col;
        winTile.style.boxShadow = `0 0 35px ${col}88, inset 0 0 20px ${col}33`;
      }

      const unit = result.type === "coins" ? "монет" : result.type === "gems" ? "гемов" : "шт.";
      const iconHtml = getItemIconHtml(result.label, result.item, "mc-icon-lg", result.type);

      const amountHtml = result.amount > 1 ? ('<span class="win-amount">x ' + num(result.amount) + ' ' + unit + '</span>') : '';
      const winLabel = esc(this.clean(result.label));
      const rarLabel = this.label(result.rarity);

      $("caseReveal").className = "case-reveal";
      $("caseReveal").innerHTML = '<span class="win-title">ВЫИГРЫШ</span>'
        + '<span style="display:flex;align-items:center;gap:12px">'
        + iconHtml
        + '<span class="win-item" style="color:' + col + '">' + winLabel + '</span>'
        + '</span>'
        + amountHtml
        + '<span class="win-rarity" style="color:' + col + ';border-color:' + col + '66;background:' + col + '18">' + rarLabel + '</span>';

      Sfx.win(result.rarity === "epic" || result.rarity === "legendary" || result.rarity === "mythic" || result.rarity === "exotic");

      const next = (state.payload.snapshot.cases || []).find(c => c.id === this.def.id);
      const canAgain = next && next.count > 0;
      const againBtnText = canAgain ? ('Открыть ещё · ' + num(next.cost)) : 'Не хватает монет';
      const disabledAttr = canAgain ? '' : 'disabled';

      $("caseActions").innerHTML = '<button class="button primary" id="caseAgain" ' + disabledAttr + '>' + againBtnText + '</button><button class="button" id="caseDone">Забрать</button>';
      $("caseAgain").onclick = () => {
        const c = (state.payload.snapshot.cases || []).find(x => x.id === this.def.id);
        if (c && c.count > 0) {
          this.open(c);
          action("case.open", c.id);
        }
      };
      $("caseDone").onclick = () => this.close();

      if (result.rarity === "epic" || result.rarity === "legendary" || result.rarity === "mythic" || result.rarity === "exotic") {
        this.confetti(col);
      }
    },

    confetti(col) {
      const palette = [col, "#2fe0c0", "#3b9dff", "#f5c25b", "#ff4f79", "#ffffff"];
      for (let i = 0; i < 30; i++) {
        const s = document.createElement("i");
        s.className = "confetti";
        s.style.background = palette[i % palette.length];
        s.style.setProperty("--cx", `${Math.random() * 460 - 230}px`);
        s.style.setProperty("--cy", `${-Math.random() * 280 - 50}px`);
        s.style.setProperty("--cr", `${Math.random() * 720 - 360}deg`);
        s.style.animationDelay = `${Math.random() * 0.2}s`;
        document.body.appendChild(s);
        setTimeout(() => s.remove(), 1700);
      }
    },

    fail() {
      if (this.active) toast("Кейс не открылся — попробуйте ещё раз");
      this.close();
    },

    close(keepLayer) {
      this.active = false;
      clearTimeout(this.timeout);
      cancelAnimationFrame(this.raf);
      this.raf = 0;
      if (keepLayer) return;
      $("caseLayer").classList.remove("open");
      send({ type: "modal", open: false });
    }
  };

  /* Case Preview Drop Table Overlay */
  const CasePreview = {
    open(c) {
      const col = CaseSpin.color(c.rarity);
      $("previewCaseImg").src = CASE_ICONS[c.id] || "";
      $("previewCaseTitle").textContent = c.title;
      $("previewCaseSub").innerHTML = '<span class="case-rarity" style="color:' + col + ';border-color:' + col + '66;background:' + col + '14;margin:0 6px 0 0;">' + CaseSpin.label(c.rarity) + '</span> Стоимость: <b style="color:var(--gold);">' + num(c.cost) + '</b> монет';
      $("previewLootCount").textContent = ((c.loot || []).length) + " предметов";

      let totalWeight = 0;
      (c.loot || []).forEach(l => totalWeight += Math.max(1, l.weight || 1));

      const gridHtml = (c.loot || []).map(l => {
        const lcol = CaseSpin.color(l.rarity);
        const icon = getItemIconHtml(l.label, l.item, "mc-icon", l.type);
        const chanceRaw = (Math.max(1, l.weight || 1) / totalWeight) * 100;
        const chance = chanceRaw < 1 ? chanceRaw.toFixed(2) : Math.round(chanceRaw);
        return '<div class="drop-item-card" style="border-color:' + lcol + '33;box-shadow:inset 0 0 16px ' + lcol + '10;">'
          + icon
          + '<b>' + esc(CaseSpin.clean(l.label)) + '</b>'
          + '<span class="drop-item-chance" style="color:' + lcol + ';border-color:' + lcol + '44;background:' + lcol + '14;">' + chance + '% · ' + CaseSpin.label(l.rarity) + '</span>'
          + '</div>';
      }).join("");

      $("previewDropGrid").innerHTML = gridHtml;

      const can1 = c.count >= 1;
      const can5 = c.count >= 5;
      const can10 = c.count >= 10;

      const btn1Text = can1 ? ('Крутить x1 · ' + num(c.cost) + ' ¤') : ('Нужно ' + num(c.cost) + ' ¤');
      const btn5Text = can5 ? ('Крутить x5 · ' + num(c.cost * 5) + ' ¤') : ('Нужно ' + num(c.cost * 5) + ' ¤ (x5)');
      const btn10Text = can10 ? ('Крутить x10 · ' + num(c.cost * 10) + ' ¤') : ('Нужно ' + num(c.cost * 10) + ' ¤ (x10)');

      $("previewActions").innerHTML =
        '<button class="button primary" id="open1Btn" ' + (can1 ? '' : 'disabled') + '>' + btn1Text + '</button>'
        + '<button class="button primary" id="open5Btn" ' + (can5 ? '' : 'disabled') + ' style="background:linear-gradient(135deg, rgba(59,157,255,0.22), rgba(47,224,192,0.22));border-color:rgba(59,157,255,0.5)">' + btn5Text + '</button>'
        + '<button class="button primary" id="open10Btn" ' + (can10 ? '' : 'disabled') + ' style="background:linear-gradient(135deg, rgba(176,114,255,0.22), rgba(245,194,91,0.22));border-color:rgba(176,114,255,0.5)">' + btn10Text + '</button>';

      $("open1Btn").onclick = () => {
        this.close();
        if (c.count >= 1) {
          CaseSpin.open(c);
          action("case.open", c.id);
        }
      };
      if ($("open5Btn")) $("open5Btn").onclick = () => {
        this.close();
        if (c.count >= 5) {
          CaseSpin.open(c);
          action("case.open", c.id + ":5");
        }
      };
      if ($("open10Btn")) $("open10Btn").onclick = () => {
        this.close();
        if (c.count >= 10) {
          CaseSpin.open(c);
          action("case.open", c.id + ":10");
        }
      };

      $("casePreviewLayer").classList.add("open");
      send({ type: "modal", open: true });
    },
    close() {
      $("casePreviewLayer").classList.remove("open");
      send({ type: "modal", open: false });
    }
  };

  $("closeCasePreview").onclick = () => CasePreview.close();
  $("previewCloseBtn").onclick = () => CasePreview.close();

  /* Views */
  function title(h1, p) {
    return `<div class="view-title"><div><h1>${esc(h1)}</h1><p>${esc(p)}</p></div></div>`;
  }

  function profileView(s) {
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
  }

  function storeView(s) {
    const cards = (s.store || []).map(o => `<article class="card offer">
      ${o.badge ? `<span class="offer-badge">${esc(o.badge)}</span>` : ""}
      <div class="offer-art">${getItemIconHtml(o.title, o.id, "mc-icon")}</div>
      <h3>${esc(o.title)}</h3>
      <p>${esc(o.subtitle)}</p>
      <div class="offer-foot">
        <span class="price">${num(o.price)} ${o.currency === "gems" ? "крист." : "монет"}</span>
        <button class="button ${o.owned ? "" : "primary"} buy" data-id="${esc(o.id)}" data-title="${esc(o.title)}" ${o.owned ? "disabled" : ""}>${o.owned ? "Куплено" : "Купить"}</button>
      </div>
    </article>`).join("");
    return `<div class="view">${title("Магазин Улучшений", "Привилегии, бустеры и наборы")}
      <div class="store-grid">${cards || '<div class="empty">Товаров нет</div>'}</div>
    </div>`;
  }

  function casesView(s) {
    const cards = (s.cases || []).map(c => {
      const col = CaseSpin.color(c.rarity);
      const iconUrl = CASE_ICONS[c.id] || "";
      return `<article class="card case" style="border-color:${col}33;cursor:pointer;" data-preview="${esc(c.id)}">
        <span class="case-rarity" style="color:${col};border-color:${col}55;background:${col}14;">${CaseSpin.label(c.rarity)}</span>
        <img src="${iconUrl}" class="case-card-img" alt="${esc(c.title)}" />
        <h3>${esc(c.title)}</h3>
        <div style="font-size:12px;color:var(--gold);font-weight:700;margin:4px 0 14px;">${num(c.cost)} монет</div>
        <div class="case-actions-row" onclick="event.stopPropagation()">
          <button class="button preview-case" data-id="${esc(c.id)}">Просмотр</button>
          <button class="button primary open-case" data-id="${esc(c.id)}" ${c.count > 0 ? "" : "disabled"}>${c.count > 0 ? "Рулетка" : "Мало монет"}</button>
        </div>
      </article>`;
    }).join("");

    return `<div class="view">${title("Кейсы Прогрессии", "10 уникальных кейсов со сбалансированным лутом")}
      <div class="case-grid">${cards || '<div class="empty">Кейсов нет</div>'}</div>
    </div>`;
  }

  function passView(s) {
    const season = s.season || { tier: 1, maxTier: 10 };
    const maxT = season.maxTier || 10;
    const claimedList = (season.claimedTiers || []).map(Number);
    const cards = [];
    for (let t = 1; t <= maxT; t++) {
      const unlocked = (season.tier || 1) >= t;
      const isClaimed = claimedList.includes(t);
      const isClaimable = unlocked && !isClaimed;
      const rewardCoins = 100 + t * 35;

      let btnText = "Закрыто";
      let btnClass = "";
      let disabledAttr = "disabled";

      if (isClaimed) {
        btnText = "Забрано";
        btnClass = "";
        disabledAttr = "disabled style='opacity:0.45;border-color:var(--line);color:var(--muted);cursor:default'";
      } else if (isClaimable) {
        btnText = "Забрать";
        btnClass = "primary claim-pass";
        disabledAttr = "";
      }

      cards.push(`<div class="reward ${isClaimable ? "claimable" : ""}">
        <span class="reward-level">Уровень ${t}</span>
        <div class="reward-icon" style="color:${isClaimable ? "var(--accent)" : isClaimed ? "var(--gold)" : "var(--muted)"}">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.6"><ellipse cx="12" cy="7" rx="7" ry="3"/><path d="M5 7v4c0 1.7 3.1 3 7 3s7-1.3 7-3V7"/><path d="M5 11v4c0 1.7 3.1 3 7 3s7-1.3 7-3v-4"/></svg>
        </div>
        <div style="font-size:11px;font-weight:700;color:${isClaimed ? "var(--muted)" : "var(--gold)"};margin-bottom:6px;">+${rewardCoins} ¤</div>
        <button class="button ${btnClass}" data-level="${t}" ${disabledAttr} style="width:100%;font-size:9.5px;">${btnText}</button>
      </div>`);
    }

    return `<div class="view">${title("Сезонный Пропуск", "Выполняйте квесты и забирайте награды")}
      <section class="card season">
        <div class="season-head">
          <div><h3>${esc(season.title)}</h3><p>Доступно к получению: <b>${season.claimable}</b> наград</p></div>
          <b class="tier">T${season.tier}</b>
        </div>
        <div class="progress-label"><span>Прогресс сезона</span><span>${Math.round((season.tierProgress || 0) * 100)}%</span></div>
        <div class="progress"><i style="width:${Math.round((season.tierProgress || 0) * 100)}%"></i></div>
      </section>
      <div class="section-title"><b>Награды уровней</b><span>Сезон 1</span></div>
      <div class="card pass-track" style="grid-template-columns:repeat(auto-fit,minmax(96px,1fr));">${cards.join("")}</div>
    </div>`;
  }

  function fishingView(s) {
    const fishes = s.fishes || [];
    let totalFish = 0, totalValue = 0;
    fishes.forEach(f => { totalFish += (f.count || 0); totalValue += (f.count || 0) * (f.priceCoins || 0); });

    const cards = fishes.map(f => `<article class="card offer" style="min-height:130px;padding:12px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
        <h3 style="font-size:12.5px;margin:0;font-weight:700;">${esc(f.name)}</h3>
        <span class="offer-badge" style="position:static;">${f.count} шт.</span>
      </div>
      <p style="font-size:10px;color:var(--muted);margin:0 0 10px;">Цена за шт: <b style="color:var(--gold);">${num(f.priceCoins)}</b> монет</p>
      <div class="offer-foot">
        <span class="price">${num(f.count * f.priceCoins)} монет</span>
        <button class="button sell-single-fish" data-fish="${esc(f.id)}" ${f.count > 0 ? "" : "disabled"}>Продать</button>
      </div>
    </article>`).join("");

    return `<div class="view">${title("Скупщик Рыбы", "Продавайте улов прямо из инвентаря")}
      <div class="grid two">
        <section class="card hero" style="min-height:140px;padding:16px;">
          <div style="display:flex;gap:14px;align-items:center;">
            <div class="avatar" style="width:64px;height:64px;font-size:24px;">🐟</div>
            <div>
              <h2 style="font-size:18px;margin:0 0 4px;">Скупка Рыбы</h2>
              <div class="rank">В инвентаре: <b>${totalFish}</b> шт. (${num(totalValue)} монет)</div>
            </div>
          </div>
          <button class="button primary sell-all-fish" ${totalFish <= 0 ? "disabled" : ""} style="height:36px;padding:0 18px;font-weight:750;margin-left:auto;">
            ${totalFish > 0 ? `Продать всё (+${num(totalValue)})` : "Инвентарь пуст"}
          </button>
        </section>
        <section class="stats">
          <div class="stat"><small>Видов рыбы</small><b>${fishes.length} шт.</b></div>
          <div class="stat"><small>Баланс</small><b>${compact(s.wallet.coins)}</b></div>
        </section>
      </div>
      <div class="section-title"><b>Таблица цен скупки</b><span>Нажмите для продажи партии</span></div>
      <div class="store-grid" style="grid-template-columns:repeat(auto-fit,minmax(180px,1fr));">${cards || '<div class="empty">У вас нет рыбы в инвентаре</div>'}</div>
    </div>`;
  }

  function auctionView(s) {
    const sampleLots = [
      { title: "Кристалл глубин (x4)", price: 120, seller: "Renfild", cat: "Ресурс" },
      { title: "Светящийся крючок MK-III", price: 350, seller: "AquaTech", cat: "Снаряжение" },
      { title: "Древнечешуйник (x2)", price: 480, seller: "FisherCat", cat: "Рыба" },
      { title: "Сплав орихалка (x8)", price: 640, seller: "Engineer", cat: "Материал" },
      { title: "Капсула сжатого кислорода", price: 90, seller: "Diver_1", cat: "Расходник" },
      { title: "Солнечный осётр (x5)", price: 250, seller: "StarFisher", cat: "Рыба" }
    ];
    const cards = sampleLots.map((lot, idx) => `<article class="card offer" style="min-height:125px;padding:14px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
        <span class="offer-badge" style="position:static;">${esc(lot.cat)}</span>
        <span style="font-size:10px;color:var(--muted);">Продавец: <b style="color:var(--accent);">${esc(lot.seller)}</b></span>
      </div>
      <h3 style="font-size:13.5px;margin:0 0 4px;font-weight:700;">${esc(lot.title)}</h3>
      <div class="offer-foot">
        <span class="price">${num(lot.price)} монет</span>
        <button class="button primary buy-auction" data-idx="${idx}">Купить</button>
      </div>
    </article>`).join("");

    return `<div class="view">${title("Аукцион и Рынок", "Покупайте и продавайте предметы между игроками")}
      <div class="grid two" style="margin-bottom:12px;">
        <section class="card hero" style="min-height:110px;padding:16px;">
          <div>
            <h2 style="font-size:18px;margin:0 0 4px;">Торговая Биржа</h2>
            <p style="font-size:11px;color:var(--muted);margin:0;">Держите предмет в руке и напишите в чат: <b style="color:var(--gold);">/ah sell &lt;цена&gt;</b></p>
          </div>
        </section>
        <section class="stats">
          <div class="stat"><small>Активных лотов</small><b>${sampleLots.length} шт.</b></div>
          <div class="stat"><small>Ваш баланс</small><b>${compact(s.wallet.coins)}</b></div>
        </section>
      </div>
      <div class="section-title"><b>Свежие предложения</b><span>Обновляется автоматически</span></div>
      <div class="store-grid" style="grid-template-columns:repeat(auto-fit,minmax(220px,1fr));">${cards}</div>
    </div>`;
  }

  function kitsView(s) {
    const kits = s.kits || [];
    const cards = kits.map(k => `<article class="card case" style="display:flex;flex-direction:column;justify-content:space-between;padding:14px;min-height:110px;">
      <div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <h3 style="font-size:13px;margin:0;font-weight:700;">${esc(k.title)}</h3>
          <span class="offer-badge" style="position:static;">${esc(k.badge)}</span>
        </div>
        <p style="font-size:11px;color:var(--muted);margin:0 0 10px;line-height:1.4;">${esc(k.description)}</p>
      </div>
      <button class="button primary claim-kit" data-kit="${esc(k.id)}" style="align-self:flex-start;">Забрать набор</button>
    </article>`).join("");

    return `<div class="view">${title("Наборы Снаряжения (Kits)", "Получайте экипировку и стартовые ресурсы")}
      <div class="case-grid" style="grid-template-columns:repeat(auto-fit,minmax(220px,1fr));">${cards || '<div class="empty">Наборов нет</div>'}</div>
    </div>`;
  }

  function warpsView(s) {
    const warps = s.warps || [];
    const cards = warps.map(w => `<article class="card offer" style="display:flex;flex-direction:column;justify-content:space-between;padding:16px;min-height:115px;">
      <div>
        <div style="font-size:10px;font-weight:800;color:var(--accent);letter-spacing:0.12em;margin-bottom:6px;">[${esc(w.tag)}]</div>
        <h3 style="font-size:14px;margin:0 0 4px;font-weight:700;">${esc(w.title)}</h3>
        <p style="font-size:11px;color:var(--muted);margin:0 0 12px;line-height:1.4;">${esc(w.description)}</p>
      </div>
      <button class="button tp-warp" data-warp="${esc(w.id)}" style="background:linear-gradient(90deg,var(--accent),var(--accent2));color:#08131a;font-weight:700;">Телепортироваться</button>
    </article>`).join("");

    return `<div class="view">${title("Навигация и Варпы", "Быстрое перемещение по ключевым точкам мира")}
      <div class="store-grid" style="grid-template-columns:repeat(auto-fit,minmax(220px,1fr));">${cards || '<div class="empty">Варпов нет</div>'}</div>
    </div>`;
  }

  function topsView(s) {
    const rows = (s.tops || []).map(e => `<div class="row ${e.self ? "self" : ""}">
      <b class="place">#${e.place}</b>
      <span>${esc(e.player)}</span>
      <span class="row-value">${esc(e.value)}</span>
    </div>`).join("");
    return `<div class="view">${title("Топ игроков", "Рейтинг по игровому времени и достижениям")}
      <section class="card rows">${rows || '<div class="empty">Рейтинг пуст</div>'}</section>
    </div>`;
  }

  function settingsView() {
    const a = state.payload.appearance;
    return `<div class="view settings">${title("Настройки", "Применяются только к AquaLumen UI")}
      <section class="card">
        <div class="setting">
          <div class="setting-info"><b>Цветовая тема</b><span>Меняет палитру и акцент интерфейса</span></div>
          <div class="theme-picker">
            ${["aqua_lumen", "violet_lumen", "midnight_rose"].map(t => `<button aria-label="${t}" class="swatch ${a.theme === t ? "active" : ""}" data-theme="${t}"></button>`).join("")}
          </div>
        </div>
        <div class="setting">
          <div class="setting-info"><b>Анимации</b><span>Плавные переходы и рулетка</span></div>
          <button class="toggle ${a.animations ? "on" : ""}" id="motionToggle"><i></i></button>
        </div>
      </section>
    </div>`;
  }

  function bindViewActions() {
    document.querySelectorAll(".buy").forEach(b => b.onclick = () => confirmAction("Покупка", `Купить «${b.dataset.title}»?`, "store.buy", b.dataset.id));
    
    document.querySelectorAll(".preview-case").forEach(b => b.onclick = (e) => {
      e.stopPropagation();
      const def = (state.payload.snapshot.cases || []).find(c => c.id === b.dataset.id);
      if (def) CasePreview.open(def);
    });
    
    document.querySelectorAll(".card.case").forEach(card => card.onclick = () => {
      const cid = card.dataset.preview;
      const def = (state.payload.snapshot.cases || []).find(c => c.id === cid);
      if (def) CasePreview.open(def);
    });

    document.querySelectorAll(".open-case").forEach(b => b.onclick = (e) => {
      e.stopPropagation();
      const def = (state.payload.snapshot.cases || []).find(c => c.id === b.dataset.id);
      if (!def || def.count <= 0) return;
      CaseSpin.open(def);
      action("case.open", def.id);
    });

    document.querySelectorAll(".claim-pass").forEach(b => b.onclick = () => {
      const lvl = Number(b.dataset.level);
      if (!state.payload.snapshot.season.claimedTiers) state.payload.snapshot.season.claimedTiers = [];
      if (!state.payload.snapshot.season.claimedTiers.map(Number).includes(lvl)) {
        state.payload.snapshot.season.claimedTiers.push(lvl);
      }
      if (state.payload.snapshot.season.claimable > 0) {
        state.payload.snapshot.season.claimable--;
      }
      b.textContent = "Забрано";
      b.disabled = true;
      b.className = "button";
      b.style.opacity = "0.45";
      b.style.borderColor = "var(--line)";
      b.style.color = "var(--muted)";
      b.style.cursor = "default";
      const card = b.closest(".reward");
      if (card) card.classList.remove("claimable");
      renderNav();
      action("pass.claim", String(lvl));
      toast("Награда уровня " + lvl + " получена!");
      setTimeout(() => action("hub.refresh"), 400);
    });

    document.querySelectorAll(".claim-kit").forEach(b => b.onclick = () => {
      action("hub.kit", b.dataset.kit);
      toast("Набор запрошен!");
      setTimeout(() => action("hub.refresh"), 500);
    });

    document.querySelectorAll(".tp-warp").forEach(b => b.onclick = () => {
      action("hub.warp", b.dataset.warp);
      toast("Телепортация...");
      setTimeout(() => send({ type: "action", action: "hub.close" }), 300);
    });

    document.querySelectorAll(".sell-all-fish").forEach(b => b.onclick = () => {
      action("fish.sell_all");
      toast("Продажа рыбы...");
      setTimeout(() => action("hub.refresh"), 400);
    });

    document.querySelectorAll(".sell-single-fish").forEach(b => b.onclick = () => {
      action("fish.sell", b.dataset.fish);
      toast("Продажа рыбы...");
      setTimeout(() => action("hub.refresh"), 400);
    });

    document.querySelectorAll(".buy-auction").forEach(b => b.onclick = () => {
      toast("Запрос на покупку лота отправлен!");
      action("auction.buy", b.dataset.idx);
    });

    document.querySelectorAll(".swatch").forEach(b => b.onclick = () => {
      send({ type: "settings", theme: b.dataset.theme });
      state.payload.appearance.theme = b.dataset.theme;
      applyAppearance();
      renderView(false, true);
    });

    const toggle = $("motionToggle");
    if (toggle) toggle.onclick = () => {
      const enabled = !state.payload.appearance.animations;
      send({ type: "settings", animations: enabled });
      state.payload.appearance.animations = enabled;
      applyAppearance();
      renderView(false, true);
    };
  }

  function renderView(animate, preserveScroll = true) {
    const s = state.payload.snapshot;
    const views = {
      profile: profileView, store: storeView, cases: casesView,
      pass: passView, fishing: fishingView, auction: auctionView, kits: kitsView,
      warps: warpsView, tops: topsView, settings: settingsView
    };
    const currentView = $("content")?.querySelector(".view");
    const savedScroll = (preserveScroll && currentView) ? currentView.scrollTop : 0;

    $("content").innerHTML = (views[state.tab] || profileView)(s);
    
    const newView = $("content")?.querySelector(".view");
    if (newView && savedScroll > 0) {
      newView.scrollTop = savedScroll;
    }
    if (animate) $("content").firstElementChild?.classList.add("view-enter");
    bindViewActions();
  }

  function renderNav() {
    const tabs = state.payload.enabledTabs || [];
    $("nav").innerHTML = tabs.map(k => {
      const meta = tabMeta[k] || [k, '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"/></svg>'];
      const active = state.tab === k ? "active" : "";
      let b = "";
      if (k === "pass" && state.payload.snapshot.season.claimable > 0) {
        b = `<span class="badge">${state.payload.snapshot.season.claimable}</span>`;
      }
      return `<button class="nav-button ${active}" data-tab="${k}">${meta[1]}<span class="nav-label">${meta[0]}</span>${b}</button>`;
    }).join("");

    document.querySelectorAll(".nav-button").forEach(b => {
      b.onclick = () => {
        if (state.tab === b.dataset.tab) return;
        state.tab = b.dataset.tab;
        renderNav();
        renderView(true, false);
      };
    });
  }

  function applyAppearance() {
    const a = state.payload.appearance || {};
    const root = document.documentElement;
    root.classList.toggle("reduce-motion", a.animations === false);
    if (a.theme === "violet_lumen") {
      root.style.setProperty("--accent", "#b072ff");
      root.style.setProperty("--accent2", "#ff6bc1");
    } else if (a.theme === "midnight_rose") {
      root.style.setProperty("--accent", "#ff7a9c");
      root.style.setProperty("--accent2", "#ffb27a");
    } else {
      root.style.setProperty("--accent", "#2fe0c0");
      root.style.setProperty("--accent2", "#3b9dff");
    }
  }

  let lastSnapshotJson = "";
  function applyPayload(payload) {
    if (!payload) return;
    state.payload = payload;
    const s = payload.snapshot || {};

    $("serverName").textContent = s.server.name || "AquaTech Network";
    $("online").textContent = `${s.server.online || 0}/${s.server.slots || 100}`;
    $("tps").textContent = `${(s.server.tps || 20).toFixed(1)} TPS`;
    $("coins").textContent = num(s.wallet.coins);
    $("gems").textContent = num(s.wallet.gems);
    $("build").textContent = s.server.build || "AquaLumen UI";
    $("openKey").textContent = payload.openKey || "F4";

    const d = s.wallet;
    $("dailyText").textContent = d.dailyAvailable ? `Серия: ${d.dailyStreak} дн.` : "Награда уже забрана";
    $("dailyClaim").disabled = !d.dailyAvailable;

    applyAppearance();
    
    const currentJson = JSON.stringify(s);
    if (currentJson !== lastSnapshotJson) {
      lastSnapshotJson = currentJson;
      renderNav();
      renderView(false, true);
    }

    if (s.caseResult) {
      CaseSpin.deliver(s.caseResult);
    }
  }

  $("dailyClaim").onclick = () => { action("hub.claim_daily"); toast("Запрос награды…"); };
  $("refresh").onclick = () => { action("hub.refresh"); toast("Обновление…"); };
  $("close").onclick = () => send({ type: "action", action: "hub.close" });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      if ($("casePreviewLayer").classList.contains("open")) {
        CasePreview.close();
        return;
      }
      if ($("caseLayer").classList.contains("open")) {
        CaseSpin.close();
        return;
      }
      if ($("modalLayer").classList.contains("open")) {
        $("modalLayer").classList.remove("open");
        send({ type: "modal", open: false });
        return;
      }
      send({ type: "action", action: "hub.close" });
    }
  });

  window.AquaLumen = {
    applySnapshot(snapshot) {
      applyPayload(snapshot);
    },
    update(jsonOrObj) {
      try {
        const payload = typeof jsonOrObj === "string" ? JSON.parse(jsonOrObj) : jsonOrObj;
        applyPayload(payload);
      } catch (e) {}
    },
    deliverCase(result) {
      CaseSpin.deliver(result);
    },
    closeModal() {
      if ($("casePreviewLayer") && $("casePreviewLayer").classList.contains("open")) {
        CasePreview.close();
        return;
      }
      if ($("caseLayer") && $("caseLayer").classList.contains("open")) {
        CaseSpin.close();
        return;
      }
      if ($("modalLayer") && $("modalLayer").classList.contains("open")) {
        $("modalLayer").classList.remove("open");
        send({ type: "modal", open: false });
      }
    }
  };
  window.AquaLumenUI = window.AquaLumen;

  if (window.AquaLumenBridge) {
    window.AquaLumenBridge.send({ type: "ready" });
  }
})();
</script>
</body>
</html>'''

hub_html_content = hub_html_raw.replace("__TEXTURES_JSON__", textures_json).replace("__CASE_ICONS_JSON__", case_icons_json)

# Output paths
dest_paths = [
    'mods/aqualumen-ui/src/main/resources/assets/aqualumen/hub.html',
    'mods/aqualumen-ui/src/main/resources/assets/aqualumen/html/hub.html',
    'mods/aqualumen-ui/src/main/resources/assets/aqualumen/ui/hub.html',
    'mods/aquatech-ui/src/main/resources/assets/aquatech_ui/hub.html',
    'mods/aquatech-ui/src/main/resources/assets/aquatech_ui/html/hub.html',
    'server/config/aqualumen/html/hub.html',
    'server/config/aqualumen/hub.html',
    'config/aqualumen/html/hub.html',
    'config/aqualumen/hub.html'
]

for p in dest_paths:
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(hub_html_content)
    print(f'Wrote {len(hub_html_content)} bytes to {p}')
