"""Строит новый спаун AquaTech: портовый остров-хаб вокруг точки (0, 190, 0).

    python tools/build_spawn_hub.py

Результат:
  mods/aquatech-ui/art/spawn_hub.nbt    ванильная структура (ставится `/place template`, работает из консоли)
  mods/aquatech-ui/art/spawn_hub.schem  та же постройка для WorldEdit/FAWE (//schem load spawn_hub, //paste)

Мир: водяной слой Y175..189, палуба лежит на Y189 (верхний блок воды), игрок ходит по Y190.
Структура занимает X -22..22, Y 179..210, Z -24..22. Выше воды она всё очищает воздухом, так что старый спаун
исчезает. Ниже воды только сваи.

Вставка: `place template minecraft:aquatech_spawn -22 179 -24` (файл лежит в world/generated/minecraft/structures/).
Точки NPC (координаты мира): торговец рыбой (0.5, 190, 16.5) лицом на север, пчеловод (14.5, 190, 0.5) лицом на запад.
"""
import gzip
import math
import struct
from pathlib import Path

ART = Path(__file__).resolve().parent.parent / "mods" / "aquatech-ui" / "art"
DATA_VERSION = 3465  # Minecraft 1.20.1

X0, X1, Y0, Y1, Z0, Z1 = -22, 22, 179, 210, -24, 22
DECK = 189

B = {}          # (x, y, z) -> "minecraft:block[props]"
DECK_CELLS = set()


def put(x, y, z, state):
    assert X0 <= x <= X1 and Y0 <= y <= Y1 and Z0 <= z <= Z1, (x, y, z, state)
    B[(x, y, z)] = state if ":" in state else "minecraft:" + state


def fill(xa, xb, ya, yb, za, zb, state):
    for x in range(min(xa, xb), max(xa, xb) + 1):
        for y in range(min(ya, yb), max(ya, yb) + 1):
            for z in range(min(za, zb), max(za, zb) + 1):
                put(x, y, z, state)


def deck(x, z, state):
    put(x, DECK, z, state)
    DECK_CELLS.add((x, z))


PLANKS, DARK = "spruce_planks", "dark_oak_planks"
LOG_AXIS = lambda a: f"stripped_spruce_log[axis={a}]"


def stairs(x, y, z, facing, half="bottom", block="spruce_stairs"):
    put(x, y, z, f"{block}[facing={facing},half={half},shape=straight]")


def lamp_post(x, z, h=2):
    for k in range(h):
        put(x, DECK + 1 + k, z, "spruce_fence")
    put(x, DECK + 1 + h, z, "lantern[hanging=false]")


# ---------------------------------------------------------------------------------------------- plaza (круглая площадь)
R = 10.5
for x in range(-12, 13):
    for z in range(-12, 13):
        d = math.hypot(x, z)
        if d > R:
            continue
        if d <= 1.5:
            s = "smooth_stone"
        elif d <= 4.5:
            s = PLANKS
        elif d <= 5.7:
            s = DARK
        elif d <= 9.5:
            s = PLANKS if (x + z) % 4 else "spruce_planks"
        else:
            s = "stripped_spruce_wood[axis=y]"
        deck(x, z, s)
# компас: четыре луча из полированного андезита и резные блоки на концах
for t in range(2, 10):
    for (x, z) in ((t, 0), (-t, 0), (0, t), (0, -t)):
        deck(x, z, "polished_andesite")
for (x, z) in ((9, 0), (-9, 0), (0, 9), (0, -9)):
    deck(x, z, "chiseled_stone_bricks")
for (x, z) in ((1, 1), (1, -1), (-1, 1), (-1, -1), (1, 0), (-1, 0), (0, 1), (0, -1), (0, 0)):
    deck(x, z, "smooth_stone")
deck(0, 0, "polished_andesite")

# фонари на кольце площади
for k in range(8):
    a = math.radians(45 * k + 22.5)
    lx, lz = round(8.2 * math.cos(a)), round(8.2 * math.sin(a))
    lamp_post(lx, lz, 3)


# ---------------------------------------------------------------------------------------------- пирсы
def pier(axis, sign, t0, t1):
    """Пирс шириной 5 от плазы. axis 'z' идёт на север/юг, 'x' на запад/восток."""
    for t in range(t0, t1 + 1):
        for o in range(-2, 3):
            x, z = (o, sign * t) if axis == "z" else (sign * t, o)
            if abs(o) == 2:
                s = LOG_AXIS(axis)
            elif o == 0 and t % 5 == 0:
                s = DARK
            else:
                s = PLANKS
            deck(x, z, s)


