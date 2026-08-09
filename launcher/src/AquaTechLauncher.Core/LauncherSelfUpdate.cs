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
            var json = await HttpDownload.GetStringAsync(LauncherConstants.BootstrapManifestUrl, ct);
            man = JsonSerializer.Deserialize<BootstrapManifest>(json);
        }
        catch (Exception ex)
        {
            return (false, $"Не удалось проверить обновление: {ex.Message}");
        }

        if (man == null || string.IsNullOrWhiteSpace(man.Version) || string.IsNullOrWhiteSpace(man.LauncherZip))
            return (false, "Манифест лаунчера пустой");

        if (VersionsEqual(man.Version, LauncherConstants.Version))
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
            if exist "{appOld}" rmdir /s /q "{appOld}"
            if exist "{appDir}" ren "{appDir}" "app_old"
            mkdir "{appDir}"
            xcopy /e /y /q "{src}\*" "{appDir}\"
            if errorlevel 1 (
              if exist "{appOld}" (
                rmdir /s /q "{appDir}"
                ren "{appOld}" "app"
              )
              exit /b 1
            )
            if exist "{appOld}" rmdir /s /q "{appOld}"
            rmdir /s /q "{stage}"
            start "" "{Path.Combine(appDir, exeName)}"
            del "%~f0"
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

    public static bool VersionsEqual(string a, string b) =>
        string.Equals(a.Trim(), b.Trim(), StringComparison.OrdinalIgnoreCase);

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
