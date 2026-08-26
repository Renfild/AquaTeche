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
    private static readonly string[] ApiBases =
    [
        LauncherConstants.PortalApiBase,
        LauncherConstants.FallbackPortalApiBase,
    ];

    public static async Task<PortalStats?> FetchServerStatusAsync(CancellationToken ct = default)
    {
        foreach (var baseUrl in ApiBases)
        {
            try
            {
                var json = await HttpDownload.GetStringAsync($"{baseUrl}/api/server-status", ct);
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
                /* try next */
            }
        }
        return null;
    }

    public static async Task<(bool Ok, UserProfile? Profile, string? Session, string Error)> TryRestoreSessionAsync(
        string? sessionId, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(sessionId))
            return (false, null, null, "no session");
        HttpDownload.SetPortalSession(sessionId);

        foreach (var baseUrl in ApiBases)
        {
            try
            {
                var (status, body) = await HttpDownload.GetRawAsync($"{baseUrl}/api/me", ct);
                if (status != 200 || string.IsNullOrWhiteSpace(body) || body.StartsWith("<"))
                    continue;

                using var doc = JsonDocument.Parse(body);
                var root = doc.RootElement;
                if (!root.TryGetProperty("user", out var userEl))
                    continue;

                var nick = userEl.GetProperty("nick").GetString();
                if (string.IsNullOrWhiteSpace(nick))
                    continue;

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
            catch
            {
                /* try next */
            }
        }

        HttpDownload.SetPortalSession(null);
        return (false, null, null, "Сессия истекла");
    }

    public const string UnclaimedNickMessage =
        "Этот ник уже есть с сервера. Задай пароль на странице регистрации.";

    public static async Task<bool> NickIsUnclaimedAsync(string nick, CancellationToken ct = default)
    {
        var q = Uri.EscapeDataString(nick.Trim());
        foreach (var baseUrl in ApiBases)
        {
            try
            {
                var (status, body) = await HttpDownload.GetRawAsync($"{baseUrl}/api/auth/nick?nick={q}", ct);
                if (status != 200 || string.IsNullOrWhiteSpace(body) || body.StartsWith("<"))
                    continue;
                using var doc = JsonDocument.Parse(body);
                var root = doc.RootElement;
                return root.TryGetProperty("unclaimed", out var u) && u.GetBoolean();
            }
            catch
            {
                /* try next */
            }
        }
        return false;
    }

    public static string LoginFailureMessage(string? jsonBody, int status)
    {
        if (status == 429)
            return ExtractError(jsonBody) ?? "Слишком много попыток. Подожди минуту.";
        var code = ExtractCode(jsonBody);
        if (code == "unclaimed")
            return UnclaimedNickMessage;
        var msg = ExtractError(jsonBody);
        if (!string.IsNullOrWhiteSpace(msg) && msg.Contains("регистрац", StringComparison.OrdinalIgnoreCase))
            return UnclaimedNickMessage;
        if (status == 401)
            return CleanError(msg ?? "Неверный логин или пароль");
        return CleanError(msg ?? $"Ошибка входа ({status})");
    }

    public static async Task<(bool Ok, UserProfile? Profile, string? Session, string Error)> LoginAsync(
        string nick, string password, CancellationToken ct = default)
    {
        var payload = JsonSerializer.Serialize(new { nick = nick.Trim(), password });
        string lastError = "Не удалось связаться с сервером авторизации";

        foreach (var baseUrl in ApiBases)
        {
            try
            {
                var (status, json, cookieSession) = await HttpDownload.PostJsonRawAsync(
                    $"{baseUrl}/api/login", payload, ct);

                if (string.IsNullOrWhiteSpace(json) || json.StartsWith("<"))
                {
                    lastError = "Сервер вернул неожиданный ответ";
                    continue;
                }

                if (status != 200)
                    return (false, null, null, LoginFailureMessage(json, status));

                using var doc = JsonDocument.Parse(json);
                var root = doc.RootElement;
                if (!root.TryGetProperty("ok", out var okEl) || !okEl.GetBoolean())
                    return (false, null, null, LoginFailureMessage(json, status));

                var userNick = root.GetProperty("user").GetProperty("nick").GetString();
                var session = root.TryGetProperty("session", out var s)
                    ? s.GetString()
                    : null;
                if (string.IsNullOrWhiteSpace(session))
                    session = cookieSession;
                if (string.IsNullOrWhiteSpace(session))
                    session = HttpDownload.GetPortalSession();
                if (string.IsNullOrWhiteSpace(userNick) || string.IsNullOrWhiteSpace(session))
                    return (false, null, null, "Ошибка получения сессии");

                HttpDownload.SetPortalSession(session);

                var (_, profile, _, _) = await TryRestoreSessionAsync(session, ct);
                profile ??= new UserProfile(userNick, false, 0, 0, 0);

                return (true, profile, session, "");
            }
            catch (Exception ex)
            {
                lastError = CleanError(ex.Message);
            }
        }

        return (false, null, null, lastError);
    }

    private static string? ExtractError(string? jsonBody)
    {
        if (string.IsNullOrWhiteSpace(jsonBody) || jsonBody.StartsWith("<"))
            return null;
        try
        {
            using var doc = JsonDocument.Parse(jsonBody);
            if (doc.RootElement.TryGetProperty("error", out var err))
                return err.GetString();
        }
        catch (JsonException)
        {
            /* not json */
        }
        return null;
    }

    private static string? ExtractCode(string? jsonBody)
    {
        if (string.IsNullOrWhiteSpace(jsonBody) || jsonBody.StartsWith("<"))
            return null;
        try
        {
            using var doc = JsonDocument.Parse(jsonBody);
            if (doc.RootElement.TryGetProperty("code", out var c))
                return c.GetString();
        }
        catch (JsonException)
        {
            /* not json */
        }
        return null;
    }

    private static string CleanError(string raw)
    {
        if (string.IsNullOrWhiteSpace(raw)) return "Ошибка авторизации";
        if (raw.Contains("<") || raw.Contains("DOCTYPE") || raw.Contains("Just a moment"))
        {
            return "Защита Cloudflare заблокировала запрос. Попробуйте снова через минуту.";
        }
        if (raw.StartsWith("HTTP 401")) return "Неверный логин или пароль";
        if (raw.StartsWith("HTTP 403")) return "Доступ ограничен сервером (403)";
        if (raw.Length > 120) return raw[..120] + "…";
        return raw;
    }
}
