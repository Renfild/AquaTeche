# HiTech1211 internals — how Next is built

Local analysis of `C:\Users\xieto\McSkill\clients\HiTech1211`.  
**Do not deploy to Apex. Do not copy `next_*` jars into AquaTech.** License on every Next jar: All Rights Reserved. Vendor: `dev.nexteam`.

Local copy (gitignored): `scripts/scratch/hitech1211/` — jars, extracted assets, kubejs, resources, config.  
Jar class index: `scripts/scratch/_hitech_jar_index/index.txt`  
Lang dumps: `scripts/scratch/_hitech_next_lang/`  
Player-facing map: `HITECH1211_UX.md`

There are **no Bukkit plugins**. “Plugin layer” is NeoForge mods that replace Essentials / LuckPerms hooks / DeluxeMenus.

---

## Stack

```
Title / Pause / Locations     next_ui  (Essential Elementa + shaders)
Shared widgets / fonts        next_canvas
Chat HUD                      chatplus  → next_chat (prefixes / commands)
Money                         next_economy  (balances + ExchangeScreen + FTB reward)
Cases (player + editor)       next_shop  + legacy nextshop
Auction                       next_auction
Daily / ratings / crates      next_rewards
Guide hints + GuideScreen     next_guide
NPC trades                    next_npc
Kits editor                   next_kits
Homes / warps / holograms     next_essentials
Limits / AC                   next_limiter + next_guard
Compat mixins                 next_tweaker
Platform                      next_core  (packets, LuckPerms SERVER, KubeJS, JEI)
Pack content                  next_hitech + kubejs hitech_* + MBD2/LDLib
```

Kotlin + Java 21. Networking = NeoForge `STREAM_CODEC` packets (1.21 style).  
GUI toolkit = **Elementa** (Hypixel/Skyblock-style immediate UI), not vanilla Screen widgets and not HTML/MCEF.

AquaTech F4 hub is the opposite toolkit: MCEF/html + native fallback. HiTech never used a browser hub.

---

## How a button becomes a screen

1. KubeJS `NextUIEvents.pauseScreen` adds button ids (`next_ui:quests`, `next_rewards:daily`, …).
2. `RegisterPauseScreenButtonsEvent` / `PauseScreenManager` builds `PauseButton` icons from `assets/.../textures/gui/pause/*.png`.
3. Each feature mod registers a `PauseScreenListener` (auction, rewards, shop).
4. Click → S2C packet (`OpenRewardsScreenPacket`, `OpenExchangeScreenPacket`, `OpenGuidePacket`, `OpenCaseEditorPacket`) **or** client command (`/home`, warp market, FTB quests GUI).
5. Screen is Elementa: `AuctionScreen`, `ExchangeScreen`, `GuideScreen`, `CaseEditorScreen`, `Rating` tables, `TradeScreen`.

Pause icons in `next_ui` jar:

```
textures/gui/pause/home.png
textures/gui/pause/quests.png
textures/gui/pause/market.png
textures/gui/pause/money_change.png
textures/gui/pause/rtp.png
textures/gui/pause/settings.png
textures/gui/pause/shop.png
```

`next_rewards` adds `daily.png`, `leaderboard.png`.  
`next_auction` adds `pause/auction.png`.

Title screen is fully replaced: `next_ui.client.gui.screen.TitleScreen` + overlay pack  
`resources/ui/assets/next_ui/textures/gui/main_menu/background.png` (3 MB).  
Shaders: contrast + vignette. Particles + `MenuBar` (Play / Mods / Settings / Quit).

Location picker: `LocationScreen` — 3D scene (`SceneRenderManager`) + `SideBar` + `LocationButton`. Sounds: `location_swap.ogg`.

HUD: `BrandingOverlay` (project/server), `StatsOverlay`, custom `TabListOverlay` (mixin on vanilla tab).  
Toasts: `ShowNotificationToastPacket` + `notification_{error,info,success}.png`.

---

## Chat

`chatplus` mixins vanilla `ChatComponent` / `ChatScreen`. Own `ChatPlusScreen`, tabs, find, bookmark, copy.  
`next_chat` is tiny (8 classes): command mixins so prefixes / channel routing live server-side.  
Client filter = regex on private-use prefix characters (see `config/chatplus-v1.json`).

---

## Economy / shop / auction / rewards

| Flow | Packet / screen |
|------|-----------------|
| Balances | `RequestBalancesPacket` / `ResponseBalancesPacket` |
| Exchange | `OpenExchangeScreenPacket` → `ExchangeScreen` → `PerformExchangePacket` |
| Item prices | `SyncItemPricesPacket` (min price tooltip) |
| Auction list | `RequestListingsPacket` → `ListingsPacket` → `AuctionScreen` |
| Buy / sell / remove / history | `RequestBuy/Sell/Remove/HistoryPacket` |
| Daily UI | `OpenRewardsScreenPacket` |
| Ratings | `OpenRatingPacket` (money + playtime tables, hourly prize) |
| Quest crate | `RewardCrateMenu` + GeckoLib crate block |
| Case editor (admin) | `OpenCaseEditorPacket` → `CaseEditorScreen` → `SaveCasePacket` |
| Case loot types | item / privilege / pokemon modals |

