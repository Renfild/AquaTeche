from pathlib import Path

p = Path(__file__).resolve().parent / "generate_600_ocean_quests.py"
t = p.read_text(encoding="utf-8")
for old, new in [("act_i", "ACT_I"), ("act_ii", "ACT_II"),
                 ("act_iii", "ACT_III"), ("act_iv", "ACT_IV")]:
    t = t.replace(f', "{old}",\n', f", {new},\n")
p.write_text(t, encoding="utf-8")
print("remaining literal act refs:", t.count('"act_'))
