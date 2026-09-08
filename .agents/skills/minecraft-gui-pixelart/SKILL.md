---
name: minecraft-gui-pixelart
description: Pixel-perfect Minecraft machine GUI and icon art in authentic MetaLabs/HiTech styles (A gray, B dark, C steel LED, D 9-slice kit). Use when drawing or generating mod GUIs, slots, gauges, machine panels, item icons, flipbook block faces, or when the user mentions GUI-текстуры, слоты, шкалы, иконки механизмов, пиксель-арт for AquaTech.
---

# Minecraft GUI pixel art (MetaLabs / HiTech)

Generate pixel-perfect machine GUI sheets and icons as PNG (+ `.aseprite`) for
Minecraft 1.20.1 Forge. Style is reverse-engineered from real mod textures — when
disputed, **sample PNGs**, do not argue from memory.

**First action every run:** open `references/style-picker.md` and lock ONE style
before drawing. Mixing languages on one sheet fails review.

## Four styles

| ID | Name | Source | Typical size | Use for |
| :--- | :--- | :--- | :--- | :--- |
| **A** | gray tech | HiTech `fisher.png` | GUI 176×166 | Existing AquaTech fisher/cache |
| **B** | dark tech | LoliEnergistics | GUI 176×194 | Dark cyan-circuit commissions |
| **C** | steel LED | HiTech1211 Nexteam 1.21.1 | 16×16 faces, flipbooks | Blocks, materials, coins |
| **D** | vanilla 9-slice kit | HiTech1211 `next_content_core` | composed 176×166 | **Default new machine GUIs** |

Details:

- **A** — whole GUI `#C6C6C6`, vanilla dark slots, ONE segmented v-gauge 20×64 (white
  border, unlit baked, lit fill from bottom), inventory (8,84)/(8,142). See
  `references/real-mod-references.md`.
- **B** — panel `#2E2E40` + 1px cyan frame `#4EF9FF`, title plate, socket slots,
  thin bottom progress; gray inventory attached below. Same ref file.
- **C** — steel frame + `#0D0D11` screen, LED dots, flipbook strips `16×(16·N)`,
  master-recolor materials. See `references/hitech1211-style.md`. Never mix C's HD
  elementa menu language with pixel widgets.
- **D** — ~20 tiny sprites / ~10 colors: 24×24 raised bg tile, classic slots,
  12×12 panel buttons (hover = edge swap), 14×42 energy track + inset fill,
  gray→white progress arrow. Volume from bevels only. See
  `references/hitech1211-elementa-gui.md`. Ground-truth PNGs:
  `assets/references_mods/hitech1211_kit/`.

## Non-negotiable rules

Delivery fails if any break:

1. **Sheet 256×256, GUI at (0,0)** for A/B/D composed sheets. Gauge TRACKS baked;
   only FILLS are sprites — tex(176,0) if ≤80px wide, else band below GUI
   (tex(24,200)). Style D may also ship loose kit sprites (bg/slot/button/energy)
   composed in Forge/code — still empty slots, one energy + optional progress.
2. **Slots are empty.** No baked items. Style B keeps centered socket glyph.
3. **Exactly ONE energy/progress fill animation** on the machine panel (storage = none).
   A progress *arrow* + energy bar together is OK in Style D (two widgets, one fill
   blit each) — not three decorative gauges. Real `fisher.png` has three gauges;
   that is LOOK reference, not OUR count.
4. **No decorative animation widgets** (radar, spinner, circuit blink on the GUI sheet).
   Block-face LED blink belongs on Style C flipbooks, not the container PNG.
5. **PNG / Aseprite only.** Integer grid. No SVG. No anti-aliased soft edges on
   pixel widgets (selective 1px AA on curves for Style C items only — see
   `pixel-art-fundamentals.md`).
6. **Palette is fixed** per style (sampled hex in references). Do not invent neon
   purple SaaS accents.
7. **Minimalism gate (Style D default):** if removing an ornament does not hurt
   reading slots/energy/progress — remove it.

## Workflow

1. Read `references/style-picker.md` → lock style.
2. Read `references/pixel-art-fundamentals.md` (light / hue-shift / clusters / selout).
3. Style refs: `real-mod-references.md` (A/B), `hitech1211-style.md` (C),
   `hitech1211-elementa-gui.md` (D sprites), `hitech1211-machinescreen.md` (D
   layout / 15-machine rows). Anatomy: `loli-mod-anatomy.md`. Coordinates:
   `gui-grid-specs.md`. Icons: `machine-drawing.md`. Forge blit: `forge-render.md`.
