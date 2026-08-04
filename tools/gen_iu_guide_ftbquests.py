# -*- coding: utf-8 -*-
"""
Build FTB Quests chapter from Industrial Upgrade in-mod guidebook.

Source of truth: GuideBookCore.init() bytecode + ru_ru.json titles.
No rewards. Every industrialupgrade: item ID validated against jar models.
"""
from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech")
JAR = ROOT / "server" / "mods" / "IndustrialUpgrade-1.20.1-3.4.0.11.jar"
JAVAP = ROOT / "_iu_GuideBookCore_javap.txt"
OUT_CHAPTER = ROOT / "config" / "ftbquests" / "quests" / "chapters" / "2F_ws_industrial_upgrade.snbt"
OUT_SERVER = ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters" / "2F_ws_industrial_upgrade.snbt"
DEBUG = ROOT / "_iu_guide_quests.json"

CHAPTER_ID = "WS2F0000CH"
GROUP_ID = "0AC7A00000000005"
FILENAME = "2F_ws_industrial_upgrade"

# Verified nested IDs (must exist in models unless minecraft:)
# Cross-checked against GuideBookCore itemStack/icon + jar models/item/*
MANUAL_ITEMS: dict[str, str] = {
    "start": "industrialupgrade:book/guide_book",
    "anvil": "industrialupgrade:block_anvil/block_anvil",
    "forge_hammer": "industrialupgrade:forge_hammer",
    "casings": "industrialupgrade:casing/copper",
    "smelterystart": "industrialupgrade:smeltery/smeltery_controller",
    "smelteryforms": "industrialupgrade:smeltery/smeltery_casting",
    "electrum": "industrialupgrade:itemingots/electrum_ingot",
    "squeezer": "industrialupgrade:primal_fluid_integrator/primal_fluid_integrator",
    "dryer": "industrialupgrade:dryer/dryer",
    "raw_latex": "industrialupgrade:raw_latex",
    "latex": "industrialupgrade:crafting_elements/crafting_290_element",
    "primal_heater": "industrialupgrade:primal_fluid_heater/primal_fluid_heater",
    "steam": "industrialupgrade:bucket/steam",
    "superheated_steam": "industrialupgrade:bucket/superheated_steam",
    "ferromanganese": "industrialupgrade:alloyingot/ferromanganese",
    "strong_anvil": "industrialupgrade:block_strong_anvil/block_strong_anvil",
    "electronic_circuit": "industrialupgrade:crafting_elements/crafting_272_element",
    "advanced_circuit": "industrialupgrade:crafting_elements/crafting_273_element",
    "elemotor": "industrialupgrade:crafting_elements/crafting_276_element",
    "machine_casing": "industrialupgrade:crafting_elements/crafting_137_element",
    "base_machines": "industrialupgrade:blockresource/machine",
    "reBattery": "industrialupgrade:battery/re_battery",
    "plastic_plate": "industrialupgrade:plastic_plate",
    "plast": "industrialupgrade:plast",
    # iudust meta 60 = silicon (grinding flint path in guide)
    "flint_dust": "industrialupgrade:itemdust/silicon_dust",
    "alloy_coal_dust": "industrialupgrade:crafting_elements/crafting_499_element",
    "impurity_coal_dust": "industrialupgrade:crafting_elements/crafting_498_element",
    "crushed_uranium_ore": "industrialupgrade:crushed/uranium",
    "circuit_board": "industrialupgrade:crafting_elements/crafting_487_element",
    "programmed_circuit_board": "industrialupgrade:crafting_elements/crafting_488_element",
    "overclockerUpgrade": "industrialupgrade:upgrades/overclockerupgrade1",
    "coolupgrade": "industrialupgrade:itemcoolupgrade/azote",
    "transformerUpgrade": "industrialupgrade:upgrades/transformerupgrade1",
    "graviTool": "industrialupgrade:gravitool/gravitool",
    "pipette": "industrialupgrade:pipette",
    "recipe_schedule": "industrialupgrade:recipe_schedule",
    "pollution": "industrialupgrade:pollution_device",
    "energy": "industrialupgrade:ef/reader",
    "bee": "industrialupgrade:jar_bee/bees",
    "crop": "industrialupgrade:crops/crops",
    "rubber_tree": "industrialupgrade:sapling/rubber_sapling",
    "heat": "industrialupgrade:basemachine3/cooling",
    "radiation": "industrialupgrade:crafting_elements/crafting_40_element",
    "reactor_simulate": "industrialupgrade:basemachine3/simulation_reactors",
    "reactor_logic": "industrialupgrade:reactors/quad_mox_fuel_rod",
    "space": "industrialupgrade:basemachine3/research_table_space",
    "space_worlds": "industrialupgrade:space/planetary_translocator",
    "colony": "industrialupgrade:colonial_building/low_house",
    "vein": "industrialupgrade:basaltheavyore/galena",
    "volcano": "industrialupgrade:blockbasalts/basalt",
    "gasvein": "industrialupgrade:bucket/gas",
    "mineralvein": "industrialupgrade:mineral/crystal",
    "oil_vein": "industrialupgrade:veinoil/oil",
    "storage_system": "industrialupgrade:storagesystem/controller",
    "energies": "industrialupgrade:imp_se_gen/imp_se_gen",
    "other_features": "industrialupgrade:nitrate_mud/nitrate_mud",
    "villager": "minecraft:villager_spawn_egg",
    "dosimeter": "industrialupgrade:crafting_elements/crafting_40_element",
    "pellets": "industrialupgrade:nuclearresource/uranium_pellet",
    "cokeoven": "industrialupgrade:cokeoven/coke_oven_main",
    "oilgetter": "industrialupgrade:refiner/refiner",
    "oilquarry": "industrialupgrade:petrol_quarry/petrol_quarry",
    "oiladvrefiner": "industrialupgrade:adv_refiner/adv_refiner",
    "liqued_heater": "industrialupgrade:basemachine3/fluid_heat",
    "primal_rolling": "industrialupgrade:basemachine3/rolling_machine",
    "silicon_handler": "industrialupgrade:itemdust/silicon_dioxide_dust",
    "steam_polisher": "industrialupgrade:basemachine3/steam_sharpener",
    "steam_machine_block": "industrialupgrade:blockresource/steam_machine",
    "fluidcoppersulfate": "industrialupgrade:bucket/coppersulfate",
    "calcium_carbide": "industrialupgrade:crafting_elements/crafting_280_element",
    "advanced_hull_machine": "industrialupgrade:blockresource/advanced_machine",
    "perfect_hull_plating": "industrialupgrade:crafting_elements/crafting_138_element",
    "photon_hull_plate": "industrialupgrade:crafting_elements/crafting_139_element",
    "entitymodules": "industrialupgrade:entitymodules/module_mob",
    "fluid_item_pipe": "industrialupgrade:wiring/pipes",
    "fluid_reactor": "industrialupgrade:water_reactors/water_reactor",
    "gas_reactor": "industrialupgrade:gas_reactor/gas_controller",
    "generator_fluid_matter": "industrialupgrade:simplemachine/generator_matter",
    "crop_stake": "industrialupgrade:crop/crop",
    "pollution_scanner": "industrialupgrade:pollution_device",
    "pressure_space_sensor": "industrialupgrade:spaceupgrademodules/space_upgrademodule3",
    "se_sensor": "industrialupgrade:crafting_elements/crafting_79_element",
    "solid_refrigerator": "industrialupgrade:basemachine3/solid_cooling",
    "radioactive_waste": "industrialupgrade:crafting_elements/crafting_443_element",
    "scrapBox": "industrialupgrade:crafting_elements/crafting_288_element",
    "quantum_plasma": "industrialupgrade:crafting_elements/crafting_646_element",
    "research_lens_2": "industrialupgrade:research_lens/lens_2",
    "research_lens_3": "industrialupgrade:research_lens/lens_3",
    "research_lens_4": "industrialupgrade:research_lens/lens_4",
    "research_lens_5": "industrialupgrade:research_lens/lens_5",
    "research_lens_6": "industrialupgrade:research_lens/lens_6",
}

