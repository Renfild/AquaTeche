# Industrial Upgrade — руководство из мода

Источник: `IndustrialUpgrade-1.20.1-3.4.0.11.jar` · GuideBookCore · 366 записей · язык ru_ru + en_us

## Книга-гайд (предмет)

Руководство: Промышленная модернизация

## Вкладки

- **Обзор** (`main`) — Основная информация: 24 записей
- **Примитив** (`primal`) — Примитивная эра: 24 записей
- **Пар** (`steam`) — steam: 38 записей
- **Электрика** (`baseElectric`) — baseElectric: 59 записей
- **Продвинутая** (`advancedElectricTab`) — Продвинутая электрическая эра: 93 записей
- **Улучшенная** (`improvedElectricTab`) — Улучшенная электрическая эра: 104 записей
- **Совершенная** (`perElectric`) — perElectric: 24 записей

---

## Обзор — Основная информация

### Начало

- **id:** `start`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:book/guide_book`
- **icon field:** `book`

**Описание (RU):**

Приветствую тебя, игрок, с началом прохождения данного мода. Этот гайдбук будет подсказывать и направлять тебя для изучения мода. Информация структурирована создателем мода и является точной. Однако она может меняться в зависимости от версии. В данном планшете вы можете просматривать и выполнять задания. Задание "обнаружение" означает, что вам следует иметь указанные ресурсы в инвентаре для выполнения. Чтобы выполнить задание с жидкостью, в инвентаре должен быть жидкостный предмет с нужной жидкостью. Также можно иметь жидкость или предметы отдельно. Если предметы не указаны — задание можно выполнить сразу. Эта вкладка "Начало" поможет вам ознакомиться с базовыми понятиями мода. Рекомендуется пройти их, прежде чем задавать некоторые вопросы. Также у мода есть аддоны, которые помогут в развитии — они указаны сверху справа в планшете. Вы также можете присоединиться к комьюнити мода: предлагать идеи, помогать новичкам или задать вопрос. В случае нахождения багов/крашей/дюпов, сообщите разработчику через Discord или GitHub — ссылки находятся в левом верхнем углу планшета. Задания также называют квестами. Квесты можно передвигать в любые стороны, как вам удобно, аналогично достижениям в Minecraft. Некоторые задания скрыты — сначала нужно выполнить предыдущие.

**Description (EN):**

Welcome, player, to the beginning of this mod. This guidebook will assist and direct you in exploring the mod. The information is structured by the mod creator and is accurate, though it may vary depending on the version. In this tablet, you can view and complete quests. A "detection" quest means you must have the specified resources in your inventory. For fluid-based quests, you must have a fluid container with the required fluid. You may also have the fluid or items separately. If no items are listed, the quest can be completed immediately. This "Getting Started" tab will help you understand the basic concepts of the mod. It’s recommended to go through it before asking questions. The mod also has addons to help with progression — they are shown in the top-right of the tablet. You can also join the mod's community: suggest ideas, help newcomers, or ask questions. If you encounter bugs/crashes/dupes, report them to the developer via Discord or GitHub — links are in the top-left of the tablet. Quests are also known as tasks. Tasks can be moved freely like advancements in Minecraft. Some quests are hidden — you must complete previous ones first.

### Энергия мода

- **id:** `energy`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:ef/reader`
- **icon field:** `efReader`

**Описание (RU):**

В данном моде используется уникальная энергия — Energy Flux (EF). Она не соединяется с механизмами, работающими на FE (Forge Energy). Чтобы соединить EF с другими типами энергии, используйте конвертер из Power Utilities (сверху справа планшета). Энергия передаётся по проводам. Каждый провод имеет максимальную пропускную способность на один механизм (например, если выход 50 EF, а два механизма потребляют 32 и 18 EF, и провод поддерживает до 32 EF, то он не перегорит, так как ни один канал не превышает лимит). Также есть система трансформаторов, которая работает иначе, чем в других модах. Энергия передаётся по тиковой системе, максимальный уровень определяется по формуле: 8 * 4^tier. Провода могут перегореть, а механизмы — взорваться.

**Description (EN):**

This mod uses a unique type of energy — Energy Flux (EF). It does not connect to machines powered by FE (Forge Energy). To link EF with other energy types, use the converter from Power Utilities (top right of the tablet). Energy is transmitted via wires. Each wire has a maximum capacity per machine (e.g., if the output is 50 EF, and two machines consume 32 and 18 EF, and the wire supports up to 32 EF, it won’t burn out, as no single channel exceeds the limit). The transformer system also works differently than in other mods. Energy is tick-based, and the max level is calculated by the formula: 8 * 4^tier. Wires can burn out, and machines can explode.

### Система нагревания и охлаждения

- **id:** `heat`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:basemachine3/cooling`
- **icon field:** `cooling`

**Описание (RU):**

В моде добавлена система нагрева и охлаждения механизмов. Есть два типа нагрева: 1. нагрев от рецепта, 2. нагрев для рецепта. Для второго используются нагреватели (жидкостный или электрический). Примитивные и паровые механизмы можно нагреть, установив под них лаву. В нагревателях можно задать максимальную температуру — при этом увеличится расход жидкости или энергии. Нагрев от рецепта чаще встречается в мультимеханизмах или карьерах (например, Simply Quarries). Охлаждаются механизмы с помощью холодильников (твёрдотельных, жидкостных, электрических). Нагрев также зависит от биома: в жарких — нагрев выше, в холодных — охлаждение быстрее. Можно использовать модули, заменяющие нагреватели/холодильники, а также специальные трубы — для передачи тепла, холода или сразу обоих.

**Description (EN):**

The mod adds a heating and cooling system for machines. There are two types of heating: 1. recipe-based heating, 2. heating required for a recipe. For the second type, use heaters (fluid or electric). Primitive and steam machines can be heated by placing lava beneath them. Heaters can be configured with a max temperature — this increases fluid or energy consumption. Recipe-based heating is more common in multiblocks or quarries (e.g., Simply Quarries). Cooling is done using solid-state, fluid, or electric coolers. Heating is biome-dependent: hotter biomes heat faster, colder ones cool faster. Modules can replace heaters/coolers, and special pipes are used to transfer heat, cold, or both.

### Система генерации руд

- **id:** `vein`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:basaltheavyore/galena`
- **icon field:** `heavyore`

**Описание (RU):**

В моде отключён спавн стандартных руд — вместо этого используется система жил. Жила — это область генерации руд со своими особенностями. 1. Жилу можно определить по камушкам на поверхности. Чем больше разных камней, тем больше жила. До 5 камней — 1–3 стака руды (маленькая), 5–10 — 3–7 стаков (средняя), 10–15 — 7–15 стаков (большая). При наведении на камень видно проценты генерации — это можно сопоставить с объёмом руды. Минеральная руда (всегда 50%) появляется только в центре жилы и не считается в общем количестве. Её можно переработать в минеральном сепараторе. Также можно получить руду в процентном соотношении, разрушая эти камни молотом из ферромарганца. Копать под камушками безопасно — под ними 100% будет руда. Обычно жилы встречаются на высоте от 40 до 70. Формы жил: сфера, тор, линейная и кубическая.

**Description (EN):**

Vanilla ore spawn is disabled in this mod — instead, ore veins are used. A vein is a special generation area with unique characteristics. 1. You can identify a vein by surface rocks. The more types of rocks, the larger the vein. Up to 5 rocks — 1–3 stacks of ore (small), 5–10 — 3–7 stacks (medium), 10–15 — 7–15 stacks (large). When hovering over a rock, you’ll see generation percentages — this indicates ore volume. Mineral ore (always 50%) spawns only in the center and doesn't count toward the total. It can be processed in a mineral separator. You can also get ore from rocks using a ferromanganese hammer. It’s safe to dig under surface rocks — ore is guaranteed underneath. Veins typically appear between Y levels 40 and 70. Vein shapes: sphere, torus, linear, and cubic.

### Виды энергии

- **id:** `energies`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:imp_se_gen/imp_se_gen`
- **icon field:** `imp_se_generator`

**Описание (RU):**

В моде существует множество видов энергии. Квантовая энергия (QE) используется для квантовых механизмов, создаётся только квантовыми генераторами или конвертерами Power Utilities. Конвертация: 16 EF = 1 QE. Солнечная энергия (SE) — генерируется солнечными генераторами, зависит от положения солнца. Можно настраивать через модули. Энергия опыта (EE) создаётся из жидкого опыта, который можно сконвертировать в обычный через механизмы (например, хранилище опыта) или получать из карьеров, печей или напрямую с игрока. Радиационная энергия — опасна, будет описана позже. Если механизм с ней сломать, радиация останется в чанке и повредит территорию — потребуется очистка. Энергии пара и биоматерии используются аналогично — для питания механизмов. Ночная энергия — обратная версия солнечной. Энергия тока (A, амперы) нужна для зарядки предметов чистой энергией. Энергия позитронов используется в циклотроне для получения тяжёлых элементов.

**Description (EN):**

The mod features multiple types of energy. Quantum Energy (QE) is used for quantum machines and is only generated by quantum generators or Power Utilities converters. Conversion rate: 16 EF = 1 QE. Solar Energy (SE) is generated by solar generators and depends on sun position. It can be configured via modules. Experience Energy (EE) is made from liquid experience, which can be converted using machines (like experience storage), or obtained from quarries, furnaces, or directly from the player. Radiation energy is dangerous and will be detailed later. Breaking a machine with radiation leaves contamination in the chunk — requiring cleanup. Steam and biomass energy are used similarly — to power machines. Night Energy is the inverse of solar energy. Current energy (A, Amperes) is used to charge items with pure energy. Positron energy is used in cyclotrons to create heavy elements.

### Система радиации

- **id:** `radiation`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:crafting_elements/crafting_40_element`
- **icon field:** `crafting_elements`

**Описание (RU):**

Радиация — одна из самых опасных вещей в моде. Обычно обнаруживается в чанках с помощью дозиметра. Возникает из-за загрязнения: разрушения механизмов с радиацией или наличия радиоактивных жил. Часто это уранит и эвксанит. Нахождение в заражённом чанке приводит к накоплению радиации в теле. Чтобы избавиться от неё — используйте радиопротектор или умрите. Используйте защитный костюм. Если радиация сильнее — потребуется улучшенная версия костюма. Накопление радиации влияет на геймплей: игрок получает эффекты в зависимости от её уровня.

**Description (EN):**

Radiation is one of the most dangerous mechanics in the mod. It’s usually detected in chunks using a dosimeter. It appears due to contamination from broken radiation machines or radioactive veins, often uranite and euxenite. Being in an irradiated chunk causes radiation buildup in your body. To remove it, use a radioprotector or die. Use a protective suit. Stronger radiation requires an upgraded suit. Accumulated radiation affects gameplay — players receive effects based on radiation level.

### Вулканы

- **id:** `volcano`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:blockbasalts/basalt`
- **icon field:** `basalts`

**Описание (RU):**

Вулканы — это очень большие структуры, которые появляются только в горных массивах. Их высота варьируется от 50 до 120 блоков. В старых версиях внутри вулканов можно найти сундуки с ресурсами. Очень опасно находиться внутри без термостойкой брони — на игрока накладывается эффект отравленного воздуха. Из базальта можно получить фторводород. Также во вулканах можно добыть серу и борную руду. Некоторые жилы руд также генерируются внутри вулкана. Внутри есть лава, которую можно собрать.

**Description (EN):**

Volcanoes are massive structures that only spawn in mountainous regions. Their height ranges from 50 to 120 blocks. In older versions, chests with resources could be found inside. It is very dangerous to enter without heat-resistant armor — you’ll receive poisoned air effects. Basalt can be used to obtain hydrofluoric acid. You can also mine sulfur and boron ores inside volcanoes. Some ore veins generate within volcanoes. There is also lava inside, which can be collected.

### Загрязнение воздуха и почвы

- **id:** `pollution`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:pollution_device`
- **icon field:** `pollutionDevice`

**Описание (RU):**

Загрязнение происходит в результате работы механизмов. Загрязнение воздуха увеличивается каждую секунду в радиусе 10 чанков. Каждые 5 минут уровень загрязнения снижается в 2 раза — это также касается и почвы. Однако загрязнение не расширяется. На 2 уровне загрязнения почвы игрок получает эффект медлительности на 10 секунд, на 3 уровне — слабость на 10 секунд, на 4 уровне — тошноту на 10 секунд. Эти эффекты могут накладываться одновременно. При загрязнении воздуха: на 2 уровне — тошнота, на 3 — слепота, на 4 — отравление газом. Проверить уровень загрязнения можно с помощью сканера загрязнения. Устранить загрязнение воздуха можно с помощью воздухоразделительной установки, а загрязнение почвы — с помощью соответствующих очистительных механизмов. Загрязнение почвы также влияет на рост растений в чанке.

**Description (EN):**

Pollution is caused by machine operation. Air pollution increases every second within a 10-chunk radius. Every 5 minutes, pollution levels halve — this applies to soil as well. Pollution does not spread. At soil pollution level 2, the player gains slowness for 10 seconds, at level 3 — weakness, at level 4 — nausea. These effects may overlap. Air pollution effects: level 2 — nausea, level 3 — blindness, level 4 — gas poisoning. Use a pollution scanner to check levels. Use an air separator to clean air pollution, and soil purifier machines for soil pollution. Soil pollution also affects plant growth in the chunk.

### Пчёлы

- **id:** `bee`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:jar_bee/bees`
- **icon field:** `jarBees`

**Описание (RU):**

Пчёлы — один из самых важных компонентов развития. Они обладают уникальной механикой. Сначала игроку нужно найти улей — они генерируются в соответствующих биомах. Чтобы получить пчелу, необходимо сломать улей сачком. При этом вы обязательно получите урон. Из улья выпадет королева, а также рабочие пчёлы. Рабочие бывают четырёх типов: 1. Рабочие — опыляют агрокультуры и производят мёд и маточное молоко. 2. Лекари — лечат больных пчёл. 3. Строители — создают потомство.  4. Защитники/Атакующие — защищают пасеку или нападают на другие. Для лечения пчёл требуется маточное молоко. Каждая пчела нуждается в потреблении мёда. Пчёлы могут сражаться между собой, а также помогать друг другу — передавать мёд, маточное молоко или лечить. Для опыления необходимо использовать агрокультуры из данного мода, посаженные на жёрдочках. Пчёлы могут мутировать спустя 5–10 минут реального времени. Всего существует 19 генов для мутаций. Можно использовать рамки для улучшения условий жизни пчёл.   Мёд и маточное молоко можно собирать, но после каждой процедуры потребуется подождать 10 операций сбора. Для развития личинок также нужно маточное молоко. Пчёлы могут умирать от старости. Королева умирает, когда погибает последний трутень.   Помимо стандартного производства мёда и маточного молока, пчёлы могут производить продукцию агрокультур в зависимости от количества опылений в тик. Чем чаще опыляют, тем выше шанс, что пчела мутирует в определённый тип. Например, пчела может быть на 30% золотой, 20% алмазной, 10% изумрудной, 70% железной или 30% иридиевой. Это работает со всеми агрокультурами мода, если это не основной цветок пчелы. Для получения дополнительной информации используйте анализатор пчёл.

**Description (EN):**

Bees are one of the most important parts of progression. They have a unique mechanic. First, find a hive — they spawn in appropriate biomes. To obtain a bee, break a hive with a net. You will take damage. The hive drops a queen and worker bees. Workers come in four types: 1. Workers — pollinate crops and produce honey and royal jelly. 2. Healers — heal sick bees. 3. Builders — breed offspring. 4. Guards/Attackers — protect the hive or attack others. Royal jelly is required to heal bees. Each bee consumes honey. Bees can fight or help each other — sharing honey, royal jelly, or healing. To pollinate, use mod-specific crops planted on stakes. Bees may mutate after 5–10 minutes of real time. There are 19 genes for mutations. Use frames to improve bee living conditions. Honey and royal jelly can be collected, but require 10 collection operations between each. Larvae also require royal jelly to grow. Bees can die of old age. The queen dies when the last drone dies. In addition to producing honey and royal jelly, bees can produce crop products depending on pollination rate per tick. The more they pollinate, the higher the chance to mutate into specific types. For example, a bee might be 30% gold, 20% diamond, 10% emerald, 70% iron, or 30% iridium. This works for all mod crops except the bee’s main flower. Use a bee analyzer for more details.

### Растения/Агрокультуры/Мультикультуры

- **id:** `crop`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:crops/crops`
- **icon field:** `crops`

**Описание (RU):**

Каждое растение должно быть посажено на основной блок через жёрдочку. Все растения можно садить только на жёрдочках. Растение может превратиться в бурьян. Загрязнение (радиоактивное, почвы и воздуха) влияет на скорость роста растения. Рост можно ускорить с помощью пчёл или удобрений из мода. Со временем растение может мутировать — существует 18 генов для модификации. Для селекции используйте двойную жёрдочку (устанавливается повторной установкой жёрдочки поверх другой), а затем посадите нужные растения. Возможные комбинации можно узнать через JEI.   Бурьян необходимо удалять мотыгой. Также можно использовать пестициды через механизм "Очиститель полей".

**Description (EN):**

Each plant must be planted on a base block using a trellis. All plants can only be planted on trellises. A plant may turn into weeds. Pollution (radioactive, soil, and air) affects the growth rate of the plant. Growth can be accelerated using bees or fertilizers from the mod. Over time, a plant can mutate — there are 18 genes for modification. Mutation may lead to higher yield, resistance to diseases, or faster growth. However, negative mutations are also possible. Plants can crossbreed if placed next to each other. Crossbreeding results in a hybrid that inherits genes from both parents. Some rare crops can only be obtained through this method. You can use the plant analyzer to check the genes, health, and mutation rate. Some crops require specific conditions like light level, temperature, or soil type. There are three main types: regular crops, agro-cultures, and multi-cultures. Multi-cultures are combinations of several crops in one and produce multiple types of yield. They are harder to maintain but more efficient in the long run. Avoid over-polluting the area or the plants may die.

### Подземные газовые жилы

- **id:** `gasvein`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:bucket/gas`
- **icon field:** `gasBlock`

**Описание (RU):**

В моде присутствуют подземные жилы газообразных веществ: брома, хлора, иода и фтора. Они генерируются на высотах от 5 до 70 блоков и могут содержать от 40000 до 240000 мБ жидкости. Для их обнаружения используйте газовый сенсор. При нахождении в потоке газа обязательно надевайте защитный костюм — в противном случае вы получите эффект отравления газом.

**Description (EN):**

The mod features underground veins of gaseous substances: bromine, chlorine, iodine, and fluorine. They generate between Y-levels 5 and 70 and can contain between 40,000 and 240,000 mB of fluid. To detect them, use a gas sensor. When standing in a gas flow, always wear a protective suit — otherwise, you'll suffer from gas poisoning effects.

### Минеральные / Нефтяные / Газовые жилы

- **id:** `mineralvein`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:mineral/crystal`
- **icon field:** `mineral`

**Описание (RU):**

