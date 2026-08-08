using System.Diagnostics;
using System.IO.Compression;
using System.Text.Json;
using System.Text.Json.Nodes;

namespace AquaTechLauncher.Core;

public sealed class ForgeInstaller
{
    public string? FindForgeVersionId(string gameDir)
    {
        var vDir = Path.Combine(gameDir, "versions");
        if (!Directory.Exists(vDir)) return null;
        foreach (var d in Directory.EnumerateDirectories(vDir).OrderByDescending(x => x))
        {
            var name = Path.GetFileName(d);
            if (name.Contains("forge", StringComparison.OrdinalIgnoreCase)
                && name.Contains("1.20.1", StringComparison.OrdinalIgnoreCase))
            {
                var j = Path.Combine(d, name + ".json");
                if (File.Exists(j)) return name;
            }
        }
        return null;
    }

    public async Task EnsureReadyAsync(string gameDir, string java, Action<string>? log = null, CancellationToken ct = default)
    {
        EnsureLauncherProfiles(gameDir);
        if (FindForgeVersionId(gameDir) != null
            && File.Exists(Path.Combine(gameDir, "libraries", "net", "minecraftforge", "forge",
                LauncherConstants.ForgeCoord, $"forge-{LauncherConstants.ForgeCoord}-client.jar")))
        {
            await EnsureLangProvidersAsync(gameDir, log, ct);
            return;
        }

        if (await TryFastInstallAsync(gameDir, log, ct))
            return;

        await OfficialInstallAsync(gameDir, java, log, ct);
        if (FindForgeVersionId(gameDir) == null)
            throw new InvalidOperationException("Forge не установился");
        await EnsureLangProvidersAsync(gameDir, log, ct);
    }

    private async Task<bool> TryFastInstallAsync(string gameDir, Action<string>? log, CancellationToken ct)
    {
        var zip = FindBundled("forge-runtime-1.20.1-47.4.0.zip");
        if (zip == null) return false;
        log?.Invoke($"Быстрая установка Forge (~{new FileInfo(zip).Length / 1024.0 / 1024.0:0.0} МБ)…");
        ZipFile.ExtractToDirectory(zip, gameDir, overwriteFiles: true);

        var verJson = Path.Combine(gameDir, "versions", LauncherConstants.ForgeVersionId,
            LauncherConstants.ForgeVersionId + ".json");
        var clientJar = Path.Combine(gameDir, "libraries", "net", "minecraftforge", "forge",
            LauncherConstants.ForgeCoord, $"forge-{LauncherConstants.ForgeCoord}-client.jar");
        var uniJar = Path.Combine(gameDir, "libraries", "net", "minecraftforge", "forge",
            LauncherConstants.ForgeCoord, $"forge-{LauncherConstants.ForgeCoord}-universal.jar");
        if (!File.Exists(verJson) || !File.Exists(clientJar) || !File.Exists(uniJar))
        {
            log?.Invoke("Forge runtime zip неполный");
            return false;
        }

        await EnsureVanillaAsync(gameDir, log, ct);
        await EnsureMinecraftSrgAsync(gameDir, log, ct);
        PatchLangProvidersIntoVersionJson(verJson);
        await EnsureLangProvidersAsync(gameDir, log, ct);
        await PrefetchLibrariesAsync(gameDir, verJson, log, ct);

        var vanilla = Path.Combine(gameDir, "versions", LauncherConstants.McVersion, LauncherConstants.McVersion + ".jar");
        var versionJar = Path.Combine(gameDir, "versions", LauncherConstants.ForgeVersionId,
            LauncherConstants.ForgeVersionId + ".jar");
        if (File.Exists(vanilla) && (!File.Exists(versionJar) || new FileInfo(versionJar).Length < 1_000_000))
            File.Copy(vanilla, versionJar, true);

        return FindForgeVersionId(gameDir) != null;
    }

    private async Task OfficialInstallAsync(string gameDir, string java, Action<string>? log, CancellationToken ct)
    {
        var installer = FindBundled("forge-1.20.1-47.4.0-installer.jar")
            ?? Path.Combine(gameDir, "forge-1.20.1-47.4.0-installer.jar");
        if (!File.Exists(installer))
        {
            var url = $"https://maven.minecraftforge.net/net/minecraftforge/forge/{LauncherConstants.ForgeCoord}/forge-{LauncherConstants.ForgeCoord}-installer.jar";
            log?.Invoke("Скачиваем Forge installer…");
            await HttpDownload.DownloadMirroredAsync(url, installer, ct);
        }

        log?.Invoke("Официальный Forge installer (медленно)…");
        var javaExe = java.EndsWith("javaw.exe", StringComparison.OrdinalIgnoreCase)
            ? Path.Combine(Path.GetDirectoryName(java)!, "java.exe")
            : java;
        var psi = new ProcessStartInfo
        {
            FileName = File.Exists(javaExe) ? javaExe : java,
            Arguments = $"-jar \"{installer}\" --installClient \"{gameDir}\"",
            WorkingDirectory = gameDir,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
        };
        using var p = Process.Start(psi)!;
        _ = p.StandardOutput.ReadToEndAsync(ct);
        _ = p.StandardError.ReadToEndAsync(ct);
        await p.WaitForExitAsync(ct);
        if (p.ExitCode != 0)
            log?.Invoke($"Forge installer exit {p.ExitCode}");
    }

