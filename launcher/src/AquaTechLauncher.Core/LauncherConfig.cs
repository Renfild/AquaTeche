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

    [JsonPropertyName("portal_session")]
    public string? PortalSession { get; set; }

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
        cfg.UpdateUrl = LauncherConstants.DefaultUpdateUrl;
        if (string.IsNullOrWhiteSpace(cfg.GameDir))
            cfg.GameDir = LauncherConstants.GameDirDefault;
        if (cfg.RamMb < 1024) cfg.RamMb = 4096;
        return cfg;
    }

    public void Save()
    {
        UpdateUrl = LauncherConstants.DefaultUpdateUrl;
        var json = JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true });
        File.WriteAllText(LauncherConstants.ConfigPath, json);
    }

    public static string NormalizeUpdateUrl(string? url)
    {
        // Pack CDN is baked into the launcher; ignore user/config overrides that point at tunnels.
        _ = url;
        return LauncherConstants.DefaultUpdateUrl;
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
