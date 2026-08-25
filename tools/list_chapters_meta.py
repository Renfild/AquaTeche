import os

for f in sorted(os.listdir('config/ftbquests/quests/chapters')):
    p = os.path.join('config/ftbquests/quests/chapters', f)
    with open(p, 'r', encoding='utf-8') as file:
        content = file.read()
        title_line = [l.strip() for l in content.splitlines() if 'title:' in l]
        order_line = [l.strip() for l in content.splitlines() if 'order_index:' in l]
        id_line = [l.strip() for l in content.splitlines() if 'id:' in l]
        print(f"{f} -> id: {id_line[0] if id_line else ''}, order: {order_line[0] if order_line else ''}, title: {title_line[0] if title_line else ''}")
