execute as @a[tag=watpr.in_water,gamemode=!creative,gamemode=!spectator] unless predicate watpr:in_vehicle_blacklist at @s run function watpr:in_water

schedule function watpr:main_5 5t replace