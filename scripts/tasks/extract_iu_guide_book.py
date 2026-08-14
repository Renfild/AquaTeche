#!/usr/bin/env python3
"""Dump Industrial Upgrade in-mod guidebook to Markdown (ru + en).

Source: GuideBookCore.init() quest tree + assets/industrialupgrade/lang/*.json
Output: docs/IU_GUIDE_BOOK.md
"""
from __future__ import annotations

import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "quests"))

from gen_iu_guide_ftbquests import (  # noqa: E402
    JAR,
    MANUAL_ITEMS,
    TAB_LABEL,
    build_leaf_index,
    load_models,
    parse_quests,
    pick_leaf,
    resolve_item,
    snakify,
)

JAVAP = ROOT / "scripts" / "scratch" / "_iu_GuideBookCore_javap.txt"
OUT = ROOT / "docs" / "IU_GUIDE_BOOK.md"


def strip_mc(s: str) -> str:
    return re.sub(r"§.", "", s or "").strip()


def title_for(q: dict, ru: dict, en: dict, item: str | None) -> str:
    name = q["name"]
    title = ru.get(f"iu.guide_quest_name.{name}") or en.get(f"iu.guide_quest_name.{name}")
    if title:
        return strip_mc(title)
    title = name.replace("_", " ")
    if item and item.startswith("industrialupgrade:"):
        path = item.split(":", 1)[1]
        leaf = path.split("/")[-1]
        for key in (
            f"iu.{path.replace('/', '.')}",
            f"industrialupgrade.{path.replace('/', '.')}",
            f"iu.crafting_elements.{leaf}",
            f"iu.upgrades.{leaf}",
            f"item.industrialupgrade.{leaf}",
        ):
            if key in ru:
                return strip_mc(ru[key])
            if key in en:
                return strip_mc(en[key])
    return title


def main() -> int:
    if not JAR.is_file():
        print(f"missing {JAR}", file=sys.stderr)
        return 1
    if not JAVAP.is_file():
        print(f"missing {JAVAP} — run javap on GuideBookCore first", file=sys.stderr)
        return 1

    # gen_iu_guide reads JAVAP from ROOT/_iu_... — patch for this run
    import gen_iu_guide_ftbquests as gen

    gen.JAVAP = JAVAP

    z = zipfile.ZipFile(JAR)
    models = load_models(z)
    ru = json.loads(z.read("assets/industrialupgrade/lang/ru_ru.json").decode("utf-8"))
    en = json.loads(z.read("assets/industrialupgrade/lang/en_us.json").decode("utf-8"))
    by_leaf = build_leaf_index(models)

    quests = parse_quests()
    tab_order = [
        "main",
        "primal",
        "steam",
        "baseElectric",
        "advancedElectricTab",
        "improvedElectricTab",
        "perElectric",
    ]

    rows: list[dict] = []
    for q in quests:
        item = resolve_item(q, by_leaf, models)
        desc_ru = strip_mc(
            ru.get(f"iu.guide_quest_description.{q['name']}", "")
            or en.get(f"iu.guide_quest_description.{q['name']}", "")
        )
        desc_en = strip_mc(en.get(f"iu.guide_quest_description.{q['name']}", ""))
        rows.append(
            {
                **q,
                "item": item,
                "title": title_for(q, ru, en, item),
                "desc_ru": desc_ru,
                "desc_en": desc_en,
            }
        )

    book_intro = strip_mc(ru.get("iu.book.guide_book", en.get("iu.book.guide_book", "")))

    lines: list[str] = []
    lines.append("# Industrial Upgrade — руководство из мода")
    lines.append("")
    lines.append(
        f"Источник: `{JAR.name}` · GuideBookCore · "
        f"{len(rows)} записей · язык ru_ru + en_us"
    )
    lines.append("")
    if book_intro:
        lines.append("## Книга-гайд (предмет)")
        lines.append("")
        lines.append(book_intro)
        lines.append("")

    lines.append("## Вкладки")
    lines.append("")
    for tab in tab_order:
        label = TAB_LABEL.get(tab, tab)
        tab_ru = strip_mc(ru.get(f"iu.guidetab.{tab}", en.get(f"iu.guidetab.{tab}", tab)))
        count = sum(1 for r in rows if r["tab"] == tab)
        lines.append(f"- **{label}** (`{tab}`) — {tab_ru or tab}: {count} записей")
    lines.append("")

    for tab in tab_order:
        tab_rows = [r for r in rows if r["tab"] == tab]
        if not tab_rows:
            continue
        label = TAB_LABEL.get(tab, tab)
        tab_ru = strip_mc(ru.get(f"iu.guidetab.{tab}", en.get(f"iu.guidetab.{tab}", tab)))
        lines.append(f"---")
        lines.append("")
        lines.append(f"## {label} — {tab_ru or tab}")
        lines.append("")

        for r in tab_rows:
            prev = f" ← `{r['prev']}`" if r.get("prev") else ""
            lines.append(f"### {r['title']}{prev}")
            lines.append("")
            lines.append(f"- **id:** `{r['name']}`")
            lines.append(f"- **вкладка:** `{r['tab']}`")
            if r.get("item"):
                lines.append(f"- **предмет:** `{r['item']}`")
            if r.get("icon_field"):
                lines.append(f"- **icon field:** `{r['icon_field']}`")
            lines.append("")
            if r["desc_ru"]:
                lines.append("**Описание (RU):**")
                lines.append("")
                lines.append(r["desc_ru"])
                lines.append("")
            if r["desc_en"] and r["desc_en"] != r["desc_ru"]:
                lines.append("**Description (EN):**")
                lines.append("")
                lines.append(r["desc_en"])
                lines.append("")
            if not r["desc_ru"] and not r["desc_en"]:
                lines.append("*(описание в lang отсутствует)*")
                lines.append("")

    # Extra guide.* keys not tied to quest ids
    extra_prefixes = ("guide.", "quarry.guide.")
    extras = sorted(k for k in ru if any(k.startswith(p) for p in extra_prefixes))
    if extras:
        lines.append("---")
        lines.append("")
        lines.append("## Дополнительные тексты guide.* / quarry.guide.*")
        lines.append("")
        for key in extras:
            lines.append(f"### `{key}`")
            lines.append("")
            lines.append(strip_mc(ru.get(key, "")))
            en_t = strip_mc(en.get(key, ""))
            if en_t and en_t != strip_mc(ru.get(key, "")):
                lines.append("")
                lines.append(f"*(EN)* {en_t}")
            lines.append("")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"OK {OUT} ({len(rows)} quests, {OUT.stat().st_size // 1024} KB)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
