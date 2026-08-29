"""Dump quest title, task item, reward items per chapter."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("config/ftbquests/quests/chapters")


def main() -> None:
    for path in sorted(ROOT.glob("*.snbt")):
        text = path.read_text(encoding="utf-8")
        match = re.search(r"quests: \[([\s\S]*)\n\t\]", text)
        if not match:
            continue
        body = match.group(1)
        print(f"\n=== {path.name} ===")
        for block_m in re.finditer(r"\n\t\t\{([\s\S]*?)\n\t\t\}", body):
            block = block_m.group(0)
            title_m = re.search(r'title: "([^"]*)"', block)
            title = title_m.group(1) if title_m else "(no title)"
            task_items = re.findall(r'tasks: \[[\s\S]*?item: (?:"([^"]+)"|\{[^}]*id: "([^"]+)")', block)
            tasks = [a or b for a, b in task_items]
            reward_block = re.search(r"rewards: \[([\s\S]*?)\]\s*\n\t\t\t(tasks:|x:)", block)
            rewards = []
            if reward_block:
                rewards = re.findall(
                    r'item: "([^"]+)"',
                    reward_block.group(1),
                )
                counts = re.findall(r"count: (\d+)", reward_block.group(1))
            print(f"  TASK {', '.join(tasks) or '?'}  |  REW {rewards}  |  {title}")


if __name__ == "__main__":
    main()
