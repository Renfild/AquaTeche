# -*- coding: utf-8 -*-
"""
AquaTech: Patchouli Guidebook Generator (Version 2.0 - Rich, Beautiful & Illustrated)
Generates clean, well-spaced pages with crafting recipes and spotlight previews.
No technical hosting/backend jargon.
"""
import os
import json

categories = {
    'start': {
        'name': 'Старт на AquaTech',
        'description': 'Океанический скайблок, первые шаги и выживание',
        'icon': 'minecraft:compass',
        'sortnum': 1
    },
    'fishing': {
        'name': 'Искусство Рыбалки',
        'description': 'Цепочка удочек, свежесть, рейт-моды и наживки',
        'icon': 'starcatcher:starcatcher_rod',
        'sortnum': 2
    },
    'machines': {
        'name': 'Механизмы AquaTech',
        'description': 'Авто-рыбак MK-2, экстрактор, синтезатор и крафты',
        'icon': 'aquatech_machines:extractor',
        'sortnum': 3
    },
    'botania': {
        'name': 'Симбиоз с Botania',
        'description': 'Мана-Фабрикатор, Цветолов и техно-магия',
        'icon': 'botania:manasteel_ingot',
        'sortnum': 4
    },
    'custom_mods': {
        'name': 'Особое Снаряжение',
        'description': 'Эхолот, магнит, ящик для снастей и механики',
        'icon': 'aquatech_ui:sonar_goggles',
        'sortnum': 5
    },
    'progression': {
        'name': 'Прогрессия сборки',
        'description': 'От плота к Industrial Upgrade, AE2 и Эндгейму',
        'icon': 'minecraft:netherite_ingot',
        'sortnum': 6
    },
    'economy': {
        'name': 'Экономика и F4',
        'description': 'АкваМонеты, скупка рыбы, аукцион и кейсы',
        'icon': 'minecraft:sunflower',
        'sortnum': 7
    },
    'perks': {
        'name': 'Привилегии и Ранги',
        'description': 'Ранги от Моряка до VIP, обмен валют и гемы',
        'icon': 'minecraft:nether_star',
        'sortnum': 8
    }
}

