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
    .aqua-coin-icon{width:14px;height:14px;object-fit:contain;vertical-align:-2px;flex-shrink:0;filter:drop-shadow(0 0 5px rgba(47,224,192,.55))}
    .chip .aqua-coin-icon{width:16px;height:16px}
    .coins-amt{display:inline-flex;align-items:center;gap:3px;white-space:nowrap;vertical-align:middle}
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
    .daily{display:none!important}
    .content{grid-row:2;overflow:hidden;position:relative;padding:20px 22px}
    .view{height:100%;overflow-y:auto;padding-right:5px;padding-bottom:80px;box-sizing:border-box;scroll-behavior:smooth}.view.view-enter{animation:view-in .22s var(--ease) both}
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
    .store-grid,.case-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:14px;padding-bottom:80px}
    .offer{min-height:176px;display:flex;flex-direction:column;position:relative;padding:18px 16px 16px;border:1px solid rgba(255,255,255,.08);border-radius:18px;background:rgba(255,255,255,.03)}
    .offer:hover{border-color:rgba(255,255,255,.14);background:rgba(255,255,255,.045)}
    .offer.is-owned{border-color:rgba(76,208,138,.28)}
    .offer-badge{position:absolute;right:14px;top:14px;padding:4px 9px;border-radius:999px;background:rgba(255,255,255,.08);color:var(--muted);font-size:11px;font-weight:600;letter-spacing:0;border:0}
    .offer-art,.rank-glyph{width:40px;height:40px;display:grid;place-items:center;margin:0 0 14px;border-radius:11px;background:rgba(255,255,255,.06);color:var(--accent)}
    .offer-art svg,.rank-glyph svg{width:22px;height:22px}
    .offer h3{margin:0;font-size:17px;font-weight:600;letter-spacing:-.02em}
    .offer p{margin:6px 0 14px;color:var(--muted);font-size:12px;line-height:1.45}
    .offer-foot{margin-top:auto;display:flex;align-items:center;justify-content:space-between;gap:8px}
    .price{font-size:13px;font-weight:600;color:var(--text);font-variant-numeric:tabular-nums}
    .button{min-height:32px;padding:0 14px;border:1px solid rgba(255,255,255,.12);border-radius:980px;background:rgba(255,255,255,.06);color:var(--text);font-size:12px;font-weight:600;cursor:pointer;transition:.18s var(--ease)}
    .button:hover{background:rgba(255,255,255,.1);transform:none}
    .button:disabled{opacity:.4;cursor:default;transform:none}
    .button.primary{border:0;background:var(--accent);color:#06211c;font-weight:650}
    .rank-card{text-align:left;--rc:var(--line)}
    .rank-card:hover{border-color:var(--rc)}
    .rank-card .rank-glyph{width:46px;height:46px;border-radius:13px}
    .rank-card .rank-glyph svg{width:24px;height:24px}
    
    /* Premium Case Cards */
    .case{min-height:245px;text-align:center;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:16px 14px;position:relative;transition:transform .22s var(--ease), border-color .22s}
    .case:hover{transform:translateY(-3px);box-shadow:0 12px 30px rgba(0,0,0,.4)}
    .case-card-img{width:96px;height:96px;object-fit:contain;margin:4px 0 10px;filter:drop-shadow(0 8px 18px rgba(0,0,0,.65));transition:transform .26s var(--ease)}
    .case:hover .case-card-img{transform:scale(1.08) translateY(-2px)}
    .case-art{position:relative;display:flex;align-items:center;justify-content:center;margin:4px 0 10px}
    .case-art .case-card-img{margin:0}
    .case-owned{position:absolute;right:-6px;top:-6px;min-width:28px;height:20px;padding:0 7px;border-radius:999px;background:rgba(52,199,89,.2);border:1px solid rgba(52,199,89,.4);color:#34c759;font-size:11px;font-weight:650;letter-spacing:0;display:flex;align-items:center;justify-content:center;font-variant-numeric:tabular-nums}
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
    .rank-modal-head .rank-glyph{width:48px;height:48px;margin:0}
    .rank-modal-head .rank-glyph svg{width:24px;height:24px}
    .rank-name{margin:0;font-size:15px;font-weight:750;letter-spacing:.01em}
    .rank-tag{font-size:10px;font-weight:700;letter-spacing:.16em;margin:4px 0 5px}
    .rank-sub{margin:0 0 10px;color:var(--muted);font-size:11px}
    .rank-modal{width:min(440px,92vw);position:relative}
    .rank-modal h2{margin:0;font-size:19px}
    .rank-modal .rank-modal-close{position:absolute;top:12px;right:12px}
    .rank-modal-head{display:flex;align-items:center;gap:14px;margin-bottom:6px;text-align:left}
    .rank-modal-head .rank-glyph{margin:0;height:auto}
    .rank-modal-sub{margin:0 0 10px;color:var(--muted);font-size:11px}
    .rank-modal-tag{font-size:10px;font-weight:700;letter-spacing:.16em;margin-top:4px}
    .rank-perks{max-height:240px;overflow:auto;margin-bottom:14px}
    .rank-perk{display:flex;align-items:center;gap:10px;padding:8px 2px;font-size:12px;border-bottom:1px solid rgba(255,255,255,.05)}
    .rank-perk:last-child{border-bottom:none}
    .rank-perk svg{width:15px;height:15px;flex:0 0 auto}
    .view-enter .stagger>*{animation:card-in .28s var(--ease) both;animation-delay:calc(var(--i,0)*30ms)}
    @keyframes card-in{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:none}}
    .brand-mark{background:linear-gradient(135deg,var(--prank,var(--accent)),var(--accent2))}
    .topbar{border-bottom-color:var(--prank-soft,var(--line))}
    .owned-check{color:var(--success);display:inline-grid;place-items:center;vertical-align:-2px;margin-left:6px}
    .owned-check svg{width:13px;height:13px}
    
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

    .case-reveal{min-height:108px;display:grid;place-items:center;align-content:center;gap:7px;animation:reveal-in .32s ease-out}
    .case-reveal .win-title{font-size:11px;letter-spacing:.08em;color:var(--muted);font-weight:700}
    .case-reveal .win-item{font-size:22px;font-weight:800}
    .case-reveal .win-rarity{padding:3px 12px;border:1px solid;border-radius:999px;font-size:10px;letter-spacing:.14em;font-weight:700}
    .case-reveal .win-amount{color:var(--gold);font-size:13px;font-weight:700}
    .case-wait{min-height:108px;display:grid;place-items:center;color:var(--muted);font-size:12px;text-align:center;padding:0 16px}
    .case-actions{display:flex;justify-content:center;gap:10px;margin-top:6px;min-height:36px}
    .case-actions .button{min-width:132px}
    @keyframes reveal-in{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
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
        <span class="chip" style="color:var(--gold);"><img class="aqua-coin-icon" src="__COIN_SRC__" alt=""><b id="coins">0</b></span>
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
<div class="modal-layer" id="rankModalLayer">
  <section class="modal rank-modal" role="dialog" aria-modal="true">
    <button class="icon-button rank-modal-close" id="rankModalClose" aria-label="Закрыть"><svg viewBox="0 0 24 24"><path d="m6 6 12 12M18 6 6 18"/></svg></button>
    <div class="rank-modal-head">
      <div class="rank-glyph" id="rankCrest"></div>
      <div>
        <h2 id="rankModalTitle">Привилегия</h2>
        <div class="rank-modal-tag" id="rankModalTag"></div>
      </div>
    </div>
    <p class="rank-modal-sub" id="rankModalSub"></p>
    <div class="rank-perks" id="rankPerks"></div>
    <div class="modal-actions">
      <button class="button" id="rankModalCancel">Закрыть</button>
      <button class="button primary" id="rankModalBuy">Купить</button>
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
try {
(() => {
  "use strict";

  const ITEM_TEXTURES = __TEXTURES_JSON__;
  const CASE_ICONS = __CASE_ICONS_JSON__;
  const COIN_SRC = "__COIN_SRC__";
  function coinIco(){return '<img class="aqua-coin-icon" src="'+COIN_SRC+'" alt="">'}
  function coins(n){return '<span class="coins-amt">'+num(n)+coinIco()+'</span>'}

  const tabMeta = {
    profile:["Профиль",'<svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="3.6"/><path d="M5 20c1.4-3.6 4-5.2 7-5.2s5.6 1.6 7 5.2"/></svg>'],
    store:["Магазин",'<svg viewBox="0 0 24 24"><path d="M4 8h16l-1.2 12H5.2L4 8Z"/><path d="M8.5 8V6.2a3.5 3.5 0 0 1 7 0V8"/></svg>'],
    cases:["Кейсы",'<svg viewBox="0 0 24 24"><path d="M3 8h18v4H3zM3 12v8h18v-8M3 8l2-4h6l2 4"/><circle cx="12" cy="16" r="1.4"/></svg>'],
    pass:["Пропуск",'<svg viewBox="0 0 24 24"><path d="M12 3l2.7 5.6 6.1.8-4.5 4.2 1.1 6-5.4-3-5.4 3 1.1-6L3.2 9.4l6.1-.8L12 3Z"/></svg>'],
    fishing:["Рыбалка",'<svg viewBox="0 0 24 24"><path d="M3 12c3-4 6-4 9 0s6 4 9 0"/><circle cx="18" cy="8" r="1.6"/></svg>'],
    events:["События",'<svg viewBox="0 0 24 24"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>'],
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
        tops:[],store:[],cases:[],kits:[],warps:[],fishes:[],quests:[],eventLine:"",
        server:{name:"AquaTech Network",online:1,slots:100,tps:20.0,build:"AquaLumen UI"},
        caseResult:null
      },
      enabledTabs:["profile","store","cases","pass","fishing","events","auction","kits","warps","tops","settings"],
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
    $("modalText").innerHTML=msgText;
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
      return `<img src="${COIN_SRC}" class="${cls} aqua-coin-icon" style="width:22px;height:22px" alt="" />`;
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
  const Sfx = {
    ctx: null, master: null, lastTick: 0,
    ensure() {
      try {
        if (!this.ctx) {
          const AC = window.AudioContext || window.webkitAudioContext;
          if (AC) {
            this.ctx = new AC();
            // Dynamics compressor + master limiter to completely eliminate audio clipping / bass boost
            const comp = this.ctx.createDynamicsCompressor();
            comp.threshold.setValueAtTime(-14, this.ctx.currentTime);
            comp.knee.setValueAtTime(30, this.ctx.currentTime);
            comp.ratio.setValueAtTime(12, this.ctx.currentTime);
            comp.attack.setValueAtTime(0.003, this.ctx.currentTime);
            comp.release.setValueAtTime(0.25, this.ctx.currentTime);

            const gain = this.ctx.createGain();
            gain.gain.setValueAtTime(0.35, this.ctx.currentTime);

            comp.connect(gain);
            gain.connect(this.ctx.destination);
            this.master = comp;
          }
        }
        if (this.ctx && this.ctx.state === "suspended") this.ctx.resume();
      } catch (e) { this.ctx = null; this.master = null; }
    },
    tick(speedRatio) {
      if (!this.ctx || !this.master) return;
      const now = performance.now();
      if (now - this.lastTick < 50) return;
      this.lastTick = now;
      try {
        const t = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = "sine";
        const f = 1100 + (speedRatio || 1.0) * 700;
        osc.frequency.setValueAtTime(f, t);
        osc.frequency.exponentialRampToValueAtTime(350, t + 0.02);
        gain.gain.setValueAtTime(0.03, t);
        gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.02);
        osc.connect(gain);
        gain.connect(this.master);
        osc.start(t);
        osc.stop(t + 0.022);
      } catch (e) {}
    },
    win(big) {
      this.ensure();
      if (!this.ctx || !this.master) return;
      try {
        const t = this.ctx.currentTime;
        const notes = big ? [523.25, 659.25, 783.99, 1046.50] : [392.00, 523.25, 659.25, 783.99];
        notes.forEach((f, i) => {
          const start = t + i * 0.11;
          const osc = this.ctx.createOscillator();
          const gain = this.ctx.createGain();
          osc.type = i % 2 ? "triangle" : "sine";
          osc.frequency.setValueAtTime(f, start);
          gain.gain.setValueAtTime(0.0001, start);
          gain.gain.linearRampToValueAtTime(big ? 0.08 : 0.06, start + 0.03);
          gain.gain.exponentialRampToValueAtTime(0.0001, start + 0.42);
          osc.connect(gain);
          gain.connect(this.master);
          osc.start(start);
          osc.stop(start + 0.45);
        });
      } catch (e) {}
    }
  };

  /* Smooth GPU 60 FPS Roulette Spin Engine */
  const CaseSpin = {
    active: false,
    delivered: false,
    revealed: false,
    def: null,
    raf: 0,
    timeout: 0,
    pending: null,
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
      this.delivered = false;
      this.revealed = false;
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
      $("caseSub").innerHTML = 'Стоимость: ' + coins(def.cost) + ' · ' + this.label(def.rarity);
      $("caseReveal").className = "case-wait";
      $("caseReveal").innerHTML = "<span>Крутим рулетку…</span>";
      this.bindActions("spin");
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

      this.timeout = setTimeout(() => this.fail(), 12000);
      if (this.pending && (!this.pending.caseId || this.pending.caseId === this.def.id)) {
        const queued = this.pending;
        this.pending = null;
        requestAnimationFrame(() => this.deliver(queued));
      }
    },

    bindActions(mode) {
      const closeBtn = '<button class="button" id="caseDone">Закрыть</button>';
      if (mode !== "done") {
        $("caseActions").innerHTML = closeBtn;
        $("caseDone").onclick = () => this.close();
        return;
      }
      const snap = (state.payload && state.payload.snapshot) || {};
      const next = ((snap.cases || []).find(c => c.id === this.def.id)) || this.def;
      const canAgain = caseBudget(next).can(1);
      const again = canAgain
        ? '<button class="button primary" id="caseAgain">Открыть ещё</button>'
        : '<button class="button primary" id="caseAgain" disabled>Открыть ещё</button>';
      $("caseActions").innerHTML = again + closeBtn;
      $("caseAgain").onclick = () => {
        const snapNow = (state.payload && state.payload.snapshot) || {};
        const c = (snapNow.cases || []).find(x => x.id === this.def.id) || this.def;
        if (!c || !caseBudget(c).can(1)) return;
        this.open(c);
        action("case.open", c.id);
      };
      $("caseDone").onclick = () => this.close();
    },

    deliver(result) {
      if (!result) return;
      const mine = this.def && (!result.caseId || result.caseId === this.def.id);
      if (!this.active || !mine) {
        this.pending = result;
        return;
      }
      if (this.revealed) {
        if ($("caseReveal").classList.contains("case-reveal")) return;
        this.revealed = false;
        this.delivered = true;
        clearTimeout(this.timeout);
        this.reveal(result);
        return;
      }
      if (this.delivered) return;
      this.delivered = true;
      clearTimeout(this.timeout);

      const animations = !(state.payload && state.payload.appearance && state.payload.appearance.animations === false);
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

      if (this.raf) cancelAnimationFrame(this.raf);
      this.raf = requestAnimationFrame(loop);
    },

    reveal(result) {
      if (this.revealed) return;
      this.revealed = true;
      if (this.raf) {
        cancelAnimationFrame(this.raf);
        this.raf = 0;
      }
      if (!result && this.resultIndex >= 0) result = this.tiles[this.resultIndex];
      if (!result) { this.fail(); return; }

      action("case.claim", "");
      const col = this.color(result.rarity);
      try {
        const strip = $("caseStrip");
        if (this.resultIndex >= 0 && strip.children[this.resultIndex]) {
          const winTile = strip.children[this.resultIndex];
          winTile.classList.add("win");
          winTile.style.borderColor = col;
          winTile.style.boxShadow = `0 0 35px ${col}88, inset 0 0 20px ${col}33`;
        }

        const iconHtml = getItemIconHtml(result.label, result.item, "mc-icon-lg", result.type);
        const amountHtml = result.amount > 1
          ? ('<span class="win-amount">× ' + (result.type === "coins" ? coins(result.amount) : (result.type === "gems" ? (num(result.amount) + ' гемов') : (num(result.amount) + ' шт.'))) + '</span>')
          : '';
        const winLabel = esc(this.clean(result.label));
        const rarLabel = this.label(result.rarity);

        $("caseReveal").className = "case-reveal";
        $("caseReveal").innerHTML = '<span class="win-title">Вам выпало</span>'
          + '<span style="display:flex;align-items:center;gap:12px">'
          + iconHtml
          + '<span class="win-item" style="color:' + col + '">' + winLabel + '</span>'
          + '</span>'
          + amountHtml
          + '<span class="win-rarity" style="color:' + col + ';border-color:' + col + '66;background:' + col + '18">' + rarLabel + '</span>';
        $("caseSub").textContent = "Награда уже в инвентаре";
      } catch (e) {
        $("caseReveal").className = "case-reveal";
        $("caseReveal").innerHTML = '<span class="win-title">Вам выпало</span><span class="win-item">' + esc(this.clean((result && result.label) || "предмет")) + '</span>';
      }

      try {
        Sfx.win(result.rarity === "epic" || result.rarity === "legendary" || result.rarity === "mythic" || result.rarity === "exotic");
      } catch (e) {}

      this.bindActions("done");

      if (result.rarity === "epic" || result.rarity === "legendary" || result.rarity === "mythic" || result.rarity === "exotic") {
        this.confetti(col);
      }
    },

    confetti(col) {
      const palette = [col, "#2fe0c0", "#3b9dff", "#f5c25b", "#ff4f79", "#ffffff"];
      for (let i = 0; i < 25; i++) {
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
      if (this.revealed) return;
      this.revealed = true;
      if (this.raf) {
        cancelAnimationFrame(this.raf);
        this.raf = 0;
      }
      clearTimeout(this.timeout);
      $("caseReveal").className = "case-wait";
      $("caseReveal").innerHTML = "<span>Не удалось открыть. Можно закрыть и попробовать ещё раз.</span>";
      this.bindActions("done");
      toast("Кейс не открылся — попробуйте ещё раз");
    },

    close(keepLayer) {
      this.active = false;
      this.delivered = false;
      this.revealed = false;
      clearTimeout(this.timeout);
      if (this.raf) {
        cancelAnimationFrame(this.raf);
        this.raf = 0;
      }
      if (keepLayer) return;
      $("caseLayer").classList.remove("open");
      send({ type: "modal", open: false });
    }
  };


  function caseBudget(c) {
    const keys = Number(c && c.count) || 0;
    const coins = Number((state.payload && state.payload.snapshot && state.payload.snapshot.wallet || {}).coins) || 0;
    const cost = Number(c && c.cost) || 0;
    return {
      keys, coins, cost,
      can(n) { return keys >= n || (cost > 0 && coins >= cost * n) || (cost <= 0 && n <= 1); }
    };
  }

  /* Case Preview Drop Table Overlay */
  const CasePreview = {
    open(c) {
      const col = CaseSpin.color(c.rarity);
      $("previewCaseImg").src = CASE_ICONS[c.id] || "";
      $("previewCaseTitle").textContent = c.title;
      const owned = Number(c.count) || 0;
      $("previewCaseSub").innerHTML = '<span class="case-rarity" style="color:' + col + ';border-color:' + col + '66;background:' + col + '14;margin:0 6px 0 0;">' + CaseSpin.label(c.rarity) + '</span> Стоимость: <b>' + coins(c.cost) + '</b>' + (owned > 0 ? (' · у вас ×' + owned) : '');
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

      const b = caseBudget(c);
      const can1 = b.can(1);
      const can5 = b.can(5);
      const can10 = b.can(10);
      const labelFor = (n) => b.keys >= n ? ('Открыть ×' + n) : ('Купить ×' + n + ' · ' + coins(b.cost * n));
      const btn1Text = can1 ? labelFor(1) : ('Нужно ' + coins(b.cost));
      const btn5Text = can5 ? labelFor(5) : ('Нужно ' + coins(b.cost * 5));
      const btn10Text = can10 ? labelFor(10) : ('Нужно ' + coins(b.cost * 10));

      $("previewActions").innerHTML =
        '<button class="button primary" id="open1Btn" ' + (can1 ? '' : 'disabled') + '>' + btn1Text + '</button>'
        + '<button class="button primary" id="open5Btn" ' + (can5 ? '' : 'disabled') + ' style="background:linear-gradient(135deg, rgba(59,157,255,0.22), rgba(47,224,192,0.22));border-color:rgba(59,157,255,0.5)">' + btn5Text + '</button>'
        + '<button class="button primary" id="open10Btn" ' + (can10 ? '' : 'disabled') + ' style="background:linear-gradient(135deg, rgba(176,114,255,0.22), rgba(245,194,91,0.22));border-color:rgba(176,114,255,0.5)">' + btn10Text + '</button>';

      $("open1Btn").onclick = () => {
        this.close();
        if (caseBudget(c).can(1)) {
          CaseSpin.open(c);
          action("case.open", c.id);
        }
      };
      if ($("open5Btn")) $("open5Btn").onclick = () => {
        this.close();
        if (caseBudget(c).can(5)) {
          CaseSpin.open(c);
          action("case.open", c.id + ":5");
        }
      };
      if ($("open10Btn")) $("open10Btn").onclick = () => {
        this.close();
        if (caseBudget(c).can(10)) {
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

  const RANK_META = {
    "rank.sailor":  { prefix: "МОРЯК",   color: "#2fe0c0", perks: ["Префикс [МОРЯК] в чате", "2 точки дома /sethome", "Цветной ник в чате и Tab", "Базовый морской набор в F4"] },
    "rank.skipper": { prefix: "ШКИПЕР",  color: "#3b9dff", perks: ["Префикс [ШКИПЕР] в чате", "3 точки дома /sethome", "Приоритетный вход на сервер", "Кит Шкипера в F4"] },
    "rank.captain": { prefix: "КАПИТАН", color: "#f5c25b", perks: ["Префикс [КАПИТАН] в чате", "5 точек дома /sethome", "Полёт /fly на приватах", "Множитель удачи ×2", "Кит Капитана в F4"] },
    "rank.admiral": { prefix: "АДМИРАЛ", color: "#ff8c42", perks: ["Префикс [АДМИРАЛ] в чате", "10 точек дома /sethome", "Полёт /fly и смена ника /nick", "Множитель удачи ×4", "Кит Адмирала в F4"] },
    "rank.legend":  { prefix: "ЛЕГЕНДА", color: "#c264ff", perks: ["Префикс [ЛЕГЕНДА] в чате", "15 точек дома /sethome", "/fly, /hat и /nick", "Множитель удачи ×8", "Эксклюзивный кейс Легенды", "Максимальный кит сезона"] },
    "rank.vip":     { prefix: "VIP",     color: "#ff6b6b", perks: ["Префикс [VIP] в чате", "Виртуальный верстак /wb", "Эндер-сундук /ec", "Полёт /fly", "Косметика AquaLumen"] },
  };

  function rankGlyph(id, color) {
    const s = `fill="none" stroke="${color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"`;
    const icons = {
      "rank.sailor": `<svg viewBox="0 0 24 24" ${s}><path d="M3 15c1.8 3.2 5 5 9 5s7.2-1.8 9-5"/><path d="M12 4v12"/><path d="M8 9h8"/></svg>`,
      "rank.skipper": `<svg viewBox="0 0 24 24" ${s}><circle cx="12" cy="12" r="8.5"/><path d="M12 7v5l3.2 1.8"/><circle cx="12" cy="12" r="1.2" fill="${color}" stroke="none"/></svg>`,
      "rank.captain": `<svg viewBox="0 0 24 24" ${s}><circle cx="12" cy="12" r="3"/><path d="M12 3v3M12 18v3M3 12h3M18 12h3M6.2 6.2l2.1 2.1M15.7 15.7l2.1 2.1M17.8 6.2l-2.1 2.1M8.3 15.7l-2.1 2.1"/></svg>`,
      "rank.admiral": `<svg viewBox="0 0 24 24" ${s}><path d="m12 4 2.2 6.4H21l-5.4 4 2.1 6.6L12 17.2 6.3 21l2.1-6.6L3 10.4h6.8Z"/></svg>`,
      "rank.legend": `<svg viewBox="0 0 24 24" ${s}><path d="M12 3.5 13.6 8H18l-3.6 2.8L15.8 16 12 13.4 8.2 16l1.4-5.2L6 8h4.4Z"/><path d="M8 19h8" opacity=".7"/></svg>`,
      "rank.vip": `<svg viewBox="0 0 24 24" ${s}><path d="M12 3.5 19 9.2 16.4 20H7.6L5 9.2Z"/><path d="M8.2 9.4 12 16.5l3.8-7.1"/></svg>`
    };
    return icons[id] || icons["rank.sailor"];
  }

  function storeGlyph(id) {
    if (id === "gems.5") {
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3.5 19 10l-7 10.5L5 10Z"/><path d="M5 10h14M12 3.5 9.2 10 12 20.5M12 3.5 14.8 10 12 20.5"/></svg>`;
    }
    if (id === "pass.premium") {
      return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="6" width="18" height="12" rx="2.5"/><path d="M8 6v12M3 12h5"/></svg>`;
    }
    return "";
  }

  let rankModalOffer = null;
  function openRankModal(o) {
    rankModalOffer = o;
    const meta = RANK_META[o.id] || { prefix: o.title.toUpperCase(), color: "#2fe0c0", perks: [] };
    $("rankCrest").innerHTML = rankGlyph(o.id, meta.color);
    $("rankModalTitle").textContent = o.title;
    const tag = $("rankModalTag");
    tag.textContent = "";
    tag.style.display = "none";
    $("rankModalSub").textContent = o.subtitle || "";
    $("rankPerks").innerHTML = meta.perks.map(p =>
      `<div class="rank-perk"><svg viewBox="0 0 24 24" fill="none" stroke="${meta.color}" stroke-width="2.4"><path d="M4 12.5l5 5L20 6.5"/></svg>${esc(p)}</div>`
    ).join("");
    const buy = $("rankModalBuy");
    buy.innerHTML = o.currency === "gems"
      ? (`Купить — ${num(o.price)} крист.`)
      : (`Купить — ${coins(o.price)}`);
    buy.style.display = o.owned ? "none" : "";
    $("rankModalLayer").classList.add("open");
    send({ type: "modal", open: true });
  }

  function closeRankModal() {
    $("rankModalLayer").classList.remove("open");
    rankModalOffer = null;
    send({ type: "modal", open: false });
  }

  function storeView(s) {
    const cards = (s.store || []).map((o, cardIndex) => {
      const meta = RANK_META[o.id];
      if (!meta) {
        const art = storeGlyph(o.id) || getItemIconHtml(o.title, o.id, "mc-icon");
        return `<article class="card offer${o.owned ? " is-owned" : ""}" style="--i:${cardIndex}">
          ${o.owned ? `<span class="offer-badge">Куплено</span>` : ""}
          <div class="offer-art">${art}</div>
          <h3>${esc(o.title)}</h3>
          <p>${esc(o.subtitle)}</p>
          <div class="offer-foot">
            <span class="price">${o.currency === "gems" ? (num(o.price) + " крист.") : coins(o.price)}</span>
            <button class="button ${o.owned ? "" : "primary"} buy" data-id="${esc(o.id)}" data-title="${esc(o.title)}" ${o.owned ? "disabled" : ""}>${o.owned ? "Куплено" : "Купить"}</button>
          </div>
        </article>`;
      }
      const c = meta.color;
      return `<article class="card offer rank-card${o.owned ? " is-owned" : ""}" style="--rc:${c};--i:${cardIndex}">
        ${o.owned ? `<span class="offer-badge">Активна</span>` : ""}
        <div class="rank-glyph" style="color:${c};background:${c}14">${rankGlyph(o.id, c)}</div>
        <h3>${esc(o.title)}</h3>
        <p>${esc(o.subtitle)}</p>
        <div class="offer-foot">
          <span class="price">${o.currency === "gems" ? (num(o.price) + " крист.") : coins(o.price)}${o.owned ? `<span class="owned-check" title="Активна"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6"><path d="M4 12.5l5 5L20 6.5"/></svg></span>` : ""}</span>
          ${o.owned ? "" : `<button class="button primary rank-more" data-id="${esc(o.id)}">Подробнее</button>`}
        </div>
      </article>`;
    }).join("");
    return `<div class="view">${title("Магазин Улучшений", "Привилегии, префиксы и обмен")}
      <div class="store-grid stagger">${cards || '<div class="empty">Товаров нет</div>'}</div>
    </div>`;
  }

  function eventsView(s) {
    const banner = s.eventLine || "Сейчас тихо — ловите в своё удовольствие";
    const quests = Array.isArray(s.quests) ? s.quests : [];
    const cards = quests.map(q => {
      const ready = !q.claimed && Number(q.progress) >= Number(q.goal);
      let btn;
      if (q.claimed) btn = `<button class="button" disabled>Получено</button>`;
      else if (ready) btn = `<button class="button primary event-claim" data-idx="${q.idx}">Забрать</button>`;
      else btn = `<button class="button event-reroll" data-idx="${q.idx}">↻ ${coins(100)}</button>`;
      const pct = Number(q.goal) > 0 ? Math.min(100, Math.round(Number(q.progress) / Number(q.goal) * 100)) : 0;
      return `<article class="card offer" style="min-height:auto">
        <h3>${esc(q.desc)}</h3>
        <p>+${coins(q.reward)}</p>
        <div class="progress-label"><span>${q.progress} / ${q.goal}</span><span>${pct}%</span></div>
        <div class="progress"><i style="width:${pct}%"></i></div>
        <div class="offer-foot" style="margin-top:10px">${btn}</div>
      </article>`;
    }).join("");
    return `<div class="view">${title("События океана", banner)}
      <div class="store-grid">${cards || '<div class="empty">Контракты появятся после перезапуска сервера с модом aquatech_ui</div>'}</div>
    </div>`;
  }

  function casesView(s) {
    const cards = (s.cases || []).map((c, cardIndex) => {
      const col = CaseSpin.color(c.rarity);
      const iconUrl = CASE_ICONS[c.id] || "";
      const b = caseBudget(c);
      const openLabel = b.keys > 0 ? "Открыть" : (b.can(1) ? "Купить" : "Мало монет");
      const stock = coins(c.cost);
      const ownedBadge = b.keys > 0 ? ('<span class="case-owned">×' + b.keys + '</span>') : "";
      return `<article class="card case" style="--i:${cardIndex};border-color:${col}33;cursor:pointer;" data-preview="${esc(c.id)}">
        <span class="case-rarity" style="color:${col};border-color:${col}55;background:${col}14;">${CaseSpin.label(c.rarity)}</span>
        <div class="case-art">
          <img src="${iconUrl}" class="case-card-img" alt="${esc(c.title)}" />
          ${ownedBadge}
        </div>
        <h3>${esc(c.title)}</h3>
        <div style="font-size:12px;color:var(--text);font-weight:600;margin:4px 0 14px;">${stock}</div>
        <div class="case-actions-row" onclick="event.stopPropagation()">
          <button class="button preview-case" data-id="${esc(c.id)}">Состав</button>
          <button class="button primary open-case" data-id="${esc(c.id)}" ${b.can(1) ? "" : "disabled"}>${openLabel}</button>
        </div>
      </article>`;
    }).join("");

    return `<div class="view">${title("Кейсы", "Все кейсы в каталоге. Если кейс выдан — на карточке ×N.")}
      <div class="case-grid stagger">${cards || '<div class="empty">Кейсов нет</div>'}</div>
    </div>`;
  }

  const PASS_REWARDS = [
    { tier: 1, label: "Монеты", coins: 2500 },
    { tier: 2, label: "Монеты", coins: 3000 },
    { tier: 3, label: "Монеты", coins: 3500 },
    { tier: 4, label: "Монеты", coins: 4000 },
    { tier: 5, label: "Кейс I: Первопроходец", item: "starter", type: "case", coins: 5000 },
    { tier: 6, label: "Монеты", coins: 6000 },
    { tier: 7, label: "Монеты", coins: 7000 },
    { tier: 8, label: "Монеты", coins: 8000 },
    { tier: 9, label: "Монеты", coins: 9000 },
    { tier: 10, label: "Кейс II: Инженер", item: "smeltery", type: "case", coins: 12000 },
    { tier: 11, label: "Множитель улова ×4", item: "aquatech_ui:rate_x4", coins: 14000 },
    { tier: 12, label: "Монеты", coins: 16000 },
    { tier: 13, label: "Монеты", coins: 18000 },
    { tier: 14, label: "Монеты", coins: 20000 },
    { tier: 15, label: "Кейс V: Цифровая МЭ", item: "applied", type: "case", coins: 25000 },
    { tier: 16, label: "Монеты", coins: 30000 },
    { tier: 17, label: "Монеты", coins: 35000 },
    { tier: 18, label: "Монеты", coins: 40000 },
    { tier: 19, label: "Монеты", coins: 45000 },
    { tier: 20, label: "Кейс VII: Проводники", item: "superconductor", type: "case", coins: 50000 },
    { tier: 21, label: "Монеты", coins: 55000 },
    { tier: 22, label: "Множитель улова ×16", item: "aquatech_ui:rate_x16", coins: 60000 },
    { tier: 23, label: "Монеты", coins: 70000 },
    { tier: 24, label: "Монеты", coins: 80000 },
    { tier: 25, label: "Кейс IX: Драконий", item: "draconic", type: "case", coins: 100000 },
  ];

  function passView(s) {
    const season = s.season || { tier: 1, maxTier: 25 };
    const maxT = 25;
    const currentTier = Number(season.tier) || 1;
    const claimedList = (season.claimedTiers || []).map(Number);
    const cards = [];

    for (let t = 1; t <= maxT; t++) {
      const reward = PASS_REWARDS[t - 1] || { tier: t, label: "Награда", coins: 2500 + t * 1000 };
      const unlocked = currentTier >= t;
      const isClaimed = claimedList.includes(t);
      const isClaimable = unlocked && !isClaimed;

      let btnText = "Закрыто";
      let btnClass = "";
      let disabledAttr = "disabled";

      if (isClaimed) {
        btnText = "Забрано";
        disabledAttr = "disabled style='opacity:0.45;border-color:var(--line);color:var(--muted);cursor:default'";
      } else if (isClaimable) {
        btnText = "Забрать";
        btnClass = "primary claim-pass";
        disabledAttr = "";
      }

      let iconHtml;
      if (reward.type === "case") {
        const caseIconUrl = CASE_ICONS[reward.item] || "";
        iconHtml = caseIconUrl
          ? `<img src="${caseIconUrl}" style="width:42px;height:42px;object-fit:contain;filter:drop-shadow(0 4px 10px rgba(0,0,0,0.5))" />`
          : `<svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="var(--gold)" stroke-width="1.6"><rect x="3" y="6" width="18" height="15" rx="3"/><path d="M3 11h18M12 6v5"/></svg>`;
      } else if (!reward.item) {
        iconHtml = `<svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="var(--gold)" stroke-width="1.6"><circle cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="5.2"/><path d="M12 9.4v5.2"/></svg>`;
      } else {
        iconHtml = getItemIconHtml(reward.label, reward.item, "mc-icon");
      }

      cards.push(`<div class="card reward ${isClaimable ? "claimable" : ""}" style="min-height:190px;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:12px 10px;text-align:center;border-color:${isClaimable ? "var(--accent)" : isClaimed ? "rgba(255,255,255,0.06)" : "var(--line)"};${isClaimed ? "opacity:0.55;" : ""}">
        <span class="reward-level" style="font-weight:700;letter-spacing:0.04em;color:${unlocked ? "var(--accent)" : "var(--muted)"}">УРОВЕНЬ ${t}</span>
        <div class="reward-icon" style="height:54px;display:grid;place-items:center;margin:6px 0;">
          ${iconHtml}
        </div>
        <div style="font-size:11px;font-weight:700;line-height:1.2;margin-bottom:2px;max-width:110px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${esc(reward.label)}">${esc(reward.label)}</div>
        <div style="font-size:10px;font-weight:700;color:var(--gold);margin-bottom:6px;">+${coins(reward.coins)}</div>
        <button class="button ${btnClass}" data-level="${t}" ${disabledAttr} style="width:100%;font-size:9.5px;">${btnText}</button>
      </div>`);
    }

    return `<div class="view">${title("Сезонный Пропуск", "Выполняйте задания, ловите рыбу и забирайте ценные награды")}
      <section class="card season" style="margin-bottom:14px;">
        <div class="season-head">
          <div><h3 style="font-size:16px;">${esc(season.title || "Сезон I: Покорение Океана")}</h3><p>Доступно к получению: <b style="color:var(--accent);">${season.claimable || 0}</b> наград</p></div>
          <b class="tier" style="font-size:26px;">T${currentTier}</b>
        </div>
        <div class="progress-label"><span>Прогресс сезона</span><span>${Math.round((season.tierProgress || 0) * 100)}%</span></div>
        <div class="progress"><i style="width:${Math.round((season.tierProgress || 0) * 100)}%"></i></div>
      </section>
      <div class="section-title"><b>Линейка Наград (25 уровней)</b><span style="color:var(--accent);">Без гемов · Окупаемость 100%</span></div>
      <div class="pass-track" style="grid-template-columns:repeat(auto-fill,minmax(125px,1fr));gap:10px;padding-bottom:80px;">${cards.join("")}</div>
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
      <p style="font-size:10px;color:var(--muted);margin:0 0 10px;">Цена за шт: <b style="color:var(--gold);">${coins(f.priceCoins)}</b></p>
      <div class="offer-foot">
        <span class="price">${coins(f.count * f.priceCoins)}</span>
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
              <div class="rank">В инвентаре: <b>${totalFish}</b> шт. (${coins(totalValue)})</div>
            </div>
          </div>
          <button class="button primary sell-all-fish" ${totalFish <= 0 ? "disabled" : ""} style="height:36px;padding:0 18px;font-weight:750;margin-left:auto;">
            ${totalFish > 0 ? `Продать всё (+${coins(totalValue)})` : "Инвентарь пуст"}
          </button>
        </section>
        <section class="stats">
          <div class="stat"><small>Видов рыбы</small><b>${fishes.length} шт.</b></div>
          <div class="stat"><small>Баланс</small><b>${coins(s.wallet.coins)}</b></div>
        </section>
      </div>
      <div class="section-title"><b>Таблица цен скупки</b><span>Нажмите для продажи партии</span></div>
      <div class="store-grid" style="grid-template-columns:repeat(auto-fit,minmax(180px,1fr));">${cards || '<div class="empty">У вас нет рыбы в инвентаре</div>'}</div>
    </div>`;
  }

  function auctionView(s) {
    const lots = s.market || [];
    const activeCount = lots.length;
    let cards = "";

    if (lots.length === 0) {
      cards = `<div class="empty-card" style="grid-column:1/-1;text-align:center;padding:36px 20px;background:rgba(255,255,255,.02);border:1px dashed var(--line);border-radius:16px;">
        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="var(--muted)" stroke-width="1.5" style="margin-bottom:8px;"><path d="M4 8h16l-1.2 12H5.2L4 8Z"/><path d="M8.5 8V6.2a3.5 3.5 0 0 1 7 0V8"/></svg>
        <h3 style="font-size:15px;margin:0 0 6px;color:var(--text);">На бирже пока нет активных лотов</h3>
        <p style="font-size:11.5px;color:var(--muted);margin:0 0 14px;line-height:1.4;">Выставите первый предмет на продажу другим игрокам прямо сейчас!</p>
        <div style="font-size:11px;color:var(--accent);background:rgba(47,224,192,.08);padding:6px 14px;border-radius:8px;display:inline-block;border:1px solid rgba(47,224,192,.2);">
          Возьмите предмет в руку и напишите в чат: <b style="color:var(--gold);">/ah sell &lt;цена&gt;</b>
        </div>
      </div>`;
    } else {
      cards = lots.map(lot => {
        const icon = getItemIconHtml(lot.label, lot.itemId, "mc-icon");
        const isSelf = Boolean(lot.self);
        const actionBtn = isSelf
          ? `<button class="button cancel-auction" data-id="${lot.id}" style="background:rgba(255,107,107,.15);border-color:rgba(255,107,107,.4);color:var(--danger);font-weight:700;">Снять</button>`
          : `<button class="button primary buy-auction" data-id="${lot.id}">Купить</button>`;

        return `<article class="card offer" style="min-height:130px;padding:14px;display:flex;flex-direction:column;justify-content:space-between;">
          <div>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
              <span class="offer-badge" style="position:static;">x${num(lot.count || 1)}</span>
              <span style="font-size:10px;color:var(--muted);">Продавец: <b style="color:${isSelf ? 'var(--gold)' : 'var(--accent)'};">${esc(lot.seller || 'Игрок')}${isSelf ? ' (Вы)' : ''}</b></span>
            </div>
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:4px;">
              ${icon}
              <h3 style="font-size:13.5px;margin:0;font-weight:700;line-height:1.2;">${esc(lot.label || lot.itemId || 'Предмет')}</h3>
            </div>
          </div>
          <div class="offer-foot" style="margin-top:10px;">
            <span class="price" style="color:var(--gold);font-weight:800;">${coins(lot.price)}</span>
            ${actionBtn}
          </div>
        </article>`;
      }).join("");
    }

    return `<div class="view">${title("Аукцион и Рынок", "Покупайте и продавайте предметы между игроками")}
      <div class="grid two" style="margin-bottom:12px;">
        <section class="card hero" style="min-height:110px;padding:16px;">
          <div>
            <h2 style="font-size:18px;margin:0 0 4px;">Торговая Биржа</h2>
            <p style="font-size:11px;color:var(--muted);margin:0;">Держите предмет в руке и напишите в чат: <b style="color:var(--gold);">/ah sell &lt;цена&gt;</b></p>
          </div>
        </section>
        <section class="stats">
          <div class="stat"><small>Активных лотов</small><b>${activeCount} шт.</b></div>
          <div class="stat"><small>Ваш баланс</small><b>${coins(s.wallet ? s.wallet.coins : 0)}</b></div>
        </section>
      </div>
      <div class="section-title"><b>Свежие предложения</b><span>Обновляется в реальном времени</span></div>
      <div class="store-grid" style="grid-template-columns:repeat(auto-fit,minmax(230px,1fr));">${cards}</div>
    </div>`;
  }

  function kitsView(s) {
    const kits = s.kits || [];
    const cards = kits.map((k, cardIndex) => {
      const krc = (RANK_META["rank." + k.id] || {}).color;
      return `<article class="card case" style="--i:${cardIndex};${krc ? `border-color:${krc}44` : ""};display:flex;flex-direction:column;justify-content:space-between;padding:14px;min-height:110px;">
      <div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <h3 style="font-size:13px;margin:0;font-weight:700;">${esc(k.title)}</h3>
          <span class="offer-badge" style="position:static;">${esc(k.badge)}</span>
        </div>
        <p style="font-size:11px;color:var(--muted);margin:0 0 10px;line-height:1.4;">${esc(k.description)}</p>
      </div>
      <button class="button primary claim-kit" data-kit="${esc(k.id)}" style="align-self:flex-start;">Забрать набор</button>
    </article>`}).join("");

    return `<div class="view">${title("Наборы Снаряжения (Kits)", "Получайте экипировку и стартовые ресурсы")}
      <div class="case-grid stagger" style="grid-template-columns:repeat(auto-fit,minmax(220px,1fr));">${cards || '<div class="empty">Наборов нет</div>'}</div>
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
    document.querySelectorAll(".rank-more").forEach(b => b.onclick = () => {
      const offer = (state.payload.snapshot.store || []).find(x => x.id === b.dataset.id);
      if (offer && !offer.owned) openRankModal(offer);
    });
    document.querySelectorAll(".event-claim").forEach(b => b.onclick = () => action("events.claim", b.dataset.idx));
    document.querySelectorAll(".event-reroll").forEach(b => b.onclick = () => confirmAction("Сменить контракт", "Списать " + coins(100) + "?", "events.reroll", b.dataset.idx));
    
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
      if (!def || !caseBudget(def).can(1)) return;
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
      toast("Покупка лота...");
      action("auction.buy", String(b.dataset.id));
      setTimeout(() => action("hub.refresh"), 500);
    });

    document.querySelectorAll(".cancel-auction").forEach(b => b.onclick = () => {
      toast("Снятие лота...");
      action("auction.cancel", String(b.dataset.id));
      setTimeout(() => action("hub.refresh"), 500);
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
      pass: passView, fishing: fishingView, events: eventsView, auction: auctionView, kits: kitsView,
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
    const spr = (state.payload.snapshot || {}).profile;
    const prank = spr && spr.rankColor ? "#" + ((spr.rankColor & 0xffffff).toString(16).padStart(6, "0")) : "";
    document.documentElement.style.setProperty("--prank", prank);
    document.documentElement.style.setProperty("--prank-soft", prank ? prank + "30" : "");
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

    applyAppearance();
    
    const currentJson = JSON.stringify(s);
    if (currentJson !== lastSnapshotJson) {
      lastSnapshotJson = currentJson;
      renderNav();
      renderView(false, true);
    }

    if (s.caseResult) {
      const res = s.caseResult;
      delete s.caseResult;
      if (payload.snapshot) delete payload.snapshot.caseResult;
      CaseSpin.deliver(res);
    }
  }

  $("refresh").onclick = () => { action("hub.refresh"); toast("Обновление…"); };
  $("close").onclick = () => send({ type: "action", action: "hub.close" });

  $("rankModalClose").onclick = closeRankModal;
  $("rankModalCancel").onclick = closeRankModal;
  $("rankModalBuy").onclick = () => {
    if (!rankModalOffer) return;
    const id = rankModalOffer.id, title = rankModalOffer.title;
    closeRankModal();
    action("store.buy", id);
    toast(`Покупка «${title}»…`);
  };

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
      if ($("rankModalLayer").classList.contains("open")) {
        closeRankModal();
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
      if ($("rankModalLayer") && $("rankModalLayer").classList.contains("open")) {
        closeRankModal();
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
} catch (pageError) {
  var hubErrorBox = document.getElementById("content");
  if (hubErrorBox) {
    hubErrorBox.innerHTML = '<div class="empty">Ошибка интерфейса: ' +
      String(pageError && pageError.message ? pageError.message : pageError) + '</div>';
  }
  if (window.console && console.error) console.error("[AquaLumen] hub page error:", pageError);
}
</script>
</body>
</html>'''

def _coin_src():
    from io import BytesIO
    import base64
    from pathlib import Path
    coin = Path("docs/assets/images/coin_pixel.png")
    if coin.is_file():
        return "data:image/png;base64," + base64.b64encode(coin.read_bytes()).decode("ascii"), coin.read_bytes()
    logo = Path("docs/assets/logo.png")
    if not logo.is_file():
        return "coin.png", None
    try:
        from PIL import Image
    except ImportError:
        return "coin.png", logo.read_bytes()
    img = Image.open(logo).convert("RGBA")
    img.thumbnail((48, 48), Image.Resampling.LANCZOS)
    buf = BytesIO()
    img.save(buf, format="PNG", optimize=True)
    raw = buf.getvalue()
    return "data:image/png;base64," + base64.b64encode(raw).decode("ascii"), raw


coin_src, coin_png = _coin_src()
hub_html_content = (
    hub_html_raw
    .replace("__TEXTURES_JSON__", textures_json)
    .replace("__CASE_ICONS_JSON__", case_icons_json)
    .replace("__COIN_SRC__", coin_src)
)

try:
    from io import BytesIO
    from pathlib import Path as _P
    from PIL import Image as _Im
    _logo = _P("docs/assets/logo.png")
    if _logo.is_file():
        _src = _Im.open(_logo).convert("RGBA")
        _src.thumbnail((32, 32), _Im.Resampling.LANCZOS)
        _canvas = _Im.new("RGBA", (32, 32), (0, 0, 0, 0))
        _canvas.paste(_src, ((32 - _src.width) // 2, (32 - _src.height) // 2), _src)
        _buf = BytesIO()
        _canvas.save(_buf, format="PNG", optimize=True)
        _mc = _buf.getvalue()
        for _mc_path in (
            "mods/aqualumen-ui/src/main/resources/assets/aqualumen/textures/gui/coin.png",
            "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/textures/gui/coin.png",
        ):
            os.makedirs(os.path.dirname(_mc_path), exist_ok=True)
            with open(_mc_path, "wb") as _f:
                _f.write(_mc)
            print(f"Wrote {len(_mc)} bytes to {_mc_path}")
except Exception as _coin_err:
    print("coin.png 32px skipped:", _coin_err)

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
    if coin_png:
        with open(os.path.join(os.path.dirname(p), 'coin.png'), 'wb') as f:
            f.write(coin_png)
    print(f'Wrote {len(hub_html_content)} bytes to {p}')
