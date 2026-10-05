using System.Formats.Tar;
using System.IO.Compression;
using System.Security.Cryptography;

namespace AquaTechLauncher.Core;

/// <summary>
/// Ставит сборку Chromium (JCEF) для мода MCEF до запуска игры. Сам мод качает её с
/// mcef-download.cinemamod.com, а этот хост у части игроков из РФ недоступен: экран «MCEF is downloading» вечно стоит на 0%.
/// Лаунчер берёт ту же сборку с наших зеркал и кладёт туда, где её ищет мод.
/// </summary>
public static class JcefInstaller
{
    public const string Commit = "a78e832f9f13c2c688caea3d04d8b84fcd238d94";
    public const string Platform = "windows_amd64";
    public const string ArchiveSha256 = "E98C385542620F31A594D6FC3C38ED6BCA8A547E24CED982A1339BB3333668BE";
    public const long ArchiveSize = 124_309_275;

    // Файл-метка, которую мод записывает после распаковки: он сверяет её с хешем на зеркале.
    private const string ChecksumFileBase64 =
        "DQpBbGdvcml0aG0gICAgICAgSGFzaCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBQYXRoDQotLS0tLS0tLS0gICAgICAgLS0tLSAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAtLS0tDQpTSEEyNTYgICAgICAgICAgRTk4QzM4NTU0MjYyMEYzMUE1OTRENkZDM0MzOEVENkJDQThBNTQ3RTI0Q0VEOTgyQTEzMzlCQjMzMzM2NjhCRSAgICAgICBEOlxhXGphdmEtY2VmXGphdmEtY2VmXGpjZWZfYnVpbOKApg0KDQo=";

    public static readonly string[] ArchiveUrls =
    [
        $"https://aquateche.store/mcef/java-cef-builds/{Commit}/{Platform}.tar.gz",
        $"https://aquatech.santcrail.workers.dev/mcef/java-cef-builds/{Commit}/{Platform}.tar.gz",
        $"https://aquatech-7gs.pages.dev/mcef/java-cef-builds/{Commit}/{Platform}.tar.gz",
        $"https://github.com/Renfild/AquaTeche/releases/download/mcef-a78e832-1/{Platform}.tar.gz",
        $"https://mcef-download.cinemamod.com/java-cef-builds/{Commit}/{Platform}.tar.gz",
    ];

    private static readonly TimeSpan PerMirrorTimeout = TimeSpan.FromMinutes(4);

    public static string LibrariesRoot(string gameDir) => Path.Combine(gameDir, "mods", "mcef-libraries");

    public static string LibrariesDir(string gameDir) => Path.Combine(LibrariesRoot(gameDir), Platform);

    public static string ChecksumPath(string gameDir) => Path.Combine(LibrariesRoot(gameDir), Platform + ".tar.gz.sha256");

    public static string SettingsPath(string gameDir) => Path.Combine(gameDir, "config", "mcef", "mcef.properties");

    /// <summary>Готова ли распакованная сборка именно нужной версии.</summary>
    public static bool IsInstalled(string gameDir)
    {
        try
        {
            if (!File.Exists(Path.Combine(LibrariesDir(gameDir), "libcef.dll"))) return false;
            if (!File.Exists(Path.Combine(LibrariesDir(gameDir), "jcef.dll"))) return false;
            var mark = ChecksumPath(gameDir);
            return File.Exists(mark)
                   && File.ReadAllText(mark).Contains(ArchiveSha256, StringComparison.OrdinalIgnoreCase);
        }
        catch (IOException)
        {
            return false;
        }
    }

    /// <summary>
    /// skip-download=true: если хеш на зеркале не получить, мод не лезет качать сам и не виснет на 0%,
    /// а берёт то, что уже лежит на диске. Включаем только когда сборка точно на месте.
    /// </summary>
    public static void WriteSettings(string gameDir)
    {
        var path = SettingsPath(gameDir);
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        const string text =
            "#Managed by the AquaTech launcher: Chromium is installed before the game starts\n" +
            "use-cache=true\n" +
            "skip-download=true\n" +
            "download-mirror=https\\://aquateche.store/mcef\n" +
            "user-agent=null\n";
        if (File.Exists(path) && File.ReadAllText(path) == text) return;
        File.WriteAllText(path, text);
    }

    public static async Task EnsureAsync(
        string gameDir,
        Action<string, string> log,
        Action<double> progress,
        CancellationToken ct = default)
    {
        if (!OperatingSystem.IsWindows()) return;
        if (IsInstalled(gameDir))
        {
            WriteSettings(gameDir);
            return;
        }

        log("Ставим Chromium для меню F4 (один раз, около 125 МБ)…", "info");
        var root = LibrariesRoot(gameDir);
        Directory.CreateDirectory(root);
        var part = Path.Combine(root, Platform + ".tar.gz.part");

        string? lastError = null;
        var downloaded = false;
        foreach (var url in ArchiveUrls)
        {
            ct.ThrowIfCancellationRequested();
            try
            {
                log($"Chromium: {new Uri(url).Host}…", "dim");
                using var cts = CancellationTokenSource.CreateLinkedTokenSource(ct);
                cts.CancelAfter(PerMirrorTimeout);
                await HttpDownload.DownloadAsync(url, part, cts.Token);
                if (!ArchiveMatches(part))
                {
                    lastError = $"{new Uri(url).Host}: файл не совпал по хешу";
                    TryDelete(part);
                    continue;
                }
                downloaded = true;
                break;
            }
            catch (OperationCanceledException) when (!ct.IsCancellationRequested)
            {
                lastError = $"{new Uri(url).Host}: таймаут";
                TryDelete(part);
            }
            catch (Exception ex) when (ex is HttpRequestException or IOException or TaskCanceledException)
            {
                lastError = $"{new Uri(url).Host}: {ex.Message}";
                TryDelete(part);
            }
        }

        if (!downloaded)
        {
            // Не роняем запуск: мод сам попробует скачать, как раньше.
            log($"Chromium не скачался ({lastError}). Игра запустится, но меню F4 может не открыться.", "warn");
            return;
        }

        Extract(part, root, gameDir);
        TryDelete(part);
        File.WriteAllBytes(ChecksumPath(gameDir), Convert.FromBase64String(ChecksumFileBase64));
        WriteSettings(gameDir);
        log("Chromium установлен.", "ok");
    }

    public static bool ArchiveMatches(string path)
    {
        if (!File.Exists(path)) return false;
        using var sha = SHA256.Create();
        using var fs = File.OpenRead(path);
        var hash = Convert.ToHexString(sha.ComputeHash(fs));
        return string.Equals(hash, ArchiveSha256, StringComparison.OrdinalIgnoreCase);
    }

    private static void Extract(string archive, string root, string gameDir)
    {
        var target = LibrariesDir(gameDir);
        if (Directory.Exists(target)) Directory.Delete(target, recursive: true);
        using var fs = File.OpenRead(archive);
        using var gz = new GZipStream(fs, CompressionMode.Decompress);
        TarFile.ExtractToDirectory(gz, root, overwriteFiles: true);
    }

    private static void TryDelete(string path)
    {
        try
        {
            if (File.Exists(path)) File.Delete(path);
        }
        catch (IOException)
        {
            // файл займёт следующая попытка
        }
    }
}