entries = {
    # =========================================================================
    # 1. START
    # =========================================================================
    'start/welcome': {
        'name': 'Мир AquaTech',
        'category': 'aquatech_ui:start',
        'icon': 'minecraft:grass_block',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Мир AquaTech',
                'text': 'Добро пожаловать в $(#29b6f6)$(bold)AquaTech$()!$(br2)Это техно-магический $(bold)океанический скайблок$().$(br2)Твой путь начинается на деревянном плоту 4×4 посреди бескрайних вод.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Без шахт под ногами',
                'text': 'Здесь $(italic)нет привычных подземных шахт$().$(br2)Все руды, металлы и сокровища добываются через $(#29b6f6)рыболовство$(), науку и переработку сырья.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Правила океана',
                'text': '$(li)Не прыгай в воду без снаряжения — на глубине холод и давление.$(br)$(li)Твой остров защищен приватом.$(br)$(li)Главная кнопка — $(bold)F4$() (кабинет).$(br2)$(italic)Удачного выживания!$()'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:guide_book',
                'title': 'Крафт Гайдбука',
                'text': 'Если потеряешь эту книгу — её всегда можно создать из обычной книги и призмарина.'
            }
        ]
    },
    'start/first_steps': {
        'name': 'Первые шаги',
        'category': 'aquatech_ui:start',
        'icon': 'starcatcher:bamboo_rod',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Первые шаги',
                'text': 'С чего начать путь:$(br2)$(bold)1. Меню F4$():$(br)Открой раздел $(bold)Киты$() и забери стартовые предметы.$(br2)$(bold)2. Забрось удочку$():$(br)Поймай первую рыбу, медь, железо и глину.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:bamboo_rod_craft',
                'title': 'Бамбуковая удочка',
                'text': 'Базовая снасть. Позволяет добывать первые самородки меди и железа.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:humble_rod_craft',
                'title': 'Скромная удочка',
                'text': 'Следующий тир: крафтится из меди и бамбуковой удочки. Ловит олово и уголь.'
            }
        ]
    },
    'start/f4_hub': {
        'name': 'Личный кабинет F4',
        'category': 'aquatech_ui:start',
        'icon': 'minecraft:gold_ingot',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Личный кабинет F4',
                'text': 'Клавиша $(bold)F4$() открывает центр управления $(#29b6f6)AquaLumen$():$(br2)$(li)$(bold)Профиль$() — статистика и баланс.$(br)$(li)$(bold)Скупщик$() — продажа улова.$(br)$(li)$(bold)Магазин$() — ресурсы за монеты.$(br)$(li)$(bold)Кейсы$() — сундуки с гарантом.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Сервисы F4',
                'text': '$(li)$(bold)Киты$() — получение наборов.$(br)$(li)$(bold)Пропуск$() — 25 уровней наград.$(br)$(li)$(bold)Аукцион$() — рынок игроков.$(br)$(li)$(bold)События$() — турниры и бури.$(br2)Меню открывается в любой точке мира без задержек!'
            }
        ]
    },
    'start/commands': {
        'name': 'Полезные команды',
        'category': 'aquatech_ui:start',
        'icon': 'minecraft:writable_book',
        'sortnum': 4,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Команды навигации',
                'text': '$(bold)/spawn$() — возврат на спавн.$(br2)$(bold)/sethome <имя>$() — поставить точку дома.$(br2)$(bold)/home <имя>$() — вернуться домой.$(br2)$(bold)/warp <имя>$() — телепорт на варп.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Торговля и общение',
                'text': '$(bold)/ah sell <цена>$() — продать предмет на аукционе.$(br2)$(bold)/pay <ник> <сумма>$() — перевод монет.$(br2)$(bold)/msg <ник> <текст>$() — личное сообщение.$(br2)$(italic)Наборы предметов забираются в меню F4 → Киты.$()'
            }
        ]
    },

    # =========================================================================
    # 2. FISHING
    # =========================================================================
    'fishing/rods_chain': {
        'name': 'Цепочка удочек',
        'category': 'aquatech_ui:fishing',
        'icon': 'starcatcher:obsidian_rod',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Цепочка удочек',
                'text': 'Каждая новая удочка крафтится из $(bold)предыдущей$() плюс ресурсы, которые эта удочка ловит!$(br2)T1: Бамбуковая$(br)T2: Скромная$(br)T3: Добрая старая$(br)T4: Натуралиста$(br)T5: Слаймовая'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:good_old_rod_craft',
                'title': 'Добрая старая удочка',
                'text': 'Крафтится из Скромной удочки, железа, олова и меди.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:naturalist_rod_craft',
                'title': 'Удочка Натуралиста',
                'text': 'Железный тир. Добывает редстоун, шпинель и ценные минералы.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:obsidian_rod_craft',
                'title': 'Обсидиановая удочка',
                'text': 'Тир 8: незерит и плачущий обсидиан. Необходима для крафта Авто-рыбака!'
            }
        ]
    },
    'fishing/rate_mods': {
        'name': 'Рейт-апгрейды',
        'category': 'aquatech_ui:fishing',
        'icon': 'aquatech_ui:rate_x4',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Рейт-апгрейды',
                'text': 'Рейт-моды $(bold)rate_mod$() от x2 до x64 вставляются в слот наживки удочки!$(br2)Они умножают весь дроп:$(br)$(li)x2 — двойной улов руды и рыбы$(br)$(li)x4 / x8 — четырёх- и восьмикратный$(br)$(li)x16 / x32 / x64 — горы сырья!'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:rate_x2',
                'title': 'Модуль Rate x2',
                'text': 'Начальный модуль удвоения. Крафтится из манастали, меди и железа.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:rate_x4',
                'title': 'Модуль Rate x4',
                'text': 'Учетверение улова. Требует модуль x2, жемчуг маны, серебро и алмаз маны.'
            }
        ]
    },
    'fishing/freshness_grades': {
        'name': 'Свежесть и грейды',
        'category': 'aquatech_ui:fishing',
        'icon': 'minecraft:clock',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Свежесть улова',
                'text': 'Вся пойманная рыба свежая в течение 30 минут:$(br2)$(#4caf50)$(bold)+20% к стоимости$() у скупщика F4!$(br2)Таймер отображается в тултипе предмета зеленым цветом. Сдавай улов сразу для максимального заработка.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Грейды качества',
                'text': 'При вылове рыба может получить грейд качества:$(br2)$(li)$(bold)Серебряный$(): цена ×1.25$(br)$(li)$(bold)Золотой$(): цена ×1.6$(br)$(li)$(bold)Радужный$(): цена ×3.0!$(br2)Шансы на грейды выше в шторм и ночью.'
            }
        ]
    },
    'fishing/baits': {
        'name': 'Наживки',
        'category': 'aquatech_ui:fishing',
        'icon': 'aquatech_ui:bait_shoal',
        'sortnum': 4,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_ui:bait_shoal',
                'title': 'Стайная наживка',
                'text': 'Дает +30% шанс поймать дубликат рыбы за один заброс. Незаменима для заполнения Атласа видов.'
            },
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_ui:bait_ore',
                'title': 'Рудная наживка',
                'text': 'Дает +35% шанс достать дополнительный кусок редкой руды или драгоценный самородок.'
            },
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_ui:bait_abyss',
                'title': 'Бездненная наживка',
                'text': 'Редчайшая наживка: дает +40% шанс выловить рыбу или руду из тира ВЫШЕ твоей текущей удочки!'
            }
        ]
    },

    # =========================================================================
    # 3. MACHINES
    # =========================================================================
    'machines/fisher': {
        'name': 'Авто-рыбак MK-2',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:fisher',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:fisher',
                'title': 'Авто-рыбак MK-2',
                'text': 'Автономная станция непрерывного лова. Работает на энергии FE с установленной удочкой. Выталкивает улов в сундук.$(br2)С Ядром Рыболова ловит рыбу; в этом режиме рейт удочки ограничен ×4, чтобы станция не превращалась в монетный кран. Ресурсный режим (без ядра) рейтом не ограничен.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:fisher',
                'title': 'Крафт Авто-рыбака',
                'text': 'Собирается на верстаке из Обсидиановой удочки, процессоров и композитных сплавов.'
            }
        ]
    },
    'machines/excavator': {
        'name': 'Экскаватор',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:excavator',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:excavator',
                'title': 'Экскаватор',
                'text': 'Глубинная буровая установка. Бурит пласты океанического дна, добывая глину, базальт, гравий и древние породы.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:excavator',
                'title': 'Крафт Экскаватора',
                'text': 'Собирается из алмазных блоков, незеритовых слитков и прочных стальных шестерней.'
            }
        ]
    },
    'machines/extractor': {
        'name': 'Экстрактор',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:extractor',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:extractor',
                'title': 'Экстрактор',
                'text': 'Высокоточный сепаратор сырья: удваивает выход металлов из сырых руд, очищает чистый кварц и серу.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:extractor',
                'title': 'Крафт Экстрактора',
                'text': 'Создается из обсидиана, поршней и продвинутых компонентов.'
            }
        ]
    },
    'machines/synthesizer': {
        'name': 'Синтезатор',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:synthesizer',
        'sortnum': 4,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:synthesizer',
                'title': 'Синтезатор',
                'text': 'Гидротермальный синтезатор. Использует лаву и химические реагенты для создания сплавов и вулканических кристаллов.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:synthesizer',
                'title': 'Крафт Синтезатора',
                'text': 'Требует вёдра лавы, титан и термостойкие пластины.'
            }
        ]
    },
    'machines/centrifuge': {
        'name': 'Центрифуга',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:centrifuge',
        'sortnum': 5,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:centrifuge',
                'title': 'Центрифуга',
                'text': 'Разделяет сложные жидкости и рассолы. Производит морскую соль и минеральные соли для химических цепочек.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:centrifuge',
                'title': 'Крафт Центрифуги',
                'text': 'Собирается из стали, стекла и приводов центробежного вращения.'
            }
        ]
    },
    'machines/upgrades': {
        'name': 'Улучшения машин',
        'category': 'aquatech_ui:machines',
        'icon': 'aquatech_machines:speed_upgrade_1',
        'sortnum': 6,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:speed_upgrade_1',
                'title': 'Улучшения машин',
                'text': 'Все механизмы принимают модули скорости и энергоэффективности. Модули надежно защищены от вытягивания трубами.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:speed_upgrade_1',
                'title': 'Скорость x1',
                'text': 'Сокращает время каждого рабочего цикла механизма вдвое.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:speed_upgrade_4',
                'title': 'Скорость x4',
                'text': 'Максимальное 4-кратное ускорение рабочего цикла машины.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:energy_efficiency_crystal',
                'title': 'Энергоэффективность',
                'text': 'Снижает потребление энергии FE на каждую операцию механизма.'
            }
        ]
    },

    # =========================================================================
    # 4. BOTANIA
    # =========================================================================
    'botania/flower_collector': {
        'name': 'Цветолов',
        'category': 'aquatech_ui:botania',
        'icon': 'aquatech_machines:flower_collector',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:flower_collector',
                'title': 'Цветолов',
                'text': 'Материализует 162 вида цветов Botania из эфира! Расходует 250 маны и 9 600 FE за цветок.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_machines:flower_collector',
                'title': 'Крафт Цветолова',
                'text': 'Собирается из мана-жемчуга, лепестков и промышленных микросхем.'
            }
        ]
    },
    'botania/mana_fabricator': {
        'name': 'Мана-Фабрикатор',
        'category': 'aquatech_ui:botania',
        'icon': 'aquatech_machines:mana_fabricator',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:mana_fabricator',
                'title': 'Мана-Фабрикатор',
                'text': 'Преобразует 2 400 FE в 200 маны каждые 2 секунды. Устанавливается вплотную к Бассейну маны (Mana Pool).'
            },
            {
                'type': 'patchouli:text',
                'title': 'Подключение',
                'text': 'Подключи провод FE от генераторов к фабрикатору.$(br2)Он будет бесперебойно наполнять мана-пул без необходимости ручного кормления цветов!'
            }
        ]
    },
    'botania/cycle': {
        'name': 'Техно-магический цикл',
        'category': 'aquatech_ui:botania',
        'icon': 'botania:mana_pylon',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Симбиоз систем',
                'text': 'Связка механизмов дает полную автономию:$(br2)1. Генераторы вырабатывают энергию FE.$(br)2. Мана-Фабрикатор льет ману в пул.$(br)3. Цветолов добывает любые цветы.$(br)4. Автоматизируется крафт манастали, террастали и рун.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Результат',
                'text': 'Больше не нужно вручную разводить семена и лепестки.$(br2)Инженерия AquaTech открывает прямой доступ ко всей мощи магии Botania!'
            }
        ]
    },

    # =========================================================================
    # 5. CUSTOM MODS & GEAR
    # =========================================================================
    'custom_mods/gear': {
        'name': 'Особое Снаряжение',
        'category': 'aquatech_ui:custom_mods',
        'icon': 'aquatech_ui:sonar_goggles',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_ui:sonar_goggles',
                'title': 'Эхолотные очки',
                'text': 'Особый прибор для исследования глубин: позволяет видеть силуэты рыбы сквозь толщу воды.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:sonar_goggles',
                'title': 'Крафт Эхолота',
                'text': 'Собирается из кожаного шлема, кристалла призмарина, стекла и Сердца моря.'
            },
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_ui:abyssal_magnet',
                'title': 'Бездненный магнит',
                'text': 'Притягивает выловленный дроп и руду прямо в инвентарь игрока с большой дистанции.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech_ui:abyssal_magnet',
                'title': 'Крафт Магнита',
                'text': 'Создается из Сердца моря, слитков железа и редстоуна.'
            },
            {
                'type': 'patchouli:spotlight',
                'item': 'starcatcher:tackle_box',
                'title': 'Ящик для снастей',
                'text': 'Органайзер для удочек, наживок и рыбы. Продаётся у Кота-рыболова или в F4 → Киты.'
            }
        ]
    },

    # =========================================================================
    # 6. PROGRESSION
    # =========================================================================
    'progression/tier1_early': {
        'name': 'Акт 1: Плот и выживание',
        'category': 'aquatech_ui:progression',
        'icon': 'starcatcher:humble_rod',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Акт 1: Старт',
                'text': 'Твой путь на плоту:$(br2)$(li)Забери стартовый набор в меню F4 → Киты.$(br)$(li)Лови бамбуковой удочкой медь и железо.$(br)$(li)Скрафти Скромную удочку.$(br)$(li)Сдавай рыбу скупщику.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:kickstarter_boat',
                'title': 'Лодка с сундуком',
                'text': 'Удобная стартовая лодка для рыбалки и исследования океана.'
            }
        ]
    },
    'progression/tier2_mid': {
        'name': 'Акт 2: Стальной клёв',
        'category': 'aquatech_ui:progression',
        'icon': 'aquatech_machines:abyssal_alloy',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Акт 2: Глубина',
                'text': 'Развитие базы:$(br2)$(li)Удочки T4–T8 ловят золото, лазурит, алмазы и титан.$(br)$(li)Запуск Industrial Upgrade: генераторы, проводка, сплавы.$(br)$(li)Постройка первого Авто-рыбака MK-2.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Схемы и сплавы',
                'text': 'Создавай электронные схемы и композит. Океаническая руда поступает автоматически, питая твои плавильные печи.'
            }
        ]
    },
    'progression/tier3_ae2': {
        'name': 'Акт 3: Цифровизация (AE2)',
        'category': 'aquatech_ui:progression',
        'icon': 'ae2:controller',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Акт 3: AE2',
                'text': 'В сборке AquaTech $(bold)не нужно искать метеориты$()!$(br2)Все 4 типа прессов для процессоров свободно продаются в F4 → Магазин за АкваМонеты.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:me_controller',
                'title': 'МЭ Контроллер',
                'text': 'Сердце цифровой системы хранения и автокрафта.'
            },
            {
                'type': 'patchouli:crafting',
                'recipe': 'aquatech:me_drive',
                'title': 'МЭ Накопитель',
                'text': 'Устанавливай ячейки памяти и подключай терминалы доступа к ресурсам.'
            }
        ]
    },
    'progression/tier4_endgame': {
        'name': 'Акт 4: Эндгейм',
        'category': 'aquatech_ui:progression',
        'icon': 'aquatech_machines:volcanic_crystal',
        'sortnum': 4,
        'pages': [
            {
                'type': 'patchouli:spotlight',
                'item': 'aquatech_machines:fishing_core',
                'title': 'Рыбное ядро',
                'text': 'Сердце высших технологий. Крафтится на верстаке 8×8 Avaritia из редчайших сокровищ океанических глубин.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Вершина мастерства',
                'text': 'Альфа-удочка T11 с рейт-модом x64, полная автоматизация и доминирование на Аукционе!$(br2)$(italic)Ты стал владыкой океана AquaTech!$()'
            }
        ]
    },

    # =========================================================================
    # 7. ECONOMY
    # =========================================================================
    'economy/coins': {
        'name': 'АкваМонеты',
        'category': 'aquatech_ui:economy',
        'icon': 'minecraft:gold_nugget',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'АкваМонеты',
                'text': 'Основная валюта сервера:$(br2)$(li)Продавай рыбу скупщику F4.$(br)$(li)Выполняй 3 контракта дня.$(br)$(li)Торгуй на Аукционе.$(br)$(li)Побеждай в турнирах выходных.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Формула цены',
                'text': 'Цена у скупщика зависит от:$(br)$(bold)База × Вес × Редкость × Свежесть (+20%) × Грейд (до 3x) × Тренд дня$().$(br2)Следи за трендами дня в F4!'
            }
        ]
    },
    'economy/server_shop': {
        'name': 'Серверные поставки',
        'category': 'aquatech_ui:economy',
        'icon': 'minecraft:emerald',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Магазин поставок',
                'text': 'Вкладка F4 → Магазин:$(br2)Здесь можно купить редкие компоненты:$(br)$(li)Печати AE2 (кремний, инженерная и др.)$(br)$(li)Микросхемы Industrial Upgrade$(br)$(li)Манасталь, жемчуг маны$(br)$(li)Композит и кристаллы'
            },
            {
                'type': 'patchouli:text',
                'title': 'Мгновенная доставка',
                'text': 'Товары доставляются прямо в инвентарь за доли секунды.$(br2)Если не хватает пары деталей для важного механизма — склад всегда открыт!'
            }
        ]
    },
    'economy/auction': {
        'name': 'Аукцион игроков',
        'category': 'aquatech_ui:economy',
        'icon': 'minecraft:chest',
        'sortnum': 3,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Аукцион игроков',
                'text': 'Свободный рынок сервера:$(br2)$(bold)Продажа$():$(br)Возьми предмет в руку и напиши:$(br)$(bold)/ah sell <цена>$()$(br2)Лот мгновенно появится в каталоге F4 → Аукцион.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Удобство рынка',
                'text': 'Удобный поиск по названиям, фильтрация по типам и сортировка по цене.$(br2)Монеты начисляются даже если ты не в сети!'
            }
        ]
    },
    'economy/cases_pass': {
        'name': 'Кейсы и Пропуск',
        'category': 'aquatech_ui:economy',
        'icon': 'minecraft:ender_chest',
        'sortnum': 4,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Кейсы с гарантией',
                'text': 'В разделе F4 → Кейсы есть 10 тиров сундуков.$(br2)$(bold)Система Pity (гарант)$():$(br)Счетчик попыток гарантирует выпадение редкого суперприза при достижении лимита!'
            },
            {
                'type': 'patchouli:text',
                'title': 'Боевой Пропуск',
                'text': '25 уровней сезонных наград.$(br2)Доступен абсолютно бесплатно каждому игроку! Опыт даётся за вылов рыбы.$(br2)Награды: кейсы, рейт-моды и монеты.'
            }
        ]
    },

    # =========================================================================
    # 8. PERKS
    # =========================================================================
    'perks/ranks': {
        'name': 'Донат-ранги',
        'category': 'aquatech_ui:perks',
        'icon': 'minecraft:golden_helmet',
        'sortnum': 1,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'Ранги: Часть 1',
                'text': 'Линейка привилегий сервера:$(br2)$(li)$(#ffd54f)$(bold)Моряк$() — префикс, 2 дома.$(br)$(li)$(#ffd54f)$(bold)Шкипер$() — приоритет входа, кит.$(br)$(li)$(#ffd54f)$(bold)Капитан$() — полёт /fly на приватах.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Ранги: Часть 2',
                'text': '$(li)$(#ffd54f)$(bold)Адмирал$() — никнейм /nick, 10 домов.$(br)$(li)$(#ffd54f)$(bold)Легенда$() — максимальные лимиты.$(br)$(li)$(#ffd54f)$(bold)VIP$() — верстак /wb и эндер /ec.$(br2)$(italic)Ранги выдаются навсегда!$()'
            },
            {
                'type': 'patchouli:text',
                'title': 'Получение рангов',
                'text': 'Ранги приобретаются в F4 за $(#ffd54f)АкваГемы$().$(br2)Привилегии активируются мгновенно и действуют на сервере и сайте.'
            }
        ]
    },
    'perks/gems': {
        'name': 'АкваГемы',
        'category': 'aquatech_ui:perks',
        'icon': 'minecraft:emerald_block',
        'sortnum': 2,
        'pages': [
            {
                'type': 'patchouli:text',
                'title': 'АкваГемы',
                'text': 'АкваГемы — премиальная валюта.$(br2)$(bold)Честный обмен$():$(br)В магазине F4 доступен обмен:$(br)$(#ffd54f)$(bold)50 000 монет → 5 гемов$()!$(br2)Заработать на донат можно только своей активностью и ловлей рыбы.'
            },
            {
                'type': 'patchouli:text',
                'title': 'Честная игра',
                'text': 'На сервере нет скрытых привилегий: каждый упорный игрок может достичь вершин без реальных вложений!'
            }
        ]
    }
}