С помощью буровой установки можно обнаруживать различные типы жил. Их появление зависит от биома: • В горных массивах чаще всего встречаются минеральные жилы.  • В жарких биомах — нефтяные жилы.  • В холодных — газовые жилы (природный газ). Для проверки наличия жилы исследуйте каждый чанк. Минеральные жилы добываются с помощью анализатора с установленным модулем "Карьер" и анализаторными сундуками по бокам. Также можно использовать карьеры из Simply Quarries, квантовый карьер или его беспроводную версию (способную добывать до 12 жил одновременно). Для добычи нефти используйте станок-качалку. Его можно ускорить с помощью модуля "Производимость" и настроить на беспроводную передачу ресурсов. Газовые жилы (природный газ) добываются при помощи газовой установки, которую также можно улучшать и сделать беспроводной. Для особых газов — фтора, хлора, брома и иода — используется газовая скважина. С её помощью исследуются чанки, шанс обнаружения жилы составляет 25%.

**Description (EN):**

Using a drilling rig, you can detect various types of underground veins. Their appearance depends on the biome: • In mountainous regions, mineral veins are the most common. • In hot biomes — oil veins. • In cold biomes — gas veins (natural gas). To check for a vein, explore each chunk. Mineral veins are extracted using an analyzer with a "Quarry" module and analyzer chests on the sides. You can also use Simply Quarries’ quarries, the Quantum Quarry, or its wireless version (which can mine up to 12 veins simultaneously). To extract oil, use a pump jack. It can be upgraded with a "Productivity" module and configured for wireless resource transfer. Gas veins (natural gas) are extracted using a gas extractor, which can also be upgraded and made wireless. Special gases — fluorine, chlorine, bromine, and iodine — require a gas well. Use it to scan chunks; the chance of discovering a vein is 25%.

### Гевея

- **id:** `rubber_tree`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:sapling/rubber_sapling`
- **icon field:** `rubberSapling`

**Описание (RU):**

Генерация гевеи была кардинально изменена. Теперь существует 4 вида гевеи: обычная, две болотные и тропическая. Как видно из названий, они генерируются в соответствующих биомах. Также, по порядку слева направо можно оценить их качество: • У обычной гевеи складки редкие и долго восстанавливаются. • У болотных — больше складок и быстрее восстановление. • У тропической — максимальное количество складок и самая быстрая регенерация. Латекс добывается с помощью краника — скручивайте им складки на стволе. Сначала выпадет сырой латекс. Его необходимо переработать в жидкий, а затем высушить, чтобы получить готовый латекс.

**Description (EN):**

Rubber tree generation has been drastically changed. There are now 4 types: regular, two swamp variants, and tropical. As their names suggest, they generate in appropriate biomes. From left to right, their quality improves: • Regular rubber trees have few latex spots that regenerate slowly. • Swamp variants have more spots and faster regeneration. • Tropical ones have the most latex spots and the fastest recovery. Latex is extracted using a tree tap — use it to twist the latex spots on the trunk. Raw latex is obtained first, which must then be processed into liquid latex, and finally dried to get usable latex.

### Торф / Селитра / Кальций

- **id:** `other_features`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:nitrate_mud/nitrate_mud`
- **icon field:** `ore2`

**Описание (RU):**

Особые компоненты для развития в моде. Спавнятся во всех водоёмах, заменяя глину. Являются ключевыми ресурсами для химических процессов и удобрений.

**Description (EN):**

Special components essential for progression in the mod. They spawn in all water bodies, replacing clay. These are key resources for chemical processes and fertilizers.

### Нефтяные жилы

- **id:** `oil_vein`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:veinoil/oil`
- **icon field:** `oilblock`

**Описание (RU):**

В моде также реализована генерация нефти. Существует 6 видов нефти: от лёгкой до тяжёлой и от сладкой до кислой. Каждый тип нефти даёт разные продукты при переработке. Нефтяные жилы генерируются только в пустынных биомах.

**Description (EN):**

The mod also includes oil generation. There are 6 types of oil: ranging from light to heavy and from sweet to sour. Each type yields different products when processed. Oil veins only generate in desert biomes.

### Жители

- **id:** `villager`
- **вкладка:** `main`
- **предмет:** `minecraft:villager_spawn_egg`

**Описание (RU):**

В моде доступно 6 видов жителей: механик, инженер, ядерщик, металлург, химик и ботаник. 

- Механик: требует механизм генератор. Продаёт компоненты для создания механизмов и инструменты, взаимодействующие с ними. 
- Инженер: требует примитивный программируемый стол. Продаёт компоненты для схем и датчиков. 
- Химик: требует примитивный жидкостный интегратор. Продаёт химические компоненты. 
- Ботаник: требует пасеку. Продаёт семена, пчёл и компоненты для агрокультур и пчеловодства. 
- Ядерщик: требует обогатитель. Продаёт компоненты, связанные с радиоактивностью и реакторами. 
- Металлург: требует наковальню из данного мода. Продаёт слитки, пластины и сплавы.

**Description (EN):**

The mod includes 6 types of villagers: mechanic, engineer, nuclear scientist, metallurgist, chemist, and botanist.

- Mechanic: requires a generator. Sells components for machines and tools that interact with them.
- Engineer: requires a primitive programmable table. Sells components for circuits and sensors.
- Chemist: requires a primitive fluid integrator. Sells chemical components.
- Botanist: requires an apiary. Sells seeds, bees, and items for agriculture and beekeeping.
- Nuclear Scientist: requires an enricher. Sells components related to radioactivity and reactors.
- Metallurgist: requires an anvil from this mod. Sells ingots, plates, and alloys.

### Пипетка

- **id:** `pipette`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:pipette`
- **icon field:** `pipette`

**Описание (RU):**

Используется для опустошения баков в механизмах. Наведитесь на бак в интерфейсе и нажмите ПКМ. Чтобы очистить пипетку, нажмите ШИФТ + ПКМ.

**Description (EN):**

Used to empty tanks inside machines. Hover over a tank in the interface and right-click. To clear the pipette, press SHIFT + right-click.

### Система хранения

- **id:** `storage_system`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:storagesystem/controller`
- **icon field:** `storageSystem`

**Описание (RU):**

В моде добавлена система хранения предметов и жидкостей. Для работы системы нужен контроллер, который преобразует EF в собственную энергию системы и поддерживает её работу. Для хранения ресурсов используются блоки хранения ячеек, сами ячейки и терминал. Блоки можно подключать как напрямую друг к другу, так и через кабель. Каждый блок системы, кроме контроллера, создаёт узел соединения, поэтому провод между соседними блоками ставить не обязательно. Система может хранить до 300 уникальных типов предметов и до 50 типов жидкостей. Для автокрафта нужен терминал шаблонов и сам шаблон. Существуют два режима шаблонов: верстак и механизм. В режиме верстака задаётся обычный рецепт верстака, а в режиме механизма — рецепт для выбранного механизма. Максимальное количество входов и выходов — 36. Чтобы поместить предмет или блок в рецепт, возьмите его в руки и нажмите по слоту. Если нужно добавить жидкость, возьмите капсулу или любой жидкостный предмет, содержащий нужную жидкость, и нажмите по слоту. Средняя кнопка мыши переключает режим слота и обратно. Для изменения количества используйте прокрутку колёсика мыши. Если нужно изменить количество быстрее, используйте Shift, Ctrl или обе клавиши вместе. Когда рецепт настроен, нажмите на стрелку под шаблоном. Шаблон верстака помещается в интерфейс верстака, а шаблон механизма — в интерфейс механизмов. Для работы автокрафта также нужен процессор. Каждый уровень процессора может выполнять 2^(n - 1) процессов и не требует отдельного количества памяти. Например, обычный процессор может выполнять 1 крафт независимо от его размера. Для просмотра жидкостей используйте жидкостный терминал, а для просмотра автокрафта — терминал автокрафтов. Для внешнего импорта и экспорта ресурсов используйте соответствующие шины для предметов и жидкостей. У каждого типа шин есть свои уникальные режимы: чёрный и белый список у шин импорта, режим загрузки ресурсов у шин экспорта — по очереди или случайно, а также настройка работы по редстоун-сигналу для всех шин.

**Description (EN):**

The mod adds a storage system for items and fluids. The system requires a controller, which converts EF into the system's own energy and keeps it running. Storage cell blocks, the cells themselves, and a terminal are required to store resources. Blocks can be connected directly to each other or through cables. Every system block, except the controller, creates a connection node, so cables are not required between adjacent blocks. The system can store up to 300 unique item types and up to 50 fluid types. Autocrafting requires a pattern terminal and a pattern. There are two pattern modes: crafting table and machine. Crafting table mode is used for regular crafting table recipes, while machine mode is used for recipes of selected machines. The maximum number of inputs and outputs is 36. To place an item or block into a recipe, hold it in your hand and click the slot. To add a fluid, hold a capsule or any fluid container that contains the required fluid and click the slot. Use the middle mouse button to switch the slot mode and switch it back. Use the mouse wheel to change the amount. To change the amount faster, use Shift, Ctrl, or both keys together. When the recipe is configured, press the arrow under the pattern. Crafting table patterns are placed into the crafting table interface, while machine patterns are placed into the machine interface. Autocrafting also requires a processor. Each processor tier can run 2^(n - 1) processes and does not require a separate memory amount. For example, a basic processor can run 1 craft regardless of its size. Use the fluid terminal to view fluids, and the autocrafting terminal to view autocrafting tasks. To import and export resources externally, use the corresponding item and fluid buses. Each bus type has its own modes: blacklist and whitelist for import buses, resource insertion mode for export buses — sequential or random — and redstone signal control for all buses.

### Полёт на планеты, спутники и астероиды

- **id:** `space_worlds`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:space/planetary_translocator`
- **icon field:** `planetary_translocator`

**Описание (RU):**

После изучения объекта на 100% вы сможете путешествовать на разные космические объекты, но возможность полёта зависит от нескольких условий. Сначала нужно создать планетарный транслятор. Зарядите его и откройте интерфейс. В интерфейсе выберите систему и нужную планету. Чтобы открыть спутники планеты, нажмите на неё. После этого нажмите кнопку телепортации. Для телепортации должны выполняться обязательные условия. Главное требование — наличие электрической брони из мода, от нано до спектральной. Также обязательно нужен модуль кислорода для брони. Далее идут дополнительные требования. При исследовании объектов для роверов могли требоваться модули тепла или холода. Модуль тепловой защиты является аналогом такого требования для брони: сколько модулей было указано в столе исследования, столько же должно быть установлено в броню. Если в мире есть давление, потребуется модуль давления. При телепортации расходуется энергия, количество которой зависит от расстояния до Земли. Счётчик слева внизу подсказывает необходимую информацию. Если вы случайно выбросите нужный предмет, вас телепортирует обратно на Землю. Если заряд закончится, вас также телепортирует на Землю. На астероидах можно найти другие руды.

**Description (EN):**

After researching an object to 100%, you can travel to different space objects, but the flight depends on several conditions. First, you need to craft a Planetary Translocator. Charge it and open its interface. In the interface, select a system and the desired planet. To open a planet's moons, click the planet. After that, press the teleport button. Several required conditions must be met before teleportation. The main requirement is electric armor from the mod, from nano armor to spectral armor. An oxygen module for the armor is also required. There are additional requirements as well. During object research, rovers may have required heat or cold modules. The thermal protection module is the armor equivalent of that requirement: the same number of modules shown in the research table must be installed in the armor. If the world has pressure, a pressure module is required. Teleportation consumes energy depending on your distance from Earth. The counter in the lower-left corner shows the required information. If you accidentally drop the required item, you will be teleported back to Earth. If the charge runs out, you will also be teleported back to Earth. Asteroids contain different ores.

### Механический рецептор

- **id:** `recipe_schedule`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:recipe_schedule`
- **icon field:** `recipe_schedule`

**Описание (RU):**

Предмет с сохранённой настройкой рецепта. Переносит выбранный режим обработки между машиной и настройщиком.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Симулятор реакторов

- **id:** `reactor_simulate`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:basemachine3/simulation_reactors`
- **icon field:** `simulation_reactors`

**Описание (RU):**

Позволяет создавать схемы реакторов разных типов в идеальных условиях. Внутри вы можете выбрать тип и вид реактора. После выбора будет доступен инвентарь — поместите в него компоненты и запустите симуляцию. После запуска можно просмотреть характеристики работы реактора (слева по центру [i]). Также возможно записать схему в кодировщик реакторных схем — просто положите его внутрь, и данные будут записаны.

**Description (EN):**

Allows you to build reactor schemes under ideal conditions. Inside, you can choose the reactor type and variant. After choosing, an inventory becomes available — place components inside and start the simulation. After starting, reactor performance metrics can be viewed (center-left [i]). You can also write the scheme into a reactor schematic template — simply insert it inside, and the data will be saved.

### Система работы реактора

- **id:** `reactor_logic`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:reactors/quad_mox_fuel_rod`
- **icon field:** `quad_mox_fuel_rod`

**Описание (RU):**

Система реактора проработана детально. Каждый компонент влияет на стабильность схемы. Существует 3 уровня стабильности:

- **Стабильная**: компоненты работают без перегрева.
- **Нестабильная**: требуется дополнительное охлаждение, иначе произойдёт взрыв по истечении таймера.
- **Очень нестабильная**: рекомендуется немедленно отключить реактор, шанс взрыва — 100%.

Реакторы имеют уровни, прокачиваемые через энергию. Если уровень реактора ниже уровня компонента, то компонент невозможно вставить.

Стержни не истощаются, но излучают радиацию и тепло. Цель — свести тепло к минимуму. Радиоактивность можно удалить через **защитный купол**, а тепло — через компоненты:
- **Пластина** — блокирует тепло, но повышает его на 5–20% за клетку.
- **Теплообменник** — забирает много тепла, но сам не охлаждается.
- **Теплоотвод** — немного охлаждается, забирает тепло.
- **Компонентный теплоотвод** — охлаждает другие компоненты, но может разрушиться при перегреве.
- **Конденсатор** — берёт на себя тепло, не охлаждается.
- **Охлаждающий стержень** — пассивный охладитель, требует перезаправки.
- **Энергосоединитель** — соединяет стержни, увеличивает выход энергии на 50–65%, но увеличивает тепло на 0–20%.
- **Нейтронный протектор** — аналог энергосоединителя, но надёжнее и снижает тепловыделение на 5–20%.

**Description (EN):**

The reactor system is deeply simulated. Each component affects the stability of the scheme. There are 3 stability levels:

- **Stable**: components operate without overheating.
- **Unstable**: requires extra cooling, otherwise an explosion will occur after the timer expires.
- **Highly Unstable**: immediate shutdown is recommended, 100% explosion chance.

Reactors have levels that can be upgraded with energy. If a component's level is higher than the reactor’s level, it cannot be inserted.

Fuel rods do not deplete but emit radiation and heat. The goal is to minimize heat. Radiation can be removed via a **protective dome**, and heat via components:
- **Plate** — blocks heat, but increases heat generation by 5–20% per cell.
- **Heat Exchanger** — absorbs lots of heat but does not cool itself.
- **Heat Vent** — cools slightly and absorbs heat.
- **Component Heat Vent** — cools adjacent components but can break from overheating.
- **Capacitor** — absorbs heat, does not cool.
- **Coolant Rod** — passive cooler, needs refilling.
- **Energy Coupler** — connects rods, increases energy output by 50–65% but also increases heat by 0–20%.
- **Neutron Protector** — similar to energy connector, more reliable and reduces heat generation by 5–20%.

### Космос

- **id:** `space`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:basemachine3/research_table_space`
- **icon field:** `research_table_space`

**Описание (RU):**

Космос — это огромная и увлекательная часть геймплея в данном моде. Однако он не такой привычный, как может показаться. Здесь вы не будете просто летать на планеты, добывать руду вручную или сражаться с боссами. Космос представлен в виде симуляции, где игрок делает определённые шаги для его исследования. Каждый космический объект имеет два параметра: уровень линзы и процент исследования.

Что такое уровень линзы? Для исследования требуется специальная линза — от 1 до 7 уровня. Они создаются по мере вашего прогресса в моде. Каждый уровень линзы открывает доступ к новым планетам и спутникам, а с ними — к новым жидкостям и рудам.

Для начала исследований необходимо отправлять экспедиции. Каждая экспедиция увеличивает процент исследования на 5%. Таким образом, для полного исследования (100%) потребуется 20 успешных экспедиций. Для этого используются космические аппараты — всего их четыре: марсоход, зонд, спутник и ракета. У каждого из них есть четыре типа — от обычного до совершенного.

В информационной панели каждого объекта указано, какие аппараты можно использовать. Поместите нужный аппарат в ракетную площадку и нажмите кнопку «Отправить». Также вы можете просматривать ресурсы, доступные для добычи, но они открываются по мере роста процента исследования. Некоторые ресурсы можно получить только определёнными аппаратами.

Слева внизу находится информационная панель, где отображаются все важные параметры для успешного запуска экспедиции: время полёта, необходимое топливо и его уровень, уровень аппарата, а также необходимые модули.

**Description (EN):**

Space is a vast and fascinating part of the gameplay in this mod. However, it’s not as straightforward as it may seem. Here, you won’t simply fly to planets, mine ores manually, or fight bosses. Space is represented as a simulation where the player takes specific steps to explore it. Each celestial object has two parameters: lens level and exploration percentage.

What is the lens level? To explore, you need a special lens, ranging from level 1 to 7, crafted as you progress through the mod. Each lens level unlocks new planets and moons, along with new fluids and ores.

To start exploration, you send expeditions. Each expedition increases exploration percentage by 5%. Thus, it takes 20 successful expeditions to reach 100% exploration. There are four types of spacecraft: rover, probe, satellite, and rocket. Each comes in four variants — from basic to advanced.

The information panel for each object shows which spacecraft are available. Place the desired craft on the launch pad and press the "Send" button. You can also view which resources are available to mine, but they unlock progressively with exploration percentage. Some resources can only be collected by specific crafts.

At the bottom left is an info panel showing all the key parameters for a successful expedition: flight duration, required fuel and its level, spacecraft level, and necessary modules.

### Колонии

- **id:** `colony`
- **вкладка:** `main`
- **предмет:** `industrialupgrade:colonial_building/low_house`
- **icon field:** `colonial_building`

**Описание (RU):**

Каждая колония имеет свой уровень и накапливает опыт, который вырабатывается рабочими. С повышением уровня становятся доступны новые здания для улучшения. Список доступных зданий зависит от текущего уровня колонии. Чтобы добавить здание, возьмите его в руку и нажмите на слот в правом верхнем углу.

Недостаток рабочих приведёт к проблемам с производительностью и обеспечением едой. Один рабочий на фабрике производит 2 единицы еды. Шахта позволяет добывать ресурсы, а жидкостная установка — жидкости. С 7 уровня становится доступен параметр «счастье рабочих». Чем ниже счастье, тем ниже эффективность труда.

Для добычи ресурсов и жидкостей необходимо построить склад. Чтобы отправить ресурсы, нажмите кнопку «Отправить». Передача ресурсов не требует топлива или энергии. Также есть возможность автоматизировать этот процесс соответствующей кнопкой.

Обязательно создавайте генераторы энергии и кислорода — это критически важно! При возникновении проблем у вас будет всего 30 секунд на их решение. В противном случае колония будет уничтожена. Защита — обязательный элемент: без неё колония будет разрушаться. Все текущие проблемы отображаются в левом нижнем углу интерфейса. Также вы можете просматривать склад и список ресурсов и жидкостей, доступных для добычи на текущем уровне.

**Description (EN):**

Each colony has a level and accumulates experience produced by workers. As the level increases, new buildings for upgrades become available. The available buildings depend on the colony’s current level. To add a building, hold it in your hand and click the slot at the top right.