pier("z", -1, 10, 14)
pier("z", 1, 10, 12)
pier("x", 1, 10, 12)
pier("x", -1, 10, 12)


def platform(xa, xb, za, zb, cut=2, base=PLANKS):
    """Прямоугольная площадка со срезанными углами и каймой из брёвен."""
    for x in range(xa, xb + 1):
        for z in range(za, zb + 1):
            dx = min(x - xa, xb - x)
            dz = min(z - za, zb - z)
            if dx + dz < cut:
                continue
            edge = dx == 0 or dz == 0
            axis = "x" if dz == 0 else "z"
            deck(x, z, LOG_AXIS(axis) if edge else base)


# ---------------------------------------------------------------------------------------------- маяк (север)
platform(-7, 7, -24, -13, cut=3)
TX, TZ = 0, -19


def ring(cx, cz, r_out, r_in):
    for x in range(cx - 6, cx + 7):
        for z in range(cz - 6, cz + 7):
            d = math.hypot(x - cx, z - cz)
            if r_in <= d <= r_out:
                yield x, z


for y in range(DECK + 1, DECK + 15):
    r_out = 3.6 if y <= DECK + 7 else 3.1
    r_in = r_out - 1.0
    band = (y - DECK - 1) // 2 % 2
    base = "stone_bricks" if y <= DECK + 2 else ("white_concrete" if band == 0 else "red_concrete")
    for x, z in ring(TX, TZ, r_out, r_in):
        put(x, y, z, base)
# крыльцо у основания и дверь с южной стороны
south = TZ + 3
for dz in (south, south + 1):
    put(TX, DECK + 1, dz, "air")
    put(TX, DECK + 2, dz, "air")
put(TX, DECK + 1, south, "spruce_door[facing=south,half=lower,hinge=left,open=false,powered=false]")
put(TX, DECK + 2, south, "spruce_door[facing=south,half=upper,hinge=left,open=false,powered=false]")
for dx in (-1, 1):
    put(TX + dx, DECK + 1, south + 1, "spruce_stairs[facing=south,half=bottom,shape=straight]")
put(TX, DECK + 1, south + 1, "stone_brick_slab[type=bottom]")
# окна и лестница внутри
for (wx, wz) in ((TX - 3, TZ), (TX + 3, TZ), (TX, TZ - 3)):
    put(wx, DECK + 5, wz, "glass_pane")
    put(wx, DECK + 10, wz, "glass_pane")
for y in range(DECK + 1, DECK + 15):
    put(TX, y, TZ - 2, "ladder[facing=south]")
# галерея и фонарная комната
for x, z in ring(TX, TZ, 4.6, 0):
    d = math.hypot(x - TX, z - TZ)
    if (x, z) != (TX, TZ - 2):
        put(x, DECK + 15, z, "stone_brick_slab[type=bottom]" if d > 3.2 else "stone_bricks")
for x, z in ring(TX, TZ, 4.6, 4.0):
    put(x, DECK + 16, z, "spruce_fence")
for x, z in ring(TX, TZ, 2.6, 1.6):
    put(x, DECK + 16, z, "glass")
    put(x, DECK + 17, z, "glass")
put(TX, DECK + 16, TZ, "sea_lantern")
put(TX, DECK + 17, TZ, "sea_lantern")
for x, z in ring(TX, TZ, 3.4, 0):
    put(x, DECK + 18, z, "stone_brick_slab[type=bottom]")
for x, z in ring(TX, TZ, 2.3, 0):
    put(x, DECK + 19, z, "red_concrete")
for x, z in ring(TX, TZ, 1.2, 0):
    put(x, DECK + 20, z, "red_concrete")
put(TX, DECK + 21, TZ, "lightning_rod[facing=up,powered=false,waterlogged=false]")
# фонари у входа
put(TX - 2, DECK + 1, south + 2, "spruce_fence")
put(TX - 2, DECK + 2, south + 2, "lantern[hanging=false]")
put(TX + 2, DECK + 1, south + 2, "spruce_fence")
put(TX + 2, DECK + 2, south + 2, "lantern[hanging=false]")
for (bx, bz) in ((-5, -15), (-5, -16), (5, -15)):
    put(bx, DECK + 1, bz, "barrel[facing=up]")

