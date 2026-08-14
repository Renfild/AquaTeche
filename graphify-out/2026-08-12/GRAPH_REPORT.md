# Graph Report - AquaTech  (2026-08-12)

## Corpus Check
- 429 files · ~173,182 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2240 nodes · 3809 edges · 205 communities (165 shown, 40 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 43 edges (avg confidence: 0.77)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ef21dc2b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- index.js
- ZipFile
- CaseManager
- AquaTech Addon + KubeJS System
- js/site.js
- AquaTechLauncher
- aquatech_launcher.py
- .LoginAsync
- main.go
- SoftButton
- check_pack_update_available
- What You Must Do When Invoked
- 🧭 Handoff для Antigravity — спринт 10–11 августа 2026
- generate_workshop_quests.py
- AquaTechLauncher.csproj
- gen_machine_guis.py
- Полная таблица лута удочек AquaTech
- QuestManager
- gen_clean_machine_guis.py
- MainViewModel
- HttpDownload
- CasesMod.java
- Fact
- .BuildAsync
- PlayerSyncHandler.java
- net.minecraftforge.eventbus.api.SubscribeEvent
- patches/patch_fawe_mohist.py
- .ApplyAsync
- Path
- AquaTechLauncher.Core
- .EnsureJava17Async
- ModCreativeTab.java
- PatchIUNoFreeScanner
- AquaTechAPIHandler
- NetworkHandler
- ForgeInstaller
- KitManager
- gen_sc_fishing_ui.py
- Улучшенная — Улучшенная электрическая эра
- setup_apex_mysql.py
- .PlayAsync
- CasesMod — Кейсы, Киты, Варпы, Квесты, Привилегии (Forge 1.20.1)
- build_launch_cmd
- FishSellService.java
- setup_d1.py
- AquaTech: Ocean Horizon — Changelog (beta)
- dependencies
- AquaTechLauncher.ViewModels
- .CheckAndApplyAsync
- Маршрут Горизонта — AquaTech meta-progression
- CaseListSyncS2CPacket
- WarpManager
- gen_stardew_rhythm_ui.py
- test_launcher_smoke.py
- publish_client_pack.py
- upload_to_github.py
- MenuCatalogSyncS2CPacket
- 🗺️ Полный Роадмап Развития Игрока: AquaTech (Minecraft 1.20.1)
- Mod Development Guidelines
- app.js
- bootstrap_p0_local.py
- Продвинутая — Продвинутая электрическая эра
- Электрика — baseElectric
- generate_600_ocean_quests.py
- install_oraxen_rank_prefixes.py
- manifest.json
- patch_chapter
- Minimalist Minecraft Pixel Art GUI Skill
- .calculatePrice
- neutralize_aquatech_sc_preview.py
- CurrencyManager
- net.minecraft.server.level.ServerPlayer
- launcher_tests/test_bootstrap_update.py
- PityManager
- AquaTechLauncher
- Quest ID Freeze (AquaTech Beta)
- reorganize_layout.py
- sync_lodestone_mods.py
- AquaTech Release & Development Workflows
- RelayCommand
- ClaimQuestC2SPacket
- TeleportWarpC2SPacket
- AquaTech
- gen_rhythm_textures.py
- generate_resource_rods.py
- slice_rhythm_ui.py
- generate_spine_quests.py
- download_industrial_upgrade.py
- download_opt_mods.py
- upload_launcher_release.py
- Program
- OpenCaseC2SPacket
- Пар — steam
- make_rate
- finish_pack_release.py
- strip_op_quest_rewards.py
- install_kubejs.py
- make_boot_fixes_datapack.py
- gradlew
- deploy/deploy_runtime.ps1
- generate_site.py
- cf_token_playwright.js
- upload_ftbquests_assets.py
- force_deep_ocean_world.py
- download
- upload_pack_release.py
- 40_aquatech_diving.js
- Дополнительные тексты guide.* / quarry.guide.*
- deploy/deploy_industrial_upgrade.ps1
- scripts/
- deploy_to_cloudflare.py
- export_client_pack.py
- generate_manifest.py
- fix_iu_item_ids.py
- portal/set_github_cf_secret.py
- portal/smoke_portal_and_versions.py
- portal/test_portal_login_cookie.py
- compress_quest_image.py
- setup_mohist_server.py
- generate_aquatech_ui_assets.py
- tools/get_cf_token.py
- tools/patch_fawe_mohist.py
- tools/patch_iu_no_free_scanner.py
- boost_ocean_life.py
- create_ocean_atom_island.py
- create_ocean_raft.py
- tools/set_github_cf_secret.py
- tools/smoke_portal_and_versions.py
- tools/test_portal_login_cookie.py
- tools/test_portal_login_happy.py
- aquatech/bootstrap
- setup_luckperms_config.py
- Примитив — Примитивная эра
- Совершенная — perElectric
- Обзор — Основная информация
- FishMarketNpcInteract
- deploy_apexnodes_sftp.py
- LauncherEngine
- .PingAsync
- net.minecraft.network.FriendlyByteBuf
- repair_server_world.py
- ClaimKitC2SPacket
- Q: Install gstack caveman graphify for AquaTech Cursor
- check_aquateche_domain.py
- Industrial Upgrade — руководство из мода
- RequestOpenMenuC2SPacket
- fix_aquatech_sc_fish_minigame.py
- replace_release_bootstrap_exe.py
- tools/test_bootstrap_update.py
- _java_major_version

## God Nodes (most connected - your core abstractions)
1. `Улучшенная — Улучшенная электрическая эра` - 105 edges
2. `Продвинутая — Продвинутая электрическая эра` - 94 edges
3. `Электрика — baseElectric` - 60 edges
4. `json()` - 46 edges
5. `bad()` - 45 edges
6. `Пар — steam` - 39 edges
7. `AquaTechLauncher` - 37 edges
8. `MainViewModel` - 34 edges
9. `handleApi()` - 32 edges
10. `Дополнительные тексты guide.* / quarry.guide.*` - 31 edges

## Surprising Connections (you probably didn't know these)
- `main()` --calls--> `build_leaf_index()`  [INFERRED]
  scripts/tasks/extract_iu_guide_book.py → tools/quests/gen_iu_guide_ftbquests.py
- `main()` --calls--> `load_models()`  [INFERRED]
  scripts/tasks/extract_iu_guide_book.py → tools/quests/gen_iu_guide_ftbquests.py
- `main()` --calls--> `parse_quests()`  [INFERRED]
  scripts/tasks/extract_iu_guide_book.py → tools/quests/gen_iu_guide_ftbquests.py
- `main()` --calls--> `resolve_item()`  [INFERRED]
  scripts/tasks/extract_iu_guide_book.py → tools/quests/gen_iu_guide_ftbquests.py
- `main()` --calls--> `msgBox()`  [INFERRED]
  bootstrap/main.go → bootstrap/ui_windows.go

## Import Cycles
- None detected.

## Communities (205 total, 40 thin omitted)

### Community 0 - "index.js"
Cohesion: 0.09
Nodes (76): onRequestPatch(), mapRow(), onRequestGet(), onRequestPost(), parsePerks(), SHORT, onRequestGet(), onRequestDelete() (+68 more)

### Community 1 - "ZipFile"
Cohesion: 0.05
Nodes (60): main(), strip_mc(), title_for(), bump_jar(), main(), Path, main(), patch_class() (+52 more)

### Community 2 - "CaseManager"
Cohesion: 0.10
Nodes (11): CaseDefinition, CaseItem, Rarity, COMMON, EPIC, LEGENDARY, RARE, UNCOMMON (+3 more)

### Community 3 - "AquaTech Addon + KubeJS System"
Cohesion: 0.04
Nodes (41): 1) New KubeJS nerf script, 2) New KubeJS craft (gated), 3) Extend fishing resource mapping (Java), 4) New rate tier (Java + data), 5) New casesmod case (JSON only), 6) StarCatcher texture refresh, Examples — AquaTech addons, Feature request → layer (quick) (+33 more)

