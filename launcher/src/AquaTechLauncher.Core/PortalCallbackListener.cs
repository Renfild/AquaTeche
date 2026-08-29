using System.Net;
using System.Net.Sockets;
using System.Text;

namespace AquaTechLauncher.Core;

/// <summary>
/// Listens on 127.0.0.1:{port} for the website's SSO redirect
/// (…/api/portal_callback?session=…&nick=…) and hands the session to the app.
/// TcpListener instead of HttpListener: no admin/URLACL rights needed.
/// </summary>
public sealed class PortalCallbackListener : IDisposable
{
    public const int DefaultPort = 12450;

    private TcpListener? _listener;
    private CancellationTokenSource? _cts;

    public event Action<string, string?>? CallbackReceived;

    public bool IsRunning { get; private set; }

    public static (string? Session, string? Nick) ParseCallbackUrl(string? url)
    {
        if (string.IsNullOrWhiteSpace(url)) return (null, null);
        if (!Uri.TryCreate(url, UriKind.Absolute, out var uri)) return (null, null);
        if (!uri.AbsolutePath.EndsWith("portal_callback", StringComparison.Ordinal)) return (null, null);

        string? session = null;
        string? nick = null;
        var query = uri.Query.TrimStart('?');
        foreach (var pair in query.Split('&', StringSplitOptions.RemoveEmptyEntries))
        {
            var kv = pair.Split('=', 2);
            if (kv.Length != 2) continue;
            var value = Uri.UnescapeDataString(kv[1]);
            if (kv[0] == "session") session = value;
            else if (kv[0] == "nick") nick = value;
        }
        return (
            string.IsNullOrWhiteSpace(session) ? null : session,
            string.IsNullOrWhiteSpace(nick) ? null : nick);
    }

    /// <returns>false when the port is busy (second launcher instance) or blocked.</returns>
    public bool Start(int port = DefaultPort)
    {
        if (IsRunning) return true;
        try
        {
            _listener = new TcpListener(IPAddress.Loopback, port);
            _listener.Start();
            _cts = new CancellationTokenSource();
            IsRunning = true;
            _ = AcceptLoopAsync(_listener, _cts.Token);
            return true;
        }
        catch
        {
            Stop();
            return false;
        }
    }

    public void Stop()
    {
        try
        {
            _cts?.Cancel();
            _listener?.Stop();
        }
        catch
        {
            /* already closed */
        }
        _listener = null;
        _cts = null;
        IsRunning = false;
    }

    public void Dispose() => Stop();

    private async Task AcceptLoopAsync(TcpListener listener, CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            TcpClient client;
            try
            {
                client = await listener.AcceptTcpClientAsync(ct);
            }
            catch
            {
                break;
            }
            _ = HandleClientAsync(client, ct);
        }
        IsRunning = false;
    }

    private async Task HandleClientAsync(TcpClient client, CancellationToken ct)
    {
        using var _ = client;
        try
        {
            var stream = client.GetStream();
            var buf = new byte[8192];
            var read = await stream.ReadAsync(buf, ct);
            if (read <= 0) return;

            var requestLine = Encoding.ASCII.GetString(buf, 0, read).Split('\r', '\n')[0];
            var parts = requestLine.Split(' ');
            if (parts.Length < 2) return;
            var (session, nick) = ParseCallbackUrl("http://127.0.0.1" + parts[1]);
            if (session == null) return;

            await stream.WriteAsync(BuildOkResponse(), ct);
            CallbackReceived?.Invoke(session, nick);
        }
        catch
        {
            /* browser closed the tab or vanished */
        }
    }

    private static ReadOnlyMemory<byte> BuildOkResponse()
    {
        const string body =
            "<!DOCTYPE html><html lang=\"ru\"><meta charset=\"utf-8\"><title>AquaTech</title>" +
            "<body style=\"background:#070b12;color:#f8fafc;font-family:'Segoe UI',sans-serif;" +
            "display:grid;place-items:center;height:100vh;margin:0\">" +
            "<div style=\"text-align:center\"><h1>Вход выполнен</h1>" +
            "<p>Вернись в лаунчер AquaTech — эту вкладку можно закрыть.</p></div>";
        var head = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n" +
                   $"Content-Length: {Encoding.UTF8.GetByteCount(body)}\r\n\r\n";
        var memory = new MemoryStream();
        memory.Write(Encoding.ASCII.GetBytes(head));
        memory.Write(Encoding.UTF8.GetBytes(body));
        return memory.ToArray();
    }
}
