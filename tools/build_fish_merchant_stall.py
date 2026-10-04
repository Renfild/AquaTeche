"""Строит схематику пристани-лавки для торговца рыбой (Sponge .schem v2, читает FAWE/WorldEdit).

    python tools/build_fish_merchant_stall.py

Результат: mods/aquatech-ui/art/fish_merchant_stall.schem
Точка вставки: игрок стоит на передней кромке настила, по центру. Торговец стоит на 9 блоков вперёд.
"""
import gzip
import struct
from pathlib import Path

DATA_VERSION = 3465  # Minecraft 1.20.1
OUT = Path(__file__).resolve().parent.parent / "mods" / "aquatech-ui" / "art" / "fish_merchant_stall.schem"

W, H, L = 15, 12, 13          # X, Y, Z
DECK_Y = 4                    # сваи занимают y=0..3
ORIGIN = (7, DECK_Y + 1, 0)   # где «стоит» игрок при //paste
MERCHANT = (7, DECK_Y + 1, 9)

blocks = {}


def put(x, y, z, block):
    assert 0 <= x < W and 0 <= y < H and 0 <= z < L, (x, y, z, block)
    blocks[(x, y, z)] = block


def fill(x0, x1, y0, y1, z0, z1, block):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(z0, z1 + 1):
                put(x, y, z, block)


def stairs(x, y, z, facing):
    put(x, y, z, f"minecraft:spruce_stairs[facing={facing},half=bottom,shape=straight]")


# --- сваи и настил ---------------------------------------------------------------------------
for px in (0, 7, 14):
    for pz in (0, 6, 12):
        fill(px, px, 0, DECK_Y - 1, pz, pz, "minecraft:spruce_log[axis=y]")

fill(0, W - 1, DECK_Y, DECK_Y, 0, L - 1, "minecraft:spruce_planks")
for x in range(W):
    put(x, DECK_Y, 0, "minecraft:stripped_spruce_log[axis=x]")
    put(x, DECK_Y, L - 1, "minecraft:stripped_spruce_log[axis=x]")
for z in range(1, L - 1):
    put(0, DECK_Y, z, "minecraft:stripped_spruce_log[axis=z]")
    put(W - 1, DECK_Y, z, "minecraft:stripped_spruce_log[axis=z]")
# тёмный пол внутри лавки и дорожка от входа
fill(3, 11, DECK_Y, DECK_Y, 5, 11, "minecraft:dark_oak_planks")
fill(6, 8, DECK_Y, DECK_Y, 1, 4, "minecraft:dark_oak_planks")

# --- лавка: столбы, стены, крыша -------------------------------------------------------------
for px, pz in ((3, 5), (11, 5), (3, 11), (11, 11)):
    fill(px, px, DECK_Y + 1, DECK_Y + 4, pz, pz, "minecraft:stripped_spruce_log[axis=y]")
fill(3, 11, DECK_Y + 1, DECK_Y + 3, 11, 11, "minecraft:spruce_planks")  # задняя стена
fill(3, 3, DECK_Y + 1, DECK_Y + 1, 6, 10, "minecraft:spruce_fence")      # боковые перила
fill(11, 11, DECK_Y + 1, DECK_Y + 1, 6, 10, "minecraft:spruce_fence")
fill(3, 3, DECK_Y + 4, DECK_Y + 4, 6, 10, "minecraft:spruce_planks")     # фронтоны
fill(11, 11, DECK_Y + 4, DECK_Y + 4, 6, 10, "minecraft:spruce_planks")

eave = DECK_Y + 4
for x in range(2, 13):
    stairs(x, eave, 5, "north")
    stairs(x, eave, 11, "south")
    stairs(x, eave + 1, 6, "north")
    stairs(x, eave + 1, 10, "south")
    fill(x, x, eave + 1, eave + 1, 7, 9, "minecraft:spruce_planks")
    stairs(x, eave + 2, 7, "north")
    stairs(x, eave + 2, 9, "south")
    put(x, eave + 2, 8, "minecraft:spruce_slab[type=bottom]")
# балки под крышей
for x in (3, 7, 11):
    fill(x, x, eave, eave, 6, 10, "minecraft:stripped_spruce_log[axis=z]")

# --- прилавок и интерьер ---------------------------------------------------------------------
for x in range(4, 11):
    put(x, DECK_Y + 1, 7, "minecraft:stripped_spruce_log[axis=x]")
put(5, DECK_Y + 2, 7, "minecraft:lantern[hanging=false]")
put(9, DECK_Y + 2, 7, "minecraft:lantern[hanging=false]")
put(7, DECK_Y + 2, 7, "minecraft:sea_pickle[pickles=4,waterlogged=false]")
put(6, DECK_Y + 2, 7, "minecraft:sea_pickle[pickles=2,waterlogged=false]")
put(8, DECK_Y + 2, 7, "minecraft:sea_pickle[pickles=3,waterlogged=false]")