### Community 4 - "js/site.js"
Cohesion: 0.13
Nodes (31): api(), applySiteCopy(), catalogCard(), copyIP(), ensureAudio(), esc(), formatNewsDate(), getUser() (+23 more)

### Community 5 - "AquaTechLauncher"
Cohesion: 0.12
Nodes (6): AquaTechLauncher, normalize_game_dir(), normalize_server_cfg(), Default game folder; user can override in settings., Center on the primary monitor (custom-launcher UX from typical MC launchers)., Install AquaTech pack into the game folder. Order (Play): 1) Local AquaTech-…

### Community 6 - "aquatech_launcher.py"
Cohesion: 0.06
Nodes (40): Popen, ThreadingHTTPServer, _artifact_path(), check_launcher_update_available(), ensure_libraries_and_natives(), ensure_servers_dat(), fetch_bootstrap_manifest(), _fetch_json_hard() (+32 more)

### Community 7 - ".LoginAsync"
Cohesion: 0.30
Nodes (8): Error, PortalApi, PortalStats, CancellationToken, Task, Nick, Ok, Session

### Community 8 - "main.go"
Cohesion: 0.09
Nodes (31): copyDir(), copyFile(), downloadFile(), fetchBestManifest(), fetchManifest(), findLauncher(), localVer(), logf() (+23 more)

