# water (fluid) +1 depth
# solid (not ignore) -2 depth
# air -- finish

# store result used for optimization
execute store success score #water watpr.depth if predicate watpr:water
scoreboard players operation #temp watpr.depth += #water watpr.depth

execute if score #water watpr.depth matches 0 if block ~ ~ ~ #watpr:air run return 1
execute if score #water watpr.depth matches 0 unless block ~ ~ ~ #watpr:depth_ignore run function watpr:ray_remove_depth

execute positioned ~ ~1 ~ run function watpr:ray_depth