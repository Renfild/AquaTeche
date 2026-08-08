# Маршрут Горизонта — AquaTech meta-progression

Параллельная дорога поверх FTB Acts (ID квестов сюжета не ломаем).

## Горизонты → LuckPerms

| Tier | Звание | LP group | Homes |
|------|--------|----------|-------|
| H0 | Пролог | default | 1 |
| H1 | Матрос | sailor | 2 |
| H2 | Шкипер | skipper | 3 |
| H3 | Капитан | captain | 4 + fly |
| H4 | Адмирал | admiral | 6 + nick |
| H5 | Легенда | legend | 8 + hat |

Повышение: квесты главы `00_horizon_route` → `/aquatech promote @p N`  
При promote/settier старые флотские группы **снимаются**, затем выдаётся целевая.  
Track LP: `horizon` (`/lp track info horizon`).

## Команды игрока

- `/aquatech horizon` — текущий Горизонт
- `/aquatech daily` — контракт дня (прогресс / сдача → Aqua XP + XP сезона)
- `/aquatech season` — уровень сезона

## Команды админа

- `/aquatech settier <player> <0-5>`
- `/aquatech promote <player> <1-5>`
- `/aquatech grantxp <player> <n>`
- `/aquatech storm on|off` — ручной FORCE override
- `/aquatech storm auto` — вернуть авто-расписание
- `/aquatech storm status` — активен? режим? следующий переход

## Шторм выходного дня (авто)

По умолчанию **AUTO**: активен **Пт–Вс** по `Europe/Moscow` (config `aquatech_ui-server.toml` → `[storm]`).  
Пн–Чт выключается сам. Редкий улов ×2, пока активен.  
Ручной `on`/`off` держится до `storm auto` или смены режима (SavedData мира).

## Контракты дня

| Тип | Условие |
|-----|---------|
| Улов | улов удочкой **AquaTech** |
| Глубина | effective давление ≥ 8 (после брони) |
| Водоросли | ломать kelp / seagrass |
| Цех | рядом с **работающей** машиной AquaTech |
| Рынок | иметь N медных монет Lightman's |

## Варпы

Два основных: `spawn` · `shop` (casesmod F4 + Essentials).

Файлы в `server/plugins/Essentials/warps/`.  
При необходимости: `/setwarp spawn` и `/setwarp shop` на точке.

## FTB

Глава **★ Маршрут Горизонта** (`00_horizon_route.snbt`), group `0AC7A00000000000`.  
Новые ID: `HF…` — безопасны для freeze spine.

## Сезон

Daily даёт Aqua XP и XP сезона (`/aquatech season`).  
Отдельной валюты «жетоны» больше нет.

## Чеклист после деплоя (smoke H0)

1. Перезапуск сервера (мод + LP groups/track + FTB chapter)
2. `/lp sync` · `/lp track info horizon`
3. `/aquatech storm status` — проверить AUTO / Пт–Вс
4. На точках мира: `/setwarp spawn` и `/setwarp shop` (если нужно обновить координаты)
5. DecentHolograms: `/dh reload` · поправить `harbor_guide` координаты
6. `/ajlb add …` по `server/plugins/ajLeaderboards/AQUATECH_BOARDS.yml`
7. Тест: `/aquatech daily` сдаёт контракт → XP
8. Тест нового игрока ≤20 мин: H0→Матрос + 1× daily

Скрипт-памятка: `setup_horizon_route.ps1`