    public static async Task EnsureVanillaAsync(string gameDir, Action<string>? log, CancellationToken ct)
    {
        var jsonPath = Path.Combine(gameDir, "versions", LauncherConstants.McVersion, LauncherConstants.McVersion + ".json");
        if (!File.Exists(jsonPath) || new FileInfo(jsonPath).Length < 1000)
        {
            Directory.CreateDirectory(Path.GetDirectoryName(jsonPath)!);
            var manifest = await HttpDownload.GetStringAsync(
                "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json", ct);
            using var doc = JsonDocument.Parse(manifest);
            var entry = doc.RootElement.GetProperty("versions").EnumerateArray()
                .First(v => v.GetProperty("id").GetString() == LauncherConstants.McVersion);
            var verUrl = entry.GetProperty("url").GetString()!;
            var verJson = await HttpDownload.GetStringAsync(verUrl, ct);
            await File.WriteAllTextAsync(jsonPath, verJson, ct);
        }

        var jarPath = Path.Combine(gameDir, "versions", LauncherConstants.McVersion, LauncherConstants.McVersion + ".jar");
        if (File.Exists(jarPath) && new FileInfo(jarPath).Length > 10_000_000) return;

        using var vdoc = JsonDocument.Parse(await File.ReadAllTextAsync(jsonPath, ct));
        var clientUrl = vdoc.RootElement.GetProperty("downloads").GetProperty("client").GetProperty("url").GetString()!;
        log?.Invoke($"Скачиваем Minecraft {LauncherConstants.McVersion}…");
        Exception? last = null;
        foreach (var u in new[]
                 {
                     $"https://bmclapi2.bangbang93.com/version/{LauncherConstants.McVersion}/client",
                     clientUrl,
                 })
        {
            try
            {
                await HttpDownload.DownloadAsync(u, jarPath, ct);
                if (File.Exists(jarPath) && new FileInfo(jarPath).Length > 10_000_000)
                {
                    log?.Invoke($"Minecraft {LauncherConstants.McVersion}.jar готов");
                    return;
                }
            }
            catch (Exception ex)
            {
                last = ex;
                try { File.Delete(jarPath); } catch { /* ignore */ }
            }
        }
        throw last ?? new IOException("Не удалось скачать Minecraft");
    }

    public static async Task EnsureLangProvidersAsync(string gameDir, Action<string>? log, CancellationToken ct)
    {
        var libs = Path.Combine(gameDir, "libraries");
        var jobs = new List<(string Url, string Path)>();
        foreach (var name in LauncherConstants.ForgeLangProviders)
        {
            var rel = $"net/minecraftforge/{name}/{LauncherConstants.ForgeCoord}/{name}-{LauncherConstants.ForgeCoord}.jar";
            var path = Path.Combine(libs, rel.Replace('/', Path.DirectorySeparatorChar));
            if (File.Exists(path) && new FileInfo(path).Length > 100) continue;
            jobs.Add(($"https://maven.minecraftforge.net/{rel}", path));
        }
        if (jobs.Count == 0) return;
        log?.Invoke($"FML language providers: {jobs.Count}…");
        foreach (var (url, path) in jobs)
        {
            Directory.CreateDirectory(Path.GetDirectoryName(path)!);
            await HttpDownload.DownloadMirroredAsync(url, path, ct);
        }
    }

    public static void PatchLangProvidersIntoVersionJson(string verJsonPath)
    {
        if (!File.Exists(verJsonPath)) return;
        var node = JsonNode.Parse(File.ReadAllText(verJsonPath)) as JsonObject;
        if (node == null) return;
        var libs = node["libraries"] as JsonArray ?? new JsonArray();
        node["libraries"] = libs;
        var have = libs.Select(l => l?["name"]?.GetValue<string>()).Where(x => x != null).ToHashSet();
        var changed = false;
        foreach (var name in LauncherConstants.ForgeLangProviders)
        {
            var full = $"net.minecraftforge:{name}:{LauncherConstants.ForgeCoord}";
            if (have.Contains(full)) continue;
            var rel = $"net/minecraftforge/{name}/{LauncherConstants.ForgeCoord}/{name}-{LauncherConstants.ForgeCoord}.jar";
            libs.Add(new JsonObject
            {
                ["name"] = full,
                ["downloads"] = new JsonObject
                {
                    ["artifact"] = new JsonObject
                    {
                        ["path"] = rel,
                        ["url"] = $"https://maven.minecraftforge.net/{rel}",
                    }
                }
            });
            changed = true;
        }
        if (changed) File.WriteAllText(verJsonPath, node.ToJsonString(new JsonSerializerOptions { WriteIndented = true }));
    }