### Community 9 - "SoftButton"
Cohesion: 0.13
Nodes (6): Entry, Frame, NavItem, Rounded launcher button with hover., Sidebar navigation row., SoftButton

### Community 10 - "check_pack_update_available"
Cohesion: 0.18
Nodes (12): apply_manifest_sync(), build_manifest_from_pack(), check_pack_update_available(), md5_file(), _pack_looks_ready(), purge_pack_extras(), Heuristic: pack already installed — skip long CDN timeout cascades on Play., Delete files under PACK_FOLDERS that are not listed in the manifest (LoliLand-… (+4 more)

### Community 11 - "What You Must Do When Invoked"
Cohesion: 0.08
Nodes (24): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+16 more)

### Community 12 - "🧭 Handoff для Antigravity — спринт 10–11 августа 2026"
Cohesion: 0.04
Nodes (45): 1. Текущее состояние (snapshot на 2026-08-11), 2.1 Два типа удочек, 2.2 Что ломалось и как чинили, 2.3 Datapack aquatech_boot_fixes, 2. Рыбалка StarCatcher + aquatech_ui (критично), 3.1 Что случилось, 3.2 Текущий source of truth (квесты), 3.3 Правила для агента (НЕ НАРУШАТЬ) (+37 more)

### Community 13 - "generate_workshop_quests.py"
Cohesion: 0.15
Nodes (18): chapter_code(), chapter_snbt(), ensure_workshop_group(), esc(), layout_xy(), main(), Path, quest_block() (+10 more)

### Community 14 - "AquaTechLauncher.csproj"
Cohesion: 0.10
Nodes (17): net9.0, Microsoft.NET.Sdk, net9.0, Microsoft.NET.Sdk, net9.0, Microsoft.NET.Sdk, Avalonia (11.2.3), Avalonia.Desktop (11.2.3) (+9 more)

### Community 15 - "gen_machine_guis.py"
Cohesion: 0.37
Nodes (20): arrow_fill(), bevel_panel(), draw(), energy_fill_strip(), flame_fill(), inv_grid(), main(), make_auto_fisher() (+12 more)

### Community 16 - "Полная таблица лута удочек AquaTech"
Cohesion: 0.11
Nodes (18): Fish-only (без ресурсного пула AquaTech), T10 — `starcatcher:obsidian_rod`, T11 — `starcatcher:lush_glowberry_rod`, T12 — `starcatcher:magmaforged_rod`, T13 — `starcatcher:alpha_rod`, T1 — `starcatcher:bamboo_rod` / `minecraft:fishing_rod`, T2 — `starcatcher:humble_rod`, T3 — `starcatcher:good_old_rod` (+10 more)

### Community 17 - "QuestManager"
Cohesion: 0.18
Nodes (3): PlayerQuestData, QuestDefinition, QuestManager

### Community 18 - "gen_clean_machine_guis.py"
Cohesion: 0.32
Nodes (19): arrow_well(), divider(), energy_well(), inv_grid(), main(), make_altar(), make_auto_fisher(), make_hydro() (+11 more)

### Community 19 - "MainViewModel"
Cohesion: 0.19
Nodes (8): bool, double, IBrush, LogLine, MainViewModel, int, string, ObservableCollection

### Community 20 - "HttpDownload"
Cohesion: 0.19
Nodes (10): Body, CookieContainer, HttpClient, HttpClientHandler, HttpDownload, CancellationToken, Task, SessionId (+2 more)

### Community 21 - "CasesMod.java"
Cohesion: 0.21
Nodes (8): com.google.gson.Gson, java.lang.reflect.Type, CasesMod, net.minecraftforge.event.RegisterCommandsEvent, net.minecraftforge.event.server.ServerStartingEvent, net.minecraftforge.fml.common.Mod, net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent, org.apache.logging.log4j.Logger

### Community 22 - "Fact"
Cohesion: 0.17
Nodes (7): AquaTechLauncher.Core.Tests, Fact, HttpResponseMessage, ManifestNeedsDownloadTests, SessionCookieParseTests, VersionCompareTests, ZipVerifyTests

### Community 23 - ".BuildAsync"
Cohesion: 0.24
Nodes (9): Func, JsonArray, JsonObject, LaunchCommandBuilder, ProcessSpawner, Action, CancellationToken, List (+1 more)

### Community 24 - "PlayerSyncHandler.java"
Cohesion: 0.18
Nodes (8): RecentWinsManager, WinEntry, Mod.EventBusSubscriber, PlayerSyncHandler, Context, RecentWinsSyncS2CPacket, net.minecraft.server.MinecraftServer, PlayerLoggedInEvent

