# AquaTech Addon Reference

## Live pack truth

Do **not** treat `registries.json` as installed-mod truth. Prefer:

- `server/mods/*.jar` (runtime)
- Root `kubejs/` (script source of truth)
- `mods/aquatech-ui`, `mods/casesmod` (team Java)

Create is **not** on the lean server. CraftTweaker is parked (`scripts/*.zs.disabled`).

## Path index

| Concern | Path |
|---------|------|
| KubeJS source | `kubejs/server_scripts/`, `kubejs/startup_scripts/` |
| aquatech_ui source | `mods/aquatech-ui/` |
| casesmod source | `mods/casesmod/` |
| cases JSON | `config/casesmod/` |
| aquatech config | `config/aquatech_ui-common.toml` |
| FTB quests live | `config/ftbquests/quests/` |
| FTB backup | `config/ftbquests_backup_*/` |
| Datapacks templates | `server/world_datapack_templates/`, `datapacks/` |
| Parked jars | `_parked_mods/` |
| SC patch tools | `tools/patch_starcatcher_*.ps1` |
| Deploy UI | `deploy_aquatech_ui.ps1` |
| Deploy kubejs+runtime | `deploy_runtime.ps1` |

## Fishing addon map (aquatech_ui)

| Class | Role |
|-------|------|
| `FishingRodCompat` | SC rod → resource tier / fish-only |
| `FishingLootHandler` | IU loot, rate multiply, cancel vanilla drops |
| `RateModItem` | ×2…×64 in rod tackle NBT |
| `StarCatcherRodEvents` | Shift+RMB tackle GUI |
| `StarCatcherRodTooltips` | client Shift tooltips |
| `AutoFisherBlockEntity` | only `isResourceRod` |
| `RodLootRanges` | base totals (2–4) |

Resource rods (exactly 5):
`naturalist_rod`, `starcatcher_rod`, `obsidian_rod`, `lush_glowberry_rod`, `magmaforged_rod`

## casesmod config shapes

- `config/casesmod/cases/*.json` — case pools
- `config/casesmod/kits.json` — kits
- `config/casesmod/quests.json` — F4 stage quests
- `config/casesmod/warps.json` — menu warps

Prefer editing JSON; bump Java only for new packets/UI.

## KubeJS event cheat sheet (1.20.1 Forge)

```js
ServerEvents.recipes(event => { })
ServerEvents.tags('item', event => { })
ServerEvents.loaded(event => { })
ItemEvents.rightClicked(event => { }) // use sparingly; prefer Java for complex UX
StartupEvents.registry('item', event => { }) // startup_scripts only
```

IDs: `'namespace:path'`. NBT items: `Item.of('id', { ... })`.

## When to jar-patch vs datapack

- **Datapack**: additive tags/recipes/loot under a namespace you control or `#replace` carefully.
- **Jar patch**: replacing assets inside `starcatcher-*.jar` (textures) or bulk-editing hundreds of fish JSON elevation fields — keep a `tools/patch_*.ps1` so it is repeatable after upgrades.

## Smoke tests by feature type

| Feature | Test |
|---------|------|
| Resource fishing | Catch with naturalist rod at Y~190 → IU mats, amount 2–4 without rate |
| Rate mod | Shift+RMB → insert rate_x8 → haul ~16–32 |
| Fish-only rod | humble_rod → SC fish, no IU override |
| KubeJS remove | Creative/TP item uncraftable in JEI |
| casesmod | `/menu` opens, case opens, kit claim |
| Deploy | Same jar MD5 on server + CF client mods |