TAB_LABEL = {
    "main": "Обзор",
    "primal": "Примитив",
    "steam": "Пар",
    "baseElectric": "Электрика",
    "advancedElectricTab": "Продвинутая",
    "improvedElectricTab": "Улучшенная",
    "perElectric": "Совершенная",
}


def load_models(z: zipfile.ZipFile) -> set[str]:
    out = set()
    for n in z.namelist():
        if n.startswith("assets/industrialupgrade/models/item/") and n.endswith(".json"):
            rel = n[len("assets/industrialupgrade/models/item/") : -5].replace("\\", "/")
            out.add("industrialupgrade:" + rel)
    return out


def snakify(name: str) -> str:
    s = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", name)
    return s.replace("__", "_").lower()


def build_leaf_index(models: set[str]) -> dict[str, list[str]]:
    idx: dict[str, list[str]] = {}
    for m in models:
        idx.setdefault(m.split("/")[-1], []).append(m)
    return idx


PREFER = (
    "basemachine3/",
    "simplemachine/",
    "moremachine",
    "blockresource/",
    "wiring_storage/",
    "cable/",
    "battery/",
    "circuit/",
    "crafting_elements/",
    "crafting/",
    "itemingots/",
    "itemplates/",
    "casing/",
    "tools/",
    "book/",
    "blastfurnace/",
    "machines/",
    "smeltery/",
    "cokeoven/",
    "resource/",
    "primal_",
    "bucket/",
    "upgrades/",
    "nuclearresource/",
    "colonial_building/",
    "space/",
    "jar_bee/",
    "crops/",
    "sapling/",
    "iudust/",
    "crushed/",
    "pipes/",
)


