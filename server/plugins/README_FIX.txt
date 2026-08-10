# AquaTech plugins — FAWE / WorldEdit (2026-08-10)

## /wand ошибка BlockTypes + TypeProperty
На Mohist + модах FAWE 2.8.3 падает при инициализации BlockTypes:
  IllegalArgumentException: WorldEdit needs an update to support TypeProperty
После этого любой //wand / selection даёт NoClassDefFoundError: BlockTypes.

Фикс: bytecode-патч PaperweightAdapter$1 (tools/patch_fawe_typeproperty.py) —
неизвестные property (TypeProperty) мапятся как IntegerProperty вместо throw.
Патченый jar: plugins/FastAsyncWorldEdit.jar
Бэкап до патча: FastAsyncWorldEdit.jar.pre-typeproperty.bak

После замены jar — полный рестарт Mohist в Lodestone.

## KubeJS ForgeEvents / empty recipe
40_aquatech_fishing_drops.js — stub (loot в aquatech_ui), ForgeEvents только в startup_scripts.
Рецепты с пустым result — Item.exists + без NBT в Item.of для лодки.
