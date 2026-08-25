import os

for f in sorted(os.listdir('config/ftbquests/quests/chapters')):
    p = os.path.join('config/ftbquests/quests/chapters', f)
    with open(p, 'r', encoding='utf-8') as file:
        content = file.read()
        titles = [l.strip() for l in content.splitlines() if l.strip().startswith('title:')]
        orders = [l.strip() for l in content.splitlines() if l.strip().startswith('order_index:')]
        q_count = content.count('tasks: [')
        root_title = titles[-1] if titles else ""
        order_str = orders[0] if orders else ""
        print(f"{f:30} -> {order_str:15} | {root_title:35} | Quests: {q_count}")
