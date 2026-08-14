# -*- coding: utf-8 -*-
"""Apply thematic icons/shapes/tags to FTB Quests chapters + expand theme.txt."""
from __future__ import annotations

import re
from pathlib import Path

ROOTS = [
    Path(r"C:\Users\xieto\Desktop\AquaTech\config\ftbquests\quests"),
    Path(r"C:\Users\xieto\Desktop\AquaTech\server\config\ftbquests\quests"),
]

# filename stem -> (icon, default_quest_shape, theme_tag, optional pretty title override)
CHAPTER_THEME = {
    "00_horizon_route": ("minecraft:nautilus_shell", "circle", "horizon", None),
    "01_kickstarter": ("minecraft:oak_chest_boat", "rsquare", "catastrophe", None),
    "02_catch": ("minecraft:fishing_rod", "circle", "catch", None),
    "03_atoll": ("minecraft:oak_sapling", "hexagon", "atoll", None),
    "04_roost": ("minecraft:egg", "rsquare", "roost", None),
    "05_swarm": ("minecraft:honeycomb", "hexagon", "swarm", None),
    "06_kinetics": ("minecraft:water_bucket", "gear", "kinetics", None),
    "07_steam": ("minecraft:furnace", "octagon", "steam", None),
    "08_power": ("minecraft:redstone", "diamond", "power", None),
    "09_industry": ("industrialupgrade:blockresource/machine", "rsquare", "industry", None),
    "10_depths": ("minecraft:prismarine_shard", "pentagon", "depths", None),
    "11_me": ("ae2:controller", "hexagon", "me", None),
    "12_dreadnought": ("minecraft:netherite_ingot", "diamond", "dreadnought", None),
    "20_ws_bees": ("minecraft:beehive", "hexagon", "ws_bees", None),
    "21_ws_roost": ("minecraft:chicken_spawn_egg", "rsquare", "ws_roost", None),
    "22_ws_mystical": ("minecraft:wheat_seeds", "diamond", "ws_mystical", None),
    "23_ws_create_water": ("minecraft:water_bucket", "gear", "ws_create", None),
    "24_ws_atoll_atmosphere": ("minecraft:lily_pad", "circle", "ws_atoll", None),
    "25_ws_ocean_rituals": ("minecraft:heart_of_the_sea", "heart", "ws_rituals", None),
    "26_ws_mek": ("minecraft:redstone_block", "octagon", "ws_mek", None),
    "27_ws_ae2": ("ae2:controller", "hexagon", "ws_ae2", None),
    "28_ws_thermal": ("minecraft:magma_block", "diamond", "ws_thermal", None),
    "29_ws_if": ("minecraft:hopper", "rsquare", "ws_if", None),
    "2A_ws_apotheosis": ("minecraft:enchanted_book", "pentagon", "ws_apoth", None),
    "2B_ws_enderio": ("minecraft:ender_pearl", "diamond", "ws_enderio", None),
    "2C_ws_avaritia": ("minecraft:nether_star", "diamond", "ws_avaritia", None),
    "2D_ws_draconic": ("minecraft:dragon_egg", "octagon", "ws_draconic", None),
    "2E_ws_botania_plus": ("minecraft:lily_of_the_valley", "heart", "ws_botania", None),
    "2F_ws_industrial_upgrade": ("industrialupgrade:machines/advanced_solar_paneliu", "gear", "ws_iu", None),
}

GROUPS_SNBT = """{
	chapter_groups: [
		{
			id: "0AC7A00000000000"
			title: "§b★ §3Маршрут Горизонта"
		}
		{
			id: "0AC7A00000000001"
			title: "§cАкт I §8· §fКатастрофа"
		}
		{
			id: "0AC7A00000000002"
			title: "§aАкт II §8· §fЖизнь на атолле"
		}
		{
			id: "0AC7A00000000003"
			title: "§6Акт III §8· §fИндустрия волн"
		}
		{
			id: "0AC7A00000000004"
			title: "§dАкт IV §8· §fГоризонт"
		}
		{
			id: "0AC7A00000000005"
			title: "§e⚙ §6Мастерские"
		}
	]
}
"""

