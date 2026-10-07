#!/usr/bin/env python3
"""Rebuild the main Draconic Evolution recipes around the mods installed on the AquaTech server.

Reads the original recipes from the Draconic Evolution jar, applies the tier ladder below and writes:
  kubejs/server_scripts/32_aquatech_draconic.js   (copied to server/kubejs/server_scripts/)
  DRACONIC_RECIPES.md                              (was -> became table for reviewing and for the quests)

Tier ladder
  Draconium  Botania manasteel + mana diamond, AE2 processors
  Wyvern     Botania terrasteel + dragonstone, IndustrialUpgrade MFE, MEGA processors
  Draconic   MythicBotany alfsteel, Avaritia diamond lattice, ExtendedCrafting tables, MEGA energy cell
  Chaotic    Avaritia crystal matrix + neutronium, chaos fragments from the Productive Bees chaos bee

Run:  python tools/gen_draconic_recipes.py
"""
import copy
import glob
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODS = ROOT / "server" / "mods"
DE_JAR = next(MODS.glob("Draconic-Evolution-*.jar"))
PB_JAR = next(MODS.glob("productivebees-*.jar"))
MC_JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"

OUT_JS = ROOT / "kubejs" / "server_scripts" / "32_aquatech_draconic.js"
OUT_JS_SERVER = ROOT / "server" / "kubejs" / "server_scripts" / "32_aquatech_draconic.js"
OUT_MD = ROOT / "DRACONIC_RECIPES.md"

# ---------------------------------------------------------------------------------------------------------------------
# Tokens (every id is verified against the installed jars below)
# ---------------------------------------------------------------------------------------------------------------------
MANASTEEL = "botania:manasteel_ingot"
MANASTEEL_BLOCK = "botania:manasteel_block"
MANADIA = "botania:mana_diamond"
TERRA = "botania:terrasteel_ingot"
DRAGONSTONE = "botania:dragonstone"
PIXIE = "botania:pixie_dust"
ELEM = "botania:elementium_ingot"
LIVINGROCK = "botania:livingrock"
ALF = "mythicbotany:alfsteel_ingot"

LOGIC = "ae2:logic_processor"
CALC = "ae2:calculation_processor"
ENG = "ae2:engineering_processor"
FLUIX = "ae2:fluix_crystal"
CHARGED = "ae2:charged_certus_quartz_crystal"
FLUIX_PEARL = "ae2:fluix_pearl"
W_RECEIVER = "ae2:wireless_receiver"
W_BOOSTER = "ae2:wireless_booster"

ACCUM = "megacells:accumulation_processor"
MEGA_CELL = "megacells:mega_energy_cell"

LATTICE = "avaritia:diamond_lattice"
MATRIX = "avaritia:crystal_matrix_ingot"
NEUTRON = "avaritia:neutron_ingot"

ADV_TABLE = "extendedcrafting:advanced_table"

URANIUM_ROD = "alexscaves:uranium_rod"
NEO_S = "alexscaves:scarlet_neodymium_ingot"
NEO_A = "alexscaves:azure_neodymium_ingot"

IU_MFE = "industrialupgrade:wiring_storage/mfe_iu"
IU_QUA = "industrialupgrade:wiring_storage/qua_mfsu"
IU_NANO = "industrialupgrade:circuit/nanocircuit"
IU_QUANTUM = "industrialupgrade:circuit/quantumcircuit"

ABYSSAL = "aquamirae:abyssal_amethyst"

# tags / vanilla ids of the originals
T_IRON = "#forge:ingots/iron"
T_DIAMOND = "#forge:gems/diamond"
T_EMERALD = "#forge:gems/emerald"
T_REDSTONE_BLOCK = "#forge:storage_blocks/redstone"
T_DRACONIUM = "#forge:ingots/draconium"
T_AWAKENED = "#forge:ingots/draconium_awakened"
T_NETHERITE = "#forge:ingots/netherite"
I_DRACONIUM_CORE = "draconicevolution:draconium_core"
I_AWAKENED_CORE = "draconicevolution:awakened_core"
I_CHAOTIC_CORE = "draconicevolution:chaotic_core"
I_LARGE_FRAG = "draconicevolution:large_chaos_frag"
I_MEDIUM_FRAG = "draconicevolution:medium_chaos_frag"


def ing(s):
    """'#forge:x' -> tag ingredient, 'mod:item' -> item ingredient."""
    return {"tag": s[1:]} if s.startswith("#") else {"item": s}