`next_shop` in this pack is mostly the **case editor**, not a DeluxeMenus chest shop. Player donate URL is config: `next-shop-client.toml` → `https://mcskill.net/pay`.

FTB reward class: `NextBalanceReward` (`ftbquests.reward.next_economy.next_balance`).

---

## Guide / teaching

Not Patchouli. Two layers:

1. **Hints** — KubeJS `NextGuideClientEvents.hints` builds lines with `.mouseRight()`, `.button("shift")`, `.item(...)`, `.tag(...)`.  
   Client: `HintManager` + mouse PNG icons (`hint/mouse_left.png` etc.).
2. **Guide book** — `GuideData` / `CardData` / `ActionData` synced, `GuideScreen` (Elementa + scrollbar). Opened by `OpenGuidePacket`.

NPCs: KubeJS `NextNPCEvents.registerNPC` → GeckoLib models in `resources/misc/assets/next_npc/`. Trade UI: `TradeScreen` + `PerformTradePacket` + `TradeLimitsPacket`.

---

## “Plugins” that are actually mods

| Bukkit analog | Next mod | Evidence |
|---------------|----------|----------|
| Essentials | `next_essentials` | home, fly, heal, hologram, invsee (`InvseeMenu`), schematic packets |
| LuckPerms | **LuckPerms NeoForge** required by `next_core` (SERVER) | `SyncPermissionsPacket` |
| DeluxeMenus | Elementa screens + pause hub | no chest menus |
| Vault | `next_economy` | dual currency (next + donate) |
| DeluxeChat | `next_chat` + ChatPlus | prefixes + tabs |
| Citizens | `next_npc` | trade NPCs |
| WorldGuard | `next_guard` + `next_limiter` | region/team limits (tooltips in `limits.js`) |
| Crate plugins | `next_rewards` crate + `next_shop` cases | |

`next_core` also: login queue (`SetLoginQueuePositionPacket`), proxy transfer (`VelocityTransferPacket`), tab list sync, `NotificationToast`, custom dimension names, FTB Quests client mixin.

---

## Tweaker (why the pack feels “finished”)

`next_tweaker` is a mixin dump, not gameplay:

- Kill FTB Library sidebar
- EMI ↔ FTB quests recipe screen
- Xaero no internet
- Jade AE2 part storage
- Hide KubeJS load errors in prod
- Sodium / Iris / ImmediatelyFast compat for Draconic
- Unlock gamemode switcher, no “unsecure server” toast
- Copy-ID button on quests (admin)

This is the “polish layer”. AquaTech equivalent is scattered (packetfixer, FancyMenu, our own mixins).

---

## Pack content (not the hub)

`next_hitech` ships machine screens as Kotlin: Press, Smelter, Furnace, Pulverizer, Assembler, OreMiner, Crucible, Insolator, LootFabricator, Water/Rock generators, Molecular Transformer, Compression Chamber.

KubeJS **startup** registers `hitech_content:*` items (alloys, hearts, matter data cards) + fluids/blocks + creative tabs.  
KubeJS **client** is lang + tooltips + EMI generators + Photon VFX. **No server_scripts on the client.**

FTB quest **art** is huge in `resources/misc/assets/ftbquests/textures/` (planets, fusion/SPS cutouts, welcome team photos, Discord/forum cards). Theme file only styles widgets; chapters still server-only.

---

## How AquaTech should steal this (local only)

Do **not** port Elementa or Next jars.

| Their piece | Our piece | Concrete steal |
|-------------|-----------|----------------|
| Esc pause = OS of the server | FancyMenu pause is dead glass | Add the same 8–10 actions as FancyMenu buttons that run `/hub`, F4 tab, `/ftbquests`, `/warp market` |
| Elementa screens | AquaLumen F4 + html | Keep F4; pause is just a launcher into F4 |
| STREAM_CODEC feature packets | HubActionHandler strings | Already our pattern |
| next_guide hints | none | KubeJS tooltips + overlay hints for AF / dredger / rods |
| limits.js red lines | IslandLimiter silent | Show the number on the item |
| ChatPlus tabs | AquaChat | Already; optional prefix chars |
| Case editor Elementa | admin.html / F4 | We have cases; add guarantee copy |
| FTB welcome art | our ocean theme | Optional chapter splash images — not required |
| next_tweaker sidebar off | FTB sidebar | Disable sidebar in ftblibrary-client |

First local slice if you want code next: **FancyMenu pause buttons** wired to existing AquaTech commands. No Apex upload.

Steal order (patterns, redraw our art):

1. Pause → F4 / quests / market / cases / daily / RTP / home
2. KubeJS red tooltips for IslandLimiter numbers
3. Case guarantee one-liner in F4
4. Mouse-button glyphs in rod/machine tooltips
5. Optional FTB chapter splash images (ours, not McSkill photos)

---

## What this client cannot give you

- FTB chapter SNBT / quest text  
- LuckPerms groups / kit contents  
- Case loot tables (server)  
- Warp coordinates  
- next_essentials hologram data  

Those live on McSkill’s **server** jar/config, not in this launcher profile.
