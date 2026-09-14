---
name: minecraft-mod-dev
description: Expert Minecraft 1.20.1 Mod Development agent skill for Forge, Mohist, Fabric, and NeoForge. Specializes in LoliLand and McSkill/Nexteam design systems (LuminousUI, 9-slice MachineScreen, upgrade cartridges pixel-art), industrial machine architecture distilled from Mekanism, Thermal Series and Industrial Upgrade (component tiles, cached recipes, upgrade math, energy bridges), GeckoLib animations, custom blocks, items, tile entities, GUI screens, networking, and Mohist hybrid server compatibility.
---

# Minecraft Mod Development Skill (Minecraft 1.20.1 Forge + Mohist, LoliLand Style)

## 🧠 Системное руководство: Разработка модов Minecraft 1.20.1 (Forge / Mohist) в стилистике LoliLand

### 1. Роль и цель

Ведущий разработчик модов Minecraft с глубокой экспертизой в:
- **Forge 1.20.1** (ForgeGradle 6, Java 17, официальные маппинги Mojang/Parchment).
- **Гибридное ядро Mohist 1.20.1** (Forge + Bukkit/Spigot/Paper API).
- Полный цикл создания мода: от архитектуры и генерации ресурсов до сборки, отладки и тестирования на сервере.
- **Визуальный стиль и дизайн-система проекта LoliLand (LuminousUI)**.

**Главная цель**: Создавать моды без багов и регрессий, которые гарантированно стабильно работают как в одиночной игре, так и на сервере Mohist 1.20.1, органично вписываясь в визуальную стилистику и UI-паттерны LoliLand.

---

### 2. Целевая платформа и технические ограничения

- **Minecraft**: 1.20.1.
- **Загрузчик**: Forge 47.x (стабильная ветка для 1.20.1).
- **Java**: Java 17 (строго обязательно: Forge 1.20.1 не поддерживает рантайм Java 21+).
- **Серверное ядро**: Mohist 1.20.1 (гибрид Forge + Bukkit/Spigot/Paper).
- **Система сборки**: Gradle + ForgeGradle 6.

#### Особенности и ловушки Mohist:
Mohist внедряет слой Bukkit API поверх Forge-рантайма:
- Вся игровая логика мода (блоки, предметы, тайлы, сущности, рецепты, события) реализуется **только через Forge API**.
- **Категорически нельзя** полагаться на Bukkit-события (`org.bukkit.event.*`) для модового контента — Bukkit не знает о внутренних типах модовых блоков и сущностей Forge.
- Избегай нестабильных Mixin-хаков, конфликтующих с динамическим загрузчиком классов Mohist (большинство крашей на гибридах вызваны инвазивными Mixin).
- Не используй вызовы Bukkit напрямую в общем коде мода, за исключением изолированных мостов/адаптеров (`server.bukkit.*`).

---

### 3. Архитектура и структура проекта (Forge 1.20.1)

Стандартная иерархия проекта мода:

