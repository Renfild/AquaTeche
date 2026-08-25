#!/usr/bin/env python3
"""Regenerate secret + endgame chapters with generous rewards (ME cells, panels, prefix)."""
import uuid

def uid(): return uuid.uuid4().hex.upper()

# ---------- SECRET CHAPTER ----------
secret_quests = [
 {
  'title': 'Шёпот Бездны',
  'desc': ['Секрет: поймайте осколок эха абиссальной удочкой прямо из океана.',
           'Подсказка: глубина, ночь и новолуние резко повышают шанс.'],
  'task_item': 'minecraft:echo_shard', 'count': 3,
  'rewards': [
      ('item', 'ae2:fluid_storage_cell_256k', 1),
      ('item', 'ae2:item_storage_cell_256k', 1),
      ('item', 'industrialupgrade:adv_solar_energy', 1),
      ('xp', 2000),
  ],
  'x': 0.0, 'y': 0.0,
 },
 {
  'title': 'Звезда в Сети',
  'desc': ['Секрет: поднимите Морскую Звезду (Nether Star) удочкой.',
           'Только Абиссальная удочка или Альфа. Шанс мизерный — терпение и шторм.'],
  'task_item': 'minecraft:nether_star', 'count': 1,
  'rewards': [
      ('item', 'avaritia:infinity_ingot', 1),
      ('item', 'ae2:cell_component_256k', 4),
      ('xp', 5000),
  ],
  'deps_idx': [0],
  'x': 1.5, 'y': 0.0,
 },
 {
  'title': 'Рыбацкая байка',
  'desc': ['Секрет: наловите 128 рыб StarCatcher.', 'Награда говорит сама за себя...'],
  'task_item': 'starcatcher:blossomfish', 'count': 128,
  'rewards': [
      ('item', 'aquatech_ui:rate_x32', 1),
      ('item', 'ae2:item_storage_cell_256k', 2),
      ('xp', 3000),
  ],
  'x': -1.5, 'y': 0.0,
 },
 {
  'title': 'Хранитель Глубин',
  'desc': ['Финал секретной ветки: докажите, что океан вам больше не противник.',
           'Соберите полный набор трофеев бездны.'],
  'task_item': 'aquatech_ui:ocean_bounty_upgrade', 'count': 1,
  'rewards': [
      ('item', 'avaritia:compressed_chest', 1),
      ('item', 'ae2:quantum_link_chamber', 1),
      ('item', 'industrialupgrade:admpanel/admpanel', 1),
      ('xp', 10000),
  ],
  'deps_idx': [0, 1, 2],
  'x': 0.0, 'y': 2.0,
 },
]

def quest_block(q, ids):
    deps = ''
    if q.get('deps_idx'):
        dep_ids = ','.join(f'"{ids[i]}"' for i in q['deps_idx'])
        deps = f'\n\t\tdependencies: [{dep_ids}]'
    rewards = []
    for r in q['rewards']:
        rid = uid()
        if r[0] == 'item':
            count_line = f'\n\t\t\t\tcount: {r[2]}' if r[2] != 1 else ''
            rewards.append(f'{{\n\t\t\t\tid: "{rid}"\n\t\t\t\titem: "{r[1]}"{count_line}\n\t\t\t\ttype: "item"\n\t\t\t}}')
        elif r[0] == 'command':
            cmd = r[1].replace('"', '\\"')
            rewards.append(f'{{\n\t\t\t\tid: "{rid}"\n\t\t\t\tcommand: "{cmd}"\n\t\t\t\ttype: "command"\n\t\t\t}}')
        else:
            rewards.append(f'{{\n\t\t\t\tid: "{rid}"\n\t\t\t\ttype: "xp"\n\t\t\t\txp: {r[1]}\n\t\t\t}}')
    desc_lines = '\n'.join(f'\t\t\t\t"{d}"' for d in q['desc'])
    task_id = uid()
    count_line = f'\n\t\t\t\tcount: {q["count"]}L' if q['count'] != 1 else ''
    return (
        f'\t\t{{\n'
        f'\t\t\tdescription: [\n{desc_lines}\n\t\t\t]\n'
        f'{deps}\n'
        f'\t\t\tid: "{uid()}"\n'
        f'\t\t\trewards: [{",".join(rewards)}]\n'
        f'\t\t\tsubtitle: "{q.get("subtitle", "")}"\n'
        f'\t\t\ttasks: [{{\n'
        f'\t\t\t\tid: "{task_id}"{count_line}\n'
        f'\t\t\t\titem: "{q["task_item"]}"\n'
        f'\t\t\t\ttype: "item"\n'
        f'\t\t\t}}]\n'
        f'\t\t\ttitle: "{q["title"]}"\n'
        f'\t\t\tx: {q["x"]}d\n'
        f'\t\t\ty: {q["y"]}d\n'
        f'\t\t}}'
    )

