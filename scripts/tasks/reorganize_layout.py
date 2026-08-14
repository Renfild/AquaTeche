"""One-shot layout pass: root clutter -> scripts/, tools/ by domain.

Safe to re-run (skips missing sources). Does not touch server/, mods sources, kubejs, docs portal.
"""
from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# scripts/ layout
SCRIPTS = ROOT / "scripts"
CRAFT = SCRIPTS / "crafttweaker"
DEPLOY = SCRIPTS / "deploy"
TASKS = SCRIPTS / "tasks"
SCRATCH = SCRIPTS / "scratch"
ARCHIVE = SCRIPTS / "archive"

# tools/ domains (critical release entrypoints stay in tools/)
TOOLS = ROOT / "tools"
TOOL_SUBS = {
    "quests": [
        "generate_600_ocean_quests.py",
        "generate_workshop_quests.py",
        "generate_spine_quests.py",
        "workshop_guides.py",
        "workshop_quest_extras.py",
        "wire_aquatech_quests.py",
        "wire_aquatech_quests_p2.py",
        "inject_aqua_xp_rewards.py",
        "strip_op_quest_rewards.py",
        "validate_quests.py",
        "check_all_chapters.py",
        "build_ftb_quests.py",
        "gen_iu_guide_ftbquests.py",
        "patch_ftbquests.py",
        "count_chapter_quests.py",
        "find_chapter_group_format.py",
        "retarget_quest_groups.py",
        "theme_ftb_chapters.py",
        "upload_ftbquests_assets.py",
        "compress_quest_image.py",
    ],
    "patches": [
        "patch_fawe_mohist.py",
        "patch_fawe_typeproperty.py",
        "patch_iu_no_free_scanner.py",
        "patch_iu_fishing_hints.py",
        "patch_skyblockbuilder_exitportal.py",
        "fix_iu_item_ids.py",
        "toggle_iu_space_dims.py",
        "update_fawe.py",
    ],
    "portal": [
        "smoke_portal_and_versions.py",
        "test_portal_login_happy.py",
        "test_portal_login_cookie.py",
        "get_cf_token.py",
        "get_cf_token_persistent.py",
        "cf_token_playwright.js",
        "set_github_cf_secret.py",
        "setup_d1.py",
    ],
    "assets": [
        "gen_machine_guis.py",
        "gen_clean_machine_guis.py",
        "gen_rate_mod_textures.py",
        "gen_rhythm_textures.py",
        "gen_stardew_rhythm_ui.py",
        "gen_sc_fishing_ui.py",
        "generate_aquatech_ui_assets.py",
        "generate_resource_rods.py",
        "generate_fish_table.py",
        "slice_rhythm_ui.py",
        "extract_starcatcher_chances.py",
        "gen_premium_cases.py",
        "validate_cases_json.py",
        "validate_kubejs_textures.py",
    ],
    "server_setup": [
        "setup_luckperms_config.py",
        "setup_luckperms_groups.py",
        "setup_ocean_world.py",
        "setup_mohist_server.py",
        "configure_minimal_default_permissions.py",
        "configure_worldguard_explosions.py",
        "install_dev_overlay_mods.py",
        "install_kubejs.py",
        "install_aquatech_rank_prefixes.py",
        "install_oraxen_rank_prefixes.py",
        "create_ocean_raft.py",
        "create_ocean_atom_island.py",
        "force_deep_ocean_world.py",
        "boost_ocean_life.py",
        "reduce_ores.py",
        "reset_player_y.py",
        "make_boot_fixes_datapack.py",
        "download_industrial_upgrade.py",
        "download_opt_mods.py",
        "prune_mods_whitelist.py",
    ],
    "launcher_tests": [
        "test_launcher_smoke.py",
        "test_clean_user_installation.py",
        "_test_launch.py",
    ],
}

ROOT_TO_DEPLOY = [
    "deploy_aquatech_ui.ps1",
    "deploy_casesmod.ps1",
    "deploy_industrial_upgrade.ps1",
    "deploy_launcher.ps1",
    "deploy_runtime.ps1",
    "setup_horizon_route.ps1",
    "start_sync_server.bat",
    "update_and_push.bat",
]

ROOT_TO_TASKS = [
    "check_versions.py",
]

ROOT_TO_SCRATCH = [
    "NameCommand.class",
    "plugin.yml",
    "registries.json",
    "forge-1.20.1-47.4.0-installer.jar.log",
]

ROOT_TO_ARCHIVE_DEMOS = [
    "menu_ui_demo.html",
    "media__1786049049121.png",
]

ROOT_TO_ARCHIVE_DOCS = [
    "starcatcher_fish_chances.md",
]

ROOT_DIRS_TO_ARCHIVE = [
    ("fishing", "extracted/fishing"),
    ("starcatcher", "extracted/starcatcher"),
    ("com", "extracted/com"),
    ("META-INF", "scratch_meta_inf"),
    ("casesmod-fixed-source_1", "extracted/casesmod-fixed-source_1"),
]

TOOLS_SCRATCH = [
    "_tmp_guidebook.txt",
    "_tmp_iucore.txt",
    "_tmp_iuevent.txt",
]


