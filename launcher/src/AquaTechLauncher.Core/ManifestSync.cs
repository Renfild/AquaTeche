using System.Text.Json;
using System.Text.Json.Serialization;

namespace AquaTechLauncher.Core;

public sealed class PackManifest
{
    [JsonPropertyName("version")]
    public string? Version { get; set; }

    [JsonPropertyName("files")]
    public List<PackFileEntry> Files { get; set; } = [];
}

public sealed class PackFileEntry
{
    [JsonPropertyName("path")]
    public string Path { get; set; } = "";

    [JsonPropertyName("md5")]
    public string? Md5 { get; set; }

    [JsonPropertyName("size")]
    public long Size { get; set; }

    [JsonPropertyName("url")]
    public string? Url { get; set; }
}

public sealed class ManifestSync
{
    /// <summary>True when local file is missing or does not match size/hash rules.</summary>
    public static bool NeedsDownload(string localPath, PackFileEntry item, bool verifyHash)
    {
        if (!File.Exists(localPath))
            return true;
        var sameSize = item.Size <= 0 || new FileInfo(localPath).Length == item.Size;
        if (!sameSize)
            return true;
        if (!verifyHash)
            return false;
        if (string.IsNullOrEmpty(item.Md5))
            return false;
        return !string.Equals(
            HttpDownload.Md5File(localPath),
            item.Md5,
            StringComparison.OrdinalIgnoreCase);
    }

    public async Task<(int Updated, int Failed, int Deleted)> ApplyAsync(
        string gameDir,
        PackManifest manifest,
        bool verifyHash,
        Action<string>? log = null,
        Action<double>? progress = null,
        CancellationToken ct = default)
    {
        var files = manifest.Files;
        if (files.Count == 0)
        {
            log?.Invoke("Манифест пустой — нечего синхронизировать");
            return (0, 0, 0);
        }

        PreCleanStaleFirstParty(gameDir, log);
        var wanted = new HashSet<string>(
            files.Select(f => f.Path.Replace('\\', '/').TrimStart('/')),
            StringComparer.OrdinalIgnoreCase);

        var jobs = new List<PackFileEntry>();
        var checkedN = 0;
        log?.Invoke($"Проверяем целостность ({files.Count} файлов сборки v{manifest.Version})…");
        progress?.Invoke(5);

        foreach (var item in files)
        {
            ct.ThrowIfCancellationRequested();
            var rel = item.Path.Replace('\\', '/').TrimStart('/');
            var local = System.IO.Path.Combine(gameDir, rel.Replace('/', System.IO.Path.DirectorySeparatorChar));
            checkedN++;
            if (checkedN % 25 == 0)
                progress?.Invoke(5 + 25.0 * checkedN / files.Count);

            if (NeedsDownload(local, item, verifyHash))
                jobs.Add(item);
        }

        if (jobs.Count == 0)
        {
            var deletedOnly = PurgeExtras(gameDir, wanted, log);
            SavePackVersion(gameDir, manifest.Version);
            log?.Invoke($"Сборка v{manifest.Version} актуальна ({files.Count} файлов)" + (deletedOnly > 0 ? $", удалено {deletedOnly}" : ""));
            progress?.Invoke(100);
            return (0, 0, deletedOnly);
        }

        log?.Invoke($"Обновляем {jobs.Count}/{files.Count} файлов сборки v{manifest.Version}…");
        progress?.Invoke(35);

        var updated = 0;
        var failed = 0;
        var done = 0;
        using var gate = new SemaphoreSlim(10);
        var tasks = jobs.Select(async item =>
        {
            await gate.WaitAsync(ct);
            try
            {
                var relName = System.IO.Path.GetFileName(item.Path);
                var ok = await DownloadOneAsync(gameDir, item, ct);
                Interlocked.Increment(ref done);
                if (ok) Interlocked.Increment(ref updated);
                else
                {
                    Interlocked.Increment(ref failed);
                    log?.Invoke($"Ошибка загрузки: {relName}");
                }
                var d = done;
                if (d == 1 || d % 5 == 0 || d == jobs.Count)
                {
                    progress?.Invoke(35 + 60.0 * d / jobs.Count);
                    log?.Invoke($"↓ [{d}/{jobs.Count}] {relName}…");
                }
            }
            finally
            {
                gate.Release();
            }
        });
        await Task.WhenAll(tasks);

        // Always purge unwanted files (especially stale mods)
        var deleted = PurgeExtras(gameDir, wanted, log);
        if (failed == 0)
        {
            SavePackVersion(gameDir, manifest.Version);
        }

        log?.Invoke(failed > 0
            ? $"Синхронизация v{manifest.Version}: {updated} ок, {failed} ошибок" + (deleted > 0 ? $", −{deleted}" : "")
            : $"Сборка v{manifest.Version} обновлена: {updated}/{files.Count}" + (deleted > 0 ? $", удалено {deleted}" : ""));
        progress?.Invoke(100);
        return (updated, failed, deleted);
    }

    private static void SavePackVersion(string gameDir, string? version)
    {
        try
        {
            if (!string.IsNullOrWhiteSpace(version))
            {
                File.WriteAllText(System.IO.Path.Combine(gameDir, ".pack_version"), version.Trim());
            }
        }
        catch { /* ignore */ }
    }

