---
type: "query"
date: "2026-09-05T10:10:00.000000+00:00"
question: "Apple Design audit and implementation of payment tiles (SBP, MIR, Visa/Mastercard)"
contributor: "graphify"
outcome: "useful"
source_nodes: ["site.js", "site.css", "profile.html", "store.html"]
---

# Q: Apple Design audit and implementation of payment tiles (SBP, MIR, Visa/Mastercard)

## Answer

Comprehensive Apple Design / HIG Liquid Glass overhaul of the balance top-up modal (`openTopupModal`):

1. **Apple Pay Sheet Pattern**:
   - Replaced basic 3-column text boxes with full-width interactive Apple payment tiles (`.apple-pay-methods-list > .apple-pay-method-card`).
   - Integrated authentic vector brand logos in squircle containers (`12px` border-radius with tinted translucent background):
     - **СБП**: Official multi-color 4-triangle star emblem (`#F4B400`, `#0077FF`, `#00A859`, `#E84135`).
     - **Карты МИР**: Emerald card badge with authentic МИР lettering.
     - **Visa / Mastercard**: Iconic interlocking red/amber spheres.

2. **Apple HIG Interaction & Ergonomics**:
   - Touch target: ≥64px vertical height, meeting and exceeding Apple HIG 44pt mobile minimum.
   - Continuous squircle curvature: `border-radius: 16px`.
   - Materials: Frosted Liquid Glass (`backdrop-filter: blur(20px)`, `rgba(255, 255, 255, 0.035)` base).
   - Selection Indicator: Apple-style radio circle. Inactive: subtle ring (`rgba(255, 255, 255, 0.25)`). Active: vibrant `#2fe0c0` cyan filled circle with sharp SVG checkmark.
   - Active tile state: Bioluminescent cyan border (`#2fe0c0`) with soft outer aura `0 8px 24px -4px rgba(47, 224, 192, 0.22)`.
   - Full keyboard navigation: `tabindex="0"`, Enter / Space hotkeys, `:focus-visible` offset ring.

3. **Proactive Frictionless Visa Handling**:
   - When the user selects Visa with an amount < 450 ₽, the UI automatically assists by setting `amountInput.value = 450`, updating coins and total displays, and notifying via toast: *"Сумма обновлена до 450 ₽ (минимум для Visa/Mastercard)"*.
   - Dynamic submit CTA copy reflects the exact chosen provider (`Пополнить на X ₽ (через СБП)`, `... (картой МИР)`, `... (картой Visa / MC)`).

4. **Visual Verification**:
   - Tested and captured across desktop and iPhone viewports (`topup_apple_desktop_sbp.png`, `topup_apple_desktop_mir.png`, `topup_apple_desktop_visa.png`, `topup_apple_mobile_visa.png`, `topup_apple_mobile_sbp.png`).

## Outcome

- Signal: useful

## Source Nodes

- site.js
- site.css
- profile.html
