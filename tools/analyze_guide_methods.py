import re, json

with open('guide_core_verbose.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Find all occurrences of GuideTab instantiation or strings
tab_names = re.findall(r'String\s+\"([^\"]+Tab[^\"]*)\"', text)
print("Tab names in constant pool / bytecode:", set(tab_names))

# Find method names in GuideBookCore
method_defs = re.findall(r'(?:public|private|static)\s+(?:static\s+)?[a-zA-Z0-9_<>,\[\]\s]+\s+([a-zA-Z0-9_]+)\([^)]*\)(?:\s+throws\s+[a-zA-Z0-9_.,\s]+)?;', text)
print("Method defs:", method_defs)
