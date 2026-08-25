import os

print("--- Validating SNBT Quest files in config/ftbquests and server/config/ftbquests ---")

for d in ['config/ftbquests/quests/chapters', 'server/config/ftbquests/quests/chapters']:
    if os.path.exists(d):
        for f in os.listdir(d):
            if f.endswith('.snbt'):
                p = os.path.join(d, f)
                with open(p, 'r', encoding='utf-8') as file:
                    content = file.read()
                    open_braces = content.count('{')
                    close_braces = content.count('}')
                    open_brackets = content.count('[')
                    close_brackets = content.count(']')
                    if open_braces != close_braces:
                        print(f"ERROR: Braces mismatch in {p}: {{={open_braces}, }}={close_braces}")
                    elif open_brackets != close_brackets:
                        print(f"ERROR: Brackets mismatch in {p}: [={open_brackets}, ]={close_brackets}")
                    else:
                        print(f"OK: {p}")
