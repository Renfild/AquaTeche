#!/usr/bin/env python3
"""
True Minecraft Isometric 3D Block Renderer using Pillow.
Renders standard 16x16 textures (top, left_side, right_side) into a pixel-perfect
Minecraft 3D block sprite (36x38 or 44x44) matching authentic in-game inventory block rendering.
"""
from PIL import Image, ImageEnhance
import math
import numpy as np

def render_minecraft_block(top_img: Image.Image, side_img: Image.Image, out_size: int = 48) -> Image.Image:
    """
    Renders top face and side face into a standard Minecraft inventory isometric block.
    Projection: 30-degree isometric (rhombus width: 32, height: 16, depth: 16).
    """
    top_img = top_img.convert("RGBA")
    side_img = side_img.convert("RGBA")

    # Crop to 1st square if animated
    tw, th = top_img.size
    if th > tw:
        top_img = top_img.crop((0, 0, tw, tw))
    sw, sh = side_img.size
    if sh > sw:
        side_img = side_img.crop((0, 0, sw, sw))

    # Resize input textures to 16x16 for pixel-crisp mapping
    top_16 = top_img.resize((16, 16), Image.NEAREST)
    side_16 = side_img.resize((16, 16), Image.NEAREST)

    # Shading
    # Top: 100%
    # Right side: 80% (0.8)
    # Left side: 60% (0.6)
    left_side = side_16.copy()
    enhancer_left = ImageEnhance.Brightness(left_side)
    left_side = enhancer_left.enhance(0.62)

    right_side = side_16.copy()
    enhancer_right = ImageEnhance.Brightness(right_side)
    right_side = enhancer_right.enhance(0.82)

    # Standard Minecraft Isometric Dimetric Geometry:
    # Top face diamond: 32 wide, 16 high (step is 2:1 ratio)
    # Block height: 16 pixels vertical
    # Total block size: 32 wide, 32 high
    # We will render onto a 34x34 canvas with 1px border room, then upscale to out_size.
    canvas = Image.new("RGBA", (34, 34), (0, 0, 0, 0))

    # Pixel-by-pixel or polygon mapping for exact pixel art look (no bilinear blur!):
    # For each coordinate (u, v) in 16x16 texture:
    # 1. Top face:
    # (u, v) in [0..15]:
    # x = 16 + (u - v)
    # y = 1 + (u + v) // 2
    # In standard 2:1 isometric:
    # When u increases by 1: x += 1, y += 0.5
    # When v increases by 1: x -= 1, y += 0.5
    top_pix = top_16.load()
    left_pix = left_side.load()
    right_pix = right_side.load()

    # Draw Top Face
    # Standard MC dimetric grid:
    # Vertex Top: (16, 1)
    # Vertex Right: (31, 8)
    # Vertex Bottom: (16, 15)
    # Vertex Left: (1, 8)
    for u in range(16):
        for v in range(16):
            col = top_pix[u, v]
            if col[3] == 0:
                continue
            # Isometric coords for 16x16 grid:
            # Each texture pixel maps to a 2x1 screen block or staggered pixel
            px = 16 + u - v
            py = 1 + (u + v) // 2
            # Staggered 2:1 fill
            canvas.putpixel((px, py), col)
            if (u + v) % 2 == 1:
                canvas.putpixel((px - 1, py), col)

    # Draw Left Face:
    # u goes horizontal (along bottom edge of top face, from left to center),
    # v goes down vertically (0 to 15)
    # Top-left corner of left face: Vertex Left (1, 9)
    # Top-right corner of left face: Vertex Bottom (16, 16)
    for u in range(16):
        for v in range(16):
            col = left_pix[u, v]
            if col[3] == 0:
                continue
            # x goes from 1 to 16
            # y = 9 + u//2 + v
            px = 1 + u
            py = 9 + (u // 2) + v
            canvas.putpixel((px, py), col)

    # Draw Right Face:
    # u goes horizontal (from center bottom to right),
    # v goes down vertically (0 to 15)
    # Top-left corner: Vertex Bottom (17, 16)
    # Top-right corner: Vertex Right (32, 9)
    for u in range(16):
        for v in range(16):
            col = right_pix[u, v]
            if col[3] == 0:
                continue
            px = 17 + u
            py = 16 - (u // 2) + v
            if 0 <= px < 34 and 0 <= py < 34:
                canvas.putpixel((px, py), col)

    # Resize to target out_size with nearest neighbor for crisp pixel art
    return canvas.resize((out_size, out_size), Image.NEAREST)

if __name__ == "__main__":
    import base64, io, json
    with open("tools/extracted_case_textures.json", "r", encoding="utf-8") as f:
        texs = json.load(f)
    
    # Test on graviton solar panel
    top_b64 = texs.get("industrialupgrade:machines/graviton_solar_panel")
    side_b64 = texs.get("industrialupgrade:machines/graviton_solar_panel__side") or top_b64
    
    top_img = Image.open(io.BytesIO(base64.b64decode(top_b64.split(",", 1)[1])))
    side_img = Image.open(io.BytesIO(base64.b64decode(side_b64.split(",", 1)[1])))
    
    res = render_minecraft_block(top_img, side_img, 64)
    res.save("tools/test_block_render.png")
    print("Rendered test_block_render.png successfully!")
