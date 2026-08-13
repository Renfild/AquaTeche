# AquaTech — agent guidance

## Anti-AI-Slop Rules (Always On)
- **Writing (`anti-ai-slop-writing`)**: No banned AI words/phrases, no rule of three defaults, no em-dash spam, active direct tone, natural sentence length variation.
- **Coding (`anti-ai-slop-coding`)**: Domain-specific symbol names, no restating comments, no YAGNI abstraction layers, smallest diff, proper error handling, match repo style.
- **Design (`anti-ai-slop-design`)**: Brand and clear hierarchy first. No purple SaaS gradients, no default Inter/Roboto stacks, cards only when necessary, concrete UI copy.

## Caveman & Agent Skills
- **Caveman mode**: `/caveman` for terse chat compression (`lite|full|ultra`). Code, commits, and PR bodies remain in standard English.
- **Agent skills**: `graphify`, `grill-me`, `interface-kit`, `junior-to-senior`, `loop-factory`, `deslopify`, `last-20-percent`, `context-canary`, `caveman-*`, `cavecrew`.

## gstack & Claude Skills
- Full suite available: `/gstack-*` and direct aliases (`/ship`, `/review`, `/qa`, `/browse`, `/plan-*`, `/design-*`, `/autoplan`, `/canary`, `/benchmark`, `/careful`, `/freeze`, `/guard`, `/unfreeze`, `/investigate`, `/retro`, `/spec`, `/upgrade`).

## AquaTech Release & Development Workflows

### 1. Player-facing changes
- `python tools/publish_client_pack.py` — when modpack mods, KubeJS scripts, or client configs change.
- Rebuild launcher (PyInstaller + bootstrap + `upload_launcher_release.py`) — see `always-rebuild-launcher.mdc`.
- Do **not** copy jars or configs into Lodestone. Live host is Apex.

### 2. Portal changes (`docs/`, `worker/`, etc.)
- `python tools/generate_site.py`
- Deploy to `aquateche.store` + run smoke tests.

### 3. After code edits
- `python -m graphify update .` — update codebase graph index.
- Update `IMPLEMENTATION_PLAN.md` revision log (Section 6) if architecture or features changed.

### 4. After Apex server deploy
- `python scripts/tasks/smoke_apex_server.py`

### 5. FTB Quests safety
- Do not break quest structures. Deploy script must NOT sync `ftbquests` without `AQUATECH_SYNC_QUESTS=1`.

### 6. Git Commits & Push Policy
- **Never auto-commit or push**. Commit/push ONLY when explicitly requested by the user (e.g. "пушь", "закоммить", "заливай").

## graphify (memory / codebase map)
Knowledge graph lives in `graphify-out/`.

Before broad exploration:
```powershell
python -m graphify query "your question"
python -m graphify path "SymbolA" "SymbolB"
python -m graphify explain "concept"
python -m graphify god-nodes --top 15
```

After meaningful code edits:
```powershell
python -m graphify update .
python -m graphify cluster-only . --no-label
```

Work memory (feedback loop):
```powershell
python -m graphify save-result --question "..." --answer "..." --outcome useful --nodes NodeA NodeB
python -m graphify reflect
```
