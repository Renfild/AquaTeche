# Edit report — AquaTech: ШАХТ НЕТ. (recut, no new tape)

**Disposition:** in_review
**Output:** `ads/tiktok-15s/out/shakht-net.mp4` and `c:\Users\xieto\Videos\aquatech-tiktok-15s.mp4` — 1080×1920, 9.56s, h264/yuvj420p, AAC stereo
**Style:** dark-ocean editorial (tokens in `edit/frame.md`). Not vibe-life cream.

HyperFrames project lives in `edit/` (init → preview → lint/check). Studio preview confirmed beats. HyperFrames **MP4 extract failed**: only ffmpeg on this machine is Remotion’s stripped build (no frame extract filters). Same cut rendered with Remotion `TikTokAd`.

## Beat-by-beat edit decisions
| Beat / ts | A-roll | B-roll / MG | Cut & camera | On-screen super | SFX |
|-----------|--------|-------------|--------------|-----------------|-----|
| 0.00–0.87 | gameplay | `src.mp4` bobber → dunk → strike | hard cuts, short punch | — | whoosh on dunk |
| 0.87–1.20 | — | dark-grid MG | slam | **ШАХТ НЕТ.** | click |
| 1.20–2.27 | — | `inv.png` card | slam | **Копать не надо.** (ink bar) | — |
| 2.27–3.60 | — | `loot.mp4` panel | static panel | **Ловишь.** | — |
| 3.60–4.73 | — | spec ledger MG | paper-cut build | **Океан. 1.20.1. Свой плот.** | — |
| 4.73–6.13 | — | `menu.mp4` profile, panel-fit | slow scale | — | — |
| 6.13–7.47 | — | `menu.mp4` cases, panel-fit | slow scale | — | — |
| 7.47–9.50 | — | logo stamp | snap | **aquateche.store** | — |

Killed vs previous 12s cut: music, bone-crack, per-cut SFX, shop beat, second loot, second minigame, second inventory, ocean/horizon repeat.

## Assets used
- `public/src.mp4` → hook only (once).
- `public/inv.png` → inventory slam (once).
- `public/loot.mp4` → loot tables (once).
- `public/menu.mp4` → profile then cases (two different screens, not the same shot twice).
- `public/logo.png` → endcard.
- Built-in-editor: dark-grid **ШАХТ НЕТ.**, spec ledger 1.20.1 / ОКЕАН, highlight bars.

## Audio
- Voiceover: none
- Music: **removed**
- SFX: whoosh on dunk (Remotion media, cleared), click on **ШАХТ НЕТ.** HyperFrames master mix (`edit/assets/master_audio.m4a`) is whoosh@330ms + click@870ms over 9.5s silence. Remotion uses the two remote hits only in the hook; no bed.mp3.

## Design decisions
- Dark grid + ink bars instead of cream paper (brand is night ocean / cyan).
- Type in solid `#f3fbff` bars on `#0a0e12` (contrast), not drop-shadow over water. HIG Color / Accessibility 4.5:1.
- Menu shown `fit="panel"` so the whole AquaLumen window is readable.
- No scanlines + cyan frame + vignette stack on UI beats.
- No stacked talking-head split: no A-roll face in the sources.

## Verification
- Preview: HyperFrames Studio at 0s bobber, 1.1s **ШАХТ НЕТ.**, 1.8s inv bar, 4.4s spec card, 5.6s profile.
- Face check: Spider-Man skin in inv/profile is fully in panel. No talking-head crop issue.
- Lint: 0 errors. Advisory: track density. Contrast warning 2.06:1 at t=2.64s while **Ловишь.** bar is fading in over loot — false positive (hidden/mid-fade).
- ffprobe: 1080×1920 / h264 / 30fps / 9.56s / AAC. Gate: PASS (`yuvj420p` accepted).
- Frame spot-checks: `ads/tiktok-15s/out/contact/f01.jpg`–`f10.jpg`. After 5s: profile → cases → logo. No second minigame/inv/loot/ocean.
- HyperFrames `render`: FAIL (ffmpeg extract). Remotion render: PASS.

## Rights flags
- needs-final-review: Remotion whoosh sample (remotion.media); gameplay is the operator’s OBS.
- missing: **ore flying at camera** — still not in the tape. Do not post as the locked fake-hook.

## Gaps / next steps
- Optional: install a full ffmpeg, then `cd edit && npx hyperframes render` for the HTML-source MP4.
- Optional new take (not required for this recut): toss iron/titanium in water, hook-set into lens, same item in inv.
- If 9.5s feels short for TikTok, add more **different** F4 tabs or a still of a case reward — do not loop the hook footage.
