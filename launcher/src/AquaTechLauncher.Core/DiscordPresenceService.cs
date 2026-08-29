using System.IO;
using System.IO.Pipes;
using System.Text;
using System.Text.Json;

namespace AquaTechLauncher.Core;

/// <summary>
/// Minimal Discord Rich Presence client over the local IPC pipe (discord-ipc-0…9).
/// No SDK: handshake OP0, then SET_ACTIVITY OP1 frames. Silent no-op when
/// Discord is not running or ClientId is empty.
/// </summary>
public sealed class DiscordPresenceService : IDisposable
{
    /// <summary>Unix ms of the current play session (for elapsed time).</summary>
    private long _playStartUnixMs;
    private string _details = "";
    private string _state = "";
    private readonly SemaphoreSlim _sendLock = new(1, 1);
    private CancellationTokenSource? _cts;
    private Task? _loop;

    public string ClientId { get; set; } = LauncherConstants.DiscordClientId;
    public bool Enabled { get; set; } = true;

    public void Start()
    {
        if (_loop != null) return;
        if (!Enabled || string.IsNullOrWhiteSpace(ClientId)) return;
        _cts = new CancellationTokenSource();
        _loop = Task.Run(() => LoopAsync(_cts.Token));
    }

    public void SetMenu(string nick)
    {
        _details = "В лаунчере AquaTech";
        _state = string.IsNullOrWhiteSpace(nick) ? "Смотрит на океан" : $"Игрок: {nick}";
        _playStartUnixMs = 0;
        Wake();
    }

    public void SetBusyPreparing()
    {
        _details = "Готовит сборку AquaTech";
        _state = "";
        _playStartUnixMs = 0;
        Wake();
    }

    public void SetPlaying(int playersOnline, int playersMax, long playStartUnixMs)
    {
        _details = "Играет на сервере AquaTech";
        _state = playersOnline is > 0
            ? $"{playersOnline}/{(playersMax is > 0 ? playersMax : 50)} игроков онлайн"
            : "Океанский Skyblock 1.20.1";
        _playStartUnixMs = playStartUnixMs > 0 ? playStartUnixMs : DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        Wake();
    }

    public void Dispose() => Stop();

    public void Stop()
    {
        try { _cts?.Cancel(); } catch { }
        _cts = null;
        _loop = null;
    }

    private void Wake() => Start();

    private async Task LoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            if (!Enabled || string.IsNullOrWhiteSpace(ClientId))
            {
                Stop();
                return;
            }
            try
            {
                using var pipe = await ConnectAsync(ct);
                if (pipe == null)
                {
                    await Task.Delay(15000, ct);
                    continue;
                }

                await HandshakeAsync(pipe, ct);
                await SendActivityAsync(pipe, ct); // apply current state on READY
                var lastSent = DateTimeOffset.UtcNow;

                while (!ct.IsCancellationRequested)
                {
                    // Re-apply periodically (Discord refreshes elapsed time client-side,
                    // but we resend when the server status changes or every 15 min).
                    var due = lastSent + TimeSpan.FromMinutes(15);
                    var delay = due - DateTimeOffset.UtcNow;
                    if (delay > TimeSpan.FromSeconds(1)) await Task.Delay(delay, ct);
                    await SendActivityAsync(pipe, ct);
                    lastSent = DateTimeOffset.UtcNow;
                }
            }
            catch (OperationCanceledException)
            {
                return;
            }
            catch
            {
                try { await Task.Delay(15000, ct); } catch { return; }
            }
        }
    }

    private static async Task<NamedPipeClientStream?> ConnectAsync(CancellationToken ct)
    {
        for (var i = 0; i < 10; i++)
        {
            try
            {
                var pipe = new NamedPipeClientStream(".", $"discord-ipc-{i}", PipeDirection.InOut);
                await pipe.ConnectAsync(800, ct);
                return pipe;
            }
            catch
            {
                /* try next slot */
            }
        }
        return null;
    }

    private async Task HandshakeAsync(Stream pipe, CancellationToken ct)
    {
        var payload = JsonSerializer.Serialize(new
        {
            v = 1,
            client_id = ClientId,
        });
        await WriteFrameAsync(pipe, 0, payload, ct);

        // Wait for READY (op 1, event "READY"); discard anything else.
        var deadline = DateTimeOffset.UtcNow + TimeSpan.FromSeconds(5);
        while (DateTimeOffset.UtcNow < deadline)
        {
            var (op, data) = await ReadFrameAsync(pipe, ct);
            if (op < 0) throw new IOException("discord pipe closed");
            if (op == 1 && data.Contains("READY")) return;
        }
    }

    private async Task SendActivityAsync(Stream pipe, CancellationToken ct)
    {
        if (string.IsNullOrWhiteSpace(_details)) return;
        var activity = new Dictionary<string, object?>
        {
            ["details"] = _details,
            ["state"] = _state,
            ["assets"] = new Dictionary<string, object?>
            {
                ["large_image"] = "aquatech",
                ["large_text"] = "AquaTech",
            },
            ["instance"] = true,
        };
        if (_playStartUnixMs > 0)
        {
            activity["timestamps"] = new Dictionary<string, object?> { ["start"] = _playStartUnixMs };
        }
        var payload = JsonSerializer.Serialize(new
        {
            cmd = "SET_ACTIVITY",
            args = new Dictionary<string, object?>
            {
                ["pid"] = Environment.ProcessId,
                ["activity"] = activity,
            },
            nonce = Guid.NewGuid().ToString(),
        });
        await WriteFrameAsync(pipe, 1, payload, ct);
    }

    private static async Task WriteFrameAsync(Stream stream, int op, string json, CancellationToken ct)
    {
        var payload = Encoding.UTF8.GetBytes(json);
        var frame = new byte[8 + payload.Length];
        WriteInt32(frame, 0, op);
        WriteInt32(frame, 4, payload.Length);
        Buffer.BlockCopy(payload, 0, frame, 8, payload.Length);
        await stream.WriteAsync(frame, ct);
        await stream.FlushAsync(ct);
    }

    private static void WriteInt32(byte[] buffer, int offset, int value)
    {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
        buffer[offset + 2] = (byte)(value >> 16);
        buffer[offset + 3] = (byte)(value >> 24);
    }

    private static async Task<(int Op, string Data)> ReadFrameAsync(Stream stream, CancellationToken ct)
    {
        var header = new byte[8];
        await ReadExactAsync(stream, header, ct);
        var op = header[0] | (header[1] << 8) | (header[2] << 16) | (header[3] << 24);
        var len = header[4] | (header[5] << 8) | (header[6] << 16) | (header[7] << 24);
        if (len is < 0 or > 512 * 1024) throw new IOException("discord frame too large");
        var payload = new byte[len];
        await ReadExactAsync(stream, payload, ct);
        return (op, Encoding.UTF8.GetString(payload));
    }

    private static async Task ReadExactAsync(Stream stream, byte[] buffer, CancellationToken ct)
    {
        var offset = 0;
        while (offset < buffer.Length)
        {
            var read = await stream.ReadAsync(buffer.AsMemory(offset, buffer.Length - offset), ct);
            if (read <= 0) throw new IOException("discord pipe closed");
            offset += read;
        }
    }
}
