using System.Diagnostics;
using System.Net.Sockets;

namespace AquaTechLauncher.Core;

public static class ServerPing
{
    public static async Task<(bool Online, int? Ms)> PingAsync(string host, int port, int timeoutMs = 2500)
    {
        var sw = Stopwatch.StartNew();
        try
        {
            using var cts = new CancellationTokenSource(timeoutMs);
            using var client = new TcpClient();
            await client.ConnectAsync(host, port, cts.Token);
            sw.Stop();
            return (true, (int)sw.ElapsedMilliseconds);
        }
        catch
        {
            return (false, null);
        }
    }
}
