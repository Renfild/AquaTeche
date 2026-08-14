---
name: aquatech-addon-kubejs
description: >-
  Systematizes AquaTech pack addons: choose Forge mod vs KubeJS vs datapack vs
  jar patch, scaffold KubeJS scripts, extend aquatech_ui/casesmod, deploy
  safely. Use when creating addons, KubeJS/JS scripts, recipe nerfs, fishing
  compat, cases/kits config, StarCatcher patches, or team modpack content.
---

# AquaTech Addon + KubeJS System

Use this skill for **any new pack feature** so work lands in the right layer and stays deployable.

## First decision (pick ONE primary layer)

| Need | Layer | Where |
|------|-------|--------|
| New block/item/GUI/network/capability | **Forge Java mod** | `mods/aquatech-ui` or `mods/casesmod` |
| Recipes, removes, tags, gated crafts | **KubeJS** | `kubejs/server_scripts/` |
| Worldgen / biome tags / loot tables | **Datapack** | `datapacks/` or `server/world/datapacks/` |
| Rework foreign mod assets/data without source | **Jar patch script** | `tools/patch_*.ps1` then redeploy jar |
| Cases, kits, warps, F4 quests | **casesmod JSON** | `config/casesmod/` (+ server mirror) |
| Rank/UI text/tips | **Config + lang** | `config/aquatech_ui-*.toml`, `assets/*/lang` |

**Rules**
- Prefer KubeJS over editing third-party jars when recipes/tags are enough.
- Prefer aquatech_ui Java when behavior must run on both client+server (HUD, packets, BE).
- Never add CraftTweaker (`.zs`) — parked; use KubeJS only.
- Never craft/register IDs for items that are **disabled/unregistered** (e.g. old `aquatech_ui:*_fishing_rod`).
- Gate optional mods: `if (Platform.isLoaded('modid')) { ... }`.

For Forge architecture details, also follow [minecraft-mod-dev](../minecraft-mod-dev/SKILL.md).

## AquaTech stack map

```
Player loop
  StarCatcher rods ──fish──► SC fish
                 └─resource─► aquatech_ui FishingLootHandler (+ rate mods)
  IU machines / AE2 / Botania…  ◄── KubeJS nerfs & crafts
  casesmod F4 menu / kits / cases ◄── config JSON
  Horizon / skills / raft / Tab   ◄── aquatech_ui Java
```

Source of truth for scripts: **`kubejs/`** at repo root → deploy copies to server/client/CF via `deploy_runtime.ps1`.

## KubeJS script conventions

### Naming
```
kubejs/server_scripts/
  00_*.js   # hard disables / removes (run early)
  10_*.js   # balance nerfs
  30_*.js   # custom crafts
  40_*.js   # gameplay tweaks
kubejs/startup_scripts/
  *.js      # item/block registry tweaks at startup (rare)
```

### Required header
```js
// AquaTech: <one-line purpose>. Depends: <mods or "vanilla">.
ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading <name>...')
  // ...
})
```

### Patterns (copy these)
```js
// Remove by output
event.remove({ output: 'mod:item' })
// Remove by id / regex
event.remove({ id: 'mod:recipe_id' })
event.remove({ id: /mod:.*creative.*/ })

// Shaped with stable aquatech id
event.shaped('mod:result', ['ABA', 'B B', 'ABA'], {
  A: 'minecraft:iron_ingot',
  B: 'minecraft:string',
}).id('aquatech:unique_name')

// Optional mod
if (Platform.isLoaded('ae2')) {
  // ...
}
```

### Checklist before finishing a KubeJS change
- [ ] IDs exist on the **live 46-mod server** (not only `registries.json`)
- [ ] No recipes for disabled aquatech rods / parked mods (Create, CT, …)
- [ ] `Platform.isLoaded` for optional mods
- [ ] Stable `.id('aquatech:...')` on adds
- [ ] Ran / planned `deploy_runtime.ps1` (or copy `kubejs/` to server)

## Java addon conventions (team mods)

### aquatech_ui — gameplay systems
Package: `net.aquatech.ui.*`
Typical addons: fishing compat, machines, Horizon, UI overlays, packets.

When adding a feature:
1. Prefer **compat adapter** (like `FishingRodCompat`) over forking StarCatcher.
2. Client-only code → `client/` + `@Mod.EventBusSubscriber(value = Dist.CLIENT)`.
3. Protocol bumps: increment `NetworkHandler.PROTOCOL_VERSION` when packets change.
4. Build: `mods/aquatech-ui` → `gradlew build` → `scripts/deploy/deploy_aquatech_ui.ps1` (or root shim).

### casesmod — meta / menu
Package: `com.casesmod.*`
Prefer **JSON config** (`config/casesmod/cases|kits|quests|warps`) over code when possible.
Code only for new packet/UI behavior.

### New Forge addon mod (rare)
Only if feature cannot live in aquatech_ui/casesmod/KubeJS.
Reuse MDK patterns from existing mods; register in `mods.toml`; deploy jar to all mod targets used by `deploy_*.ps1`.

## StarCatcher / third-party jar patches

Use scripts, do not hand-edit production jars repeatedly:
- Textures: `tools/patch_starcatcher_rod_tex.ps1`
- Elevation (sea_level 190): `tools/patch_starcatcher_elevation.ps1`

After patch: copy jar to `mods/`, `server/mods/`, `client/mods/`, CF instance paths (scripts already do this).

## Deploy matrix (always)

| Change | Deploy command / action |
|--------|-------------------------|
| aquatech_ui Java | `deploy_aquatech_ui.ps1` |
| KubeJS | `deploy_runtime.ps1` (or sync `kubejs/` → server+client+CF) |
| casesmod Java | build jar + copy like aquatech_ui |
| casesmod JSON | sync `config/casesmod/` → `server/config/casesmod/` |
| FTB quests | sync `config/ftbquests/` → `server/config/ftbquests/` |
| Datapacks | `datapacks/` and/or `server/world/datapacks/` |

Restart client+server after jar/protocol changes. KubeJS often needs `/reload` or restart.

## New-feature workflow (agent must follow)

```
Task progress:
- [ ] 1. Classify layer (table above)
- [ ] 2. Check live mods / existing IDs
- [ ] 3. Implement smallest change in that layer
- [ ] 4. Wire lang (ru_ru + en_us) if player-facing
- [ ] 5. Deploy to all targets
- [ ] 6. Smoke notes: what to test in-game
```

## Anti-patterns (AquaTech lessons)

- Empty FTB chapters in live config while backup has content — always sync both paths.
- KubeJS recipes for unregistered items (old rods).
- Assuming Create/Thermal/Mekanism are installed — **they are not** on the lean server.
- Putting client `Screen` imports in common event classes (server crash).
- Editing only `server/kubejs` and leaving root `kubejs/` stale (root is source of truth).
- Expanding loot without rate/base controls (resource rods: base 2–4 + rate mods).

## Deeper docs

- Layer details + path index: [reference.md](reference.md)
- Copy-paste templates: [examples.md](examples.md)
