using System.Diagnostics;
using System.IO.Compression;
using System.Text.RegularExpressions;

namespace AquaTechLauncher.Core;

public sealed class JavaLocator
{
    private string? _cache;

    public async Task<string> EnsureJava17Async(string gameDir, Action<string>? log = null, Action<double>? progress = null, CancellationToken ct = default)
    {
        var found = FindJava17(gameDir);
        if (found != null) return PreferJavaw(found);

        log?.Invoke("Java 17 не найдена — скачиваем Adoptium JRE…");
        progress?.Invoke(8);
        var javaDir = LauncherConstants.JavaDir(gameDir);
        Directory.CreateDirectory(javaDir);
        var zipPath = Path.Combine(javaDir, "jre17.zip");
        await HttpDownload.DownloadAsync(LauncherConstants.JavaUrl, zipPath, ct);
        progress?.Invoke(14);
        ZipFile.ExtractToDirectory(zipPath, javaDir, overwriteFiles: true);
        try { File.Delete(zipPath); } catch { /* ignore */ }

        found = FindJava17(gameDir)
                ?? throw new FileNotFoundError("Не удалось установить Java 17");
        log?.Invoke("Java 17 готова");
        return PreferJavaw(found);
    }

    public string? FindJava17(string gameDir)
    {
        if (_cache != null && (_cache == "java" || File.Exists(_cache)))
            return _cache;

        var javaDir = LauncherConstants.JavaDir(gameDir);
        if (Directory.Exists(javaDir))
        {
            var direct = Path.Combine(javaDir, "bin", "java.exe");
            if (Ok(direct) is { } hit) { _cache = hit; return hit; }
            foreach (var p in Directory.EnumerateFiles(javaDir, "java.exe", SearchOption.AllDirectories))
            {
                if (Path.GetFileName(Path.GetDirectoryName(p))?.Equals("bin", StringComparison.OrdinalIgnoreCase) == true
                    && Ok(p) is { } h)
                {
                    _cache = h;
                    return h;
                }
            }
        }

        var candidates = new List<string>
        {
            @"C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\java.exe",
            @"C:\Program Files\Eclipse Adoptium\jre-17.0.10.7-hotspot\bin\java.exe",
            @"C:\Program Files\Java\jdk-17\bin\java.exe",
            @"C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot\bin\java.exe",
            @"C:\Program Files\Zulu\zulu-17\bin\java.exe",
        };
        foreach (var root in new[]
                 {
                     @"C:\Program Files\Eclipse Adoptium",
                     @"C:\Program Files\Java",
                     @"C:\Program Files\Microsoft",
                     @"C:\Program Files\Zulu",
                     @"C:\Program Files\Amazon Corretto",
                 })
        {
            if (!Directory.Exists(root)) continue;
            try
            {
                candidates.AddRange(Directory.EnumerateFiles(root, "java.exe", SearchOption.AllDirectories)
                    .Where(p => Path.GetFileName(Path.GetDirectoryName(p)) == "bin"));
            }
            catch { /* access */ }
        }

        foreach (var c in candidates.Distinct(StringComparer.OrdinalIgnoreCase))
        {
            if (Ok(c) is { } h) { _cache = h; return h; }
        }
        return null;
    }

    private static string PreferJavaw(string java)
    {
        if (java.EndsWith("java.exe", StringComparison.OrdinalIgnoreCase)
            && !java.EndsWith("javaw.exe", StringComparison.OrdinalIgnoreCase))
        {
            var javaw = Path.Combine(Path.GetDirectoryName(java)!, "javaw.exe");
            if (File.Exists(javaw)) return javaw;
        }
        return java;
    }

    private static string? Ok(string path)
    {
        if (string.IsNullOrWhiteSpace(path) || !File.Exists(path)) return null;
        return MajorVersion(path) == 17 ? path : null;
    }

    private static int? MajorVersion(string javaPath)
    {
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = javaPath,
                Arguments = "-version",
                RedirectStandardError = true,
                RedirectStandardOutput = true,
                UseShellExecute = false,
                CreateNoWindow = true,
            };
            using var p = Process.Start(psi)!;
            var err = p.StandardError.ReadToEnd() + p.StandardOutput.ReadToEnd();
            p.WaitForExit(8000);
            var m = Regex.Match(err, @"version ""(\d+)");
            if (m.Success && int.TryParse(m.Groups[1].Value, out var maj)) return maj;
            m = Regex.Match(err, @"version ""1\.(\d+)");
            if (m.Success && int.TryParse(m.Groups[1].Value, out maj)) return maj;
        }
        catch { /* ignore */ }
        return null;
    }
}

file sealed class FileNotFoundError(string message) : FileNotFoundException(message);