```
mods/<modid>/
├── build.gradle                  // ForgeGradle 6 конфигурация
├── src/main/java/net/<domain>/<modid>/
│   ├── <ModName>Mod.java         // Главный класс с аннотацией @Mod(MOD_ID)
│   ├── registry/                 // DeferredRegister для блоков, предметов, BE, Menu, Tabs
│   │   ├── ModBlocks.java
│   │   ├── ModItems.java
│   │   ├── ModBlockEntities.java
│   │   ├── ModMenuTypes.java
│   │   └── ModCreativeTabs.java
│   ├── block/                    // Классы блоков (наследники Block, BaseEntityBlock)
│   ├── block/entity/             // BlockEntity с сериализацией NBT и капсуляцией Energy/Items
│   ├── item/                     // Пользовательские предметы, инструменты, броня
│   ├── inventory/                // AbstractContainerMenu (серверные слоты, sync data)
│   ├── client/                   // Клиентский код (GUI, шейдеры, рендер моделей)
│   │   ├── gui/                  // AbstractContainerScreen (LuminousUI)
│   │   └── render/               // BlockEntityRenderer, слои брони, оверлеи
│   ├── network/                  // SimpleChannel пакеты (S2C, C2S)
│   ├── event/                    // Forge EventBusSubscriber подписчики
│   └── util/                     // Математические утилиты, расчет рейтов, хелперы
└── src/main/resources/
    ├── META-INF/mods.toml        // Манифест мода (обязательно!)
    ├── assets/<modid>/
    │   ├── lang/                 // ru_ru.json, en_us.json (100% покрытие)
    │   ├── textures/             // item/, block/, gui/, entity/
    │   ├── models/item/          // JSON модели предметов
    │   ├── models/block/         // JSON модели блоков
    │   └── blockstates/          // JSON описания вариантов состояний
    └── data/<modid>/
        ├── recipes/              // JSON рецепты крафта
        └── loot_tables/          // Таблицы дропа блоков и сущностей
```

#### Корректная конфигурация `META-INF/mods.toml`:
```toml
modLoader="javafml"
loaderVersion="[47,)"
license="All Rights Reserved"

[[mods]]
modId="<modid>"
version="1.0.0"
displayName="<Display Name>"
authors="AquaTech"
description='''Описание мода.'''

[[dependencies.<modid>]]
    modId="forge"
    mandatory=true
    versionRange="[47,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.<modid>]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.20.1,1.21)"
    ordering="NONE"
    side="BOTH"
```
> **Внимание**: Если объявляется необязательная зависимость от другого мода, обязательно указывай валидный `versionRange` (например `versionRange="[1.0,)"`), иначе Forge интерпретирует пустую строку как несоответствие версий и выдаст `LoadingFailedException`.

---

### 4. Ресурсы: Текстуры, Модели, Blockstate

- **Именование**: Все имена файлов в `assets/` и `data/` — строго в нижнем регистре с подчёркиваниями (`auto_fisher_front_on.png`, никаких заглавных букв!).
- **Цепочка декларации**: `blockstate → model → texture`. Blockstate ссылается на модель, модель определяет текстуры.

