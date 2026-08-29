"""Set overworld biome_source to vanilla multi_noise. Does NOT wipe chunks."""
from pathlib import Path
import nbtlib
from nbtlib import String, Compound

ROOT = Path(r"C:\Users\xieto\Desktop\AquaTech\server")
LEVEL = ROOT / "world" / "level.dat"


def patch_level_dat(path: Path = LEVEL) -> None:
    f = nbtlib.load(path)
    root = f[""] if "" in f else f
    data = root["Data"]
    dims = data["WorldGenSettings"]["dimensions"]
    over = dims["minecraft:overworld"]
    gen = over["generator"]
    gen["biome_source"] = Compound({
        "type": String("minecraft:multi_noise"),
        "preset": String("minecraft:overworld"),
    })
    f.save(path)
    bs = gen["biome_source"]
    print("Patched", path, "->", bs["type"], bs.get("preset", bs.get("biome")))


if __name__ == "__main__":
    patch_level_dat()