### Community 25 - "net.minecraftforge.eventbus.api.SubscribeEvent"
Cohesion: 0.18
Nodes (10): BreakEvent, Mod.EventBusSubscriber, PersistenceHandler, Mod.EventBusSubscriber, QuestEventHandler, net.minecraftforge.event.entity.living.LivingDeathEvent, net.minecraftforge.event.entity.player.EntityItemPickupEvent, net.minecraftforge.event.server.ServerStoppingEvent (+2 more)

### Community 26 - "patches/patch_fawe_mohist.py"
Cohesion: 0.30
Nodes (12): _asm_classpath(), _ensure_patcher_compiled(), _java_bin(), _javac_bin(), main(), patch_ibukkit(), patch_typeproperty(), Path (+4 more)

### Community 27 - ".ApplyAsync"
Cohesion: 0.21
Nodes (11): Deleted, Failed, ManifestSync, PackFileEntry, PackManifest, Action, CancellationToken, HashSet (+3 more)

### Community 28 - "Path"
Cohesion: 0.10
Nodes (33): _app_dir(), _asset_object_path(), _bundle_dir(), ensure_assets(), ensure_forge_language_providers(), ensure_forge_minecraft_srg(), ensure_launcher_profiles(), find_bundled_forge_installer() (+25 more)

### Community 29 - "AquaTechLauncher.Core"
Cohesion: 0.12
Nodes (6): AquaTechLauncher.Core, FileNotFoundException, FileNotFoundError, BootstrapManifest, SessionStore, string

### Community 30 - ".EnsureJava17Async"
Cohesion: 0.17
Nodes (9): JavaLocator, Action, CancellationToken, string, Task, LauncherConstants, HashSet, int (+1 more)

### Community 31 - "ModCreativeTab.java"
Cohesion: 0.28
Nodes (9): ModCreativeTab, ModItems, ModSounds, net.minecraft.sounds.SoundEvent, net.minecraft.world.item.CreativeModeTab, net.minecraft.world.item.Item, net.minecraftforge.eventbus.api.IEventBus, net.minecraftforge.registries.DeferredRegister (+1 more)

### Community 32 - "PatchIUNoFreeScanner"
Cohesion: 0.22
Nodes (5): org.objectweb.asm.tree.AbstractInsnNode, org.objectweb.asm.tree.FieldInsnNode, org.objectweb.asm.tree.MethodNode, PatchIBukkit, PatchIUNoFreeScanner

### Community 33 - "AquaTechAPIHandler"
Cohesion: 0.21
Nodes (4): AquaTechAPIHandler, AquaTech Sync & Web Portal Backend API Server. Serves: 1) Client Pack Updates…, Top 5 AquaTech players leaderboard., Fetch live player online count from mcsrvstat API.

### Community 34 - "NetworkHandler"
Cohesion: 0.17
Nodes (10): MinecraftServer, MenuOpenerItem, ServerPlayer, NetworkHandler, net.minecraft.world.entity.player.Player, net.minecraft.world.InteractionHand, net.minecraft.world.InteractionResultHolder, net.minecraft.world.level.Level (+2 more)

### Community 35 - "ForgeInstaller"
Cohesion: 0.44
Nodes (4): ForgeInstaller, Action, CancellationToken, Task

### Community 36 - "KitManager"
Cohesion: 0.24
Nodes (3): KitDefinition, KitItem, KitManager

### Community 37 - "gen_sc_fishing_ui.py"
Cohesion: 0.42
Nodes (14): draw_bar_fill(), draw_bar_outline(), draw_key(), draw_key_hot(), draw_pointer(), draw_rod(), draw_spark(), draw_spot() (+6 more)

### Community 38 - "Улучшенная — Улучшенная электрическая эра"
Cohesion: 0.02
Nodes (105): adamantium ← `mars_pebble`, adv rover ← `dimethylhydrazine`, advanced hull machine ← `bloodstone`, antiairpollution ← `double_molecular`, antisoilpollution ← `double_molecular`, azurebrilliant ← `radioprotector`, bloodstone ← `mimas_pebble`, construction foam ← `cooling_mixture` (+97 more)

### Community 39 - "setup_apex_mysql.py"
Cohesion: 0.24
Nodes (15): apex_command(), _apex_headers(), apex_json(), apex_power(), apex_state(), apply_schema(), load_or_create_secrets(), main() (+7 more)

### Community 40 - ".PlayAsync"
Cohesion: 0.31
Nodes (7): IReadOnlyList, LauncherConfig, PlayOrchestrator, Action, CancellationToken, Task, Process

