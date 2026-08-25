import re, json

with open('steamTab_dump.txt', 'r', encoding='utf-8') as f:
    text = f.read()

lines_calls = re.findall(r'//\s+Method\s+com/denfop/api/guidebook/Lines\.create:\([^\)]*\)Lcom/denfop/api/guidebook/Lines;', text)
print(f"Total Lines.create in steamTab: {len(lines_calls)}")

# Let's see the instructions around Lines.create
for m in re.finditer(r'Lines\.create', text):
    start = max(0, m.start() - 300)
    end = min(len(text), m.end() + 100)
    print("--- LINES SNIPPET ---")
    print(text[start:end])