A shortage of workers causes problems with food and productivity. One worker in a factory produces 2 units of food. Mines allow resource extraction, and fluid plants handle liquids. Starting from level 7, the "worker happiness" indicator becomes available. The lower the happiness, the lower the efficiency.

To gather resources and fluids, you need to build a warehouse. To send resources, press the "Send" button. Resource transfer requires no fuel or energy. You can also automate the process with a special button.

It’s crucial to create energy and oxygen generators! If problems arise, you’ll have only 30 seconds to fix them. Otherwise, the colony will be destroyed. Defense is mandatory: without it, the colony will deteriorate. All current issues appear in the bottom left of the interface. You can also view the warehouse and the list of resources and fluids available for extraction at the current level.

---

## Примитив — Примитивная эра

### Наковальня

- **id:** `anvil`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:block_anvil/block_anvil`

**Описание (RU):**

Ручная рабочая станция для ранней обработки металла. Позволяет ковать простые детали и заготовки до появления электрических станков.

**Description (EN):**

A manual workstation for early metal processing. It lets the player forge simple parts and blanks before electric machines are available.

### Кузнечный молот ← `anvil`

- **id:** `forge_hammer`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:forge_hammer`

**Описание (RU):**

Ручной инструмент для ковки. Используется в ранней металлообработке, когда нужно превратить материал в пластину, корпус или простую заготовку.

**Description (EN):**

A hand tool for forging. It is used to turn materials into plates, casings or simple blanks during early metalworking.

### Медная оболочка ← `forge_hammer`

- **id:** `casings`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:casing/copper`

**Описание (RU):**

Корпусная заготовка для механизмов. Даёт машине прочную основу, в которую потом устанавливаются рабочие узлы, слоты и энергетические части.

**Description (EN):**

A casing blank for machines. It forms the strong body that later holds working parts, slots and energy components.

### Контроллер плавильни ← `casings`

- **id:** `smelterystart`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:smeltery/smeltery_controller`

**Описание (RU):**

Создайте компоненты плавильни — необходимое их количество указано на самих предметах. Установите основной блок — контроллер — и соберите конструкцию. Завершающим шагом нажмите ПКМ по контроллеру. В топливные баки заливайте лаву или базальтовую лаву. Базальтовая лава даёт x1.5 ускорение. Поместите слитки, блоки или руды в плавильную печь, подождите — и ингредиенты расплавятся. Более подробное описание работы плавильни указано в самом контроллере в верхнем левом углу интерфейса.

**Description (EN):**

Create the components of the smeltery — the required number of each is indicated on the items themselves. Place the main block — the controller — and assemble the structure. As a final step, right-click the controller. Fill the fuel tanks with lava or basalt lava. Basalt lava provides a 1.5x speed boost. Insert ingots, blocks, or ores into the smeltery furnace, wait, and the ingredients will melt. More detailed information on how the smeltery works is shown in the top-left corner of the controller interface.

### Отливочный блок плавильни ← `smelterystart`

- **id:** `smelteryforms`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:smeltery/smeltery_casting`

**Описание (RU):**

Необходимые компоненты для создания отливок размещаются в отливочном блоке. Если рецепт вам не нужен, рекомендуется убрать форму!

**Description (EN):**

Required components for casting are placed into the casting block. If you don't need a recipe, it is recommended to remove the mold!

### electrum ← `smelteryforms`

- **id:** `electrum`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:itemingots/electrum_ingot`

**Описание (RU):**

Смешайте в плавильне золото и серебро.

**Description (EN):**

Mix gold and silver in the smeltery.

### Примитивный жидкостный интегратор ← `electrum`

- **id:** `squeezer`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:primal_fluid_integrator/primal_fluid_integrator`

**Описание (RU):**

Выжимает сырьё и растительные материалы, отделяя из них жидкую или мягкую фракцию. Полезен для латекса, органики и ранней химической переработки.

**Description (EN):**

Squeezes raw and plant materials, separating liquid or soft fractions from them. It is useful for latex, organics and early chemistry.

### Примитивная сушилка латекса ← `squeezer`

- **id:** `dryer`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:dryer/dryer`

**Описание (RU):**

Сушит влажные материалы и подготавливает их к дальнейшей обработке. Убирает лишнюю жидкость там, где обычная плавка или крафт не подходят.

**Description (EN):**

Dries wet materials and prepares them for further processing. It removes excess liquid when normal smelting or crafting is not enough.

### raw latex ← `dryer`

- **id:** `raw_latex`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:raw_latex`

**Описание (RU):**

Добывается с гевеи с помощью краника.

**Description (EN):**

Obtained from rubber trees using a tree tap.

### Латекс ← `raw_latex`

- **id:** `latex`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:crafting_elements/crafting_290_element`

**Описание (RU):**

Получается путём сушки сырого латекса.

**Description (EN):**

Produced by drying raw latex.

### Примитивный нагреватель жидкости ← `electrum`

- **id:** `primal_heater`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:primal_fluid_heater/primal_fluid_heater`

**Описание (RU):**

Простой нагреватель жидкостей. Работает с внутренним баком и доводит жидкость до нужного горячего состояния для ранних технологических цепочек.

**Description (EN):**

A simple fluid heater. It works with an internal tank and brings fluids to a hot state for early processing chains.

### steam ← `primal_heater`

- **id:** `steam`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:bucket/steam`

**Описание (RU):**

Нажмите ПКМ капсулой с водой по нагревателю.

**Description (EN):**

Right-click the heater with a water fluid cell.

### superheated steam ← `steam`

- **id:** `superheated_steam`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:bucket/superheated_steam`

**Описание (RU):**

Нажмите ПКМ капсулой с паром по нагревателю.

**Description (EN):**

Right-click the heater with a steam fluid cell.

### Ферромарганцевый слиток ← `smelteryforms`

- **id:** `ferromanganese`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:alloyingot/ferromanganese`

**Описание (RU):**

Смешайте марганец и железо в плавильне.

**Description (EN):**

Mix manganese and iron in the smeltery.

### molot ← `ferromanganese`

- **id:** `molot`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:energy/molot`

**Описание (RU):**

Тяжёлый ручной молот для грубой обработки прочных материалов. Подходит для операций, где обычного молота уже недостаточно.

**Description (EN):**

A heavy manual hammer for rough processing of strong materials. It is used when a basic hammer is no longer enough.

### Кучка алмазной пыли ← `molot`

- **id:** `diamond`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:smalldust/diamond`

**Описание (RU):**

Разбивайте камни кристальной жилы, чтобы с некоторым шансом получить алмазную руду.

**Description (EN):**

Break the stones of the crystal vein to get a chance of obtaining diamond ore.

### Примитивный сжиматель ← `ferromanganese`

- **id:** `compressor`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:compressor/compressor`

**Описание (RU):**

Сжимает материалы в плотные формы: пластины, блоки, прессованные заготовки и похожие детали. Используется для рецептов, где давление важнее нагрева.

**Description (EN):**

Compresses materials into dense forms such as plates, blocks and pressed blanks. It is used where pressure matters more than heat.

### Примитивный прокатный механизм ← `compressor`

- **id:** `primal_rolling`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:basemachine3/rolling_machine`

**Описание (RU):**

Прокатывает металл в листы и тонкие заготовки. Нужен для пластин, корпусов и деталей, которые должны иметь ровную форму.

**Description (EN):**

Rolls metal into sheets and thin blanks. It is used for plates, casings and parts that need a flat shape.

### Примитивный изолятор проводов ← `primal_rolling`

- **id:** `primal_wire_insulator`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:primal_wire_insulator/primal_wire_insulator`

**Описание (RU):**

Наносит изоляцию на проводники. Превращает голый провод в безопасный кабель для подключения к энергосети.

**Description (EN):**

Applies insulation to conductors. It turns bare wire into safe cable for power networks.

### Примитивный дробитель ← `ferromanganese`

- **id:** `macerator`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:macerator/macerator`

**Описание (RU):**

Дробит руды, камень и материалы в пыль или измельчённые заготовки. Даёт сырьё для промывки, плавки, химии и дальнейшего разделения.

**Description (EN):**

Crushes ores, stone and materials into dust or crushed blanks. It prepares material for washing, smelting, chemistry and separation.

### flint dust ← `macerator`

- **id:** `flint_dust`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:itemdust/silicon_dust`

**Описание (RU):**

Положите кремний в дробитель и раздробите его.

**Description (EN):**

Put flint into the macerator and grind it.

### silicon handler ← `flint_dust`

- **id:** `silicon_handler`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:itemdust/silicon_dioxide_dust`

**Описание (RU):**

Обрабатывает кремниевое сырьё и выращивает пригодный кристалл. Это отдельная стадия перед точной обработкой кремния для электроники.

**Description (EN):**

Processes silicon material and grows a usable crystal. This is a separate stage before precise silicon processing for electronics.

### Примитивный жидкостный интегратор ← `silicon_handler`

- **id:** `primal_fluid_integrator`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:primal_fluid_integrator/primal_fluid_integrator`

**Описание (RU):**

Пропитывает предмет жидкостью из бака. Используется для реакций, где твёрдая заготовка должна вобрать раствор или химический реагент.

**Description (EN):**

Soaks an item with fluid from its tank. It is used when a solid blank must absorb a solution or chemical reagent.

### Информация о примитивной эре

- **id:** `primal_information`
- **вкладка:** `primal`
- **предмет:** `industrialupgrade:book/book_iu`
- **icon field:** `book`

**Описание (RU):**

В данной эре некоторые механизмы имеют скилл, который ускоряет их работу от 50% до 400%. В наковальне игрок с определённым шансом может выполнить рецепт повторно. Для нагрева некоторых механизмов используйте лаву под ними. Для починки механизмов зажмите Shift и кликните ПКМ. Механизмы не имеют графического интерфейса — все действия нужно выполнять вручную. Заливка и опустошение жидкостей осуществляется через ПКМ.

**Description (EN):**

In this age, some machines have a skill system that increases their operation speed by 50% to 400%. In the anvil, a player has a chance to perform the recipe a second time based on skill level. To heat certain machines, place lava underneath them. To repair machines, hold Shift and right-click. These machines have no GUI — all actions must be performed manually. Use right-click to pour or drain fluids.

---

## Пар — steam

### steam machine block

- **id:** `steam_machine_block`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:blockresource/steam_machine`

**Описание (RU):**

Паровые механизмы потребляют пар только от паровых котлов (обычных и мультиблочных), преобразователей пара и торфяного парогенератора. Других источников нет. Получать и передавать пар можно только через размещение рядом с источником или с помощью паровых труб. Перегретый пар нельзя передавать паровыми трубами — используйте жидкостные трубы или капсулы. Для нагрева механизмов используйте лаву под ними. Для работы требуется обычный пар и нужный уровень давления. Если давление выше или ниже требуемого — механизм не будет работать.

**Description (EN):**

Steam machines consume steam only from steam boilers (regular and multiblock), steam converters, and the peat steam generator. No other sources are valid. Steam can only be transferred by placing machines next to a source or using steam pipes. Superheated steam cannot be transferred via steam pipes — use fluid pipes or fluid cell instead. Lava must be placed under machines to provide heat. Machines require both regular steam and correct pressure to operate. If the pressure is too high or low, they will not work.

### Паровой котёл ← `steam_machine_block`

- **id:** `steamboiler`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steamboiler`

**Описание (RU):**

Работает каждые 3 тика, потребляет 2 мб воды и производит 2 мб пара.

**Description (EN):**

Operates every 3 ticks, consumes 2 mB of water, and produces 2 mB of steam.

### Паровой преобразователь давления ← `steamboiler`

- **id:** `steampressureconverter`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steampressureconverter`

**Описание (RU):**

Для работы требуется нагрев от лавы под ним. Используйте кнопки "+" и "-" для регулировки давления от 0 до 4. Если давление не требуется — установите 0.

**Description (EN):**

Requires lava heating underneath. Use "+" and "-" buttons to adjust pressure from 0 to 4. If pressure is not needed, lower it to 0.

### silicon crystal ← `steampressureconverter`

- **id:** `silicon_crystal`
- **вкладка:** `steam`

**Описание (RU):**

Кремниевая заготовка для электронной цепочки. После точной обработки становится основой для плат и более сложных схем.

**Description (EN):**

A silicon blank for electronics. After precise processing it becomes a base for boards and advanced circuits.

### Примитивный лазерный полировщик ← `silicon_crystal`

- **id:** `primal_laser_polisher`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:primal_laser_polisher/primal_laser_polisher`

**Описание (RU):**

Полирует кристаллы и точные детали лазером. Нужен там, где материалу требуется чистая поверхность без грубой механической обработки.

**Description (EN):**

Polishes crystals and precise parts with a laser. It is used when a clean surface is needed without rough mechanical damage.

### Паровой полировщик ← `primal_laser_polisher`

- **id:** `steam_polisher`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_sharpener`

**Описание (RU):**

Паровая полировальная машина. Использует паровую систему для аккуратной обработки кристаллов и деталей без электрического питания.

**Description (EN):**

A steam-powered polishing machine. It processes crystals and parts without electric power.

### Паровой преобразователь тока ← `steam_polisher`

- **id:** `steam_ampere_generator`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_ampere_generator`

**Описание (RU):**

Генерирует электричество из пара. Требуется лава под механизмом.

**Description (EN):**

Generates electricity from steam. Requires lava underneath.

### Паровой электролизёр ← `steam_ampere_generator`

- **id:** `steam_electrolyzer`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_electrolyzer`

**Описание (RU):**

Для работы требуется электричество и лава под ним. Жидкости забираются с помощью капсул или жидкостных труб.

**Description (EN):**

Requires electricity and lava underneath to operate. Use fluid cells or fluid pipes to extract fluids.

### oxygen ← `steam_electrolyzer`

- **id:** `oxygen`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:bucket/oxygen`

**Описание (RU):**

Кислород используется как окислитель и реагент в химических машинах: электролизёрах, газовом комбайнере, жидкостных интеграторах и разделителях. Он участвует в получении оксидов, кислот и некоторых продуктов из рудных или космических материалов.

**Description (EN):**

Oxygen is an oxidizer and reagent used by electrolyzers, gas combiners, fluid integrators and separators. It takes part in producing oxides, acids and some products from ore or space materials.

### Газовая камера ← `oxygen`

- **id:** `primal_gas_chamber`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:gas_chamber/primal_gas_chamber`

**Описание (RU):**

Проводит реакции с газами внутри рабочей камеры. Работает с газовыми реагентами и предметами, когда обычных жидкостных машин недостаточно.

**Description (EN):**

Runs reactions with gases inside a work chamber. It handles gas reagents and items when fluid machines are not enough.

### sulfurtrioxide ← `primal_gas_chamber`

- **id:** `sulfurtrioxide`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:bucket/sulfurtrioxide`

**Описание (RU):**

Триоксид серы получается в газовом комбайнере из сернистого оксида и кислорода. Затем он смешивается с водой для серной кислоты и используется в серной химической цепочке.

**Description (EN):**

Sulfur trioxide is produced in the gas combiner from sulfur oxide and oxygen. It is then mixed with water to make sulfuric acid and is used in sulfur chemistry.

### fluidcoppersulfate ← `sulfurtrioxide`

- **id:** `fluidcoppersulfate`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:bucket/coppersulfate`

**Описание (RU):**

Раствор медного сульфата — жидкий медьсодержащий реагент. Используется в жидкостных интеграторах и смесителях для обработки меди, плат и химических заготовок.

**Description (EN):**

Copper sulfate solution is a copper-bearing liquid reagent. It is used in fluid integrators and mixers for copper, circuit board and chemical material processing.

### Печатная плата ← `fluidcoppersulfate`

- **id:** `circuit_board`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_487_element`

**Описание (RU):**

Пустая плата для электроники. На неё наносятся дорожки и устанавливаются компоненты, прежде чем плата станет рабочей схемой.

**Description (EN):**

A blank board for electronics. Traces and components are added to it before it becomes a working circuit.

### Примитивный программированный стол ← `circuit_board`

- **id:** `primal_programming_table`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:primal_programming_table/primal_programming_table`

**Описание (RU):**

Стол для программирования простых плат. Записывает управляющую логику в подготовленную плату и превращает её в рабочий электронный компонент.

**Description (EN):**

A table for programming simple boards. It writes control logic into a prepared board and turns it into an electronic component.

### Программированная печатная плата ← `primal_programming_table`

- **id:** `programmed_circuit_board`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_488_element`

**Описание (RU):**

Плата с записанной логикой управления. Используется в механизмах и электронных сборках как готовый управляющий элемент.

**Description (EN):**

A circuit board with stored control logic. It is used as a ready control part in machines and electronic assemblies.

### Паровой сепаратор ← `programmed_circuit_board`

- **id:** `steam_handler_ore`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_handler_ore`

**Описание (RU):**

Паровая машина для обработки тяжёлых руд. Разделяет плотное сырьё на полезные части без электрической сети.

**Description (EN):**

A steam machine for heavy ore processing. It separates dense ore material without requiring an electric network.

### Угольная пыль с примесью ← `steam_handler_ore`

- **id:** `impurity_coal_dust`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_498_element`

**Описание (RU):**

Угольная пыль с примесями. Это промежуточный материал: её очищают или смешивают, чтобы получить пригодную углеродную смесь.

**Description (EN):**

Coal dust with impurities. It is an intermediate material that must be cleaned or mixed into a usable carbon blend.

### Угольная пыль для сплавов ← `impurity_coal_dust`

- **id:** `alloy_coal_dust`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_499_element`

**Описание (RU):**

Подготовленная углеродная смесь для металлургии. Даёт контролируемый углеродный состав при работе со сталью и сплавами.

**Description (EN):**

A prepared carbon blend for metallurgy. It provides controlled carbon content for steel and alloy work.

### Стальная оболочка ← `alloy_coal_dust`

- **id:** `steel`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:casing/steel`

**Описание (RU):**

Прочный конструкционный металл для корпусов, инструментов и машин. Хорошо подходит для механизмов, которым уже недостаточно ранних металлов.

**Description (EN):**

A strong structural metal for casings, tools and machines. It supports mechanisms that are too demanding for early metals.

### steel hammer ← `steel`

- **id:** `steel_hammer`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:energy/steel_hammer`

**Описание (RU):**

Прочный молот для ручной обработки жёстких материалов. Служит дольше ранних молотов и лучше подходит для стальных заготовок.

**Description (EN):**

A durable hammer for harder materials. It lasts longer than early hammers and suits steel blanks.

### Укреплённая наковальня ← `steel`

- **id:** `block_strong_anvil`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:block_strong_anvil/block_strong_anvil`

**Описание (RU):**

Усиленная наковальня для тяжёлой ручной обработки. Рассчитана на прочные сплавы и операции, где обычная наковальня быстро ограничивает производство.

**Description (EN):**

A reinforced anvil for heavy manual processing. It is made for strong alloys and operations that exceed a basic anvil.

### Тугоплавкая печь ← `block_strong_anvil`

- **id:** `refractory_furnace`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:refractory_furnace/refractory_furnace`

**Описание (RU):**

Плавит руды и материалы в расплавленные металлы. Работает с огнеупорной обработкой и выдаёт жидкий металл для литейных процессов.

**Description (EN):**

Melts ores and materials into molten metals. It handles high-temperature processing and outputs liquid metal for casting.

