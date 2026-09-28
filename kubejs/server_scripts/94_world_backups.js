// Автобэкап миров: после старта сервера, если свежего бэкапа нет, копируем миры в backups/.
// Сервер перезапускается ежедневно в 04:00 по расписанию панели — значит бэкап раз в сутки.
// Держим последние 3 бэкапа. Ручной запуск: /atbackup (нужен OP).
// KubeJS блокирует java.io/java.nio/java.util.zip классами, а Rhino путается в перегрузках
// resolve()/getFile(). Поэтому пути строим одним вызовом FileSystem.getPath(...),
// копирование/удаление — через Guava MoreFiles, листинг — через File-объекты из listFiles().

let FileUtils = Java.loadClass('org.apache.commons.io.FileUtils')
let LevelResource = Java.loadClass('net.minecraft.world.level.storage.LevelResource')
let Bukkit = Java.loadClass('org.bukkit.Bukkit')

const BACKUP_DIR = 'backups'
const WORLD_DIRS = ['world', 'world_nether', 'world_the_end']
const KEEP = 3
const MIN_AGE_MS = 20 * 60 * 60 * 1000
const START_DELAY_TICKS = 20 * 60

function pad2(n) {
    return n < 10 ? '0' + n : '' + n
}

function stamp() {
    let d = new Date()
    return `${d.getFullYear()}${pad2(d.getMonth() + 1)}${pad2(d.getDate())}_${pad2(d.getHours())}${pad2(d.getMinutes())}`
}

function fsOf(server) {
    return server.getWorldPath(LevelResource.ROOT).getFileSystem()
}

function rootAbs() {
    return Bukkit.getWorldContainer().getAbsolutePath()
}

function backupsPath(server) {
    return fsOf(server).getPath(rootAbs(), BACKUP_DIR)
}

function backupsDir(server) {
    let dir = backupsPath(server).toFile()
    if (!dir.isDirectory()) dir.mkdirs()
    return dir
}

function archives(server) {
    let list = []
    let kids = backupsDir(server).listFiles()
    if (kids) {
        for (let i = 0; i < kids.length; i++) {
            let name = kids[i].getName()
            if (kids[i].isDirectory() && name.startsWith('world-')) list.push(kids[i])
        }
    }
    list.sort((a, b) => b.getName().compareTo(a.getName()))
    return list
}

function rotate(server) {
    let list = archives(server)
    for (let i = KEEP; i < list.length; i++) {
        console.info(`[backup] удаляю старый бэкап ${list[i].getName()}`)
        FileUtils.deleteDirectory(list[i])
    }
}

function makeBackup(server) {
    let started = Date.now()
    let name = 'world-' + stamp()
    let targetPath = fsOf(server).getPath(rootAbs(), BACKUP_DIR, name)
    console.info(`[backup] копирую ${WORLD_DIRS.join(', ')} -> ${targetPath}`)
    server.runCommandSilent('save-all flush')
    for (let i = 0; i < WORLD_DIRS.length; i++) {
        let src = fsOf(server).getPath(rootAbs(), WORLD_DIRS[i])
        if (src.toFile().isDirectory()) {
            FileUtils.copyDirectory(src.toFile(), fsOf(server).getPath(rootAbs(), BACKUP_DIR, name, WORLD_DIRS[i]).toFile())
        }
    }
    console.info(`[backup] готово: ${name} за ${Math.round((Date.now() - started) / 1000)}s`)
    rotate(server)
}

function maybeBackup(server) {
    let list = archives(server)
    if (list.length && Date.now() - list[0].lastModified() < MIN_AGE_MS) {
        console.info('[backup] свежий бэкап уже есть, пропускаю')
        return
    }
    makeBackup(server)
}

ServerEvents.loaded(event => {
    event.server.scheduleInTicks(START_DELAY_TICKS, () => maybeBackup(event.server))
})

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event
    event.register(
        Commands.literal('atbackup')
            .requires(source => source.hasPermission(4))
            .executes(ctx => {
                ctx.source.server.scheduleInTicks(1, () => makeBackup(ctx.source.server))
                ctx.source.sendSystemMessage(Text.gold('[backup] бэкап запущен, результат в консоли'))
                return 1
            })
    )
})
