# AquaTech plugins — что было сломано (2026-08-07)

## Причины
1. FancyNpcs-2.0.11 — Paper-plugin (`PluginMeta`). На этом Mohist класса нет → плагин не грузится.
   NPC делай через мод Easy NPC (уже в mods/).
2. Стоял обычный WorldEdit 7.2.15 — на Mohist падает PaperweightAdapter → // команды мёртвые.
   WorldGuard без нормального WorldEdit тоже страдает.
3. FAWE 2.11.x требует Java 21, сервер на Java 17 → FAWE лежал в disabled.
4. ViaVersion / SkinsRestorer были в plugins/disabled/.

## Что сделано
- FancyNpcs → plugins/disabled/ (несовместим)
- worldedit-bukkit → disabled
- Установлен FastAsyncWorldEdit 2.8.3 (Java 17 + 1.20.1, provides WorldEdit)
- Восстановлены ViaVersion.jar и SkinsRestorer.jar
- DiscordSRV оставлен в disabled (нужен токен бота в конфиге)

## После фикса
Перезапусти сервер. В логе жди:
  Enabling FastAsyncWorldEdit v2.8.3
  Enabling WorldGuard ...
  Enabling ViaVersion ...
  Enabling SkinsRestorer ...
НЕ должно быть: Could not load FancyNpcs / PluginMeta

Проверка в игре: /plugins  и  //wand (нужны права)