THEME_TAGS = r"""
# --- Per-theme quest outline colors (add tag without # on quests/chapters via script) ---
[#horizon]
quest_started_color:     #C8FFD166
quest_completed_color:   #C8FF9F1C
dependency_line_completed_color: #FFD166

[#catastrophe]
quest_started_color:     #C8FF6B6B
quest_completed_color:   #C8C92A2A
dependency_line_completed_color: #E03131

[#catch]
quest_started_color:     #C84DABF7
quest_completed_color:   #C81C7ED6
dependency_line_completed_color: #339AF0

[#atoll]
quest_started_color:     #C869DB7C
quest_completed_color:   #C82F9E44
dependency_line_completed_color: #40C057

[#roost]
quest_started_color:     #C8FFC078
quest_completed_color:   #C8F08C00
dependency_line_completed_color: #FD7E14

[#swarm]
quest_started_color:     #C8FFE066
quest_completed_color:   #C8FAB005
dependency_line_completed_color: #FCC419

[#kinetics]
quest_started_color:     #C874C0FC
quest_completed_color:   #C81C7ED6
dependency_line_completed_color: #4DABF7

[#steam]
quest_started_color:     #C8ADB5BD
quest_completed_color:   #C8495057
dependency_line_completed_color: #868E96

[#power]
quest_started_color:     #C8FF6B6B
quest_completed_color:   #C8E03131
dependency_line_completed_color: #FA5252

[#industry]
quest_started_color:     #C8FFA94D
quest_completed_color:   #C8E8590C
dependency_line_completed_color: #FD7E14

[#depths]
quest_started_color:     #C83BC9DB
quest_completed_color:   #C80B7285
dependency_line_completed_color: #15AABF

[#me]
quest_started_color:     #C8B197FC
quest_completed_color:   #C87043D6
dependency_line_completed_color: #9775FA

[#dreadnought]
quest_started_color:     #C8E599F7
quest_completed_color:   #C89C36B5
dependency_line_completed_color: #CC5DE8

[#ws_bees]
quest_started_color:     #C8FFE066
quest_completed_color:   #C8F59F00
dependency_line_completed_color: #FAB005

[#ws_roost]
quest_started_color:     #C8FFC9C9
quest_completed_color:   #C8FA5252
dependency_line_completed_color: #FF8787

[#ws_mystical]
quest_started_color:     #C8B2F2BB
quest_completed_color:   #C837B24D
dependency_line_completed_color: #51CF66

[#ws_create]
quest_started_color:     #C8A5D8FF
quest_completed_color:   #C81C7ED6
dependency_line_completed_color: #4DABF7

[#ws_atoll]
quest_started_color:     #C896F2D7
quest_completed_color:   #C80CA678
dependency_line_completed_color: #20C997

[#ws_rituals]
quest_started_color:     #C874C0FC
quest_completed_color:   #C8364FC7
dependency_line_completed_color: #5C7CFA

[#ws_mek]
quest_started_color:     #C8FFA8A8
quest_completed_color:   #C8C92A2A
dependency_line_completed_color: #FF6B6B

[#ws_ae2]
quest_started_color:     #C8D0BFFF
quest_completed_color:   #C87950F2
dependency_line_completed_color: #9775FA

[#ws_thermal]
quest_started_color:     #C8FFD8A8
quest_completed_color:   #C8E67700
dependency_line_completed_color: #FF922B

[#ws_if]
quest_started_color:     #C8C3FAE8
quest_completed_color:     #C812B886
dependency_line_completed_color: #20C997

[#ws_apoth]
quest_started_color:     #C8E5DBFF
quest_completed_color:   #C87950F2
dependency_line_completed_color: #B197FC

[#ws_enderio]
quest_started_color:     #C8B2F2BB
quest_completed_color:   #C837B24D
dependency_line_completed_color: #69DB7C

[#ws_avaritia]
quest_started_color:     #C8FFC9C9
quest_completed_color:   #C8FA0000
dependency_line_completed_color: #FF6B6B

[#ws_draconic]
quest_started_color:     #C8E599F7
quest_completed_color:   #C89C36B5
dependency_line_completed_color: #DA77F2

[#ws_botania]
quest_started_color:     #C8D8F5A2
quest_completed_color:   #C874B816
dependency_line_completed_color: #94D82D

[#ws_iu]
quest_started_color:     #C8FFEC99
quest_completed_color:   #C8F08C00
dependency_line_completed_color: #FCC419
"""


