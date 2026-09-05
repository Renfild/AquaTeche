#!/usr/bin/env python3
r"""Deploy new AquaTech logo across the entire project and create desktop PNGs.

- Master 1024x1024 transparent square PNG with centered emblem.
- 512x512 for FancyMenu, docs/portal website, and ads/tiktok.
- 256x256 and multi-size .ico for launcher bootstrap (winres/icon.ico, icon.png, icon16.png).
- CurseForge testing instance: C:\Users\xieto\curseforge\minecraft\Instances\1 20 1\config\fancymenu\assets\logo.png.
- Desktop exports for user convenience:
    C:\Users\xieto\Desktop\aquatech_logo.png (1024x1024 master)
    C:\Users\xieto\Desktop\aquatech_logo_512.png
"""
import os
from PIL import Image

def process_and_deploy():
    src_jpg = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_cube_minimal_1788432094803.jpg"
    im = Image.open(src_jpg).convert("RGB")
    w, h = im.size

    # 1. Clean Chroma-Cutout on Pure Black
    raw_rgba = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    im_px = im.load()
    out_px = raw_rgba.load()

    for y in range(h):
        for x in range(w):
            r, g, b = im_px[x, y]
            brightness = max(r, g, b)
            if brightness <= 12:
                out_px[x, y] = (0, 0, 0, 0)
            elif brightness < 35:
                alpha = int(((brightness - 12) / 23.0) * 255)
                out_px[x, y] = (r, g, b, alpha)
            else:
                out_px[x, y] = (r, g, b, 255)

    # 2. Crop to Tight Bounding Box
    bbox = raw_rgba.getbbox()
    cropped = raw_rgba.crop(bbox)
    cw, ch = cropped.size

    # 3. Create Master 1024x1024 Centered Square PNG (with 5% breathing padding)
    MASTER_SIZE = 1024
    master = Image.new("RGBA", (MASTER_SIZE, MASTER_SIZE), (0, 0, 0, 0))
    # Scale to fit 90% of square (920px max dimension)
    scale = 920.0 / max(cw, ch)
    nw, nh = int(cw * scale), int(ch * scale)
    scaled_emblem = cropped.resize((nw, nh), Image.LANCZOS)
    ox = (MASTER_SIZE - nw) // 2
    oy = (MASTER_SIZE - nh) // 2
    master.paste(scaled_emblem, (ox, oy), scaled_emblem)

    # 4. Generate Standard Derivatives
    img_512 = master.resize((512, 512), Image.LANCZOS)
    img_256 = master.resize((256, 256), Image.LANCZOS)
    img_128 = master.resize((128, 128), Image.LANCZOS)
    img_64  = master.resize((64, 64), Image.LANCZOS)
    img_48  = master.resize((48, 48), Image.LANCZOS)
    img_32  = master.resize((32, 32), Image.LANCZOS)
    img_16  = master.resize((16, 16), Image.LANCZOS)

    # Target Deployment Dictionaries
    deployments_512 = [
        r"d:\AquaTech\config\fancymenu\assets\logo.png",
        r"d:\AquaTech\dist\AquaTech-Client\config\fancymenu\assets\logo.png",
        r"C:\Users\xieto\curseforge\minecraft\Instances\1 20 1\config\fancymenu\assets\logo.png",
        r"d:\AquaTech\docs\assets\logo.png",
        r"d:\AquaTech\ads\tiktok-15s\public\logo.png",
        r"d:\AquaTech\ads\tiktok-15s\edit\assets\logo.png",
        r"C:\Users\xieto\Desktop\aquatech_logo_512.png"
    ]

    deployments_256 = [
        r"d:\AquaTech\tools\aquatech_icon.png",
        r"d:\AquaTech\bootstrap\winres\icon.png"
    ]

    # Save 512x512
    for target in deployments_512:
        try:
            os.makedirs(os.path.dirname(target), exist_ok=True)
            img_512.save(target, "PNG")
            print(f"OK deployed 512x512 -> {target}")
        except Exception as e:
            print(f"ERR {target}: {e}")

    # Save 256x256
    for target in deployments_256:
        try:
            os.makedirs(os.path.dirname(target), exist_ok=True)
            img_256.save(target, "PNG")
            print(f"OK deployed 256x256 -> {target}")
        except Exception as e:
            print(f"ERR {target}: {e}")

    # Save 16x16 icon
    icon16_path = r"d:\AquaTech\bootstrap\winres\icon16.png"
    if os.path.exists(os.path.dirname(icon16_path)):
        img_16.save(icon16_path, "PNG")
        print(f"OK deployed 16x16 -> {icon16_path}")

    # Save Windows .ico file with all mipmap sizes
    ico_path = r"d:\AquaTech\bootstrap\winres\icon.ico"
    if os.path.exists(os.path.dirname(ico_path)):
        master.save(ico_path, format="ICO", sizes=[(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)])
        print(f"OK deployed .ico -> {ico_path}")

    # Save Master 1024x1024 on Desktop and artifacts
    desktop_master = r"C:\Users\xieto\Desktop\aquatech_logo.png"
    master.save(desktop_master, "PNG")
    print(f"OK deployed Desktop Master 1024x1024 -> {desktop_master}")

    artifact_master = r"C:\Users\xieto\.gemini\antigravity-ide\brain\531f1ed4-ea9c-4420-b760-5e17beab3bf0\aquatech_logo_master.png"
    master.save(artifact_master, "PNG")
    print(f"OK saved artifact master -> {artifact_master}")

if __name__ == "__main__":
    process_and_deploy()
