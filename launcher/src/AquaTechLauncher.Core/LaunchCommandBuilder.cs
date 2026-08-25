using System.Diagnostics;
using System.IO.Compression;
using System.Text.Json;
using System.Text.Json.Nodes;

namespace AquaTechLauncher.Core;

public sealed class LaunchCommandBuilder
{
    public async Task<List<string>> BuildAsync(
        string gameDir,
        string username,
        int ramMb,
        string java,
        string autoJoin,
        string? sessionToken = null,
        Action<string>? log = null,
        CancellationToken ct = default)
    {
        EnsureRussianOptions(gameDir);
        ServerListHelper.EnsureServerEntry(gameDir, autoJoin, "AquaTech");
        var forge = new ForgeInstaller();
        var verId = forge.FindForgeVersionId(gameDir)
            ?? throw new FileNotFoundException("Forge version JSON not found");
        await ForgeInstaller.EnsureVanillaAsync(gameDir, log, ct);

        var merged = LoadMergedVersion(gameDir, verId);
        var nativesDir = Path.Combine(gameDir, "versions", verId, "natives");
        var assetsDir = Path.Combine(gameDir, "assets");
        var libsDir = Path.Combine(gameDir, "libraries");

        var assetTask = EnsureAssetsAsync(gameDir, merged, log, ct);
        var libsTask = EnsureLibrariesAndNativesAsync(gameDir, merged, nativesDir, log, ct);
        await Task.WhenAll(assetTask, libsTask);
        var assetIndex = await assetTask;
        var cpParts = await libsTask;

        var inherits = merged["inheritsFrom"]?.GetValue<string>() ?? LauncherConstants.McVersion;
        var vanillaJar = Path.Combine(gameDir, "versions", inherits, inherits + ".jar");
        var versionJar = Path.Combine(gameDir, "versions", verId, verId + ".jar");
        if (!File.Exists(vanillaJar) || new FileInfo(vanillaJar).Length < 10_000_000)
            throw new FileNotFoundException($"Нет Minecraft {inherits}.jar");
        if (!File.Exists(versionJar) || new FileInfo(versionJar).Length < 1_000_000)
            File.Copy(vanillaJar, versionJar, true);
        cpParts.Add(versionJar);

        var forgeLib = Path.Combine(gameDir, "libraries", "net", "minecraftforge", "forge", LauncherConstants.ForgeCoord);
        foreach (var name in new[]
                 {
                     $"forge-{LauncherConstants.ForgeCoord}-client.jar",
                     $"forge-{LauncherConstants.ForgeCoord}-universal.jar",
                 })
        {
            var fjar = Path.Combine(forgeLib, name);
            if (File.Exists(fjar)) cpParts.Add(fjar);
        }

        var verJsonPath = Path.Combine(gameDir, "versions", verId, verId + ".json");
        ForgeInstaller.PatchLangProvidersIntoVersionJson(verJsonPath);
        await ForgeInstaller.EnsureLangProvidersAsync(gameDir, log, ct);
        foreach (var name in LauncherConstants.ForgeLangProviders)
        {
            var rel = $"net/minecraftforge/{name}/{LauncherConstants.ForgeCoord}/{name}-{LauncherConstants.ForgeCoord}.jar";
            var p = Path.Combine(libsDir, rel.Replace('/', Path.DirectorySeparatorChar));
            if (File.Exists(p)) cpParts.Add(p);
        }

        // dedupe
        var seen = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        var cpUnique = new List<string>();
        foreach (var p in cpParts)
        {
            var key = p.ToLowerInvariant();
            if (!seen.Add(key)) continue;
            if (p.Replace('\\', '/').EndsWith($"/versions/{inherits}/{inherits}.jar", StringComparison.OrdinalIgnoreCase))
                continue;
            if (key.Contains("natives-windows") || key.Contains("natives-linux") || key.Contains("natives-macos"))
                continue;
            cpUnique.Add(p);
        }

        var cp = string.Join(Path.PathSeparator, cpUnique);
        var mainClass = merged["mainClass"]?.GetValue<string>() ?? "cpw.mods.bootstraplauncher.BootstrapLauncher";

        string Expand(string arg) => arg
            .Replace("${natives_directory}", nativesDir)
            .Replace("${launcher_name}", "AquaTechLauncher")
            .Replace("${launcher_version}", LauncherConstants.Version)
            .Replace("${classpath}", cp)
            .Replace("${library_directory}", libsDir)
            .Replace("${classpath_separator}", Path.PathSeparator.ToString())
            .Replace("${auth_player_name}", username)
            .Replace("${version_name}", verId)
            .Replace("${game_directory}", gameDir)
            .Replace("${assets_root}", assetsDir)
            .Replace("${assets_index_name}", assetIndex)
            .Replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000")
            .Replace("${auth_access_token}", "0")
            .Replace("${clientid}", "0")
            .Replace("${auth_xuid}", "0")
            .Replace("${user_type}", "legacy")
            .Replace("${version_type}", "release")
            .Replace("${resolution_width}", "1280")
            .Replace("${resolution_height}", "720");

        var jvmArgs = FlattenArgs(merged["arguments"]?["jvm"] as JsonArray, Expand);
        var gameArgs = FlattenArgs(merged["arguments"]?["game"] as JsonArray, Expand);

        var needed = new Dictionary<string, string>
        {
            ["--username"] = username,
            ["--version"] = verId,
            ["--gameDir"] = gameDir,
            ["--assetsDir"] = assetsDir,
            ["--assetIndex"] = assetIndex,
            ["--uuid"] = "00000000-0000-0000-0000-000000000000",
            ["--accessToken"] = "0",
            ["--userType"] = "legacy",
            ["--versionType"] = "release",
        };
        foreach (var (flag, val) in needed)
            if (!gameArgs.Contains(flag)) { gameArgs.Add(flag); gameArgs.Add(val); }

        if (!Directory.EnumerateFiles(nativesDir, "*.dll").Any())
        {
            log?.Invoke("natives пустые — перекачаем LWJGL…");
            await EnsureLibrariesAndNativesAsync(gameDir, merged, nativesDir, log, ct);
            if (!Directory.EnumerateFiles(nativesDir, "*.dll").Any())
                throw new FileNotFoundException($"Нет LWJGL natives в {nativesDir}");
        }

        var forgeClient = Path.Combine(forgeLib, $"forge-{LauncherConstants.ForgeCoord}-client.jar");
        var forgeUni = Path.Combine(forgeLib, $"forge-{LauncherConstants.ForgeCoord}-universal.jar");
        if (!File.Exists(forgeClient) || !File.Exists(forgeUni))
            throw new FileNotFoundException("Нет forge-client/universal.jar");

        var javaExec = java;
        var cmd = new List<string>
        {
            javaExec,
            $"-Xmx{ramMb}M",
            $"-Xms{Math.Min(ramMb, 2048)}M",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:G1NewSizePercent=20",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapRegionSize=32M",
            "-Dlog4j2.formatMsgNoLookups=true",
            "-Djava.net.preferIPv4Stack=true",
            "-Dforge.logging.console.level=info",
            $"-XX:ErrorFile={Path.Combine(gameDir, "logs", "hs_err_pid%p.log")}",
        };
        var autoJoinArgument = BuildAutoJoinArgument(autoJoin);
        if (autoJoinArgument != null)
        {
            cmd.Add(autoJoinArgument);
            log?.Invoke($"авто-вход на {autoJoin} после загрузки меню");
        }
        if (!string.IsNullOrWhiteSpace(sessionToken))
        {
            cmd.Add($"-Daquatech.session_token={sessionToken.Trim()}");
            log?.Invoke("токен сессии передан в Minecraft");
        }
        cmd.AddRange(jvmArgs);
        cmd.Add(mainClass);
        cmd.AddRange(gameArgs);
        return cmd;
    }

