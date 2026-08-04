# AquaTech

## Root (runtime + deploy)
- `server/` — Mohist server
- `client/`, `dist/` — client pack mirrors
- `mods/` — jars + `aquatech-ui/` / `casesmod/` sources
- `config/`, `defaultconfigs/`, `datapacks/`, `kubejs/`, `scripts/`, `resourcepacks/`
- `deploy_*.ps1`, `setup_horizon_route.ps1`
- docs: `CHANGELOG.md`, `HORIZON_ROUTE.md`, `PLAYER_ROADMAP.md`, `QUEST_ID_FREEZE.md`

## tools/
Maintenance scripts (quests, Industrial Upgrade, cases, world setup).
- Pixel fishing HUD: `mods/aquatech-ui/tools/gen_pixel_balance_ui.py`
- Cleanup: `tools/cleanup_workspace.ps1`

Keep download caches, parked jars, and scratch art out of the root (see `.gitignore`).
