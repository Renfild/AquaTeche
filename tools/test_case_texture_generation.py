import io, json, os, zipfile, base64
from PIL import Image, ImageEnhance

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

IU_JAR = os.path.join(ROOT, 'server/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar')
AV_JAR = os.path.join(ROOT, 'server/mods/Re-Avaritia-forge-1.20.1-1.4.1-release.jar')
DE_JAR = os.path.join(ROOT, 'server/mods/Draconic-Evolution-1.20.1-3.1.2.621-universal.jar')
VAN_JAR = os.path.join(os.path.expanduser('~'), '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar')

def read_jar_frame0(jar_path, inner_path):
    with zipfile.ZipFile(jar_path, 'r') as zf:
        raw = zf.read(inner_path)
        im = Image.open(io.BytesIO(raw)).convert('RGBA')
        w, h = im.size
        if h > w:
            im = im.crop((0, 0, w, w))
        return im

def iso_cube(top, left, right=None, size=64):
    if right is None:
        right = left
    top_16 = top.resize((16, 16), Image.NEAREST)
    left_16 = left.resize((16, 16), Image.NEAREST)
    right_16 = right.resize((16, 16), Image.NEAREST)
    
    top_f = top_16.transform((64, 32), Image.AFFINE, (0.25, -0.5, 8, 0.25, 0.5, -8), resample=Image.NEAREST)
    left_f = left_16.transform((32, 48), Image.AFFINE, (0.5, 0, 0, -0.25, 0.5, 0), resample=Image.NEAREST)
    right_f = right_16.transform((32, 48), Image.AFFINE, (0.5, 0, 0, 0.25, 0.5, -8), resample=Image.NEAREST)
    
    def shade(img, k):
        px = img.load()
        for y in range(img.height):
            for x in range(img.width):
                r, g, b, a = px[x, y]
                if a:
                    px[x, y] = (int(r * k), int(g * k), int(b * k), a)
        return img
    
    out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    out.alpha_composite(top_f, (0, 0))
    out.alpha_composite(shade(left_f, 0.85), (0, 16))
    out.alpha_composite(shade(right_f, 0.65), (32, 16))
    return out

# 1. Extreme Crafting Table
ext_top = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/block/machine/craft/extreme_top.png')
ext_side = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/block/machine/craft/extreme_side.png')
ext_table = iso_cube(ext_top, ext_side)
ext_table.save('scratch/out_extreme_table.png')

# 2. Neutron Compressor
comp_front = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/block/machine/compressor/compressor_front.png')
comp_side = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/block/machine/compressor/compressor_side_left.png')
comp_top = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/block/machine/compressor/compressor_top.png')
compressor = iso_cube(comp_top, comp_front, comp_side)
compressor.save('scratch/out_compressor.png')

# 3. Infinity Catalyst (16x16 scaled to 48x48)
inf_cat = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/item/resource/infinity/infinity_catalyst.png')
inf_cat_48 = inf_cat.resize((48, 48), Image.NEAREST)
inf_cat_48.save('scratch/out_infinity_catalyst.png')

# 4. Neutron Ingot (16x16 scaled to 48x48)
neut_ingot = read_jar_frame0(AV_JAR, 'assets/avaritia/textures/item/resource/neutron/neutron_ingot.png')
neut_ingot_48 = neut_ingot.resize((48, 48), Image.NEAREST)
neut_ingot_48.save('scratch/out_neutron_ingot.png')

# 5. Dragon Heart (16x16 scaled to 48x48)
drag_heart = read_jar_frame0(DE_JAR, 'assets/draconicevolution/textures/item/components/dragon_heart.png')
drag_heart_48 = drag_heart.resize((48, 48), Image.NEAREST)
drag_heart_48.save('scratch/out_dragon_heart.png')

# 6. Basic Crafting Injector
inj_base = read_jar_frame0(DE_JAR, 'assets/draconicevolution/textures/block/crafting/injector_base.png')
with zipfile.ZipFile(DE_JAR, 'r') as zf:
    core_raw = Image.open(io.BytesIO(zf.read('assets/draconicevolution/textures/block/crafting/injector_core_draconium.png'))).convert('L')
core = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
core_px = core.load()
for y in range(16):
    for x in range(16):
        v = core_raw.getpixel((x, y))
        if v > 10:
            core_px[x, y] = (int(0x93 * v / 255), int(0x33 * v / 255), int(0xea * v / 255), 255)
inj_side = inj_base.copy()
inj_side.alpha_composite(core.resize((12, 8), Image.NEAREST), (2, 4))
inj_top = inj_base.copy()
inj_top.alpha_composite(core.resize((8, 8), Image.NEAREST), (4, 4))
injector = iso_cube(inj_top, inj_side)
injector.save('scratch/out_injector.png')

# 7. Graviton Solar Panel
grav_side = read_jar_frame0(IU_JAR, 'assets/industrialupgrade/textures/block/solar_panels/graviton/side.png')
grav_top = read_jar_frame0(IU_JAR, 'assets/industrialupgrade/textures/block/solar_panels/graviton/glass.png')
grav_panel = iso_cube(grav_top, grav_side)
grav_panel.save('scratch/out_graviton_panel.png')

# 8. Hadron Solar Panel
hadr_side = read_jar_frame0(IU_JAR, 'assets/industrialupgrade/textures/block/solar_panels/hadron/side.png')
hadr_top = read_jar_frame0(IU_JAR, 'assets/industrialupgrade/textures/block/solar_panels/hadron/glass.png')
hadr_panel = iso_cube(hadr_top, hadr_side)
hadr_panel.save('scratch/out_hadron_panel.png')

# 9. Dragon Egg 3D
with open('scratch/test_dragon_egg.png', 'rb') as f:
    egg = Image.open(f).convert('RGBA')
egg.save('scratch/out_dragon_egg.png')

print('All 9 test textures generated successfully!')
