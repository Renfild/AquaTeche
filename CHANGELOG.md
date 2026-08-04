# AquaTech: Ocean Horizon — Changelog (beta)

## 2026-08-03 — Back to ocean world (no SkyblockBuilder)

- Restored ocean world from `world_backup_pre_iu_raft_*`; void skyblock world parked.
- **SkyblockBuilder** and **Simply Quarries** jars parked (server/client/CF).
- `level-type=minecraft:normal` + aquatech ocean datapacks again.
- **PersonalRaftSpawner** in aquatech-ui: new players get personal 4×4 rafts spaced 256 apart; returning players keep position.
- IslandGuard claims work without SB; IU quarry crafts blocked via KubeJS (`kubejs/server_scripts/00_disable_quarries.js`).
- Custom recipes migrated from CraftTweaker to KubeJS; CraftTweaker/EasyTweaker parked.

## 2026-08-03 — Raft + Industrial Survival concept sync

- CraftTweaker deny-list fixed to **nested IU IDs** (teleporter/jetpacks); added warp lens, neutron collectors, infinity catalysts, QuantumGenerators, crystal teleport armor; **Alfheim `world_seed` kept**.
- DE armor creative/elytra flight disabled in config; Botania `spawnWithLexicon=false`; `/gardenofglass` blocked.
- **Aquaculture** parked (conflict with Tide Tension); fishing listener priority **HIGHEST**.
- **IslandGuardHandler**: build/break only on your SB island (±96 from center); visits ≠ build rights.
- Casesmod: warps **spawn/shop** only (Y191); crates Primitive/Steam/Electric; kit «Паровой инженер»; leftover Gold case removed.

## 2026-08-03 — IU ocean raft rebuild

- SkyblockBuilder restored: personal **4×4 oak rafts**, `islandDistance` 192, spawn height 190; starter IU tools.
- Fishing retargeted to **Industrial Upgrade** mats (copper/tin/latex/plates/circuits).
- Cases/kits/F4 quests rewritten for IU **bundles** + Aqua XP.
- CraftTweaker `aquatech_iu_nerf.zs`: remove TP / flight / creative / quantum_miner crafts.
- FTB OP rewards (waystones, quantum, dislocators, infinity, jetpacks) stripped → copper IU.
- IU workshop positioned as **core tech path**; transformer explosions stay off.

## 2026-07-30 — Horizon Phase 1 (видимая дорога)
- **Deploy:** pressure от Y=190 + броня; LP→TAB hook; depth-loot рыбалки убран
- **LP:** promote/settier снимает старые флотские группы; track `horizon`; кириллические prefix
- **HUD:** Горизонт Hn + контракт дня N/M
- **Daily:** FISH только AquaTech; MACHINE только работающие BE; MARKET (медные монеты)
- **Docs:** `HORIZON_ROUTE.md` + `setup_horizon_route.ps1` smoke checklist

## 2026-07-29 — Маршрут Горизонта (meta road)
- **FTB:** глава `00_horizon_route` + group «★ Маршрут Горизонта» (ID `HF…`, spine не тронут)
- **Ранги:** sailor → skipper → captain → admiral → legend + VIP cosmetics; homes 2/3/4/6/8
- **Мод:** `/aquatech daily|season|horizon|promote|storm`, Жетон Прилива, контракты дня, шторм ×2 лут
- **Варпы:** spawn/pier/market/atoll/harbor заготовки + `setup_horizon_route.ps1`
- **Доки:** `HORIZON_ROUTE.md`, голограмма гавани, ajLeaderboards план

## Beta freeze note
Quest IDs in `server/config/ftbquests/quests/chapters/*.snbt` are **frozen** for this beta.
Do not renumber spine IDs (`1x0000000000000N`) after players have progress — only add new side-quest IDs.

## 2026-07-28 — Machine GUIs + progression logic
- **UI:** unique ocean-steampunk GUI atlases for all 6 machines; shared `AbstractAquaMachineScreen` with energy/progress/burn widgets and cyan chrome
- **Upgrades:** Speed / Efficiency / Double Hook now affect AutoFisher, Ocean Filter, Seabed Dredger, Hydro Reactor
- **Fishing:** lures + tackles on the rod (tackle box) modify loot; abyssal table heavily nerfed; key skill nodes apply catch/speed/FE bonuses
- **Balance:** Hydro Reactor ~1200 FE/t (was 25k); dredger early loot only; AutoFisher requires adjacent water; mid/late machine recipes moved to CraftTweaker (cheap datapacks removed)

## 2026-07-28 — AquaTech UI hub expansion
- **Bugs:** Hydro Reactor / Ocean Altar / Abyssal Portal now use real block classes, BlockItems, menus, and tickers; skill capability clones on dimension change; skills sync on login; null-safe skill packet; whitelist skill unlock validation
- **Machines:** Hydro Reactor (kelp pellet → FE), Ocean Altar (4 relics → Neptune Trident), Abyssal Portal shrine buff; recipes for reactor/altar/portal/pellet/lures
- **Quests:** aquatech_ui side-quests in chapters 04–09, 11–13; every capstone grants Aqua XP via `/aquatech grantxp` + Lightman's coins
- **Art:** full AI→pixel-art pass for rods, lures, tackles, upgrades, gear, and all 6 machine block faces
- **Deploy:** rebuilt `aquatech_ui-1.0.0.jar` to server/client/mods folders; CraftTweaker lure IDs corrected

## 2026-07-28 — Roadmap completion pass
- Phase 0: smaller ocean HUD, TAB/FeatherBoard disabled, ch10 Cyrillic rewrite, quest item validation (0 issues)
- Phase 1: full rewrite of XII Dreadnought (Eureka/VS), bridges 01→12 verified
- Phase 2: CraftTweaker `aquatech_balance.zs` gates MA netherite/uranium + Roost late chickens + Create mechanical arm
- Phase 3: aquatech_ui rods/machines/pressure wired into chapters 01/02/03/10
- Phase 4: Skyblock starter inventory, LuckPerms sailor/captain/admiral, WorldGuard spawn/PvP, economy coins, kit update
- Phase 5: optional XIII Horizon Raids chapter, fleet side quest, ajLeaderboards board plan
- Phase 6: `export_client_pack.py`, backup script, this changelog, quest ID freeze

## How to progress (Act I)
1. Join → receive novice rod + boat + quest book
2. Open FTB Quests → Act I · Kickstarter
3. Fish resources → Catch → Atoll life