#### Модель предмета (`models/item/<item_name>.json`):
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "<modid>:item/<item_name>"
  }
}
```

#### Модель предмета для блока (BlockItem):
```json
{
  "parent": "<modid>:block/<block_name>"
}
```

#### Модель блока (`models/block/<block_name>.json`):
```json
{
  "parent": "minecraft:block/cube",
  "textures": {
    "particle": "<modid>:block/<block_name>_front",
    "down": "<modid>:block/<block_name>_bottom",
    "up": "<modid>:block/<block_name>_top",
    "north": "<modid>:block/<block_name>_front",
    "south": "<modid>:block/<block_name>_side",
    "west": "<modid>:block/<block_name>_side",
    "east": "<modid>:block/<block_name>_side"
  }
}
```

#### Blockstate с ориентацией по сторонам света (`blockstates/<block_name>.json`):
```json
{
  "variants": {
    "facing=north": { "model": "<modid>:block/<block_name>" },
    "facing=south": { "model": "<modid>:block/<block_name>", "y": 180 },
    "facing=west":  { "model": "<modid>:block/<block_name>", "y": 270 },
    "facing=east":  { "model": "<modid>:block/<block_name>", "y": 90 }
  }
}
```

---

### 5. Сложные 3D-модели и Анимации (GeckoLib + Blockbench)

Для многодетальных механизмов, мобов, боссов и анимированного оружия:
1. Создавай геометрию в **Blockbench** (формат GeckoLib Animated Model).
2. Экспортируй модель (`.geo.json`) и анимации (`.animation.json`).
3. В коде используй базовые классы `GeoItem`, `GeoEntity`, `GeoBlockEntity`.
4. Реализуй `AnimatableInstanceCache` через `GeckoLibUtil.createInstanceCache(this)`.
5. Настраивай переключение анимаций контроллера:
```java
@Override
public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(new AnimationController<>(this, "controller", 5, state -> {
        if (state.getAnimatable().isActive()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("work"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
    }));
}
```

---

### 6. Регистрация контента и GUI-архитектура

#### Стандарт `DeferredRegister<T>`:
```java
public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
```

#### Разделение Menu и Screen:
1. **`AbstractContainerMenu`**:
   - Работает на сервере и клиенте.
   - Управляет слотами предметов через `SlotItemHandler`.
   - Синхронизирует целочисленные данные (прогресс, энергия) через `ContainerData` / `addDataSlots`.
   - Реализует `quickMoveStack` (Shift-клик) без зацикливания.
   - Метод `stillValid` должен проверять дистанцию или `!blockEntity.isRemoved()`.
2. **`AbstractContainerScreen`**:
   - Только клиент!
   - Рендерит фон (176×166 или 256×256), шкалы энергии/прогресса, кастомные подсказки.
3. **Регистрация ScreenFactory**:
   - Выполняется строго на `FMLClientSetupEvent`:
   ```java
   MenuScreens.register(ModMenuTypes.FISHER_MENU.get(), FisherScreen::new);
   ```
4. **Открытие GUI на ПКМ**:
   - В методе `Block.use()` проверяй руку:
   ```java
   if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
   if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
       NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
   ```

---

### 7. Комплексная дизайн-система: LoliLand (LuminousUI) и McSkill (Nexteam Tech)

Промышленный стандарт оформления интерфейсов, текстур и пиксель-арта для высокотехнологичных серверов Minecraft 1.20.1 (Forge/Mohist). Объединяет опыт двух ведущих экосистем: модульной рамочной эстетики **LoliLand (LuminousUI)** и декларативного компонентного пайплайна **McSkill HiTech (Nexteam `next_content_core`)**.

#### 7.1. Философия и визуальные архетипы

1. **Nexteam Dark Tech (Промышленный минимализм)**:
   - **Чистая функциональность**: 9-slice масштабируемые контейнеры, отказ от статичных монолитных текстур 176×166 в пользу гибкой компоновки виджетов.
   - **Микро-безели и фаски**: Объем достигается не шумом или размытыми тенями, а строгой светотенью в 1 пиксель (сверху/слева блик, снизу/справа тень).
   - **Точные модули**: Стандартизированные размеры элементов (слоты 18×18, индикаторы энергии 14×42, стрелки прогресса 22×15, жидкостные баки 18×42).
   - **Интерактивность**: Кнопки боковой конфигурации сторон (12×12) с живым 3D-предпросмотром граней механизма и цветовым кодированием портов IO.

2. **LoliLand LuminousUI (Неоновая технологичность и магия)**:
   - **Модульная динамическая сборка окон**: Окна собираются из 4 угловых сегментов (`corner.png`), горизонтальных и вертикальных растягивающихся балок (`horizontal.png`, `vertical.png` по 120 px), накладного шильдика названия (`title/tag.png`) с мягким неоновым бэк-свечением (`title/glow.png`).
   - **Тематическая адаптивность**: Уникальная цветовая гамма под каждую ветку мода (Industrial, Infinity, LoliUtility, Draconic, Blood Magic).
   - **Световые акценты**: Аддитивный блендинг для светящихся контуров, неоновые индикаторы режимов работы, анимированные золотые рамки рулеток и кейсов (`case_glow_lines.png`, `glow/bright.png`).

---

#### 7.2. Точные цветовые палитры (Sampled HEX)

##### А. Dark Tech / Nexteam Базовая палитра GUI
| Элемент | HEX | Назначение |
|:---|:---|:---|
| **Border Dark** | `#0A0A12` / `#060409` | Внешний контур и глубокие разделительные линии |
| **Panel Base** | `#242031` / `#2E2E40` | Внутреннее полотно слотов и фон панелей |
| **Bevel Shadow**| `#181524` | Внутренняя тень слота (верхняя и левая кромка) |
| **Bevel Light** | `#464058` | Внутренний блик слота (нижняя и правая кромка) |
| **Bevel Highlight**| `#6A647C` | Яркий акцент внешнего обрамления контейнера |
| **Track Empty** | `#1B1924` | Неактивная подложка шкал прогресса и энергии |
| **Cyan Frame**  | `#4EF9FF` / `#19FFE1`| Акцентная окантовка выделенных панелей и сокетов |

##### Б. LoliLand LuminousUI Тематические гаммы
| Тема | Основные цвета (HEX) | Акценты и свечение | Назначение |
|:---|:---|:---|:---|
| **Industrial / Tech** | `#0D1825` (navy), `#1A2D3F` (slate), `#5E6B6F` (steel) | `#19FFE1` (cyan neon), `#C2FFD9` (mint) | Механизмы, авто-рыболовы, экстракторы, генераторы |
| **Infinity / Endgame** | `#1A0A0E` (dark ruby), `#6C1414` (deep red), `#D6242A` | `#FF424F` (crimson flame), `#F9DB5E` (gold trim) | Сингулярности, квантовые сборщики, эндгейм-блоки |
| **Utility / Bronze** | `#31211D` (dark timber), `#844D33` (bronze base) | `#BA7E49` (warm copper), `#FFC96B` (amber gold) | Кейсы, утилитарные сундуки, инвентарные панели |
| **Draconic Chaos** | `#050508` (void black), `#1A0826` (cosmic dark) | `#FF8A00` (chaos core), `#6C24B5` (draconic violet) | Реакторы хаоса, энергетические ядра |
| **Magic / Arcane** | `#1F0A1A` (dark violet), `#4A0E17` (blood wine) | `#8E24AA` (arcane rune), `#E53935` (blood glow) | Алтари, набалдашники, руническая матрица |

##### В. Цветовая градация множителей и улучшений (Rate Tiers & Upgrades)
| Тир / Модуль | Основной цвет | Блик (Core) | Аура / Контур | Применение |
|:---|:---|:---|:---|:---|
| **Rate x2** | `#66E12D` (Lime) | `#B4FF73` | `#338A14` | Начальный множитель улова / скорости |
| **Rate x4** | `#3ECFA8` (Mint) | `#8CFCE1` | `#1A7E62` | Продвинутый множитель улова |
| **Rate x8** | `#3EA8E8` (Sky Blue) | `#8CDBFC` | `#1A5E8A` | Высокий множитель улова |
| **Rate x16** | `#606EEB` (Royal Blue) | `#A2ACFC` | `#2D3A9C` | Элитный множитель улова |
| **Rate x32** | `#A44EE8` (Purple) | `#D296FC` | `#5C1F96` | Мастер-множитель улова |
| **Rate x64** | `#E84EC3` (Magenta) | `#FC96E5` | `#961F7A` | Максимальный множитель улова |
| **Speed Upgrade** | `#E8D44D` (Gold) | `#FFFF99` | `#96851F` | Ускоритель цикла механизма |
| **Energy Efficiency** | `#FFD660` (Amber) | `#FFF2AA` | `#9E7A1C` | Снижение энергопотребления (FE/t) |

---

#### 7.3. Архитектура GUI и Nine-Slice Engine (Forge 1.20.1)

Вместо фиксированных монолитных текстур 176×166 используется компонентная архитектура с масштабируемыми 9-slice спрайтами.

##### 1. Спецификация Nine-Slice контейнера (`background.png`)
- Базовый размер спрайта: **24×24 пикселя**.
- Толщина кромки (`border`): **4 пикселя**.
- Внутренняя область масштабирования: 16×16 пикселей.
- Рендеринг в `AbstractContainerScreen` (Forge 1.20.1):
```java
public static void renderNineSliced(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int border, int textureSize) {
    // 4 Угла (без растяжения)
    graphics.blit(texture, x, y, 0, 0, border, border, textureSize, textureSize); // Top-Left
    graphics.blit(texture, x + width - border, y, textureSize - border, 0, border, border, textureSize, textureSize); // Top-Right
    graphics.blit(texture, x, y + height - border, 0, textureSize - border, border, border, textureSize, textureSize); // Bottom-Left
    graphics.blit(texture, x + width - border, y + height - border, textureSize - border, textureSize - border, border, border, textureSize, textureSize); // Bottom-Right

    // 4 Грани (горизонтальные и вертикальные полосы)
    graphics.blit(texture, x + border, y, width - 2 * border, border, border, 0, textureSize - 2 * border, border, textureSize, textureSize); // Top
    graphics.blit(texture, x + border, y + height - border, width - 2 * border, border, border, textureSize - border, textureSize - 2 * border, border, textureSize, textureSize); // Bottom
    graphics.blit(texture, x, y + border, border, height - 2 * border, 0, border, border, textureSize - 2 * border, textureSize, textureSize); // Left
    graphics.blit(texture, x + width - border, y + border, border, height - 2 * border, textureSize - border, border, border, textureSize - 2 * border, textureSize, textureSize); // Right

    // Центральное полотно
    graphics.blit(texture, x + border, y + border, width - 2 * border, height - 2 * border, border, border, textureSize - 2 * border, textureSize - 2 * border, textureSize, textureSize);
}
```

##### 2. Спецификация слотов инвентаря (`slot.png`)
- Размер спрайта слота: **18×18 пикселей**.
- Смещение при рендере: `slot.x - 1, slot.y - 1` относительно координаты ванильного `Slot` (16×16).
- Структура пикселей:
  - Внешний ободок: 1px тёмный контур (`#0A0A12`).
  - Внутренняя фаска: 1px тень сверху и слева (`#181524`), 1px блик снизу и справа (`#464058`).
  - Центральная посадочная зона: 16×16 темный фон (`#242031`).

##### 3. Стандарты виджетов механизмов
- **Шкала прогресса (Progress Bar / Arrow)**:
  - Размер трека: **22×15 пикселей** (`progress_bar.png`).
  - Размер заполнения: **22×16 пикселей** (`progress_bar_active.png`).
  - Тип анимации: Горизонтальный кроп слева направо (`width = progress * 22 / maxProgress`).
  - Смещение Y активного спрайта: `-1px` по вертикали для идеальной стыковки наконечника стрелки.
  - Интерактивность: Клик ЛКМ по прогресс-бару отправляет вызов в рецептурную книгу JEI (`showCategory(RecipeType)`).
- **Шкала энергии (Energy Bar)**:
  - Габариты корпуса: **14×42 пикселя** (`energy_bar.png`).
  - Внутренняя шкала: **12×40 пикселей** (`energy_bar_active.png`), смещение `+1, +1`.
  - Тип анимации: Вертикальный кроп снизу вверх (`height = energy * 40 / maxEnergy`, `yOffset = 40 - height`).
  - Цветовая индикация: Градиент от насыщенного красного (`#E53935`) / неонового циана (`#19FFE1`) до яркого пика.
  - Тултип: Четкое форматирование `§fЭнергия: §e%,d §7/ §6%,d §7FE` с отображением дельты расхода (`§c-%d FE/t`).
- **Жидкостный резервуар (Fluid Tank)**:
  - Габариты корпуса: **18×42 пикселя** (`fluid_tank.png`).
  - Рендеринг: Послойный (Слой 1: фон бака $\to$ Слой 2: тайлинг текстуры флюида через `GuiGraphics.blit` с учетом `FluidStack.getFluid().getFluidType()` $\to$ Слой 3: наложение полупрозрачного стекла с насечками делений `fluid_tank_overlay.png` 18×42).
- **Панель конфигурации сторон (Side Config Panel)**:
  - Кнопки переключения режимов авто-экспорта/импорта: **12×12 пикселей** (`panel_button.png`).
  - 3D-проекция сторон блока: Изометрический или развернутый куб граней (Front, Back, Top, Bottom, Left, Right).
  - Цветовая маркировка режимов портов:
    - `NONE`: Прозрачный / темно-серый (`#404040`).
    - `INPUT`: Лазурный синий (`#2196F3`).
    - `OUTPUT`: Насыщенный оранжевый (`#FF9800`).
    - `ENERGY_IN`: Рубиновый красный (`#F44336`).
    - `BOTH`: Изумрудный зеленый (`#4CAF50`).

---

#### 7.4. Модульная рамочная система LuminousUI

Для больших диалоговых окон, магазинов, аукционов и кейсов используется система сборных рамок Luminous:
- **Угловые элементы (`corner.png`)**: 4 симметричных сегмента с выраженной технологичной фаской или закруглением.
- **Направляющие балки (`horizontal.png`, `vertical.png`)**: Текстурные полосы шириной 120 пикселей, которые растягиваются или тайлятся между углами.
- **Шильдик заголовка (`title/tag.png`)**: Накладная плашка, центрируемая на верхней балке окна с названием блока/сервиса.
- **Неоновая подсветка заголовка (`title/glow.png`)**: Аддитивный слой с альфа-блендингом (`RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)`), создающий эффект мягкого неонового сияния надписью.
- **Анимированные световые линии кейсов (`case_glow_lines.png`, `glow/bright.png`)**: Световые импульсы, циклически пробегающие по контуру рамки при открытии рулетки или активации эндгейм-механизма.

---

#### 7.5. Производственный стандарт пиксель-арта: Upgrade Cartridges (Чипы улучшений)

Стандарт пиксель-арта карточек улучшений (множители рыбалки, скорость, энергоэффективность):

##### 1. Базовые правила геометрии (16×16)
- **Холст**: Ровно 16×16 пикселей. Запрещен сабпиксельный рендеринг, антиалиасинг размытием и полупрозрачные пиксели внутри предмета.
- **Корпус картриджа**: Тёмно-графитовый микрочип (`#131119`), вертикальные стальные направляющие рельсы (`#D8F1ED`).
- **Контактная гребёнка (Gold Pins)**: Ровно 4 золотых контакта (`#D19939` с верхним бликом `#FFFF80`) на правой кромке чипа.
- **Дисплейный карман**: Углубленная прямоугольная матрица цвета мокрого асфальта (`#434851`).
- **Глифы множителей (Typography)**: Высота символов ровно 5 пикселей, ширина 3 пикселя.
- **Светотеневая модель**: Источник света — строго сверху-слева (Top-Left). Внутренний 1px сердечник символа — белый/ярко-пастельный (White-hot core), внешний 1px контур — чистый насыщенный неоновый цвет соответствующего тира.

##### 2. Запреты при создании предметов:
- ❌ Никакого «мыла» и автоматических размытых градиентов (Gaussian Blur / Soft Brush).
- ❌ Никаких одиночных паразитных пикселей («шума» / jaggies). Все пиксели объединены в строгие кластеры.
- ❌ Никаких стандартных фиолетовых "SaaS" градиентов. Используются только проверенные палитры тиров.

---

#### 7.6. Интеграция с JEI (Just Enough Items)
Для удобства игроков каждый механизм должен регистрировать кликабельную область прогресс-бара в JEI:
```java
public class MachineRecipeClickArea implements IGuiContainerHandler<MachineScreen> {
    @Override
    public Collection<IGuiClickArea> getGuiClickAreas(MachineScreen containerScreen, double mouseX, double mouseY) {
        return List.of(IGuiClickArea.createBasic(
            79, 34, 22, 15, // Координаты стрелки прогресса внутри GUI
            RecipeTypes.MACHINE_RECIPE_TYPE // Категория рецептов JEI
        ));
    }
}
```

---

#### 7.7. IU-школа (Industrial Upgrade, com.denfop) — светлая индустриальная классика
Противоположность тёмным Lumen/Dark Tech. Реальная палитра сэмплами из джарки 3.4.0.11:
- **GUI машин — светлые**: полотно `#EAEAEA/#E7E7E7`, тёмные стальные рамы `#4A4E53`, средний металл `#73777D`. Никаких градиентов — плоские зоны + 1px тёмные контуры (школа IC2).
- **Предметные текстуры — жёсткая 4-тональная рампа материала**: блик → насыщенная база → тень → почти чёрный контур. Пример адамантита: `#DF4C19` → `#AC2E03` → `#7E1A0A` → `#2E0600`. Каждый материал (сплав, пластина, корпус) — свой цвет по этой схеме; фаски и «заклёпки» обязательны.
- **Правило рампы**: 1 материал = 4 фиксированных тона; никаких полутонов и антиалиасинга. Двойные пластины/корпуса = та же рампа + выступающий рельеф на 1px.
- **Когда применять**: если механизмы должны читаться «классическим индустриальным» (IC2-наследие), а не неоновыми Lumen/Dark Tech.

---

### 8. Отладка и тестирование на Mohist

#### Инструменты:
- **ConsoleFilterNext**: Фильтрация шума логов и выделение сообщений разрабатываемого мода.
- **Modern Runtime Deobfuscation**: Расшифровка SRG-имён в читаемые методы в логах сервера.

#### Чек-лист проверки перед деплоем:
1. `./gradlew build` собирает jar без ошибок компиляции и без `warnings as errors`.
2. Загрузка jar на сервер Mohist: проверка отсутствия `ClassNotFoundException`, `InvalidMixinException`, `LoadingFailedException`.
3. Все блоки и предметы выдаются через `/give` и видны в креативных вкладках.
4. Механизмы открывают GUI на ПКМ, принимают энергию FE, корректно обрабатывают сырье и отдают продукцию.
5. Инвентарь сохраняется при перезагрузке чанка/сервера (`saveAdditional` и `load` NBT).
6. Авто-выталкивание продукции в сундуки не вызывает крашей при соседстве с модовыми хранилищами (Applied Energistics 2, Industrial Upgrade).

---

### 9. Паттерны промышленных модов: Mekanism 10.4 / Thermal Series 11 / Industrial Upgrade 3.4

Разобраны декомпиляцией реальных продакшн-джарок под 1.20.1 Forge (исходники: `scratch/skill-training/src/`, полный разбор с числами и сниппетами — **`references/industrial-mods-patterns.md`**). Применяй эти паттерны при проектировании машин, энергии, апгрейдов и GUI для AquaTech.

#### 9.1. Компонентная архитектура тайла (у всех трёх модов)
- Логика машины не живёт в одном BlockEntity: энергия, side-config, авто-выброс, апгрейды, redstone — отдельные компоненты (`ITileComponent` у Mekanism, `AbstractComponent` у IU), каждый со своим `read/write NBT` (субтег `component_N`) и `tickServer`.
- Тяжёлая инициализация (скан соседей, регистрация в energy net) — **отложенно на первый серверный тик** (`requestSingleWorldTick`), никогда в `onLoad()` во время загрузки чанка.

#### 9.2. Tick-цикл и lit-состояние
- Статические тикеры `tickServer/tickClient` (Mekanism), подклассы переопределяют только `onUpdateServer()`.
- Переключение blockstate `lit=true/false` — **дебаунсится** (`updateDelay`), чтобы мигание прогресса не спамило block updates. Компаратор — по флагу раз в тик.

#### 9.3. Кэш рецепта + трекинг ошибок (главный паттерн Mekanism `CachedRecipe`)
- Рецепт ищется один раз и кэшируется; инвалидация — по `IContentsListener` при изменении слота, не каждый тик.
- Каждая причина простоя — типизированная ошибка (`NOT_ENOUGH_ENERGY`, `NOT_ENOUGH_INPUT`, `NOT_ENOUGH_OUTPUT_SPACE`), GUI показывает соответствующее предупреждение.
- Энергия списывается **за тик**, не за операцию; прогресс растёт только когда энергия+вход+место под выход всё в порядке.

#### 9.4. Апгрейды: математика Mekanism (эталон)
- `ticksRequired = base * maxUpgradeMultiplier^(-installed(SPEED)/max(SPEED))`; `energyPerTick = base * mult^(2*speed − energy)`; `maxUpgradeMultiplier = 10` (конфиг). Максимумы: SPEED/ENERGY/GAS 8, MUFFLING 4.
- Пересчёт производных полей — только в хуке `recalculateUpgrades(Upgrade)`; при разборке ключом машина сохраняет снапшот через `getUpgradeData()` и восстанавливается.
- Thermal (augments): множители хранятся как `base*` значения и **каждый раз пересчитываются из базы** (`applyModifiers`), чтобы бонусы не накапливались повторным умножением.
- IU (modules): аддитивные проценты по тирам I/II/III (5/10/15%), enum-driven реестр модулей.

#### 9.5. Энергия
- Тиры Mekanism: 4M/16M/64M/256M буфер и 4k/16k/64k/256k FE-t на выход — ×4 за тир на ёмкость и throughput.
- CoFH `EnergyStorageCoFH`: int-FE, fluent `setCapacity/setMaxReceive/setMaxExtract`, creative/enabled как `Supplier`.
- Мост FE к соседям (подход IU, валидирован нашей machines v2): обернуть кап `ForgeCapabilities.ENERGY` каждого соседа один раз в `Sink/Source/SinkSource` и опрашивать по тику.
- Батарейный слот машины: `fillOrConvert` — заполнение контейнера из энергетических предметов + конвертация, выполняется первым тиком.

#### 9.6. Side-config и авто-выброс
- Per-face enum (`NONE/INPUT/OUTPUT/ENERGY/INPUT_OUTPUT`) с цветом для GUI, персист в NBT; экспонирование капабилити — per-side view (helper `forSideWithConfig`).
- Авто-выброс: отдельный компонент (ejector), пушит только грани OUTPUT, симулирует вставку перед коммитом.

#### 9.7. Sync меню→экран
- Ванильные `ContainerData` — только для ≤5 простых int. Mekanism использует типизированные `SyncableInt/Long/ItemStack/FluidStack/Enum.create(getter, setter)` с дельта-проверкой: сервер шлёт только изменившееся.
- Полное состояние — одним пакетом при открытии GUI; IU батчит обновления тайлов («overtime update»).

#### 9.8. GUI-фреймворки
- Mekanism `GuiElement`-дерево: каждый виджет — самостоятельный элемент со своей текстурой/тултипом/кликом (`GuiRightArrow`, `GuiBigLight`, `GuiSideHolder`, `GuiInnerScreen`); динамический текст — в recessed-панель, не `drawString` напрямую.
- CoFH `ElementBase`: энергия рендерится вертикальным кропом снизу вверх — та же математика, что в наших машинах; панель аугментов с drag-drop.
- Прогресс-стрелка кликабельна → JEI-категория (уже применено в 7.3).

#### 9.9. Прочие продакшн-детали
- Wrench: `tryWrench() → WrenchResult` (rotate / dismantle с сохранением апгрейдов / security deny / pass); security-проверка перед открытием GUI.
- `RedstoneControl` enum (DISABLED/HIGH/LOW/PULSE) на каждой машине, персист в NBT.
- Звук работающей машины — зацикленный ambient, включается только при `isActive`.
- Серверные тултипы (PowerTier и т.п.) строятся из состояния компонент, не хардкодом.
