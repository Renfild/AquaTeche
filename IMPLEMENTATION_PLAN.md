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











