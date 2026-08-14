---
type: "query"
date: "2026-08-13T16:29:12.012488+00:00"
question: "What is Stage 3 packets and resource cache in aquatech_ui?"
contributor: "graphify"
outcome: "useful"
source_nodes: ["ContainerOpenService", "IslandLimiterTracker", "ResourceCacheManager", "C2SOpenContainerPacket", "S2COpenContainerPacket"]
---

# Q: What is Stage 3 packets and resource cache in aquatech_ui?

## Answer

C2SOpenContainerPacket delegates to ContainerOpenService: STORAGE_VAULT opens ender ChestMenu, BLOCK_LIMITERS syncs IslandLimiterTracker then S2COpenContainer, PERSONALIZATION opens PersonalizationScreen. IslandLimiterRules caps 5 aquatech machines per raft. ResourceCacheManager HTTPS allowlist aquateche.store+crafatar, PNG magic, disk cache, HUD prefetch. Protocol 7. F4 buttons via AquaContainerOverlay. Commands /aquatech vault|limiters|look. Source 1.0.24 / casesmod 1.0.8, pack not published.

## Outcome

- Signal: useful

## Source Nodes

- ContainerOpenService
- IslandLimiterTracker
- ResourceCacheManager
- C2SOpenContainerPacket
- S2COpenContainerPacket