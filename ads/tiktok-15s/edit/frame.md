# AquaTech short — frame spec

Dark-ocean inversion of the editorial house look. Not cream paper. Tokens from the AquaTech portal cyan, not vibe-life peach.

## Palette

```css
:root {
  --ink: #0a0e12;
  --fg: #f3fbff;
  --accent: #3df0ff;
  --kw: #7ee9f2;
  --stage: #020b12;
  --line: rgba(243, 251, 255, 0.08);
}
```

Ink bars + `#f3fbff` type: contrast well above 4.5:1. Cyan is the keyword color inside a bar, never the only way a line reads.

## Type

- Display sans: **Manrope 800** (Cyrillic + Latin, local woff2)
- Twist italic: **Spectral 400 italic** — one word max (`НЕТ`)
- Spec/number: **JetBrains Mono 500**

No CDN at render. No Inter.

## Motion

Paper-cut: hard cuts, no crossfades. Arrivals `power3.out`. Camera drifts `power1.inOut`, scale ≤ 1.08. Animate `.cam` / `.inner`, never `.clip`.

## Devices in this cut

1. Dark-grid interstitial — **ШАХТ НЕТ.**
2. Highlight-bar supers — **Копать не надо.** / **Ловишь.** / **Океан. 1.20.1. Свой плот.**
3. UI-card — inventory screenshot slam
4. Spec ledger card — 1.20.1 / океан / свой плот (built in editor, no new tape)
5. Logo stamp + URL bar

No stacked talking-head split (no A-roll face). No scanlines + cyan frame + vignette stack.

## Audio

No music. Two hits only: whoosh on dunk (0.33s), click on **ШАХТ НЕТ.** (0.87s). Single master `assets/master_audio.m4a`.
