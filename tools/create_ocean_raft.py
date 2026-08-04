"""4x4 floating oak raft for AquaTech + SkyblockBuilder island spawn."""
from pathlib import Path
import nbtlib
from nbtlib import File, Compound, List, Int, String

ROOT = Path(__file__).resolve().parents[1]
OUTS = [
    ROOT / "server" / "config" / "skyblockbuilder" / "templates" / "ocean_raft_4x4.nbt",
    ROOT / "config" / "skyblockbuilder" / "templates" / "ocean_raft_4x4.nbt",
]

# 4x4 oak planks deck + oak log border + chest at (1,1,1)
palette = List[Compound](
    [
        Compound({"Name": String("minecraft:oak_log"), "Properties": Compound({"axis": String("y")})}),
        Compound({"Name": String("minecraft:oak_planks")}),
        Compound({"Name": String("minecraft:chest"), "Properties": Compound({"facing": String("south"), "type": String("single"), "waterlogged": String("false")})}),
    ]
)

blocks = []
for x in range(4):
    for z in range(4):
        # border logs, inner planks
        state = 0 if x in (0, 3) or z in (0, 3) else 1
        blocks.append(Compound({"pos": List[Int]([Int(x), Int(0), Int(z)]), "state": Int(state)}))

# chest on top of center-ish plank for starter chest feel (optional furniture)
blocks.append(Compound({"pos": List[Int]([Int(1), Int(1), Int(1)]), "state": Int(2)}))

structure = File(
    {
        "size": List[Int]([Int(4), Int(2), Int(4)]),
        "palette": palette,
        "blocks": List[Compound](blocks),
        "entities": List[Compound]([]),
        "DataVersion": Int(3465),
    }
)

for out in OUTS:
    out.parent.mkdir(parents=True, exist_ok=True)
    structure.save(out, gzipped=True)
    print("Wrote", out)