def label(i):
    if isinstance(i, list):
        return " | ".join(label(x) for x in i)
    if "tag" in i:
        return "#" + i["tag"].replace("forge:", "")
    return i.get("item", "?")


# ---------------------------------------------------------------------------------------------------------------------
# Spec: recipe path (under data/draconicevolution/recipes/) -> list of operations
#   ("key", letter, new)                shaped: replace one key letter
#   ("all", old, new)                   replace every ingredient equal to `old` (keys, ingredients)
#   ("some", old, new, n)               fusion: replace the first n ingredients equal to `old`
#   ("cat", new)                        fusion: replace the catalyst
# ---------------------------------------------------------------------------------------------------------------------
T0_GENERIC = [("all", T_IRON, MANASTEEL), ("all", T_DIAMOND, MANADIA), ("all", T_EMERALD, CHARGED)]
GENERIC_OPS = set(T0_GENERIC)

SPEC = {
    # --- cores -----------------------------------------------------------------------------------------------------
    "components/draconium_core": [("key", "B", MANASTEEL), ("key", "C", MANADIA)],
    "components/wyvern_core": [("key", "A", TERRA)],
    "components/wyvern_energy_core": [("key", "B", IU_MFE)],
    "components/draconic_energy_core": [("key", "C", MEGA_CELL)],
    "components/chaotic_energy_core": [("key", "C", IU_QUA)],
    "components/awakened_core": [("some", T_AWAKENED, ALF, 2)],
    "components/chaotic_core": [("some", T_AWAKENED, MATRIX, 2), ("some", T_AWAKENED, NEUTRON, 2)],
    "awakened_draconium_block": [("some", I_DRACONIUM_CORE, ALF, 2)],
    # --- machines --------------------------------------------------------------------------------------------------
    "machines/crafting_core": [("key", "A", ENG), ("key", "B", MANADIA)],
    "machines/basic_crafting_injector": [("key", "A", MANADIA), ("key", "C", LIVINGROCK), ("key", "D", MANASTEEL_BLOCK)],
    "machines/wyvern_crafting_injector": [("all", T_DIAMOND, DRAGONSTONE)],
    "machines/awakened_crafting_injector": [("all", T_DIAMOND, LATTICE)],
    # chaos fragments only come from the chaos bee, so the first chaotic injector must not need them
    "machines/chaotic_crafting_injector": [("all", I_LARGE_FRAG, NEUTRON), ("all", T_DIAMOND, MATRIX)],
    "machines/basic_relay_crystal": [("key", "A", FLUIX)],
    "machines/wyvern_relay_crystal": [("key", "C", ENG)],
    "machines/draconic_relay_crystal": [("all", T_DIAMOND, LATTICE)],
    "machines/basic_wireless_crystal": [("key", "C", W_RECEIVER)],
    "machines/wyvern_wireless_crystal": [("key", "C", W_BOOSTER)],
    "machines/draconic_wireless_crystal": [("key", "C", W_BOOSTER), ("key", "A", PIXIE)],
    "machines/energy_core": [("key", "C", ACCUM)],
    "machines/energy_core_stabilizer": [("key", "A", DRAGONSTONE)],
    "machines/energy_pylon": [("key", "C", CHARGED), ("key", "E", MANADIA), ("key", "B", PIXIE)],
    "machines/energy_transfuser": [("key", "A", TERRA)],
    "machines/generator": [("key", "B", MANASTEEL), ("key", "A", IU_NANO)],
    "machines/grinder": [("key", "A", MANASTEEL)],
    "machines/particle_generator": [("key", "A", CHARGED)],
    "machines/draconium_chest": [("some", {"item": "minecraft:crafting_table"}, ADV_TABLE, 2)],
    "celestial_manipulator": [("key", "E", MANASTEEL), ("key", "A", CALC)],
    "tools/crystal_binder": list(T0_GENERIC),
    # magnet needed the (disabled) dislocator, so it is rebuilt without it
    "tools/magnet": [("key", "D", FLUIX_PEARL), ("key", "C", MANASTEEL)],
    # --- misc devices (draconium tier) ----------------------------------------------------------------------------------
    "disenchanter": list(T0_GENERIC),
    "dislocation_inhibitor": list(T0_GENERIC),
    "entity_detector": list(T0_GENERIC),
    "entity_detector_advanced": list(T0_GENERIC),
    "fluid_gate": list(T0_GENERIC),
    "flux_gate": list(T0_GENERIC),
    "rain_sensor": list(T0_GENERIC),
    # --- reactor ---------------------------------------------------------------------------------------------------
    "machines/reactor_core": [("some", T_DRACONIUM, URANIUM_ROD, 3)],
    "machines/reactor_injector": [("all", T_IRON, NEO_S), ("some", T_DRACONIUM, IU_QUANTUM, 1)],
    "machines/reactor_stabilizer": [("some", T_AWAKENED, NEO_A, 2)],
    "machines/reactor_prt_focus_ring": [("key", "A", ELEM), ("key", "B", LATTICE)],
    "machines/reactor_prt_in_rotor": [("key", "C", NEO_S)],
    "machines/reactor_prt_out_rotor": [("key", "A", LATTICE), ("key", "C", NEO_A)],
    "machines/reactor_prt_stab_frame": [("key", "A", NEO_S)],
    # --- wyvern tools: terrasteel instead of two draconium ingots -----------------------------------------------------
    "tools/wyvern_capacitor": [("some", T_DRACONIUM, TERRA, 2)],
    "advanced_magnet_placeholder": [],
    # --- draconic capacitor / staff ----------------------------------------------------------------------------------
    "tools/draconic_capacitor": [("some", T_AWAKENED, ALF, 2)],
    "tools/draconic_staff": [("some", T_AWAKENED, MATRIX, 2)],
    "tools/chaotic_capacitor": [("some", T_AWAKENED, MATRIX, 2)],
}
del SPEC["advanced_magnet_placeholder"]
for tool in ("axe", "bow", "chestpiece", "hoe", "pickaxe", "shovel", "sword"):
    SPEC["tools/wyvern_" + tool] = [("some", T_DRACONIUM, TERRA, 2)]
    SPEC["tools/draconic_" + tool] = [("some", T_NETHERITE, ALF, 2)]
    SPEC["tools/chaotic_" + tool] = [("some", T_AWAKENED, MATRIX, 2), ("some", T_AWAKENED, NEUTRON, 1)]

