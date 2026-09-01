# AGENT CUT — AquaTech TikTok 15s

Paste this into the next chat together with the raw clips.

You are cutting `ads/tiktok-15s` (Remotion 4, composition `TikTokAd`, 1080×1920, 30fps, **450 frames / 15.00s**). Not a server commercial. Fifteen seconds of “Minecraft broke.” First second: is this a bug? Then: I want that.

Read `remotion-best-practices` → markup + render skills. Drive motion with `useCurrentFrame()` + `interpolate()` only. No CSS/Tailwind animations. `<Video>` from `@remotion/media`. Hardcode `from` / `trimBefore` / `durationInFrames`. One JSX node per clip, no `.map()`. Ultrawide OBS → `objectFit: "cover"` center crop.

Do not commit `public/*.mp4` or `node_modules`. Copy finished mp4 to `c:\Users\xieto\Videos\` and `ads/tiktok-15s/out/`.

---

## Kill rules (stop and tell the user)

- First ~1s has **no ore/ingot flying at camera** → do not post. Ask for the fake (throw block in water + hook-set). Do not substitute StarCatcher minigame UI as the hook.
- Grass, spawn, logo, nametag, or chat in the first 1.2s → recut those frames out.
- Any plan longer than **1.2s**. Montage shots must be **0.6–0.9s**.
- On-screen copy longer than the locked lines. No “индустриальный океанский скайблок…”.
- Logo fly-in from the ocean. Fade to black. Soft dissolves.
- Voiceover in the hook.
- Crafting-table guide, chat, rules, “пиши /ah”, skin-face close-up.

Locked copy (do not paraphrase):

- Hook slam: **ШАХТ НЕТ.**
- End URL: **aquateche.store**
- Optional shorter, never longer: **Копать не надо.** / **Ловишь.** / **Океан. 1.20.1. Свой плот.** / **Скачал — ты уже в воде.**

---

## When clips land — procedure

1. Copy sources into `ads/tiktok-15s/public/` with stable names (`hook-bobber.mp4`, `hook-ore.mp4`, `inv-ore.mp4`, `raft-tiny.mp4`, `chest-ore.mp4`, `autofisher.mp4`, `cast.mp4`, `f4-case.mp4`, `inv-rare.mp4`, `raft-big.mp4`, `night-cast.mp4`, `ore-pop.mp4`, `hopper.mp4`, `horizon.mp4`). Generalize `CoverClip` beyond `fp.mp4 | raft.mp4`.
2. Probe duration/fps. `trimBefore` is **composition frames** (seconds × 30), not source 60fps frames.
3. Label each file against the shot table below. Missing shots: skip that beat or hold an adjacent legal shot — except the ore-fly, which is mandatory.
4. Rebuild scenes. Dump stills at key frames, then `npx remotion render TikTokAd out/shakht-net.mp4`.
5. If F4/case/chest/autofisher were not filmed, do **not** hallucinate UI. Cut around them. Say what is missing.

Old draft used sunset bobber + StarCatcher UI. That hook is dead. Replace it.

---

## Timeline (30fps)

| Scene | Time | Frames | File |
|-------|------|--------|------|
| Hook | 0.00–1.50 | 0–45 | `scenes/Hook.tsx` |
| Raft | 1.50–5.00 | 45–150 (105f) | `scenes/Raft.tsx` |
| Montage | 5.00–11.00 | 150–330 (180f) | `scenes/Montage.tsx` |
| End | 11.00–15.00 | 330–450 (120f) | `scenes/EndCard.tsx` |

Hook is **45f not 36f** so **ШАХТ НЕТ.** can hold 0.35–0.45s. Steal those 9 frames from the old raft block. Keep `TikTokAd` Series in sync.

### Hook 0.00–1.50 — stop-scroll. No music. No AquaTech.

| Time | Frames (local) | Picture | Sound | Type |
|------|----------------|---------|-------|------|
| 0.00–0.35 | 0–11 | Camera almost in water. Ripple + red bobber. Hands/rod barely in frame. | 3–4 frames silence, then dull gulp | none |
| 0.35–0.70 | 11–21 | Bobber yanks under. Camera punch DOWN. | Low splash. No music | none |
| 0.70–1.05 | 21–32 | Hook-set. **Ore BLOCK** fills half the frame, flies into lens. Motion blur. | Whistle + wet hit | none |
| 1.05–1.50 | 32–45 | Smash to **black**. White **ШАХТ НЕТ.** huge, center, weight 900, no rainbow stroke. | Dry inventory slot click (table slam) | 13f hold (~0.43s) |

Motion on slam: 1-frame white flash optional, scale 1.22→1 spring, opacity 0→1 in 2f. Camera shake only on the ore shot (translate ±8–14px, 4–6f), not on the title.

If they also filmed inventory with the **same** item after the fly: 3–4 frames of that grid can replace or precede the black, then slam text. Prefer black+type if the inventory still shows HUD clutter.

### Raft 1.50–5.00 — not a bug, a server. Same player, same day.

No base, farms, mobs, other players. Contrast: tiny raft vs fat chest. Fish items **off screen**.

| Time | Local f | Picture | Sound | Type |
|------|---------|---------|-------|------|
| 1.50–2.30 | 0–24 | 5–7 plank raft, ocean only. Autofisher beside tugs line like a metronome. Player idle. | Water + mechanical tick | none |
| 2.30–3.10 | 24–48 | Hand opens chest. Iron, ingots, ore. **No fish.** | Lid + item clatter | **Копать не надо.** slam in |
| 3.10–3.90 | 48–72 | Second chest / next slots. Pan, autofisher hits again louder | Same tick, up | **Ловишь.** slam in |
| 3.90–5.00 | 72–105 | Self-cast, bobber flies, empty horizon, raft tiny | Cast + water hush | both lines still on, smaller |

If raft is already huge, use the small-raft take or crop tighter. Autofisher **in this act is allowed** (rhythm). Do not turn it into a crafting tutorial.

Type motion: hard stamp (scale+opacity), not fade. After both lines exist, scale them down ~0.85 for the cast hold.

### Montage 5.00–11.00 — greedy, no lecture

Cut every **18–27 frames** (0.6–0.9s). One lower-third from **5.20s** (frame 6 of this scene): **Океан. 1.20.1. Свой плот.** Bottom third, not center. Lives for the whole montage.

Order (reorder OK, do not stretch):

1. F4 — menu flashes half-screen, cursor hits a case
2. Case — 4–6 frames of spin, rare item in hand (rod / ingot / glow). Not the full 8s roll
3. Inventory rare slot, ~0.5s
4. Bigger raft, **same angle** as the tiny raft
5. Night cast: moon, black water, red bobber
6. Second hook-set, ore/ingot again, **shorter** than the opening fly
7. Autofisher + chest/hopper, item sparkle dust
8. Waterline, raft exits frame, infinite ocean

Bass from 5.00, hits on cuts. No lyric guide track. No per-shot captions.

### End 11.00–15.00 — stamp, not titles

Cut to black **on the last splash**. No fade.

| Time | Local f | Picture | Sound | Type |
|------|---------|---------|-------|------|
| 11.00–11.25 | 0–8 | Black empty | Splash dies | none |
| 11.25–13.40 | 8–72 | `logo.png` center, **already there** (2f snap + tiny spring settle). Not rising from water. | Same dry slot-click as hook | **aquateche.store** under logo |
| 13.40–15.00 | 72–120 | Logo stays. New bottom line | Voice **or** type, not both | **Скачал — ты уже в воде.** |

Hold last frame through **15.00**. Do not disappear at 14.2.

If no VO file: type only, larger. If VO file: hide that line or keep it tiny — not both competing.

Do not show the word “AquaTech” as a giant extra title unless the logo is unreadable; logo + URL is the brand. Current `EndCard` has a separate “AquaTech” wordmark — drop it if the logo already says it, keep URL + CTA.

---

## Motion-graphics house (this is the “cool” part)

Feel: glitch / impact / stamp. Not SaaS, not ocean-logo morph, not Hormozi rainbow captions.

- Palette: `#000000` / `#f3fbff` / `#7ee9f2`. Pixelated logo (`imageRendering: "pixelated"`).
- Font: heavy grotesque (Segoe UI / Arial black). Tracking tight. **No stroke rainbow, no glow soup.** Light `textShadow` only over gameplay.
- Cuts: hard `Sequence` swaps. Optional 1-frame white or black flash on act changes (hook→raft, montage→end).
- Slam type: `scale` 1.18→1 with `Easing.spring`, `output: "perceptual-scale"`, opacity in 2 frames.
- Ore fly: add `translate` shake + slight `rotate` ±2deg on the **clip container**, 4–6 frames, then settle.
- Optional: 4–6% grain overlay, vignette on wides. No film-damage Halloween pack.
- Kinetic extra (only if it stays readable): one-frame offset chromatic on **ШАХТ НЕТ.** (2px R/B split), gone by frame 3 of the slam.
- Lower third: slides 8–12px up, not a banner bar, not a card.

