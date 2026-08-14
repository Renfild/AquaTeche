"""Create a tiny ocean 'atom' raft NBT for SkyblockBuilder."""
from pathlib import Path

import nbtlib
from nbtlib import Byte, Compound, File, Int, List, String

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "server" / "config" / "skyblockbuilder" / "templates" / "ocean_atom.nbt"
OUT_ROOT = ROOT / "config" / "skyblockbuilder" / "templates" / "ocean_atom.nbt"

# Tiny 3x3 prismarine atom floating just above sea level feel
# Layout (Y):
# 0: prismarine bricks platform
# 1: mossy cobble rim + dirt center + chest
# 2: oak sapling on dirt, lantern on rim

palette = List[Compound]([
    Compound({"Name": String("minecraft:prismarine_bricks")}),
    Compound({"Name": String("minecraft:mossy_cobblestone")}),
    Compound({"Name": String("minecraft:dirt")}),
    Compound({
        "Name": String("minecraft:chest"),
        "Properties": Compound({
            "facing": String("south"),
            "type": String("single"),
            "waterlogged": String("false"),
        }),
    }),
    Compound({
        "Name": String("minecraft:oak_sapling"),
        "Properties": Compound({"stage": String("0")}),
    }),
    Compound({
        "Name": String("minecraft:lantern"),
        "Properties": Compound({"hanging": String("false")}),
    }),
    Compound({"Name": String("minecraft:crafting_table")}),
])

blocks = []

# 3x3 brick base
for x in range(3):
    for z in range(3):
        blocks.append(Compound({"pos": List[Int]([Int(x), Int(0), Int(z)]), "state": Int(0)}))

# Rim mossy + center dirt
for x in range(3):
    for z in range(3):
        if x == 1 and z == 1:
            blocks.append(Compound({"pos": List[Int]([Int(x), Int(1), Int(z)]), "state": Int(2)}))
        else:
            blocks.append(Compound({"pos": List[Int]([Int(x), Int(1), Int(z)]), "state": Int(1)}))

# Chest with balanced starter cache (not OP)
chest_items = List[Compound]([
    Compound({"Slot": Byte(0), "id": String("minecraft:bread"), "Count": Byte(8)}),
    Compound({"Slot": Byte(1), "id": String("minecraft:oak_sapling"), "Count": Byte(2)}),
    Compound({"Slot": Byte(2), "id": String("minecraft:bone_meal"), "Count": Byte(4)}),
    Compound({"Slot": Byte(3), "id": String("minecraft:torch"), "Count": Byte(8)}),
    Compound({"Slot": Byte(4), "id": String("minecraft:oak_planks"), "Count": Byte(8)}),
])

blocks.append(Compound({
    "pos": List[Int]([Int(0), Int(2), Int(1)]),
    "state": Int(3),
    "nbt": Compound({
        "id": String("minecraft:chest"),
        "Items": chest_items,
    }),
}))

# Sapling on dirt
blocks.append(Compound({"pos": List[Int]([Int(1), Int(2), Int(1)]), "state": Int(4)}))
# Lantern
blocks.append(Compound({"pos": List[Int]([Int(2), Int(2), Int(1)]), "state": Int(5)}))
# Crafting table
blocks.append(Compound({"pos": List[Int]([Int(1), Int(2), Int(0)]), "state": Int(6)}))

structure = File({
    "size": List[Int]([Int(3), Int(3), Int(3)]),
    "palette": palette,
    "blocks": List[Compound](blocks),
    "entities": List[Compound]([]),
    "DataVersion": Int(3465),  # 1.20.1
})

OUT.parent.mkdir(parents=True, exist_ok=True)
structure.save(OUT, gzipped=True)
OUT_ROOT.parent.mkdir(parents=True, exist_ok=True)
structure.save(OUT_ROOT, gzipped=True)
print(f"Wrote {OUT}")
print(f"Wrote {OUT_ROOT}")
