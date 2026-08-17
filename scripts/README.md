# scripts/

Папка для **задачных** скриптов и хлама, который не должен валяться в корне.

| Подпапка | Что класть |
|----------|------------|
| `tasks/` | Одноразовые/агентские Python-скрипты под конкретную задачу |
| `deploy/` | `deploy_*.ps1`, bat-обёртки деплоя (в корне остались тонкие shim) |
| `scratch/` | Временные дампы, `_tmp_*`, peeks — можно удалять |
| `archive/` | Демо, извлечённые ассеты, старые jar/доки |
| `crafttweaker/` | Старые `.zs` (отключены) |

## Правила для агента

- Новые вспомогательные скрипты → `scripts/tasks/<короткое_имя>.py`, не в корень и не россыпью в `tools/`.
- Пайплайн релиза/пака/лаунчера остаётся в `tools/` (`publish_client_pack.py`, `sync_lodestone_mods.py`, …).
- Не коммить секреты; `scratch/` лучше не трекать (см. `.gitignore`).

## ApexNodes deploy

Secrets (gitignored) — preferred:

```json
// .apex_deploy.json
{ "sftp_pass": "...", "apex_api_key": "ptlc_..." }
```

Or env:

```powershell
$env:AQUATECH_SFTP_PASS = '...'          # panel password
$env:AQUATECH_APEX_API_KEY = 'ptlc_...'  # Account -> API Credentials
python scripts/tasks/deploy_apexnodes_sftp.py
```

Deploy auto:
- mirrors repo `kubejs/` + aquatech datapacks into `server/` before upload
- **does not** overwrite `config/ftbquests` unless `AQUATECH_SYNC_QUESTS=1` (live editor is source of truth)
- **full** SFTP: panel backup first (`pre-deploy-TIMESTAMP`; rotates if limit=1; skip with `--skip-backup` / `AQUATECH_SKIP_BACKUP=1`)
- purges stale remote `aquatech_ui-` / `aqualumen-` / `packetfixer-` jars
- injects MySQL placeholders from `.apex_mysql.json`

Quest/config only (fast — no backup):
  `$env:AQUATECH_SFTP_ONLY = 'config/ftbquests'`

Smoke after deploy (panel + jars + FAWE + MariaDB):

```powershell
python scripts/tasks/smoke_apex_server.py
```

Console helpers (WorldGuard `-w`, Chunky spawn pregen):

```powershell
python scripts/tasks/apex_console_ops.py --wg-flag other-explosion deny --world world
python scripts/tasks/apex_console_ops.py --chunky-spawn --dry-run
python scripts/tasks/apex_console_ops.py --chunky-spawn --radius 400
```

FAWE Mohist patch (when jar updates):

```powershell
python scripts/tasks/bootstrap_p0_local.py
# or: python tools/patches/patch_fawe_mohist.py server/plugins/FastAsyncWorldEdit.jar
```

After FTB Quests editor / SNBT under `server/config/ftbquests/` — upload + restart:

```powershell
$env:AQUATECH_SFTP_ONLY = 'config/ftbquests'
python scripts/tasks/deploy_apexnodes_sftp.py
```

`--no-restart` / `--restart-only` as before.

## ApexNodes MariaDB

```powershell
$env:AQUATECH_SFTP_PASS = '...'
$env:AQUATECH_APEX_API_KEY = 'ptlc_...'
python scripts/tasks/setup_apex_mysql.py
```

Секреты → `.apex_mysql.json` (gitignored). Плагины в репо с плейсхолдерами `__AQUATECH_MYSQL_*__`; deploy подставляет пароль при SFTP.
