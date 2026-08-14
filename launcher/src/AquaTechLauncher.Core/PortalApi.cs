using System.Text.Json;

namespace AquaTechLauncher.Core;

public sealed class PortalStats
{
    public bool Online { get; set; }
    public int PlayersOnline { get; set; }
    public int PlayersMax { get; set; }
}

public sealed record UserProfile(
    string Nick,
    bool IsAdmin,
    long Coins,
    long HoursPlayed,
    long Likes);

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

    public static async Task<(bool Ok, UserProfile? Profile, string? Session, string Error)> TryRestoreSessionAsync(
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
            var root = doc.RootElement;
            if (!root.TryGetProperty("user", out var userEl))
            {
                HttpDownload.SetPortalSession(null);
                return (false, null, null, "bad user");
            }
            var nick = userEl.GetProperty("nick").GetString();
            if (string.IsNullOrWhiteSpace(nick))
            {
                HttpDownload.SetPortalSession(null);
                return (false, null, null, "bad me");
            }
            var isAdmin = userEl.TryGetProperty("is_admin", out var ia) && ia.GetBoolean();
            long coins = 0;
            long hours = 0;
            long likes = 0;
            if (root.TryGetProperty("profile", out var profEl))
            {
                if (profEl.TryGetProperty("coins", out var c)) coins = c.GetInt64();
                if (profEl.TryGetProperty("hours_played", out var h)) hours = h.GetInt64();
                if (profEl.TryGetProperty("likes", out var l)) likes = l.GetInt64();
            }

            var profile = new UserProfile(nick, isAdmin, coins, hours, likes);
            return (true, profile, sessionId, "");
        }
        catch (Exception ex)
        {
            HttpDownload.SetPortalSession(null);
            return (false, null, null, ex.Message);
        }
    }

    public static async Task<(bool Ok, UserProfile? Profile, string? Session, string Error)> LoginAsync(
        string nick, string password, CancellationToken ct = default)
    {
        try
        {
            var payload = JsonSerializer.Serialize(new { nick, password });
            var (json, cookieSession) = await HttpDownload.PostJsonAsync(
                $"{LauncherConstants.PortalApiBase}/api/login", payload, ct);
            using var doc = JsonDocument.Parse(json);
            if (!doc.RootElement.TryGetProperty("ok", out var okEl) || !okEl.GetBoolean())
                return (false, null, null, "Неверный логин или пароль");
            var userNick = doc.RootElement.GetProperty("user").GetProperty("nick").GetString();
            var session = doc.RootElement.TryGetProperty("session", out var s)
                ? s.GetString()
                : null;
            if (string.IsNullOrWhiteSpace(session))
                session = cookieSession;
            if (string.IsNullOrWhiteSpace(session))
                session = HttpDownload.GetPortalSession();
            if (string.IsNullOrWhiteSpace(userNick) || string.IsNullOrWhiteSpace(session))
                return (false, null, null, "Ошибка получения сессии");
            HttpDownload.SetPortalSession(session);

            // Fetch profile info
            var (_, profile, _, _) = await TryRestoreSessionAsync(session, ct);
            profile ??= new UserProfile(userNick, false, 0, 0, 0);

            return (true, profile, session, "");
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