Audio mix (if they dump silent gameplay, layer files from `public/sfx/` or say SFX missing):

- 0.00–1.50: near silence → splash → slot click. **No music.**
- 1.50–5.00: water + autofisher tick
- 5.00–11.00: short bass, on-cut hits
- 11.00: mute all → one click → silence under logo
- Voice only at 13.40+ if provided

Gameplay clips: `muted` if we replace audio; keep diegetic splash if it syncs to the ore fly.

---

## What you may invent vs must be footage

| Beat | Source |
|------|--------|
| Black, **ШАХТ НЕТ.**, logo stamp, URL, CTA, flashes, shake, grain | Remotion |
| Bobber, dunk, ore fly, inventory, rafts, chest, F4, case, night, hopper, horizon | User video only |
| Fake ore fly | User must film (toss block + hook). Do not AI-generate Minecraft HUD/case UI |

---

## Render check stills

`npx remotion still TikTokAd out/f-XXX.png --frame=N`

- f-008 bobber, no HUD/logo
- f-025 ore filling the lens
- f-038 black + **ШАХТ НЕТ.**
- f-070 tiny raft
- f-110 chest ores, no fish
- f-170 montage + lower third
- f-340 black
- f-370 logo + aquateche.store
- f-430 CTA still on

Then full render. Copy to Videos as `aquatech-tiktok-15s.mp4`.
