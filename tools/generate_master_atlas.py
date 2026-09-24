"""Generate clean 64x64 atlas by properly downsampling 64x64 master textures."""
from pathlib import Path
from PIL import Image

def build_atlas():
    out_dir = Path("scratch/coral_colossus")
    tex_dir = out_dir / "extracted_textures"

    # All source textures are 64x64
    armor_64 = Image.open(tex_dir / "armor.png").convert("RGBA")
    steel_64 = Image.open(tex_dir / "steel.png").convert("RGBA")
    core_64 = Image.open(tex_dir / "core.png").convert("RGBA")
    coral_red_64 = Image.open(tex_dir / "coral_red.png").convert("RGBA")
    coral_yellow_64 = Image.open(tex_dir / "coral_yellow.png").convert("RGBA")

    atlas = Image.new("RGBA", (64, 64), (0, 0, 0, 255))
    emissive = Image.new("RGBA", (64, 64), (0, 0, 0, 255))

    # 1. Top-Left 32x32: armor (resampled NEAREST)
    armor_32 = armor_64.resize((32, 32), Image.Resampling.NEAREST)
    atlas.paste(armor_32, (0, 0))

    # 2. Top-Right 32x32: steel (resampled NEAREST)
    steel_32 = steel_64.resize((32, 32), Image.Resampling.NEAREST)
    atlas.paste(steel_32, (32, 0))

    # 3. Bottom-Left 32x32: core (resampled NEAREST)
    core_32 = core_64.resize((32, 32), Image.Resampling.NEAREST)
    atlas.paste(core_32, (0, 32))

    # 4. Bottom-Right:
    # (32, 32) 16x16: coral_red
    c_red_16 = coral_red_64.resize((16, 16), Image.Resampling.NEAREST)
    atlas.paste(c_red_16, (32, 32))

    # (48, 32) 16x16: coral_yellow
    c_yel_16 = coral_yellow_64.resize((16, 16), Image.Resampling.NEAREST)
    atlas.paste(c_yel_16, (48, 32))

    # (32, 48) 16x16: dark prismarine waist band
    prismarine = Image.new("RGBA", (16, 16), (28, 74, 71, 255))
    pr_px = prismarine.load()
    c_pri_light = (54, 118, 107, 255)
    c_pri_dark = (19, 52, 50, 255)
    for y in range(16):
        for x in range(16):
            if (x + y * 3) % 4 == 0:
                pr_px[x, y] = c_pri_light
            elif (x * 2 + y) % 5 == 0:
                pr_px[x, y] = c_pri_dark
            if x == 0 or y == 0:
                pr_px[x, y] = (65, 142, 130, 255)
            elif x == 15 or y == 15:
                pr_px[x, y] = (14, 38, 36, 255)
    atlas.paste(prismarine, (32, 48))

    # (48, 48) 16x16: deepslate medallion
    slate = Image.new("RGBA", (16, 16), (45, 48, 54, 255))
    sl_px = slate.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            dist = (dx*dx + dy*dy) ** 0.5
            if dist > 7:
                sl_px[x, y] = (25, 26, 30, 255)
            elif dist > 5.5:
                sl_px[x, y] = (75, 80, 90, 255)
            elif dist > 3.5:
                sl_px[x, y] = (35, 38, 44, 255)
            elif dist > 1.5:
                sl_px[x, y] = (55, 60, 68, 255)
            else:
                sl_px[x, y] = (85, 92, 105, 255)
    atlas.paste(slate, (48, 48))

    # EMISSIVE GLOW MAP:
    at_px = atlas.load()
    em_px = emissive.load()
    for y in range(64):
        for x in range(64):
            r, g, b, a = at_px[x, y]
            # Cyan glow detection: high cyan/blue/green and lower red
            if b > 110 and g > 110 and (b > r + 30 or g > r + 30) and r < 140:
                em_px[x, y] = (255, 255, 255, 255)
            else:
                em_px[x, y] = (0, 0, 0, 255)

    atlas_path = out_dir / "coral_colossus_atlas.png"
    emissive_path = out_dir / "coral_colossus_emissive.png"
    atlas.save(atlas_path)
    emissive.save(emissive_path)
    print(f"Saved {atlas_path} and {emissive_path}")

if __name__ == "__main__":
    build_atlas()