# --- modules: tier token per module generation ------------------------------------------------------------------------
SPEC["modules/module_core"] = [("all", T_IRON, MANASTEEL), ("all", "#forge:dusts/redstone", LOGIC)]
MODULE_DRACONIUM = [("all", T_IRON, MANASTEEL)]
MODULE_WYVERN = [("all", T_DRACONIUM, TERRA)]
MODULE_DRACONIC = [("all", T_NETHERITE, ALF), ("all", T_EMERALD, DRAGONSTONE)]
MODULE_CHAOTIC = [("all", T_NETHERITE, MATRIX)]
GENERIC_OPS |= set(MODULE_DRACONIUM + MODULE_WYVERN + MODULE_DRACONIC + MODULE_CHAOTIC)


def add_module_rules(names):
    for name in names:
        if name.endswith("_uncraft"):
            continue
        if "_draconium_" in name:
            SPEC["modules/" + name] = list(MODULE_DRACONIUM)
        elif "_wyvern_" in name:
            SPEC["modules/" + name] = list(MODULE_WYVERN)
        elif "_draconic_" in name:
            SPEC["modules/" + name] = list(MODULE_DRACONIC)
        elif "_chaotic_" in name:
            SPEC["modules/" + name] = list(MODULE_CHAOTIC)


# Productive Bees recipes edited in the same script (chaos bee must not need chaos fragments)
PB_CHAOS_BEE = "productivebees:draconicevolution/chaos_bee"
PB_CHAOS_COMB = "productivebees:centrifuge/draconicevolution/honeycomb_chaos"


# ---------------------------------------------------------------------------------------------------------------------
def load_item_names():
    names = set()
    jars = [Path(p) for p in glob.glob(str(MODS / "*.jar"))] + [MC_JAR]
    for jar in jars:
        try:
            z = zipfile.ZipFile(jar)
        except Exception:
            continue
        for n in z.namelist():
            m = re.match(r"assets/([a-z0-9_]+)/models/item/(.+)\.json$", n)
            if m:
                names.add(m.group(1) + ":" + m.group(2))
    return names


def load_recipes():
    z = zipfile.ZipFile(DE_JAR)
    prefix = "data/draconicevolution/recipes/"
    return {n[len(prefix):-5]: json.loads(z.read(n)) for n in z.namelist() if n.startswith(prefix) and n.endswith(".json")}


