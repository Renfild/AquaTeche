"""Slice generated fishing UI layout sheet into PNG assets."""
from PIL import Image
import os

SRC_LAYOUT = r"C:\Users\xieto\.cursor\projects\c-Users-xieto-Desktop-AquaTech\assets\c__Users_xieto_AppData_Roaming_Cursor_User_workspaceStorage_5b2eeb86146447742813c8504550c9e2_images_image-5a6c2619-b602-46d0-9feb-5892fe6d12fd.png"
SRC_PANEL = r"C:\Users\xieto\.cursor\projects\c-Users-xieto-Desktop-AquaTech\assets\c__Users_xieto_AppData_Roaming_Cursor_User_workspaceStorage_5b2eeb86146447742813c8504550c9e2_images_2da15893-c684-4fa7-ab21-a4278d03958d-7b1c26c0-1a05-4d84-ab8f-77e8a0c16d83.png"
OUT = r"c:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\minigame\rhythm"


def key_black(im: Image.Image, thresh: int = 28) -> Image.Image:
    im = im.convert("RGBA")
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if r < thresh and g < thresh and b < thresh:
                px[x, y] = (0, 0, 0, 0)
    return im


def crop_save(im, box, name):
    c = key_black(im.crop(box))
    path = os.path.join(OUT, name)
    c.save(path)
    print(name, c.size)


def main():
    os.makedirs(OUT, exist_ok=True)
    layout = Image.open(SRC_LAYOUT)
    panel = Image.open(SRC_PANEL)

    # Layout sheet crops (1024x625) — tuned to element sheet
    crops = {
        "fish_koi.png": (245, 52, 338, 128),
        "banner_elite.png": (338, 46, 625, 102),
        "dial_ring.png": (305, 98, 615, 368),
        "needle.png": (718, 145, 868, 375),
        "bar_bg.png": (328, 368, 608, 404),
        "orbs_row.png": (315, 408, 615, 452),
        "banner_bottom.png": (285, 468, 625, 545),
        "orb_glow.png": (748, 458, 862, 572),
        "frame_corner.png": (218, 40, 298, 118),
        "wood_side.png": (218, 118, 276, 520),
    }
    for name, box in crops.items():
        crop_save(layout, box, name)

    # Full assembled panel for backdrop
    crop_save(panel, (0, 0, panel.width, panel.height), "panel_full.png")

    # Slime overlay from dial right side (for pulse anim)
    dial = key_black(layout.crop((305, 98, 615, 368)))
    dial.save(os.path.join(OUT, "_dial_tmp.png"))
    w, h = dial.size
    slime = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    spx = dial.load()
    gpx = slime.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = spx[x, y]
            if a > 0 and g > r + 20 and g > 80:  # green slime tint
                gpx[x, y] = (r, g, b, min(255, a))
    for fi in range(3):
        pulse = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        pp = pulse.load()
        alpha_mult = 0.55 + fi * 0.2
        for y in range(h):
            for x in range(w):
                r, g, b, a = gpx[x, y]
                if a > 0:
                    na = int(a * alpha_mult)
                    pp[x, y] = (r, g, b, na)
        pulse.save(os.path.join(OUT, f"slime_f{fi}.png"))

    # Orb glow animation frames
    orb = key_black(layout.crop((748, 458, 862, 572)))
    for fi in range(3):
        o = orb.copy()
        px = o.load()
        mult = 0.7 + fi * 0.15
        for y in range(o.height):
            for x in range(o.width):
                r, g, b, a = px[x, y]
                if a > 0:
                    px[x, y] = (r, g, b, min(255, int(a * mult)))
        o.save(os.path.join(OUT, f"orb_pulse_f{fi}.png"))

    # Bar fill shimmer frames (extract blue fill from panel)
    bar = key_black(layout.crop((328, 368, 608, 404)))
    bar.save(os.path.join(OUT, "bar_fill.png"))

    if os.path.exists(os.path.join(OUT, "_dial_tmp.png")):
        os.remove(os.path.join(OUT, "_dial_tmp.png"))

    print("done ->", OUT)


if __name__ == "__main__":
    main()
