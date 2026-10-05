#!/usr/bin/env python3
"""Изометрическая иконка блока по его настоящей модели (elements из JSON-модели), без запуска игры.

Нужна для блоков с нестандартной моделью (паровые машины, химзавод, объединитель панелей, ядро слияния):
куб из первой попавшейся текстуры выглядит на них как шум. Здесь каждый пиксель иконки пускает луч через все
элементы модели (орфографическая проекция, вид сверху-спереди), берёт ближайший непрозрачный пиксель текстуры грани
и затеняет грань как в игре. Повороты элементов (rotation) учитываются, display.gui не используется.

    from render_block_icon import ModelIcons
    icons = ModelIcons(jars)          # jars: объект с read(ns, inner_path)
    image = icons.render("industrialupgrade:basemachine3/steam_pump")   # PIL.Image 64x64 или None
"""
import io
import json
import math

from PIL import Image

# Вид: камера смотрит с (-1, 1, -1): видны верх, север (слева) и запад (справа). Север у машин обычно «лицо».
CAMERA = (-1.0, 1.0, -1.0)
SHADE = {"up": 1.0, "down": 0.5, "north": 0.85, "south": 0.85, "west": 0.65, "east": 0.65}

# Модели vanilla, которых нет в jar модов: какую текстурную переменную берёт каждая грань.
BUILTIN = {
    "block/cube": {f: "#" + f for f in SHADE},
    "block/cube_all": {f: "#all" for f in SHADE},
    "block/cube_column": {"up": "#end", "down": "#end", "north": "#side", "south": "#side", "west": "#side", "east": "#side"},
    "block/cube_bottom_top": {"up": "#top", "down": "#bottom", "north": "#side", "south": "#side", "west": "#side", "east": "#side"},
    "block/orientable": {"up": "#top", "down": "#bottom", "north": "#front", "south": "#side", "west": "#side", "east": "#side"},
}


def _norm(v):
    length = math.sqrt(sum(a * a for a in v))
    return tuple(a / length for a in v)


def _cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def _dot(a, b):
    return sum(x * y for x, y in zip(a, b))


