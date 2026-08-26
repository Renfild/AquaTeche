# -*- coding: utf-8 -*-
"""Era-gate FTB chapters, optionalize disabled crafts, wrap bare ItemRewards."""
from __future__ import annotations

import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CFG = ROOT / "config" / "ftbquests" / "quests"
SRV = ROOT / "server" / "config" / "ftbquests" / "quests"
CH = CFG / "chapters"

sys.path.insert(0, str(ROOT / "tools" / "quests"))
from useful_rewards import wrap_string_rewards  # noqa: E402

QUEST_ID = re.compile(r'^\t\t\tid: "([0-9A-Fa-f]+)"\s*$', re.M)
DEPS = re.compile(r'^\t\t\tdependencies: \[([^\]]*)\]\s*$', re.M)
TITLE = re.compile(r'^\t\t\ttitle: "([^"]*)"', re.M)
TASK_ITEM = re.compile(
    r'type: "item"\s*\n\t\t\t\}\s*\n\t\t\ttitle:',
)
ITEM_IN_QUEST = re.compile(r'item: (?:"([^"]+)"|\{[^}]*id: "([^"]+)")')

BLOCKED_SUBSTR = (
    "quarry",
    "alkalineearth",
    "petrol_quarry",
    "research_lens",
    "research_table_space",
    "rocket_launch",
    "rocket_assembler",
    "rover_assembler",
    "probe_assembler",
    "satellite_assembler",
    "hologram_space",
    "upgrade_rover",
    "asteroidore",
    "bucket/hydrazine",
)

ERA_GATES = [
    # next_chapter_file, root_quest, previous_capstone_quest
    ("steam_era.snbt", "6F51B358741C21C5", "00AE2E0A6BA75908"),  # Ch1 anvil → steam
    ("basic_electric_era.snbt", "88FD1F77E2424BA9", "4FE087A7196F1E38"),  # blast furnace
    ("improved_electric_era.snbt", "2F3318CF641E793D", "799AE4C1FE0064F3"),  # radioactive waste
]

SECRET_RETARGET = {
    # soil purifier sits behind hydrazine/space — point secret at laser polisher (era root)
    "B969D019BBF81B8E": "2F3318CF641E793D",
}

GROUPS_SNBT = """{
	chapter_groups: [
		{
			id: "ERA0000000000001"
			title: "Эпохи"
		}
		{
			id: "MOD0000000000001"
			title: "Моды"
		}
		{
			id: "END0000000000001"
			title: "Финал"
		}
	]
}
"""

CHAPTER_GROUP = {
    "7D2835D587AABDAB.snbt": "ERA0000000000001",
    "1.snbt": "ERA0000000000001",
    "steam_era.snbt": "ERA0000000000001",
    "basic_electric_era.snbt": "ERA0000000000001",
    "improved_electric_era.snbt": "ERA0000000000001",
    "57FF374744F4AC76.snbt": "ERA0000000000001",
    "botania_aquatech.snbt": "MOD0000000000001",
    "alexscaves_aquatech.snbt": "MOD0000000000001",
    "ae2_aquatech.snbt": "MOD0000000000001",
    "avaritia_aquatech.snbt": "MOD0000000000001",
    "secret_aquatech.snbt": "END0000000000001",
    "endgame_aquatech.snbt": "END0000000000001",
}


def split_quests(text: str) -> list[tuple[int, int, str]]:
    """Return (start, end, block) for each top-level quest object."""
    lines = text.splitlines(keepends=True)
    blocks = []
    i = 0
    while i < len(lines):
        if lines[i] == "\t\t{\n" or lines[i] == "\t\t{":
            start = i
            depth = 0
            j = i
            while j < len(lines):
                depth += lines[j].count("{") - lines[j].count("}")
                j += 1
                if depth == 0:
                    blocks.append((start, j, "".join(lines[start:j])))
                    i = j
                    break
            else:
                break
        else:
            i += 1
    return blocks


def quest_id(block: str) -> str | None:
    m = QUEST_ID.search(block)
    return m.group(1) if m else None


def quest_deps(block: str) -> list[str]:
    m = DEPS.search(block)
    if not m:
        return []
    return re.findall(r'"([0-9A-Fa-f]+)"', m.group(1))


def task_items(block: str) -> list[str]:
    ids = []
    for m in ITEM_IN_QUEST.finditer(block):
        ids.append(m.group(1) or m.group(2))
    return ids


def is_blocked_item(item: str) -> bool:
    low = item.lower()
    return any(s in low for s in BLOCKED_SUBSTR)


def set_deps(block: str, deps: list[str]) -> str:
    payload = ", ".join(f'"{d}"' for d in deps)
    line = f'\t\t\tdependencies: [{payload}]\n'
    if DEPS.search(block):
        return DEPS.sub(line, block, count=1)
    # insert after opening brace
    return re.sub(r"^(\t\t\{\n)", r"\1" + line, block, count=1)


def ensure_optional(block: str) -> str:
    if re.search(r"^\t\t\toptional: true\s*$", block, re.M):
        return block
    return re.sub(r"^(\t\t\{\n)", r"\1\t\t\toptional: true\n", block, count=1)


def set_group(text: str, gid: str) -> str:
    if re.search(r'^\tgroup: "', text, re.M):
        return re.sub(r'^\tgroup: ".*"\s*$', f'\tgroup: "{gid}"', text, count=1, flags=re.M)
    return re.sub(r"^(\{\n)", r'\1\tgroup: "' + gid + '"\n', text, count=1)


