using System.Diagnostics;
using System.IO.Compression;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace AquaTechLauncher.Core;

public sealed class BootstrapManifest
{
    [JsonPropertyName("version")]
    public string Version { get; set; } = "";

    [JsonPropertyName("launcher_zip")]
    public string LauncherZip { get; set; } = "";

    [JsonPropertyName("launcher_exe")]
    public string LauncherExe { get; set; } = "AquaTechLauncher.exe";

    [JsonPropertyName("launcher_zip_md5")]
    public string? LauncherZipMd5 { get; set; }

    [JsonPropertyName("launcher_zip_size")]
    public long? LauncherZipSize { get; set; }
}

/// <summary>
/// In-app self-update: if bootstrap.json reports a newer client, download zip into
/// %LOCALAPPDATA%\AquaTech and swap the app folder after exit (same layout as Go bootstrap).
/// </summary>
public static class LauncherSelfUpdate
{
    public static async Task<(bool Updated, string Message)> CheckAndApplyAsync(
        Action<string>? log = null,
        Action<double>? progress = null,
        CancellationToken ct = default)
    {
        BootstrapManifest? man;
        try
        {
            man = await FetchBestManifestAsync(ct);
        }
        catch (Exception ex)
        {
            return (false, $"Не удалось проверить обновление: {ex.Message}");
        }

        if (man == null || string.IsNullOrWhiteSpace(man.Version) || string.IsNullOrWhiteSpace(man.LauncherZip))
            return (false, "Манифест лаунчера пустой");

        if (!VersionNewer(man.Version, LauncherConstants.Version))
            return (false, $"Актуально (v{LauncherConstants.Version})");

        var root = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "AquaTech");
        var appDir = Path.Combine(root, "app");
        var exeDir = Path.GetDirectoryName(Environment.ProcessPath) ?? AppContext.BaseDirectory;
        var installed = PathsEqual(exeDir, appDir)
                        || PathsEqual(exeDir, Path.Combine(appDir, "AquaTechLauncher"));
        if (!installed)
        {
            log?.Invoke($"Доступна v{man.Version}, но это не установленная копия — пропускаем автозамену");
            return (false, $"Доступна v{man.Version} (запусти через AquaTech.exe)");
        }

        Directory.CreateDirectory(root);
        var zipPath = Path.Combine(root, "AquaTechLauncher.zip");
        log?.Invoke($"Скачиваем лаунчер v{man.Version}…");
        progress?.Invoke(10);
        await HttpDownload.DownloadAsync(man.LauncherZip, zipPath, ct);
        progress?.Invoke(55);

        if (!VerifyZip(zipPath, man, out var verifyErr))
        {
            try { File.Delete(zipPath); } catch { /* ignore */ }
            return (false, verifyErr);
        }
        progress?.Invoke(70);

        var stage = Path.Combine(root, "app_new");
        if (Directory.Exists(stage)) Directory.Delete(stage, true);
        Directory.CreateDirectory(stage);
        ZipFile.ExtractToDirectory(zipPath, stage, overwriteFiles: true);

        var src = stage;
        var entries = Directory.GetDirectories(stage);
        if (entries.Length == 1 && Directory.GetFiles(stage).Length == 0)
            src = entries[0];

        var exeName = string.IsNullOrWhiteSpace(man.LauncherExe) ? "AquaTechLauncher.exe" : man.LauncherExe;
        var stagedExe = Path.Combine(src, exeName);
        if (!File.Exists(stagedExe))
        {
            stagedExe = Directory.EnumerateFiles(src, exeName, SearchOption.AllDirectories).FirstOrDefault()
                        ?? throw new FileNotFoundException("В обновлении нет AquaTechLauncher.exe");
            src = Path.GetDirectoryName(stagedExe)!;
        }

        File.WriteAllText(Path.Combine(root, "version.txt"), man.Version.Trim());
        progress?.Invoke(90);

