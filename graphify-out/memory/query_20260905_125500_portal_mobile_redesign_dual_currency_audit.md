---
type: "query"
date: "2026-09-05T09:55:00.000000+00:00"
question: "AquaTech portal mobile redesign, dual currency system, and profile tabs audit"
contributor: "graphify"
outcome: "useful"
source_nodes: ["site.js", "profile.html", "store.html", "cases.html", "top.html"]
---

# Q: AquaTech portal mobile redesign, dual currency system, and profile tabs audit

## Answer

Comprehensive audit and modernization of the AquaTech web portal (`https://aquatech-7gs.pages.dev` / `aquateche.store`):

1. **Dual Currency Architecture**:
   - Explicitly separated Rubles (`rub_balance`, `₽`) for privilege ranks/store purchases and AquaCoins (`coins`, `⬡`) for in-game economy, fishing rewards, and F4 case openings.
   - Introduced custom high-contrast AquaTech cyan hexagonal badge (`.aquatech-coin-badge`) replacing generic coin emojis across all headers, profile bento cards, statistics, and case opening modals.

2. **Profile Tabs & Interactivity (`profile.html`)**:
   - Verified 5 client-side tabs without full-page reloads: `#overview` (bento grid, rank, stats, achievements), `#skin` (3D WebGL skin viewer, custom controls, skin/cape upload), `#theme` (8 theme accents: Ocean, Depth, Storm, Abyss, Magma, Sky, Cyber, Aurora), `#password` (secure credential updates), `#about` (social handles, bio, favorite fishing rod).
   - Injected role badges (`ВЛАДЕЛЕЦ` for Renfild).

3. **Mobile & Apple HIG Responsive Polish**:
   - Header collapses to compact brand mark + dual balance pills + animated hamburger drawer.
   - Profile balance cards stack horizontally into compact glassmorphism pills (~52px touch target) without horizontal scroll or clipping.
   - Case modals and store privilege grids adapt dynamically to small viewports (<480px).

4. **Testing Protocol**:
   - Headless automated browser testing conducted via Playwright on Cloudflare Pages (`https://aquatech-7gs.pages.dev`) to bypass apex domain Cloudflare Turnstile bot challenges.

## Outcome

- Signal: useful

## Source Nodes

- site.js
- profile.html
- store.html
- cases.html
- top.html
