import re, json

with open('steamTab_dump.txt', 'r', encoding='utf-8') as f:
    text = f.read()

chunks = text.split('invokestatic  #96                 // Method com/denfop/api/guidebook/Quest$Builder.create:()')

for i, chunk in enumerate(chunks[1:10]):
    # Find name
    name_m = re.search(r'//\s+String\s+([a-zA-Z0-9_]+)\n\s+\d+:\s+invokevirtual\s+#\d+\s+//\s+Method\s+com/denfop/api/guidebook/Quest\$Builder\.name:', chunk)
    qname = name_m.group(1) if name_m else "unknown"

    # Find prev (the string pushed right before prev call)
    prev_m = re.findall(r'//\s+String\s+([a-zA-Z0-9_]+)\n\s+\d+:\s+invokevirtual\s+#\d+\s+//\s+Method\s+com/denfop/api/guidebook/Quest\$Builder\.prev:', chunk)
    print(f"Quest #{i+1}: {qname:25} | prev: {prev_m}")
