#!/usr/bin/env python3
"""Sanity check for config/ftbquests chapter files: balanced SNBT, unique ids, dependencies point to real quests, no cycles.

    python tools/check_ftb_chapters.py            # all chapters in config/ftbquests and the copy in server/config/ftbquests
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DIRS = [ROOT / "config" / "ftbquests" / "quests" / "chapters", ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters"]


def strip_strings(text):
    return re.sub(r'"(?:[^"\\]|\\.)*"', '""', text)


def check_dir(d):
    problems = []
    all_ids, deps = {}, []
    for f in sorted(d.glob("*.snbt")):
        raw = f.read_text(encoding="utf-8")
        t = strip_strings(raw)
        for a, b in ("{}", "[]"):
            if t.count(a) != t.count(b):
                problems.append(f"{f.name}: unbalanced {a}{b} ({t.count(a)} vs {t.count(b)})")
        text = raw.replace("\r", "")
        quest_blocks = re.findall(r"\n\t\t\{\n(.*?)\n\t\t\}", text, re.S)
        for q in quest_blocks:
            if "tasks:" not in q:
                continue  # image blocks and other non-quest entries
            qid = re.findall(r'\n\t\t\tid: "([0-9A-F]{16})"', "\n" + q)
            if not qid:
                problems.append(f"{f.name}: quest without id")
                continue
            if qid[0] in all_ids:
                problems.append(f"{f.name}: duplicate quest id {qid[0]} (also in {all_ids[qid[0]]})")
            all_ids[qid[0]] = f.name
            m = re.search(r"dependencies: \[(.*?)\]", q, re.S)
            if m:
                for dep in re.findall(r'"([0-9A-F]{16})"', m.group(1)):
                    deps.append((f.name, qid[0], dep))
    for fname, qid, dep in deps:
        if dep not in all_ids:
            problems.append(f"{fname}: quest {qid} depends on missing {dep}")
    graph = {}
    for _, qid, dep in deps:
        graph.setdefault(qid, []).append(dep)
    state = {}

    def visit(n, trail):
        if state.get(n) == 2:
            return
        if state.get(n) == 1:
            problems.append("cycle: " + " -> ".join(trail + [n]))
            return
        state[n] = 1
        for m in graph.get(n, []):
            visit(m, trail + [n])
        state[n] = 2

    for n in list(graph):
        visit(n, [])
    return problems, len(all_ids)


def main():
    bad = False
    for d in DIRS:
        problems, n = check_dir(d)
        print(f"{d.relative_to(ROOT)}: {n} quests, {len(problems)} problems")
        for p in problems:
            print("  ", p)
            bad = True
    a = sorted((DIRS[0]).glob("*.snbt"))
    for f in a:
        other = DIRS[1] / f.name
        if not other.exists() or other.read_bytes() != f.read_bytes():
            print(f"  server copy differs: {f.name}")
            bad = True
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
