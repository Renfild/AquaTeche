using System.Text.Json;
using System.Text.Json.Serialization;

namespace AquaTechLauncher.Core;

public sealed class LauncherConfig
{
    [JsonPropertyName("username")]
    public string Username { get; set; } = "";

    [JsonPropertyName("game_dir")]
    public string GameDir { get; set; } = LauncherConstants.GameDirDefault;

    [JsonPropertyName("ram_mb")]
    public int RamMb { get; set; } = 4096;

    [JsonPropertyName("update_url")]
    public string UpdateUrl { get; set; } = LauncherConstants.DefaultUpdateUrl;

    [JsonPropertyName("server_host")]
    public string? ServerHost { get; set; }

    [JsonPropertyName("server_port")]
    public string? ServerPort { get; set; }

    public static LauncherConfig Load()
    {
        var cfg = new LauncherConfig();
        try
        {
            var path = LauncherConstants.ConfigPath;
            if (File.Exists(path))
            {
                var loaded = JsonSerializer.Deserialize<LauncherConfig>(File.ReadAllText(path));
                if (loaded != null) cfg = loaded;
            }
        }
        catch
        {
            /* keep defaults */
        }
        cfg.UpdateUrl = NormalizeUpdateUrl(cfg.UpdateUrl);
        if (string.IsNullOrWhiteSpace(cfg.GameDir))
            cfg.GameDir = LauncherConstants.GameDirDefault;
        if (cfg.RamMb < 1024) cfg.RamMb = 4096;
        return cfg;
    }

    public void Save()
    {
        UpdateUrl = NormalizeUpdateUrl(UpdateUrl);
        var json = JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true });
        File.WriteAllText(LauncherConstants.ConfigPath, json);
    }

    public static string NormalizeUpdateUrl(string? url)
    {
        var u = (url ?? "").Trim().TrimEnd('/');
        if (string.IsNullOrWhiteSpace(u))
            return LauncherConstants.DefaultUpdateUrl;
        var low = u.ToLowerInvariant();
        if (low.Contains("tun.ply.gg") || low.Contains("playit") || low.Contains("pages.dev"))
            return LauncherConstants.DefaultUpdateUrl;
        if (low.Contains(LauncherConstants.ServerHost.ToLowerInvariant()))
            return LauncherConstants.DefaultUpdateUrl;
        return u;
    }

    public string EffectiveHost =>
        string.IsNullOrWhiteSpace(ServerHost) ? LauncherConstants.ServerHost : ServerHost.Trim();

    public int EffectivePort
    {
        get
        {
            if (int.TryParse(ServerPort, out var p) && p > 0) return p;
            return LauncherConstants.ServerPort;
        }
    }
}
