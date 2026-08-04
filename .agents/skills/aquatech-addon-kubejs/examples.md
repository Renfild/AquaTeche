# Examples — AquaTech addons

## 1) New KubeJS nerf script

File: `kubejs/server_scripts/10_aquatech_example_nerf.js`

```js
// AquaTech: disable broken mobility crafts. Depends: industrialupgrade, ae2 (optional).
ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading example nerfs...')

  const banOut = [
    'industrialupgrade:basemachine3/teleporter_iu',
  ]
  banOut.forEach((id) => event.remove({ output: id }))

  if (Platform.isLoaded('ae2')) {
    event.remove({ id: /ae2:.*spatial.*/ })
  }
})
```

Then: run `deploy_runtime.ps1`, restart or `/reload`.

## 2) New KubeJS craft (gated)

```js
ServerEvents.recipes((event) => {
  console.log('[AquaTech] Loading example craft...')

  if (!Platform.isLoaded('ae2')) return

  event.remove({ output: 'ae2:drive' })
  event.shaped('ae2:drive', ['IEI', 'CEC', 'IEI'], {
    I: 'minecraft:iron_ingot',
    E: 'ae2:engineering_processor',
    C: 'ae2:fluix_glass_cable',
  }).id('aquatech:me_drive')
})
```

## 3) Extend fishing resource mapping (Java)

In `FishingRodCompat.resolveRodType`:

```java
return switch (path) {
    case "naturalist_rod" -> AquaTechFishingRodItem.RodType.IRON;
    case "starcatcher_rod" -> AquaTechFishingRodItem.RodType.GOLD;
    // add new SC rod path HERE only if it should grant IU loot
    default -> null; // fish-only
};
```

Update Shift tooltip lines in `StarCatcherRodTooltips`. Build + `deploy_aquatech_ui.ps1`.

## 4) New rate tier (Java + data)

1. Add enum value in `RateModItem.RateTier`
2. Lang `ru_ru.json` / `en_us.json`
3. Model `assets/aquatech_ui/models/item/rate_xN.json`
4. Recipe under `data/aquatech_ui/recipes/rate_xN.json`
5. Creative tab already iterates `RATE_MODS`

## 5) New casesmod case (JSON only)

`config/casesmod/cases/my_case.json` — mirror to `server/config/casesmod/cases/`.
No Java rebuild if schema matches existing cases.

## 6) StarCatcher texture refresh

1. Drop PNGs in `Desktop/fishing/items`
2. Adjust map in `tools/patch_starcatcher_rod_tex.ps1` if needed
3. Run script → jars deployed to all mod folders
4. Restart client

## Feature request → layer (quick)

| Request | Layer |
|---------|-------|
| «убери телепорт IU» | KubeJS `10_*.js` |
| «новая удочка ловит олово» | Java `FishingRodCompat` + loot |
| «кейс на медь» | casesmod JSON |
| «текст в Tab» | aquatech_ui config/lang |
| «другие текстуры удочек» | jar patch script |
| «квест на авторыбалку» | FTB snbt (and sync server config) |