def same(a, b):
    return isinstance(a, dict) and a == b


def apply_ops(recipe, ops, log):
    r = copy.deepcopy(recipe)
    for op in ops:
        kind = op[0]
        if kind == "key":
            _, letter, new = op
            if letter not in r["key"]:
                raise SystemExit(f"key {letter} missing in recipe")
            r["key"][letter] = ing(new)
        elif kind == "all":
            _, old, new = op
            old_i = ing(old) if isinstance(old, str) else old
            new_i = ing(new)
            hit = 0
            for k, v in (r.get("key") or {}).items():
                if same(v, old_i):
                    r["key"][k] = new_i
                    hit += 1
            if "ingredients" in r:
                r["ingredients"] = [new_i if same(x, old_i) else x for x in r["ingredients"]]
                hit += sum(1 for x in recipe["ingredients"] if same(x, old_i))
            if hit == 0 and op not in GENERIC_OPS:
                log.append(f"  (note) '{label(old_i)}' not present, skipped")
        elif kind == "some":
            _, old, new, n = op
            old_i = ing(old) if isinstance(old, str) else old
            new_i = ing(new)
            left = n
            out = []
            for x in r["ingredients"]:
                if left > 0 and same(x, old_i):
                    out.append(new_i)
                    left -= 1
                else:
                    out.append(x)
            if left > 0:
                raise SystemExit(f"fusion has fewer than {n} of {label(old_i)}")
            r["ingredients"] = out
        elif kind == "cat":
            r["catalyst"] = ing(op[1])
    return r


def multiset(r):
    items = []
    if "key" in r:
        pat = "".join(r["pattern"])
        for ch in pat:
            if ch != " " and ch in r["key"]:
                items.append(label(r["key"][ch]))
    if "ingredients" in r:
        items += [label(i) for i in r["ingredients"]]
    if "catalyst" in r:
        items.append("catalyst: " + label(r["catalyst"]))
    from collections import Counter
    return Counter(items)


def short(s):
    return s.replace("draconicevolution:", "de:").replace("minecraft:", "mc:")


def diff_text(old, new):
    a, b = multiset(old), multiset(new)
    removed = {k: v - b.get(k, 0) for k, v in a.items() if v > b.get(k, 0)}
    added = {k: v - a.get(k, 0) for k, v in b.items() if v > a.get(k, 0)}
    fmt = lambda d: ", ".join(f"{v}x {short(k)}" for k, v in d.items())
    return fmt(removed), fmt(added)


def validate(recipe, rid, names, problems):
    def check(i):
        if isinstance(i, list):
            for x in i:
                check(x)
        elif "item" in i and i["item"] not in names:
            problems.append(f"{rid}: unknown item {i['item']}")
    for v in (recipe.get("key") or {}).values():
        check(v)
    for v in recipe.get("ingredients", []):
        check(v)
    if "catalyst" in recipe:
        check(recipe["catalyst"])
    if "key" in recipe:
        used = set("".join(recipe["pattern"])) - {" "}
        if not used <= set(recipe["key"]):
            problems.append(f"{rid}: pattern uses undefined keys {used - set(recipe['key'])}")


def pb_recipes():
    z = zipfile.ZipFile(PB_JAR)
    bee = json.loads(z.read("data/productivebees/recipes/draconicevolution/chaos_bee.json"))
    comb = json.loads(z.read("data/productivebees/recipes/centrifuge/draconicevolution/honeycomb_chaos.json"))
    bee.pop("conditions", None)
    comb.pop("conditions", None)
    # chaotic cores need chaos fragments, which only this bee's combs give: use neutronium and the crystal matrix instead
    cores = 0
    new_ing = []
    for x in bee["ingredients"]:
        if x == {"item": I_CHAOTIC_CORE}:
            new_ing.append(ing(NEUTRON if cores == 0 else MATRIX))
            cores += 1
        elif x == {"item": I_MEDIUM_FRAG}:
            new_ing.append({"item": I_AWAKENED_CORE})
        else:
            new_ing.append(x)
    bee["ingredients"] = new_ing
    comb["outputs"] = [
        {"item": {"item": "draconicevolution:small_chaos_frag"}, "min": 1, "max": 2},
        {"item": {"item": I_MEDIUM_FRAG}, "chance": 15},
        {"item": {"tag": "forge:wax"}},
    ]
    return bee, comb


