# AquaLumen UI

Хаб-интерфейс в стиле Luminous UI (LoliLand) для **Forge 1.20.1 / Mohist**, в собственной цветовой палитре **Aqua Lumen**.

| | |
|---|---|
| Mod ID | `aqualumen` |
| Пакет | `store.aquateche.aqualumen` |
| Версия MC | 1.20.1 (Forge 47.x, Mohist 1.20.1) |
| Java | 17 |
| Сторона | сервер + клиент, ванильные клиенты подключаются без мода |

## Что внутри

- Единый хаб: **Профиль • Магазин • Кейсы • Battle Pass • Топы • Настройки**.
- Трёхзонная оболочка (шапка / боковое меню / контент) — навигация не смещается при переключении вкладок.
- Рисование без текстур: скруглённые панели, градиенты, свечение, кольцевой прогресс (`client/render/Gfx.java`).
- Три темы в конфиге: `aqua_lumen` (по умолчанию), `violet_lumen`, `midnight_rose` + ручной accent.
- Сервер — единственный источник правды: клиент получает только снимок данных и отправляет только id действия.
- Fallback для ванильных клиентов: тот же хаб в виде сундука 3x9 (`common/compat/ChestFallbackUI.java`).

## Структура

```text
src/main/java/store/aquateche/aqualumen/
├─ AquaLumenUI.java              # точка входа, конфиги, DistExecutor
├─ config/LumenConfig.java       # COMMON (баланс) + CLIENT (внешний вид)
├─ registry/ModRegistries.java   # Компас проекта + вкладка креатива
├─ common/
│  ├─ ServerEvents.java          # кто из игроков с модом, тиковые обновления
│  ├─ command/LumenCommands.java # /aqualumen, /hub
│  ├─ data/HubSnapshot.java      # сетевая модель данных
│  ├─ service/HubDataService.java# сборка снимка, выбор UI
│  ├─ service/HubActionHandler.java # валидация действий + антиспам
│  └─ compat/ChestFallbackUI.java
├─ network/                      # SimpleChannel + 3 пакета
└─ client/                       # только клиент
   ├─ LumenClient.java           # клавиша H, кэш снимка, handshake
   ├─ theme/LumenTheme.java      # палитры
   ├─ render/Gfx.java            # примитивы рисования
   ├─ widget/LumenWidgets.java   # NavButton, PillButton, карточки
   └─ screen/HubScreen.java + HubTabs.java
```

## Сборка

```bash
# Java 17 обязателен
gradle wrapper --gradle-version 8.1.1   # один раз: обёртки в репозитории нет
./gradlew build          # → build/libs/aqualumen-forge-1.20.1-0.3.4-alpha.jar
./gradlew runClient      # локальная проверка интерфейса
python3 tools/gen_item_texture.py   # перегенерировать иконку предмета
```

Первая сборка требует интернета (ForgeGradle скачивает MDK и маппинги). Сборка `0.3.4-alpha` проходит через `./gradlew build` (Java 21 toolchain used locally).

## Команды

| Команда | Права | Действие |
|---|---|---|
| `/hub`, `/aqualumen open` | все | открыть хаб |
| `/aqualumen open <игрок>` | 2 | открыть хаб другому игроку |
| `/aqualumen refresh` | все | принудительно обновить данные |
| `/aqualumen status` | 2 | сколько хабов открыто |
| `/aqualumen reload` | 3 | сбросить кэш |

Клавиша по умолчанию — **H**.

## Точки интеграции

Профиль читает статистику Minecraft, завершённые FTB Quests и монеты Lightman's Currency. Scoreboard остаётся приоритетным источником валюты. Ежедневные награды, кейсы и магазин ждут серверных интеграций.

## Лицензия

MIT. Не является официальным продуктом Minecraft и не связан с Mojang или с проектом LoliLand: совпадает только шаблон UX, ассеты и палитра собственные.

## Требования для сборки

- **JDK 17.** Нужен именно JDK, а не JRE: `javac -version` должен отвечать. Forge 1.20.1 на 21-й Java не собирается.
- **Gradle 8.1.1.** ForgeGradle 6 не работает на Gradle 8.7 и новее — падает на конфигурации. Если в системе стоит другая версия, обёртка (шаг выше) поставит нужную локально.
- **Обёртки в репозитории нет.** Файлы `gradlew`, `gradlew.bat` и `gradle/wrapper/` создаются командой `gradle wrapper --gradle-version 8.1.1` — бинарный `gradle-wrapper.jar` в исходники не коммитился.
- **Интернет на первую сборку.** ForgeGradle скачивает около гигабайта зависимостей и декомпилирует Minecraft: 10–20 минут и заметная нагрузка на диск. Дальше всё берётся из кэша.
- **Проверка результата.** `build/libs/aqualumen-forge-1.20.1-0.3.4-alpha.jar` — в `mods/` на сервере и в клиентскую сборку. `./gradlew runClient` поднимает клиент с модом без ручной установки.
