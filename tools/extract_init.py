import re, json

with open('guide_core_verbose.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Let's find the init method bytecode
init_start = text.find('public static void init();')
if init_start != -1:
    init_end = text.find('public static com.denfop.api.guidebook.GuideTab addQuestToTab', init_start)
    if init_end == -1:
        init_end = init_start + 500000
    init_code = text[init_start:init_end]
    with open('init_code.txt', 'w', encoding='utf-8') as out:
        out.write(init_code)
    print("Extracted init_code.txt, length:", len(init_code))
else:
    print("init method not found")