### Community 41 - "CasesMod — Кейсы, Киты, Варпы, Квесты, Привилегии (Forge 1.20.1)"
Cohesion: 0.15
Nodes (12): CasesMod — Кейсы, Киты, Варпы, Квесты, Привилегии (Forge 1.20.1), UX-концепт: Liquid Glass, Анимация открытия кейса, Все команды, Интеграция с донат-магазином, Кастомизация кейсов, Кастомизация китов / варпов / квестов, Открытие меню игроком (+4 more)

### Community 42 - "build_launch_cmd"
Cohesion: 0.15
Nodes (15): build_launch_cmd(), ensure_default_russian_options(), ensure_vanilla_client_jar(), ensure_vanilla_version_json(), _forge_lang_provider_specs(), _http_json(), load_merged_version(), patch_forge_version_json_lang_providers() (+7 more)

### Community 43 - "FishSellService.java"
Cohesion: 0.14
Nodes (5): FishSellService, BalanceSyncS2CPacket, Context, C2SSellFishPacket, Context

### Community 44 - "setup_d1.py"
Cohesion: 0.35
Nodes (12): account(), api(), apply_sql(), bind_pages(), ensure_db(), list_dbs(), main(), patch_wrangler() (+4 more)

### Community 45 - "AquaTech: Ocean Horizon — Changelog (beta)"
Cohesion: 0.17
Nodes (11): 2026-07-28 — AquaTech UI hub expansion, 2026-07-28 — Machine GUIs + progression logic, 2026-07-28 — Roadmap completion pass, 2026-07-29 — Маршрут Горизонта (meta road), 2026-07-30 — Horizon Phase 1 (видимая дорога), 2026-08-03 — Back to ocean world (no SkyblockBuilder), 2026-08-03 — IU ocean raft rebuild, 2026-08-03 — Raft + Industrial Survival concept sync (+3 more)

### Community 46 - "dependencies"
Cohesion: 0.17
Nodes (11): @cloudflare/workerd-windows-64, @esbuild/win32-x64, playwright, dependencies, @cloudflare/workerd-windows-64, @esbuild/win32-x64, playwright, wrangler (+3 more)

### Community 47 - "AquaTechLauncher.ViewModels"
Cohesion: 0.15
Nodes (8): Application, AquaTechLauncher.Views, AquaTechLauncher.ViewModels, App, ViewModelBase, MainWindow, ObservableObject, Window

### Community 48 - ".CheckAndApplyAsync"
Cohesion: 0.20
Nodes (8): InlineData, LauncherSelfUpdate, Action, CancellationToken, Task, Updated, Message, Theory

### Community 49 - "Маршрут Горизонта — AquaTech meta-progression"
Cohesion: 0.18
Nodes (10): FTB, Варпы, Горизонты → LuckPerms, Команды админа, Команды игрока, Контракты дня, Маршрут Горизонта — AquaTech meta-progression, Сезон (+2 more)

### Community 50 - "CaseListSyncS2CPacket"
Cohesion: 0.33
Nodes (5): CaseListSyncS2CPacket, CaseSnapshot, ItemSnapshot, Context, ServerPlayer

### Community 52 - "gen_stardew_rhythm_ui.py"
Cohesion: 0.51
Nodes (10): draw_bar(), draw_bubble_frames(), draw_dial(), draw_gem(), draw_needle(), draw_panel(), main(), px() (+2 more)

### Community 53 - "test_launcher_smoke.py"
Cohesion: 0.45
Nodes (10): _fail(), find_java(), find_sync_base(), main(), _ok(), Path, Local smoke tests for the LEGACY Python launcher (tools/aquatech_launcher.py).…, test_forge_launch() (+2 more)

