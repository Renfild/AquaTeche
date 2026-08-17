"""Generates the 16x16 item texture for the Project Compass (aqua palette)."""
from PIL import Image, ImageDraw
import pathlib

OUT = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/assets/aqualumen/textures/item/hub_compass.png"
OUT.parent.mkdir(parents=True, exist_ok=True)

SIZE = 16
img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

shell_dark = (14, 21, 30, 255)
shell_edge = (47, 224, 192, 255)
glass = (24, 40, 54, 255)
accent = (59, 157, 255, 255)
needle = (245, 194, 91, 255)

d.ellipse([1, 1, 14, 14], fill=shell_dark, outline=shell_edge)
d.ellipse([3, 3, 12, 12], fill=glass)
d.line([8, 4, 8, 11], fill=accent)
d.line([4, 8, 11, 8], fill=accent)
d.line([6, 10, 9, 5], fill=needle, width=1)
d.point((8, 8), fill=(242, 247, 250, 255))

img.save(OUT)
print(f"written {OUT}")
