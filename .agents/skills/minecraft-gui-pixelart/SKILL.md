---
name: minecraft-gui-pixelart
description: Pixel-perfect Minecraft machine GUI and icon art in the authentic MetaLabs/HiTech mod style. Use when drawing, designing, or generating mod GUIs (container screens, slots, progress gauges, machine panels), machine/item icons, or iso block renders — and whenever the user mentions GUI-текстуры, слоты, шкалы прогресса, иконки механизмов, пиксель-арт интерфейсов for AquaTech or any Minecraft mod.
---

# Minecraft GUI pixel art (authentic MetaLabs/HiTech style)

Generate pixel-perfect machine GUI sheets and icons as PNG (Aseprite-compatible) for
Minecraft 1.20.1 mods. The style is reverse-engineered from real extracted MetaLabs mod
textures (LoliEnergistics, HiTech-Elements, luminous) — see
`references/real-mod-references.md`. When style is disputed, sample the original PNGs;
do not argue from memory.

## Two styles (both authentic)

- **Style A "gray tech"** (HiTech `fisher.png`): whole GUI is the vanilla gray box
  `#C6C6C6`, vanilla dark slots, ONE segmented vertical gauge on the right (20×64, white
  border, unlit desaturated fill baked, lit fill sprite blitted from the bottom, 8×8 icon
  label above), vanilla inventory at (8,84)/(8,142). GUI 176×166.
- **Style B "dark tech"** (LoliEnergistics): dark navy machine panel `#2E2E40` with 1px
  cyan circuit frame `#4EF9FF` (corner brackets, mid-edge ticks, 2×2 nodes), baked black
  title plate with a cyan status square, dark socket slots `#232330`, white dotted
  transfer arrow, thin bottom progress bar with a lightning label; vanilla gray inventory
  box attached below (rows at y=112, hotbar y=170). GUI 176×194.

## Non-negotiable rules

A delivery fails review if any of these break:

1. **Sheet 256×256, GUI at (0,0).** Gauge TRACKS are baked into the background; only
   gauge FILLS live in sprites — tex(176,0) when ≤80px wide, else the free band below the
   GUI (tex(24,200)), as documented per variant.
2. **Slots are empty.** No baked items, ever. Style B slots keep the centered socket glyph.
3. **Exactly ONE gauge per machine.** Storage GUIs have none. The real fisher.png has
   three gauges — that is the reference for LOOK, not for count.
4. **No decorative animation widgets.** The gauge fill is the only animated element, and
   its sprite repeats the track geometry so it stays inside the track.
5. **PNG / Aseprite only.** Integer grid, no anti-aliasing, no SVG.
6. **Palette is fixed** (sampled hex values in the references). Style A accent = the
   gauge color pair; Style B accent = cyan frame + one gauge color pair.

## Workflow

1. Read `references/real-mod-references.md` (styles + palettes) and
   `references/gui-grid-specs.md` (coordinates). Read `references/loli-mod-anatomy.md`
   to see how the real mods structure assets/namespaces (widget files, slot silhouettes,
   9-slice kit) — match that layout when exporting for the mod. Read
   `references/machine-drawing.md` before drawing new icons; read
   `references/forge-render.md` before wiring textures into Forge code.
2. Add or edit a variant in `VARIANTS` inside `scripts/generate_gui.py` (style, layout
   function, gauge color pair). Follow an existing layout function.
3. Regenerate and verify (every texture is written TWICE: `*.png` for the game and
   `*.aseprite` — native Aseprite format you can open and fix directly):

   ```bash
   python scripts/generate_gui.py                     # writes assets/gui_*.png + .aseprite + preview.html
   python -m http.server 8791 --bind 127.0.0.1        # from assets/, then open preview.html
   python scripts/aseprite_writer.py                  # self-test of the .aseprite writer
   ```

4. Check the preview in the browser: fill stays inside the track, slots empty, nothing
   else moves. If a canvas looks stale, call `draw(0.65)` in the page — the rAF loop can
   pause in a background tab. Screenshot and look at it — do not trust code alone.
5. Run `python scripts/qa_check.py` — it must pass (sizes, style body colors, empty
   slots, sprite zones).
6. Deliver: PNG paths, style (A/B), gauge kind + coordinates, fill sprite location, and
   the Forge blit snippet from `references/forge-render.md`.

## Built-in variants

| Variant | Style | Gauge | Gauge dest | Fill sprite |
| :--- | :--- | :--- | :--- | :--- |
| auto_fisher | A gray | v-gauge 20×64, red | (142,11) | tex(176,0) |
| star_cache | A gray | none (storage) | — | — |
| alloy_smelter | B dark | h-bar 130×7, orange | (24,88) | tex(24,200) |
| abyssal_crusher | B dark | v-bar 12×52, magenta | (86,14) | tex(176,0) |

Gauge color pairs (lit/unlit): red `#B00000/#896767`, green `#00FF00/#678967`,
blue `#1082B6/#676789`, orange `#FF9D2E/#897567`, magenta `#F05AD0/#89677C`,
mint `#44E1AA/#67897D`.

## Icons

32×32 iso cubes in `assets/icons_machines.png` (one lit color per machine, shades
derived: top ×1.0, wedge ×1.45, right ×0.75, left ×0.5, pattern ×0.35). Spec and
Aseprite settings in `references/machine-drawing.md`.

## QA checklist

- [ ] `python scripts/generate_gui.py` runs clean; `python scripts/qa_check.py` passes.
- [ ] Preview screenshots show: empty slots, one centered gauge, fill inside track, no other animation.
- [ ] Zoom 800% on a crop: 1px outlines, no anti-aliasing, sprites inside documented zones.
- [ ] Menu slot coordinates in Forge code match `references/gui-grid-specs.md`.