### Community 54 - "publish_client_pack.py"
Cohesion: 0.36
Nodes (10): asset_name(), main(), md5_file(), Path, Build AquaTech client pack + manifest for online CDN (website + GitHub…, Prefer repo roots, fall back to existing pack., should_skip(), sync_config_kube_resources() (+2 more)

### Community 55 - "upload_to_github.py"
Cohesion: 0.20
Nodes (6): collect_files(), md5_file(), Path, AquaTech GitHub Release Uploader Загружает все моды и файлы сборки в GitHub…, Upload all files to GitHub Release, return {rel_path: download_url}, upload_release()

### Community 56 - "MenuCatalogSyncS2CPacket"
Cohesion: 0.44
Nodes (5): Context, KitSnap, MenuCatalogSyncS2CPacket, QuestSnap, WarpSnap

### Community 57 - "🗺️ Полный Роадмап Развития Игрока: AquaTech (Minecraft 1.20.1)"
Cohesion: 0.20
Nodes (9): 🌊 АКТ 1. Плот выжившего & Примитивный улов (Primal Era), 🧱 АКТ 2. Мультиблочная плавильня & Прокатка (Early LV Era), 💨 АКТ 3. Паровые котлы & Первое электричество (Steam Era ➔ LV 32 EU/t), ⚙️ АКТ 4. Индустриальная Доменная Печь & Сплавы (MV Era — 128 EU/t), ☢️ АКТ 5. Химический синтез & Ядерная энергетика (HV/EV Era — 512–2048 EU/t), 🌌 АКТ 6. Генераторы материи & Квантовый эндгейм (Quantum & Cosmic Era), 🎯 Главная концепция выживания, 📊 Матрица Дорожной Цепочки Удочек (+1 more)

### Community 58 - "Mod Development Guidelines"
Cohesion: 0.22
Nodes (8): 1. Registration API & Mod ID, 2. GUI Architecture (Menu + Screen Separation), 3. Network Synchronization, 4. Models, Visuals & Language Keys, 5. Mohist Compatibility, Mod Development Guidelines, Project Rules & Development Standards: Minecraft 1.20.1 (Forge + Mohist), Technical Architecture & Environment

### Community 59 - "app.js"
Cohesion: 0.32
Nodes (13): afterLogin(), appendLogs(), applyCfg(), boot(), ensureAuthed(), paint(), paintAccount(), paintPackBanner() (+5 more)

### Community 60 - "bootstrap_p0_local.py"
Cohesion: 0.43
Nodes (7): main(), md5(), patch_fawe(), Path, One-shot: patch FAWE in scratch/ + write .apex_deploy.json from terminal logs., recover_secrets(), write_secrets()

### Community 61 - "Продвинутая — Продвинутая электрическая эра"
Cohesion: 0.02
Nodes (94): antiairpollution1 ← `cooling`, antisoilpollution1 ← `cooling`, autoheater ← `coolupgrade`, draconid ← `ariel_pebble`, graviTool ← `imp_alloy_smelter`, hive ← `squeezer`, iron hammer ← `primal_wire_insulator`, module quickly ← `substitute` (+86 more)

### Community 62 - "Электрика — baseElectric"
Cohesion: 0.03
Nodes (60): acetylene ← `calcium_carbide`, base machines ← `solid_refrigerator`, bromine ← `propane`, gas ← `gas_sensor`, nitrate dust ← `orewashing`, nitricacid ← `nitrogendioxide`, nitrogen ← `item_divider`, nitrogendioxide ← `nitrogenoxy` (+52 more)

### Community 63 - "generate_600_ocean_quests.py"
Cohesion: 0.44
Nodes (7): chapter_file(), data_snbt(), esc(), groups_snbt(), main(), quest_snbt(), tag_task()

### Community 64 - "install_oraxen_rank_prefixes.py"
Cohesion: 0.39
Nodes (7): copy_textures(), ensure_extra_groups(), glyph_char(), main(), upsert_lp_group(), write_char_map(), write_glyphs_yml()

### Community 65 - "manifest.json"
Cohesion: 0.25
Nodes (7): cdn, files, forge_version, mc_version, server_ip, server_port, version

### Community 66 - "patch_chapter"
Cohesion: 0.39
Nodes (7): ensure_quest_tags(), main(), patch_chapter(), Path, Set top-level chapter field (one tab indent)., Add theme tag to each quest. After FTB 4.22 resave, quest fields use 3 tabs., set_or_replace_field()

### Community 67 - "Minimalist Minecraft Pixel Art GUI Skill"
Cohesion: 0.29
Nodes (6): 1. Minimalist Tech Design Principles (Auto-Fisher Style), 2. Advanced Slot & Logic Architecture, 3. Standard Layout Bounds ($176 \times 176$ px Canvas), 4. Forge 1.20.1 Render Code Pattern, Auto-Fisher / Processing Container Logic:, Minimalist Minecraft Pixel Art GUI Skill

### Community 68 - ".calculatePrice"
Cohesion: 0.36
Nodes (3): FishPriceCalculator, PriceResult, net.minecraft.world.item.ItemStack

### Community 72 - "launcher_tests/test_bootstrap_update.py"
Cohesion: 0.49
Nodes (10): _fail(), _get_json(), _head_ok(), main(), _ok(), test_check_launcher_update_available_shape(), test_go_needs_update_via_subprocess(), test_live_bootstrap_reachable() (+2 more)

### Community 74 - "AquaTechLauncher"
Cohesion: 0.18
Nodes (6): Control, AquaTechLauncher, IDataTemplate, Kind, UiSounds, ViewLocator

### Community 75 - "Quest ID Freeze (AquaTech Beta)"
Cohesion: 0.29
Nodes (6): Capstones / bridges, Horizon Route (2026-07-29), Quest ID Freeze (AquaTech Beta), Safe changes, Validation, Workshops / Мастерские (2026-08-02)

### Community 76 - "reorganize_layout.py"
Cohesion: 0.43
Nodes (6): deepen_repo_root(), main(), move(), Path, One-shot layout pass: root clutter -> scripts/, tools/ by domain. Safe to re-…, tools/foo.py used parents[1]; tools/domain/foo.py needs parents[2].

### Community 77 - "sync_lodestone_mods.py"
Cohesion: 0.52
Nodes (6): find_lodestone_mods(), jars_for_prefix(), md5_file(), Path, Push first-party jars from server/mods into the live Lodestone instance. Keeps…, sync()

### Community 78 - "AquaTech Release & Development Workflows"
Cohesion: 0.15
Nodes (12): 1. Player-facing changes, 2. Portal changes (`docs/`, `worker/`, etc.), 3. After code edits, 4. After Apex server deploy, 5. FTB Quests safety, 6. Git Commits & Push Policy, Anti-AI-Slop Rules (Always On), AquaTech — agent guidance (+4 more)

### Community 79 - "RelayCommand"
Cohesion: 0.17
Nodes (3): Kind, Task, RelayCommand

### Community 83 - "AquaTech"
Cohesion: 0.33
Nodes (5): AquaTech, Cleanup, Layout, scripts/, tools/

### Community 84 - "gen_rhythm_textures.py"
Cohesion: 0.53
Nodes (4): make_dot(), make_key(), make_star(), px()

### Community 85 - "generate_resource_rods.py"
Cohesion: 0.47
Nodes (5): loot_table(), main(), Path, Generate AquaTech resource fishing rods datapack (10 tiers). The datapack…, write()

### Community 86 - "slice_rhythm_ui.py"
Cohesion: 0.53
Nodes (5): crop_save(), key_black(), main(), Image, Slice generated fishing UI layout sheet into PNG assets.

### Community 87 - "generate_spine_quests.py"
Cohesion: 0.60
Nodes (4): chapter_snbt(), esc(), main(), quest_block()

### Community 88 - "download_industrial_upgrade.py"
Cohesion: 0.53
Nodes (5): api_get(), download_file(), main(), pick_version(), Path

### Community 89 - "download_opt_mods.py"
Cohesion: 0.53
Nodes (5): api_get(), download(), main(), pick_version(), Path

### Community 90 - "upload_launcher_release.py"
Cohesion: 0.53
Nodes (5): api(), main(), md5_file(), Path, token()

### Community 91 - "Program"
Cohesion: 0.40
Nodes (3): AppBuilder, Program, STAThread

### Community 93 - "Пар — steam"
Cohesion: 0.05
Nodes (39): fluidcoppersulfate ← `sulfurtrioxide`, oxygen ← `steam_electrolyzer`, silicon crystal ← `steampressureconverter`, steam machine block, steel hammer ← `steel`, sulfurtrioxide ← `primal_gas_chamber`, titanium steel ← `steam_crystal_charge`, Газовая камера ← `oxygen` (+31 more)

### Community 94 - "make_rate"
Cohesion: 0.60
Nodes (4): draw_digit(), main(), make_rate(), Image

### Community 95 - "finish_pack_release.py"
Cohesion: 0.80
Nodes (4): api(), find_release(), main(), token()

### Community 96 - "strip_op_quest_rewards.py"
Cohesion: 0.50
Nodes (3): main(), patch_file(), Path

### Community 97 - "install_kubejs.py"
Cohesion: 0.70
Nodes (4): download(), install(), main(), Path

### Community 98 - "make_boot_fixes_datapack.py"
Cohesion: 0.60
Nodes (4): main(), Path, tag_path(), write_tag()

### Community 99 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 102 - "cf_token_playwright.js"
Cohesion: 0.50
Nodes (3): { chromium }, fs, path

### Community 103 - "upload_ftbquests_assets.py"
Cohesion: 0.83
Nodes (3): api(), main(), token()

### Community 105 - "download"
Cohesion: 0.67
Nodes (3): download(), main(), Path

### Community 106 - "upload_pack_release.py"
Cohesion: 0.83
Nodes (3): api(), main(), token()

### Community 108 - "Дополнительные тексты guide.* / quarry.guide.*"
Cohesion: 0.06
Nodes (31): `guide.chemicalplant`, `guide.chemicalplant1`, `guide.chemicalplant2`, `guide.chemicalplant3`, `guide.geothermalpump`, `guide.geothermalpump1`, `guide.geothermalpump2`, `guide.geothermalpump3` (+23 more)

### Community 110 - "scripts/"
Cohesion: 0.40
Nodes (4): ApexNodes deploy, ApexNodes MariaDB, scripts/, Правила для агента

### Community 187 - "Примитив — Примитивная эра"
Cohesion: 0.08
Nodes (25): electrum ← `smelteryforms`, flint dust ← `macerator`, molot ← `ferromanganese`, raw latex ← `dryer`, silicon handler ← `flint_dust`, steam ← `primal_heater`, superheated steam ← `steam`, Информация о примитивной эре (+17 more)

### Community 188 - "Совершенная — perElectric"
Cohesion: 0.08
Nodes (25): solid matter ← `auto_digger`, xenon ← `proteus_pebble`, Административная солнечная панель ← `research_lens_6`, Водяной преобразователь ← `ender_assembler`, Воздушный преобразователь ← `earth_assembler`, Земляной преобразователь ← `nether_assembler`, Исследовательская линза VI ← `xenon`, Камешек с Протея ← `photon_hull_plate` (+17 more)

### Community 189 - "Обзор — Основная информация"
Cohesion: 0.08
Nodes (25): Виды энергии, Вулканы, Гевея, Жители, Загрязнение воздуха и почвы, Колонии, Космос, Механический рецептор (+17 more)

### Community 190 - "FishMarketNpcInteract"
Cohesion: 0.33
Nodes (4): EntityInteract, FishMarketNpcInteract, Mod.EventBusSubscriber, net.minecraft.world.entity.Entity

### Community 191 - "deploy_apexnodes_sftp.py"
Cohesion: 0.11
Nodes (32): main(), wg_flag_cmd(), apex_command(), apex_create_backup(), apex_delete_backup(), _apex_headers(), apex_json(), apex_list_backups() (+24 more)

### Community 192 - "LauncherEngine"
Cohesion: 0.09
Nodes (12): BaseException, BaseHTTPRequestHandler, ApiHandler, _friendly_portal_error(), LauncherEngine, _portal_login_page_url(), _portal_post(), Path (+4 more)

### Community 193 - ".PingAsync"
Cohesion: 0.33
Nodes (4): ServerPing, Task, Ms, Online

### Community 194 - "net.minecraft.network.FriendlyByteBuf"
Cohesion: 0.13
Nodes (7): CaseResultS2CPacket, Context, Context, OpenFishMarketS2CPacket, Context, OpenMenuS2CPacket, net.minecraft.network.FriendlyByteBuf

### Community 197 - "Q: Install gstack caveman graphify for AquaTech Cursor"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Install gstack caveman graphify for AquaTech Cursor, Source Nodes

### Community 198 - "check_aquateche_domain.py"
Cohesion: 0.60
Nodes (4): api(), main(), Print Cloudflare NS for aquateche.store and check zone/domain status., token()

### Community 199 - "Industrial Upgrade — руководство из мода"
Cohesion: 0.50
Nodes (3): Industrial Upgrade — руководство из мода, Вкладки, Книга-гайд (предмет)

### Community 205 - "replace_release_bootstrap_exe.py"
Cohesion: 0.83
Nodes (3): api(), main(), token()

### Community 209 - "_java_major_version"
Cohesion: 0.50
Nodes (3): find_java(), _java_major_version(), Return major version (17, 21, 25...) or None.

## Knowledge Gaps
- **621 isolated node(s):** `aquatech/bootstrap`, `version`, `mc_version`, `forge_version`, `server_ip` (+616 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Industrial Upgrade — руководство из мода` connect `Industrial Upgrade — руководство из мода` to `Улучшенная — Улучшенная электрическая эра`, `Дополнительные тексты guide.* / quarry.guide.*`, `Пар — steam`, `Обзор — Основная информация`, `Примитив — Примитивная эра`, `Совершенная — perElectric`, `Продвинутая — Продвинутая электрическая эра`, `Электрика — baseElectric`?**
  _High betweenness centrality (0.013) - this node is a cross-community bridge._
- **Why does `Улучшенная — Улучшенная электрическая эра` connect `Улучшенная — Улучшенная электрическая эра` to `Industrial Upgrade — руководство из мода`?**
  _High betweenness centrality (0.008) - this node is a cross-community bridge._
- **Why does `Электрика — baseElectric` connect `Электрика — baseElectric` to `Industrial Upgrade — руководство из мода`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **What connects `aquatech/bootstrap`, `version`, `mc_version` to the rest of the system?**
  _621 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `index.js` be split into smaller, more focused modules?**
  _Cohesion score 0.08516483516483517 - nodes in this community are weakly interconnected._
- **Should `ZipFile` be split into smaller, more focused modules?**
  _Cohesion score 0.05009009009009009 - nodes in this community are weakly interconnected._
- **Should `CaseManager` be split into smaller, more focused modules?**
  _Cohesion score 0.10099573257467995 - nodes in this community are weakly interconnected._