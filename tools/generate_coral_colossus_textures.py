"""Generate pixel-perfect textures for Coral Colossus matching reference screenshots."""
import os
from PIL import Image

def hex_to_rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def make_atlas():
    w, h = 64, 64
    atlas = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 255))

    pixels = atlas.load()
    glow_pixels = glow.load()

    # Copper palette matching screenshots (warm, cut copper with patina flecks and bevels)
    c_cop_base = hex_to_rgb("#b56247")
    c_cop_light = hex_to_rgb("#cf7e63")
    c_cop_high = hex_to_rgb("#e2957b")
    c_cop_dark = hex_to_rgb("#853922")
    c_cop_deep = hex_to_rgb("#5e2313")
    c_patina = hex_to_rgb("#4e8f7e")

    # Deepslate palette
    c_ds_base = hex_to_rgb("#2d3036")
    c_ds_light = hex_to_rgb("#40444d")
    c_ds_dark = hex_to_rgb("#1c1d22")
    c_ds_deep = hex_to_rgb("#121316")

    # 1. Tile (0, 0) [0..15, 0..15]: COPPER_ARMOR (primary warm copper with bevel and seams)
    for y in range(16):
        for x in range(16):
            c = c_cop_base
            if (x * 3 + y * 7) % 7 == 0:
                c = c_cop_light
            elif (x * 5 + y * 11) % 6 == 0:
                c = c_cop_dark
            if (x in (3, 11) and y in (4, 12)):
                c = c_patina # occasional patina fleck

            # Bevel borders
            if x == 0 or y == 0:
                c = c_cop_high
            elif x == 15 or y == 15:
                c = c_cop_deep
            # Panel seam groove
            if x in (7, 8) or y in (7, 8):
                c = c_cop_deep
            # Rivets
            if (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
                c = c_cop_high
            pixels[x, y] = (*c, 255)

    # 2. Tile (1, 0) [16..31, 0..15]: COPPER_PLATES (smooth outer plates with bevel)
    for y in range(16):
        for x in range(16):
            px, py = 16 + x, y
            c = c_cop_base
            if (x + y * 2) % 5 == 0:
                c = c_cop_light
            elif (x * 2 + y) % 7 == 0:
                c = c_cop_dark
            if x == 0 or y == 0 or x == 1 or y == 1:
                c = c_cop_high
            elif x == 15 or y == 15 or x == 14 or y == 14:
                c = c_cop_dark
            pixels[px, py] = (*c, 255)

    # 3. Tile (2, 0) [32..47, 0..15]: DEEPSLATE (chiseled dark charcoal slate stone)
    for y in range(16):
        for x in range(16):
            px, py = 32 + x, y
            c = c_ds_base
            if y % 4 == 0:
                c = c_ds_dark
            elif y % 4 == 1 and x % 2 == 0:
                c = c_ds_light
            elif (x + y * 3) % 7 == 0:
                c = c_ds_deep
            if x == 0 or y == 0:
                c = c_ds_light
            elif x == 15 or y == 15:
                c = c_ds_deep
            pixels[px, py] = (*c, 255)

    # 4. Tile (3, 0) [48..63, 0..15]: DEEPSLATE_JOINT (mechanical circular / pivot joint)
    for y in range(16):
        for x in range(16):
            px, py = 48 + x, y
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx*dx + dy*dy) ** 0.5
            if dist > 7:
                c = c_cop_deep
            elif dist > 5.5:
                c = hex_to_rgb("#666b7a") # metallic rim
            elif dist > 3.5:
                c = c_ds_dark
            elif dist > 1.5:
                c = c_cop_base # center copper hub
            else:
                c = c_cop_high
            pixels[px, py] = (*c, 255)

    # 5. Tile (0, 1) [0..15, 16..31]: HEAD_FACE (copper helm with two cyan eye slits)
    for y in range(16):
        for x in range(16):
            px, py = x, 16 + y
            c = c_cop_base
            # Eyes at y=6,7, x=3..5 and x=10..12
            if y in (6, 7) and ((3 <= x <= 5) or (10 <= x <= 12)):
                c = hex_to_rgb("#00ffff")
                if y == 6 and x in (4, 11):
                    c = hex_to_rgb("#ffffff")
                glow_pixels[px, py] = (*c, 255)
            elif y in (5, 8) and (2 <= x <= 13):
                c = c_cop_deep # brow and eye socket shadow
            elif x in (6, 7, 8, 9) and 4 <= y <= 11:
                c = c_cop_light # nose bridge
            elif y >= 12:
                c = c_cop_dark # chin guard
            pixels[px, py] = (*c, 255)

    # 6. Tile (1, 1) [16..31, 16..31]: TORSO_MEDALLION (grey/deepslate circular chest medallion)
    for y in range(16):
        for x in range(16):
            px, py = 16 + x, 16 + y
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx*dx + dy*dy) ** 0.5
            if dist > 7.5:
                c = c_cop_base
            elif dist > 6.0:
                c = c_cop_deep # recessed groove
            elif dist > 4.5:
                c = c_ds_light # outer grey stone disc
            elif dist > 2.5:
                c = c_ds_base # textured slate
            else:
                c = c_ds_dark # center boss
            pixels[px, py] = (*c, 255)

    # 7. Tile (2, 1) [32..47, 16..31]: CHEST_PLATE (copper breastplate with bolts)
    for y in range(16):
        for x in range(16):
            px, py = 32 + x, 16 + y
            c = c_cop_base
            if x == 0 or y == 0:
                c = c_cop_high
            elif x == 15 or y == 15:
                c = c_cop_deep
            if (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
                c = c_cop_high
            pixels[px, py] = (*c, 255)

    # 8. Tile (3, 1) [48..63, 16..31]: PRISMARINE_TRIM (dark teal prismarine inlay)
    c_pr_base = hex_to_rgb("#3a7c6f")
    c_pr_light = hex_to_rgb("#519e8e")
    c_pr_dark = hex_to_rgb("#23544a")
    for y in range(16):
        for x in range(16):
            px, py = 48 + x, 16 + y
            c = c_pr_base
            if (x + y) % 3 == 0:
                c = c_pr_light
            elif (x * 2 + y) % 5 == 0:
                c = c_pr_dark
            if x == 0 or y == 0:
                c = c_pr_light
            elif x == 15 or y == 15:
                c = c_pr_dark
            pixels[px, py] = (*c, 255)

    # 9. Tile (0, 2) [0..15, 32..47]: CANNON_MUZZLE (glowing cyan forearm cannon bore)
    for y in range(16):
        for x in range(16):
            px, py = x, 32 + y
            dx = x - 7.5
            dy = y - 7.5
            dist = (dx*dx + dy*dy) ** 0.5
            if dist > 7:
                c = c_cop_deep # outer copper bezel
            elif dist > 5.5:
                c = c_ds_dark # recessed collar
            elif dist > 4.0:
                c = hex_to_rgb("#0077b6") # dark cyan edge
                glow_pixels[px, py] = (*c, 255)
            elif dist > 2.0:
                c = hex_to_rgb("#00ffff") # bright glowing cannon bore
                glow_pixels[px, py] = (*c, 255)
            else:
                c = hex_to_rgb("#e0ffff") # super-charged white center
                glow_pixels[px, py] = (*c, 255)
            pixels[px, py] = (*c, 255)

    # 10. Tile (1, 2) [16..31, 32..47]: BACK_REACTOR (copper vent with square glowing cyan vent)
    for y in range(16):
        for x in range(16):
            px, py = 16 + x, 32 + y
            c = c_cop_base
            # Square glowing vent at x=5..10, y=5..10
            if 5 <= x <= 10 and 5 <= y <= 10:
                c = hex_to_rgb("#00ffff")
                if 6 <= x <= 9 and 6 <= y <= 9:
                    c = hex_to_rgb("#e0ffff")
                glow_pixels[px, py] = (*c, 255)
            elif 4 <= x <= 11 and 4 <= y <= 11:
                c = c_cop_deep # recessed border
            pixels[px, py] = (*c, 255)

    # 11. Tile (2, 2) [32..47, 32..47]: GEM_CYAN (faceted glowing cyan crystal)
    for y in range(16):
        for x in range(16):
            px, py = 32 + x, 32 + y
            dx = abs(x - 7.5)
            dy = abs(y - 7.5)
            dist = dx + dy
            if dist > 8:
                c = hex_to_rgb("#004d40")
            elif dist > 5:
                c = hex_to_rgb("#0097a7")
            elif dist > 3:
                c = hex_to_rgb("#00ffff")
            elif dist > 1:
                c = hex_to_rgb("#80ffff")
            else:
                c = hex_to_rgb("#ffffff")
            glow_pixels[px, py] = (*c, 255)
            pixels[px, py] = (*c, 255)

    # 12. Tile (3, 2) [48..63, 32..47]: SPINE_CORE (dark mechanical spine with cyan nodes)
    for y in range(16):
        for x in range(16):
            px, py = 48 + x, 32 + y
            c = c_ds_dark
            if x in (2, 3, 12, 13):
                c = c_ds_light
            if 6 <= x <= 9 and ((2 <= y <= 4) or (7 <= y <= 9) or (12 <= y <= 14)):
                c = hex_to_rgb("#00ffff")
                glow_pixels[px, py] = (*c, 255)
            pixels[px, py] = (*c, 255)

    # 13. Tile (0, 3) [0..15, 48..63]: CORAL_RED_BLOCK (ruby fire coral)
    c_cr_base = hex_to_rgb("#d62839")
    c_cr_dark = hex_to_rgb("#8e1515")
    c_cr_light = hex_to_rgb("#ea4355")
    c_cr_high = hex_to_rgb("#ff6b7a")
    for y in range(16):
        for x in range(16):
            px, py = x, 48 + y
            val = (x * 3 + y * 7) % 6
            c = c_cr_base
            if val == 0: c = c_cr_dark
            elif val in (3, 4): c = c_cr_light
            elif val == 5: c = c_cr_high
            pixels[px, py] = (*c, 255)

    # 14. Tile (1, 3) [16..31, 48..63]: YELLOW_CORAL_BLOCK (golden horn coral)
    c_cy_base = hex_to_rgb("#f4d03f")
    c_cy_dark = hex_to_rgb("#b78a08")
    c_cy_light = hex_to_rgb("#f9e79f")
    c_cy_high = hex_to_rgb("#ffec8b")
    for y in range(16):
        for x in range(16):
            px, py = 16 + x, 48 + y
            val = (x * 5 + y * 11) % 6
            c = c_cy_base
            if val == 0: c = c_cy_dark
            elif val in (3, 4): c = c_cy_light
            elif val == 5: c = c_cy_high
            pixels[px, py] = (*c, 255)

    # 15. Tile (2, 3) [32..47, 48..63]: CORAL_PINK (magenta tube coral)
    c_cp_base = hex_to_rgb("#e056fd")
    c_cp_dark = hex_to_rgb("#8c1ca3")
    c_cp_light = hex_to_rgb("#f39cff")
    for y in range(16):
        for x in range(16):
            px, py = 32 + x, 48 + y
            val = (x * 7 + y * 5) % 5
            c = c_cp_base
            if val == 0: c = c_cp_dark
            elif val == 3: c = c_cp_light
            pixels[px, py] = (*c, 255)

    # 16. Tile (3, 3) [48..63, 48..63]: BARNACLE_LIGHTS
    for y in range(16):
        for x in range(16):
            px, py = 48 + x, 48 + y
            c = c_ds_base
            if (x, y) in [(3, 4), (4, 4), (10, 8), (11, 8), (6, 12), (7, 12)]:
                c = hex_to_rgb("#c8d6e5")
            if (x, y) in [(4, 4), (11, 8)]:
                c = hex_to_rgb("#00ffff")
                glow_pixels[px, py] = (*c, 255)
            elif (x, y) in [(7, 12)]:
                c = hex_to_rgb("#fef160")
                glow_pixels[px, py] = (*c, 255)
            pixels[px, py] = (*c, 255)

    atlas_path = "scratch/coral_colossus/coral_colossus_atlas.png"
    glow_path = "scratch/coral_colossus/coral_colossus_emissive.png"
    atlas.save(atlas_path)
    glow.save(glow_path)
    print(f"Saved {atlas_path} and {glow_path}")

    gem_img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    gem_pix = gem_img.load()
    for y in range(16):
        for x in range(16):
            gem_pix[x, y] = pixels[32 + x, 32 + y]
    gem_path = "scratch/coral_colossus/gem_cyan.png"
    gem_img.save(gem_path)
    print(f"Saved {gem_path}")

if __name__ == "__main__":
    make_atlas()
