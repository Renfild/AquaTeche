# 🗺️ Master Implementation Plan: AquaTech Minecraft 1.20.1 (Forge + Mohist)

> [!IMPORTANT]
> **ОБЯЗАТЕЛЬНОЕ ТРЕБОВАНИЕ К АРХИТЕКТУРЕ И ВЕДЕНИЮ ПРОЕКТА:**
> При внесении любых изменений, доработок, добавлении новых функций, изменении крафтов, модов или скриптов **настоящий Implementation Plan ДОЛЖЕН БЫТЬ ОБНОВЛЕН**. 
> Все изменения обязаны фиксироваться в **Разделе 6 (Журнал Ревизий и История Изменений)** с указанием даты, версии, сути изменений и затронутых файлов. Это позволяет оценить текущее состояние архитектуры и историю проекта при открытии рабочей копии на любых других машинах или другими разработчиками/AI-агентами.

---

## 🎯 Общий обзор архитектуры проекта

Проект **AquaTech** представляет собой высокотехнологичную океаническую сборку на базе **Minecraft 1.20.1 (Forge MDK 47.x + гибридный сервер Mohist)**. 

### Основная концепция сборки:
Игрок начинает выживание посреди бесконечного океана без традиционных подземных шахт. Вся добыча руд, металлов, органики и компонентов **Industrial Upgrade (IU)** строится на:
1. Многоуровневой системе рыбалки (**Resource Rods & StarCatcher**).
2. Автоматизации улова через **Авторыболов (`aquatech_ui:auto_fisher`)**, океанические фильтры и драги.
3. Сложной промышленной цепочке переработки в **Industrial Upgrade (IU)** и **Applied Energistics 2 (AE2)**.

---

## 🏗️ Разделение на Технические Задания (ТЗ)

---

### 📋 ТЗ-1: Разработка Ядерного Мода AquaTech UI (`net.aquatech.ui`)

#### 📌 Цель:
Создание уникального Forge-мода `aquatech_ui` для обработки механик рыбалки, прокачки навыков океана, кастомных интерфейсов (GUI/HUD) и синхронизации пакетов сервер-клиент.

#### ⚙️ Поэтапная реализация ТЗ-1:
* **Этап 1.1: Регистрация API и Предметов.**
  * Создание `DeferredRegister<Item>` и `RegistryObject<Item>` в `ModItems.java`.
  * Регистрация удочек новичка, железа, призмарина, бездны, термальной и эндер-удочки.
  * Регистрация наживок (`speed_tackle`, `luck_tackle`, `depth_tackle`) и приманок.
  * Регистрация апгрейдов (`speed_upgrade`, `efficiency_upgrade`, `double_hook_upgrade`).
* **Этап 1.2: Механика перехвата улова (`FishingLootHandler.java`).**
  * Перехват Forge-события `ItemFishedEvent` (приоритет `HIGHEST`).
  * Реализация логики выдачи ресурсов Industrial Upgrade в зависимости от удочки (`rollStarCatcherRodLoot`).
  * Поддержка умножителей скорости/количества улова и начисление Очков Навыков Океана (`AquaSkillCapability`).
* **Этап 1.3: Адаптер совместимости StarCatcher (`FishingRodCompat.java`).**
  * Интеграция 12 специализированных удочек StarCatcher (`naturalist_rod`, `starcatcher_rod`, `obsidian_rod`, `lush_glowberry_rod`, `magmaforged_rod`, `bamboo_rod`, `good_old_rod`, `slimed_rod`, `iceborn_rod`, `sharktooth_rod`, `azure_crystal_rod`, `alpha_rod`).
  * Назначение уникальных тематических пулов вылавливания ресурсов для каждой из 12 удочек.
* **Этап 1.4: Автоматизация (Авторыболов `AutoFisherBlockEntity.java`).**
  * Создание блочного контекста `AbstractContainerMenu` и `AbstractContainerScreen` для `auto_fisher`.
  * Реализация автоматического цикла ловли с поддержкой карт ускорения и фильтров.
  * Выход **3×2** (6 слотов); остаток улова → соседний инвентарь → дроп рядом с блоком (без void при ×8+).
  * GUI: удочка/апгрейд слева, стрелка прогресса по центру, сетка дропа справа (`auto_fisher.png`).
* **Этап 1.5: Прочность удочек StarCatcher (`RodDurability` + KubeJS).**
  * `RodDurability.java` / `RodDurabilityApplier.java`: лимиты уловов, износ на ручной ловле и AF, force-`maxDamage` через Unsafe (KubeJS `item.maxDamage =` на raw Item часто no-op).
  * Клиент: полоска `RodDurabilityBar` (всегда видна) + тултип «Осталось уловов: X / Y».
  * `kubejs/startup_scripts/40_rod_durability.js`: `item.setMaxDamage(N)` — ранние 128, середина 192, поздние 256, топ 320.

---

### 📋 ТЗ-2: Разработка Слой Меню и Экономики (`com.casesmod`)

#### 📌 Цель:
Создание сервено-клиентского Forge-мода `casesmod` для обработки меню F4, выпадающих кейсов, игровых наборов (китов), начисления опыта, личных аккаунтов и варпов.

#### ⚙️ Поэтапная реализация ТЗ-2:
* **Этап 2.1: Конфигурационный слой JSON (`config/casesmod/`).**
  * Разработка схем конфигурации: `cases.json`, `kits.json`, `quests.json`, `warps.json`.
  * Определение весов, категорий редкости (COMMON, UNCOMMON, RARE, EPIC, LEGENDARY) и команд выгоды.
* **Этап 2.2: Исполнительное ядро `CasesMod.java`.**
  * Регистрация пакетов сетевой синхронизации `MenuCatalogSyncS2CPacket`, `OpenCaseC2SPacket`.
  * Реализация системы аккаунтов игроков `PlayerAccountManager`.
* **Этап 2.3: Графический интерфейс и анимации.**
  * Отрисовка рулетки кейсов и предпросмотра наборов.

---

### 📋 ТЗ-3: Настройка Игрового Баланса и Рецептов KubeJS (`kubejs/server_scripts/`)

#### 📌 Цель:
Полный контроль игрового баланса, создание цепочек крафта, блокировка ломающих игру предметов и адаптация рецептов под океанический сеттинг.

#### ⚙️ Поэтапная реализация ТЗ-3:
* **Этап 3.1: Блокировка читерских предметов (`00_disable_quarries.js` & `10_aquatech_iu_nerf.js`).**
  * Отключение карьеров, телепортов, бесконечных креативных предметов и мгновенных авто-шахтеров.
* **Этап 3.2: Цепочка прогрессивного крафта 12 удочек (`20_aquatech_rod_crafts.js`).**
  * Создание усложненной ступенчатой цепочки рецептов (Tier 1 ➔ Tier 13): `humble_rod` ➔ `bamboo_rod` ➔ `good_old_rod` ➔ `naturalist_rod` ➔ `slimed_rod` ➔ `iceborn_rod` ➔ `starcatcher_rod` ➔ `azure_crystal_rod` ➔ `sharktooth_rod` ➔ `obsidian_rod` ➔ `lush_glowberry_rod` ➔ `magmaforged_rod` ➔ `alpha_rod`.
  * Требование предыдущей удочки в крафте + уникальных материалов эпохи Industrial Upgrade (схемы, сплавы, уран, осмиридий).
* **Этап 3.2b: Баланс лута T1/T2 + тултипы + фантомы.**
  * T1 `bamboo_rod`: титан убран; добавлен дроп `minecraft:bamboo`; T2 `humble_rod`: титан **60%**.
  * `kubejs/client_scripts/add_fishing_resource_tooltips.js` синхронизирован с `rollStarCatcherRodLoot` (earliest tier).
  * `60_disable_phantoms.js`: `doInsomnia false` + cancel spawn phantom.
* **Этап 3.3: Кастомные крафты сборки (`30_aquatech_crafting.js`).**
  * Адаптация рецептов контроллеров и накопителей AE2 (`ae2:controller`, `ae2:drive`).
  * Рецепты лодки Кикстартера, апгрейдов скорости, океанических фильтров, драги дна и гидрореактора.