    public static string? BuildAutoJoinArgument(string autoJoin)
    {
        return string.IsNullOrWhiteSpace(autoJoin)
            ? null
            : $"-Daquatech.autoJoin={autoJoin.Trim()}";
    }

    private static JsonObject LoadMergedVersion(string gameDir, string verId)
    {
        var path = Path.Combine(gameDir, "versions", verId, verId + ".json");
        var child = JsonNode.Parse(File.ReadAllText(path))!.AsObject();
        JsonObject parent = new();
        var inherits = child["inheritsFrom"]?.GetValue<string>();
        if (!string.IsNullOrEmpty(inherits))
        {
            var ppath = Path.Combine(gameDir, "versions", inherits, inherits + ".json");
            parent = JsonNode.Parse(File.ReadAllText(ppath))!.AsObject();
        }

        var merged = new JsonObject();
        foreach (var kv in parent)
            merged[kv.Key] = kv.Value?.DeepClone();
        foreach (var kv in child)
        {
            if (kv.Key is "arguments" or "libraries") continue;
            if (kv.Value != null) merged[kv.Key] = kv.Value.DeepClone();
        }

        var libs = new JsonArray();
        var seen = new HashSet<string>();
        foreach (var src in new[] { parent["libraries"] as JsonArray, child["libraries"] as JsonArray })
        {
            if (src == null) continue;
            foreach (var lib in src)
            {
                var name = lib?["name"]?.GetValue<string>();
                if (string.IsNullOrEmpty(name) || !seen.Add(name)) continue;
                libs.Add(lib!.DeepClone());
            }
        }
        merged["libraries"] = libs;

        var pargs = parent["arguments"] as JsonObject ?? new JsonObject();
        var cargs = child["arguments"] as JsonObject ?? new JsonObject();
        var jvm = new JsonArray();
        var game = new JsonArray();
        foreach (var a in (pargs["jvm"] as JsonArray) ?? []) jvm.Add(a!.DeepClone());
        foreach (var a in (cargs["jvm"] as JsonArray) ?? []) jvm.Add(a!.DeepClone());
        foreach (var a in (pargs["game"] as JsonArray) ?? []) game.Add(a!.DeepClone());
        foreach (var a in (cargs["game"] as JsonArray) ?? []) game.Add(a!.DeepClone());
        merged["arguments"] = new JsonObject { ["jvm"] = jvm, ["game"] = game };
        if (child["mainClass"] != null) merged["mainClass"] = child["mainClass"]!.DeepClone();
        if (merged["assetIndex"] == null && parent["assetIndex"] != null)
            merged["assetIndex"] = parent["assetIndex"]!.DeepClone();
        return merged;
    }

