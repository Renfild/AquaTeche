using System.Net.Http.Headers;
using System.Security.Cryptography;

namespace AquaTechLauncher.Core;

public static class HttpDownload
{
    private static readonly HttpClient Client = CreateClient();

    private static HttpClient CreateClient()
    {
        var c = new HttpClient { Timeout = TimeSpan.FromMinutes(10) };
        c.DefaultRequestHeaders.UserAgent.ParseAdd($"Mozilla/5.0 AquaTechLauncher/{LauncherConstants.Version}");
        c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("*/*"));
        return c;
    }

    public static async Task DownloadAsync(string url, string destPath, CancellationToken ct = default)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(destPath)!);
        var tmp = destPath + ".part";
        try
        {
            using var resp = await Client.GetAsync(url, HttpCompletionOption.ResponseHeadersRead, ct);
            resp.EnsureSuccessStatusCode();
            await using var src = await resp.Content.ReadAsStreamAsync(ct);
            await using var dst = File.Create(tmp);
            await src.CopyToAsync(dst, ct);
            dst.Close();
            if (File.Exists(destPath)) File.Delete(destPath);
            File.Move(tmp, destPath);
        }
        catch
        {
            try { if (File.Exists(tmp)) File.Delete(tmp); } catch { /* ignore */ }
            throw;
        }
    }

    public static async Task DownloadMirroredAsync(string url, string destPath, CancellationToken ct = default)
    {
        var urls = new List<string> { url };
        if (url.Contains("maven.minecraftforge.net", StringComparison.OrdinalIgnoreCase))
        {
            var path = url.Split(["maven.minecraftforge.net/"], StringSplitOptions.None).LastOrDefault();
            if (!string.IsNullOrEmpty(path))
            {
                urls.Insert(0, $"https://bmclapi2.bangbang93.com/maven/{path}");
                urls.Add($"https://maven.aliyun.com/repository/public/{path}");
            }
        }
        Exception? last = null;
        foreach (var u in urls.Distinct())
        {
            try
            {
                await DownloadAsync(u, destPath, ct);
                return;
            }
            catch (Exception ex)
            {
                last = ex;
                try { if (File.Exists(destPath)) File.Delete(destPath); } catch { /* ignore */ }
            }
        }
        throw last ?? new IOException($"Download failed: {url}");
    }

    public static async Task<string> GetStringAsync(string url, CancellationToken ct = default)
    {
        using var resp = await Client.GetAsync(url, ct);
        resp.EnsureSuccessStatusCode();
        return await resp.Content.ReadAsStringAsync(ct);
    }

    public static async Task<string> PostJsonAsync(string url, string jsonBody, CancellationToken ct = default)
    {
        using var content = new StringContent(jsonBody, System.Text.Encoding.UTF8, "application/json");
        using var resp = await Client.PostAsync(url, content, ct);
        var body = await resp.Content.ReadAsStringAsync(ct);
        if (!resp.IsSuccessStatusCode)
            throw new HttpRequestException($"HTTP {(int)resp.StatusCode}: {body}");
        return body;
    }

    public static string Md5File(string path)
    {
        using var fs = File.OpenRead(path);
        var hash = MD5.HashData(fs);
        return Convert.ToHexString(hash).ToLowerInvariant();
    }
}
