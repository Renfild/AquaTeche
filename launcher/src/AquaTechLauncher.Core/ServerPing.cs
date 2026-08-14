using System.Diagnostics;
using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;

namespace AquaTechLauncher.Core;

public sealed record ServerStatusResult(
    bool Online,
    int? LatencyMs,
    int OnlinePlayers,
    int MaxPlayers,
    string? Motd,
    string? VersionName);

public static class ServerPing
{
    public static async Task<(bool Online, int? Ms)> PingAsync(string host, int port, int timeoutMs = 2500)
    {
        var res = await QueryStatusAsync(host, port, timeoutMs);
        return (res.Online, res.LatencyMs);
    }

    public static async Task<ServerStatusResult> QueryStatusAsync(string host, int port, int timeoutMs = 3000)
    {
        var sw = Stopwatch.StartNew();
        try
        {
            using var cts = new CancellationTokenSource(timeoutMs);
            using var client = new TcpClient();
            await client.ConnectAsync(host, port, cts.Token);
            var stream = client.GetStream();

            // 1. Send Handshake Packet (ID 0x00, protocol 763, host, port, state 1)
            using var handshakeStream = new MemoryStream();
            WriteVarInt(handshakeStream, 0x00); // Packet ID
            WriteVarInt(handshakeStream, 763);  // Protocol Version (1.20.1)
            WriteString(handshakeStream, host); // Server Address
            WriteUShort(handshakeStream, (ushort)port); // Server Port
            WriteVarInt(handshakeStream, 1);    // Next State: Status (1)

            var handshakeBytes = handshakeStream.ToArray();
            WriteVarInt(stream, handshakeBytes.Length);
            await stream.WriteAsync(handshakeBytes, cts.Token);

            // 2. Send Status Request Packet (ID 0x00, empty)
            using var statusReqStream = new MemoryStream();
            WriteVarInt(statusReqStream, 0x00);
            var statusReqBytes = statusReqStream.ToArray();
            WriteVarInt(stream, statusReqBytes.Length);
            await stream.WriteAsync(statusReqBytes, cts.Token);
            await stream.FlushAsync(cts.Token);

            // 3. Read Status Response
            var packetLength = ReadVarInt(stream);
            var packetId = ReadVarInt(stream);
            if (packetId != 0x00)
                throw new InvalidDataException($"Unexpected packet ID: {packetId}");

            var jsonLength = ReadVarInt(stream);
            var buffer = new byte[jsonLength];
            var totalRead = 0;
            while (totalRead < jsonLength)
            {
                var read = await stream.ReadAsync(buffer.AsMemory(totalRead, jsonLength - totalRead), cts.Token);
                if (read == 0) break;
                totalRead += read;
            }

            sw.Stop();
            var json = Encoding.UTF8.GetString(buffer, 0, totalRead);
            var node = JsonNode.Parse(json);
            var onlinePlayers = node?["players"]?["online"]?.GetValue<int>() ?? 0;
            var maxPlayers = node?["players"]?["max"]?.GetValue<int>() ?? 0;
            var versionName = node?["version"]?["name"]?.GetValue<string>();
            var motd = node?["description"]?.ToString();

            return new ServerStatusResult(
                Online: true,
                LatencyMs: (int)sw.ElapsedMilliseconds,
                OnlinePlayers: onlinePlayers,
                MaxPlayers: maxPlayers,
                Motd: motd,
                VersionName: versionName);
        }
        catch
        {
            // Fallback simple TCP ping
            try
            {
                sw.Restart();
                using var cts2 = new CancellationTokenSource(1500);
                using var client2 = new TcpClient();
                await client2.ConnectAsync(host, port, cts2.Token);
                sw.Stop();
                return new ServerStatusResult(true, (int)sw.ElapsedMilliseconds, 0, 0, null, null);
            }
            catch
            {
                return new ServerStatusResult(false, null, 0, 0, null, null);
            }
        }
    }

    private static void WriteVarInt(Stream stream, int value)
    {
        while ((value & -128) != 0)
        {
            stream.WriteByte((byte)(value & 127 | 128));
            value = (int)((uint)value >> 7);
        }
        stream.WriteByte((byte)value);
    }

    private static int ReadVarInt(Stream stream)
    {
        var numRead = 0;
        var result = 0;
        int read;
        do
        {
            read = stream.ReadByte();
            if (read == -1) throw new EndOfStreamException();
            var value = read & 0b01111111;
            result |= value << (7 * numRead);
            numRead++;
            if (numRead > 5) throw new InvalidOperationException("VarInt is too big");
        } while ((read & 0b10000000) != 0);

        return result;
    }

    private static void WriteString(Stream stream, string value)
    {
        var bytes = Encoding.UTF8.GetBytes(value);
        WriteVarInt(stream, bytes.Length);
        stream.Write(bytes, 0, bytes.Length);
    }

    private static void WriteUShort(Stream stream, ushort value)
    {
        stream.WriteByte((byte)((value >> 8) & 0xFF));
        stream.WriteByte((byte)(value & 0xFF));
    }
}