fill(6, 8, DECK_Y + 1, DECK_Y + 1, 8, 10, "minecraft:cyan_carpet")      # ковёр под торговцем
for x in (4, 5, 9, 10):
    put(x, DECK_Y + 1, 10, "minecraft:barrel[facing=up]")
put(4, DECK_Y + 2, 10, "minecraft:lantern[hanging=false]")
put(10, DECK_Y + 2, 10, "minecraft:lantern[hanging=false]")
put(5, DECK_Y + 1, 8, "minecraft:water_cauldron[level=3]")               # ведро с уловом
put(9, DECK_Y + 1, 8, "minecraft:water_cauldron[level=3]")
put(7, DECK_Y + 3, 8, "minecraft:lantern[hanging=true]")

# --- причал: перила и фонари -----------------------------------------------------------------
rails = set()
for z in range(0, L):
    rails.add((0, DECK_Y + 1, z))
    rails.add((W - 1, DECK_Y + 1, z))
for x in range(0, W):
    rails.add((x, DECK_Y + 1, L - 1))
# проход в передней части оставляем открытым
for pos in rails:
    put(*pos, "minecraft:spruce_fence")
for cx, cz in ((0, 0), (W - 1, 0), (0, L - 1), (W - 1, L - 1), (0, 6), (W - 1, 6)):
    put(cx, DECK_Y + 2, cz, "minecraft:spruce_fence")
    put(cx, DECK_Y + 3, cz, "minecraft:lantern[hanging=false]")

# бочки у входа
for pos in ((12, DECK_Y + 1, 2), (13, DECK_Y + 1, 2), (13, DECK_Y + 1, 3)):
    put(*pos, "minecraft:barrel[facing=up]")
put(13, DECK_Y + 2, 2, "minecraft:lantern[hanging=false]")
for pos in ((1, DECK_Y + 1, 2), (1, DECK_Y + 1, 3)):
    put(*pos, "minecraft:dried_kelp_block")


# --- связность заборов ----------------------------------------------------------------------
def is_solid_neighbour(name):
    return "planks" in name or "_log" in name or name.startswith("minecraft:barrel")


for pos, block in list(blocks.items()):
    if not block.startswith("minecraft:spruce_fence"):
        continue
    x, y, z = pos
    props = []
    for key, (dx, dz) in (("north", (0, -1)), ("south", (0, 1)), ("east", (1, 0)), ("west", (-1, 0))):
        other = blocks.get((x + dx, y, z + dz))
        if other and (other.startswith("minecraft:spruce_fence") or is_solid_neighbour(other)):
            props.append(f"{key}=true")
    blocks[pos] = "minecraft:spruce_fence" + (f"[{','.join(props)}]" if props else "")


# --- запись Sponge schematic v2 --------------------------------------------------------------
def tag_name(name):
    raw = name.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def nbt_int(name, value):
    return b"\x03" + tag_name(name) + struct.pack(">i", value)


def nbt_short(name, value):
    return b"\x02" + tag_name(name) + struct.pack(">h", value)


def nbt_int_array(name, values):
    return b"\x0b" + tag_name(name) + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def nbt_byte_array(name, data):
    return b"\x07" + tag_name(name) + struct.pack(">i", len(data)) + data


def nbt_compound(name, payload):
    return b"\x0a" + tag_name(name) + payload + b"\x00"


def varint(value):
    out = bytearray()
    while True:
        part = value & 0x7F
        value >>= 7
        if value:
            out.append(part | 0x80)
        else:
            out.append(part)
            return bytes(out)


palette = {"minecraft:air": 0}
data = bytearray()
for y in range(H):
    for z in range(L):
        for x in range(W):
            block = blocks.get((x, y, z), "minecraft:air")
            if block not in palette:
                palette[block] = len(palette)
            data += varint(palette[block])

palette_payload = b"".join(nbt_int(name, idx) for name, idx in palette.items())
body = (
    nbt_int("Version", 2)
    + nbt_int("DataVersion", DATA_VERSION)
    + nbt_short("Width", W)
    + nbt_short("Height", H)
    + nbt_short("Length", L)
    + nbt_int_array("Offset", [-ORIGIN[0], -ORIGIN[1], -ORIGIN[2]])
    + nbt_int("PaletteMax", len(palette))
    + nbt_compound("Palette", palette_payload)
    + nbt_byte_array("BlockData", bytes(data))
)
root = b"\x0a" + tag_name("Schematic") + body + b"\x00"

OUT.parent.mkdir(parents=True, exist_ok=True)
with gzip.open(OUT, "wb") as fh:
    fh.write(root)

print(f"{OUT}  {W}x{H}x{L}, blocks={len(blocks)}, palette={len(palette)}")
print(f"merchant at paste offset: dx={MERCHANT[0] - ORIGIN[0]} dy={MERCHANT[1] - ORIGIN[1]} dz={MERCHANT[2] - ORIGIN[2]}")
