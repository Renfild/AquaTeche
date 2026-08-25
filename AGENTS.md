# AquaTech — agent guidance

## Anti-AI-Slop Rules (Always On)
- **Writing (`anti-ai-slop-writing`)**: No banned AI words/phrases, no rule of three defaults, no em-dash spam, active direct tone, natural sentence length variation.
- **Coding (`anti-ai-slop-coding`)**: Domain-specific symbol names, no restating comments, no YAGNI abstraction layers, smallest diff, proper error handling, match repo style.
- **Design (`anti-ai-slop-design`)**: Brand and clear hierarchy first. No purple SaaS gradients, no default Inter/Roboto stacks, cards only when necessary, concrete UI copy.

## Engineering & Execution Protocol (Deep Thinking, Research & Double-Check Verification)
1. **Research & Information Gathering First**: Always inspect actual files, search the codebase, and verify active data structures before formulating any answer or plan.
2. **Deep Thinking & Risk Analysis**: Thoroughly analyze consequences, check all edge cases and dependencies, and ensure no handcrafted files or user customizations are altered.
3. **Pre-Execution Double Check**: Explicitly verify the plan and target files against the user's explicit instructions before executing.
4. **Mandatory Testing & Post-Verification**: Validate every change through automated builds (`dotnet build`, `go build`, `./gradlew build`), run smoke tests, and perform a final double-check verification of outputs before reporting completion.
5. **Smallest Clean Diffs**: No speculative abstractions; implement only what is necessary and verified.

## Caveman & Agent Skills
- **Caveman mode**: `/caveman` for terse chat compression (`lite|full|ultra`). Code, commits, and PR bodies remain in standard English.
- **Agent skills**: `graphify`, `grill-me`, `interface-kit`, `junior-to-senior`, `loop-factory`, `deslopify`, `last-20-percent`, `context-canary`, `caveman-*`, `cavecrew`.

## gstack & Claude Skills
- Full suite available: `/gstack-*` and direct aliases (`/ship`, `/review`, `/qa`, `/browse`, `/plan-*`, `/design-*`, `/autoplan`, `/canary`, `/benchmark`, `/careful`, `/freeze`, `/guard`, `/unfreeze`, `/investigate`, `/retro`, `/spec`, `/upgrade`).

## AquaTech Release & Development Workflows

### 1. Player-facing changes
- `python tools/publish_client_pack.py` — when modpack mods, KubeJS scripts, or client configs change.
- Rebuild launcher (`dotnet publish` + Go bootstrap + `upload_launcher_release.py`) — see `always-rebuild-launcher.mdc`.
- Do **not** copy jars or configs into Lodestone. Live host is Apex.

### 2. Portal changes (`docs/`, `worker/`, etc.)
- Deploy to Cloudflare (`python tools/deploy_to_cloudflare.py`) + run smoke tests. (Do NOT run `generate_site.py` which overwrites custom `docs/` HTML).

### 3. After code edits
- `python -m graphify update .` — update codebase graph index.
- Update `IMPLEMENTATION_PLAN.md` revision log (Section 6) if architecture or features changed.

### 4. After Apex server deploy
- `python scripts/tasks/smoke_apex_server.py`

### 5. FTB Quests safety
- Do not break quest structures. Deploy script must NOT sync `ftbquests` without `AQUATECH_SYNC_QUESTS=1`.

### 6. Git Commits & Push Policy
- **Never auto-commit or push**. Commit/push ONLY when explicitly requested by the user (e.g. "пушь", "закоммить", "заливай").

### 7. Repository Hygiene & Artifact Prevention
- Never keep build artifacts (`dist/`, `build/`), compiled binaries (`AquaTech.exe`, `AquaTechLauncher.exe`), intermediate PyInstaller `.spec` files, stray logs (`*.log`), or world region files (`*.mca`) in the repository root or tracking index.
- Maintain strict `.gitignore` exclusions for `launcher/src/**/bin/`, `launcher/src/**/obj/`, `_disabled_mods_backup/`, and root `.png` captures.

### 8. Launcher Performance & Networking Standards
- **Parallel Mirror Fetching**: All multi-mirror/CDN manifest probes in Go bootstrap and C# core MUST run concurrently (`Task.WhenAny` / goroutines) with 10–15s timeouts for metadata, never sequential loops.
- **Fast Warm Start**: Manifest synchronization MUST skip full MD5 file hashing when local `.pack_version` matches the manifest version (validating file existence and byte length instead).
- **Single-File Native DLLs**: Keep `IncludeNativeLibrariesForSelfExtract` disabled in `AquaTechLauncher.csproj` so native rendering binaries reside extracted in the distribution zip, eliminating 3–8s self-extract delay on startup.

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