# ---------------------------------------------------------------------------------------------- рыбный рынок (юг)
platform(-8, 8, 11, 21, cut=3)
fill(-5, 5, DECK, DECK, 13, 20, DARK)
for x in range(-5, 6):
    DECK_CELLS.add((x, 13))
for px, pz in ((-5, 13), (5, 13), (-5, 20), (5, 20)):
    fill(px, px, DECK + 1, DECK + 4, pz, pz, LOG_AXIS("y"))
# крыша-навес в полоску
for x in range(-6, 7):
    stripe = "white_concrete" if x % 2 == 0 else "blue_concrete"
    for z in range(12, 22):
        put(x, DECK + 5, z, stripe)
    put(x, DECK + 4, 12, "blue_wool" if x % 2 else "white_wool")
    put(x, DECK + 6, 12, "spruce_slab[type=bottom]")
    put(x, DECK + 6, 21, "spruce_slab[type=bottom]")
for z in range(12, 22):
    put(-7, DECK + 5, z, "spruce_slab[type=top]")
    put(7, DECK + 5, z, "spruce_slab[type=top]")
# прилавок, торговец стоит за ним на (0.5, 190, 16.5)
for x in range(-4, 5):
    put(x, DECK + 1, 15, LOG_AXIS("x"))
    put(x, DECK + 2, 15, "spruce_pressure_plate" if x in (-2, 0, 2) else "air")
for x, s in ((-3, "lantern[hanging=false]"), (3, "lantern[hanging=false]"), (-1, "sea_pickle[pickles=4,waterlogged=false]"),
             (1, "sea_pickle[pickles=3,waterlogged=false]")):
    put(x, DECK + 2, 15, s)
put(-4, DECK + 2, 15, "water_cauldron[level=3]")
put(4, DECK + 2, 15, "water_cauldron[level=3]")
# задняя стена: стеллажи с бочками и сушёной рыбой
fill(-5, 5, DECK + 1, DECK + 3, 20, 20, PLANKS)
for x in range(-4, 5, 2):
    put(x, DECK + 1, 19, "barrel[facing=up]")
    put(x, DECK + 2, 19, "dried_kelp_block")
put(0, DECK + 1, 19, "air")
put(0, DECK + 2, 19, "air")
put(-4, DECK + 3, 19, "lantern[hanging=true]")
put(4, DECK + 3, 19, "lantern[hanging=true]")
put(0, DECK + 4, 15, "lantern[hanging=true]")
for x in range(-1, 2):
    for z in (16, 17, 18):
        put(x, DECK + 1, z, "cyan_carpet")
# ящики на причале
for pos in ((-7, 13), (-7, 14), (-6, 14), (7, 13), (7, 14)):
    put(pos[0], DECK + 1, pos[1], "barrel[facing=up]")
put(-7, DECK + 2, 13, "lantern[hanging=false]")
put(7, DECK + 2, 13, "lantern[hanging=false]")

# ---------------------------------------------------------------------------------------------- пасека (восток)
platform(11, 22, -8, 8, cut=3)
fill(12, 21, DECK, DECK, -6, 6, PLANKS)
for x in range(12, 22):
    for z in range(-6, 7):
        DECK_CELLS.add((x, z))
# навес в жёлто-синюю полоску над пчеловодом (14.5, 190, 0.5), модель пчеловода стоит справа от него (на севере)
for z in range(-5, 6):
    stripe = "yellow_concrete" if z % 2 == 0 else "blue_concrete"
    for x in range(12, 19):
        put(x, DECK + 5, z, stripe)
for px, pz in ((12, -5), (18, -5), (12, 5), (18, 5)):
    fill(px, px, DECK + 1, DECK + 4, pz, pz, LOG_AXIS("y"))
for x in range(12, 19):
    put(x, DECK + 6, -6, "spruce_slab[type=bottom]")
    put(x, DECK + 6, 6, "spruce_slab[type=bottom]")
