# -*- coding: utf-8 -*-
import re
from pathlib import Path

p = Path(__file__).resolve().parents[2] / "config" / "ftbquests" / "quests" / "chapters" / "1.snbt"
text = p.read_text(encoding="utf-8")
pat = re.compile(
    r"\{\s*\n"
    r"\t\t\t\t\tcount: (\d+)\s*\n"
    r'\t\t\t\t\tid: "([^"]+)"\s*\n'
    r'\t\t\t\t\titem: "([^"]+)"\s*\n'
    r'\t\t\t\t\ttype: "item"\s*\n'
    r"\t\t\t\t\}"
)
n = 0


def repl(m: re.Match) -> str:
    global n
    n += 1
    return (
        "{\n"
        f'\t\t\t\t\tid: "{m.group(2)}"\n'
        "\t\t\t\t\titem: {\n"
        f"\t\t\t\t\t\tCount: {m.group(1)}b\n"
        f'\t\t\t\t\t\tid: "{m.group(3)}"\n'
        "\t\t\t\t\t}\n"
        '\t\t\t\t\ttype: "item"\n'
        "\t\t\t\t}"
    )


new = pat.sub(repl, text)
p.write_text(new, encoding="utf-8")
print("wrapped", n)