        var bat = Path.Combine(root, "apply_update.cmd");
        var pid = Environment.ProcessId;
        var appOld = Path.Combine(root, "app_old");
        File.WriteAllText(bat, $"""
            @echo off
            :wait
            tasklist /FI "PID eq {pid}" 2>NUL | find "{pid}" >NUL
            if not errorlevel 1 (
              timeout /t 1 /nobreak >NUL
              goto wait
            )
            timeout /t 1 /nobreak >NUL

            if exist "{appOld}" rmdir /s /q "{appOld}" 2>NUL

            set RETRY_COUNT=0
            :try_rename
            if not exist "{appDir}" goto do_copy
            ren "{appDir}" "app_old" 2>NUL
            if not errorlevel 1 goto do_copy
            set /a RETRY_COUNT+=1
            if %RETRY_COUNT% geq 6 goto rollback
            timeout /t 1 /nobreak >NUL
            goto try_rename

            :do_copy
            mkdir "{appDir}" 2>NUL
            set COPY_RETRIES=0
            :try_copy
            xcopy /e /y /q "{src}\*" "{appDir}\" >NUL 2>NUL
            if not errorlevel 1 goto success
            set /a COPY_RETRIES+=1
            if %COPY_RETRIES% geq 6 goto rollback
            timeout /t 1 /nobreak >NUL
            goto try_copy

            :rollback
            if exist "{appOld}" (
              rmdir /s /q "{appDir}" 2>NUL
              ren "{appOld}" "app" 2>NUL
            )
            exit /b 1

            :success
            if exist "{appOld}" rmdir /s /q "{appOld}" 2>NUL
            rmdir /s /q "{stage}" 2>NUL
            start "" "{Path.Combine(appDir, exeName)}"
            del "%~f0" 2>NUL
            """);

        log?.Invoke($"Обновление v{man.Version} готово — перезапуск…");
        progress?.Invoke(100);
        Process.Start(new ProcessStartInfo
        {
            FileName = bat,
            UseShellExecute = true,
            CreateNoWindow = true,
            WindowStyle = ProcessWindowStyle.Hidden,
        });
        return (true, man.Version);
    }

    public static bool VerifyZip(string zipPath, BootstrapManifest man, out string error)
    {
        error = "";
        if (!File.Exists(zipPath))
        {
            error = "Zip обновления не скачался";
            return false;
        }
        var len = new FileInfo(zipPath).Length;
        if (man.LauncherZipSize is > 0 && len != man.LauncherZipSize.Value)
        {
            error = $"Размер zip не совпал ({len} ≠ {man.LauncherZipSize})";
            return false;
        }
        if (!string.IsNullOrWhiteSpace(man.LauncherZipMd5))
        {
            var got = HttpDownload.Md5File(zipPath);
            if (!got.Equals(man.LauncherZipMd5.Trim(), StringComparison.OrdinalIgnoreCase))
            {
                error = $"MD5 zip не совпал ({got})";
                return false;
            }
        }
        return true;
    }

    public static async Task<BootstrapManifest?> FetchBestManifestAsync(CancellationToken ct = default)
    {
        var stamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
        var tasks = LauncherConstants.BootstrapManifestUrls.Select(async url =>
        {
            try
            {
                var bust = url.Contains('?', StringComparison.Ordinal) ? $"{url}&t={stamp}" : $"{url}?t={stamp}";
                var json = await HttpDownload.GetStringAsync(bust, ct);
                var man = JsonSerializer.Deserialize<BootstrapManifest>(json);
                return man;
            }
            catch
            {
                return null;
            }
        }).ToList();

        var results = await Task.WhenAll(tasks);
        BootstrapManifest? best = null;
        foreach (var man in results)
        {
            if (man == null || string.IsNullOrWhiteSpace(man.Version))
                continue;
            if (best == null || VersionNewer(man.Version, best.Version))
                best = man;
        }
        return best;
    }

    public static bool VersionsEqual(string a, string b) =>
        string.Equals(a.Trim(), b.Trim(), StringComparison.OrdinalIgnoreCase);

    public static bool VersionNewer(string candidate, string baseline)
    {
        if (!TryVersionKey(candidate, out var a))
            return false;
        if (!TryVersionKey(baseline, out var b))
            return true;
        if (a.Major != b.Major) return a.Major > b.Major;
        if (a.Minor != b.Minor) return a.Minor > b.Minor;
        return a.Patch > b.Patch;
    }

    private static bool TryVersionKey(string raw, out (int Major, int Minor, int Patch) key)
    {
        key = default;
        var v = raw.Trim();
        var parts = v.Split('.');
        if (parts.Length < 2)
            return false;
        var nums = new int[3];
        for (var i = 0; i < 3 && i < parts.Length; i++)
        {
            var n = 0;
            foreach (var ch in parts[i])
            {
                if (ch < '0' || ch > '9')
                    break;
                n = n * 10 + (ch - '0');
            }
            nums[i] = n;
        }
        key = (nums[0], nums[1], nums[2]);
        return true;
    }

    private static bool PathsEqual(string a, string b)
    {
        try
        {
            return string.Equals(
                Path.GetFullPath(a).TrimEnd('\\', '/'),
                Path.GetFullPath(b).TrimEnd('\\', '/'),
                StringComparison.OrdinalIgnoreCase);
        }
        catch
        {
            return false;
        }
    }
}