put(15, DECK + 4, -5, "lantern[hanging=true]")
put(15, DECK + 4, 5, "lantern[hanging=true]")
put(15, DECK + 4, 0, "lantern[hanging=true]")
# улья на столбах вдоль восточного края
for z in (-5, -3, 1, 3, 5):
    put(21, DECK + 1, z, LOG_AXIS("y"))
    put(21, DECK + 2, z, "beehive[facing=west,honey_level=%d]" % (5 if z in (-3, 3) else 2))
    put(20, DECK + 1, z, "moss_block")
    put(20, DECK + 2, z, ["dandelion", "azure_bluet", "cornflower", "oxeye_daisy", "allium"][(z + 5) % 5])
put(21, DECK + 1, -1, "honeycomb_block")
put(21, DECK + 1, 0, "honey_block")
put(21, DECK + 1, 1, "honeycomb_block")
# клумбы в углах и скамьи
for x in (19, 20):
    for z in (-7, 7):
        put(x, DECK + 1, z, "moss_block")
        put(x, DECK + 2, z, "azalea")
for z in (-3, 3):
    stairs(16, DECK + 1, z, "west")
for (x, z) in ((11, -7), (11, 7)):
    lamp_post(x, z, 2)

# ---------------------------------------------------------------------------------------------- портовая контора (запад)
platform(-22, -11, -8, 8, cut=3)
fill(-20, -12, DECK, DECK, -5, 5, DARK)
for x in range(-20, -11):
    for z in range(-5, 6):
        DECK_CELLS.add((x, z))
CX0, CX1, CZ0, CZ1 = -20, -14, -4, 4
fill(CX0, CX1, DECK + 1, DECK + 4, CZ0, CZ0, PLANKS)
fill(CX0, CX1, DECK + 1, DECK + 4, CZ1, CZ1, PLANKS)
fill(CX0, CX0, DECK + 1, DECK + 4, CZ0, CZ1, PLANKS)
fill(CX1, CX1, DECK + 1, DECK + 4, CZ0, CZ1, PLANKS)
for (x, z) in ((CX0, CZ0), (CX1, CZ0), (CX0, CZ1), (CX1, CZ1)):
    fill(x, x, DECK + 1, DECK + 4, z, z, LOG_AXIS("y"))
# дверь с восточной стороны (к площади), окна по бокам
for dy in (1, 2):
    put(CX1, DECK + dy, 0, "air")
put(CX1, DECK + 1, 0, "spruce_door[facing=east,half=lower,hinge=left,open=false,powered=false]")
put(CX1, DECK + 2, 0, "spruce_door[facing=east,half=upper,hinge=left,open=false,powered=false]")
for z in (-2, 2):
    put(CX1, DECK + 2, z, "glass_pane")
for x in (-18, -16):
    put(x, DECK + 2, CZ0, "glass_pane")
    put(x, DECK + 2, CZ1, "glass_pane")
put(CX0, DECK + 2, 0, "glass_pane")
# двускатная крыша с коньком вдоль Z: ступени с обеих сторон, под ними доски
for z in range(CZ0 - 1, CZ1 + 2):
    for step, (xw, xe) in enumerate(((-21, -13), (-20, -14), (-19, -15), (-18, -16))):
        stairs(xw, DECK + 4 + step, z, "west")
        stairs(xe, DECK + 4 + step, z, "east")
    put(-17, DECK + 7, z, PLANKS)
for z in range(CZ0, CZ1 + 1):
    fill(-19, -15, DECK + 5, DECK + 5, z, z, PLANKS)
    fill(-18, -16, DECK + 6, DECK + 6, z, z, PLANKS)
for z in (CZ0, CZ1):
    fill(-19, -15, DECK + 5, DECK + 5, z, z, PLANKS)
    fill(-18, -16, DECK + 6, DECK + 6, z, z, PLANKS)
# внутри: кафедра, полки, бочки, фонарь
put(-19, DECK + 1, 0, "lectern[facing=east,has_book=false,powered=false]")
for z in (-3, -2, -1, 1, 2, 3):
    put(-19, DECK + 1, z, "bookshelf")
    put(-19, DECK + 2, z, "bookshelf")
put(-16, DECK + 1, -3, "barrel[facing=up]")
put(-16, DECK + 1, 3, "barrel[facing=up]")
put(-16, DECK + 2, 3, "lantern[hanging=false]")
put(-16, DECK + 4, 0, "lantern[hanging=true]")
for x in range(-18, -15):
    for z in (-1, 0, 1):
        put(x, DECK + 1, z, "red_carpet")
