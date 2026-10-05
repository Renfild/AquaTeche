using Xunit;

namespace AquaTechLauncher.Core.Tests;

public class JcefInstallerTests : IDisposable
{
    private readonly string _dir = Path.Combine(Path.GetTempPath(), "jcef-test-" + Guid.NewGuid().ToString("N"));

    public JcefInstallerTests() => Directory.CreateDirectory(_dir);

    public void Dispose()
    {
        try { Directory.Delete(_dir, true); } catch (IOException) { }
    }

    private void PlaceLibraries(string markText)
    {
        var libs = JcefInstaller.LibrariesDir(_dir);
        Directory.CreateDirectory(libs);
        File.WriteAllText(Path.Combine(libs, "libcef.dll"), "x");
        File.WriteAllText(Path.Combine(libs, "jcef.dll"), "x");
        File.WriteAllText(JcefInstaller.ChecksumPath(_dir), markText);
    }

    [Fact]
    public void Not_installed_when_folder_missing()
    {
        Assert.False(JcefInstaller.IsInstalled(_dir));
    }

    [Fact]
    public void Installed_when_libs_and_matching_mark_exist()
    {
        PlaceLibraries("SHA256   " + JcefInstaller.ArchiveSha256.ToLowerInvariant() + "   path");
        Assert.True(JcefInstaller.IsInstalled(_dir));
    }

    [Fact]
    public void Not_installed_when_mark_has_other_hash()
    {
        PlaceLibraries("SHA256   0000000000000000000000000000000000000000000000000000000000000000");
        Assert.False(JcefInstaller.IsInstalled(_dir));
    }

    [Fact]
    public void Not_installed_when_core_library_missing()
    {
        PlaceLibraries(JcefInstaller.ArchiveSha256);
        File.Delete(Path.Combine(JcefInstaller.LibrariesDir(_dir), "libcef.dll"));
        Assert.False(JcefInstaller.IsInstalled(_dir));
    }

    [Fact]
    public void Settings_skip_download_and_point_to_mirror()
    {
        JcefInstaller.WriteSettings(_dir);
        var text = File.ReadAllText(JcefInstaller.SettingsPath(_dir));
        Assert.Contains("skip-download=true", text);
        Assert.Contains("download-mirror=https\\://aquateche.store/mcef", text);
    }

    [Fact]
    public void Archive_with_wrong_content_does_not_match()
    {
        var f = Path.Combine(_dir, "a.tar.gz");
        File.WriteAllText(f, "not the build");
        Assert.False(JcefInstaller.ArchiveMatches(f));
    }

    [Fact]
    public void Missing_archive_does_not_match()
    {
        Assert.False(JcefInstaller.ArchiveMatches(Path.Combine(_dir, "none.tar.gz")));
    }
}