def set_or_replace_field(text: str, key: str, value: str) -> str:
    """Set top-level chapter field (one tab indent)."""
    pat = re.compile(rf'(?m)^(\t){re.escape(key)}:.*$')
    line = f'\t{key}: "{value}"'
    if pat.search(text):
        return pat.sub(line, text, count=1)
    # insert after opening brace / near filename or id
    m = re.search(r'(?m)^\{$', text)
    if not m:
        return text
    # put after first few header fields: after filename if present else after {
    fm = re.search(r'(?m)^\tfilename:.*$', text)
    if fm:
        idx = fm.end()
        return text[:idx] + "\n" + line + text[idx:]
    return text[: m.end()] + "\n" + line + text[m.end() :]


def ensure_quest_tags(text: str, tag: str) -> str:
    """Add theme tag to each quest. After FTB 4.22 resave, quest fields use 3 tabs."""
    lines = text.splitlines(keepends=True)
    out: list[str] = []
    i = 0
    while i < len(lines):
        line = lines[i]
        out.append(line)
        # Quest-level id only (exactly 3 tabs). Nested reward/task ids use 4+.
        if re.match(r'^\t\t\tid:\s*"', line):
            has_tags = False
            j = i + 1
            while j < len(lines):
                nxt = lines[j]
                if re.match(r'^\t\t\{\s*$', nxt) or re.match(r'^\t\]', nxt):
                    break
                if re.match(r'^\t\t\ttags:', nxt):
                    has_tags = True
                    if f'"{tag}"' not in nxt:
                        lines[j] = re.sub(r'\[', f'[ "{tag}", ', nxt, count=1)
                    break
                if re.match(r'^\t\t\}\s*$', nxt):
                    break
                j += 1
            if not has_tags:
                out.append(f'\t\t\ttags: ["{tag}"]\n')
        i += 1
    return "".join(out)


def patch_chapter(path: Path) -> bool:
    stem = path.stem
    if stem not in CHAPTER_THEME:
        return False
    icon, shape, tag, _title = CHAPTER_THEME[stem]
    text = path.read_text(encoding="utf-8")
    original = text
    text = set_or_replace_field(text, "icon", icon)
    text = set_or_replace_field(text, "default_quest_shape", shape)
    text = ensure_quest_tags(text, tag)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main():
    for root in ROOTS:
        if not root.exists():
            print("skip missing", root)
            continue
        (root / "chapter_groups.snbt").write_text(GROUPS_SNBT, encoding="utf-8")
        print("groups ->", root)
        ch_dir = root / "chapters"
        n = 0
        for p in sorted(ch_dir.glob("*.snbt")):
            if patch_chapter(p):
                n += 1
                print(" patched", p.name)
        print(f"patched {n} chapters in {root}")

    theme_paths = [
        Path(r"C:\Users\xieto\Desktop\AquaTech\kubejs\assets\ftbquests\ftb_quests_theme.txt"),
        Path(r"C:\Users\xieto\Desktop\AquaTech\server\kubejs\assets\ftbquests\ftb_quests_theme.txt"),
    ]
    for tp in theme_paths:
        tp.parent.mkdir(parents=True, exist_ok=True)
        if tp.exists():
            base = tp.read_text(encoding="utf-8")
            # strip old tag sections we manage
            base = re.split(r"\n# --- Per-theme quest outline colors", base, maxsplit=1)[0].rstrip() + "\n"
        else:
            base = "[*]\n"
        tp.write_text(base + "\n" + THEME_TAGS.lstrip("\n"), encoding="utf-8")
        print("theme ->", tp)


if __name__ == "__main__":
    main()