# колокол и объявления снаружи
put(-12, DECK + 1, -3, "spruce_fence")
put(-12, DECK + 2, -3, "spruce_fence")
put(-12, DECK + 3, -3, "spruce_fence")
put(-11, DECK + 3, -3, "spruce_fence")
put(-11, DECK + 2, -3, "bell[attachment=ceiling,facing=north,powered=false]")
put(-12, DECK + 1, 3, "barrel[facing=up]")
put(-12, DECK + 1, 4, "barrel[facing=up]")
put(-12, DECK + 2, 4, "lantern[hanging=false]")

# ---------------------------------------------------------------------------------------------- перила, фонари, сваи
# арки на входе в каждый пирс: два столба, балка и фонарь
for sx, sz, axis in ((0, -11, "x"), (0, 11, "x"), (-11, 0, "z"), (11, 0, "z")):
    for o in (-2, 2):
        px, pz = (o, sz) if axis == "x" else (sx, o)
        fill(px, px, DECK + 1, DECK + 4, pz, pz, LOG_AXIS("y"))
    for o in range(-2, 3):
        px, pz = (o, sz) if axis == "x" else (sx, o)
        put(px, DECK + 5, pz, LOG_AXIS(axis))
    lx, lz = (0, sz) if axis == "x" else (sx, 0)
    put(lx, DECK + 4, lz, "lantern[hanging=true]")
    put(lx, DECK + 6, lz, "spruce_slab[type=bottom]")

# клумбы на площади
for fx, fz in ((5, 5), (-5, 5), (5, -5), (-5, -5)):
    for dx in (0, 1):
        for dz in (0, 1):
            x, z = fx - dx * (1 if fx > 0 else -1), fz - dz * (1 if fz > 0 else -1)
            put(x, DECK + 1, z, "moss_block")
            put(x, DECK + 2, z, ("flowering_azalea", "azalea", "cornflower", "dandelion")[(dx + 2 * dz) % 4])

# сушилка для рыбы на рынке
for z in range(16, 20):
    put(-7, DECK + 1, z, "spruce_fence")
    put(-7, DECK + 2, z, "spruce_fence")
for z in (17, 18):
    put(-7, DECK + 1, z, "dried_kelp_block")

# перила на пирсах там, где нет площадок
for t in range(11, 14):
    for o in (-2, 2):
        if (o, -t) in DECK_CELLS:
            put(o, DECK + 1, -t, "spruce_fence")
for x, z in ((-2, -12), (2, -12), (-2, 11), (2, 11), (11, -2), (11, 2), (-11, -2), (-11, 2)):
    if (x, z) in DECK_CELLS:
        lamp_post(x, z, 2)

# сваи под палубой
for (x, z) in sorted(DECK_CELLS):
    if x % 3 == 0 and z % 3 == 0:
        for y in range(180, DECK):
            put(x, y, z, "spruce_log[axis=y]")
# под башней опора плотнее
for x, z in ring(TX, TZ, 3.6, 0):
    put(x, DECK - 1, z, "stone_bricks")
    put(x, DECK - 2, z, "stone_bricks")

# ---------------------------------------------------------------------------------------------- связность заборов и стёкол
def connects(name):
    return any(name.startswith("minecraft:" + k) for k in (
        "spruce_fence", "spruce_planks", "stripped_spruce_log", "spruce_log", "barrel", "dark_oak_planks",
        "stone_bricks", "white_concrete", "red_concrete", "bookshelf", "blue_concrete", "yellow_concrete"))


for pos, state in list(B.items()):
    base = state.split("[")[0]
    if base not in ("minecraft:spruce_fence", "minecraft:glass_pane"):
        continue
    x, y, z = pos
    props = []
    for key, (dx, dz) in (("north", (0, -1)), ("south", (0, 1)), ("east", (1, 0)), ("west", (-1, 0))):
        other = B.get((x + dx, y, z + dz))
        if other and (other.split("[")[0] == base or connects(other)):
            props.append(f"{key}=true")
    B[pos] = base + (f"[{','.join(props)}]" if props else "")

# ---------------------------------------------------------------------------------------------- воздух выше воды, вода на месте старой палубы
for x in range(X0, X1 + 1):
    for z in range(Z0, Z1 + 1):
        for y in range(DECK + 1, Y1 + 1):
            B.setdefault((x, y, z), "minecraft:air")
