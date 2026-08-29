using System.Text.Json;

namespace AquaTechLauncher.Core;

/// <summary>Offline copy of the last successful news fetch.</summary>
public static class NewsCache
{
    public static string CachePath =>
        Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "AquaTech", "news_cache.json");

    public static IReadOnlyList<NewsItem> Load()
    {
        try
        {
            if (!File.Exists(CachePath)) return [];
            var items = JsonSerializer.Deserialize<List<NewsItem>>(File.ReadAllText(CachePath));
            return items ?? [];
        }
        catch
        {
            return [];
        }
    }

    public static void Save(IReadOnlyList<NewsItem> items)
    {
        try
        {
            Directory.CreateDirectory(Path.GetDirectoryName(CachePath)!);
            File.WriteAllText(CachePath, JsonSerializer.Serialize(items, new JsonSerializerOptions { WriteIndented = true }));
        }
        catch
        {
            /* cache is best-effort */
        }
    }
}