---

### 📋 ТЗ-4: Система Датапаков Океанического Фарма (`aquatech_resource_rods`)

#### 📌 Цель:
Перехват стандартной ловли рыбы датапаком для выдачи тировых ресурсов в зависимости от NBT-тега удочки `SelectedItem.tag.aqRodTier`.

#### ⚙️ Поэтапная реализация ТЗ-4:
* **Этап 4.1: Генератор датапаков (`tools/generate_resource_rods.py`).**
  * Настройка 10 тиров таблиц лута (`loot_tables/rod_tier_1..10.json`).
  * Командный скрипт `data/aqrod/functions/catch/convert.mcfunction` для утилизации ванильной рыбы и мусора в радиусе 6 блоков.
* **Этап 4.2: Синхронизация.**
  * Автоматический экспорт датапака в `server/world/datapacks/` и шаблоны мира.

---

### 📋 ТЗ-5: Система Квестов и Обучения Игроков (FTB Quests)

#### 📌 Цель:
Создание понятной визуальной цепочки квестов в FTB Quests, направляющей игрока через все 7 эпох Industrial Upgrade и механики AquaTech.

#### ⚙️ Поэтапная реализация ТЗ-5:
* **Этап 5.1: Интеграция руководства Industrial Upgrade (`tools/gen_iu_guide_ftbquests.py`).**
  * Парсинг Bytecode мода IU и генерация главы квестов `2F_ws_industrial_upgrade.snbt`.
* **Этап 5.2: Настройка подсказок и вылавливания (`tools/patch_iu_fishing_hints.py`).**
  * Внедрение подсказок про вылавливание руд, латекса, гевеи и нефти удочками AquaTech.

---

### 📋 ТЗ-6: Автоматизация Сборки и Матрица Деплоя (`deploy_*.ps1`)

#### 📌 Цель:
Обеспечение моментального билда, проверки контрольных сумм MD5 и синхронизации файлов между всеми клиентами и серверами.

#### ⚙️ Поэтапная реализация ТЗ-6:
* **Этап 6.1: Скрипт деплоя мода `deploy_aquatech_ui.ps1`.**
  * Автоматический билд `gradlew build` в `mods/aquatech-ui`.
  * Распространение сгенерированного `.jar` по 7 директориям (сервер, клиент, CurseForge).
* **Этап 6.2: Скрипт деплоя скриптов `deploy_runtime.ps1`.**
  * Синхронизация папок `kubejs/`, модов `rhino`, `recipe_generator` и `blueprint` между всеми инстансами.
* **Этап 6.3: Клиентский лаунчер + онлайн-обновления пака (актуально).**
  * Bootstrap: `dist/releases/AquaTech.exe` + onedir zip `AquaTechLauncher.zip` (`docs/bootstrap.json`).
  * Актуальные теги (2026-08-11): pack **2.9.18**, launcher **client-2.9.29**, `aquatech_ui` **1.0.12**.
  * Lodestone больше не синхронизируем. Живой хост — Apex; клиенты берут банки из CDN-пака.
  * Пак: `python tools/publish_client_pack.py` → `upload_pack_release.py`; лаунчер: `dotnet publish` + Go bootstrap + `upload_launcher_release.py`.

#### 🔄 Как обновлять клиентскую сборку (чеклист)

**Ты (локально):**
1. Меняешь моды / kubejs / конфиги (CurseForge или `server/`).
2. `python C:\Users\xieto\Desktop\AquaTech\tools\publish_client_pack.py`  
   → пересобирает `dist/AquaTech-Client` + `manifest.json` (MD5).
3. Запускаешь `dist\releases\AquaTechLauncher.exe`  
   → в логе: «Локальная сборка: …\AquaTech-Client»  
   → качает/копирует только изменённые файлы.

**Друзья — онлайн (без zip):**
1. После правок снова `publish_client_pack.py`.
2. `start_sync_server.bat` (HTTP на порту **8080**, раздаёт `dist/AquaTech-Client`).
3. Playit.gg: отдельный **TCP → 8080** (игра 25565 — другой тоннель).
4. Друзьям в лаунчере поле **«URL обновлений»** = публичный Playit URL  
   (или `update_url.txt` рядом с exe).
5. Кнопка **«Обновить»** или **«Играть»** — синк по MD5.