### Мини-плавильня ← `refractory_furnace`

- **id:** `mini_smeltery`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:mini_smeltery/mini_smeltery`

**Описание (RU):**

Небольшая литейная установка. Принимает расплавленный металл и отливает из него базовые твёрдые заготовки.

**Description (EN):**

A compact casting unit. It accepts molten metal and casts it into basic solid blanks.

### Стол для пайки электроники ← `steel`

- **id:** `primal_soldering_mechanism`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:primal_soldering_mechanism/primal_soldering_mechanism`

**Описание (RU):**

Механизм пайки для ранней электроники. Соединяет платы, контакты и мелкие детали в готовые электронные компоненты.

**Description (EN):**

A soldering mechanism for early electronics. It joins boards, contacts and small parts into finished electronic components.

### Паровой преобразователь пара ← `primal_soldering_mechanism`

- **id:** `steam_converter`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_converter`

**Описание (RU):**

Генерирует каждые 2 тика: потребляет 1 мб воды, производит 2 мб пара.

**Description (EN):**

Operates every 2 ticks, consumes 1 mB of water, and produces 2 mB of steam.

### Паровой генератор на торфе ← `steam_converter`

- **id:** `steam_peat_generator`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_peat_generator`

**Описание (RU):**

Генерирует каждый тик: потребляет 1 мб воды, производит 2 мб пара.

**Description (EN):**

Operates every tick, consumes 1 mB of water, and produces 2 mB of steam.

### Паровая помпа ← `steam_converter`

- **id:** `steam_pump`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_pump`

**Описание (RU):**

Паровой насос для перекачки жидкостей. Двигает воду, лаву и другие жидкости без электрического двигателя.

**Description (EN):**

A steam-powered pump for moving fluids. It moves water, lava and other fluids without an electric motor.

### Контроллер парового котла ← `steam_pump`

- **id:** `steam_boiler_controller`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:steam_boiler/steam_boiler_controller`

**Описание (RU):**

Генерирует каждый тик: потребляет 1 мб воды, производит 2 мб пара. Для работы требуется нагрев от нагревательного механизма. В теплообменнике должно быть 1 ферритовый кольцевой индуктор и 10 катушек.

**Description (EN):**

Operates every tick, consumes 1 mB of water, and produces 2 mB of steam. Requires heating from a heating machine and the heat exchanger must contain 1 ferrite ring inductor and 10 coils.

### Паровое хранилище ← `steam_boiler_controller`

- **id:** `steam_storage`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_storage`

**Описание (RU):**

Передаёт пар только со стороны, с которой был установлен. С других сторон только отправляет пар.

**Description (EN):**

Transfers steam only from the side it was placed from; sends steam out through other sides.

### Паровой карьер ← `steam_storage`

- **id:** `steam_quarry`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_quarry`

**Описание (RU):**

Паровой карьер. Использует пар и буровые трубы, добывая блоки из рабочей области по мере продвижения вниз.

**Description (EN):**

A steam quarry. It consumes steam and drill pipes while mining blocks from its working area downward.

### Улучшенный паровой карьер ← `steam_quarry`

- **id:** `adv_steam_quarry`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/adv_steam_quarry`

**Описание (RU):**

Улучшенный паровой карьер. Работает по той же схеме с трубами и паром, но добывает быстрее и удобнее обычной версии.

**Description (EN):**

An improved steam quarry. It keeps the pipe-and-steam mining logic while working faster and more conveniently.

### Паровой зарядник кристаллов ← `primal_soldering_mechanism`

- **id:** `steam_crystal_charge`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_crystal_charge`

**Описание (RU):**

Потребляет электричество.

**Description (EN):**

Consumes electricity.

### Паровой нагреватель жидкостей ← `steam_crystal_charge`

- **id:** `steam_fluid_heater`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:basemachine3/steam_fluid_heater`

**Описание (RU):**

Преобразует обычный пар в перегретый. Использует пар, предназначенный для паровых механизмов.

**Description (EN):**

Converts regular steam into superheated steam. Uses steam intended for steam machines.

### titanium steel ← `steam_crystal_charge`

- **id:** `titanium_steel`
- **вкладка:** `steam`

**Описание (RU):**

Прочный сплав для деталей с высокой нагрузкой. Выдерживает температуру и износ лучше обычной стали.

**Description (EN):**

A strong alloy for high-load parts. It resists heat and wear better than ordinary steel.

### Стол для сборки электроники ← `titanium_steel`

- **id:** `electronics_assembler`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:electronics_assembler/electronics_assembler`

**Описание (RU):**

Собирает электронные компоненты из плат, проводников и мелких деталей. Удобен для повторяемой сборки схем и модулей.

**Description (EN):**

Assembles electronic components from boards, conductors and small parts. It is useful for repeated circuit and module production.

### Электросхема ← `electronics_assembler`

- **id:** `electronic_circuit`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_272_element`

**Описание (RU):**

Базовая электронная схема. Содержит простую управляющую логику для электрических машин и улучшений.

**Description (EN):**

A basic electronic circuit. It provides simple control logic for electric machines and upgrades.

### Обшивка корпуса ← `electronic_circuit`

- **id:** `machine_casing`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:crafting_elements/crafting_137_element`

**Описание (RU):**

Корпус машины, внутри которого размещаются рабочие слоты, энергия и механические части. Используется как основа большинства механизмов.

**Description (EN):**

A machine body that holds working slots, energy and mechanical parts. It is the base of most mechanisms.

### Контроллер доменной печи ← `machine_casing`

- **id:** `blast_furnace_main`
- **вкладка:** `steam`
- **предмет:** `industrialupgrade:blastfurnace/blast_furnace_main`

**Описание (RU):**

Главный блок доменной печи. Управляет многоблочной плавкой материалов, которым нужна высокая температура.

**Description (EN):**

The main block of the blast furnace. It controls multiblock smelting for materials that require high temperature.

---

## Электрика — baseElectric

### Электромотор

- **id:** `elemotor`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_276_element`

**Описание (RU):**

Электродвигатель для механизмов с движущимися узлами. Превращает электрическую энергию в рабочее механическое движение.

**Description (EN):**

An electric motor for machines with moving parts. It turns electric energy into mechanical motion.

### Жидкостный нагреватель ← `elemotor`

- **id:** `liqued_heater`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/fluid_heat`

**Описание (RU):**

Электрический нагреватель жидкостей. Греет содержимое бака и используется в процессах, где важна температура самой жидкости.

**Description (EN):**

An electric fluid heater. It heats tank contents for processes where the fluid temperature matters.

### Генератор ← `liqued_heater`

- **id:** `generator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/generator_iu`

**Описание (RU):**

Сжигает твёрдое топливо и производит EF. Простая энергетическая машина для питания ранней электрической сети.

**Description (EN):**

Burns solid fuel and produces EF. It is a simple power source for early electric networks.

### Редстоуновый генератор ← `generator`

- **id:** `redstone_generator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/redstone_generator`

**Описание (RU):**

Производит EF из редстоун-материалов. Даёт компактную энергию из редстоуна без жидкого топлива.

**Description (EN):**

Produces EF from redstone materials. It provides compact power from redstone without liquid fuel.

### Геотермальный генератор ← `generator`

- **id:** `geogenerator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/geogenerator_iu`

**Описание (RU):**

Производит EF из горячих жидкостей, прежде всего лавы. Работает как источник энергии для геотермальной линии.

**Description (EN):**

Produces EF from hot fluids, mainly lava. It is the power source for the geothermal line.

### Твердотельный холодильник ← `geogenerator`

- **id:** `solid_refrigerator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/solid_cooling`

**Описание (RU):**

Твердотельный холодильник создаёт холод из снежков, снега, льда, плотного льда и синего льда. Предмет расходуется, заполняет буфер холода на заданное время, а затем холод отдаётся в систему охлаждения механизмов.

**Description (EN):**

Creates cooling power from snowballs, snow, ice, packed ice and blue ice. The item is consumed, fills the cooling buffer for a set time, and the stored cooling is then supplied to machine cooling systems.

### base machines ← `solid_refrigerator`

- **id:** `base_machines`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:blockresource/machine`

**Описание (RU):**

Базовый набор электрических переработчиков: дробление, сжатие, извлечение, плавка, прокатка, резка и выдавливание. Это основа автоматической обработки ресурсов.

**Description (EN):**

The core set of electric processors: crushing, compressing, extracting, smelting, rolling, cutting and extruding. They form the base of automated resource processing.

### Производитель электросхем ← `base_machines`

- **id:** `generator_microchip`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine/generator_microchip`

**Описание (RU):**

Микросхема управления генераторами. Используется как электронный контроллер для энергетических машин.

**Description (EN):**

A control chip for generators. It acts as the electronic controller for power machines.

### Завод сплавов ← `generator_microchip`

- **id:** `alloy_smelter`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine/alloy_smelter`

**Описание (RU):**

Плавит несколько материалов вместе и получает сплавы. Работает с рецептами, где обычная печь не может смешать металлы правильно.

**Description (EN):**

Smelts several materials together into alloys. It handles recipes that a normal furnace cannot mix correctly.

### Механический штамп ← `alloy_smelter`

- **id:** `gearing`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:moremachine3/gearing`

**Описание (RU):**

Изготавливает шестерни и зубчатые детали. Такие детали нужны машинам, где движение передаётся через механические узлы.

**Description (EN):**

Manufactures gears and toothed parts. These parts are used by machines that transfer motion mechanically.

### Электрический сборщик электроники ← `gearing`

- **id:** `electronic_assembler`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/electronic_assembler`

**Описание (RU):**

Автоматически собирает электронные детали из плат, проводов и компонентов. Ускоряет производство сложной электроники.

**Description (EN):**

Automatically assembles electronic parts from boards, wires and components. It speeds up advanced electronics production.

### Химический завод ← `electronic_assembler`

- **id:** `plastic_creator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine2/plastic_creator`

**Описание (RU):**

Создаёт пластик из подготовленных химических компонентов. Работает как химическая машина для полимерной цепочки.

**Description (EN):**

Creates plastic from prepared chemical components. It is the chemical machine for the polymer chain.

### Пластавтомат ← `plastic_creator`

- **id:** `plastic_plate_creator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine2/plastic_plate_creator`

**Описание (RU):**

Формует пластик в пластины. Делает ровные пластиковые заготовки для корпусов, изоляции и деталей.

**Description (EN):**

Forms plastic into plates. It makes flat plastic blanks for casings, insulation and parts.

### Сварочный аппарат ← `plastic_plate_creator`

- **id:** `welding`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/welding`

**Описание (RU):**

Сваривает металлические детали в прочные узлы. Используется там, где обычное соединение деталей недостаточно прочное.

**Description (EN):**

Welds metal parts into strong assemblies. It is used where ordinary joining is not strong enough.

### Рудопромывочный механизм ← `welding`

- **id:** `orewashing`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:moremachine3/orewashing`

**Описание (RU):**

Промывает измельчённую руду и отделяет полезную часть от пустой породы. Улучшает качество сырья перед дальнейшей переработкой.

**Description (EN):**

Washes crushed ore and separates useful material from waste rock. It improves ore quality before further processing.

### nitrate dust ← `orewashing`

- **id:** `nitrate_dust`
- **вкладка:** `baseElectric`

**Описание (RU):**

Нитратный порошок для химических реакций. Используется как твёрдый реагент в азотной химии и обработке материалов.

**Description (EN):**

A nitrate powder for chemical reactions. It acts as a solid reagent in nitrogen chemistry and material treatment.

### Разделитель предметов ← `nitrate_dust`

- **id:** `item_divider`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/item_divider`

**Описание (RU):**

Разделяет предметы на меньшие части по рецептам. Подходит для получения долей, пыли и промежуточных материалов.

**Description (EN):**

Splits items into smaller recipe-defined parts. It is useful for fractions, dusts and intermediate materials.

### nitrogen ← `item_divider`

- **id:** `nitrogen`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/nitrogen`

**Описание (RU):**

Азот — газ для химии и охлаждения. Он участвует в азотно-водородной цепочке, азотных оксидах и кислотах, принимается жидкостным холодильником как рабочая охлаждающая жидкость и используется в космических/материальных рецептах.

**Description (EN):**

Nitrogen is used for chemistry and cooling. It is part of nitrogen-hydrogen compounds, nitrogen oxides and acids, is accepted by the fluid cooling machine, and appears in space/material recipes.

### Улучшенная электросхема ← `nitrogen`

- **id:** `advanced_circuit`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_273_element`

**Описание (RU):**

Улучшенная электронная схема с более сложной логикой управления. Применяется в машинах, которым уже мало базовой платы.

**Description (EN):**

An advanced circuit with more complex control logic. It is used by machines that need more than a basic board.

### Газовый сенсор ← `advanced_circuit`

- **id:** `gas_sensor`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:tools/gas_sensor`

**Описание (RU):**

Портативный датчик газов и загрязнений. Показывает опасную атмосферу и помогает контролировать состояние зоны вокруг игрока.

**Description (EN):**

A portable gas and pollution sensor. It shows unsafe atmosphere and helps monitor the area around the player.

### gas ← `gas_sensor`

- **id:** `gas`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/gas`

**Описание (RU):**

Природный газ добывается из газовых залежей и используется как топливо и сырьё. Его находят датчиками/скважинами, подают в насосы и турбины, а затем перерабатывают в энергетических или химических машинах.

**Description (EN):**

Natural gas is extracted from gas deposits and used as both fuel and chemical feedstock. Sensors and wells locate it, pumps move it, and turbines or chemical machines consume it.

### Тройной твердотельный смеситель ← `gas`

- **id:** `triple_solid_mixer`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/triple_solid_mixer`

**Описание (RU):**

Смешивает три твёрдых компонента в один материал. Используется для порошков и составов, где важны точные пропорции.

**Description (EN):**

Mixes three solid components into one material. It is used for powders and blends with strict proportions.

### Газовый смеситель ← `triple_solid_mixer`

- **id:** `gas_combiner`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/gas_combiner`

**Описание (RU):**

Соединяет газы в новые соединения. Работает как газовый реактор для азотной, водородной и другой химии.

**Description (EN):**

Combines gases into new compounds. It works as a gas reactor for nitrogen, hydrogen and other chemistry.

### nitrogenhydride ← `gas_combiner`

- **id:** `nitrogenhydride`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/nitrogenhydride`

**Описание (RU):**

Азотно-водородное соединение получается из азота и водорода. Используется как промежуточный газ в дальнейших реакциях, включая цепочки азотных соединений и космическую переработку.

**Description (EN):**

Nitrogen hydride is produced from nitrogen and hydrogen. It is an intermediate gas for later nitrogen reactions and some space processing recipes.

### nitrogenoxy ← `nitrogenhydride`

- **id:** `nitrogenoxy`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/nitrogenoxy`

**Описание (RU):**

Оксид азота — промежуточный газ азотной химии. Через газовый комбайнер окисляется кислородом до диоксида азота, после чего линия выходит на азотную кислоту.

**Description (EN):**

Nitrogen oxide is an intermediate in nitrogen chemistry. The gas combiner oxidizes it into nitrogen dioxide, leading into nitric acid production.

### nitrogendioxide ← `nitrogenoxy`

- **id:** `nitrogendioxide`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/nitrogendioxide`

**Описание (RU):**

Диоксид азота — активный газ для получения азотной кислоты. В газовом комбайнере смешивается с водой и даёт кислоту для травления, пластика и химической переработки.

**Description (EN):**

Nitrogen dioxide is the active gas used to make nitric acid. In the gas combiner it mixes with water to produce acid for etching, plastics and chemical processing.

### nitricacid ← `nitrogendioxide`

- **id:** `nitricacid`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/nitricacid`

**Описание (RU):**

Азотная кислота — сильный жидкий реагент. Используется в газовом комбайнере, жидкостных интеграторах, пластиковой цепочке, огнеупорной обработке и переработке отдельных космических пород.

**Description (EN):**

Nitric acid is a strong liquid reagent. It is used by gas combiners, fluid integrators, plastic processing, refractory processing and some space-rock recipes.

### Жидкостный интегратор ← `nitricacid`

- **id:** `fluid_integrator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/fluid_integrator`

**Описание (RU):**

Вводит жидкость в предмет или материал. Используется для пропитки, растворения и химической обработки твёрдых заготовок.

**Description (EN):**

Injects a fluid into an item or material. It is used for soaking, dissolving and chemical treatment of solid blanks.

### silver nitrate dust ← `fluid_integrator`

- **id:** `silver_nitrate_dust`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:itemdust/silver_nitrate_dust`

**Описание (RU):**

Нитрат серебра в твёрдой форме. Служит химическим реагентом для специальных обработок и серебросодержащих цепочек.

**Description (EN):**

Solid silver nitrate. It is a reagent for special processing and silver-related chemistry.

### Урановая дроблёная руда ← `silver_nitrate_dust`

- **id:** `crushed_uranium_ore`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crushed/uranium`

**Описание (RU):**

Измельчённая урановая руда. Относится к радиоактивной переработке и требует отдельной обработки вместо обычной рудной линии.

**Description (EN):**

Crushed uranium ore. It belongs to radioactive processing and needs dedicated handling instead of the normal ore line.

### Обработчик радиоактивных руд ← `crushed_uranium_ore`

- **id:** `radioactive_handler_ore`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/radioactive_handler_ore`

**Описание (RU):**

Обрабатывает радиоактивные руды отдельно от обычных машин. Подготавливает урановое сырьё к очистке и дальнейшей ядерной переработке.

**Description (EN):**

Processes radioactive ores separately from ordinary machines. It prepares uranium material for purification and nuclear work.

### Промышленный очиститель радиоактивных элементов ← `radioactive_handler_ore`

- **id:** `industrial_ore_purifier`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/industrial_ore_purifier`

**Описание (RU):**

Глубоко очищает рудное сырьё после дробления и промывки. Убирает лишние примеси и даёт более чистый материал.

**Description (EN):**

Deeply purifies ore material after crushing and washing. It removes excess impurities and outputs cleaner material.

### Датчик: SE ← `industrial_ore_purifier`

- **id:** `se_sensor`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_79_element`

**Описание (RU):**

Датчик солнечной энергии. Используется солнечными установками для контроля света и состояния генерации.

**Description (EN):**

A solar energy sensor. It is used by solar equipment to read light and generation conditions.

### Генератор солнечной энергии ← `se_sensor`

- **id:** `se_gen`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:se_gen/se_gen`

**Описание (RU):**

Солнечный генератор, работающий от освещения. Производит энергию без топлива, пока условия позволяют получать солнечный поток.

**Description (EN):**

A solar generator that works from light. It produces energy without fuel while light conditions are valid.

### Производитель соларитовых пластин ← `se_gen`

- **id:** `gen_sunnarium_plate`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:gen_sunnarium_plate/gen_sunnarium_plate`

**Описание (RU):**

Создаёт суннариевые пластины для солнечных технологий. Обрабатывает материал в форму, удобную для панелей и солнечных элементов.

**Description (EN):**

Creates sunnarium plates for solar technology. It prepares the material in a form suitable for panels and solar parts.

### Преобразователь соларита ← `gen_sunnarium_plate`

- **id:** `gen_sunnarium`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:gen_sunnarium/gen_sunnarium`

**Описание (RU):**

Создаёт суннариевые элементы и панели. Работает с материалами солнечной энергетики и подготавливает их к сборке генераторов.

