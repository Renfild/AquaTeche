namespace AquaTechLauncher.Core;

public sealed class PlayOrchestrator
{
    private readonly JavaLocator _java = new();
    private readonly ForgeInstaller _forge = new();
    private readonly ManifestSync _sync = new();
    private readonly LaunchCommandBuilder _launch = new();

    public Task<PackManifest> FetchManifestAsync(string updateUrl, Action<string>? log = null, CancellationToken ct = default)
    {
        return _sync.FetchManifestAsync(updateUrl, log, ct);
    }

    public async Task PlayAsync(
        LauncherConfig cfg,
        Action<string, string> log,
        Action<double> progress,
        CancellationToken ct = default)
    {
        var gameDir = cfg.GameDir;
        var username = cfg.Username.Trim();
        if (string.IsNullOrEmpty(username))
            throw new InvalidOperationException("Введи никнейм");

        foreach (var sub in new[] { "mods", "config", "kubejs", "resourcepacks", "logs", "versions", "libraries", "assets" })
            Directory.CreateDirectory(Path.Combine(gameDir, sub));

        progress(3);
        var java = await _java.EnsureJava17Async(gameDir, m => log(m, "info"), progress, ct);
        log($"Java 17: {java}", "ok");
        progress(10);

        var forgeClient = Path.Combine(gameDir, "libraries", "net", "minecraftforge", "forge",
            LauncherConstants.ForgeCoord, $"forge-{LauncherConstants.ForgeCoord}-client.jar");
        var vanillaJar = Path.Combine(gameDir, "versions", LauncherConstants.McVersion, LauncherConstants.McVersion + ".jar");
        var warm = _forge.FindForgeVersionId(gameDir) != null
                   && File.Exists(forgeClient)
                   && File.Exists(vanillaJar) && new FileInfo(vanillaJar).Length > 10_000_000;

        if (warm)
            log("Minecraft/Forge уже установлены — быстрый старт", "ok");
        else
        {
            log($"Готовим Minecraft {LauncherConstants.McVersion} / Forge…", "info");
            await _forge.EnsureReadyAsync(gameDir, java, m => log(m, "info"), ct);
            log($"Forge: {_forge.FindForgeVersionId(gameDir)}", "ok");
        }
        progress(30);

        log("Синхронизируем сборку…", "info");
        await SyncPackAsync(cfg, verifyHash: true, skipIfReady: false, log, p => progress(30 + p * 0.55), ct);
        progress(88);

        log("Собираем classpath / natives / assets…", "info");
        var autoJoin = $"{cfg.EffectiveHost}:{cfg.EffectivePort}";
        var sessionToken = cfg.PortalSession ?? HttpDownload.GetPortalSession();
        var cmd = await _launch.BuildAsync(gameDir, username, cfg.RamMb, java, autoJoin, sessionToken, m => log(m, "dim"), ct);
        var proc = ProcessSpawner.Spawn(cmd, gameDir);
        log($"Процесс Minecraft PID {proc.Id} — проверяем…", "dim");

        var polls = warm ? 8 : 16;
        for (var i = 0; i < polls; i++)
        {
            await Task.Delay(500, ct);
            if (proc.HasExited)
            {
                log($"Minecraft сразу закрылся (код {proc.ExitCode})", "err");
                DumpTail(Path.Combine(gameDir, "logs", "minecraft_console.log"), log);
                DumpTail(Path.Combine(gameDir, "logs", "latest.log"), log);
                throw new InvalidOperationException($"Minecraft exit {proc.ExitCode}");
            }
        }

        progress(100);
        log("Игра запущена. Лаунчер можно свернуть — Minecraft работает отдельно.", "ok");
    }

    public async Task UpdateAsync(
        LauncherConfig cfg,
        Action<string, string> log,
        Action<double> progress,
        CancellationToken ct = default)
    {
        var gameDir = cfg.GameDir;
        foreach (var sub in LauncherConstants.PackFolders)
            Directory.CreateDirectory(Path.Combine(gameDir, sub));

        log("Проверяем обновления сборки…", "info");
        await SyncPackAsync(cfg, verifyHash: true, skipIfReady: false, log, progress, ct);
        progress(100);
        log("Сборка обновлена. Можно играть.", "ok");
    }

    private async Task SyncPackAsync(
        LauncherConfig cfg,
        bool verifyHash,
        bool skipIfReady,
        Action<string, string> log,
        Action<double> progress,
        CancellationToken ct)
    {
        if (skipIfReady && ManifestSync.PackLooksReady(cfg.GameDir))
        {
            log("Сборка уже на месте — пропускаем полную проверку", "ok");
            progress(100);
            return;
        }

        var man = await _sync.FetchManifestAsync(cfg.UpdateUrl, m => log(m, "info"), ct);
        var (updated, failed, deleted) = await _sync.ApplyAsync(
            cfg.GameDir, man, verifyHash, m => log(m, "info"), progress, ct);
        if (failed > 0)
            throw new IOException($"Синхронизация не удалась ({failed} ошибок). Проверь интернет и нажми Играть ещё раз.");
        if (deleted > 0 || updated > 0)
            log($"Синхронизация ок: +{updated}, −{deleted}", "ok");
        else
            log($"Сборка актуальна ({man.Files.Count} файлов)", "ok");
    }

    private static void DumpTail(string path, Action<string, string> log)
    {
        try
        {
            if (!File.Exists(path) || new FileInfo(path).Length < 8) return;
            log($"— {Path.GetFileName(path)}: {path}", "warn");
            foreach (var line in File.ReadLines(path).TakeLast(20))
                log(line.Length > 220 ? line[..220] : line, "dim");
        }
        catch
        {
            /* ignore */
        }
    }
}