for x in range(-8, 9):
    for z in range(-8, 15):
        if (x, z) not in DECK_CELLS:
            B.setdefault((x, DECK, z), "minecraft:water[level=0]")


# ---------------------------------------------------------------------------------------------- запись
def tag_name(name):
    raw = name.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def t_int(v):
    return struct.pack(">i", v)


def t_str(v):
    raw = v.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def parse_state(state):
    if "[" not in state:
        return state, {}
    name, rest = state.split("[", 1)
    props = dict(kv.split("=", 1) for kv in rest.rstrip("]").split(","))
    return name, props


palette, blocks = {}, []
for (x, y, z), state in sorted(B.items()):
    if state not in palette:
        palette[state] = len(palette)
    blocks.append(((x - X0, y - Y0, z - Z0), palette[state]))

pal_payload = b""
for state, _ in sorted(palette.items(), key=lambda kv: kv[1]):
    name, props = parse_state(state)
    entry = b"\x08" + tag_name("Name") + t_str(name)
    if props:
        entry += b"\x0a" + tag_name("Properties") + b"".join(b"\x08" + tag_name(k) + t_str(v) for k, v in props.items()) + b"\x00"
    pal_payload += entry + b"\x00"

blk_payload = b""
for (px, py, pz), idx in blocks:
    blk_payload += b"\x03" + tag_name("state") + t_int(idx)
    blk_payload += b"\x09" + tag_name("pos") + b"\x03" + struct.pack(">i", 3) + t_int(px) + t_int(py) + t_int(pz)
    blk_payload += b"\x00"

SIZE = (X1 - X0 + 1, Y1 - Y0 + 1, Z1 - Z0 + 1)
root = (
    b"\x0a" + tag_name("")
    + b"\x03" + tag_name("DataVersion") + t_int(DATA_VERSION)
    + b"\x09" + tag_name("size") + b"\x03" + struct.pack(">i", 3) + b"".join(t_int(v) for v in SIZE)
    + b"\x09" + tag_name("entities") + b"\x0a" + struct.pack(">i", 0)
    + b"\x09" + tag_name("palette") + b"\x0a" + struct.pack(">i", len(palette)) + pal_payload
    + b"\x09" + tag_name("blocks") + b"\x0a" + struct.pack(">i", len(blocks)) + blk_payload
    + b"\x00"
)
ART.mkdir(parents=True, exist_ok=True)
with gzip.open(ART / "spawn_hub.nbt", "wb") as fh:
    fh.write(root)


# Sponge schematic v2 для FAWE
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


spal = {"minecraft:air": 0}
data = bytearray()
for y in range(SIZE[1]):
    for z in range(SIZE[2]):
        for x in range(SIZE[0]):
            state = B.get((x + X0, y + Y0, z + Z0), "minecraft:water[level=0]" if y + Y0 <= DECK else "minecraft:air")
            if (x + X0, y + Y0, z + Z0) not in B and y + Y0 <= DECK:
                state = "minecraft:air"  # ниже палубы вне построек оставляем воду нетронутой (FAWE -a пропускает воздух)
            if state not in spal:
                spal[state] = len(spal)
            data += varint(spal[state])
pp = b"".join(b"\x03" + tag_name(k) + t_int(v) for k, v in spal.items())
body = (
    b"\x03" + tag_name("Version") + t_int(2) + b"\x03" + tag_name("DataVersion") + t_int(DATA_VERSION)
    + b"\x02" + tag_name("Width") + struct.pack(">h", SIZE[0]) + b"\x02" + tag_name("Height") + struct.pack(">h", SIZE[1])
    + b"\x02" + tag_name("Length") + struct.pack(">h", SIZE[2])
    + b"\x0b" + tag_name("Offset") + struct.pack(">i", 3) + t_int(-X0) + t_int(-DECK) + t_int(-Z0)
    + b"\x03" + tag_name("PaletteMax") + t_int(len(spal))
    + b"\x0a" + tag_name("Palette") + pp + b"\x00"
    + b"\x07" + tag_name("BlockData") + struct.pack(">i", len(data)) + bytes(data)
)
with gzip.open(ART / "spawn_hub.schem", "wb") as fh:
    fh.write(b"\x0a" + tag_name("Schematic") + body + b"\x00")

print(f"blocks {len(B)}, solid {sum(1 for s in B.values() if s != 'minecraft:air')}, palette {len(palette)}, size {SIZE}")
