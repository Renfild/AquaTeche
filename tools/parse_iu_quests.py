import re, json

with open('guide_core_decomp.txt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Total lines in guide_core_decomp.txt: {len(lines)}")

# Let's inspect where Quest.builder or GuideTab or addQuestToTab are called
quest_blocks = []
current_block = []

for line in lines:
    current_block.append(line)
    if 'addQuestToTab' in line or 'addTab' in line:
        quest_blocks.append(''.join(current_block))
        current_block = []

print(f"Total quest/tab blocks: {len(quest_blocks)}")
with open('quest_blocks_sample.txt', 'w', encoding='utf-8') as out:
    for b in quest_blocks[:10]:
        out.write("=== BLOCK ===\n" + b + "\n")