    private static List<string> FlattenArgs(JsonArray? items, Func<string, string> expand)
    {
        var outList = new List<string>();
        if (items == null) return outList;
        foreach (var arg in items)
        {
            if (arg is JsonValue jv && jv.TryGetValue<string>(out var s))
            {
                var expanded = expand(s);
                if (expanded.Contains("${")) continue;
                outList.Add(expanded);
            }
            else if (arg is JsonObject jo)
            {
                var rules = jo["rules"] as JsonArray;
                if (rules != null && rules.Any(r => r?["features"] != null)) continue;
                var allow = true;
                if (rules != null)
                {
                    foreach (var rule in rules)
                    {
                        var osName = rule?["os"]?["name"]?.GetValue<string>();
                        var action = rule?["action"]?.GetValue<string>();
                        if (osName != null && osName != "windows" && action == "allow") allow = false;
                        if (osName == "windows" && action == "disallow") allow = false;
                    }
                }
                if (!allow) continue;
                var val = jo["value"];
                if (val is JsonValue vv && vv.TryGetValue<string>(out var vs))
                {
                    var expanded = expand(vs);
                    if (!expanded.Contains("${")) outList.Add(expanded);
                }
                else if (val is JsonArray va)
                {
                    foreach (var v in va)
                    {
                        if (v is JsonValue x && x.TryGetValue<string>(out var xs))
                        {
                            var expanded = expand(xs);
                            if (!expanded.Contains("${")) outList.Add(expanded);
                        }
                    }
                }
            }
        }
        // strip dangling -cp
        var cleaned = new List<string>();
        for (var i = 0; i < outList.Count; i++)
        {
            if (outList[i] is "-cp" or "-classpath" or "-p")
            {
                if (i + 1 < outList.Count && !outList[i + 1].StartsWith('-'))
                {
                    cleaned.Add(outList[i]);
                    cleaned.Add(outList[i + 1]);
                    i++;
                }
                continue;
            }
            cleaned.Add(outList[i]);
        }
        return cleaned;
    }

