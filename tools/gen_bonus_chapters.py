#!/usr/bin/env python3
"""Add descriptions to quests in 57FF chapter + create secret & endgame chapters."""
import re, uuid, glob, json

def uid(): return uuid.uuid4().hex.upper()

# ---------- 1. Descriptions for 57FF ----------
f = 'config/ftbquests/quests/chapters/57FF374744F4AC76.snbt'
txt = open(f, encoding='utf-8').read()
desc_map = {
 '5D894131F2E2420F': 'Сколотите 12 сундуков — базе нужно хранение. Крафт: 8 досок по кругу.',
 '3C6CA2FE90915F3B': 'Проведите предметы между машинами: 32 предметных кабеля Industrial Upgrade.',
 '31820C5727A1B1B1': 'Масштабируйте логистику: ещё больше кабелей высших ярусов для автоматизации.',
}
count = 0
for qid, d in desc_map.items():
    pat = f'entity_vis_size: 1.0f\n\t\t\tid: "{qid}"'
    if pat in txt:
        txt = txt.replace(pat, f'description: [\n\t\t\t\t"{d}"\n\t\t\t]\n\t\t\t' + pat)
        count += 1
open(f, 'w', encoding='utf-8', newline='\n').write(txt)
print(f'descriptions added: {count}')

# ---------- 2. Secret chapter ----------
secret_chapter = {
 'id': uid(),
 'filename': 'secret_aquatech',
 'order_index': 6,
 'title': '???: Тайны Океана',
 'icon': 'minecraft:heart_of_the_sea',
 'subtitle': 'Скрытые задания для самых любопытных. Никто не знает, что тут скрыто... пока не найдёт.',
 'quests': [
  {
   'title': 'Шёпот Бездны',
   'desc': ['Секрет: поймайте осколок эха абиссальной удочкой прямо из океана.', 'Подсказка: глубже — лучше. Ночь и новолуние увеличивают шанс.'],
   'task_item': 'minecraft:echo_shard', 'count': 1,
   'rewards': [('item', 'minecraft:heart_of_the_sea', 2), ('xp', 500)],
   'shape': 'circle',
  },
  {
   'title': 'Звезда в Сети',
   'desc': ['Секрет: поднимите Морскую Звезду (Nether Star) удочкой.', 'Только Абиссальная удочка или Альфа. Шанс мизерный — терпение.'],
   'task_item': 'minecraft:nether_star', 'count': 1,
   'rewards': [('item', 'avaritia:infinity_ingot', 1), ('xp', 2000)],
   'deps_idx': [0], 'shape': 'rsquare',
  },
  {
   'title': 'Рыбацкая байка',
   'desc': ['Секрет: наловите 256 любой рыбы StarCatcher за одну сессию.', 'Награда говорит сама за себя...'],
   'task_item': 'starcatcher:blossomfish', 'count': 64,
   'rewards': [('item', 'aquatech_ui:rate_x16', 1), ('xp', 300)],
   'deps_idx': [], 'shape': '',
  },
 ],
}

def quest_block(q, prev_ids):
    deps = ''
    if q.get('deps_idx'):
        dep_ids = ','.join(f'"{prev_ids[i]}"' for i in q['deps_idx'])
        deps = f'\n\t\tdependencies: [{dep_ids}]'
    rewards = []
    for r in q['rewards']:
        rid = uid()
        if r[0] == 'item':
            rewards.append(f'{{\n\t\t\t\tid: "{rid}"\n\t\t\t\titem: "{r[1]}"\n\t\t\t\ttype: "item"\n\t\t\t}}')
        else:
            xp = r[-1]
            rewards.append(f'{{\n\t\t\t\tid: "{rid}"\n\t\t\t\ttype: "xp"\n\t\t\t\txp: {xp}\n\t\t\t}}')
    task_id = uid()
    desc_lines = '\n'.join(f'\t\t\t\t"{d}"' for d in q['desc'])
    shape = f'\n\t\ticon_shape: "{q["shape"]}"' if q.get('shape') else ''
    count_line = f'\n\t\t\t\tcount: {q["count"]}L' if q.get('count', 1) != 1 else ''
    return (
        f'\t\t{{{shape}\n'
        f'\t\t\tdescription: [\n{desc_lines}\n\t\t\t]\n'
        f'{deps}\n'
        f'\t\t\tid: "{uid()}"\n'
        f'\t\t\trewards: [{",".join(rewards)}]\n'
        f'\t\t\ttasks: [{{\n'
        f'\t\t\t\tid: "{task_id}"{count_line}\n'
        f'\t\t\t\titem: "{q["task_item"]}"\n'
        f'\t\t\t\ttype: "item"\n'
        f'\t\t\t}}]\n'
        f'\t\t\ttitle: "{q["title"]}"\n'
        f'\t\t\tx: {q.get("x", 0.0)}d\n'
        f'\t\t\ty: {q.get("y", 0.0)}d\n'
        f'\t\t}}'
    )