**Description (EN):**

Creates sunnarium elements and panels. It processes solar-energy materials for generator assembly.

### Углеволокно ← `gen_sunnarium`

- **id:** `calcium_carbide`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_280_element`

**Описание (RU):**

Карбид кальция — твёрдый реагент для получения ацетилена. Используется в химической цепочке полимеров.

**Description (EN):**

Calcium carbide, a solid reagent for acetylene production. It is used in the polymer chemistry chain.

### acetylene ← `calcium_carbide`

- **id:** `acetylene`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/acetylene`

**Описание (RU):**

Ацетилен — горючий углеводородный газ. В химических машинах соединяется с водородом в этилен, участвует в газовой турбине и нужен для органической/пластиковой цепочки.

**Description (EN):**

Acetylene is a flammable hydrocarbon gas. Chemical machines combine it with hydrogen into ethylene, and it is also used by gas turbines and organic/plastic chains.

### Полимеризатор ← `acetylene`

- **id:** `polymerizer`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/polymerizer`

**Описание (RU):**

Запускает полимеризацию жидкостей и газов. Превращает химическое сырьё в полимерные материалы.

**Description (EN):**

Runs polymerization of fluids and gases. It turns chemical feedstock into polymer materials.

### Твердотельно-жидкостный интегратор ← `polymerizer`

- **id:** `solid_fluid_integrator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/solid_fluid_integrator`

**Описание (RU):**

Соединяет твёрдый компонент с жидкостью в одном процессе. Нужен для материалов, которые должны связать или впитать реагент.

**Description (EN):**

Combines a solid component with a fluid in one process. It is used when an item must bind or absorb a reagent.

### polyeth ← `solid_fluid_integrator`

- **id:** `polyeth`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/polyeth`

**Описание (RU):**

Полиэтилен — жидкий полимерный материал. Используется в пластиковой цепочке и полимеризаторе как основа для получения пластиковых деталей и изоляционных материалов.

**Description (EN):**

Polyethylene is a liquid polymer material. It is used in plastic production and polymerizer chains as a base for plastic parts and insulation materials.

### Разделитель жидкостей ← `polyeth`

- **id:** `fluid_separator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/fluid_separator`

**Описание (RU):**

Разделяет жидкость на несколько компонентов. Полезен для смесей, топлива и нефтехимических промежуточных продуктов.

**Description (EN):**

Separates a fluid into multiple components. It is useful for mixtures, fuels and petrochemical intermediates.

### propane ← `fluid_separator`

- **id:** `propane`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/propane`

**Описание (RU):**

Пропан — горючий газ и нефтехимический реагент. Используется в газовой турбине, газовом комбайнере и переработке органических фракций, где нужен лёгкий углеводород.

**Description (EN):**

Propane is a flammable gas and petrochemical reagent. It is used by gas turbines, gas combiners and organic processing where a light hydrocarbon is needed.

### bromine ← `propane`

- **id:** `bromine`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/bromine`

**Описание (RU):**

Бром — химический реагент и газовый ресурс. Используется в газовых скважинах/датчиках, электролизёрах, газовом комбайнере и рецептах с космическими породами.

**Description (EN):**

Bromine is a chemical reagent and gas resource. It is connected to gas wells/sensors, electrolyzers, gas combiners and recipes involving space materials.

### propylene ← `bromine`

- **id:** `propylene`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:bucket/propylene`

**Описание (RU):**

Пропилен — органический газ для полимерной цепочки. Используется в полимеризаторе и жидкостных интеграторах, когда нужно получить пластики или органические промежуточные вещества.

**Description (EN):**

Propylene is an organic gas for polymer processing. It is used by polymerizers and fluid integrators to produce plastics or organic intermediates.

### plast ← `propylene`

- **id:** `plast`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:plast`

**Описание (RU):**

Готовый пластик для изоляции, корпусов и деталей машин. Это твёрдая форма полимерного материала.

**Description (EN):**

Finished plastic for insulation, casings and machine parts. It is the solid form of polymer material.

### plastic plate ← `plast`

- **id:** `plastic_plate`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:plastic_plate`

**Описание (RU):**

Пластиковая пластина для корпусов, изоляции и сборки механизмов. Удобная плоская форма готового пластика.

**Description (EN):**

A plastic plate for casings, insulation and machine assembly. It is the flat form of finished plastic.

### Минеральный сепаратор ← `plastic_plate`

- **id:** `handler_ho`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine1/handler_ho`

**Описание (RU):**

Обрабатывает тяжёлые руды и минералы, отделяя полезные части от пустой породы. Подходит для сырья, которое обычные машины перерабатывают хуже.

**Description (EN):**

Processes heavy ores and minerals, separating useful material from waste. It handles resources that normal machines process poorly.

### Нефтеперерабатывающий завод ← `handler_ho`

- **id:** `refiner`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:refiner/refiner`

**Описание (RU):**

Перерабатывает нефть и тяжёлые жидкости на полезные фракции. Работает как центральная машина топливной и нефтехимической линии.

**Description (EN):**

Refines oil and heavy fluids into useful fractions. It is the central machine of fuel and petrochemical processing.

### Дизельный генератор ← `refiner`

- **id:** `gen_disel`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine2/gen_disel`

**Описание (RU):**

Генератор на дизельном топливе. Производит EF из плотного жидкого топлива с нефтяной цепочки.

**Description (EN):**

A diesel generator. It produces EF from dense liquid fuel from the oil chain.

### Бензиновый генератор ← `refiner`

- **id:** `gen_pet`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine2/gen_pet`

**Описание (RU):**

Генератор на бензине или лёгком топливе. Использует жидкое топливо для стабильной выработки EF.

**Description (EN):**

A petrol or light-fuel generator. It uses liquid fuel for stable EF production.

### Жидкостный холодильник ← `refiner`

- **id:** `fluid_cooling`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/fluid_cooling`

**Описание (RU):**

Создаёт холод для системы охлаждения механизмов, расходуя жидкий азот, водород или гелий из бака. Водород даёт холод быстрее, азот работает медленнее, гелий расходуется как самый экономный вариант; накопленный холод отдаётся через CoolComponent соседним/подключённым машинам.

**Description (EN):**

Creates cooling power for machine cooling networks by consuming liquid nitrogen, hydrogen or helium from its tank. Hydrogen fills cooling faster, nitrogen works slower, and helium is the most economical option; the stored cooling is provided through CoolComponent to connected machines.

### Ветрогенератор ← `fluid_cooling`

- **id:** `simple_wind_generator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/simple_wind_generator`

**Описание (RU):**

Ветрогенератор. Производит EF от ветра, поэтому зависит от условий установки и не расходует топливо.

**Description (EN):**

A wind generator. It produces EF from wind, depends on placement conditions and consumes no fuel.

### Гидрогенератор ← `fluid_cooling`

- **id:** `simple_water_generator`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/simple_water_generator`

**Описание (RU):**

Водяной генератор. Получает энергию от воды и подходит для постоянной выработки при правильной установке.

**Description (EN):**

A water generator. It gets energy from water and suits constant generation when installed correctly.

### Улучшенный завод сплавов ← `fluid_cooling`

- **id:** `adv_alloy_smelter`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine1/adv_alloy_smelter`

**Описание (RU):**

Улучшенная плавильня сплавов. Быстрее и удобнее обрабатывает сложные металлические смеси.

**Description (EN):**

An improved alloy smelter. It processes complex metal mixtures faster and more conveniently.

### Центрифуга пчелиных продуктов ← `adv_alloy_smelter`

- **id:** `centrifuge`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/centrifuge`

**Описание (RU):**

Разделяет смеси вращением. Работает с пылями, жидкостями и материалами, которые нужно разложить на фракции.

**Description (EN):**

Separates mixtures by spinning. It works with dusts, fluids and materials that must be split into fractions.

### Обогатитель ← `centrifuge`

- **id:** `enrichment`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine1/enrichment`

**Описание (RU):**

Обогащает сырьё, увеличивая полезную долю материала. Используется перед сложной рудной и радиоактивной переработкой.

**Description (EN):**

Enriches raw material by increasing the useful fraction. It is used before advanced ore and radioactive processing.

### Переработчик радиоактивных отходов ← `enrichment`

- **id:** `nuclear_waste_recycler`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:basemachine3/nuclear_waste_recycler`

**Описание (RU):**

Перерабатывает ядерные отходы и возвращает часть полезных материалов. Помогает уменьшить накопление опасного остатка.

**Description (EN):**

Recycles nuclear waste and recovers useful components. It helps reduce dangerous leftover buildup.

### Радиоактивные отходы ← `nuclear_waste_recycler`

- **id:** `radioactive_waste`
- **вкладка:** `baseElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_443_element`

**Описание (RU):**

Опасный остаток ядерной переработки. Требует отдельного хранения и специальных машин, а не обычной обработки.

**Description (EN):**

A dangerous residue from nuclear processing. It needs dedicated storage and special machines, not normal processing.

---

## Продвинутая — Продвинутая электрическая эра

### Продвинутый завод сплавов

- **id:** `imp_alloy_smelter`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/imp_alloy_smelter`

**Описание (RU):**

Усиленная плавильня сплавов. Работает с более сложными смесями и быстрее обрабатывает материалы.

**Description (EN):**

Heats materials and turns them into ingots, alloys or molten fluids for metallurgy.

### Предметные и жидкостные трубы

- **id:** `fluid_item_pipe`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:wiring/pipes`
- **icon field:** `item_pipes`

**Описание (RU):**

В моде есть собственные трубы, механика которых отличается от других модов. Для работы требуется две трубы: выходная и входная. Минимальная дистанция работы — 2 блока, то есть выходная труба должна быть установлена рядом с блоком, из которого будут поступать ресурсы или жидкости, а входная — рядом с блоком, в который они будут поступать. Для каждой стороны предусмотрены свои фильтры — белый и чёрный список. Поднесите предмет или предмет с жидкостью, чтобы настроить фильтр. Чтобы проверить, включён ли фильтр, настройте его со стороны блока, который будет подключён к трубе. Также можно настроить реакцию на подачу красного сигнала от редстоуна. Передача в трубах происходит каждые 2 тика, с максимальным возможным количеством предметов или жидкости.

**Description (EN):**

The mod features its own pipes, whose mechanics differ from those in other mods. To operate, you need two pipes: an output pipe and an input pipe. The minimum working distance is 2 blocks, meaning the output pipe should be placed next to the block from which resources or fluids will be extracted, and the input pipe should be placed next to the block where they will be delivered. Each side has its own filters — whitelist and blacklist. Hold an item or an item containing a fluid to configure the filter. To check whether the filter is active, configure it from the side of the block that will be connected to the pipe. You can also set it to respond to a redstone signal. Pipes transfer every 2 ticks, moving the maximum possible amount of items or fluid.

### graviTool ← `imp_alloy_smelter`

- **id:** `graviTool`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:gravitool/gravitool`

**Описание (RU):**

Гравитационный инструмент для перемещения и технической работы. Совмещает функции, связанные с управлением блоками и тяжёлыми объектами.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### relocator ← `graviTool`

- **id:** `relocator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:energy/relocator`

**Описание (RU):**

Перемещатель для блоков или механизмов. Помогает переносить выбранные объекты без полной разборки линии.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Холодильник ← `imp_alloy_smelter`

- **id:** `cooling`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/cooling`

**Описание (RU):**

Электрический холодильник создаёт холод из EF и отдаёт его в систему охлаждения механизмов через CoolComponent. В интерфейсе регулируется ёмкость холода, а машина сама поддерживает буфер, пока хватает энергии.

**Description (EN):**

An electric refrigerator that converts EF into cooling power and sends it into the machine cooling system through CoolComponent. Its cooling capacity can be adjusted in the GUI and it keeps the buffer filled while it has energy.

### antiairpollution1 ← `cooling`

- **id:** `antiairpollution1`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Улучшенное средство очистки воздуха. Сильнее снижает загрязнение и подходит для более грязных производств.

**Description (EN):**

Cleans pollution or shows environmental state for air, soil and dangerous areas.

### antisoilpollution1 ← `cooling`

- **id:** `antisoilpollution1`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Улучшенное средство очистки почвы. Быстрее уменьшает загрязнение земли вокруг промышленных объектов.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Устройство управления проводами ← `cooling`

- **id:** `substitute`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/substitute`

**Описание (RU):**

Энергетический заменитель. Работает с сетью так, чтобы подменять или стабилизировать источник энергии в поддерживаемых схемах.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Устройство демонтажа проводов ← `substitute`

- **id:** `energy_remover`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/energy_remover`

**Описание (RU):**

Удаляет энергию из сети или машины. Используется как технический слив для разгрузки энергетической линии.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### module quickly ← `substitute`

- **id:** `module_quickly`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Набор модулей для автоматических механизмов. Меняет скорость, раздельную работу, хранение, стаки или подачу воды в поддерживаемых машинах.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Азотный модуль охлаждения ← `module_quickly`

- **id:** `coolupgrade`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:itemcoolupgrade/azote`

**Описание (RU):**

Охлаждающее улучшение. Снижает тепловую нагрузку или повышает эффективность охлаждения в совместимых механизмах.

**Description (EN):**

Lowers the temperature of a fluid or item for processes that require active cooling.

### autoheater ← `coolupgrade`

- **id:** `autoheater`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Автонагріватель для поддерживаемых механизмов. Добавляет или поддерживает тепло без ручного обслуживания.

**Description (EN):**

Transfers heat to a fluid, item or system for recipes that depend on temperature.

### Продвинутый нефтеперерабатывающий завод ← `autoheater`

- **id:** `imp_refiner`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/imp_refiner`

**Описание (RU):**

Улучшенный нефтепереработчик. Быстрее и глубже разделяет нефтяные жидкости на топливные и химические фракции.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Контроллер улучшенной коксовой печи ← `imp_refiner`

- **id:** `adv_coke_oven_main`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:adv_cokeoven/adv_coke_oven_main`

**Описание (RU):**

Главный блок продвинутой коксовой печи. Управляет многоблочной переработкой топлива, жидкостей и побочных продуктов.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Беспроводная нефтянная помпа ← `adv_coke_oven_main`

- **id:** `wireless_oil_pump`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/wireless_oil_pump`

**Описание (RU):**

Беспроводной насос нефти. Добывает нефть из найденной залежи без длинной линии труб до самой жилы.

**Description (EN):**

Moves fluid or gas from a source into a processing line for extraction and machine supply.

### Беспроводной минеральный карьер ← `wireless_oil_pump`

- **id:** `wireless_mineral_quarry`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/wireless_mineral_quarry`

**Описание (RU):**

Беспроводной минеральный карьер. Добывает минералы из найденной залежи без обычной шахтной установки.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### Беспроводная газовая установка ← `wireless_mineral_quarry`

- **id:** `wireless_gas_pump`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/wireless_gas_pump`

**Описание (RU):**

Беспроводной газовый насос. Получает газ из залежи и передаёт его в систему переработки.

**Description (EN):**

Moves fluid or gas from a source into a processing line for extraction and machine supply.

### Хранилище радиации ← `adv_coke_oven_main`

- **id:** `radiation_storage`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/radiation_storage`

**Описание (RU):**

Хранилище радиационного ресурса. Принимает и удерживает радиационную энергию или связанный с ней ресурс для ядерных систем.

**Description (EN):**

Works with radioactive materials, fuel or waste and requires separate handling and danger control.

### Автоматический механизм ← `radiation_storage`

- **id:** `automatic_mechanism`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/automatic_mechanism`

**Описание (RU):**

Автоматический механизм для повторяемых действий с предметами. Выполняет рабочие операции без постоянного участия игрока.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Беспроводной контроллер реакторов ← `automatic_mechanism`

- **id:** `wireless_controller_reactors`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/wireless_controller_reactors`

**Описание (RU):**

Беспроводной контроллер реакторов. Позволяет следить за реакторной системой и управлять ею на расстоянии.

**Description (EN):**

Controls a multiblock reactor: checks the structure, monitors working parts and links the system into one machine.

### Охотничий модуль: Моб ← `wireless_controller_reactors`

- **id:** `entitymodules`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:entitymodules/module_mob`

**Описание (RU):**

Модуль сущностей для автоматических машин. Используется там, где механизм должен работать с мобами или сущностями.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Автоматический охотник ← `entitymodules`

- **id:** `spawner`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/spawner`

**Описание (RU):**

Автоматический спавнер. Использует внутреннюю логику сущностей и ресурсы машины для появления мобов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Распаковщик коробок ← `entitymodules`

- **id:** `auto_open_box`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/auto_open_box`

**Описание (RU):**

Автоматически открывает коробки и контейнерные предметы. Выгружает содержимое в выходные слоты.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Холодильник охлаждающих стержней ← `auto_open_box`

- **id:** `refrigerator_coolant`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/refrigerator_coolant`

**Описание (RU):**

Заправляет реакторные охлаждающие элементы специальными жидкостями. Обычный элемент наполняется водородом, улучшенный — азотом, продвинутый — гелием; машина тратит EF и жидкость из внутреннего бака.

**Description (EN):**

Fills reactor coolant items with special cooling fluids. The basic coolant uses hydrogen, the advanced one uses nitrogen, and the improved one uses helium; the machine consumes EF and fluid from its tank.

### Автоматический верстак ← `refrigerator_coolant`

- **id:** `autocrafter`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/autocrafter`

**Описание (RU):**

Автоматический крафтер. Собирает предметы по заданному рецепту из предметов во входных слотах.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Камера ядерного синтеза ← `refrigerator_coolant`

- **id:** `autofuse`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/autofuse`

**Описание (RU):**

Автоматически объединяет предметы или заряды в поддерживаемых рецептах. Используется для операций слияния без ручной сборки.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Обработчик графита ← `autofuse`

- **id:** `graphite_handler`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/graphite_handler`

**Описание (RU):**

Обрабатывает графитовые материалы для реакторов. Готовит элементы, связанные с графитовыми корпусами и стержнями.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Тессеракт ← `graphite_handler`

- **id:** `tesseract`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/tesseract`

**Описание (RU):**

Тессеракт для удалённой передачи ресурсов. Используется как высокоуровневый узел связи между точками сети.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Продвинутый генератор солнечной энергии ← `tesseract`

- **id:** `imp_se_gen`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:imp_se_gen/imp_se_gen`

**Описание (RU):**

Улучшенный генератор солнечной энергии. Производит больше энергии от света и работает с продвинутыми солнечными компонентами.

**Description (EN):**

Produces energy from light. Output depends on panel tier and lighting conditions.

### Объединитель генераторов солнечной энергии ← `imp_se_gen`

- **id:** `combiner_se_generators`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/combiner_se_generators`

**Описание (RU):**

Объединяет солнечные генераторы в более мощный блок. Упрощает крупные солнечные поля и уменьшает количество отдельных панелей.

**Description (EN):**

Produces energy from light. Output depends on panel tier and lighting conditions.

### Комбинированный генератор жидкой материи ← `combiner_se_generators`

- **id:** `combiner_matter`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/combiner_matter`

**Описание (RU):**

Объединяет разные виды твёрдой материи в более сложные формы. Используется в цепочках материального синтеза.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Квантовый карьер ← `combiner_matter`

- **id:** `quantum_quarry`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine/quantum_quarry`

**Описание (RU):**