def rewrite_chapter(path: Path, blocked_ids: set[str], id_to_deps: dict[str, list[str]]) -> int:
    text = path.read_text(encoding="utf-8")
    blocks = split_quests(text)
    if not blocks:
        return 0
    changed = 0
    pieces = []
    cursor = 0
    raw = path.read_text(encoding="utf-8")
    lines = raw.splitlines(keepends=True)
    # rebuild from line ranges
    out = []
    last = 0
    line_blocks = split_quests(raw)
    for start, end, block in line_blocks:
        out.extend(lines[last:start])
        qid = quest_id(block)
        new = block
        if qid and qid in blocked_ids:
            new = ensure_optional(new)
        deps = quest_deps(new)
        rewritten = []
        for d in deps:
            if d in SECRET_RETARGET:
                d = SECRET_RETARGET[d]
            if d in blocked_ids:
                # walk to first non-blocked ancestor
                seen = set()
                cur = d
                while cur in blocked_ids and cur not in seen:
                    seen.add(cur)
                    parents = id_to_deps.get(cur, [])
                    cur = parents[0] if parents else ""
                    if not cur:
                        break
                if cur and cur not in blocked_ids:
                    d = cur
                else:
                    continue
            if d and d not in rewritten:
                rewritten.append(d)
        if rewritten != deps:
            new = set_deps(new, rewritten)
        if new != block:
            changed += 1
        out.append(new if new.endswith("\n") or new.endswith("\r\n") else new + "\n")
        last = end
    out.extend(lines[last:])
    new_text = "".join(out)
    if path.name in CHAPTER_GROUP:
        new_text = set_group(new_text, CHAPTER_GROUP[path.name])
    if new_text != raw:
        path.write_text(new_text, encoding="utf-8")
    return changed


def add_era_gate(path: Path, root_id: str, cap_id: str) -> bool:
    text = path.read_text(encoding="utf-8")
    line_blocks = split_quests(text)
    lines = text.splitlines(keepends=True)
    out = []
    last = 0
    hit = False
    for start, end, block in line_blocks:
        out.extend(lines[last:start])
        qid = quest_id(block)
        new = block
        if qid == root_id:
            deps = quest_deps(block)
            if cap_id not in deps:
                deps = [cap_id] + deps
                new = set_deps(block, deps)
                hit = True
        out.append(new if new.endswith("\n") else new + "\n")
        last = end
    out.extend(lines[last:])
    if hit:
        path.write_text("".join(out), encoding="utf-8")
    return hit


def retarget_secrets(path: Path) -> int:
    text = path.read_text(encoding="utf-8")
    orig = text
    for old, new in SECRET_RETARGET.items():
        text = text.replace(f'"{old}"', f'"{new}"')
    if text != orig:
        path.write_text(text, encoding="utf-8")
        return 1
    return 0


def index_all() -> tuple[dict[str, list[str]], set[str], dict[str, str]]:
    id_to_deps: dict[str, list[str]] = {}
    blocked: set[str] = set()
    id_to_title: dict[str, str] = {}
    for path in sorted(CH.glob("*.snbt")):
        text = path.read_text(encoding="utf-8")
        for _, _, block in split_quests(text):
            qid = quest_id(block)
            if not qid:
                continue
            id_to_deps[qid] = quest_deps(block)
            tm = TITLE.search(block)
            if tm:
                id_to_title[qid] = tm.group(1)
            items = task_items(block)
            # task items only: last item: before type item in tasks section is noisy
            # heuristic: blocked if ANY task item matches
            if any(is_blocked_item(it) for it in items):
                # skip reward-only matches by requiring tasks: nearby — items includes rewards.
                # Fine-grained: look in tasks block only
                tm_sec = re.search(r"tasks: \[([\s\S]*?)\]\s*\n\t\t\t(?:title|x|y|rewards)", block)
                if not tm_sec:
                    tm_sec = re.search(r"tasks: \[([\s\S]*?)\]\s*\n\t\t\}", block)
                task_blob = tm_sec.group(1) if tm_sec else ""
                task_ids = [a or b for a, b in ITEM_IN_QUEST.findall(task_blob)] if task_blob else items
                if any(is_blocked_item(it) for it in task_ids):
                    blocked.add(qid)
    return id_to_deps, blocked, id_to_title


def copy_to_server() -> None:
    src = CFG
    dst = SRV
    if not dst.parent.exists():
        return
    for p in src.rglob("*"):
        rel = p.relative_to(src)
        target = dst / rel
        if p.is_dir():
            target.mkdir(parents=True, exist_ok=True)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(p, target)


def main() -> int:
    id_to_deps, blocked, titles = index_all()
    print(f"blocked quests: {len(blocked)}")
    for qid in sorted(blocked):
        print(f"  {qid}  {titles.get(qid, '?')}")

    n = 0
    for path in sorted(CH.glob("*.snbt")):
        n += rewrite_chapter(path, blocked, id_to_deps)
        print(f"rewrote {path.name}")

    for fname, root, cap in ERA_GATES:
        p = CH / fname
        ok = add_era_gate(p, root, cap)
        print(f"era gate {fname}: {root} <- {cap}  {'ok' if ok else 'MISS'}")

    secrets = CH / "secret_aquatech.snbt"
    if secrets.exists():
        print("secret retarget", retarget_secrets(secrets))

    wrapped = 0
    for path in sorted(CH.glob("*.snbt")):
        w = wrap_string_rewards(path)
        if w:
            print(f"wrap {path.name}: {w}")
            wrapped += w

    (CFG / "chapter_groups.snbt").write_text(GROUPS_SNBT, encoding="utf-8")
    copy_to_server()
    print(f"quest blocks changed: {n}, wrapped rewards: {wrapped}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
