from pathlib import Path
import re

HINT = "Добывай через рыбалку AquaTech (удочки пака) — кирка/жилы не нужны."
OIL_HINT = "Нефть: вёдра crude с удочки Diamond+ (станок-качалка отключён). Позже — wireless oil pump."

PATHS = [
    Path(r"C:\Users\xieto\Desktop\AquaTech\config\ftbquests\quests\chapters\2F_ws_industrial_upgrade.snbt"),
    Path(r"C:\Users\xieto\Desktop\AquaTech\server\config\ftbquests\quests\chapters\2F_ws_industrial_upgrade.snbt"),
]

MARKERS = {
    "industrialupgrade:jar_bee/bees": HINT,
    "industrialupgrade:sapling/rubber_sapling": HINT,
    "industrialupgrade:nitrate_mud/nitrate_mud": HINT,
    "industrialupgrade:veinoil/oil": OIL_HINT,
    "industrialupgrade:raw_latex": HINT,
    "industrialupgrade:bucket/gas": HINT,
    "industrialupgrade:mineral/crystal": HINT,
}


def patch(text: str) -> str:
    for icon, hint in MARKERS.items():
        pattern = re.compile(
            r'(icon:\s*"' + re.escape(icon) + r'"[\s\S]*?subtitle:\s*")([^"]*)(")',
            re.M,
        )

        def repl(m, hint=hint):
            old = m.group(2)
            low = old.lower()
            if "aquatech" in low or "удочк" in low or "рыбалк" in low:
                return m.group(0)
            if old.strip():
                new = old.rstrip(".") + ". " + hint
            else:
                new = hint
            return m.group(1) + new + m.group(3)

        text, n = pattern.subn(repl, text)
        if n == 0:
            # description-only quests: prepend a description line if icon present and no subtitle
            icon_pat = re.compile(
                r'(icon:\s*"' + re.escape(icon) + r'"\n)(?!\s*subtitle:)',
            )
            text, _ = icon_pat.subn(
                r'\1\t\tsubtitle: "' + hint.replace("\\", "\\\\") + '"\n',
                text,
                count=2,
            )
    return text


for p in PATHS:
    if not p.exists():
        print("missing", p)
        continue
    original = p.read_text(encoding="utf-8")
    updated = patch(original)
    if updated != original:
        p.write_text(updated, encoding="utf-8")
        print("patched", p)
    else:
        print("no change", p)