Квантовый карьер добывает ресурсы через энергетический процесс без обычной шахты. Требует значительной энергии и работает с внутренней логикой добычи.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### Контроллер графито-водного реактора ← `quantum_quarry`

- **id:** `graphite_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:graphite_reactor/graphite_controller`

**Описание (RU):**

Контроллер графитового реактора. Управляет многоблочной реакторной схемой с графитовыми элементами.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Контроллер высокотемпературного реактора ← `quantum_quarry`

- **id:** `heat_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:heat_reactors/heat_controller`

**Описание (RU):**

Контроллер теплового реактора. Следит за тепловыми блоками и управляет передачей тепла внутри многоблочной установки.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Генератор нейтронных частиц ← `quantum_quarry`

- **id:** `neutron_generator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine/neutron_generator`

**Описание (RU):**

Генератор нейтронного ресурса. Используется в высокоуровневой ядерной и нейтронной технологической цепочке.

**Description (EN):**

Produces energy or a technical resource from the appropriate fuel, fluid or input material.

### neutroniumingot ← `neutron_generator`

- **id:** `neutroniumingot`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Нейтрониевый слиток для самых прочных и энергоёмких деталей. Применяется в поздних машинах и корпусах.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Модификационная станция ← `neutroniumingot`

- **id:** `upgrade_block`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:upgrade_block/upgrade_block`

**Описание (RU):**

Блок улучшений для машин. Хранит и применяет набор апгрейдов к поддерживаемым механизмам.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Станция снятия модулей ← `upgrade_block`

- **id:** `antiupgradeblock`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/antiupgradeblock`

**Описание (RU):**

Блок, снимающий или подавляющий улучшения. Используется для обслуживания машин и возврата настроек.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Станция модификации роторов ← `antiupgradeblock`

- **id:** `rotor_modifier`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/rotor_modifier`

**Описание (RU):**

Изменяет параметры роторов. Настраивает характеристики ротора перед работой в турбинной установке.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Станция модификации водяных роторов ← `rotor_modifier`

- **id:** `water_modifier`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/water_modifier`

**Описание (RU):**

Изменяет водяные роторы или водяные рабочие части. Настраивает их под нужную выработку и условия.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Контроллер земляного карьера ← `upgrade_block`

- **id:** `earth_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:earth_quarry/earth_controller`

**Описание (RU):**

Контроллер земного карьера. Управляет многоблочной установкой добычи ресурсов из грунта и пород.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Контроллер газовой турбины ← `earth_controller`

- **id:** `gas_turbine_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:gas_turbine/gas_turbine_controller`

**Описание (RU):**

Контроллер газовой турбины управляет многоблоком, который сжигает газовые топлива и превращает их в энергию. Он связывает топливные, рабочие и энергетические части турбины.

**Description (EN):**

Controls a multiblock gas turbine that burns gas fuels and turns them into energy. It links the fuel, working and energy parts of the turbine.

### Контроллер газовой скважины ← `gas_turbine_controller`

- **id:** `gas_well_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:gas_well/gas_well_controller`

**Описание (RU):**

Контроллер газовой скважины. Управляет добычей газа из залежи и передачей его в жидкостно-газовую сеть.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Ночной конвертер ← `gas_well_controller`

- **id:** `night_transformer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/night_transformer`

**Описание (RU):**

Преобразователь ночной энергии. Работает с ресурсами ночной энергетической цепочки и переводит их в нужную форму.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Ночной преобразователь ← `night_transformer`

- **id:** `night_converter`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/night_converter`

**Описание (RU):**

Конвертер ночного ресурса. Превращает материалы или энергию ночной линии в совместимые компоненты.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Инкубатор ← `night_transformer`

- **id:** `incubator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/incubator`

**Описание (RU):**

Инкубатор для пчелиных и генетических процессов. Поддерживает развитие образцов в контролируемых условиях.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Изолятор ← `incubator`

- **id:** `insulator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/insulator`

**Описание (RU):**

Изолятор для пчелиных или генетических образцов. Обрабатывает материал так, чтобы защитить или выделить нужные свойства.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Сборщик РНК ← `insulator`

- **id:** `rna_collector`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/rna_collector`

**Описание (RU):**

Собирает РНК-материал из биологических образцов. Используется в генетической цепочке перед изменением генома.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Мутатрон ← `rna_collector`

- **id:** `mutatron`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/mutatron`

**Описание (RU):**

Мутатрон изменяет генетические свойства образца. Используется для направленных мутаций и вывода новых вариантов.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Генетический стабилизатор ← `mutatron`

- **id:** `genetic_stabilizer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/genetic_stabilizer`

**Описание (RU):**

Стабилизирует генетический образец после изменений. Снижает риск нестабильных или нежелательных свойств.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Генетический реверсер ← `genetic_stabilizer`

- **id:** `reverse_transcriptor`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/reverse_transcriptor`

**Описание (RU):**

Преобразует генетическую информацию в обратном направлении. Работает с РНК/ДНК-переходами в биологической цепочке.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Генетический репликатор ← `reverse_transcriptor`

- **id:** `genetic_replicator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/genetic_replicator`

**Описание (RU):**

Копирует генетический образец. Позволяет размножать нужные свойства без повторного поиска исходного материала.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Генетический транспозёр ← `genetic_replicator`

- **id:** `genetic_transposer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/genetic_transposer`

**Описание (RU):**

Переносит генетические свойства между образцами. Используется для сборки нужного набора признаков.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Генетический полимеризатор ← `genetic_transposer`

- **id:** `genetic_polymerizer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/genetic_polymerizer`

**Описание (RU):**

Полимеризует генетический материал. Готовит стабильные биологические компоненты для дальнейшей работы.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Инокулятор ← `genetic_polymerizer`

- **id:** `inoculator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/inoculator`

**Описание (RU):**

Вводит выбранный генетический материал в образец. Используется для применения подготовленных признаков.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Экстрактор генома ← `inoculator`

- **id:** `genome_extractor`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/genome_extractor`

**Описание (RU):**

Извлекает геном из биологического образца. Даёт материал для анализа, копирования и изменения признаков.

**Description (EN):**

Works with genes, samples and traits: extracting, changing, copying or stabilizing genetic material.

### Контроллер геотермального насоса ← `genome_extractor`

- **id:** `geothermal_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:geothermalpump/geothermal_controller`

**Описание (RU):**

Контроллер геотермальной помпы. Управляет многоблочной установкой, которая работает с горячими подземными жидкостями.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Иод ← `geothermal_controller`

- **id:** `iodine`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:miscresource/iodine`

**Описание (RU):**

Йод — газовый/жидкостный химический ресурс. Связан с газовыми залежами, химическим заводом, электролизёрами и космическими рецептами, где нужны галогены.

**Description (EN):**

Iodine is a chemical gas/fluid resource. It is linked to gas deposits, the chemical plant, electrolyzers and space recipes that require halogens.

### Контроллер химической установки ← `iodine`

- **id:** `chemical_plant_controller`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:chemical_plant/chemical_plant_controller`

**Описание (RU):**

Контроллер химического завода. Управляет многоблочной установкой для крупных химических процессов с жидкостями и газами.

**Description (EN):**

Controls the multiblock chemical plant, linking inputs, outputs and working blocks for large fluid and gas processes.

### Камешек с Ариэль ← `chemical_plant_controller`

- **id:** `ariel_pebble`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:itemspace/ariel_pebble`

**Описание (RU):**

Камень с Ариэля. Используется как образец спутниковой породы и источник редких космических материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### draconid ← `ariel_pebble`

- **id:** `draconid`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:itemingots/draconid`

**Описание (RU):**

Драконидовый металл для космических и высокоуровневых деталей. Подходит для прочных и редких компонентов.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### quad molecular ← `draconid`

- **id:** `quad_molecular`
- **вкладка:** `advancedElectricTab`

**Описание (RU):**

Компонент четверного молекулярного преобразования. Используется в самых сложных процессах трансформации материи.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Улучшенная обшивка корпуса ← `draconid`

- **id:** `perfect_hull_plating`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:crafting_elements/crafting_138_element`

**Описание (RU):**

Совершенная корпусная пластина. Даёт максимальную прочность корпусам высокоуровневых машин.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Исследовательская линза V ← `perfect_hull_plating`

- **id:** `research_lens_5`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:research_lens/lens_5`

**Описание (RU):**

Исследовательская линза высокого уровня. Нужна для анализа редких космических образцов и поздних технологий.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Настройщик механических рецепторов ← `adv_alloy_smelter`

- **id:** `recipe_tuner`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/recipe_tuner`

**Описание (RU):**

Настраивает рецепт или режим поддерживаемой машины. Позволяет выбрать нужный вариант обработки без смены самой машины.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Настройщик беспроводной связи ← `purifier_soil`

- **id:** `tuner`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/tuner`

**Описание (RU):**

Беспроводной настройщик. Используется для привязки и настройки удалённых машин или контроллеров.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Приватизатор ← `tuner`

- **id:** `privatizer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/privatizer`

**Описание (RU):**

Ограничивает доступ к блоку или механизму. Защищает машину от чужого взаимодействия в мире.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Голографический проектор ← `rocket_launch_pad`

- **id:** `hologram_space`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/hologram_space`

**Описание (RU):**

Голограмма космической структуры. Показывает схему или визуальную подсказку для сборки сложной конструкции.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Прополщик ← `single_multi_crop`

- **id:** `weeder`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/weeder`

**Описание (RU):**

Удаляет сорняки с аграрных участков. Поддерживает поля в рабочем состоянии без ручной очистки.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### Удобритель растений ← `weeder`

- **id:** `plant_fertilizer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/plant_fertilizer`

**Описание (RU):**

Автоматически вносит удобрение в растения. Ускоряет рост культур в зоне действия.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### Очиститель полей ← `plant_fertilizer`

- **id:** `field_cleaner`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/field_cleaner`

**Описание (RU):**

Очищает поле от лишних растений и мусора. Подготавливает участок для дальнейшего выращивания.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### weed ex ← `field_cleaner`

- **id:** `weed_ex`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:bucket/weed_ex`

**Описание (RU):**

Жидкость против сорняков применяется в аграрной системе. Её используют очистители поля и мультифермы, чтобы удалять сорняки и защищать посадки.

**Description (EN):**

Weed-ex fluid is used by the farming system. Field cleaners and multicrop machines consume it to remove weeds and protect crops.

### Сборщик пчелиных продуктов ← `steelMesh`

- **id:** `collector_product_bee`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/collector_product_bee`

**Описание (RU):**

Собирает продукцию пчёл из пасек или ульев. Автоматизирует получение сот и других пчелиных ресурсов.

**Description (EN):**

Works with hives, apiaries and bee products for breeding, care and automatic resource collection.

### Дефлекторный щит ← `gen_wither`

- **id:** `shield`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/shield`

**Описание (RU):**

Защитный блок, создающий силовое поле или барьер. Используется для защиты зоны и важных механизмов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Электрический паровой генератор ← `redstone_generator`

- **id:** `steam_generator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/steam_generator`

**Описание (RU):**

Паровой генератор работает с паровой цепочкой и выдаёт энергию/рабочий ресурс из пара. Используется как энергетический узел там, где линия построена вокруг пара.

**Description (EN):**

The steam generator works with the steam chain and outputs energy or working resource from steam. It is an energy node for lines built around steam.

### Электрический био-генератор ← `steam_generator`

- **id:** `bio_generator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/bio_generator`

**Описание (RU):**

Генератор на биотопливе. Перерабатывает органическое топливо в энергию.

**Description (EN):**

Turns organic fuel into energy. Useful for lines based on plant and biological waste.

### Торфяной генератор ← `bio_generator`

- **id:** `peat_generator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/peat_generator`

**Описание (RU):**

Генератор на торфе. Использует торфяное топливо для стабильной небольшой выработки энергии.

**Description (EN):**

Produces energy from peat fuel and provides steady low-scale output.

### Водородный генератор ← `peat_generator`

- **id:** `gen_hyd`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/gen_hyd`

**Описание (RU):**

Генератор водородной линии производит энергию из подходящего водородного топлива или ресурса. Он относится к топливным генераторам, а не к обычной водяной турбине.

**Description (EN):**

The hydrogen-line generator produces energy from the matching hydrogen fuel or resource. It is a fuel generator, not a normal water turbine.

### Электрическая тугоплавкая печь ← `gen_sunnarium`

- **id:** `electric_refractory_furnace`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/electric_refractory_furnace`

**Описание (RU):**

Электрическая огнеупорная печь. Плавит материалы в расплавы без паровой стадии и работает от EF.

**Description (EN):**

Heats materials and turns them into ingots, alloys or molten fluids for metallurgy.

### Электрическая зельеварка ← `generator`

- **id:** `electric_brewing`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/electric_brewing`

**Описание (RU):**

Электрическая варочная установка. Обрабатывает зелья и жидкости машинным способом.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Генератор воды ← `adv_alloy_smelter`

- **id:** `watergenerator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/watergenerator`

**Описание (RU):**

Создаёт воду во внутреннем баке машинным способом. Полезен как постоянный источник воды для линий, где нельзя или неудобно тянуть обычный водозабор.

**Description (EN):**

Creates water in an internal tank by machine logic. It is useful as a permanent water source for lines where a normal water intake is inconvenient.

### Аптекарь пчёл ← `watergenerator`

- **id:** `apothecary_bee`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/apothecary_bee`

**Описание (RU):**

Пчелиная аптекарская машина. Работает с продуктами пчёл и биологическими реагентами.

**Description (EN):**

Works with hives, apiaries and bee products for breeding, care and automatic resource collection.

### Генератор лавы ← `electronic_assembler`

- **id:** `lava_gen`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/lava_gen`

**Описание (RU):**

Создаёт лаву во внутреннем баке. Лава дальше используется для геотермальной энергии, обсидиана, нагрева и горячих жидкостных процессов.

**Description (EN):**

Creates lava in an internal tank. Lava is then used for geothermal power, obsidian, heating and hot-fluid processes.

### Генератор гелия ← `lava_gen`

- **id:** `helium_generator`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/helium_generator`

**Описание (RU):**

Производит гелий как рабочий газ. Гелий используется в жидкостном охлаждении, холодильнике охлаждающих элементов и космических/газовых рецептах.

**Description (EN):**

Produces helium as a working gas. Helium is used by fluid cooling, the coolant refrigerator and space/gas recipes.

### Генератор камня ← `gearing`

- **id:** `gen_stone`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine/gen_stone`

**Описание (RU):**

Генератор камня. Создаёт каменный материал машинным способом для переработки и строительства.

**Description (EN):**

Creates stone materials by machine processing for constant supply to processing lines or construction.

### Расширенный генератор камня ← `gen_stone`

- **id:** `gen_addition_stone`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/gen_addition_stone`

**Описание (RU):**

Генератор дополнительных пород. Создаёт специальные каменные варианты для переработки или строительства.

**Description (EN):**

Creates stone materials by machine processing for constant supply to processing lines or construction.

### Нагреватель жидкости ← `liqued_heater`

- **id:** `fluid_heater`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/fluid_heater`

**Описание (RU):**

Нагревает жидкость в баке до нужного состояния. Используется в процессах, где температура жидкости влияет на рецепт.

**Description (EN):**

Transfers heat to a fluid, item or system for recipes that depend on temperature.

### hive ← `squeezer`

- **id:** `hive`
- **вкладка:** `advancedElectricTab`
- **icon field:** `hive`

**Описание (RU):**

Улей для содержания пчёл. Хранит пчелиную семью и даёт основу для пасечных механик.

**Description (EN):**

Works with hives, apiaries and bee products for breeding, care and automatic resource collection.

### Жёрдочка ← `squeezer`

- **id:** `crop_stake`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:crop/crop`

**Описание (RU):**

Опора для агрокультур. На ней выращиваются растения из системы культур мода.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### net ← `hive`

- **id:** `net`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:energy/net`

**Описание (RU):**

Сачок для работы с пчёлами и насекомыми. Используется для ловли или переноса живых объектов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Пасека ← `net`

- **id:** `apiary`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:apiary/apiary`

**Описание (RU):**

Пасека для разведения пчёл. Поддерживает работу пчелиной семьи и производство пчелиных ресурсов.

**Description (EN):**

Works with hives, apiaries and bee products for breeding, care and automatic resource collection.

### iron hammer ← `primal_wire_insulator`

- **id:** `iron_hammer`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:energy/iron_hammer`

**Описание (RU):**

Железный молот для ранней ручной обработки. Прочнее простых инструментов и подходит для базовых металлических операций.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Бродильная бочка ← `latex`

- **id:** `barrel`
- **вкладка:** `advancedElectricTab`
- **предмет:** `industrialupgrade:barrel/barrel`

**Описание (RU):**

Бочка для хранения и переноса жидкостей. Работает как простой сосуд между баками, машинами и ручными операциями.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

---

## Улучшенная — Улучшенная электрическая эра

### Лазерный полировщик

- **id:** `laser_polisher`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/laser_polisher`

**Описание (RU):**

Электрический лазерный полировщик. Точно обрабатывает кристаллы и детали, которым нужна чистая поверхность.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Автономная ферма ← `laser_polisher`

- **id:** `farmer`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:moremachine3/farmer`

**Описание (RU):**

Автоматически работает с грядками и растениями в зоне действия. Берёт на себя рутинный уход за посевами.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### fertilizer ← `farmer`

- **id:** `fertilizer`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Удобрение для ускорения роста растений. Используется в аграрных механиках и автоматических фермах.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### Одиночная агроферма ← `fertilizer`

- **id:** `single_multi_crop`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/single_multi_crop`

**Описание (RU):**

Одиночная агроферма для одного участка культуры. Следит за растением и выполняет операции ухода в своей зоне.

**Description (EN):**

Maintains crops in its working area by tending, fertilizing, cleaning or collecting plants.

### Щёлочноземельный рудник ← `laser_polisher`

- **id:** `alkalineearthquarry`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/alkalineearthquarry`

**Описание (RU):**

Карьер для щёлочноземельных ресурсов. Добывает специальные минеральные материалы из подходящих залежей.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### Стальная сетка ← `alkalineearthquarry`

- **id:** `steelMesh`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:mesh/steelmesh`

**Описание (RU):**

Стальная сетка для фильтрации и армирования. Используется в машинах, где материал нужно просеять или усилить.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### lithium ← `steelMesh`

- **id:** `lithium`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:baseore1/lithium`

**Описание (RU):**

Лёгкий металл для аккумуляторов и специальных сплавов. Особенно полезен в энергетических компонентах.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### reBattery ← `lithium`

- **id:** `reBattery`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:battery/re_battery`

**Описание (RU):**

Перезаряжаемый аккумулятор. Хранит EF в предмете и отдаёт её машинам или инструментам, которые поддерживают зарядку.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### planner ← `reBattery`

- **id:** `planner`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Планировщик многоблоков. Показывает схему конструкции и помогает проверять правильность сборки перед запуском.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Нефтеперерабатывающий завод ← `reBattery`

- **id:** `oilgetter`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:refiner/refiner`

**Описание (RU):**

Инструмент разведки нефтяных залежей. Помогает найти и проверить месторождения нефти.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Станок-качалка ← `oilgetter`

- **id:** `oilquarry`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:petrol_quarry/petrol_quarry`

**Описание (RU):**

Нефтяная буровая установка. Добывает нефть из найденной залежи и передаёт её в жидкостную переработку.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### Улучшенный нефтеперерабатывающий завод ← `oilgetter`

- **id:** `oiladvrefiner`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:adv_refiner/adv_refiner`

