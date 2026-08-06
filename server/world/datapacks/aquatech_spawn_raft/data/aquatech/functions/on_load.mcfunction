# AquaTech: Ocean Raft spawn platform (rebuilt every world load, harmless if already there)
setblock -1 189 -1 minecraft:oak_log[axis=x] replace
setblock 0 189 -1 minecraft:oak_log[axis=x] replace
setblock 1 189 -1 minecraft:oak_log[axis=x] replace
setblock -1 189 0 minecraft:oak_log[axis=z] replace
setblock 0 189 0 minecraft:oak_planks replace
setblock 1 189 0 minecraft:oak_log[axis=z] replace

setworldspawn 0 190 0 180
