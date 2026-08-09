using System.Text.Json;
using System.Text.Json.Serialization;

namespace AquaTechLauncher.Core;

public sealed class PortalStats
{
    [JsonPropertyName("online")]
    public bool Online { get; set; }

    [JsonPropertyName("players_online")]
    public int PlayersOnline { get; set; }

    [JsonPropertyName("players_max")]
    public int PlayersMax { get; set; }
}

public sealed class PortalPlayer
{
    [JsonPropertyName("nick")]
    public string Nick { get; set; } = "";

    [JsonPropertyName("privilege")]
    public string? Privilege { get; set; }

    [JsonPropertyName("likes")]
    public int Likes { get; set; }

    [JsonPropertyName("playtime_hours")]
    public int PlaytimeHours { get; set; }

    [JsonPropertyName("fish")]
    public int Fish { get; set; }
}

public static class PortalApi
{
    public static async Task<PortalStats?> FetchServerStatusAsync(CancellationToken ct = default)
    {
        try
        {
            var json = await HttpDownload.GetStringAsync($"{LauncherConstants.PortalApiBase}/api/server-status", ct);
            return JsonSerializer.Deserialize<PortalStats>(json);
        }
        catch
        {
            return null;
        }
    }

    public static async Task<IReadOnlyList<PortalPlayer>> FetchTopPlayersAsync(string sort = "likes", int limit = 5, CancellationToken ct = default)
    {
        try
        {
            var json = await HttpDownload.GetStringAsync(
                $"{LauncherConstants.PortalApiBase}/api/players?sort={Uri.EscapeDataString(sort)}&limit={limit}", ct);
            using var doc = JsonDocument.Parse(json);
            if (!doc.RootElement.TryGetProperty("players", out var arr)) return [];
            var list = new List<PortalPlayer>();
            foreach (var el in arr.EnumerateArray())
            {
                var p = el.Deserialize<PortalPlayer>();
                if (p != null && !string.IsNullOrWhiteSpace(p.Nick)) list.Add(p);
            }
            return list;
        }
        catch
        {
            return [];
        }
    }
}
