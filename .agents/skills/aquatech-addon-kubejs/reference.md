# AquaTech Addon Reference

## Live pack truth

Do **not** treat `registries.json` as installed-mod truth. Prefer:

- `server/mods/*.jar` (runtime)
- Root `kubejs/` (script source of truth)
- `mods/aquatech-ui`, `mods/aqualumen-ui` (team Java)

Create is **not** on the lean server. CraftTweaker is parked (`scripts/*.zs.disabled`).

## Path index

| Concern | Path |
|---------|------|
| KubeJS source | `kubejs/server_scripts/`, `kubejs/startup_scripts/` |
| aquatech_ui source | `mods/aquatech-ui/` |
| aqualumen source | `mods/aqualumen-ui/` |
| aquatech config | `config/aquatech_ui-common.toml` |
| FTB quests live | `config/ftbquests/quests/` |
| FTB backup | `config/ftbquests_backup_*/` |
| Datapacks templates | `server/world_datapack_templates/`, `datapacks/` |
| Parked jars | `_parked_mods/` |
| SC patch tools | `tools/patch_starcatcher_*.ps1` |
| Deploy UI | `scripts/deploy/deploy_aquatech_ui.ps1` (root shim OK) |
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

## Hub economy (AquaLumen)

Store/cases/pass live in the AquaLumen hub (`mods/aqualumen-ui`). Actions `store.buy` / `case.open` currently reply «пока недоступно» until a real economy bridge exists. Do not restore `casesmod` JSON.

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
| AquaLumen hub | F4 / `/hub` opens; store/cases say «пока недоступно» |
| Deploy | Same jar MD5 on server + CF client mods |