    private static async Task<List<string>> EnsureLibrariesAndNativesAsync(
        string gameDir, JsonObject ver, string nativesDir, Action<string>? log, CancellationToken ct)
    {
        Directory.CreateDirectory(nativesDir);
        var libsDir = Path.Combine(gameDir, "libraries");
        var cp = new List<string>();
        var downloadJobs = new List<(string Url, string Path)>();
        var nativeExtract = new List<string>();

        foreach (var lib in (ver["libraries"] as JsonArray) ?? [])
        {
            if (lib is not JsonObject lo || !LibAllowed(lo)) continue;
            var downloads = lo["downloads"] as JsonObject;
            var name = lo["name"]?.GetValue<string>() ?? "";
            var parts = name.Split(':');
            var isNatives = parts.Length >= 4 && parts[3].StartsWith("natives", StringComparison.Ordinal);

            var art = downloads?["artifact"] as JsonObject;
            var rel = art?["path"]?.GetValue<string>() ?? ArtifactPathFromName(name);
            if (rel != null)
            {
                var path = Path.Combine(libsDir, rel.Replace('/', Path.DirectorySeparatorChar));
                var url = art?["url"]?.GetValue<string>();
                if (!File.Exists(path) && !string.IsNullOrEmpty(url))
                    downloadJobs.Add((url!, path));
                if (isNatives) nativeExtract.Add(path);
                else if (File.Exists(path) || !string.IsNullOrEmpty(url)) cp.Add(path);
            }

            var nativesMap = lo["natives"] as JsonObject;
            var classifier = nativesMap?["windows"]?.GetValue<string>()
                             ?? nativesMap?["windows-x86_64"]?.GetValue<string>();
            var classifiers = downloads?["classifiers"] as JsonObject;
            if (classifier != null && classifiers?[classifier] is JsonObject nart)
            {
                var nrel = nart["path"]?.GetValue<string>();
                if (nrel != null)
                {
                    var npath = Path.Combine(libsDir, nrel.Replace('/', Path.DirectorySeparatorChar));
                    var nurl = nart["url"]?.GetValue<string>();
                    if (!File.Exists(npath) && !string.IsNullOrEmpty(nurl))
                        downloadJobs.Add((nurl!, npath));
                    nativeExtract.Add(npath);
                }
            }
        }

        downloadJobs = downloadJobs
            .GroupBy(j => j.Path, StringComparer.OrdinalIgnoreCase)
            .Select(g => g.First())
            .ToList();

        if (downloadJobs.Count > 0)
        {
            log?.Invoke($"Библиотеки: {downloadJobs.Count}…");
            using var gate = new SemaphoreSlim(24);
            await Task.WhenAll(downloadJobs.Select(async j =>
            {
                await gate.WaitAsync(ct);
                try
                {
                    Directory.CreateDirectory(Path.GetDirectoryName(j.Path)!);
                    await HttpDownload.DownloadMirroredAsync(j.Url, j.Path, ct);
                }
                catch { try { File.Delete(j.Path); } catch { /* ignore */ } }
                finally { gate.Release(); }
            }));
        }

        cp = cp.Where(File.Exists).ToList();
        foreach (var npath in nativeExtract.Distinct(StringComparer.OrdinalIgnoreCase))
        {
            if (!File.Exists(npath)) continue;
            try
            {
                using var zf = ZipFile.OpenRead(npath);
                foreach (var e in zf.Entries)
                {
                    if (string.IsNullOrEmpty(e.Name) || e.FullName.StartsWith("META-INF")) continue;
                    var dest = Path.Combine(nativesDir, Path.GetFileName(e.Name));
                    if (!File.Exists(dest)) e.ExtractToFile(dest, overwrite: true);
                }
            }
            catch { /* ignore */ }
        }
        return cp;
    }

    private static async Task<string> EnsureAssetsAsync(string gameDir, JsonObject ver, Action<string>? log, CancellationToken ct)
    {
        var assetsDir = Path.Combine(gameDir, "assets");
        Directory.CreateDirectory(assetsDir);
        var index = ver["assetIndex"] as JsonObject;
        var indexId = index?["id"]?.GetValue<string>() ?? "5";
        var indexPath = Path.Combine(assetsDir, "indexes", indexId + ".json");
        var objectsDir = Path.Combine(assetsDir, "objects");
        var ready = Path.Combine(assetsDir, "indexes", indexId + ".aquatech_ready");

        if (File.Exists(ready) && Directory.Exists(objectsDir) && Directory.EnumerateFiles(objectsDir, "*", SearchOption.AllDirectories).Any())
        {
            log?.Invoke($"assets готовы ({indexId})");
            return indexId;
        }

        if (!File.Exists(indexPath))
        {
            Directory.CreateDirectory(Path.GetDirectoryName(indexPath)!);
            var urls = new List<string>();
            if (index?["url"]?.GetValue<string>() is { } iu) urls.Add(iu);
            urls.Add($"https://resources.download.minecraft.net/indexes/{indexId}.json");
            urls.Add($"https://bmclapi2.bangbang93.com/assets/indexes/{indexId}.json");
            foreach (var u in urls)
            {
                try
                {
                    await HttpDownload.DownloadAsync(u, indexPath, ct);
                    if (File.Exists(indexPath) && new FileInfo(indexPath).Length > 100_000) break;
                }
                catch { try { File.Delete(indexPath); } catch { /* ignore */ } }
            }
        }
        if (!File.Exists(indexPath))
        {
            log?.Invoke("Нет asset index — игра может стартовать без звуков");
            return indexId;
        }

        using var doc = JsonDocument.Parse(await File.ReadAllTextAsync(indexPath, ct));
        var objects = doc.RootElement.GetProperty("objects");
        Directory.CreateDirectory(objectsDir);
        for (var i = 0; i < 256; i++)
        {
            Directory.CreateDirectory(Path.Combine(objectsDir, i.ToString("x2")));
        }

        var missing = new List<(string Hash, string Dest, long Size)>();
        foreach (var prop in objects.EnumerateObject())
        {
            var h = prop.Value.GetProperty("hash").GetString()!;
            var size = prop.Value.TryGetProperty("size", out var sz) ? sz.GetInt64() : 0;
            var dest = Path.Combine(objectsDir, h[..2], h);
            if (!File.Exists(dest) || (size > 0 && new FileInfo(dest).Length != size))
                missing.Add((h, dest, size));
        }
        if (missing.Count == 0)
        {
            File.WriteAllText(ready, DateTime.UtcNow.ToString("O"));
            log?.Invoke($"assets готовы ({indexId})");
            return indexId;
        }
        log?.Invoke($"assets: {missing.Count} файлов…");
        using var gate = new SemaphoreSlim(96);
        var done = 0;
        await Task.WhenAll(missing.Select(async m =>
        {
            await gate.WaitAsync(ct);
            try
            {
                Exception? last = null;
                foreach (var mirror in LauncherConstants.AssetMirrors)
                {
                    try
                    {
                        await HttpDownload.DownloadAssetFastAsync($"{mirror}/{m.Hash[..2]}/{m.Hash}", m.Dest, ct);
                        last = null;
                        break;
                    }
                    catch (Exception ex)
                    {
                        last = ex;
                        try { if (File.Exists(m.Dest)) File.Delete(m.Dest); } catch { /* ignore */ }
                    }
                }
                if (last != null) throw last;
            }
            catch { /* skip bad asset */ }
            finally
            {
                var d = Interlocked.Increment(ref done);
                if (d % 200 == 0 || d == missing.Count) log?.Invoke($"assets {d}/{missing.Count}");
                gate.Release();
            }
        }));
        File.WriteAllText(ready, DateTime.UtcNow.ToString("O"));
        log?.Invoke("assets готовы");
        return indexId;
    }

