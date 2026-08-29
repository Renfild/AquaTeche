using AquaTechLauncher.Core;
using Xunit;

public class PortalCallbackListenerTests
{
    [Fact]
    public void ParsesStandardCallback()
    {
        var (session, nick) = PortalCallbackListener.ParseCallbackUrl(
            "http://127.0.0.1:12450/api/portal_callback?session=abc123DEF&nick=Renfil");
        Assert.Equal("abc123DEF", session);
        Assert.Equal("Renfil", nick);
    }

    [Fact]
    public void ParsesEncodedNick()
    {
        var (session, nick) = PortalCallbackListener.ParseCallbackUrl(
            "http://127.0.0.1:12450/api/portal_callback?session=s%2Bess&nick=White%20Wolf");
        Assert.Equal("s+ess", session);
        Assert.Equal("White Wolf", nick);
    }

    [Fact]
    public void RejectsForeignPaths()
    {
        var (session, _) = PortalCallbackListener.ParseCallbackUrl(
            "http://127.0.0.1:12450/api/portal_callback_evil?session=x");
        Assert.Null(session);

        var (session2, _) = PortalCallbackListener.ParseCallbackUrl("http://127.0.0.1:12450/");
        Assert.Null(session2);
    }

    [Fact]
    public void RejectsMissingSession()
    {
        var (session, nick) = PortalCallbackListener.ParseCallbackUrl(
            "http://127.0.0.1:12450/api/portal_callback?nick=Renfil");
        Assert.Null(session);
        Assert.Equal("Renfil", nick);
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("not a url")]
    public void RejectsGarbage(string? url)
    {
        var (session, _) = PortalCallbackListener.ParseCallbackUrl(url);
        Assert.Null(session);
    }

    [Fact]
    public async Task ListenerReceivesCallbackOverTcp()
    {
        using var listener = new PortalCallbackListener();
        var port = 15731;
        Assert.True(listener.Start(port));

        var received = new TaskCompletionSource<(string?, string?)>(TaskCreationOptions.RunContinuationsAsynchronously);
        listener.CallbackReceived += (s, n) => received.TrySetResult((s, n));

        using var client = new System.Net.Sockets.TcpClient();
        await client.ConnectAsync(System.Net.IPAddress.Loopback, port);
        using var stream = client.GetStream();
        var request = System.Text.Encoding.ASCII.GetBytes(
            "GET /api/portal_callback?session=tok42&nick=Steve HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n");
        await stream.WriteAsync(request);
        var response = new byte[4096];
        var read = await stream.ReadAsync(response);

        var (session, nick) = await received.Task.WaitAsync(TimeSpan.FromSeconds(5));
        Assert.Equal("tok42", session);
        Assert.Equal("Steve", nick);
        Assert.Contains("200 OK", System.Text.Encoding.UTF8.GetString(response, 0, read));
    }
}