class ModelIcons:
    def __init__(self, jars):
        self.jars = jars
        self._images = {}

    # ---- модели ------------------------------------------------------------------------------------------------

    def _model(self, ref):
        ns, _, path = ref.rpartition(":")
        ns = ns or "minecraft"
        raw = self.jars.read(ns, f"assets/{ns}/models/{path}.json")
        return json.loads(raw.decode("utf-8-sig")) if raw else None

    def resolve(self, ref, depth=0):
        """Возвращает (elements, textures, texture_size) с учётом цепочки parent."""
        ns, _, path = ref.rpartition(":")
        model = self._model(ref)
        if model is None:
            builtin = BUILTIN.get(path)
            if builtin:
                elements = [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {f: {"texture": t} for f, t in builtin.items()}}]
                return elements, {}, [16, 16]
            return [], {}, [16, 16]
        elements, textures, size = [], {}, [16, 16]
        if "parent" in model and depth < 10:
            elements, textures, size = self.resolve(model["parent"], depth + 1)
        if "elements" in model:
            elements = model["elements"]
        textures = {**textures, **(model.get("textures") or {})}
        if "texture_size" in model:
            size = model["texture_size"]
        return elements, textures, size

    def _image(self, ref):
        if ref in self._images:
            return self._images[ref]
        ns, _, path = ref.rpartition(":")
        ns = ns or "minecraft"
        raw = self.jars.read(ns, f"assets/{ns}/textures/{path}.png")
        image = None
        if raw:
            image = Image.open(io.BytesIO(raw)).convert("RGBA")
            if image.height > image.width:  # анимация: берём первый кадр
                image = image.crop((0, 0, image.width, image.width))
        self._images[ref] = image
        return image

    @staticmethod
    def _texture_ref(textures, value):
        guard = 0
        if isinstance(value, str) and not value.startswith("#") and ":" not in value and "/" not in value and value in textures:
            value = "#" + value  # часть моделей пишет ссылку без решётки: "texture": "texture"
        while isinstance(value, str) and value.startswith("#") and guard < 8:
            value = textures.get(value[1:])
            guard += 1
        return value if isinstance(value, str) and not value.startswith("#") else None

    # ---- рендер ------------------------------------------------------------------------------------------------

    def render(self, item_id, size=64):
        ns = item_id.split(":", 1)[0]
        path = item_id.split(":", 1)[1]
        elements, textures, tex_size = self.resolve(f"{ns}:item/{path}")
        if not elements:
            return None
        return self.render_elements(elements, textures, tex_size, size)

    def render_elements(self, elements, textures, tex_size, size=64):
        cam = _norm(CAMERA)
        up_s = _norm(tuple(u - _dot((0, 1, 0), cam) * c for u, c in zip((0, 1, 0), cam)))
        right = _norm(_cross(up_s, cam))
        center = (8.0, 8.0, 8.0)

        prepared = [e for e in (self._prepare(el, textures, tex_size) for el in elements) if e]
        if not prepared:
            return None

        # масштаб: вписываем границы модели, но не мельче половины и не крупнее 1.5 полного куба
        xs, ys = [], []
        for el in prepared:
            for corner in el["corners"]:
                rel = tuple(c - m for c, m in zip(corner, center))
                xs.append(_dot(rel, right))
                ys.append(_dot(rel, up_s))
        half = max(max(abs(x) for x in xs), max(abs(y) for y in ys))
        full_cube = 8 * (abs(right[0]) + abs(right[1]) + abs(right[2]))
        scale = (size / 2 - 1.5) / min(max(half, full_cube * 0.55), full_cube * 1.5)

        out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        px = out.load()
        direction = tuple(-c for c in cam)
        for py in range(size):
            for pxx in range(size):
                sx = (pxx + 0.5 - size / 2) / scale
                sy = (size / 2 - (py + 0.5)) / scale
                origin = tuple(center[i] + sx * right[i] + sy * up_s[i] + cam[i] * 60 for i in range(3))
                color = self._trace(prepared, origin, direction)
                if color:
                    px[pxx, py] = color
        return out

    def _prepare(self, element, textures, tex_size):
        lo, hi = element["from"], element["to"]
        faces = {}
        for name, face in (element.get("faces") or {}).items():
            ref = self._texture_ref(textures, face.get("texture"))
            image = self._image(ref) if ref else None
            if image is None:
                continue
            faces[name] = (face, image)
        if not faces:
            return None
        rot = element.get("rotation")
        corners = [(x, y, z) for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])]
        if rot and rot.get("angle"):
            corners = [self._rotate(c, rot, 1) for c in corners]
        # Игра считает UV в системе 0..16 независимо от размера текстуры и от texture_size (это поле только для Blockbench).
        return {"lo": lo, "hi": hi, "faces": faces, "rot": rot if rot and rot.get("angle") else None,
                "tex": (16, 16), "corners": corners}

    @staticmethod
    def _rotate(point, rot, sign):
        """Поворот точки вокруг оси элемента; sign=-1 даёт обратный поворот (для луча)."""
        ox, oy, oz = rot["origin"]
        angle = math.radians(rot["angle"] * sign)
        x, y, z = point[0] - ox, point[1] - oy, point[2] - oz
        c, s = math.cos(angle), math.sin(angle)
        axis = rot["axis"]
        if axis == "x":
            y, z = y * c - z * s, y * s + z * c
        elif axis == "y":
            x, z = x * c + z * s, -x * s + z * c
        else:
            x, y = x * c - y * s, x * s + y * c
        return (x + ox, y + oy, z + oz)

    def _trace(self, elements, origin, direction):
        hits = []
        for el in elements:
            o, d = origin, direction
            if el["rot"]:
                o = self._rotate(origin, el["rot"], -1)
                tip = self._rotate(tuple(origin[i] + direction[i] for i in range(3)), el["rot"], -1)
                d = tuple(tip[i] - o[i] for i in range(3))
            t_near, t_far, face_in = -1e9, 1e9, None
            lo, hi = el["lo"], el["hi"]
            ok = True
            for axis in range(3):
                if abs(d[axis]) < 1e-9:
                    if o[axis] < lo[axis] or o[axis] > hi[axis]:
                        ok = False
                        break
                    continue
                t1, t2 = (lo[axis] - o[axis]) / d[axis], (hi[axis] - o[axis]) / d[axis]
                near_face = ("west", "east")[0] if axis == 0 else ("down", "up")[0] if axis == 1 else ("north", "south")[0]
                far_face = ("west", "east")[1] if axis == 0 else ("down", "up")[1] if axis == 1 else ("north", "south")[1]
                if t1 > t2:
                    t1, t2 = t2, t1
                    near_face, far_face = far_face, near_face
                if t1 > t_near:
                    t_near, face_in = t1, near_face
                t_far = min(t_far, t2)
                if t_near > t_far:
                    ok = False
                    break
            if ok and face_in and t_near > -1e8:
                hits.append((t_near, el, face_in, o, d))
        hits.sort(key=lambda h: h[0])
        for t, el, face_name, o, d in hits:
            if face_name not in el["faces"]:
                continue
            point = tuple(o[i] + d[i] * t for i in range(3))
            color = self._sample(el, face_name, point)
            if color and color[3] > 40:
                shade = SHADE[face_name]
                return (int(color[0] * shade), int(color[1] * shade), int(color[2] * shade), 255)
        return None

    @staticmethod
    def _sample(el, face_name, point):
        face, image = el["faces"][face_name]
        x1, y1, z1 = el["lo"]
        x2, y2, z2 = el["hi"]
        x, y, z = point
        default_uv = {
            "down": [x1, 16 - z2, x2, 16 - z1], "up": [x1, z1, x2, z2],
            "north": [16 - x2, 16 - y2, 16 - x1, 16 - y1], "south": [x1, 16 - y2, x2, 16 - y1],
            "west": [z1, 16 - y2, z2, 16 - y1], "east": [16 - z2, 16 - y2, 16 - z1, 16 - y1],
        }
        u1, v1, u2, v2 = face.get("uv") or default_uv[face_name]

        def frac(a, lo, hi, reverse):
            span = hi - lo
            f = 0.0 if span == 0 else (a - lo) / span
            return 1 - f if reverse else f

        if face_name == "up":
            fu, fv = frac(x, x1, x2, False), frac(z, z1, z2, False)
        elif face_name == "down":
            fu, fv = frac(x, x1, x2, False), frac(z, z1, z2, True)
        elif face_name == "north":
            fu, fv = frac(x, x1, x2, True), frac(y, y1, y2, True)
        elif face_name == "south":
            fu, fv = frac(x, x1, x2, False), frac(y, y1, y2, True)
        elif face_name == "west":
            fu, fv = frac(z, z1, z2, False), frac(y, y1, y2, True)
        else:
            fu, fv = frac(z, z1, z2, True), frac(y, y1, y2, True)
        for _ in range(int((face.get("rotation") or 0) // 90) % 4):
            fu, fv = 1 - fv, fu
        tu = u1 + (u2 - u1) * fu
        tv = v1 + (v2 - v1) * fv
        px = min(image.width - 1, max(0, int(tu * image.width / el["tex"][0])))
        py = min(image.height - 1, max(0, int(tv * image.height / el["tex"][1])))
        return image.getpixel((px, py))
