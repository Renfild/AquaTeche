import re, json

with open('guide_core_verbose.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Constant pool strings in javap look like:
# #123 = String             #456          // some_string
cp_strings = re.findall(r'#\d+\s+=\s+String\s+#\d+\s+//\s+(.*)', text)
print(f"Total CP Strings: {len(cp_strings)}")

# Let's search for strings containing guide or quest or tab
filtered = [s for s in cp_strings if any(w in s.lower() for w in ['guide', 'quest', 'tab', 'era', 'steam', 'electric', 'iu.'])]
print(f"Filtered CP Strings: {len(filtered)}")
with open('guide_cp_strings.txt', 'w', encoding='utf-8') as out:
    for s in filtered:
        out.write(s + '\n')

print("Saved filtered strings to guide_cp_strings.txt")