**Друзья — разово без сети:** скопировать папку `dist\releases\` целиком  
(`AquaTechLauncher.exe` + `AquaTech-Client\`).

**Опционально позже (GitHub):**  
`gh release upload modpack-latest dist/AquaTech-Client/mods/*.jar --clobber`  
+ запушить `manifest.json` в репо.

---

## 📜 Журнал Ревизий и История Изменений (Revision History)

> [!NOTE]
> Любой разработчик или AI-агент, вносящий изменения в проект, обязан добавлять новую запись в конец данной таблицы.

| Дата | Ревизия | Автор | Суть внесенных изменений | Затронутые файлы |
|---|---|---|---|---|
| **2026-08-03** | `v1.0.0` | Antigravity AI | Первоначальное создание мода AquaTech UI, настройка гибридного сервера Mohist и FTB Quests. | `mods/aquatech-ui`, `server/` |
| **2026-08-04** | `v1.1.0` | Antigravity AI | Интеграция 12 удочек StarCatcher в `FishingRodCompat` и `FishingLootHandler`. Добавление улова руд и сплавов Industrial Upgrade. | `FishingRodCompat.java`, `FishingLootHandler.java` |
| **2026-08-04** | `v1.2.0` | Antigravity AI | Разработка ступенчатой цепочки рецептов KubeJS (`20_aquatech_rod_crafts.js`) для 12 удочек StarCatcher. | `kubejs/server_scripts/20_aquatech_rod_crafts.js` |
| **2026-08-04** | `v1.3.0` | Antigravity AI | Создание глобального мастера-плана реализации (`IMPLEMENTATION_PLAN.md`) с ТЗ и правилом постоянной актуализации. | `IMPLEMENTATION_PLAN.md`, `implementation_plan.md` |
| **2026-08-05** | `v1.4.0` | Antigravity AI | Подготовка к TikTok: 3D-анимация `AutoFisherRenderer`, цветные подсказки удова, обновление `ru_ru.json` / `en_us.json`. | `AutoFisherRenderer.java`, `ModBusClientEvents.java`, `ru_ru.json`, `en_us.json` |
| **2026-08-05** | `v1.5.0` | Antigravity AI | Добавление подписей удочек для всех ванильных и IU руд в `FishingOreTooltips.java` и удаление устаревших описаний жилок IU. | `FishingOreTooltips.java`, `IMPLEMENTATION_PLAN.md` |
| **2026-08-05** | `v1.5.1` | Antigravity AI | Удаление 3D-анимации левитирующей удочки и частиц брызг над `AutoFisherBlockEntity` по запросу. | `AutoFisherRenderer.java`, `IMPLEMENTATION_PLAN.md` |
| **2026-08-05** | `v1.6.0` | Antigravity AI | Полное удаление предмета «Тризубец Нептуна» (`neptune_trident`) из мода `aquatech_ui`, дропов, Алтаря Океана, креативной вкладки и кейсов. | `ModItems.java`, `OceanAltarBlockEntity.java`, `FishingLootHandler.java`, `legend_case.json` |
| **2026-08-05** | `v1.7.0` | Antigravity AI | Полная очистка старых квестов (`ftbquests`), бэкапов квестов, паркованных модов (`_parked_mods`) и временных папок (`_tmp_*`) для создания новой ветки квестов «с нуля». | `config/ftbquests`, `server/config/ftbquests`, `_parked_mods` |
| **2026-08-05** | `v1.7.1` | Antigravity AI | Расширен фильтр `FishingOreTooltips` для полного вырезания строк подсказок жилок IU (`жилу`, `ищите камни`, `добывается только`, `[Shift]`) и форматирования названий удочек. | `FishingOreTooltips.java`, `IMPLEMENTATION_PLAN.md` |
| **2026-08-05** | `v1.7.2` | Antigravity AI | Установлен приоритет `EventPriority.LOWEST` в `FishingOreTooltips`, добавлен нечеткий поиск материалов по ID руд и импортирован логотип `aquatech_logo.png` в KubeJS текстуры для квестов. | `FishingOreTooltips.java`, `kubejs/assets/kubejs/textures/` |
| **2026-08-05** | `v1.7.3` | Antigravity AI | Восстановлена привязка 20 PNG-иконок префиксов/рангов из папки «Пак красивых префиксов» в битовая мапу шрифтов `minecraft/font/default.json`, LuckPerms и ресурсы мода `aquatech_ui`. | `tools/install_aquatech_rank_prefixes.py`, `default.json`, `aquatech_ui-1.0.0.jar` |
| **2026-08-05** | `v1.7.4` | Antigravity AI | Добавлены расширенные двойные пробелы между всеми словами подписей в `FishingOreTooltips.java` для устранения визуального слипания слов при юникод-шрифте. | `FishingOreTooltips.java`, `aquatech_ui-1.0.0.jar` |
| **2026-08-05** | `v1.7.5` | Antigravity AI | Отвязаны бинды клавиш `I` (Industrial Upgrade) и `O` (Avaritia config) в `options.txt`, добавлен перехват `ClientChatReceivedEvent` для глушения приветственного сообщения IU в чате при входе. | `options.txt`, `ClientEvents.java`, `aquatech_ui-1.0.0.jar` |
| **2026-08-05** | `v1.7.6` | Antigravity AI | Стандартизирована единая энергосистема FE (Forge Energy) для механизмов AquaTech, Draconic Evolution, Avaritia и AE2; обновлены тексты интерфейса с EU/t на FE/t. | `ru_ru.json`, `en_us.json`, `aquatech_ui-1.0.0.jar` |
| **2026-08-06** | `v1.8.0` | Antigravity AI | Обновлен GUI Рынка Рыбы (`fish_market.png`), открытие сделано через NPC, исправлен сборщик `reobfJar` под Java 17 (фикс `NoSuchFieldError`), добавлены русские варпы, `/casesmod listwarps` и команды добавления денег/привилегий в кейсы (`/casesmod addcasemoney`, `/casesmod addcasecmd`). | `FishMarketScreen.java`, `MainMenuScreen.java`, `MenuCommands.java`, `casesmod/build.gradle` |
| **2026-08-06** | `v1.8.1` | Antigravity AI | Обновлена типографика GUI Рынка Рыбы: текст кнопок заменен с черного/капса на яркий белый без теней (`dropShadow = false`), отключены синие прямоугольники при наведении (`GuiHotspot`), отформатирован баланс («9 855 979 дублонов»). | `FishMarketScreen.java`, `GuiHotspot.java` |
| **2026-08-06** | `v1.8.2` | Antigravity AI | Обновлена финальная текстура `fish_market.png` GUI Скупщика Рыбы, пересобрана JAR и задеплоена во все клиентские и серверные директории. | `fish_market.png`, `casesmod-1.0.0.jar` |
| **2026-08-06** | `v1.8.3` | Antigravity AI | Выровнена идеальная центровка текста по оси X (`X = 125`) для всех 4 полей GUI Рынка в соответствии с рамками текстуры `fish_market.png`. | `FishMarketScreen.java` |
| **2026-08-06** | `v1.8.4` | Antigravity AI | Ребаланс экономики: уменьшена стоимость продажи рыбы в ~4–5 раз (базовые цены 3–180 🪙 вместо 12–900 🪙, золотой множитель урезан с 2.5x до 1.5x, множитель веса с 2.5x до 1.5x). | `FishPriceCalculator.java`, `casesmod-1.0.0.jar` |
| **2026-08-06** | `v1.8.5` | Antigravity AI | Исправлено дублирование количества предметов при получении награды из кейсов (`Хлеб ×16 ×16` -> `Хлеб ×16`) и обновлен текст кнопки («К спискам кейсов»). | `CaseOpeningScreen.java`, `casesmod-1.0.0.jar` |
| **2026-08-06** | `v1.9.0` | Antigravity AI | Удалены предметы Avaritia из дропа рыбалки, усложнен крафт удочек, добавлены эндгейм-крафты Умножителей улова (x32, x64), добавлена трата прочности удочек. | `FishingLootHandler.java`, `20_aquatech_rod_crafts.js`, `30_aquatech_crafting.js` |
| **2026-08-06** | `v1.9.1` | Antigravity AI | Удален отдельный слот умножителей улова в Авто-Рыболове. Авторыболов скейлится только от умножителя в удочке; запрещена укладка RateMod в авторыболов (разрешены только модули). Исправлен баг отрицательной энергии (`-15536 FE`) через 32-битный ContainerData сплит. | `AutoFisherBlockEntity.java`, `AutoFisherMenu.java`, `HydroReactorBlockEntity.java`, `HydroReactorMenu.java`, `OceanFilterBlockEntity.java`, `OceanFilterMenu.java`, `SeabedDredgerBlockEntity.java`, `SeabedDredgerMenu.java` |
| **2026-08-07** | `v2.0.0` | Antigravity AI | **Глобальное обновление баланса и механизмов:**<br>1. Установлен мод **Extended Crafting** (`ExtendedCrafting-1.20.1-6.0.10.jar`) и библиотека `Cucumber-1.20.1-7.0.16.jar`. В KubeJS настроены верстаки 9×9 Ultimate Table для `rate_x32`, `rate_x64` и Административной Панели.<br>2. Удален гидрореактор (`hydro_reactor`), переименован `ocean_filter` в «Ботанический Экстрактор Цветов» (вылов лепестков и цветов Botania), в `seabed_dredger` добавлен Небесный Камень AE2 (`ae2:sky_stone_block`).<br>3. Добавлен `ae2_press_case.json` за 399 монет с прессами AE2, удален `free_case.json`. В KubeJS добавлены рецепты размножения прессов (Пресс + Блок Железа = 2 Пресса) и их взаимозаменяемости.<br>4. В улов удочек 1 тира (`bamboo_rod`, `humble_rod`, `fishing_rod`) добавлен 100% улов Булыжника и стартовых блоков; в улов 2 тира добавлен Титан (45%).<br>5. Вычищены ошибки KubeJS (удалены ссылки на несуществующие айди `aquatech_ui:*_rod` и `double_hook_upgrade`), удалены старые 3×3 Java-рецепты для рейтов х32/х64 из мода. | `ExtendedCrafting-1.20.1-6.0.10.jar`, `Cucumber-1.20.1-7.0.16.jar`, `30_aquatech_crafting.js`, `FishingLootHandler.java`, `FishingRodCompat.java`, `SeabedDredgerBlockEntity.java`, `OceanFilterBlockEntity.java`, `ae2_press_case.json`, `IMPLEMENTATION_PLAN.md` |
| **2026-08-07** | `v2.1.0` | Composer | Клиентский лаунчер: локальный пак + MD5-синк, кнопка «Обновить», CDN через `start_sync_server` + Playit :8080, UI v2.3.0. Зафиксирован чеклист обновления сборки в ТЗ-6.3 и `КАК_ОБНОВЛЯТЬ_СБОРКУ.txt`. | `tools/aquatech_launcher.py`, `tools/publish_client_pack.py`, `tools/start_sync_server.py`, `IMPLEMENTATION_PLAN.md`, `КАК_ОБНОВЛЯТЬ_СБОРКУ.txt` |
| **2026-08-11** | `v2.2.0` | Composer | Прочность удочек StarCatcher: `RodDurability` + износ на улове (в т.ч. fish-only), тултип остатка уловов; KubeJS `40_rod_durability.js`. Рейт-моды: `RateModItem.MAX_CATCHES = 10000` + pin bait. Pack/launcher линейка 2.9.15 / client-2.9.26, `aquatech_ui` 1.0.9. | `RodDurability.java`, `FishingLootHandler.java`, `StarCatcherRodTooltips.java`, `AutoFisherBlockEntity.java`, `RateModItem.java`, `40_rod_durability.js` |
| **2026-08-11** | `v2.2.1` | Composer | Баланс лута: титан снят с T1 `bamboo_rod`, добавлен дроп бамбука; на T2 `humble_rod` титан 45%→60%. Фантомы: `60_disable_phantoms.js` (уже был, освежён). Docs `FISHING_ROD_LOOT_TABLE.md`. `aquatech_ui` 1.0.10, pack 2.9.16, launcher 2.9.27. | `FishingLootHandler.java`, `add_fishing_resource_tooltips.js`, `60_disable_phantoms.js`, `FISHING_ROD_LOOT_TABLE.md` |
| **2026-08-11** | `v2.2.2` | Composer | Тултипы ресурсов синхронизированы с `rollStarCatcherRodLoot` (earliest tier). AF: выход 2×2→**3×3**, overflow в соседний IItemHandler / дроп в мир. Прочность ранних удочек 64→128 (середина 192, поздние 256, топ 320). Вырезан мёртвый бонус perfect reel (`quality >= 90`). `aquatech_ui` 1.0.11, pack 2.9.17, launcher 2.9.28. | `add_fishing_resource_tooltips.js`, `AutoFisherBlockEntity.java`, `AutoFisherMenu.java`, `40_rod_durability.js`, `RodDurability.java`, `FishingLootHandler.java`, `FISHING_ROD_LOOT_TABLE.md` |
| **2026-08-11** | `v2.2.3` | Composer | Фикс отображения прочности: KubeJS `item.maxDamage=` no-op на raw Item → Java `RodDurabilityApplier` (Unsafe force maxDamage) + клиентская полоска `RodDurabilityBar` (видна всегда, не только после урона) + `item.setMaxDamage()` в KubeJS. Тултип: «Осталось уловов: X / Y». `aquatech_ui` **1.0.12**, pack **2.9.18**, launcher **client-2.9.29**. | `RodDurabilityApplier.java`, `RodDurabilityBar.java`, `RodDurability.java`, `AquaTechUI.java`, `StarCatcherRodTooltips.java`, `40_rod_durability.js`, `IMPLEMENTATION_PLAN.md` |
| **2026-08-11** | `v2.2.4` | Composer | AutoFisher UI под новую текстуру: выход **6** слотов (3×2), слоты удочки/апгрейда, стрелка прогресса вместо старого бара. Overflow в chest/мир сохранён. `aquatech_ui` **1.0.13**, pack **2.9.19**, launcher **client-2.9.30**. | `AutoFisherBlockEntity.java`, `AutoFisherMenu.java`, `AutoFisherScreen.java`, `auto_fisher.png` |
| **2026-08-10** | `v2.3.0` | Composer | Портал: убраны cyan hover/outline на карточках, hero horizon под IP-блоком. Пак **2.9.5–2.9.8**: ImmediatelyFast убран (Oculus crash), `aquatech_ui` **1.0.3**, FAWE Mohist TypeProperty patch. Крафты `boner_rod` / `sky_rod`, x32/x64 только extreme. Lodestone sync при `publish_client_pack`. Таблица лута удочек на портале (`FISHING_ROD_LOOT_TABLE.md`). | `docs/`, `tools/patches/patch_fawe_mohist.py`, `kubejs/`, `tools/publish_client_pack.py`, `tools/sync_lodestone_mods.py` |
| **2026-08-11** | `v2.3.1` | Composer | **Рыбалка StarCatcher:** откат Rhythm Hook (нет текстур) → нативный SC minigame. `ItemFishedEvent`: **не cancel** (ghost bobber), `getDrops().clear()` + `awardCatch()` для resource rods, `forceReleaseBobber()` next tick. Datapack `aquatech_boot_fixes` fish: preview `minecraft:cod`, `skips_minigame: false`. `aquatech_ui` **1.0.17**, pack **2.9.24**. | `FishingLootHandler.java`, `StarCatcherAttachments.java`, `datapacks/aquatech_boot_fixes/`, `scripts/tasks/neutralize_aquatech_sc_preview.py` |
| **2026-08-11** | `v2.3.2` | Composer | **Apex deploy P0–P2:** `scripts/tasks/deploy_apexnodes_sftp.py` (SFTP + panel restart, jar purge, kubejs/datapack mirror, MySQL inject). Секреты `.apex_deploy.json` / `.apex_mysql.json`. FAWE `persistent-brushes: false`, `patch_fawe_mohist.py`. Smoke `smoke_apex_server.py`, backup rotate (limit=1), `apex_console_ops.py` (WG `-w`, Chunky). | `scripts/tasks/*.py`, `scripts/README.md`, `server/plugins/FastAsyncWorldEdit/config.yml` |
| **2026-08-11** | `v2.3.3` | Composer | **Инцидент FTB Quests:** коммит `ce93050` удалил workshop-главы; deploy затирал live-правки с Apex. Восстановление → финал: **только Lodestone-квесты** (`1.snbt` + 3 главы). Deploy **не** трогает `ftbquests` без `AQUATECH_SYNC_QUESTS=1`. | `config/ftbquests/`, `server/config/ftbquests/`, `deploy_apexnodes_sftp.py` |
| **2026-08-11** | `v2.3.4` | Composer | **Fish-only rods:** `boner_rod`/`sky_rod` — `scrubFakeAquaTechFishDrops()` (убрать cod из aquatech datapack pool), вес aquatech-fish → 0.05. `aquatech_ui` **1.0.18**, pack **pack-2.9.25**. Лаунчер bootstrap **client-2.9.42**, portal auth fixes. | `FishingLootHandler.java`, `FishingRodCompat.java`, `docs/pack/manifest.json`, `docs/bootstrap.json` |
| **2026-08-13** | `v2.4.0` | Antigravity AI | **Системный аудит и Роадмап проекта (без визуала):** Проведен комплексный аудит сборки, сайта, лаунчера и инфраструктуры. Сформирована дорожная карта исправления дюпов, сквозной токен-авторизации лаунчер-сервер, доната D1, бэкапов R2 и эндгейм-механик. | `IMPLEMENTATION_PLAN.md`, `docs/TECHNICAL_ROADMAP.md` |
| **2026-08-13** | `v2.4.1` | Antigravity AI | **Реализация Раздела 1 (Серверная сборка):** 1) Износ удочек `RodDurability` без срезки Unbreaking (1 catch = 1 damage). 2) `StarCatcherEnchantmentHandler` заблокировал Mending/Unbreaking на наковальне и починку от XP. 3) `AutoFisherBlockEntity` с принудительным `setChanged()` при улове. 4) `/deposit` и `/withdraw` в `casesmod` для конвертации монет Lightman's Currency ↔ баланс F4. 5) `70_island_auto_claim.js` для авто-привата WorldGuard. | `RodDurability.java`, `StarCatcherEnchantmentHandler.java`, `AutoFisherBlockEntity.java`, `MenuCommands.java`, `70_island_auto_claim.js` |
| **2026-08-13** | `v2.4.2` | Antigravity AI | **Реализация Раздела 3 (Лаунчер & Безопасность):** 1) Скрытие окон консолей CMD/PowerShell через `CREATE_NO_WINDOW` (`0x08000000`) и `STARTUPINFO` `SW_HIDE`. 2) Сквозная токен-авторизация `C2SAuthPacket` + `ServerAuthTracker` (авто-кик через 10с без валидного токена). 3) Парсер крашей `analyze_crash_logs()` с диагностикой OOM, OpenGL, GPU драйверов. 4) Динамический RAM Allocator `detect_optimal_ram_mb()` (50% свободного OZU) и G1GC флаги Java 17. | `C2SAuthPacket.java`, `ServerAuthTracker.java`, `ClientAuthHandler.java`, `aquatech_launcher.py`, `launcher_bridge.py` |
| **2026-08-13** | `v2.5.0` | Antigravity AI | **Реализация Web-Лаунчера по принципу LoliLand (Edge WebView2 + REST API Bridge):** 1) Легковесный нативный контейнер `aquatech_web_launcher.py` (PyWebView, 15 МБ). 2) Океанский веб-интерфейс `docs/launcher.html` + `docs/assets/css/launcher.css` по стандартам `anti-ai-slop-design`. 3) Безрамочное окно (Frameless UI), кнопка «ИГРАТЬ» с прогресс-баром, выбор RAM, индикатор онлайна, краш-диагностика на русском языке. 4) Скрипт сборки `build_web_launcher.py`. | `aquatech_web_launcher.py`, `launcher.html`, `launcher.css`, `launcher_bridge.py`, `build_web_launcher.py` |
| **2026-08-13** | `v2.5.1` | Composer | LoliLand GUI 1.2–1.3 + auth: TTF `aquatech_ui:main/header`, K/TAB/HUD/AquaButton without hover scale. Mohist `POST /api/launcher/verify-token` when `auth.requirePortalSession=true` (default false for Lodestone). `S2CSessionSyncPacket` now carries balance/rank. `aquatech_ui` **1.0.21**. | `AquaFontRenderer.java`, `OceanSkillTreeScreen.java`, `ServerAuthTracker.java`, `PortalSessionVerifier.java`, `main.json`, `header.json` |
| **2026-08-13** | `v2.5.2` | Composer | Stage 2 CEF: CinemaMod MCEF 2.1.6 in client pack only. F4 Донат + клик по нику → `AquaWebScreen` embeds `/embed/donate.html` `/embed/cabinet.html`. Worker sets `at_session` from `?session=`. `aquatech_ui` **1.0.22**, `casesmod` **1.0.7**. | `AquaWebBridge.java`, `CefHost.java`, `embed.js`, `worker/index.js`, `MainMenuScreen.java` |
| **2026-08-13** | `v2.5.3` | Composer | Ship pack **2.9.26** + launcher **client-2.9.47**. Lodestone sync removed from `publish_client_pack`. Session token JVM flag for CEF cabinet. | `publish_client_pack.py`, `aquatech_launcher.py`, `docs/pack/manifest.json` |
| **2026-08-13** | `v2.5.4` | Composer | Stage 1 native GUI kit: `AquaGlassPanel` / `AquaBadge` / `AquaCaseSlot` / `AquaDialogScreen`. TAB + K-tree + HUD + CEF frames use glass. Skill unlock confirms via dialog. TTF wrap helpers. `aquatech_ui` **1.0.23**. | `AquaGlassPanel.java`, `AquaDialogScreen.java`, `OceanSkillTreeScreen.java`, `OceanTabScreen.java` |
| **2026-08-13** | `v2.5.5` | Composer | Stage 3 packets/cache: `C2SOpenContainer` opens vault (ender chest), limiter GUI, personalization. Island machine caps + `S2CSyncLimitersPacket`. `ResourceCacheManager` HTTPS allowlist + PNG check, HUD/F4 use it. Protocol **7**. `aquatech_ui` **1.0.24**, `casesmod` **1.0.8**. | `ContainerOpenService.java`, `IslandLimiterTracker.java`, `ResourceCacheManager.java`, `MainMenuScreen.java` |
| **2026-08-13** | `v2.5.6` | Composer | Ship pack **2.9.27** + launcher **client-2.9.48**. First-party jars `aquatech_ui-1.0.24` / `casesmod-1.0.8`. | `docs/pack/manifest.json`, `docs/bootstrap.json`, `docs/assets/js/site.js` |
| **2026-08-17** | `v2.5.7` | Composer | Health pass: Go/C# bootstrap upgrade-only + Worker mirrors; C# is the ship path (`dotnet publish`). casesmod leftovers rewired to AquaLumen hub. Hub store/cases clicks send actions; server answers «пока недоступно». Dead preview/web-launcher/casesmod deploy scripts removed. | `bootstrap/update.go`, `LauncherSelfUpdate.cs`, `AquaWebIpcDispatcher.java`, `HubActionHandler.java`, `HubScreen.java` |
| **2026-08-17** | `v2.5.8` | Composer | Full ship after health pass: pack **2.9.55** (fresh aquatech_ui 1.0.24 + aqualumen 0.3.0-alpha), launcher **client-2.9.65**. | `docs/pack/manifest.json`, `docs/bootstrap.json`, `docs/assets/js/site.js` |
| **2026-08-17** | `v2.5.9` | Composer | AquaLumen **0.3.1-alpha** moves F4 hub to bundled MCEF HTML with live snapshot IPC, whitelisted server actions and native fallback. Ship pack **2.9.57**, launcher **client-2.9.66**. | `LumenWebScreen.java`, `LumenWebBridge.java`, `hub.html`, `docs/pack/manifest.json` |
| **2026-08-17** | `v2.5.10` | Composer | Fix AquaLumen MCEF resource URL and add readiness fallback after live client exposed blank F4 blur. Ship AquaLumen **0.3.2-alpha**, pack **2.9.58**, launcher **client-2.9.67**. | `LumenWebBridge.java`, `LumenWebScreen.java`, `hub.html` |
| **2026-08-18** | `v2.6.0` | ZCode | **P0 security & stability pass (live Apex):** 1) FAWE TypeProperty patch extended to `impl/fawe/v1_20_R1/PaperweightFaweAdapter` (raw-byte ifeq→goto + CP parser) — fixes live WorldGuard EventException spam on interact/inventory; junk jars (`_fawe_patch_work.jar`, `.tmp`) removed from live plugins; panel backup rotated. 2) `requirePortalSession = true` on live `config/aquatech_ui-common.toml` (+ repo mirror) — token auth enforced. 3) `SERVER_SYNC_KEY` rotated: Worker secret set, KubeJS reads key from gitignored `config/aquatech_sync_key.json` via `JsonIO` (java.nio blocked by sandbox). 4) Verified Renfild nick occupied + verify-token endpoint alive. 5) KubeJS: removed non-existent `ItemEvents.fishCaught` binding (feature was dead on load). 6) Portal working tree: fixed start.html download links (real `client-2.9.69` assets, removed phantom `.jar`s), `bootstrap.json` `launcher_exe → AquaTech.exe`, CSS cache-bust `20260818h` on all pages, hero status pill wired to `/api/server-status`, `finish_pack_release.py` tag → `pack-2.9.60`, fallback news → 2.9.69. | `tools/patches/patch_fawe_mohist.py`, `server/plugins/FastAsyncWorldEdit.jar`, `server/config/aquatech_ui-common.toml`, `kubejs/server_scripts/80_live_portal_sync.js`, `server/kubejs/server_scripts/80_live_portal_sync.js`, `docs/start.html`, `docs/index.html`, `docs/bootstrap.json`, `docs/assets/js/site.js`, `docs/assets/css/site.css`, `tools/finish_pack_release.py`, `.gitignore` |
| **2026-08-19** | `v2.6.1` | ZCode | **Unified portal redesign + working F4 case system:** 1) Главная и rods.html переведены на единый стиль start.html (step-cards, props-листы, gradient-заголовки, SVG tile-иконки вместо ИИ-JPG; `feature_*.jpg`/`banner_rods.jpg` удалены). 2) StarCatcher-брендинг убран со всего сайта → «удочки AquaTech» (HTML, siteCopy.js, site.js, footer). 3) F4-хаб: `CaseConfig` (`config/aqualumen/cases.json`, редактируемый на сервере; кейсы ocean/fisher/depth с ценами в монетах, зеркально сайту), `HubEconomy` (монеты: scoreboard → lightman-фолбэк; гемы; daily streak в persistentData), `HubActionHandler`: реализованы `case.open` (списание монет, взвешенный ролл, выдача) и `daily.claim` (награда + серия); `HubDataService` отдаёт реальные кейсы/дейли. hub.html: локальная заглушка daily-кнопки убрана. 4) site.js `CASE_LOOT_TABLES` синхронизированы с игровыми. 5) aqualumen 0.3.4-alpha пересобран, задеплоен на Apex, сервер перезапущен чисто. 6) Портал задеплоен на aquateche.store (cache-bust 20260819i, ссылки на client-2.9.75). Инцидент: параллельный запуск `generate_site.py` (другой агент) затирал страницы старыми шаблонами — восстановлено; генератор отстаёт от дизайна и требует обновления перед следующим запуском. | `docs/index.html`, `docs/rods.html`, `docs/start.html`, `docs/assets/css/site.css`, `docs/assets/js/site.js`, `functions/_lib/siteCopy.js`, `mods/aqualumen-ui/.../CaseConfig.java`, `HubEconomy.java`, `HubActionHandler.java`, `HubDataService.java`, `hub.html`, `server/mods/aqualumen-forge-1.20.1-0.3.4-alpha.jar`, `server/config/aqualumen/cases.json` |
| **2026-08-19** | `v2.6.3` | ZCode | **Прогрессия удочек/крафтов/кейсов переработана:** 1) `FishingLootHandler`: закрыты все «куриные яйца» цепи — каждый следующий тир ловит материалы крафта следующей удочки (T1 +слайм-шары; T5 +серебро/алюминий; T6 +сапфир/вольфрам/хром/золото; T7 +кристалл; T8 +кобальт/алмаз; T9 +алмазы 2–3/обсидиан/плачущий обсидиан/нерж. сталь; T10 +сердце моря/платина/призмарин; T11 +уран/инконель/незеритовый лом; T12 +осмиридий/адамантит). 2) `20_aquatech_rod_crafts.js`: из крафтов убраны электросхемы (271/272/273/274), IU-плиты (в паке нет ни одного рецепта их производства — проверено сканом 5705 рецептов jar) и азот; всё заменено на слитки/руды, которые ловятся; netherite_block → netherite_ingot. 3) Кейсы: убран денежный принтер (250→10–50k монет 45%, 2000→250k 30%) — теперь ocean=ранние материалы+60–120 монет, fisher=середина прогрессии+редкие удочки T6–T9 (2–4%), depth=поздние материалы+T10–T13 удочки (3–6%); веса всех трёх = ровно 100. 4) Фикс бага: «Продать всю рыбу» продавал ВСЁ `starcatcher:*` включая удочки/шляпы — теперь только рыба из fish_shop.json + ванильная рыба; sellSingle тоже защищён. 5) rate_x2 крафт: плиты→слитки. 6) Сайт синхронизирован: rods.html (все тиры + фикс дрейфа T3/T4), site.js CASE_LOOT_TABLES, siteCopy, cache-bust 20260819j; выяснено, что статику aquateche.store бандлит воркер (`[assets] directory=./docs`) — Pages-деплой сам по себе прод не обновляет, нужен worker deploy. 7) Деплой: aquatech_ui 1.0.24 + aqualumen 0.3.4-alpha пересобраны и залиты (версии сохранены — лут-логика серверная, клиентский пак не тронут), cases.json + крафты на live, рестарт чистый (13/13 скриптов, 0 ошибок). | `mods/aquatech-ui/.../FishingLootHandler.java`, `mods/aqualumen-ui/.../CaseConfig.java`, `FishShopConfig.java`, `kubejs/server_scripts/20_aquatech_rod_crafts.js`, `30_aquatech_crafting.js`, `server/config/aqualumen/cases.json`, `docs/rods.html`, `docs/assets/js/site.js`, `functions/_lib/siteCopy.js`, `server/mods/*.jar` |
| **2026-08-24** | `v2.7.0` | ZCode | **Lumen Market + экономика:** 1) Аукцион игроков: D1-таблица `market_listings`, worker `functions/api/market.js` (+роут в worker/index.js; важное: воркер-бандл падал из-за неверного относительного импорта `../../_lib` → `../_lib`), Java `MarketService` (sell/buy/cancel через серверный ключ, атомарная покупка на worker-уровне — повторная покупка 409, выручка продавцу сразу на портал-кошелёк и подтягивается в игру обычным coin-sync), команда `/ah sell <цена> | cancel <id>`, action `auction.buy/cancel` из хаба, вкладка «Аукцион» теперь показывает реальные лоты (были фейковые sampleLots). 2) Динамический суточный курс кота-рыболова (KubeJS, day-seed, +50..100% на трендовых рыб) — реализован параллельным агентом в `90_fisherman_cat_shop.js`. 3) Скин игрока в хабе: клиентский `PlayerHeadCapture` (PlayerInfo.getSkinLocation → NativeImage downloadTexture, лицо+шляпа → data URL в payload.playerHead) вместо mc-heads-заглушки. 4) Registry-фикс: aquatech_ui 1.0.25 (пересборка 1.0.24 ломала синк реестра у старых клиентов). 5) Прочее: иконки кейсов 2.5D/чистые слитки (tools/build_case_icons.py), фикс скролла/перфа хаба, LP-префиксы «глиф+текст» через permission-ноды (консольный `prefix set` не парсится), чистка PUA-глифов из D1 privilege. Пак 2.9.130, лаунчер client-2.9.76. | `functions/api/market.js`, `worker/index.js`, `mods/aqualumen-ui/.../MarketService.java`, `PlayerHeadCapture.java`, `HubSnapshot.java`, `HubActionHandler.java`, `LumenCommands.java`, `mods/aquatech-ui` (1.0.25), `kubejs/server_scripts/90_fisherman_cat_shop.js` |
| **2026-08-26** | `v2.8.1` | ZCode | **FTB квесты, вторая волна:** предметные награды на все главы (пар: медь×8, базовая электрика: олово×8, улучшенная: сталь×4; глава 1 уже имела предметы). Botania 43 (Botanical Machinery + Extra Machinery + MythicBotany), Avaritia 32 (размерные столы, extreme anvil/smith, blaze/crystal броня, Infinity как таск без выдачи infinity_*), Alex's Caves 33 (магнит/токсин/дино/бездна/сладости/тьма). Секрет бездны: dense energy cell + 64k вместо `ae2:quantum_link`. Скан jar-моделей: 0 bad, 0 duplicate ids, 0 dangling deps. Деплой Apex `AQUATECH_SYNC_QUESTS=1`. | `tools/quests/build_mod_tabs.py`, `config/ftbquests/`, `server/config/ftbquests/`, `scripts/tasks/scan_quest_items.py` |
| **2026-08-26** | `v2.8.2` | ZCode | **AE2 вкладка FTB:** глава `ae2_aquatech` (order 11, после эндгейма, иконка controller), 42 квеста от гайда до ячейки 64k. Без wireless/quantum/spatial в наградах. Порядок старых вкладок без сдвига: Avaritia 8, тайны 9, эндгейм 10. | `tools/quests/build_mod_tabs.py`, `tools/quests/useful_rewards.py`, `config/ftbquests/`, `server/config/ftbquests/` |
| **2026-08-26** | `v2.8.3` | ZCode | **Портал:** шапка компактнее (логотип-дом, «Ещё», CTA «Скачать» справа), focus-visible, аукцион на сайте (`market.html` + `/api/market/public` до 40 лотов), ошибки форм под полями. | `docs/`, `docs/assets/css/site.css`, `docs/assets/js/site.js`, `worker/index.js` |
| **2026-08-26** | `v2.8.6` | ZCode | **Портал: пароли/сессии + лимит машин на /is.** Регистрация от 8 символов; ник с сервера (`IN_GAME_UNREGISTERED`) задаёт пароль на /register. Логин: rate-limit, constant-time hash. `/embed/?session=` ставит cookie только если сессия есть в D1. `verify-token` требует `SERVER_SYNC_KEY`. Лаунчер не пишет plaintext session если DPAPI упал. Кап на `island_*`: IU 64 TE, Create 96, AE2 36, DE 12, hopper 48, авто-рыболов 8. aquatech_ui **1.0.34**. | `functions/_lib/auth.js`, `functions/api/login.js`, `functions/api/register.js`, `functions/api/launcher/verify-token.js`, `worker/index.js`, `IslandLimiterRules.java`, `WorldGuardIslandLookup.java`, `PortalSessionVerifier.java` |
| **2026-08-26** | `v2.8.4` | ZCode | **Статы рыбалка/монеты:** синк читает Vault/Essentials, не stale scoreboard; кастомный улов пишет `aquatech_fish`; портал `fish=MAX`; зеркало в Apex MariaDB `aquatech_player_stats`. Рестарт панели каждый день 04:00 MSK (`save-all` → `lp sync` → restart). aqualumen **0.3.18-alpha**, aquatech_ui **1.0.32**. | `HubEconomy.java`, `HubDataService.java`, `MariaStats.java`, `FishingLootHandler.java`, `functions/api/sync/player.js`, `scripts/tasks/setup_apex_daily_reload.py` |
| **2026-08-25** | `v2.8.0` | ZCode | **Ивенты + экономика + ops:** 1) Lumen Market (D1 market_listings, worker /api/market, MarketService, /ah sell/cancel, вкладка Аукцион с реальными лотами; выручка продавцу на портал-кошелёк). 2) Дневной спрос: 3 трендовые рыбы из ВСЕГО каталога (fish_demand.json, единый алгоритм Java+KubeJS), множитель ×2/×1.75/×1.5 в монетной скупке F4 (бейдж ТРЕНД, зачёркнутая цена) и у Кота (изумруды), оповещения: вход/полночь/старт. 3) OceanEventsService (aquatech_ui 1.0.27): Золотая рыба (20-мин окно каждые ~3ч, 5% шанс, джекпот 500-2500, броадкасты), Задания дня (3 от даты: любая/ночь/дождь, награды 300-1100), Недельный турнир (сб+вс, самая тяжёлая рыба по NBT weight, топ-3 призы 2500/1000/500, персист в aquatech_tournament.json). 4) Tab: баланс монет каждого игрока (PlayerProfile.coins из скорборда, 1.0.26). 5) Скин игрока в хабе (PlayerHeadCapture). 6) MOTD двухстрочный. 7) Ops: Chunky преген r8000 блоков @спавн запущен (~1M чанков, ~11ч, continue-on-restart), автобэкап мира scripts/tasks/backup_world.py (SFTP→backups/, keep 7) по расписанию 04:00. 8) Иконки кейсов: вики-рендеры (ru.wiki Grid + Downloads + file-search GIF), блоки 2.5D из jar-текстур. Паки 2.9.190-2.9.240, лаунчер client-2.9.76. | `functions/api/market.js`, `worker/index.js`, `mods/aqualumen-ui/.../MarketService.java`, `FishShopConfig.java`, `HubSnapshot.java`, `mods/aquatech-ui/.../OceanEventsService.java`, `PlayerProfile.java`, `LuckPermsBridge.java`, `PlayerHeadCapture.java`, `kubejs/server_scripts/90_fisherman_cat_shop.js`, `tools/build_case_icons.py`, `scripts/tasks/backup_world.py` |
| **2026-08-19** | `v2.6.2` | ZCode | **Coins-sync root fix + FAWE-регресс закрыт навсегда:** 1) Hub↔portal монеты: рут-кейс — legacy-кошелёк `persistentData.coins` (150 у xietoru) был невидим Java-хабу (читал только scoreboard/lightman), sync затирал портал нулями. `HubEconomy.coins()` теперь импортирует legacy в scoreboard; `trySpendCoins` — проверка суммы ДО списания + последовательный спенд scoreboard→инвентарь (был баг отрицательного счёта = бесплатные кейсы); `HubDataService.open()` греет кошелёк на главном потоке. 2) Утёкшие ключи добиты: `HubDataService.resolveSyncKey()` → `return null` + skip-guard (без файла `config/aquatech_sync_key.json` синк выключен), KubeJS-фоллбэки `'aquatech_internal_sync_key_2026'` → `''` (обе копии), `functions/api/sync/player.js` fail-closed (нет `SERVER_SYNC_KEY` → 403 всем); воркер задеплоен, smoke: wrong-key 403, real-key GET отдаёт xietoru coins=150. 3) FAWE TypeProperty регресс-кейс найден: SFTP-деплой заливал из репо непатченный `_fawe_patch_work.jar` + `.tmp` (скрипт пропускал только `.bak`/`.pre-`), Bukkit грузил мусорный jar вместо патченного → конфликт + TypeProperty. Локальный `FastAsyncWorldEdit.jar` перепатчен (impl-ветка), мусор удалён локально и на live, `should_skip` теперь отбрасывает `.tmp` и `_`-префикс. Сервер перезапущен: boot чистый (Done 7.4s, без Ambiguous/TypeProperty). 4) Аудит удочек: T1–T5 крафт-цепь валидна; T6+ закрыты кейсом «Рыбацкий» (iceborn 35%/starcatcher 25%/azure 20%/sharktooth 12%/magmaforged 8%); мёртвые крафты: lush (платина только с него самого), alpha (осмиридий/адамантит только с него + IU-space отключён), slimed (slime_block без источника до T5), непатченные дыры: серебро до T6 (нужно и rate_x4), сапфир/вольфрам/хром до T7, кобальт до T9, алмазы до T10 (только rareTreasure), обсидиан до T10 (crying_obsidian). | `mods/aqualumen-ui/.../HubEconomy.java`, `HubDataService.java`, `kubejs/server_scripts/80_live_portal_sync.js`, `server/kubejs/server_scripts/80_live_portal_sync.js`, `functions/api/sync/player.js`, `scripts/tasks/deploy_apexnodes_sftp.py`, `server/plugins/FastAsyncWorldEdit.jar`, `server/mods/aqualumen-forge-1.20.1-0.3.4-alpha.jar` |

---

## 🧭 Handoff для Antigravity — спринт 10–11 августа 2026

> [!IMPORTANT]
> Этот раздел — **полный контекст за 2 дня** (10–11 авг 2026). Читать перед любой работой с сервером, рыбалкой, квестами или деплоем.

### 1. Текущее состояние (snapshot на 2026-08-11)

| Компонент | Версия / значение |
|-----------|-------------------|
| **Живой хост** | ApexNodes `g-pl-3.apexnodes.xyz:21561` (panel id `6fdc6f7b`) |
| **Репо-сервер** | `server/` — зеркало для SFTP, не единственный runtime |
| **Lodestone** | `%USERPROFILE%\.lodestone\instances\AquaTech-*\mods` — синк first-party jars |
| **aquatech_ui** | **1.0.18** (`server/mods/`, Apex, Lodestone) |
| **casesmod** | 1.0.1 |
| **packetfixer** | 3.3.2-forge-1.20.1 |
| **Client pack** | GitHub release **pack-2.9.25** (`docs/pack/manifest.json`) |
| **Launcher bootstrap** | **client-2.9.42** (`docs/bootstrap.json`) |
| **FAWE** | patched jar + `persistent-brushes: false` |
| **MySQL** | Apex MariaDB, плейсхолдеры `__AQUATECH_MYSQL_*__` в plugin configs |

### 2. Рыбалка StarCatcher + aquatech_ui (критично)

#### 2.1 Два типа удочек

| Тип | ID | Поведение |
|-----|-----|-----------|
| **Resource rods** | `bamboo_rod` … `alpha_rod` (кроме fish-only) | SC minigame → `ItemFishedEvent` → clear SC drops → `awardCatch()` (IU/AquaTech loot pools) |
| **Fish-only** | `starcatcher:boner_rod`, `starcatcher:sky_rod` | SC minigame → **нативный** улов StarCatcher, без AquaTech resource pool |

Код: `FishingRodCompat.java` (`FISH_ONLY`), `FishingLootHandler.java`.

#### 2.2 Что ломалось и как чинили

1. **Rhythm Hook UI** — отклонён (magenta missing textures). Оставлен **нативный StarCatcher minigame**.
2. **Застрявший поплавок** — нельзя `event.setCanceled(true)` на `ItemFishedEvent`. Паттерн: clear drops, не cancel, `StarCatcherAttachments.forceReleaseBobber(player)` на следующем тике.
3. **Костяная удочка ловила cod/руды** — aquatech datapack `starcatcher/fish/*.json` с `catch_info: cod` попадал в **глобальный** SC pool. Фикс 1.0.18: `scrubFakeAquaTechFishDrops()` для fish-only; вес aquatech-fish снижен до `0.05`.

#### 2.3 Datapack aquatech_boot_fixes

Пути (синкаются в `server/datapacks`, `moonlight-global-datapacks`, `world/datapacks`):

```
datapacks/aquatech_boot_fixes/data/aquatech/starcatcher/fish/*.json
```

- `catch_info.item` = `minecraft:cod` — **только preview** в minigame
- Реальный лут resource rods — из Java `rollStarCatcherRodLoot` / `awardCatch`
- Скрипт регенерации preview: `scripts/tasks/neutralize_aquatech_sc_preview.py`

Документация лута: `docs/FISHING_ROD_LOOT_TABLE.md`.

### 3. FTB Quests — инцидент и правила

#### 3.1 Что случилось

1. Коммит **`ce93050`** (9 авг) удалил workshop-главы `20_ws_*` … `2F_ws_*`.
2. Deploy копировал `config/ftbquests` из репо на Apex → **затирал** правки из in-game редактора.
3. Пользовательские квесты жили в **Lodestone**, не в репо.

#### 3.2 Текущий source of truth (квесты)

**Единственные главы на Apex и в репо (после v2.3.4):**

| Файл | Описание |
|------|----------|
| `quests/chapters/1.snbt` | Основная глава пользователя (~18 KB) |
| `quests/chapters/41C738259E283D28.snbt` | Вспомогательная |
| `quests/chapters/57FF374744F4AC76.snbt` | Вспомогательная |
| `quests/chapters/7D2835D587AABDAB.snbt` | Вспомогательная (иконка bamboo_rod) |

Оригинал редактирования:  
`%USERPROFILE%\.lodestone\instances\AquaTech-18b48d49\config\ftbquests\`

Копии в репо: `config/ftbquests/` и `server/config/ftbquests/`.

#### 3.3 Правила для агента (НЕ НАРУШАТЬ)

- Deploy **по умолчанию НЕ** заливает `ftbquests` (`AQUATECH_SYNC_QUESTS=1` только явно).
- Перед заменой квестов — **скачать live** с Apex или взять из Lodestone.
- **Не** восстанавливать workshop `20_ws_*` без запроса владельца.
- Бэкап перед полным deploy: panel backup (rotate при limit=1).

Загрузка только квестов:

```powershell
$env:AQUATECH_SFTP_ONLY = 'config/ftbquests'
$env:AQUATECH_SKIP_BACKUP = '1'
python scripts/tasks/deploy_apexnodes_sftp.py
```

После: `ftbquests reload` через panel console или `apex_console_ops.py --cmd "ftbquests reload"`.

### 4. Apex deploy — инфраструктура

#### 4.1 Секреты (gitignored)

| Файл | Содержимое |
|------|------------|
| `.apex_deploy.json` | `sftp_pass`, `apex_api_key`, host/user/server_id |
| `.apex_mysql.json` | MariaDB host, user, password, database |

Пример: `.apex_deploy.json.example`.

#### 4.2 Основные скрипты (`scripts/tasks/`)

| Скрипт | Назначение |
|--------|------------|
| `deploy_apexnodes_sftp.py` | SFTP upload `server/` → Apex, panel restart, jar purge, kubejs mirror |
| `smoke_apex_server.py` | panel running, 1 jar per prefix, FAWE brushes, MySQL ping |
| `apex_console_ops.py` | WG `region flag -w world …`, Chunky spawn pregen |
| `bootstrap_p0_local.py` | FAWE patch + promote jar локально |
| `setup_apex_mysql.py` | provision MariaDB + plugin configs |
| `neutralize_aquatech_sc_preview.py` | cod preview в aquatech fish json |

Документация: `scripts/README.md`.

#### 4.3 Deploy flow (полный)

```powershell
# secrets in .apex_deploy.json or env
python scripts/tasks/deploy_apexnodes_sftp.py          # backup → upload → restart
python scripts/tasks/smoke_apex_server.py              # verify
python tools/sync_lodestone_mods.py                  # Lodestone jars
# restart Mohist in Lodestone UI
```

Флаги: `--skip-backup`, `--no-restart`, `--restart-only`, `AQUATECH_SFTP_ONLY=...`.

#### 4.4 FAWE × Mohist

- NPE `ItemUtil` / `BukkitImplAdapter` → `persistent-brushes: false` в `server/plugins/FastAsyncWorldEdit/config.yml`
- Jar patch: `tools/patches/patch_fawe_mohist.py` (IBukkit + TypeProperty)
- На Apex применено, smoke OK

#### 4.5 P2 ops (применено на Apex)

- WG global explosions deny на `world`
- Chunky circle r=500 вокруг spawn (одноразово после смены карты)
- Panel backup rotate при `backup_limit=1`

### 5. Клиент / лаунчер / портал (10–18 авг)

- Портал: `aquateche.store`, Worker deploy, login fixes (fallback host если DNS)
- Launcher: multi-mirror bootstrap, cache-bust, portal auth UI, auto-update banner
- Оптимизация ассетов (18 авг): перестановка зеркал на глобальный Fastly CDN Mojang (`resources.download.minecraft.net`), пул HTTP/2 с коротким таймаутом и мгновенным fallback, предсоздание 256 hex-папок `00`..`ff`, быстрый warm-start по метке `.aquatech_ready`.
- Интерфейс лаунчера (18 авг): полная очистка XAML и C# от эмодзи, брендовый бейдж `AQ`, скрытие сырого IP-адреса и кнопки копирования из карточки сервера.
- Автодобавление сервера (18 авг): автогенерация/обновление `servers.dat` в C# перед запуском + автоматическая регистрация сервера в `net.minecraft.client.multiplayer.ServerList` при старте Minecraft через мод `aqualumen-ui`.
- Pack pipeline: `python tools/publish_client_pack.py` → `python tools/upload_pack_release.py`
- После first-party jar: **всегда** `python tools/sync_lodestone_mods.py`
- Rebuild launcher: см. `.cursor/rules/always-rebuild-launcher.mdc`

### 6. Git-коммиты спринта (для blame)

```
pack-2.9.45  Sync balance with CasesMod, clean rank prefixes, frameless HUD & web modal overlays, forward legacy screens
7e16b6c  Restore Lodestone FTB quests + boner fish fix (1.0.18, pack-2.9.25)
de382d9  Restore workshop quests + stop deploy overwrite (потом откат workshop)
a8b693f  Apex backup rotate fix
70bc316  Apex P2 ops (backup, MySQL smoke, WG/Chunky)
326b1d9  Apex P0/P1 deploy hardening
c6e27a0  pack 2.9.24 StarCatcher fishing + bobber cleanup
… (launcher/portal commits 2a7edd8..b5ac552)
fa5a612  pack 2.9.8 FAWE + aquatech_ui 1.0.3
```

### 7. Открытые задачи (P3, не делать без запроса)

- [ ] Scheduled autobackup на Apex (не только pre-deploy)
- [ ] Richer post-restart smoke (console log peek, plugin load)
- [ ] Chunky progress monitoring / broken chunk audit
- [ ] Rebuild launcher bootstrap после каждого pack bump (если не сделано)
- [ ] Workshop quests `20_ws_*` — **удалены намеренно**, не восстанавливать

### 8. Чеклист верификации для Antigravity

```powershell
# 1. Smoke Apex
python scripts/tasks/smoke_apex_server.py
# ожидание: panel running, aquatech_ui-1.0.18.jar ×1, FAWE brushes false, MySQL OK

# 2. Квесты на Apex — только 4 snbt
# remote: config/ftbquests/quests/chapters/{1,41C738...,57FF37...,7D2835...}.snbt

# 3. Рыбалка in-game
# resource rod (humble) → IU loot после minigame
# boner_rod → starcatcher:* fish, НЕ cod/руды

# 4. Lodestone sync
python tools/sync_lodestone_mods.py
# один aquatech_ui-1.0.18.jar в Lodestone mods

# 5. Client pack
# docs/pack/manifest.json → pack-2.9.25, aquatech_ui-1.0.18.jar
```

### 9. Карта graphify / exploration

Перед широким поиском по коду:

```powershell
python -m graphify query "Apex deploy fishing FTB quests"
python -m graphify explain "FishingLootHandler"
```

После правок Java/kubejs: `python -m graphify update .`

---



