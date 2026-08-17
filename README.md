# AquaTech

## Layout

| Path | Role |
|------|------|
| `server/` | Mohist host |
| `mods/` | jars + `aquatech-ui` / `aqualumen-ui` sources |
| `kubejs/`, `config/`, `datapacks/`, `resourcepacks/` | pack content |
| `docs/`, `worker/`, `functions/` | portal |
| `tools/` | release pipeline + domain helpers (see below) |
| `scripts/` | task/agent scripts, deploy ps1, scratch, archive |
| `bootstrap/`, `launcher_ui/` | client launcher |

## tools/

**Stay at `tools/` root (release pipeline):**  
`publish_client_pack.py`, `upload_launcher_release.py`, `upload_pack_release.py`, `generate_site.py`, `deploy_to_cloudflare.py`, `pack_launcher_zip.py`, …

**By domain:** `quests/`, `patches/`, `portal/`, `assets/`, `server_setup/`, `launcher_tests/`.  
Old paths like `python tools/smoke_portal_and_versions.py` still work via shims.

## scripts/

See `scripts/README.md`. New one-off scripts → `scripts/tasks/`.  
Root `deploy_*.ps1` are shims into `scripts/deploy/`.

## Cleanup

`tools/cleanup_workspace.ps1` — deletes caches/parked trees (not a full reorg).  
Layout pass: `python scripts/tasks/reorganize_layout.py` (idempotent-ish).
