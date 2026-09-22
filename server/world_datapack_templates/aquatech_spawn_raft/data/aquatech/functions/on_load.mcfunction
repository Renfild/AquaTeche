# AquaTech: Ocean Raft spawn (deck at Y=189, players stand at 190)
# Rebuilt on every world load; every command is idempotent (replace).

# --- deck + rim ---
fill -7 189 -7 7 189 7 minecraft:spruce_planks replace
fill -7 189 -7 7 189 -7 minecraft:stripped_spruce_log[axis=x] replace
fill -7 189 7 7 189 7 minecraft:stripped_spruce_log[axis=x] replace
fill -7 189 -6 -7 189 6 minecraft:stripped_spruce_log[axis=z] replace
fill 7 189 -6 7 189 6 minecraft:stripped_spruce_log[axis=z] replace

# --- central plaza ---
fill -3 189 -3 3 189 3 minecraft:stone_bricks replace
fill -1 189 -1 1 189 1 minecraft:polished_andesite replace
setblock -3 189 -3 minecraft:sea_lantern replace
setblock 3 189 -3 minecraft:sea_lantern replace
setblock -3 189 3 minecraft:sea_lantern replace
setblock 3 189 3 minecraft:sea_lantern replace

# --- corner light posts ---
setblock -6 190 -6 minecraft:spruce_fence replace
setblock -6 191 -6 minecraft:spruce_fence replace
setblock -6 192 -6 minecraft:lantern replace
setblock 6 190 -6 minecraft:spruce_fence replace
setblock 6 191 -6 minecraft:spruce_fence replace
setblock 6 192 -6 minecraft:lantern replace
setblock -6 190 6 minecraft:spruce_fence replace
setblock -6 191 6 minecraft:spruce_fence replace
setblock -6 192 6 minecraft:lantern replace
setblock 6 190 6 minecraft:spruce_fence replace
setblock 6 191 6 minecraft:spruce_fence replace
setblock 6 192 6 minecraft:lantern replace

# --- info hut (west side, open toward the plaza) ---
fill -6 190 -1 -6 192 1 minecraft:spruce_planks replace
fill -5 190 -1 -4 192 -1 minecraft:spruce_planks replace
fill -5 190 1 -4 192 1 minecraft:spruce_planks replace
fill -6 193 -1 -4 193 1 minecraft:spruce_slab[type=top] replace
fill -6 190 -1 -6 192 1 minecraft:spruce_planks replace
setblock -5 192 0 minecraft:lantern[hanging=true] replace
setblock -4 190 0 minecraft:air replace
setblock -5 190 0 minecraft:barrel{Items:[{Slot:0b,id:"minecraft:bread",Count:8b},{Slot:1b,id:"minecraft:ladder",Count:16b},{Slot:2b,id:"starcatcher:tackle_box",Count:1b},{Slot:3b,id:"aquatech_ui:bait_shoal",Count:8b}]} replace

# --- south dock ---
fill -1 189 8 1 189 12 minecraft:spruce_planks replace
fill -1 189 13 1 189 13 minecraft:stripped_spruce_log[axis=x] replace
setblock -1 190 13 minecraft:spruce_fence replace
setblock 1 190 13 minecraft:spruce_fence replace
setblock 0 190 13 minecraft:lantern replace
fill -2 189 10 -2 189 11 minecraft:spruce_planks replace
fill 2 189 10 2 189 11 minecraft:spruce_planks replace

# --- spawn point ---
setworldspawn 0 190 0 180