4. Sample originals when unsure:
   - Skill ships: `assets/references_mods/` + `hitech1211_kit/`
   - Local full dump (gitignored): `scripts/scratch/hitech1211/extracted/`
   - Desktop `HiTech1211_разбор`: read `ОТЧЁТ.md` first; `01_ui_gui` sprites;
     `03_sources/next_hitech/client/screen` for row recipes; `08_практика_стиль`
     for STUDIES + item redraws (**not** PREVIEW GUI sheet — see style-picker trap)
5. Implement:
   - A/B/D sheet → edit `VARIANTS` / layout in `scripts/generate_gui.py`
   - Refresh kit PNGs if scratch extract updated → `python scripts/sync_hitech1211_kit.py`
   - C face/item → draw or master-recolor per `hitech1211-style.md` recipes
6. Regenerate and verify:

   ```bash
   cd .agents/skills/minecraft-gui-pixelart
   python scripts/generate_gui.py
   python scripts/qa_check.py
   python scripts/aseprite_writer.py   # optional self-test
   # from assets/: python -m http.server 8791 --bind 127.0.0.1 → preview.html
   ```

7. Look at preview / screenshot at 800% — do not trust code alone.
8. Deliver: PNG + `.aseprite` paths, style ID, gauge/widget coords, fill sprite
   location(s), Forge blit snippet from `forge-render.md`.

## Built-in variants (`generate_gui.py`)

| Variant | Style | Gauge / widgets | Fill sprite |
| :--- | :--- | :--- | :--- |
| auto_fisher | A | v-gauge 20×64 @ (142,11) | tex(176,0) |
| star_cache | A | none | — |
| alloy_smelter | B | h-bar 130×7 @ (24,88) | tex(24,200) |
| abyssal_crusher | B | v-bar 12×52 @ (86,14) | tex(176,0) |
| steel_press | D | energy 14×42 + progress arrow | kit fills @ tex(176,0) |
| fish_oil_press | D | Crucible row: energy · input · arrow · tank | kit fills @ tex(176,0) |
| kit_d | D | atlas of kit sprites | — |

Gauge color pairs A/B (lit/unlit): red `#B00000/#896767`, green `#00FF00/#678967`,
blue `#1082B6/#676789`, orange `#FF9D2E/#897567`, magenta `#F05AD0/#89677C`,
mint `#44E1AA/#67897D`.

Style D energy (from kit): track `#FFFFFF/#373737/#370005…`, fill `#B51508/#9E0E08/#6A0000`.

## Icons & faces

- **32×32 iso machines:** `assets/icons_machines.png` — one lit color, shades
  top×1.0 / wedge×1.45 / right×0.75 / left×0.5 / pattern×0.35 (`machine-drawing.md`).
- **16×16 materials (Style C):** master silhouette → luminance recolor; outline
  `#21243D`; 3–4 tones; clusters ≥2 px (`hitech1211-style.md`).
- **Animated fronts:** vertical strip `16×(16·N)` frames; only FRONT blinks.

## Anti-patterns (seen in failed drafts)

- Mixing Style B cyan circuits onto a Style D 9-slice panel
- Copying `08_практика_стиль/PREVIEW.png` GUI SHEET as if it were live HiTech GUI
- Using elementa HD cards for a container inventory screen
- Baking JEI/EMI preview items into slot cells
- Copying McSkill `next_*` jars or their pause PNGs into the pack (license ARR —
  steal *structure*, redraw ocean palette)
- Three fisher-style gauges “because the reference has them”
- Unique chrome per machine (15 HiTech machines share one row + one kit)

## QA checklist

- [ ] Style locked via `style-picker.md`
- [ ] `python scripts/generate_gui.py` clean; `python scripts/qa_check.py` passes
- [ ] Preview: empty slots, fills inside tracks, no extra animation on the sheet
- [ ] Zoom 800%: 1px outlines, no soft AA on GUI chrome, sprites in documented zones
- [ ] Menu slot coords in Forge match `gui-grid-specs.md`
- [ ] If Style D: kit colors only (~10), states via edge/fill swap
