# Quest ID Freeze (AquaTech Beta)

After public beta, **do not change** these spine quest IDs — player progress is stored by ID.

## Capstones / bridges
| Chapter | Capstone quest ID | Next opener depends on |
|---------|-------------------|------------------------|
| 01 Kickstarter | `1000000000000010` | 02 |
| 02 Catch | `1200000000000009` | 03 |
| 03 Atoll | `1300000000000009` | 04 |
| 04 Roost | `1400000000000009` | 05 |
| 05 Swarm | `1500000000000009` | 06 |
| 06 Kinetics | `1600000000000009` | 07 |
| 07 Steam | `1700000000000009` | 08 |
| 08 Power | `1800000000000009` | 09 |
| 09 Industry | `1900000000000009` | 10 |
| 10 Depths | `1A00000000000009` | 11 |
| 11 ME | `1B00000000000025` | 12 |
| 12 Dreadnought | `1C00000000000010` | 13 (optional) |
| 13 Horizon Raids | `1E00000000000006` | — |

## Safe changes
- Edit titles/subtitles/rewards/item counts
- Add **new** side quests with fresh IDs (`*AQT*`, `1D…`, `HF…` Horizon Route, `WS…` Workshops, etc.)
- Never reuse or renumber existing IDs

## Horizon Route (2026-07-29)
Chapter `00_horizon_route` uses IDs `HF00000000000001`… — independent of spine freeze.

## Workshops / Мастерские (2026-08-02)
Group `0AC7A00000000005`. Chapters `20_ws_*` … `2F_ws_*` use IDs `WS…` — independent of spine freeze.  
~16 chapters, ~917 quests. Includes Avaritia / Draconic / Botania+ / **Industrial Upgrade** (77 guide quests).  
Serpentine layout + milestone shapes; linear `dependencies` on every step after root. Early `WS…0001Q`… IDs kept stable; new steps append via `workshop_quest_extras.py`.  
Regenerate: `python generate_workshop_quests.py`

Endgame jars (Forge 1.20.1): Re:Avaritia + Avaritia Armor; Draconic Evolution + Brandon's Core + CodeChicken Lib; MythicBotany + LibX; Botanical Machinery + Extra; ExtraBotany + Event Wrapper. Download helper: `download_endgame_botania_addons.py`.

Industrial Upgrade + Power Utilities + Quantum Generators + Simply Quarries: `download_industrial_upgrade.py`. Quarry crafts disabled: `scripts/disable_quarries.zs`.

## Validation
```
python extract_registries.py
python validate_quests.py
python check_all_chapters.py
```