ids = [uid() for _ in secret_quests]
blocks = [quest_block(q, ids) for q in secret_quests]
secret = (
 '{\n'
 '\tdefault_hide_dependency_lines: false\n'
 '\tdefault_quest_shape: ""\n'
 '\tfilename: "secret_aquatech"\n'
 '\tgroup: ""\n'
 f'\tid: "{uid()}"\n'
 '\ticon: "minecraft:heart_of_the_sea"\n'
 '\torder_index: 6\n'
 '\tquest_links: [ ]\n'
 '\tsubtitle: "Скрытая ветка для тех, кто исследует всё. Щедрые награды: ME-ячейки, солнечные панели, бесконечность."\n'
 '\ttitle: "???: Тайны Океана"\n'
 '\tquests: [\n' + ',\n'.join(blocks) + '\n\t]\n}\n'
)
open('config/ftbquests/quests/chapters/secret_aquatech.snbt', 'w', encoding='utf-8', newline='\n').write(secret)
print('secret chapter regenerated')

# ---------- ENDGAME CHAPTER ----------
endgame_quests = [
 {'title': 'Альфа-рыбак',
  'desc': ['Добудьте Alpha Rod — вершину рыбацкой прогрессии из 13 тиров.',
           'Крафт: Osmiridium Alloy Ingot + Asteroid Adamantium Ore + предыдущая удочка.'],
  'task_item': 'starcatcher:alpha_rod', 'count': 1,
  'rewards': [
      ('item', 'aquatech_ui:rate_x64', 2),
      ('item', 'ae2:item_storage_cell_256k', 4),
      ('item', 'ae2:cell_component_256k', 4),
      ('xp', 8000),
  ],
  'deps_idx': [], 'x': -2.0, 'y': 1.0},
 {'title': 'Нейтронная звезда в кармане',
  'desc': ['Принесите 5 Nether Star, пойманных удочкой в океане — не добытых на сервере.',
           'Новолуние + ночь + шторм: шанс до ~10% на Абиссальной+.'],
  'task_item': 'minecraft:nether_star', 'count': 5,
  'rewards': [
      ('item', 'avaritia:crystal_matrix_ingot', 8),
      ('item', 'ae2:fluid_storage_cell_256k', 2),
      ('xp', 8000),
  ],
  'deps_idx': [], 'x': 2.0, 'y': 1.0},
 {'title': '★ ФИНАЛ: Административная Солнечная Панель',
  'desc': ['Чем заканчивается AquaTech.',
           'Соберите Admin Solar Panel: Avaritia Extreme Table (9×9).',
           'Infinity Ingot + Singularity + Rate x64 + Alpha Rod + Photonic Solar Panel.',
           'После неё энергия перестаёт быть вопросом. Вы прошли сборку.'],
  'task_item': 'industrialupgrade:admpanel/admpanel', 'count': 1,
  'rewards': [
      ('item', 'avaritia:infinity_ingot', 8),
      ('item', 'industrialupgrade:machines/photonic_solar_panel', 1),
      ('item', 'avaritia:compressed_chest', 1),
      ('command', '@player give @s minecraft:name_tag{display:{Name:\'{"text":"★ Endgame ★","color":"gold","bold":true}\'}}'),
      ('xp', 25000),
  ],
  'deps_idx': [0, 1], 'x': 0.0, 'y': 3.0},
]
ids = [uid() for _ in endgame_quests]
blocks = [quest_block(q, ids) for q in endgame_quests]
eg = (
 '{\n'
 '\tdefault_hide_dependency_lines: false\n'
 '\tdefault_quest_shape: ""\n'
 '\tfilename: "endgame_aquatech"\n'
 '\tgroup: ""\n'
 f'\tid: "{uid()}"\n'
 '\ticon: "industrialupgrade:admpanel/admpanel"\n'
 '\torder_index: 7\n'
 '\tquest_links: [ ]\n'
 '\tsubtitle: "Финал пути. Награды: ME-хранилища, Infinity, фотонная панель и золотой префикс ★ Endgame ★."\n'
 '\ttitle: "★ Эндгейм: Финал Пути"\n'
 '\tquests: [\n' + ',\n'.join(blocks) + '\n\t]\n}\n'
)
open('config/ftbquests/quests/chapters/endgame_aquatech.snbt', 'w', encoding='utf-8', newline='\n').write(eg)
print('endgame chapter regenerated')
