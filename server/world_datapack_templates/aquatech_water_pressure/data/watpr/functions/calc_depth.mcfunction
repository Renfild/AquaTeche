# run by in_water

scoreboard players set #temp watpr.depth 0
scoreboard players set #underneath watpr.depth 0

function watpr:ray_depth

# if underneath solid block(s), depth set to highest value
execute if score #underneath watpr.depth matches 1 run scoreboard players operation @s watpr.depth > #temp watpr.depth
execute if score #underneath watpr.depth matches 0 run scoreboard players operation @s watpr.depth = #temp watpr.depth