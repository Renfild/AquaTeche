advancement revoke @s only aqrod:catch
scoreboard players set @s aqrod.tier 1
execute if data entity @s SelectedItem.tag.aqRodTier store result score @s aqrod.tier run data get entity @s SelectedItem.tag.aqRodTier 1
function aqrod:catch/convert
execute if score @s aqrod.tier matches 1 run loot give @s loot aqrod:rod_tier_1
execute if score @s aqrod.tier matches 2 run loot give @s loot aqrod:rod_tier_2
execute if score @s aqrod.tier matches 3 run loot give @s loot aqrod:rod_tier_3
execute if score @s aqrod.tier matches 4 run loot give @s loot aqrod:rod_tier_4
execute if score @s aqrod.tier matches 5 run loot give @s loot aqrod:rod_tier_5
execute if score @s aqrod.tier matches 6 run loot give @s loot aqrod:rod_tier_6
execute if score @s aqrod.tier matches 7 run loot give @s loot aqrod:rod_tier_7
execute if score @s aqrod.tier matches 8 run loot give @s loot aqrod:rod_tier_8
execute if score @s aqrod.tier matches 9 run loot give @s loot aqrod:rod_tier_9
execute if score @s aqrod.tier matches 10 run loot give @s loot aqrod:rod_tier_10
