import re, json

with open('steamTab_dump.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Find all occurrences of prev
prev_matches = re.findall(r'ldc(?:_w)?\s+#\d+\s+//\s+String\s+([a-zA-Z0-9_]+)\n.*?invokevirtual\s+#\d+\s+//\s+Method\s+com/denfop/api/guidebook/Quest\$Builder\.prev:\(Ljava/lang/String;\)', text, re.DOTALL)
print(f"Total prev calls in steamTab: {len(prev_matches)}")
for p in prev_matches:
    print("  prev:", p)
