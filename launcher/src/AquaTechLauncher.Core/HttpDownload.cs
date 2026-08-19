using System.Net;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;

namespace AquaTechLauncher.Core;

public static class HttpDownload
{
    private static readonly CookieContainer Cookies = new();
    private static readonly SocketsHttpHandler SocketsHandler = new()
    {
        CookieContainer = Cookies,
        UseCookies = true,
        AutomaticDecompression = DecompressionMethods.All,
        MaxConnectionsPerServer = 128,
        PooledConnectionLifetime = TimeSpan.FromMinutes(5),
        PooledConnectionIdleTimeout = TimeSpan.FromMinutes(2),
        EnableMultipleHttp2Connections = true,
    };
    private static readonly HttpClient Client = CreateClient(TimeSpan.FromMinutes(10));
    private static readonly HttpClient MetadataClient = CreateClient(TimeSpan.FromSeconds(15));
    private static readonly HttpClient AssetClient = CreateClient(TimeSpan.FromSeconds(12));
    private static readonly Uri PortalUri = new(LauncherConstants.PortalApiBase + "/");
    private static readonly Uri FallbackPortalUri = new(LauncherConstants.FallbackPortalApiBase + "/");

    private static HttpClient CreateClient(TimeSpan timeout)
    {
        var c = new HttpClient(SocketsHandler) { Timeout = timeout };
        c.DefaultRequestHeaders.UserAgent.ParseAdd(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("*/*"));
        c.DefaultRequestHeaders.TryAddWithoutValidation("X-AquaTech-Launcher", "1");
        return c;
    }

    public static void SetPortalSession(string? sessionId)
    {
        ClearPortalCookies();
        if (string.IsNullOrWhiteSpace(sessionId))
            return;
        var s = sessionId.Trim();
        Cookies.Add(PortalUri, new Cookie("at_session", s)
        {
            Path = "/",
            Secure = true,
            HttpOnly = true,
        });
        Cookies.Add(FallbackPortalUri, new Cookie("at_session", s)
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
            return Cookies.GetCookies(PortalUri)["at_session"]?.Value
                ?? Cookies.GetCookies(FallbackPortalUri)["at_session"]?.Value;
        }
        catch
        {
            return null;
        }
    }

    private static void ClearPortalCookies()
    {
        foreach (Cookie c in Cookies.GetCookies(PortalUri)) c.Expired = true;
        foreach (Cookie c in Cookies.GetCookies(FallbackPortalUri)) c.Expired = true;
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

    public static async Task DownloadAssetFastAsync(string url, string destPath, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        using var resp = await AssetClient.SendAsync(req, HttpCompletionOption.ResponseHeadersRead, ct);
        resp.EnsureSuccessStatusCode();
        await using var src = await resp.Content.ReadAsStreamAsync(ct);
        await using var dst = new FileStream(destPath, FileMode.Create, FileAccess.Write, FileShare.None, 16384, useAsync: true);
        await src.CopyToAsync(dst, ct);
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
            }
        }
        throw last ?? new Exception($"Failed to download {url}");
    }

    public static async Task<string> GetStringAsync(string url, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        if (url.Contains("api.github.com", StringComparison.OrdinalIgnoreCase))
            req.Headers.TryAddWithoutValidation("Accept", "application/vnd.github.raw");
        req.Headers.CacheControl = new CacheControlHeaderValue { NoCache = true };
        using var resp = await MetadataClient.SendAsync(req, ct);
        resp.EnsureSuccessStatusCode();
        return await resp.Content.ReadAsStringAsync(ct);
    }

    public static async Task<(int StatusCode, string Body)> GetRawAsync(string url, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        using var resp = await MetadataClient.SendAsync(req, ct);
        var body = await resp.Content.ReadAsStringAsync(ct);
        return ((int)resp.StatusCode, body);
    }

    public static async Task<(string Json, string? CookieSession)> PostJsonAsync(
        string url, string jsonBody, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Post, url)
        {
            Content = new StringContent(jsonBody, Encoding.UTF8, "application/json"),
        };
        using var resp = await MetadataClient.SendAsync(req, ct);
        var body = await resp.Content.ReadAsStringAsync(ct);
        if (!resp.IsSuccessStatusCode)
            throw new HttpRequestException($"HTTP {(int)resp.StatusCode}: {body}");

        var fromHeader = ExtractSessionFromSetCookie(resp);
        var fromJar = GetPortalSession();
        return (body, fromHeader ?? fromJar);
    }

    public static string? ExtractSessionFromSetCookie(HttpResponseMessage resp)
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