def pick_leaf(leaf: str, by_leaf: dict[str, list[str]]) -> str | None:
    cands = by_leaf.get(leaf, [])
    if not cands:
        for alt in (leaf + "_iu", leaf.replace("_iu", ""), leaf.lower()):
            if alt in by_leaf:
                cands = by_leaf[alt]
                break
    if not cands:
        return None
    if len(cands) == 1:
        return cands[0]
    for p in PREFER:
        for c in cands:
            if p in c:
                return c
    return sorted(cands, key=len)[0]


def parse_quests() -> list[dict]:
    text = JAVAP.read_text(encoding="utf-16")
    m = re.search(r"public static void init\(\);([\s\S]*?)\n  public ", text)
    body = m.group(1) if m else text

    # Split into quest blocks by Builder.create ... build
    blocks = re.split(r"invokestatic\s+#\d+\s+// Method com/denfop/api/guidebook/Quest\$Builder\.create:", body)
    quests: list[dict] = []
    current_tab = "main"
    tab_x = 0

    # Detect tab creates in original body order via scanning
    # We'll process line-by-line for tabs + blocks

    lines = body.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        if 'GuideTab."<init>"' in line:
            # look back for ldc String
            for j in range(i, max(-1, i - 15), -1):
                sm = re.search(r"// String (\w+)$", lines[j])
                if sm:
                    current_tab = sm.group(1)
                    break
        if "Quest$Builder.create:" in line:
            # gather until build
            chunk = []
            i += 1
            while i < len(lines) and "Quest$Builder.build:" not in lines[i]:
                chunk.append(lines[i])
                i += 1
            chunk_text = "\n".join(chunk)

            name_m = re.search(
                r"// String (\w+)\s*\n\s*\d+:\s*invokevirtual\s+#\d+\s+// Method com/denfop/api/guidebook/Quest\$Builder\.name:",
                chunk_text,
            )
            if not name_m:
                # fallback: first String after create
                sm = re.search(r"// String (\w+)", chunk_text)
                name = sm.group(1) if sm else None
            else:
                name = name_m.group(1)

            prev_m = re.search(
                r"// String (\w+)\s*\n\s*\d+:\s*invokevirtual\s+#\d+\s+// Method com/denfop/api/guidebook/Quest\$Builder\.prev:",
                chunk_text,
            )
            prev = prev_m.group(1) if prev_m else None

            # icon field: last getstatic Field before .icon:
            icon_field = None
            icon_parts = chunk_text.split("Quest$Builder.icon:")
            if len(icon_parts) > 1:
                before = icon_parts[0]
                fields = re.findall(r"Field com/denfop/[\w$/]+\.(\w+):", before)
                # filter Shape/GuideTab noise
                fields = [f for f in fields if f not in ("DEFAULT", "EPIC", "HEXAGON", "instance")]
                if fields:
                    icon_field = fields[-1]

            # position
            pos = None
            pos_m = re.search(
                r"(?:bipush|sipush|iconst_m1|iconst_\d)[\s\S]{0,80}?Quest\$Builder\.position:",
                chunk_text,
            )
            # extract last two ints before position
            before_pos = chunk_text.split("Quest$Builder.position:")[0] if "Quest$Builder.position:" in chunk_text else ""
            ints = []
            for im in re.finditer(r"(?:bipush|sipush)\s+(-?\d+)|iconst_(\d)|iconst_m1", before_pos):
                if im.group(0) == "iconst_m1":
                    ints.append(-1)
                elif im.group(1):
                    ints.append(int(im.group(1)))
                else:
                    ints.append(int(im.group(2)))
            x = y = 0
            if len(ints) >= 2:
                x, y = ints[-2], ints[-1]

            has_loc = "localizationItem:" in chunk_text

            if name:
                quests.append(
                    {
                        "name": name,
                        "prev": prev,
                        "tab": current_tab,
                        "icon_field": icon_field,
                        "x": x,
                        "y": y,
                        "localization": has_loc,
                    }
                )
            continue
        i += 1

    # dedupe
    seen = set()
    out = []
    for q in quests:
        if q["name"] in seen:
            continue
        seen.add(q["name"])
        out.append(q)
    return out