# Build secret chapter file
ids_used = []
quest_txts = []
for i, q in enumerate(secret_chapter['quests']):
    ids_used.append(uid())
for i, q in enumerate(secret_chapter['quests']):
    quest_txts.append(quest_block(q, ids_used))

secret_txt = (
 '{\n'
 f'\tdefault_hide_dependency_lines: false\n'
 f'\tdefault_quest_shape: ""\n'
 f'\tfilename: "secret_aquatech"\n'
 f'\tgroup: ""\n'
 f'\tid: "{secret_chapter["id"]}"\n'
 f'\ticon: "{secret_chapter["icon"]}"\n'
 f'\torder_index: {secret_chapter["order_index"]}\n'
 f'\tquest_links: [ ]\n'
 f'\tsubtitle: "{secret_chapter["subtitle"]}"\n'
 f'\ttitle: "{secret_chapter["title"]}"\n'
 f'\tquests: [\n' + ',\n'.join(quest_txts) + '\n\t]\n'
 '}\n'
)
# hide dependency lines default; make all quests invisible until discovered is not a base FTB feature,
# but we can set the whole chapter to be hidden from the GUI until a quest in it is completed:
secret_txt = secret_txt.replace('default_quest_shape: ""', 'default_quest_shape: "circle"')
open('config/ftbquests/quests/chapters/secret_aquatech.snbt', 'w', encoding='utf-8', newline='\n').write(secret_txt)
print('secret chapter created')

# ---------- 3. Endgame chapter ----------
endgame_quests = [
 {'title': 'Финал: Бесконечная энергия',
  'desc': ['Цель сборки: скрафтите Административную Солнечную Панель (Admin Solar Panel).',
           'Путь: Avaritia Extreme Table (9×9) или ExtendedCrafting.',
           'Ключевые компоненты: Infinity Ingot, Rate x64, Alpha Rod, Photonic Solar Panel.',
           'Она выдаёт столько энергии, что сервер перестаёт её замечать. Это и есть конец пути.'],
  'task_item': 'industrialupgrade:admpanel/admpanel', 'count': 1,
  'rewards': [('item', 'avaritia:infinity_ingot', 3), ('xp', 10000)],
  'x': 0.0, 'y': 0.0},
 {'title': 'Альфа-рыбак',
  'desc': ['Добудьте Alpha Rod — вершину рыбацкой прогрессии.',
           'Для крафта нужны Osmiridium Alloy Ingot и Asteroid Adamantium Ore.',
           'Ловит всё, что движется. И то, что не движется — тоже.'],
  'task_item': 'starcatcher:alpha_rod', 'count': 1,
  'rewards': [('item', 'aquatech_ui:rate_x64', 1), ('xp', 5000)],
  'x': -2.0, 'y': 1.0},
 {'title': 'Нейтронная звезда в кармане',
  'desc': ['Принесите Nether Star, пойманную удочкой в океане.',
           'Да, это возможно. Да, это редкость ~6% на Абиссальной+.',
           'Новолуние, ночь и шторм повышают шанс.'],
  'task_item': 'minecraft:nether_star', 'count': 3,
  'rewards': [('item', 'avaritia:crystal_matrix_ingot', 4), ('xp', 3000)],
  'x': 2.0, 'y': 1.0},
]
ids_used = [uid() for _ in endgame_quests]
quest_txts = []
for i, q in enumerate(endgame_quests):
    # endgame: final depends on the two side ones
    q.setdefault('deps_idx', [])
    if i == 0:
        q['deps_idx'] = [1, 2]
    quest_txts.append(quest_block(q, ids_used))

eg_txt = (
 '{\n'
 '\tdefault_hide_dependency_lines: false\n'
 '\tdefault_quest_shape: ""\n'
 '\tfilename: "endgame_aquatech"\n'
 '\tgroup: ""\n'
 f'\tid: "{uid()}"\n'
 '\ticon: "industrialupgrade:admpanel/admpanel"\n'
 '\torder_index: 7\n'
 '\tquest_links: [ ]\n'
 '\tsubtitle: "Чем закончить сборку. Финальные вызовы для тех, кто дошёл до конца."\n'
 '\ttitle: "★ Эндгейм: Финал Пути"\n'
 '\tquests: [\n' + ',\n'.join(quest_txts) + '\n\t]\n}\n'
)
open('config/ftbquests/quests/chapters/endgame_aquatech.snbt', 'w', encoding='utf-8', newline='\n').write(eg_txt)
print('endgame chapter created')