def move(src: Path, dst: Path) -> None:
    if not src.exists():
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    if dst.exists():
        if src.is_dir():
            shutil.rmtree(dst)
        else:
            dst.unlink()
    shutil.move(str(src), str(dst))
    print(f"  {src.relative_to(ROOT)}  ->  {dst.relative_to(ROOT)}")


def deepen_repo_root(py_file: Path) -> None:
    """tools/foo.py used parents[1]; tools/domain/foo.py needs parents[2]."""
    text = py_file.read_text(encoding="utf-8")
    orig = text
    # Common patterns pointing at repo root from tools/
    repls = [
        (
            r"Path\(__file__\)\.resolve\(\)\.parents\[1\]",
            "Path(__file__).resolve().parents[2]",
        ),
        (
            r"Path\(__file__\)\.resolve\(\)\.parent\.parent(?!\.parent)",
            "Path(__file__).resolve().parent.parent.parent",
        ),
    ]
    for pat, rep in repls:
        text = re.sub(pat, rep, text)
    # patch_fawe_mohist ASM_DIR lived next to script as tools/_asm
    if py_file.name.startswith("patch_fawe") and "_asm" in text:
        text = text.replace(
            'Path(__file__).resolve().parent / "_asm"',
            'Path(__file__).resolve().parent / "_asm"',
        )
    if text != orig:
        py_file.write_text(text, encoding="utf-8")
        print(f"  fix roots: {py_file.relative_to(ROOT)}")


def main() -> None:
    print("=== scripts/ tree ===")
    for d in (CRAFT, DEPLOY, TASKS, SCRATCH, ARCHIVE / "demos", ARCHIVE / "docs", ARCHIVE / "jars", ARCHIVE / "extracted"):
        d.mkdir(parents=True, exist_ok=True)

    # CraftTweaker leftovers already in scripts/
    for p in SCRIPTS.glob("*.zs*"):
        move(p, CRAFT / p.name)

    print("=== root -> scripts ===")
    for name in ROOT_TO_DEPLOY:
        move(ROOT / name, DEPLOY / name)
    for name in ROOT_TO_TASKS:
        move(ROOT / name, TASKS / name)
    for name in ROOT_TO_SCRATCH:
        move(ROOT / name, SCRATCH / name)
    for name in ROOT_TO_ARCHIVE_DEMOS:
        move(ROOT / name, ARCHIVE / "demos" / name)
    for name in ROOT_TO_ARCHIVE_DOCS:
        move(ROOT / name, ARCHIVE / "docs" / name)

    # Cyrillic / mojibake loose files
    for p in ROOT.iterdir():
        if not p.is_file():
            continue
        if p.suffix.lower() in {".txt", ".md"} and p.name not in {
            "README.md",
            "CHANGELOG.md",
            "IMPLEMENTATION_PLAN.md",
            "HORIZON_ROUTE.md",
            "PLAYER_ROADMAP.md",
            "QUEST_ID_FREEZE.md",
        }:
            if any(ord(c) > 127 for c in p.name) or p.name.startswith("КАК") or "обнов" in p.name.lower():
                move(p, ARCHIVE / "docs" / p.name)

    for p in ROOT.iterdir():
        if p.is_dir() and any(ord(c) > 127 for c in p.name):
            move(p, ARCHIVE / "docs" / p.name)

    jar = ROOT / "starcatcher-2.3.19-FORGE-1.20.1 (1).jar"
    move(jar, ARCHIVE / "jars" / jar.name)

    for src_name, rel in ROOT_DIRS_TO_ARCHIVE:
        move(ROOT / src_name, ARCHIVE / rel)

    print("=== tools scratch ===")
    for name in TOOLS_SCRATCH:
        move(TOOLS / name, SCRATCH / name)
    move(TOOLS / "_iu_peek", SCRATCH / "_iu_peek")
    move(TOOLS / "_asm", TOOLS / "patches" / "_asm")

    print("=== tools by domain ===")
    for sub, names in TOOL_SUBS.items():
        dest_dir = TOOLS / sub
        dest_dir.mkdir(parents=True, exist_ok=True)
        for name in names:
            src = TOOLS / name
            if not src.exists():
                continue
            dst = dest_dir / name
            move(src, dst)
            if dst.suffix == ".py":
                deepen_repo_root(dst)

    # FAWE patches expect _asm beside them
    asm = TOOLS / "patches" / "_asm"
    if not asm.exists() and (TOOLS / "_asm").exists():
        move(TOOLS / "_asm", asm)

    move(TOOLS / "tools_npm", TOOLS / "portal" / "tools_npm")

    # Root wrappers so old muscle-memory paths still work for deploy
    print("=== root deploy shims ===")
    for name in ROOT_TO_DEPLOY:
        target = DEPLOY / name
        if not target.exists():
            continue
        shim = ROOT / name
        if shim.exists():
            continue
        if name.endswith(".ps1"):
            shim.write_text(
                f'# shim -> scripts/deploy/{name}\n'
                f'& "$PSScriptRoot\\scripts\\deploy\\{name}" @args\n',
                encoding="utf-8",
            )
        elif name.endswith(".bat"):
            shim.write_text(
                f"@echo off\r\n"
                f"call \"%~dp0scripts\\deploy\\{name}\" %*\r\n",
                encoding="utf-8",
            )
        print(f"  shim {name}")

    print("DONE")


if __name__ == "__main__":
    main()