def resolve_item(q: dict, by_leaf: dict[str, list[str]], models: set[str]) -> str | None:
    name = q["name"]
    if name in MANUAL_ITEMS:
        return MANUAL_ITEMS[name]

    # try quest name as leaf
    item = pick_leaf(name, by_leaf)
    if item:
        return item

    # try icon field
    f = q.get("icon_field")
    if f:
        for leaf in (f, snakify(f), f.lower()):
            item = pick_leaf(leaf, by_leaf)
            if item:
                return item
            # common aliases
            aliases = {
                "book": "guide_book",
                "ForgeHammer": "forge_hammer",
                "efReader": "ef_reader",
                "pollutionDevice": "pollution_device",
                "jarBees": "bees",
                "rubberSapling": "rubber_sapling",
                "block_anvil": "block_anvil",
                "block_strong_anvil": "block_strong_anvil",
                "reBattery": "re_battery",
                "overclockerUpgrade": "overclockerupgrade",
            }
            if f in aliases:
                item = pick_leaf(aliases[f], by_leaf)
                if item:
                    return item

    return None


def escape_snbt(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def wrap_desc(text: str, width: int = 55) -> list[str]:
    if not text:
        return []
    # strip formatting codes lightly; keep short
    text = text.replace("\\n", " ").replace("\n", " ")
    words = text.split()
    lines: list[str] = []
    cur: list[str] = []
    n = 0
    for w in words:
        if n + len(w) + 1 > width and cur:
            lines.append(" ".join(cur))
            cur = [w]
            n = len(w)
        else:
            cur.append(w)
            n += len(w) + 1
    if cur:
        lines.append(" ".join(cur))
    return lines[:8]  # hard cap packet size


def qid(idx: int) -> str:
    return f"WS2F{idx:04d}Q"


def tid(idx: int) -> str:
    return f"WS2F{idx:04d}T"


def main() -> None:
    z = zipfile.ZipFile(JAR)
    models = load_models(z)
    ru = json.loads(z.read("assets/industrialupgrade/lang/ru_ru.json").decode("utf-8"))
    en = json.loads(z.read("assets/industrialupgrade/lang/en_us.json").decode("utf-8"))

    # Fix MANUAL against models; refuse silent wrong remaps for critical entries
    by_leaf = build_leaf_index(models)
    manual_fixed = []
    for k, v in list(MANUAL_ITEMS.items()):
        if v.startswith("minecraft:"):
            continue
        if v not in models:
            leaf = v.split("/")[-1]
            alt = pick_leaf(leaf, by_leaf)
            if alt:
                MANUAL_ITEMS[k] = alt
                manual_fixed.append((k, v, alt))
            else:
                hits = [m for m in models if leaf in m.split("/")[-1]]
                if len(hits) == 1:
                    MANUAL_ITEMS[k] = hits[0]
                    manual_fixed.append((k, v, hits[0]))
                else:
                    print("MANUAL MISSING", k, v, "hits", hits[:5])
    if manual_fixed:
        print("manual remaps", len(manual_fixed))
        for row in manual_fixed[:20]:
            print(" ", row)

    quests = parse_quests()
    enriched = []
    bad = []
    for q in quests:
        item = resolve_item(q, by_leaf, models)
        title = ru.get(
            f"iu.guide_quest_name.{q['name']}",
            en.get(f"iu.guide_quest_name.{q['name']}"),
        )
        if not title:
            title = q["name"].replace("_", " ")
        desc = ru.get(
            f"iu.guide_quest_description.{q['name']}",
            en.get(f"iu.guide_quest_description.{q['name']}", ""),
        )
        # Better title from item lang when no guide_quest_name
        if title == q["name"].replace("_", " ") and item and item.startswith("industrialupgrade:"):
            path = item.split(":", 1)[1]
            leaf = path.split("/")[-1]
            for key in (
                f"iu.{path.replace('/', '.')}",
                f"industrialupgrade.{path.replace('/', '.')}",
                f"iu.crafting_elements.{leaf}",
                f"iu.upgrades.{leaf}",
                f"iu.dust.{leaf.replace('_dust', '')}",
                f"item.industrialupgrade.{leaf}",
            ):
                if key in ru:
                    title = ru[key]
                    break
                if key in en:
                    title = en[key]
                    break
        title = re.sub(r"§.", "", title)
        row = {**q, "item": item, "title_ru": title, "desc_ru": desc}
        enriched.append(row)
        if item is None or (item.startswith("industrialupgrade:") and item not in models):
            bad.append(row)

    # Second pass: fuzzy for remaining bad
    for row in bad:
        name = row["name"]
        hits = [m for m in models if name.replace("_", "") in m.replace("_", "").replace("/", "")]
        if not hits:
            hits = [m for m in models if name in m]
        if hits:
            best = None
            for p in PREFER:
                for h in hits:
                    if p in h:
                        best = h
                        break
                if best:
                    break
            chosen = best or sorted(hits, key=len)[0]
            row["item"] = chosen
            for q in enriched:
                if q["name"] == name:
                    q["item"] = chosen
                    break

    bad2 = [
        r
        for r in enriched
        if r["item"] is None
        or (r["item"].startswith("industrialupgrade:") and r["item"] not in models)
    ]

    DEBUG.write_text(
        json.dumps(
            {
                "count": len(enriched),
                "bad": [{"name": b["name"], "field": b.get("icon_field"), "item": b.get("item")} for b in bad2],
                "tabs": {t: sum(1 for q in enriched if q["tab"] == t) for t in sorted({q["tab"] for q in enriched})},
                "sample": enriched[:20],
            },
            ensure_ascii=False,
            indent=2,
        ),
        encoding="utf-8",
    )

    if bad2:
        print("BAD IDS remaining:", len(bad2))
        for b in bad2[:50]:
            print(" ", b["name"], b.get("icon_field"), b.get("item"))
        # For remaining: use guide_book + checkbox (still valid)
        for b in bad2:
            for q in enriched:
                if q["name"] == b["name"]:
                    q["item"] = None  # checkbox task
                    q["force_checkmark"] = True

    # Build name -> index map (1-based)
    name_to_idx = {q["name"]: i + 1 for i, q in enumerate(enriched)}

    # Tab column offsets so trees don't overlap
    tab_order = ["main", "primal", "steam", "baseElectric", "advancedElectricTab", "improvedElectricTab", "perElectric"]
    tab_ox = {t: i * 28 for i, t in enumerate(tab_order)}

    lines: list[str] = []
    lines.append("{")
    lines.append("\tdefault_hide_dependency_lines: false")
    lines.append('\tdefault_quest_shape: "rsquare"')
    lines.append(f'\tfilename: "{FILENAME}"')
    lines.append(f'\tgroup: "{GROUP_ID}"')
    lines.append('\ticon: "industrialupgrade:book/guide_book"')
    lines.append(f'\tid: "{CHAPTER_ID}"')
    lines.append("\torder_index: 35")
    lines.append("\tquest_links: [ ]")
    lines.append('\ttitle: "Мастерская · Industrial Upgrade"')
    lines.append("\tquests: [")

    for i, q in enumerate(enriched):
        idx = i + 1
        ox = tab_ox.get(q["tab"], 0)
        # Guide coords are in ~35px units; scale to FTB
        gx = (q["x"] or 0) / 35.0 + ox / 35.0 * 0.15
        # Separate tabs vertically too
        tab_oy = tab_order.index(q["tab"]) * 12 if q["tab"] in tab_order else 0
        gy = (q["y"] or 0) / 35.0 + tab_oy

        deps = []
        if q["prev"] and q["prev"] in name_to_idx:
            deps.append(qid(name_to_idx[q["prev"]]))

        tab_lab = TAB_LABEL.get(q["tab"], q["tab"])
        title = escape_snbt(q["title_ru"])
        desc_lines = [
            f"&b✦ Гайд IU&r  &8·&r  &6{tab_lab}&r",
            f"&8id: {q['name']}&r",
            "",
        ]
        for dl in wrap_desc(q["desc_ru"] or ""):
            desc_lines.append(escape_snbt(dl))
        if q.get("item"):
            desc_lines.append("")
            desc_lines.append(f"&a▶ Задача:&r получи предмет")
            desc_lines.append(f"&7{q['item']}&r")
        else:
            desc_lines.append("")
            desc_lines.append("&a▶ Задача:&r отметь квест вручную (обзор гайда)")

        shape = "hexagon" if idx == 1 else ("diamond" if not q.get("prev") else "rsquare")
        size = 1.35 if idx == 1 else (1.2 if not q.get("localization") and q["tab"] == "main" else 1.0)

        lines.append("\t\t{")
        lines.append(f"\t\t\tx: {gx:.2f}d")
        lines.append(f"\t\t\ty: {gy:.2f}d")
        lines.append(f'\t\t\tid: "{qid(idx)}"')
        lines.append(f'\t\t\ttitle: "{title}"')
        if q.get("item"):
            lines.append(f'\t\t\ticon: "{q["item"]}"')
        else:
            lines.append('\t\t\ticon: "industrialupgrade:book/guide_book"')
        lines.append(f'\t\t\tsubtitle: "{escape_snbt(tab_lab)}"')
        lines.append("\t\t\tdescription: [")
        for di, d in enumerate(desc_lines):
            comma = "," if di < len(desc_lines) - 1 else ""
            lines.append(f'\t\t\t\t"{d}"{comma}')
        lines.append("\t\t\t]")
        lines.append(f'\t\t\tshape: "{shape}"')
        lines.append(f"\t\t\tsize: {size}d")
        lines.append("\t\t\tmin_width: 240")
        if q["tab"] == "main" and q["name"] != "start":
            lines.append("\t\t\toptional: true")
        lines.append("\t\t\ttasks: [{")
        lines.append(f'\t\t\t\tid: "{tid(idx)}"')
        if q.get("item") and not q.get("force_checkmark"):
            lines.append('\t\t\t\ttype: "item"')
            lines.append(f'\t\t\t\titem: "{q["item"]}"')
            lines.append("\t\t\t\tcount: 1L")
        else:
            lines.append('\t\t\t\ttype: "checkmark"')
        lines.append("\t\t\t}]")
        lines.append("\t\t\trewards: [ ]")
        if deps:
            lines.append("\t\t\tdependencies: [")
            for di, d in enumerate(deps):
                comma = "," if di < len(deps) - 1 else ""
                lines.append(f'\t\t\t\t"{d}"{comma}')
            lines.append("\t\t\t]")
        lines.append("\t\t}" + ("," if i < len(enriched) - 1 else ""))

    lines.append("\t]")
    lines.append("}")
    lines.append("")

    text = "\n".join(lines)
    OUT_CHAPTER.write_text(text, encoding="utf-8")
    if OUT_SERVER.parent.exists():
        OUT_SERVER.parent.mkdir(parents=True, exist_ok=True)
        OUT_SERVER.write_text(text, encoding="utf-8")

    # Final validation
    ids = re.findall(r'item: "(industrialupgrade:[^"]+)"', text)
    bad_final = [i for i in ids if i not in models]
    print("quests", len(enriched), "item refs", len(ids), "BAD", len(bad_final))
    if bad_final:
        print("bad ids:", bad_final[:30])
    print("checkmark quests", sum(1 for q in enriched if not q.get("item") or q.get("force_checkmark")))
    print("wrote", OUT_CHAPTER)


if __name__ == "__main__":
    main()
