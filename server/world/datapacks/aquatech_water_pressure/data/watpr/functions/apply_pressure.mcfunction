# run by in_water

scoreboard players operation #temp watpr.depth = @s watpr.depth
# initiate vars 
execute store result score #armor watpr.depth run attribute @s generic.armor get
execute store result score #toughness watpr.depth run attribute @s generic.armor_toughness get

# apply multipliers
scoreboard players operation #armor watpr.depth *= $armor_mult watpr
scoreboard players operation #armor watpr.depth *= $total_mult watpr
scoreboard players operation #armor watpr.depth /= #10000 watpr
scoreboard players operation #toughness watpr.depth *= $toughness_mult watpr
scoreboard players operation #toughness watpr.depth *= $total_mult watpr
scoreboard players operation #toughness watpr.depth /= #10000 watpr

# apply offsets
scoreboard players operation #temp watpr.depth -= #armor watpr.depth
scoreboard players operation #temp watpr.depth -= #toughness watpr.depth
scoreboard players operation #temp watpr.depth -= $offset watpr

# apply damage/effects
execute if score $damage_diff_scaling watpr matches 0 run function watpr:damage
execute if score $damage_diff_scaling watpr matches 1 run function watpr:damage_diff

execute if score #temp watpr.depth matches 1.. run tp @s @s
execute if score #temp watpr.depth matches 6.. run effect give @s nausea 7 0 true
execute if score #temp watpr.depth matches 16.. run effect give @s blindness 2 0 true