    public async Task<PackManifest> FetchManifestAsync(string updateBase, Action<string>? log = null, CancellationToken ct = default)
    {
        var bases = new List<string>();
        foreach (var m in LauncherConstants.PackCdnMirrors)
            if (!bases.Contains(m, StringComparer.OrdinalIgnoreCase))
                bases.Add(m);
        if (!string.IsNullOrWhiteSpace(updateBase) && !bases.Contains(updateBase.TrimEnd('/'), StringComparer.OrdinalIgnoreCase))
            bases.Add(updateBase.TrimEnd('/'));

        Exception? last = null;
        foreach (var b in bases)
        {
            var url = $"{b.TrimEnd('/')}/manifest.json?_t={DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";
            try
            {
                log?.Invoke($"Манифест: {url}");
                var json = await HttpDownload.GetStringAsync(url, ct);
                var man = JsonSerializer.Deserialize<PackManifest>(json)
                          ?? throw new InvalidDataException("bad manifest");
                if (man.Files.Count == 0) throw new InvalidDataException("empty files");
                return man;
            }
            catch (Exception ex)
            {
                last = ex;
            }
        }
        throw last ?? new IOException("Не удалось скачать манифест");
    }

    private static async Task<bool> DownloadOneAsync(string gameDir, PackFileEntry item, CancellationToken ct)
    {
        var rel = item.Path.Replace('\\', '/').TrimStart('/');
        var local = System.IO.Path.Combine(gameDir, rel.Replace('/', System.IO.Path.DirectorySeparatorChar));
        Directory.CreateDirectory(System.IO.Path.GetDirectoryName(local)!);

        var urls = new List<string>();
        if (!string.IsNullOrWhiteSpace(item.Url))
            urls.Add(item.Url.Trim());
        foreach (var mirror in LauncherConstants.PackCdnMirrors)
        {
            var mUrl = $"{mirror.TrimEnd('/')}/{rel}";
            if (!urls.Contains(mUrl, StringComparer.OrdinalIgnoreCase))
                urls.Add(mUrl);
        }

        foreach (var url in urls)
        {
            try
            {
                await HttpDownload.DownloadAsync(url, local, ct);
                if (!string.IsNullOrEmpty(item.Md5))
                {
                    var got = HttpDownload.Md5File(local);
                    if (!got.Equals(item.Md5, StringComparison.OrdinalIgnoreCase))
                    {
                        File.Delete(local);
                        continue;
                    }
                }
                if (item.Size > 0 && new FileInfo(local).Length != item.Size)
                {
                    File.Delete(local);
                    continue;
                }
                return true;
            }
            catch
            {
                try { if (File.Exists(local)) File.Delete(local); } catch { /* ignore */ }
            }
        }
        return false;
    }

    public static void PreCleanStaleFirstParty(string gameDir, Action<string>? log)
    {
        try
        {
            var modsDir = System.IO.Path.Combine(gameDir, "mods");
            if (Directory.Exists(modsDir))
            {
                foreach (var f in Directory.EnumerateFiles(modsDir, "*", SearchOption.TopDirectoryOnly))
                {
                    var name = System.IO.Path.GetFileName(f).ToLowerInvariant();
                    if (name.Contains("casesmod") || name.Contains("_parked") || name.EndsWith(".disabled"))
                    {
                        try
                        {
                            File.SetAttributes(f, FileAttributes.Normal);
                            File.Delete(f);
                            log?.Invoke($"Очищен устаревший мод: {System.IO.Path.GetFileName(f)}");
                        }
                        catch { }
                    }
                }
            }
            var cfgCases = System.IO.Path.Combine(gameDir, "config", "casesmod");
            if (Directory.Exists(cfgCases))
            {
                try { Directory.Delete(cfgCases, true); } catch { }
            }
        }
        catch { /* ignore */ }
    }

    public static int PurgeExtras(string gameDir, HashSet<string> wanted, Action<string>? log)
    {
        PreCleanStaleFirstParty(gameDir, log);
        var deleted = 0;
        foreach (var folder in LauncherConstants.PackFolders)
        {
            var root = System.IO.Path.Combine(gameDir, folder);
            if (!Directory.Exists(root)) continue;
            foreach (var file in Directory.EnumerateFiles(root, "*", SearchOption.AllDirectories))
            {
                var rel = System.IO.Path.GetRelativePath(gameDir, file).Replace('\\', '/');
                var name = System.IO.Path.GetFileName(file);
                if (LauncherConstants.SyncKeepNames.Contains(name)) continue;
                if (wanted.Contains(rel)) continue;
                // Parked files or stale mods/configs
                try
                {
                    File.SetAttributes(file, FileAttributes.Normal);
                    File.Delete(file);
                    deleted++;
                    log?.Invoke($"Удален устаревший файл: {rel}");
                }
                catch (Exception ex)
                {
                    log?.Invoke($"Предупреждение: не удалось удалить {rel} ({ex.Message})");
                }
            }
        }
        if (deleted > 0) log?.Invoke($"Удалено устаревших файлов: {deleted}");
        return deleted;
    }

    public static bool PackLooksReady(string gameDir)
    {
        var mods = System.IO.Path.Combine(gameDir, "mods");
        if (!Directory.Exists(mods)) return false;
        var jars = Directory.EnumerateFiles(mods, "*.jar", SearchOption.TopDirectoryOnly).Count();
        var kube = Directory.Exists(System.IO.Path.Combine(gameDir, "kubejs"));
        return jars >= LauncherConstants.PackReadyMinJars && kube;
    }
}
