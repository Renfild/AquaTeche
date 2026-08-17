---
type: "query"
date: "2026-08-17T13:19:17.719867+00:00"
question: "How does LoliLand draw smooth vector-like in-game UI?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["AquaWebBridge", "AquaWebScreen", "HubScreen", "Gfx", "HubFont"]
---

# Q: How does LoliLand draw smooth vector-like in-game UI?

## Answer

Launcher is JCEF+Nuxt. In-game shop/web screens are MCEF inside LoliMod (Chromium texture at framebuffer size). Luminous inventory UI is a Kotlin toolkit: own UIScaler vs MC guiScale, 9-slice PNGs, fwidth/smoothstep sprite AA, SDF fonts (Nunito/Montserrat) via loligraphics FontShader. AquaTech already has MCEF (AquaWebScreen) unused by AquaLumen hub.

## Outcome

- Signal: useful

## Source Nodes

- AquaWebBridge
- AquaWebScreen
- HubScreen
- Gfx
- HubFont