**Описание (RU):**

Продвинутый нефтеперерабатывающий завод. Разделяет нефть на более полезные топливные и химические фракции.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Контроллер коксовой печи ← `oiladvrefiner`

- **id:** `cokeoven`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:cokeoven/coke_oven_main`

**Описание (RU):**

Коксовая печь для медленной термической переработки топлива и органики. Даёт продукты, нужные металлургии и химии.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Дозиметр ← `cokeoven`

- **id:** `dosimeter`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:crafting_elements/crafting_40_element`

**Описание (RU):**

Дозиметр показывает уровень радиации. Нужен для проверки опасных зон, радиоактивных предметов и ядерных установок.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### radioprotector ← `dosimeter`

- **id:** `radioprotector`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:tools/radioprotector`

**Описание (RU):**

Средство защиты от радиационного воздействия. Снижает опасность при работе с радиоактивными материалами.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### hazmat ← `dosimeter`

- **id:** `hazmat`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Защитный костюм для опасной среды. Помогает пережить радиацию, загрязнение и химические риски при работе с опасными механизмами.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Производитель реакторных стержней ← `hazmat`

- **id:** `reactor_rod_factory`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/reactor_rod_factory`

**Описание (RU):**

Собирает детали топливных стержней для реакторов. Работает с оболочками, топливом и компонентами ядерной цепочки.

**Description (EN):**

Works with radioactive materials, fuel or waste and requires separate handling and danger control.

### uranium fuel rod ← `reactor_rod_factory`

- **id:** `uranium_fuel_rod`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:reactors/uranium_fuel_rod`

**Описание (RU):**

Урановый топливный стержень для реактора. Выделяет тепло и энергию в ядерной установке, постепенно вырабатывая ресурс.

**Description (EN):**

Works with radioactive materials, fuel or waste and requires separate handling and danger control.

### leadbox ← `uranium_fuel_rod`

- **id:** `leadbox`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Свинцовый контейнер для радиоактивных предметов. Изолирует опасный материал и уменьшает риск при хранении или переноске.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Пеллета обогащённого уранового ядерного топлива ← `uranium_fuel_rod`

- **id:** `pellets`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:nuclearresource/uranium_pellet`

**Описание (RU):**

Топливные гранулы для ядерной цепочки. Используются как подготовленная форма радиоактивного материала.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Радиоизотопный термоэлектрический генератор ← `pellets`

- **id:** `pallet_generator`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/pallet_generator`

**Описание (RU):**

Формирует топливные гранулы из подготовленного материала. Используется перед сборкой ядерного топлива.

**Description (EN):**

Produces energy or a technical resource from the appropriate fuel, fluid or input material.

### radcable ← `hazmat`

- **id:** `radcable`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Кабель или труба для радиационной системы. Передаёт соответствующий ресурс между ядерными блоками.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Защитный купол реактора ← `radcable`

- **id:** `reactor_safety_doom`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/reactor_safety_doom`

**Описание (RU):**

Блок аварийной защиты реактора. Следит за опасными состояниями и помогает снизить риск неконтролируемой работы.

**Description (EN):**

Works with radioactive materials, fuel or waste and requires separate handling and danger control.

### Контроллер жидкостного реактора ← `reactor_safety_doom`

- **id:** `water_controller`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:water_reactors/water_controller`

**Описание (RU):**

Контроллер реакторной установки. Управляет многоблочной схемой охлаждения и рабочими частями реактора.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### azurebrilliant ← `radioprotector`

- **id:** `azurebrilliant`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/azurebrilliant`

**Описание (RU):**

Лазурный бриллиантовый раствор используется как специальная жидкость для защиты и высокоуровневой обработки. В коде он связан с радиозащитными предметами и жидкостным адаптером.

**Description (EN):**

Azure brilliant fluid is a special liquid for protection and high-tier processing. In code it is connected to radiation protection items and the fluid adapter.

### industrialoil ← `dosimeter`

- **id:** `industrialoil`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/industrialoil`

**Описание (RU):**

Индустриальное масло — тяжёлая нефтяная фракция. Получается в нефтепереработке и используется как база для моторного масла, топлива и химических смесей.

**Description (EN):**

Industrial oil is a heavy oil fraction. It comes from oil refining and is used as a base for motor oil, fuels and chemical mixtures.

### motoroil ← `industrialoil`

- **id:** `motoroil`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/motoroil`

**Описание (RU):**

Моторное масло — смазочная жидкость для машинных процессов. Оно участвует в механизмах обработки, снижая нагрузку/износ в рецептах, где машина требует техническую смазку.

**Description (EN):**

Motor oil is a lubricant fluid for machine processes. It participates in processing recipes where the machine needs technical lubrication.

### Солнечный опреснитель ← `motoroil`

- **id:** `solardestiller`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/solardestiller`

**Описание (RU):**

Солнечный дистиллятор. Использует солнечное тепло для испарения и очистки жидкости без обычного топлива.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Одиночный жидкостный адаптер ← `solardestiller`

- **id:** `single_fluid_adapter`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/single_fluid_adapter`

**Описание (RU):**

Адаптер для превращения предмета в жидкостный эквивалент или обратно по поддерживаемой схеме. Используется в материалах с точным объёмом жидкости.

**Description (EN):**

A technical fluid or gas used as a reagent, fuel or working medium in chemistry and energy processes.

### Генератор обсидиана ← `single_fluid_adapter`

- **id:** `gen_obsidian`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/gen_obsidian`

**Описание (RU):**

Принимает воду и лаву во внутренние баки, тратит EF и создаёт обсидиан в выходной слот. Это генератор блока, а не энергетический генератор.

**Description (EN):**

Accepts water and lava in internal tanks, consumes EF and creates obsidian in the output slot. This is a block generator, not an energy generator.

### Рыболовная машина ← `gen_obsidian`

- **id:** `fisher`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/fisher`

**Описание (RU):**

Автоматически ловит рыбу через ванильную таблицу рыбалки. Требует воду под машиной, удочку или энергетический аналог в слоте и тратит EF за улов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Производитель иссушителей ← `fisher`

- **id:** `gen_wither`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine1/gen_wither`

**Описание (RU):**

Создаёт визер-ресурсы в машинном процессе. Используется для опасных материалов, которые неудобно получать вручную.

**Description (EN):**

Produces energy or a technical resource from the appropriate fuel, fluid or input material.

### cooling mixture ← `single_fluid_adapter`

- **id:** `cooling_mixture`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Охлаждающая смесь для реакторов и холодильных механизмов. Отводит тепло там, где обычной воды или воздуха недостаточно.

**Description (EN):**

Lowers the temperature of a fluid or item for processes that require active cooling.

### construction foam ← `cooling_mixture`

- **id:** `construction_foam`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/construction_foam`

**Описание (RU):**

Строительная пена — жидкий материал для распылителя и жидкостных машин. Используется для быстрого создания/заполнения строительных блоков и как реагент в интеграторах.

**Description (EN):**

Construction foam is a liquid building material for the sprayer and fluid machines. It is used to quickly create or fill construction blocks and as a reagent in integrators.

### sprayer ← `construction_foam`

- **id:** `sprayer`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Распылитель для строительной пены и похожих жидкостей. Наносит содержимое на блоки в мире.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### reinforcedstone ← `sprayer`

- **id:** `reinforcedstone`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Укреплённый камень для прочных конструкций и защиты. Выдерживает нагрузки лучше обычного строительного блока.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Генератор жидкой материи ← `reinforcedstone`

- **id:** `generator_fluid_matter`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:simplemachine/generator_matter`

**Описание (RU):**

Генерирует жидкую материю из энергии и входных ресурсов. Используется в материальной переработке и репликации.

**Description (EN):**

Produces energy or a technical resource from the appropriate fuel, fluid or input material.

### Сканер ← `generator_fluid_matter`

- **id:** `scanner_iu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/scanner_iu`

**Описание (RU):**

Сканирует предмет и записывает его шаблон. Нужен для систем, которые работают с материей и точным воспроизведением предметов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Хранилище шаблонов ← `scanner_iu`

- **id:** `pattern_storage_iu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/pattern_storage_iu`

**Описание (RU):**

Хранит отсканированные шаблоны предметов. Позволяет держать несколько паттернов для последующей репликации.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Репликатор ← `pattern_storage_iu`

- **id:** `replicator_iu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/replicator_iu`

**Описание (RU):**

Воссоздаёт предмет по сохранённому шаблону, расходуя материю и энергию. Работает как финальная машина репликационной системы.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Сборщик материи ← `replicator_iu`

- **id:** `matter_collector`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/matter_collector`

**Описание (RU):**

Собирает материю или материальный ресурс из окружающей системы. Используется как источник для машин репликации.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Коробка утильсырья ← `replicator_iu`

- **id:** `scrapBox`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:crafting_elements/crafting_288_element`

**Описание (RU):**

Коробка утиля со случайным содержимым. Открывается как переработанный материал, полученный из скрапа.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Сборщик утильсырья ← `scrapBox`

- **id:** `assamplerscrap`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:moremachine3/assamplerscrap`

**Описание (RU):**

Автоматически обрабатывает скрап и коробки утиля. Удобен для массовой переработки отходов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Улучшение "Улучшенный ускоритель" ← `cooling_mixture`

- **id:** `overclockerUpgrade`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:upgrades/overclockerupgrade1`

**Описание (RU):**

Ускоряет работу машины, обычно увеличивая расход энергии. Используется в слотах улучшений поддерживаемых механизмов.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Улучшение "Трансформатор x2" ← `overclockerUpgrade`

- **id:** `transformerUpgrade`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:upgrades/transformerupgrade1`

**Описание (RU):**

Повышает допустимый энергетический уровень машины. Помогает безопасно подключать механизм к более сильной сети.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### МФЭ ← `transformerUpgrade`

- **id:** `mfe_iu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:wiring_storage/mfe_iu`

**Описание (RU):**

Энергетическое хранилище среднего уровня. Накапливает EF и отдаёт её в сеть через настроенные стороны.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Мусорный бак для жидкостей ← `mfe_iu`

- **id:** `fluid_trash`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/fluid_trash`

**Описание (RU):**

Уничтожает поступающую жидкость. Нужен для безопасного сброса лишних жидкостей из автоматических линий.

**Description (EN):**

A technical fluid or gas used as a reagent, fuel or working medium in chemistry and energy processes.

### Мусорный бак для энергии ← `mfe_iu`

- **id:** `energy_trash`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/energy_trash`

**Описание (RU):**

Поглощает лишнюю энергию. Используется как нагрузка или аварийный слив для энергетической сети.

**Description (EN):**

Deletes an unwanted resource from an automated line, such as items, fluids or energy that should not be stored.

### Мусорный бак для предметов ← `mfe_iu`

- **id:** `item_trash`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/item_trash`

**Описание (RU):**

Уничтожает предметы из входа. Полезен для линий, где нужно удалять мусор и лишние побочные продукты.

**Description (EN):**

Deletes an unwanted resource from an automated line, such as items, fluids or energy that should not be stored.

### Электролизёр ← `mfe_iu`

- **id:** `electrolyzer_iu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/electrolyzer_iu`

**Описание (RU):**

Разлагает жидкости или материалы электрическим процессом. Используется в химии для получения газов и отдельных компонентов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Чаровальная машина ← `electrolyzer_iu`

- **id:** `enchanter_books`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/enchanter_books`

**Описание (RU):**

Работает с зачарованными книгами и опытом. Позволяет обрабатывать или получать книги зачарований машинным способом.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Молекулярный преобразователь ← `electrolyzer_iu`

- **id:** `molecular`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:molecular/molecular`

**Описание (RU):**

Молекулярный преобразователь. Тратит энергию на превращение одного материала в другой по заданным рецептам.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Улучшенная солнечная панель ← `molecular`

- **id:** `advanced_solar_paneliu`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:machines/advanced_solar_paneliu`

**Описание (RU):**

Продвинутая солнечная панель. Производит больше энергии от света, чем базовые солнечные элементы.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Сборщик мини-панели ← `advanced_solar_paneliu`

- **id:** `minipanel`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/minipanel`

**Описание (RU):**

Малая солнечная панель. Компактно производит энергию от света и подходит для небольших установок.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Контроллер громоотвода ← `advanced_solar_paneliu`

- **id:** `lightning_rod_controller`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:lightning_rod/lightning_rod_controller`

**Описание (RU):**

Контроллер молниеотвода. Управляет установкой, которая принимает энергию от ударов молнии.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Производитель активной массы ← `minipanel`

- **id:** `matter_factory`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/matter_factory`

**Описание (RU):**

Фабрика компонентов материи. Производит части, нужные для материальных машин и репликации.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Завод батареек ← `matter_factory`

- **id:** `battery_factory`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/battery_factory`

**Описание (RU):**

Фабрика аккумуляторов. Собирает энергетические элементы и батареи для предметов и машин.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Завод розеток ← `battery_factory`

- **id:** `socket_factory`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/socket_factory`

**Описание (RU):**

Фабрика разъёмов и соединительных частей. Делает электрические компоненты для кабелей, батарей и машин.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### photoniy ingot ← `molecular`

- **id:** `photoniy_ingot`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Фотониевый слиток для высокоуровневых энергетических и световых технологий. Используется в продвинутых корпусах и компонентах.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Воздухоразделительная установка ← `photoniy_ingot`

- **id:** `aircollector`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/aircollector`

**Описание (RU):**

Собирает воздух или атмосферные компоненты из окружающей среды. Даёт газовое сырьё для химических и экологических процессов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Анализатор почвы ← `photoniy_ingot`

- **id:** `soil_analyzer`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/soil_analyzer`

**Описание (RU):**

Анализирует загрязнение и состояние почвы. Помогает понять, насколько участок заражён или пригоден для очистки.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Радиационный очиститель ← `soil_analyzer`

- **id:** `radiation_purifier`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/radiation_purifier`

**Описание (RU):**

Очищает радиационное загрязнение в зоне действия. Используется для восстановления опасных участков после ядерных процессов.

**Description (EN):**

Cleans pollution or shows environmental state for air, soil and dangerous areas.

### Реактор ядерного синтеза ← `aircollector`

- **id:** `synthesis`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine1/synthesis`

**Описание (RU):**

Синтезатор создаёт материалы из подготовленных компонентов. Работает как машина сложной сборки вещества.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### upgrade speed creation ← `aircollector`

- **id:** `upgrade_speed_creation`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Улучшение скорости для некоторых механизмов. Увеличивает темп работы там, где машина поддерживает внутренний уровень ускорения.

**Description (EN):**

A module or upgrade that changes supported machine behavior by improving speed, storage or adding a special function.

### Сканер загрязнения ← `synthesis`

- **id:** `pollution_scanner`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:pollution_device`

**Описание (RU):**

Сканер загрязнения показывает состояние воздуха, почвы и опасных зон. Помогает искать источник загрязнения и контролировать очистку.

**Description (EN):**

Cleans pollution or shows environmental state for air, soil and dangerous areas.

### Контроллер паровой турбины ← `photoniy_ingot`

- **id:** `steam_turbine_controller`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:steam_turbine/steam_turbine_controller`

**Описание (RU):**

Контроллер паровой турбины управляет многоблоком, который потребляет пар и выдаёт энергию. Проверяет структуру, рабочие части и поток пара.

**Description (EN):**

Controls a multiblock steam turbine that consumes steam and outputs energy. It checks the structure, working parts and steam flow.

### research lens ← `synthesis`

- **id:** `research_lens`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Исследовательская линза для космического стола. Открывает анализ более сложных объектов и материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Стол космических исследований ← `research_lens`

- **id:** `research_table_space`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/research_table_space`

**Описание (RU):**

Космический исследовательский стол работает с линзами и образцами небесных тел. Он открывает/обрабатывает данные по планетам, спутникам и материалам, а не является обычным верстаком.

**Description (EN):**

The space research table works with lenses and samples from celestial bodies. It processes data about planets, moons and materials rather than acting as a normal crafting table.

### Ракетная стартовая площадка ← `research_table_space`

- **id:** `rocket_launch_pad`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/rocket_launch_pad`

**Описание (RU):**

Стартовая площадка принимает ракетное топливо и энергию, заправляет и заряжает установленную космическую технику. Имеет большой топливный бак и выходные слоты для результатов/ресурсов, связанных с полётом.

**Description (EN):**

The launch pad accepts rocket fuel and energy, then refuels and charges placed space vehicles. It has a large fuel tank and output slots for flight-related results and resources.

### Сборщик марсоходов ← `rocket_launch_pad`

- **id:** `rover_assembler`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/rover_assembler`

**Описание (RU):**

Собирает роверы из корпуса, колёс, модулей и электронных частей. Машина формирует готовый предмет ровера с параметрами, которые потом используются на планетах.

**Description (EN):**

Assembles rovers from a body, wheels, modules and electronics. The machine creates the final rover item with parameters used later on planets.

### Сборщик зондов ← `rover_assembler`

- **id:** `probe_assembler`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/probe_assembler`

**Описание (RU):**

Собирает исследовательские зонды из корпуса, электроники и модулей. Зонд нужен для автоматического изучения тел и получения данных/ресурсов из космической системы.

**Description (EN):**

Assembles research probes from a body, electronics and modules. Probes are used to study celestial bodies and obtain data or resources in the space system.

### Сборщик спутников ← `probe_assembler`

- **id:** `satellite_assembler`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/satellite_assembler`

**Описание (RU):**

Собирает спутники из корпуса, электроники и модулей. Спутник работает как отдельная космическая техника для исследований и получения ресурсов с орбиты/тел.

**Description (EN):**

Assembles satellites from a body, electronics and modules. Satellites act as space equipment for research and resource collection from orbit or celestial bodies.

### Сборщик ракет ← `satellite_assembler`

- **id:** `rocket_assembler`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/rocket_assembler`

**Описание (RU):**

Собирает ракету из корпуса, двигателя, баков и модулей. Результат хранит параметры ракеты и используется стартовой площадкой для подготовки полёта.

**Description (EN):**

Assembles a rocket from a body, engine, tanks and modules. The result stores rocket parameters and is used by the launch pad for flight preparation.

### hydrazine ← `rover_assembler`

- **id:** `hydrazine`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/hydrazine`

**Описание (RU):**

Гидразин — базовое ракетное топливо. Принимается стартовой площадкой, жидкостным миксером и системой уровней топлива для роверов/ракет.

**Description (EN):**

Hydrazine is the basic rocket fuel. It is accepted by the launch pad, fluid mixer and rover/rocket fuel level system.

### rover ← `hydrazine`

- **id:** `rover`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:rover/rover`

**Описание (RU):**

Базовый ровер для работы на поверхности планет. Перевозит модули и выполняет задачи исследования местности.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Очиститель почвы ← `rover`

- **id:** `purifier_soil`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/purifier_soil`

**Описание (RU):**

Очищает загрязнённую почву. Работает как экологическая машина для восстановления заражённых участков.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### Камешек с Луны ← `rover`

- **id:** `moon_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/moon_pebble`

**Описание (RU):**

Космический камень с Луны. Содержит материалы, характерные для лунной поверхности.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### meteoric iron ← `moon_pebble`