def main():
    names = load_item_names()
    recipes = load_recipes()
    add_module_rules(sorted(k.split("/", 1)[1] for k in recipes if k.startswith("modules/")))
    problems, log = [], []
    new_recipes, rows = {}, []
    for rid, ops in sorted(SPEC.items()):
        if rid not in recipes:
            problems.append(f"{rid}: not found in the Draconic Evolution jar")
            continue
        old = recipes[rid]
        new = apply_ops(old, ops, log)
        validate(new, rid, names, problems)
        if new == old:
            continue
        new_recipes[rid] = new
        removed, added = diff_text(old, new)
        result = old["result"]["item"] if isinstance(old.get("result"), dict) else "?"
        rows.append((rid, short(result), old["type"].split(":")[1], new.get("tier", ""), removed, added))
    bee, comb = pb_recipes()
    validate(bee, "pb chaos bee", names, problems)
    if problems:
        print("\n".join(problems))
        sys.exit(1)

    entries = []
    for rid, r in sorted(new_recipes.items()):
        new_id = "aquatech:de/" + rid.replace("/", "_")
        r2 = copy.deepcopy(r)
        entries.append((f"draconicevolution:{rid}", new_id, r2))
    lines = [
        "// AquaTech: Draconic Evolution — главные рецепты пересобраны под моды сервера.",
        "// GENERATED by tools/gen_draconic_recipes.py. Не правь руками: правь спеку и перегенерируй (см. DRACONIC_RECIPES.md).",
        "// Лестница: Draconium = Botania+AE2, Wyvern = терраcталь+IU, Draconic = альфсталь+Avaritia, Chaotic = матрица+нейтроний+пчела хаоса.",
        "",
        "ServerEvents.recipes((event) => {",
        "  if (!Platform.isLoaded('draconicevolution')) return",
        "  console.log('[AquaTech] Loading Draconic Evolution rework...')",
        "",
        "  const DE_REWORK = [",
    ]
    for old_id, new_id, r in entries:
        lines.append(f"    ['{old_id}', '{new_id}', {json.dumps(r, ensure_ascii=False, separators=(',', ':'))}],")
    lines += [
        "  ]",
        "  DE_REWORK.forEach((row) => {",
        "    event.remove({ id: row[0] })",
        "    event.custom(row[2]).id(row[1])",
        "  })",
        "",
        "  // Productive Bees: пчела хаоса без осколков хаоса в рецепте, а её соты дают осколки (иначе замкнутый круг без Chaos Guardian)",
        f"  event.remove({{ id: '{PB_CHAOS_BEE}' }})",
        f"  event.custom({json.dumps(bee, ensure_ascii=False, separators=(',', ':'))}).id('aquatech:pb/chaos_bee')",
        f"  event.remove({{ id: '{PB_CHAOS_COMB}' }})",
        f"  event.custom({json.dumps(comb, ensure_ascii=False, separators=(',', ':'))}).id('aquatech:pb/honeycomb_chaos')",
        "})",
        "",
    ]
    js = "\n".join(lines)
    OUT_JS.write_text(js, encoding="utf-8", newline="\n")
    OUT_JS_SERVER.parent.mkdir(parents=True, exist_ok=True)
    OUT_JS_SERVER.write_text(js, encoding="utf-8", newline="\n")

    md = ["# Draconic Evolution: пересобранные рецепты", "",
          "Файл создаёт `tools/gen_draconic_recipes.py`. Список «было → стало» по каждому изменённому рецепту.",
          "Энергия и тир слияния (Fusion) оставлены как в моде: сложность идёт от материалов.", "",
          "| Рецепт | Результат | Тип | Тир | Убрано | Добавлено |", "|---|---|---|---|---|---|"]
    for rid, result, typ, tier, removed, added in rows:
        md.append(f"| `{rid}` | {result} | {typ} | {tier} | {removed or '—'} | {added or '—'} |")
    md.append("")
    md.append("Пчёлы (Productive Bees): `aquatech:pb/chaos_bee` и `aquatech:pb/honeycomb_chaos`: пчела хаоса собирается из ядер Awakened, нейтрония и матрицы, а её соты в центрифуге дают малые и средние осколки хаоса.")
    OUT_MD.write_text("\n".join(md) + "\n", encoding="utf-8", newline="\n")
    print(f"changed recipes: {len(rows)} (+2 Productive Bees); js {len(js)//1024} KB")
    for line in log:
        print(line)


if __name__ == "__main__":
    main()
