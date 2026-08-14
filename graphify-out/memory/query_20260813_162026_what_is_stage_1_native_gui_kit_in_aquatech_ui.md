---
type: "query"
date: "2026-08-13T16:20:26.033288+00:00"
question: "What is Stage 1 native GUI kit in aquatech_ui?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["AquaGlassPanel", "AquaDialogScreen", "AquaFontRenderer", "OceanSkillTreeScreen", "OceanTabScreen"]
---

# Q: What is Stage 1 native GUI kit in aquatech_ui?

## Answer

1.1 OceanBlurEngine own PostChain. 1.2 TTF aquatech_ui:main/header + AquaFontRenderer.drawWrapped. 1.3 AquaGlassPanel AquaBadge AquaCaseSlot AquaDialogScreen wired into OceanTabOverlay OceanTabScreen OceanSkillTreeScreen OceanHudOverlay AquaWebScreen. Skill unlock goes through AquaDialogScreen.confirm. Version 1.0.23 source, pack not published.

## Outcome

- Signal: useful

## Source Nodes

- AquaGlassPanel
- AquaDialogScreen
- AquaFontRenderer
- OceanSkillTreeScreen
- OceanTabScreen