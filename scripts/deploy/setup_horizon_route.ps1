# Setup Horizon Route warps + boards (run as OP in-game after world load)

Write-Host @"
AquaTech Horizon Route — Phase 1 in-game setup
==============================================
0) After jar deploy: restart server, then:
   /lp sync
   /lp track info horizon

1) Stand at hub spawn:
   /setwarp spawn
2) Fishing pier:
   /setwarp pier
3) Lightman's market area:
   /setwarp market
4) Starter atoll / island:
   /setwarp atoll
5) Harbor plaza (hologram):
   /setwarp harbor
   /dh reload
   (edit DecentHolograms holograms/harbor_guide.yml coords if needed)

6) Leaderboards (once):
   /ajlb add statistic_fish_caught alltime
   /ajlb add statistic_fish_caught daily
   /ajlb add statistic_play_one_minute alltime
   /ajlb add statistic_swim_one_cm alltime
   /ajlb add statistic_walk_on_water_one_cm alltime

7) Essentials homes for shop item 'home':
   In plugins/Essentials/config.yml under sethome-multiple add:
     tide: 5

8) Weekend storm (AUTO Fri–Sun Europe/Moscow):
   /aquatech storm status
   /aquatech storm auto
   (manual override: /aquatech storm on|off)

9) Smoke test (new player / alt):
   - Open guidebook + FTB chapter «Маршрут Горизонта»
   - Finish H0 → promote sailor
   - HUD shows contract N/M
   - /aquatech daily claim

Player tips: /aquatech daily · horizon · season · shop
Warps: pier · market · atoll · harbor
"@
