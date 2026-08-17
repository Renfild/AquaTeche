using System.Net;
using System.Net.Http;
using System.Text;
using AquaTechLauncher.Core;
using Xunit;

namespace AquaTechLauncher.Core.Tests;

public class ManifestNeedsDownloadTests
{
    [Fact]
    public void Missing_file_needs_download()
    {
        var item = new PackFileEntry { Path = "mods/a.jar", Size = 10, Md5 = "abc" };
        Assert.True(ManifestSync.NeedsDownload(Path.Combine(Path.GetTempPath(), Guid.NewGuid() + ".jar"), item, true));
    }

    [Fact]
    public void Matching_size_and_md5_skips()
    {
        var tmp = Path.Combine(Path.GetTempPath(), "at-test-" + Guid.NewGuid().ToString("N") + ".bin");
        try
        {
            var bytes = Encoding.UTF8.GetBytes("hello-aquatech");
            File.WriteAllBytes(tmp, bytes);
            var md5 = Convert.ToHexString(System.Security.Cryptography.MD5.HashData(bytes)).ToLowerInvariant();
            var item = new PackFileEntry { Path = "x", Size = bytes.Length, Md5 = md5 };
            Assert.False(ManifestSync.NeedsDownload(tmp, item, verifyHash: true));
            Assert.False(ManifestSync.NeedsDownload(tmp, item, verifyHash: false));
        }
        finally
        {
            try { File.Delete(tmp); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Wrong_md5_needs_download_when_verifying()
    {
        var tmp = Path.Combine(Path.GetTempPath(), "at-test-" + Guid.NewGuid().ToString("N") + ".bin");
        try
        {
            File.WriteAllBytes(tmp, Encoding.UTF8.GetBytes("hello-aquatech"));
            var item = new PackFileEntry { Path = "x", Size = new FileInfo(tmp).Length, Md5 = "00000000000000000000000000000000" };
            Assert.True(ManifestSync.NeedsDownload(tmp, item, verifyHash: true));
            Assert.False(ManifestSync.NeedsDownload(tmp, item, verifyHash: false));
        }
        finally
        {
            try { File.Delete(tmp); } catch { /* ignore */ }
        }
    }
}

public class SessionCookieParseTests
{
    [Fact]
    public void Extracts_at_session_from_set_cookie()
    {
        using var resp = new HttpResponseMessage(HttpStatusCode.OK);
        resp.Headers.TryAddWithoutValidation(
            "Set-Cookie",
            "at_session=abc123def; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=604800");
        var sid = HttpDownload.ExtractSessionFromSetCookie(resp);
        Assert.Equal("abc123def", sid);
    }

    [Fact]
    public void Missing_cookie_returns_null()
    {
        using var resp = new HttpResponseMessage(HttpStatusCode.OK);
        Assert.Null(HttpDownload.ExtractSessionFromSetCookie(resp));
    }
}

public class VersionCompareTests
{
    [Theory]
    [InlineData("2.9.19", "2.9.19", true)]
    [InlineData("2.9.19", "2.9.20", false)]
    [InlineData(" 2.9.20 ", "2.9.20", true)]
    public void VersionsEqual(string a, string b, bool expect) =>
        Assert.Equal(expect, LauncherSelfUpdate.VersionsEqual(a, b));

    [Theory]
    [InlineData("2.9.64", "2.9.59", true)]
    [InlineData("2.9.59", "2.9.64", false)]
    [InlineData("2.9.64", "2.9.64", false)]
    [InlineData("2.9.10", "2.9.9", true)]
    public void VersionNewer(string candidate, string baseline, bool expect) =>
        Assert.Equal(expect, LauncherSelfUpdate.VersionNewer(candidate, baseline));
}

public class ZipVerifyTests
{
    [Fact]
    public void Rejects_size_mismatch()
    {
        var tmp = Path.Combine(Path.GetTempPath(), "at-zip-" + Guid.NewGuid().ToString("N") + ".zip");
        try
        {
            File.WriteAllBytes(tmp, new byte[] { 1, 2, 3, 4 });
            var man = new BootstrapManifest { LauncherZipSize = 99 };
            Assert.False(LauncherSelfUpdate.VerifyZip(tmp, man, out var err));
            Assert.Contains("Размер", err);
        }
        finally
        {
            try { File.Delete(tmp); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Accepts_matching_md5()
    {
        var tmp = Path.Combine(Path.GetTempPath(), "at-zip-" + Guid.NewGuid().ToString("N") + ".zip");
        try
        {
            var bytes = new byte[] { 9, 8, 7 };
            File.WriteAllBytes(tmp, bytes);
            var md5 = Convert.ToHexString(System.Security.Cryptography.MD5.HashData(bytes)).ToLowerInvariant();
            var man = new BootstrapManifest { LauncherZipSize = bytes.Length, LauncherZipMd5 = md5 };
            Assert.True(LauncherSelfUpdate.VerifyZip(tmp, man, out _));
        }
        finally
        {
            try { File.Delete(tmp); } catch { /* ignore */ }
        }
    }
}

public class AutoJoinArgumentTests
{
    [Fact]
    public void Uses_delayed_mod_property_instead_of_quick_play()
    {
        var argument = LaunchCommandBuilder.BuildAutoJoinArgument(" play.aquatech.test:25565 ");

        Assert.Equal("-Daquatech.autoJoin=play.aquatech.test:25565", argument);
        Assert.DoesNotContain("quickPlay", argument, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void Empty_target_disables_auto_join()
    {
        Assert.Null(LaunchCommandBuilder.BuildAutoJoinArgument("  "));
    }
}