- **id:** `meteoric_iron`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemingots/meteoric_iron`

**Описание (RU):**

Метеоритное железо. Прочный космический металл для корпусов, деталей и дальнейшей космической переработки.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### spaceupgrademodule schedule ← `meteoric_iron`

- **id:** `spaceupgrademodule_schedule`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Модульное расписание для космических улучшений. Хранит настройку или набор модулей для техники.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Модификационная станция роверов ← `spaceupgrademodule_schedule`

- **id:** `upgrade_rover`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine3/upgrade_rover`

**Описание (RU):**

Машина для установки улучшений на ровер. Работает с модулями, которые расширяют возможности планетохода.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Камешек с Марса ← `upgrade_rover`

- **id:** `mars_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/mars_pebble`

**Описание (RU):**

Камень с Марса. Служит образцом планетарной породы и источником марсианских материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### adamantium ← `mars_pebble`

- **id:** `adamantium`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemingots/adamantium`

**Описание (RU):**

Космический сверхпрочный металл. Используется в тяжёлых корпусах и деталях высокого уровня.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### dimethylhydrazine ← `adamantium`

- **id:** `dimethylhydrazine`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/dimethylhydrazine`

**Описание (RU):**

Диметилгидразин — более высокий уровень ракетного топлива. Используется в стартовой площадке и космической топливной системе для техники, которой нужно топливо сильнее обычного гидразина.

**Description (EN):**

Dimethylhydrazine is a higher-tier rocket fuel. It is used by the launch pad and space fuel system for vehicles that need stronger fuel than hydrazine.

### adv rover ← `dimethylhydrazine`

- **id:** `adv_rover`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:rover/adv_rover`

**Описание (RU):**

Улучшенный ровер с расширенными возможностями. Лучше подходит для сложных планетарных задач.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Исследовательская линза II ← `adv_rover`

- **id:** `research_lens_2`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:research_lens/lens_2`

**Описание (RU):**

Исследовательская линза более высокого уровня. Позволяет анализировать сложнее материалы и объекты.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Космический датчик: Давление ← `research_lens_2`

- **id:** `pressure_space_sensor`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:spaceupgrademodules/space_upgrademodule3`

**Описание (RU):**

Космический датчик давления. Используется в модулях и оборудовании, где нужно учитывать атмосферные условия.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Камешек с Венеры ← `pressure_space_sensor`

- **id:** `venus_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/venus_pebble`

**Описание (RU):**

Камень с Венеры. Представляет плотную планетарную породу и используется как образец для переработки.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Камешек с Меркурия ← `venus_pebble`

- **id:** `mercury_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/mercury_pebble`

**Описание (RU):**

Камень с Меркурия. Даёт доступ к материалам, связанным с горячими и металлическими планетарными породами.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### mithril ← `mercury_pebble`

- **id:** `mithril`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemingots/mithril`

**Описание (RU):**

Редкий космический металл для лёгких и прочных деталей. Хорошо подходит для высокоуровневых механизмов.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Исследовательская линза III ← `mithril`

- **id:** `research_lens_3`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:research_lens/lens_3`

**Описание (RU):**

Продвинутая исследовательская линза. Увеличивает глубину анализа для космических материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Камешек с Деймоса ← `research_lens_3`

- **id:** `deimos_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/deimos_pebble`

**Описание (RU):**

Камень с Деймоса. Используется как образец малых небесных тел и источник особых минералов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### orichalcum ← `deimos_pebble`

- **id:** `orichalcum`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemingots/orichalcum`

**Описание (RU):**

Редкий сплавный металл для космических деталей. Применяется в прочных и энергетически нагруженных компонентах.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Камешек с Теффида ← `orichalcum`

- **id:** `tethys_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/tethys_pebble`

**Описание (RU):**

Камень с Тефии. Связан с ледяными и внешними спутниками, даёт особые космические материалы.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### decane ← `tethys_pebble`

- **id:** `decane`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:bucket/decane`

**Описание (RU):**

Декан — углеводородное ракетное топливо и космический реагент. Принимается стартовой площадкой, используется в разделителях/электролизёрах и входит в уровень топлива для продвинутой космической техники.

**Description (EN):**

Decane is hydrocarbon rocket fuel and a space reagent. It is accepted by the launch pad, used by separators/electrolyzers and belongs to the advanced space fuel tier.

### imp rover ← `decane`

- **id:** `imp_rover`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:rover/imp_rover`

**Описание (RU):**

Усиленный ровер для сложных планетарных условий. Поддерживает более тяжёлые задачи исследования и работы на поверхности.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Камешек с Мимаса ← `imp_rover`

- **id:** `mimas_pebble`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemspace/mimas_pebble`

**Описание (RU):**

Камень с Мимаса. Используется как образец внешних спутников и источник редких материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### bloodstone ← `mimas_pebble`

- **id:** `bloodstone`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:itemingots/bloodstone`

**Описание (RU):**

Кровавый камень — редкий материал для продвинутых пластин и космических деталей.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### advanced hull machine ← `bloodstone`

- **id:** `advanced_hull_machine`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:blockresource/advanced_machine`

**Описание (RU):**

Усиленная корпусная пластина для машин. Используется в механизмах, которым требуется прочная оболочка.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Продвинутый молекулярный преобразователь ← `advanced_hull_machine`

- **id:** `double_transformer`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:double_transformer/double_transformer`

**Описание (RU):**

Двойной молекулярный преобразователь. Обрабатывает более сложные превращения материи и потребляет значительные объёмы энергии.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Анализатор ← `double_transformer`

- **id:** `analyzer`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:basemachine2/analyzer`

**Описание (RU):**

Анализатор предметов и данных. Используется для определения свойств материалов и подготовки их к технологическим процессам.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Исследовательская линза IV ← `analyzer`

- **id:** `research_lens_4`
- **вкладка:** `improvedElectricTab`
- **предмет:** `industrialupgrade:research_lens/lens_4`

**Описание (RU):**

Высокоуровневая исследовательская линза. Подходит для анализа редких космических материалов.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### double molecular ← `analyzer`

- **id:** `double_molecular`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Компонент двойного молекулярного преобразования. Используется в машинах, работающих с более сложными превращениями материи.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### antisoilpollution ← `double_molecular`

- **id:** `antisoilpollution`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Средство очистки почвенного загрязнения. Уменьшает заражение земли в зоне применения.

**Description (EN):**

Separates oil and heavy fluids into fuel and chemical fractions.

### antiairpollution ← `double_molecular`

- **id:** `antiairpollution`
- **вкладка:** `improvedElectricTab`

**Описание (RU):**

Средство очистки загрязнения воздуха. Помогает снижать вредные выбросы в окружающей зоне.

**Description (EN):**

Cleans pollution or shows environmental state for air, soil and dangerous areas.

---

## Совершенная — perElectric

### Совершенный завод сплавов

- **id:** `per_alloy_smelter`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/per_alloy_smelter`

**Описание (RU):**

Совершенная плавильня сплавов. Быстро обрабатывает сложные смеси и подходит для поздней металлургии.

**Description (EN):**

Heats materials and turns them into ingots, alloys or molten fluids for metallurgy.

### Экскаватор ← `per_alloy_smelter`

- **id:** `auto_digger`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/auto_digger`

**Описание (RU):**

Автоматический копатель. Добывает блоки в заданной области, расходуя энергию и складывая результат в выход.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### solid matter ← `auto_digger`

- **id:** `solid_matter`
- **вкладка:** `perElectric`

**Описание (RU):**

Твёрдая материя разных типов. Используется как материал для синтеза, преобразования и высокоуровневых машин.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Контроллер ветряной турбины ← `auto_digger`

- **id:** `wind_turbine_controller`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:wind_turbine/wind_turbine_controller`

**Описание (RU):**

Контроллер ветряной турбины управляет роторами и генераторной частью. Выработка зависит от ротора, условий и правильной сборки многоблока.

**Description (EN):**

Controls a wind turbine with rotor and generator parts. Output depends on rotor, conditions and correct multiblock assembly.

### Контроллер гидротурбины ← `wind_turbine_controller`

- **id:** `hydro_turbine_controller`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:hydro_turbine/hydro_turbine_controller`

**Описание (RU):**

Контроллер гидротурбины управляет водяной турбиной. Использует поток/водную рабочую часть и превращает его в энергию через многоблочную структуру.

**Description (EN):**

Controls a hydro turbine. It uses water flow/working parts and turns them into energy through the multiblock structure.

### Объединитель панелей ← `auto_digger`

- **id:** `sintezator`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:sintezator/sintezator`

**Описание (RU):**

Синтезатор собирает сложные материалы из подготовленных компонентов. Работает как высокоуровневая машина создания вещества.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Преобразователь твёрдой материи ← `solid_matter`

- **id:** `converter_matter`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:converter_matter/converter_matter`

**Описание (RU):**

Конвертер материи переводит один вид материального ресурса в другой. Используется в цепочках синтеза и репликации.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Кристаллизатор ← `research_lens_5`

- **id:** `crystallize`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/crystallize`

**Описание (RU):**

Кристаллизатор формирует кристаллы из жидкости или подготовленного материала. Используется для точных энергетических и космических компонентов.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Эндер-преобразователь ← `crystallize`

- **id:** `ender_assembler`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/ender_assembler`

**Описание (RU):**

Эндер-сборщик создаёт детали, связанные с эндер-материалами. Работает с высокоуровневыми компонентами и особой энергией.

**Description (EN):**

Combines casings, parts and special components into a finished technical item or machine part.

### Водяной преобразователь ← `ender_assembler`

- **id:** `aqua_assembler`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/aqua_assembler`

**Описание (RU):**

Аква-сборщик создаёт детали водной элементальной линии. Использует материалы, связанные с водой и жидкостями.

**Description (EN):**

Combines casings, parts and special components into a finished technical item or machine part.

### Незерский преобразователь ← `aqua_assembler`

- **id:** `nether_assembler`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/nether_assembler`

**Описание (RU):**

Незер-сборщик создаёт детали огненной и адской линии. Работает с материалами Нижнего мира и горячими компонентами.

**Description (EN):**

Combines casings, parts and special components into a finished technical item or machine part.

### Земляной преобразователь ← `nether_assembler`

- **id:** `earth_assembler`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/earth_assembler`

**Описание (RU):**

Земной сборщик создаёт детали из плотных минеральных материалов. Используется для элементальной линии земли.

**Description (EN):**

Combines casings, parts and special components into a finished technical item or machine part.

### Воздушный преобразователь ← `earth_assembler`

- **id:** `aer_assembler`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/aer_assembler`

**Описание (RU):**

Воздушный сборщик создаёт детали, связанные с воздухом и газами. Используется в элементальной линии воздуха.

**Description (EN):**

Combines casings, parts and special components into a finished technical item or machine part.

### Преобразователь нейтрония ← `aer_assembler`

- **id:** `neutronseparator`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/neutronseparator`

**Описание (RU):**

Нейтронный сепаратор разделяет материалы на нейтронные компоненты. Работает в поздней ядерной технологической цепочке.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Квантовый рудник ← `neutronseparator`

- **id:** `quantum_miner`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/quantum_miner`

**Описание (RU):**

Квантовый добытчик получает ресурсы через энергетический процесс. Использует квантовую логику вместо обычной добычи блоков.

**Description (EN):**

Automatically extracts resources from a target area or deposit and places the result into its output inventory.

### Квантовый преобразователь ← `quantum_miner`

- **id:** `quantum_transformer`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/quantum_transformer`

**Описание (RU):**

Квантовый преобразователь меняет материалы на глубоком энергетическом уровне. Требует много энергии и работает с редкими компонентами.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Пространственная плазма квантового мира ← `quantum_transformer`

- **id:** `quantum_plasma`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_646_element`

**Описание (RU):**

Квантовая плазма — высокоэнергетический материал. Используется в поздних преобразователях и квантовых механизмах.

**Description (EN):**

Works with matter and high energy to transform materials or prepare resources for replication and synthesis.

### Электрическая камера Вильсона ← `quantum_plasma`

- **id:** `positronconverter`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:basemachine3/positronconverter`

**Описание (RU):**

Позитронный конвертер преобразует материалы через антиматериальные реакции. Используется в очень поздней технологической цепочке.

**Description (EN):**

A technical item or mechanism used as part of the mod production system. Its role is defined by its machine, material and processing logic.

### Контроллер циклотрона ← `positronconverter`

- **id:** `cyclotron_controller`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:cyclotron/cyclotron_controller`

**Описание (RU):**

Контроллер циклотрона. Управляет многоблочной установкой для ускорения частиц и получения редких материалов.

**Description (EN):**

Controls a multiblock structure: checks the layout, links the parts and starts the shared work process.

### Продвинутая обшивка корпуса ← `cyclotron_controller`

- **id:** `photon_hull_plate`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:crafting_elements/crafting_139_element`

**Описание (RU):**

Фотонная корпусная пластина для поздних машин. Используется там, где нужна прочность и работа со световой энергией.

**Description (EN):**

A technical material for strong casings, parts and high-tier mechanisms where durability and precise processing matter.

### Камешек с Протея ← `photon_hull_plate`

- **id:** `proteus_pebble`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:itemspace/proteus_pebble`

**Описание (RU):**

Камень с Протея. Содержит материалы внешних спутников и используется как космический образец.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### xenon ← `proteus_pebble`

- **id:** `xenon`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:bucket/xenon`

**Описание (RU):**

Ксенон — топливо самого высокого уровня для космической техники. Его принимает стартовая площадка, система уровней топлива роверов/ракет и космические рецепты; также он получается из отдельных космических пород в электролизёре/разделителе.

**Description (EN):**

Xenon is the highest-tier fuel for space vehicles. The launch pad, rover/rocket fuel level system and space recipes use it, and it is also obtained from specific space rocks through electrolyzing or separation.

### Исследовательская линза VI ← `xenon`

- **id:** `research_lens_6`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:research_lens/lens_6`

**Описание (RU):**

Исследовательская линза максимального уровня. Подходит для самых редких космических материалов и поздних исследований.

**Description (EN):**

Used in space research, equipment assembly and processing materials from celestial bodies.

### Административная солнечная панель ← `research_lens_6`

- **id:** `admpanel`
- **вкладка:** `perElectric`
- **предмет:** `industrialupgrade:admpanel/admpanel`

**Описание (RU):**

Административная солнечная панель работает как мощный источник энергии. Это наследник солнечных панелей: накапливает энергию во внутреннем буфере, отдаёт её в сеть, учитывает день, ночь, дождь, измерение, загрязнение и установленные панельные модули.

**Description (EN):**

The administrative solar panel is a powerful energy source. It inherits the solar panel logic: it stores energy, outputs it to the network, and accounts for day, night, rain, dimension, pollution and installed panel modules.

---

## Дополнительные тексты guide.* / quarry.guide.*

### `guide.chemicalplant`

Информация о химической установке

*(EN)* Chemical Plant Information

### `guide.chemicalplant1`

⎜ Потребляет квантовую энергию.

*(EN)* ⎜ Consumes quantum energy

### `guide.chemicalplant2`

⎜ Превращает жидкий гелий в криоген.

*(EN)* ⎜ Transforms liquid helium into cryogen

### `guide.chemicalplant3`

⎜ Для превращения нужен иод.

*(EN)* ⎜ Iodine is required for the transformation

### `guide.geothermalpump`

Информация о геотермальном насосе

*(EN)* Geothermal Pump Information

### `guide.geothermalpump1`

⎜ Потребляет квантовую энергию.

*(EN)* ⎜ Consumes quantum energy

### `guide.geothermalpump2`

⎜ Превращает горячий хладагент в нефть.

*(EN)* ⎜ Transforms hot refrigerant into oil

### `guide.geothermalpump3`

⎜ С определённым шансом можно получить куски нефти и нитраты.

*(EN)* ⎜ There is a chance to get oil chunks and nitrates

### `quarry.guide.earth_quarry`

Информация о землянном карьере

*(EN)* Earth Quarry Information

### `quarry.guide.earth_quarry1`

⎜ Подключите контроллер к энергии.

*(EN)* ⎜ Connect the controller to energy.

### `quarry.guide.earth_quarry2`

⎜ Проведите анализ в анализаторе и дождитесь конца процедуры.

*(EN)* ⎜ Carry out analysis in the Analyzer and wait until the end of the procedure.

### `quarry.guide.earth_quarry3`

⎜ После анализатора, нажмите на кнопку в этом интерфейсе.

*(EN)* ⎜ After the analyzer, click on the button in this interface.

### `quarry.guide.earth_quarry4`

⎜ Карьер копает 3x3 чанка.

*(EN)* ⎜ Digs a quarry of 3x3 chunks.

### `quarry.guide.earth_quarry5`

⎜ При повторном вскапывании ничего не произойдет.

*(EN)* ⎜ Nothing will happen if you dig again.

### `quarry.guide.earth_quarry6`

⎜ Превращает песок в литиевую руду.

*(EN)* ⎜ Transforms sand into lithium ore.

### `quarry.guide.earth_quarry7`

⎜ Превращает землю  в бериллиевую руду.

*(EN)* ⎜ Transforms earth into beryllium ore.

### `quarry.guide.earth_quarry8`

⎜ Превращает гравий в борную руду.

*(EN)* ⎜ Transforms gravel into quarry ore.

### `quarry.guide.earth_quarry9`

⎜ Ресурcы находятся в сундуках карьера.

*(EN)* ⎜ Resources are found in quarry chests.

### `quarry.guide.gas_well`

Информация о газовой установке

*(EN)* Gas well information

### `quarry.guide.gas_well1`

⎜ Подключите контроллер к источнику энергии.

*(EN)* ⎜ Connect the controller to a power source.

### `quarry.guide.gas_well2`

⎜ Выполните анализ в анализаторе и дождитесь завершения процесса.

*(EN)* ⎜ Perform analysis in the Analyzer and wait for the process to finish.

### `quarry.guide.gas_well3`

⎜ Если газовая жила не обнаружена, анализатор выдаст ошибку.

*(EN)* ⎜ If no gas vein is detected, the Analyzer will give an error.

### `quarry.guide.gas_well4`

⎜ После завершения анализа нажмите на кнопку в этом интерфейсе.

*(EN)* ⎜ After the analysis is complete, press the button in this interface.

### `quarry.guide.gas_well5`

⎜ Все газы имеют одинаковый шанс добычи.

*(EN)* ⎜ All gases have an equal chance of extraction.

### `quarry.guide.gas_well6`

⎜ Шанс обнаружить газовую жилу составляет 15%.

*(EN)* ⎜ The chance to detect a gas vein is 15%.

### `quarry.guide.gas_well7`

Используйте многоблочную структуру «Газовая установка» для обнаружения и добычи газов. Установка позволяет добывать бром, йод, хлор и фтор.

*(EN)* Use the "Gas Installation" multiblock structure to detect and extract gases. The installation allows the extraction of bromine, iodine, chlorine, and fluorine.

### `quarry.guide.gasturbine`

Руководство по газовой турбине

*(EN)* Gas Turbine Guide

### `quarry.guide.gasturbine1`

⎜ Генерирует энергию из газов.

*(EN)* ⎜ Generates energy from gases.

### `quarry.guide.gasturbine2`

⎜ Используйте радиаторы в рекуператоре (сверху и снизу структуры).

*(EN)* ⎜ Use radiators in the recuperator (top and bottom of the structure).

### `quarry.guide.gasturbine3`

⎜ Для работы турбины должны быть установлены все радиаторы.

*(EN)* ⎜ All radiators must be installed for the turbine to operate.
