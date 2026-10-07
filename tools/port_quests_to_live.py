"""Port quest texts and new cross-chapter dependencies from a freshly generated chapter into the live (in-game edited) one,
matching quests by title. Ids, positions, icons and the other dependencies of the live chapter stay as they are.

The game re-rolls ids when it re-saves a chapter, so running the generators and uploading their output would orphan the
live ids. Pull the live chapter from Apex first, then:

    python tools/port_quests_to_live.py <generated.snbt> <live.snbt> <out.snbt> ["Quest title" ...]

With titles, only those quests are patched. A quest whose live description was deleted is skipped.
"""
import io
import re
import sys

new_path, live_path, out_path = sys.argv[1:4]
ONLY = set(sys.argv[4:])
import glob
from pathlib import Path
ROOT = Path(__file__).resolve().parent.parent
UNIVERSE = set()
for f in glob.glob(str(ROOT / "config/ftbquests/quests/chapters/*.snbt")) + [live_path]:
    UNIVERSE |= set(re.findall(r'id: "([0-9A-F]{16})"', io.open(f, encoding="utf-8").read()))

LOG = io.open(out_path + ".log", "w", encoding="utf-8")
new = io.open(new_path, encoding="utf-8", newline="").read().replace("\r\n", "\n")
live = io.open(live_path, encoding="utf-8", newline="").read().replace("\r\n", "\n")

BLOCK = re.compile(r"\n\t\t\{\n(.*?)\n\t\t\}", re.S)
TITLE = re.compile(r'\n\t\t\ttitle: "([^"]*)"')
DESC = re.compile(r"\n\t\t\tdescription: \[\n.*?\n\t\t\t\]", re.S)
DEPS = re.compile(r"\n\t\t\tdependencies: (\[[^\]]*\])", re.S)
ID = re.compile(r'\n\t\t\tid: "([0-9A-F]{16})"')
HEX = re.compile(r'"([0-9A-F]{16})"')


def blocks(s):
    out = {}
    for m in BLOCK.finditer(s):
        body = m.group(1)
        t = TITLE.search("\n" + body)
        if not t:
            continue
        assert t.group(1) not in out, "duplicate title " + t.group(1)
        out[t.group(1)] = body
    return out


nb, lb = blocks(new), blocks(live)
assert set(nb) == set(lb), (set(nb) ^ set(lb))
own_new = {ID.search("\n" + b).group(1) for b in nb.values()}
changed = 0
result = live
for title, lbody in lb.items():
    nbody = nb[title]
    body = "\n" + lbody
    nd, ld = DESC.search("\n" + nbody), DESC.search(body)
    if ONLY and title not in ONLY:
        continue
    if not (nd and ld):
        LOG.write("SKIPPED (no description) " + title + chr(10))
        continue
    updated = body.replace(ld.group(0), nd.group(0))
    new_deps = DEPS.search("\n" + nbody)
    ext = [d for d in (HEX.findall(new_deps.group(1)) if new_deps else []) if d not in own_new and d in UNIVERSE]
    ldeps = DEPS.search(updated)
    have = HEX.findall(ldeps.group(1)) if ldeps else []
    missing = [d for d in ext if d not in have]
    if missing:
        allids = sorted(have + missing)
        if len(allids) == 1:
            text = '\n\t\t\tdependencies: ["%s"]' % allids[0]
        else:
            text = "\n\t\t\tdependencies: [\n" + "\n".join('\t\t\t\t"%s"' % d for d in allids) + "\n\t\t\t]"
        if ldeps:
            updated = updated.replace(ldeps.group(0), text)
        else:
            updated = updated.replace("\n\t\t\tdescription: [", text + "\n\t\t\tdescription: [", 1)
    if updated != body:
        changed += 1
        result = result.replace(lbody, updated[1:], 1)
        LOG.write(title + (" +deps" if missing else "") + chr(10))
io.open(out_path, "w", encoding="utf-8", newline="").write(result.replace("\n", "\r\n") if "\r\n" in io.open(new_path, encoding="utf-8", newline="").read() else result)
print("changed quests:", changed)
