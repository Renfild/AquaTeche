"""Force overworld biome_source to fixed deep_ocean and wipe generated chunks."""
from pathlib import Path
import shutil
import nbtlib
from nbtlib import String, Compound

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech\server")
WORLD = ROOT / "world"
LEVEL = WORLD / "level.dat"


def patch_level_dat():
    f = nbtlib.load(LEVEL)
    root = f[""] if "" in f else f
    data = root["Data"]
    dims = data["WorldGenSettings"]["dimensions"]
    over = dims["minecraft:overworld"]
    gen = over["generator"]
    gen["biome_source"] = Compound({
        "type": String("minecraft:fixed"),
        "biome": String("minecraft:deep_ocean"),
    })
    # keep noise generator + overworld settings (deeper sea from datapack)
    f.save(LEVEL)
    print("Patched level.dat -> fixed deep_ocean")
    bs = gen["biome_source"]
    print("  type:", bs["type"], "biome:", bs["biome"])


def wipe_chunks():
    for name in ("region", "entities", "poi", "DIM-1", "DIM1"):
        p = WORLD / name
        if p.exists():
            shutil.rmtree(p)
            print("Removed", p)
    # keep playerdata, datapacks, skyblock-related data under data/


if __name__ == "__main__":
    patch_level_dat()
    wipe_chunks()
