// AquaTech: Fish Stocking — океан вокруг игроков должен быть живым.
// Ванильная капа воды мала, преген Chunky оставляет чанки без сущностей.
// Раз в 20 секунд рядом с игроком в столбе воды спавнятся стаи.

const VANILLA_SCHOOLS = [
    { type: 'minecraft:cod', count: 3 },
    { type: 'minecraft:salmon', count: 3 },
    { type: 'minecraft:tropical_fish', count: 2 },
    { type: 'minecraft:pufferfish', count: 1 },
    { type: 'minecraft:squid', count: 2 },
    { type: 'minecraft:dolphin', count: 1 }
]

const AC_SCHOOLS = [
    { type: 'alexscaves:lanternfish', count: 3 },
    { type: 'alexscaves:tripodfish', count: 2 },
    { type: 'alexscaves:sea_pig', count: 2 },
    { type: 'alexscaves:trilocaris', count: 2 },
    { type: 'alexscaves:gossamer_worm', count: 1 }
]

function blockId(block) {
    if (!block) return ''
    try {
        let id = block.id
        if (typeof id === 'function') id = block.id()
        return String(id)
    } catch (err) {
        return ''
    }
}

function isWaterBlock(block) {
    let id = blockId(block)
    return id === 'minecraft:water' || id === 'minecraft:bubble_column' || id.endsWith(':water')
}

function findWaterSpot(level, px, py, pz, random) {
    for (let attempt = 0; attempt < 10; attempt++) {
        let dx = random.nextInt(48) - 24
        let dz = random.nextInt(48) - 24
        if (Math.abs(dx) < 6 && Math.abs(dz) < 6) continue
        let x = Math.floor(px + dx)
        let z = Math.floor(pz + dz)
        let yStart = Math.floor(py) + 8
        let yEnd = Math.floor(py) - 24
        for (let y = yStart; y >= yEnd; y--) {
            if (!isWaterBlock(level.getBlock(x, y, z))) continue
            // школа внутри столба воды, не только на поверхности
            return { x: x + 0.5, y: y + 0.4, z: z + 0.5 }
        }
    }
    return null
}

let stockingCounter = 0

ServerEvents.tick(event => {
    stockingCounter++
    if (stockingCounter % 400 !== 0) return

    let server = event.server
    server.players.forEach(player => {
        if (!player.level) return
        let random = player.level.random
        let groups = 3
        for (let g = 0; g < groups; g++) {
            let spot = findWaterSpot(player.level, player.x, player.y, player.z, random)
            if (!spot) continue
            let pool = (random.nextDouble() < 0.6) ? VANILLA_SCHOOLS : AC_SCHOOLS
            let school = pool[random.nextInt(pool.length)]
            if (school.type === 'minecraft:dolphin' && random.nextDouble() > 0.2) continue
            for (let i = 0; i < school.count; i++) {
                let ox = spot.x + (random.nextDouble() - 0.5) * 2.5
                let oz = spot.z + (random.nextDouble() - 0.5) * 2.5
                server.runCommandSilent(`summon ${school.type} ${ox.toFixed(1)} ${spot.y.toFixed(1)} ${oz.toFixed(1)}`)
            }
        }
    })
})
