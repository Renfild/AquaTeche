using System.Net;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;

namespace AquaTechLauncher.Core;

public static class HttpDownload
{
    private static readonly CookieContainer Cookies = new();
    private static readonly HttpClientHandler Handler = new()
    {
        CookieContainer = Cookies,
        UseCookies = true,
        AutomaticDecompression = DecompressionMethods.All,
    };
    private static readonly HttpClient Client = CreateClient();
    private static readonly Uri PortalUri = new(LauncherConstants.PortalApiBase + "/");

    private static HttpClient CreateClient()
    {
        var c = new HttpClient(Handler) { Timeout = TimeSpan.FromMinutes(10) };
        c.DefaultRequestHeaders.UserAgent.ParseAdd($"Mozilla/5.0 AquaTechLauncher/{LauncherConstants.Version}");
        c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("*/*"));
        c.DefaultRequestHeaders.TryAddWithoutValidation("X-AquaTech-Launcher", "1");
        return c;
    }

    public static void SetPortalSession(string? sessionId)
    {
        ClearPortalCookies();
        if (string.IsNullOrWhiteSpace(sessionId))
            return;
        Cookies.Add(PortalUri, new Cookie("at_session", sessionId.Trim())
        {
            Path = "/",
            Secure = true,
            HttpOnly = true,
        });
    }

    public static string? GetPortalSession()
    {
        try
        {
            return Cookies.GetCookies(PortalUri)["at_session"]?.Value;
        }
        catch
        {
            return null;
        }
    }

    private static void ClearPortalCookies()
    {
        foreach (Cookie c in Cookies.GetCookies(PortalUri))
            c.Expired = true;
    }

    public static async Task DownloadAsync(string url, string destPath, CancellationToken ct = default)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(destPath)!);
        var tmp = destPath + ".part";
        try
        {
            using var req = new HttpRequestMessage(HttpMethod.Get, url);
            using var resp = await Client.SendAsync(req, HttpCompletionOption.ResponseHeadersRead, ct);
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
            try { if (File.Exists(tmp)) File.Delete(tmp); } catch { /* ignore cleanup */ }
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
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        using var resp = await Client.SendAsync(req, ct);
        resp.EnsureSuccessStatusCode();
        return await resp.Content.ReadAsStringAsync(ct);
    }

    public static async Task<(int Status, string Body)> GetRawAsync(string url, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        using var resp = await Client.SendAsync(req, ct);
        return ((int)resp.StatusCode, await resp.Content.ReadAsStringAsync(ct));
    }

    public static async Task<(string Body, string? SessionId)> PostJsonAsync(
        string url, string jsonBody, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Post, url)
        {
            Content = new StringContent(jsonBody, Encoding.UTF8, "application/json"),
        };
        using var resp = await Client.SendAsync(req, ct);
        var body = await resp.Content.ReadAsStringAsync(ct);
        if (!resp.IsSuccessStatusCode)
            throw new HttpRequestException($"HTTP {(int)resp.StatusCode}: {body}");

        var fromHeader = ExtractSessionFromSetCookie(resp);
        var fromJar = GetPortalSession();
        return (body, fromHeader ?? fromJar);
    }

    internal static string? ExtractSessionFromSetCookie(HttpResponseMessage resp)
    {
        IEnumerable<string>? values = null;
        if (resp.Headers.TryGetValues("Set-Cookie", out var h))
            values = h;
        else if (resp.Headers.NonValidated.TryGetValues("Set-Cookie", out var nv))
            values = nv;

        if (values == null)
            return null;

        foreach (var raw in values)
        {
            var m = Regex.Match(raw, @"(?:^|,\s*)at_session=([^;,\s]+)", RegexOptions.IgnoreCase);
            if (m.Success)
                return Uri.UnescapeDataString(m.Groups[1].Value.Trim());
        }
        return null;
    }

    public static string Md5File(string path)
    {
        using var fs = File.OpenRead(path);
        var hash = MD5.HashData(fs);
        return Convert.ToHexString(hash).ToLowerInvariant();
    }
}
