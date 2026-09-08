# HiTech1211 UX map → AquaTech

Internals (jars, packets, screens): `HITECH1211_INTERNALS.md`.

Source: `C:\Users\xieto\McSkill\clients\HiTech1211` (McSkill **client**, NeoForge 1.21.1, 158 mods).
Quest SNBT and plugin configs live on McSkill **server** — not in this folder.

**Do not deploy any of this to Apex.** Local reference only. McSkill `next_*` jars are proprietary — copy patterns, not binaries.

---

## What this pack actually is

Not Bukkit DeluxeMenus. Whole player shell is the **Next** client stack:

| Layer | Mods | Player opens |
|-------|------|----------------|
| Pause hub | `next_ui` + KubeJS `pause_screen.js` | Esc |
| Main menu | `next_ui` + `resources/ui/gui/main_menu/` | Title screen |
| Economy | `next_economy` | Обмен, `/money` |
| Shop / cases | `next_shop` | Esc → Кейсы; pay URL `mcskill.net/pay` |
| Auction | `next_auction` | Esc → Аукцион |
| Daily + ratings | `next_rewards` | Esc → Ежедневки / Рейтинги |
| Chat | ChatPlus + `next_chat` | T, tabs |
| Quests | FTB Quests (server-synced) | Esc → Квесты |
| Recipes | **EMI primary**, JEI secondary | Ctrl+O / R |
| Map | Xaero (not JourneyMap) | M |
| Hints | `next_guide` + KubeJS `hints.js` | hover / look |

No FancyMenu. No plugins folder. No local `config/ftbquests/quests/`.

---

## Pause menu (the hub)

`kubejs/client_scripts/next/ui/pause_screen.js`:

```
home          → /home
quests        → FTB Quests GUI
money_exchange→ Next coins ↔ donate currency
market        → warp market
cases         → case UI
rtp           → random TP
auction       → auction house
daily         → daily reward chain
ratings       → money / playtime tops
settings      → vanilla options
```

HUD overlay (`next_ui`): project name + server name (small-caps). Tab list: world, online, server.

AquaTech today: FancyMenu pause is glass + vanilla buttons only. Real hub is **F4 AquaLumen**. HiTech puts the same actions on **Esc**.

---

## Chat

ChatPlus window 288×225, bottom-left. Tabs: все / глобал / локал / личные / система. Channel lines start with a private-use prefix so each tab is one regex. Система has notifications off.

Ctrl+F find, Ctrl+C copy, Ctrl+B bookmark.

AquaTech already has AquaChat tabs (все / глобал / локал / trade). Pattern to steal: **Unicode channel prefix** so overlay filter is one regex, not LuckPerms parse.

---

## Quests

- FTB Quests 2101 + Teams. Sidebar **disabled** (`next_tweaker.disable_sidebar = true`).
- Open via pause, not a keybind (quest key unbound).
- Completion + rewards = **toast**, not chat spam.
- Pinned tracker right, scale 0.75.
- Theme: `resources/misc/assets/ftbquests/ftb_quests_theme.txt` (dark squares, extra shapes).
- Custom reward type: `next_economy:next_balance` (Нексты).
- Crate names in KubeJS hint factory tiers: furnace → crusher → fusion → SPS → quantum → solar.

AquaTech already has FTB + ocean theme in `kubejs/assets/ftbquests/ftb_quests_theme.txt`. Steal: **pause button → quests**, toast-only, no FTB sidebar fighting F4.

Chapters: need McSkill server dump. Client cannot list them.

---

## Teaching UX (not books)

Patchouli / GuideME books are empty. Teaching is:

1. **NextGuide hints** — ПКМ + upgrade, config card copy/paste, Shift+RMB scanner.
2. **Red tooltips** — team/region limits (`limits.js`): «Ограничение на команду: N шт.»
3. **Market rules on items** — «Покупка запрещена» / «только за Нексткоины».
4. **SHIFT for details** on multiblock upgrade tables.
5. NPCs: Инженер / Механик / Исследователь / Садовод.

AquaTech already has fishing tooltips + Horizon login line. Steal: **machine/limit tooltips in red**, not a fourth progression system.

---

## Economy / cases

- Soft currency: некст. Donate: скилл / эм (per-network names).
- Exchange screen separate from shop.
- Cases: rarity IDs 1008–1012, guarantee copy in RU with color codes.
- Endgame items blacklisted from player market.

AquaTech: coins in F4 + portal, cases in F4, Lightman's, `PURCHASES_ENABLED=false`. Steal: **guarantee line on case UI**, market blacklist on late loot.

---

## HUD stack (keep small)

AppleSkin + Jade (dark, toggle, Shift details) + Raised offsets + ImmersiveUI slot juice + Legendary Tooltips + Item Borders + Xaero + Mek H.

`next_tweaker` kills Xaero internet + FTB sidebar.

---

## Map onto AquaTech (do not copy jars)

| HiTech | AquaTech already | Next local work (no Apex) |
|--------|------------------|---------------------------|
| Esc pause hub | FancyMenu pause is dead; F4 is hub | FancyMenu buttons → F4 tabs / warps / quests / daily |
| next_ui branding HUD | none | skip unless asked |
| ChatPlus tabs + prefixes | AquaChat tabs | optional prefix if Mohist chat is messy |
| FTB toast + pause open | FTB book + F4 eventLine | bind Esc «Квесты»; keep sidebar off |
| next_rewards daily chain | F4 daily coins + Horizon contract | already one claim; don't add a third daily |
| next_auction | F4 auction + site market | already |
| EMI | JEI | don't switch viewers on 1.20.1 Forge |
| limits.js red tooltips | IslandLimiter | client tooltip copy of limits |
| next_guide hints | none | KubeJS hover hints for AF / dredger / rods |
| NPC traders | none | optional later |

---

## Files worth rereading

```
C:\Users\xieto\McSkill\clients\HiTech1211\kubejs\client_scripts\next\ui\pause_screen.js
C:\Users\xieto\McSkill\clients\HiTech1211\kubejs\client_scripts\next\guide\hints.js
C:\Users\xieto\McSkill\clients\HiTech1211\kubejs\client_scripts\limits.js
C:\Users\xieto\McSkill\clients\HiTech1211\kubejs\client_scripts\next\economy\tooltips.js
C:\Users\xieto\McSkill\clients\HiTech1211\config\chatplus-v1.json
C:\Users\xieto\McSkill\clients\HiTech1211\config\next-shop-client.toml
C:\Users\xieto\McSkill\clients\HiTech1211\resources\misc\assets\ftbquests\ftb_quests_theme.txt
```

Lang dumps (local scratch): `scripts/scratch/_hitech_next_lang/`

To list real FTB chapters: need McSkill **server** `config/ftbquests/quests/chapters/` — not in the client.