    private static Task EnsureMinecraftSrgAsync(string gameDir, Action<string>? log, CancellationToken ct)
    {
        var dir = Path.Combine(gameDir, "libraries", "net", "minecraft", "client",
            $"{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}");
        Directory.CreateDirectory(dir);
        var srg = Path.Combine(dir, $"client-{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}-srg.jar");
        var extra = Path.Combine(dir, $"client-{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}-extra.jar");
        if (File.Exists(srg) && new FileInfo(srg).Length > 1_000_000
            && File.Exists(extra) && new FileInfo(extra).Length > 100_000)
            return Task.CompletedTask;

        var rtZip = FindBundled("forge-runtime-1.20.1-47.4.0.zip")
            ?? throw new FileNotFoundException("Нет client-*-srg.jar — переустанови лаунчер (runtime zip).");
        log?.Invoke("Достаём Minecraft SRG/extra из Forge runtime…");
        var need = new Dictionary<string, string>
        {
            [$"libraries/net/minecraft/client/{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}/client-{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}-srg.jar"] = srg,
            [$"libraries/net/minecraft/client/{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}/client-{LauncherConstants.McVersion}-{LauncherConstants.McpVersion}-extra.jar"] = extra,
        };
        using var zf = ZipFile.OpenRead(rtZip);
        foreach (var (arc, dest) in need)
        {
            if (File.Exists(dest) && new FileInfo(dest).Length > 100_000) continue;
            var entry = zf.GetEntry(arc) ?? zf.GetEntry(arc.Replace('/', '\\'))
                ?? throw new FileNotFoundException($"В runtime zip нет {arc}");
            Directory.CreateDirectory(Path.GetDirectoryName(dest)!);
            entry.ExtractToFile(dest, overwrite: true);
        }
        if (!File.Exists(srg) || new FileInfo(srg).Length < 1_000_000)
            throw new FileNotFoundException("client-*-srg.jar не установился");
        return Task.CompletedTask;
    }

    private static async Task PrefetchLibrariesAsync(string gameDir, string verJsonPath, Action<string>? log, CancellationToken ct)
    {
        using var doc = JsonDocument.Parse(await File.ReadAllTextAsync(verJsonPath, ct));
        if (!doc.RootElement.TryGetProperty("libraries", out var libs)) return;
        var jobs = new List<(string Url, string Path)>();
        foreach (var lib in libs.EnumerateArray())
        {
            if (!lib.TryGetProperty("downloads", out var dl)) continue;
            if (!dl.TryGetProperty("artifact", out var art)) continue;
            if (!art.TryGetProperty("path", out var pathEl) || !art.TryGetProperty("url", out var urlEl)) continue;
            var rel = pathEl.GetString()!;
            var url = urlEl.GetString()!;
            var path = Path.Combine(gameDir, "libraries", rel.Replace('/', Path.DirectorySeparatorChar));
            if (File.Exists(path) && new FileInfo(path).Length > 100) continue;
            jobs.Add((url, path));
        }
        if (jobs.Count == 0) return;
        log?.Invoke($"Forge libs: {jobs.Count}…");
        using var gate = new SemaphoreSlim(16);
        await Task.WhenAll(jobs.Select(async j =>
        {
            await gate.WaitAsync(ct);
            try
            {
                Directory.CreateDirectory(Path.GetDirectoryName(j.Path)!);
                await HttpDownload.DownloadMirroredAsync(j.Url, j.Path, ct);
            }
            catch { /* continue */ }
            finally { gate.Release(); }
        }));
    }

    private static void EnsureLauncherProfiles(string gameDir)
    {
        Directory.CreateDirectory(gameDir);
        var profiles = Path.Combine(gameDir, "launcher_profiles.json");
        if (!File.Exists(profiles))
        {
            File.WriteAllText(profiles, """
                {"profiles":{"AquaTech":{"name":"AquaTech","type":"custom","lastVersionId":"1.20.1"}},"version":3}
                """);
        }
        var ms = Path.Combine(gameDir, "launcher_profiles_microsoft_store.json");
        if (!File.Exists(ms))
            File.WriteAllText(ms, """{"profiles":{},"settings":{},"version":3}""");
    }

    public static string? FindBundled(string fileName)
    {
        var bases = new[]
        {
            AppContext.BaseDirectory,
            Path.GetDirectoryName(Environment.ProcessPath) ?? "",
            Path.Combine(AppContext.BaseDirectory, "_internal"),
        };
        foreach (var b in bases)
        {
            if (string.IsNullOrEmpty(b)) continue;
            var p = Path.Combine(b, fileName);
            if (File.Exists(p)) return p;
        }
        // Dev fallback
        var repo = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..", "..", "..", "..", "..", "tools", fileName));
        if (File.Exists(repo)) return repo;
        repo = Path.GetFullPath(Path.Combine(Environment.CurrentDirectory, "tools", fileName));
        if (File.Exists(repo)) return repo;
        return null;
    }
}