def main():
    target_dirs = [
        'mods/aquatech-ui/src/main/resources/data/aquatech_ui/patchouli_books/guide',
        'mods/aquatech-ui/src/main/resources/assets/aquatech_ui/patchouli_books/guide'
    ]

    langs = ['ru_ru', 'en_us']

    for base in target_dirs:
        for lang in langs:
            cat_dir = os.path.join(base, lang, 'categories')
            ent_dir = os.path.join(base, lang, 'entries')
            os.makedirs(cat_dir, exist_ok=True)
            os.makedirs(ent_dir, exist_ok=True)

            # Clear old categories
            for f in os.listdir(cat_dir):
                fp = os.path.join(cat_dir, f)
                if os.path.isfile(fp):
                    os.remove(fp)

            # Write categories
            for cat_id, cat_data in categories.items():
                fpath = os.path.join(cat_dir, f'{cat_id}.json')
                with open(fpath, 'w', encoding='utf-8') as f:
                    json.dump(cat_data, f, ensure_ascii=False, indent=2)

            # Clear old entries
            for root, dirs, files in os.walk(ent_dir, topdown=False):
                for file in files:
                    os.remove(os.path.join(root, file))
                for d in dirs:
                    os.rmdir(os.path.join(root, d))

            # Write entries
            for ent_path, ent_data in entries.items():
                full_path = os.path.join(ent_dir, f'{ent_path}.json')
                os.makedirs(os.path.dirname(full_path), exist_ok=True)
                with open(full_path, 'w', encoding='utf-8') as f:
                    json.dump(ent_data, f, ensure_ascii=False, indent=2)

    print(f'Successfully generated {len(categories)} categories and {len(entries)} entries across all target dirs!')

if __name__ == '__main__':
    main()