    private static bool LibAllowed(JsonObject lib)
    {
        var name = lib["name"]?.GetValue<string>() ?? "";
        if (name.Contains("natives-windows-arm64") || name.Contains("natives-windows-x86")) return false;
        var rules = lib["rules"] as JsonArray;
        if (rules == null) return true;
        var allowed = false;
        foreach (var rule in rules)
        {
            var action = rule?["action"]?.GetValue<string>() == "allow";
            var osName = rule?["os"]?["name"]?.GetValue<string>();
            if (osName == null) allowed = action;
            else if (osName == "windows") allowed = action;
        }
        return allowed;
    }

    private static string? ArtifactPathFromName(string name)
    {
        var parts = name.Split(':');
        if (parts.Length < 3) return null;
        var (group, artifact, version) = (parts[0], parts[1], parts[2]);
        return $"{group.Replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.jar";
    }

    private static void EnsureRussianOptions(string gameDir)
    {
        var opt = Path.Combine(gameDir, "options.txt");
        if (File.Exists(opt)) return;
        File.WriteAllText(opt, "lang:ru_ru\n");
    }
}

public static class ProcessSpawner
{
    private static readonly List<StreamWriter> OpenLogs = [];

    public static Process Spawn(IReadOnlyList<string> cmd, string gameDir)
    {
        var logDir = Path.Combine(gameDir, "logs");
        Directory.CreateDirectory(logDir);
        try { File.WriteAllLines(Path.Combine(logDir, "last_launch_cmd.txt"), cmd); }
        catch { /* ignore */ }

        var consolePath = Path.Combine(logDir, "minecraft_console.log");
        var console = new StreamWriter(new FileStream(consolePath, FileMode.Create, FileAccess.Write, FileShare.ReadWrite))
        { AutoFlush = true };
        lock (OpenLogs) OpenLogs.Add(console);

        var psi = new ProcessStartInfo
        {
            FileName = cmd[0],
            WorkingDirectory = gameDir,
            UseShellExecute = false,
            CreateNoWindow = true,
            WindowStyle = ProcessWindowStyle.Hidden,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
        };
        foreach (var a in cmd.Skip(1)) psi.ArgumentList.Add(a);

        var proc = Process.Start(psi)!;
        proc.OutputDataReceived += (_, e) => { if (e.Data != null) try { console.WriteLine(e.Data); } catch { /* ignore */ } };
        proc.ErrorDataReceived += (_, e) => { if (e.Data != null) try { console.WriteLine(e.Data); } catch { /* ignore */ } };
        proc.BeginOutputReadLine();
        proc.BeginErrorReadLine();
        return proc;
    }
}
