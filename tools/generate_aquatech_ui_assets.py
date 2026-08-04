"""Generate simple ocean-themed GUI PNG placeholders for aquatech-ui."""
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    import subprocess, sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pillow", "-q"])
    from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[1] / "mods" / "aquatech-ui" / "src" / "main" / "resources" / "assets" / "aquatech_ui" / "textures" / "gui"
OUT.mkdir(parents=True, exist_ok=True)

COLORS = {
    "panel_dark": (11, 31, 42, 220),
    "panel_header": (18, 50, 71, 235),
    "badge_owner": (255, 85, 85, 255),
    "badge_admin": (255, 120, 120, 255),
    "badge_mod": (85, 255, 85, 255),
    "badge_vip": (255, 255, 85, 255),
    "badge_legend": (255, 85, 255, 255),
    "accent": (90, 200, 250, 255),
}

for name, rgba in COLORS.items():
    img = Image.new("RGBA", (64, 64), rgba)
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, 63, 63), outline=(31, 111, 235, 255), width=2)
    img.save(OUT / f"{name}.png")
    print("wrote", OUT / f"{name}.png")
