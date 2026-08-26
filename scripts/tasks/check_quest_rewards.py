"""List FTB quests whose rewards block has no item type."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("config/ftbquests/quests/chapters")


def main() -> None:
    missing: list[tuple[str, str, str]] = []
    for path in sorted(ROOT.glob("*.snbt")):
        text = path.read_text(encoding="utf-8")
        match = re.search(r"quests: \[([\s\S]*)\n\t\]", text)
        if not match:
            print(path.name, "NO QUESTS")
            continue
        body = match.group(1)
        for block_m in re.finditer(r"\n\t\t\{([\s\S]*?)\n\t\t\}", body):
            block = block_m.group(0)
            qid_m = re.search(r'id: "([0-9A-Fa-f]+)"', block)
            qid = qid_m.group(1) if qid_m else "?"
            if "rewards:" not in block:
                missing.append((path.name, qid, "no rewards key"))
                continue
            rm = re.search(r"rewards: \[([\s\S]*?)\]\s*\n\t\t\t(tasks:|x:)", block)
            if not rm:
                missing.append((path.name, qid, "parse fail"))
                continue
            if 'type: "item"' not in rm.group(1):
                missing.append((path.name, qid, rm.group(1)[:80].replace("\n", " ")))
    print("quests without item reward:", len(missing))
    for row in missing:
        print(row[0], row[1], row[2])


if __name__ == "__main__":
    main()
