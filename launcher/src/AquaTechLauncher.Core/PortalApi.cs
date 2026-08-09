using System.Text.Json;

namespace AquaTechLauncher.Core;

public sealed class PortalStats
{
    public bool Online { get; set; }
    public int PlayersOnline { get; set; }
    public int PlayersMax { get; set; }
}

public static class PortalApi
{
    public static async Task<PortalStats?> FetchServerStatusAsync(CancellationToken ct = default)
    {
        try
        {
            var json = await HttpDownload.GetStringAsync($"{LauncherConstants.PortalApiBase}/api/server-status", ct);
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;
            return new PortalStats
            {
                Online = root.TryGetProperty("online", out var o) && o.GetBoolean(),
                PlayersOnline = root.TryGetProperty("players_online", out var po) ? po.GetInt32() : 0,
                PlayersMax = root.TryGetProperty("players_max", out var pm) ? pm.GetInt32() : 0,
            };
        }
        catch
        {
            return null;
        }
    }

    public static async Task<(bool Ok, string? Nick, string? Session, string Error)> TryRestoreSessionAsync(
        string? sessionId, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(sessionId))
            return (false, null, null, "no session");
        HttpDownload.SetPortalSession(sessionId);
        try
        {
            var (status, body) = await HttpDownload.GetRawAsync($"{LauncherConstants.PortalApiBase}/api/me", ct);
            if (status != 200)
            {
                HttpDownload.SetPortalSession(null);
                return (false, null, null, "expired");
            }
            using var doc = JsonDocument.Parse(body);
            var nick = doc.RootElement.GetProperty("user").GetProperty("nick").GetString();
            if (string.IsNullOrWhiteSpace(nick))
            {
                HttpDownload.SetPortalSession(null);
                return (false, null, null, "bad me");
            }
            return (true, nick, sessionId, "");
        }
        catch (Exception ex)
        {
            HttpDownload.SetPortalSession(null);
            return (false, null, null, ex.Message);
        }
    }

    public static async Task<(bool Ok, string? Nick, string? Session, string Error)> LoginAsync(
        string nick, string password, CancellationToken ct = default)
    {
        try
        {
            var payload = JsonSerializer.Serialize(new { nick, password });
            var json = await HttpDownload.PostJsonAsync(
                $"{LauncherConstants.PortalApiBase}/api/login", payload, ct);
            using var doc = JsonDocument.Parse(json);
            if (!doc.RootElement.TryGetProperty("ok", out var okEl) || !okEl.GetBoolean())
                return (false, null, null, "login failed");
            var userNick = doc.RootElement.GetProperty("user").GetProperty("nick").GetString();
            var session = doc.RootElement.TryGetProperty("session", out var s)
                ? s.GetString()
                : null;
            if (string.IsNullOrWhiteSpace(userNick) || string.IsNullOrWhiteSpace(session))
                return (false, null, null, "Лаунчер не получил сессию — обнови сайт");
            HttpDownload.SetPortalSession(session);
            return (true, userNick, session, "");
        }
        catch (HttpRequestException ex)
        {
            if (ex.Message.Contains("401")) return (false, null, null, "Неверный логин или пароль");
            return (false, null, null, ex.Message);
        }
        catch (Exception ex)
        {
            return (false, null, null, ex.Message);
        }
    }
}
