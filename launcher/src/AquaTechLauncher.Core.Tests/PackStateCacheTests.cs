using AquaTechLauncher.Core;
using Xunit;

namespace AquaTechLauncher.Core.Tests;

public class PackStateCacheTests
{
    private static string TempDir()
    {
        var dir = Path.Combine(Path.GetTempPath(), "at-cache-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(dir);
        return dir;
    }

    [Fact]
    public void Stored_md5_is_valid_while_file_untouched()
    {
        var dir = TempDir();
        try
        {
            var file = Path.Combine(dir, "mods", "test.jar");
            Directory.CreateDirectory(Path.GetDirectoryName(file)!);
            File.WriteAllBytes(file, new byte[] { 1, 2, 3 });

            var cache = new PackStateCache();
            cache.Store(file, "mods/test.jar", "d41d8cd98f00b204e9800998ecf8427e");

            Assert.Equal("d41d8cd98f00b204e9800998ecf8427e", cache.GetValidMd5(file, "mods/test.jar"));
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Cache_invalidated_when_file_modified()
    {
        var dir = TempDir();
        try
        {
            var file = Path.Combine(dir, "mods", "test.jar");
            Directory.CreateDirectory(Path.GetDirectoryName(file)!);
            File.WriteAllBytes(file, new byte[] { 1 });
            var cache = new PackStateCache();
            cache.Store(file, "mods/test.jar", "aaa");

            Thread.Sleep(10);
            File.WriteAllBytes(file, new byte[] { 1, 2, 3, 4 });

            Assert.Null(cache.GetValidMd5(file, "mods/test.jar"));
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Cache_invalidated_when_mtime_changes_same_size()
    {
        var dir = TempDir();
        try
        {
            var file = Path.Combine(dir, "config", "opts.txt");
            Directory.CreateDirectory(Path.GetDirectoryName(file)!);
            File.WriteAllBytes(file, new byte[] { 9, 9 });
            var cache = new PackStateCache();
            cache.Store(file, "config/opts.txt", "bbb");

            File.WriteAllBytes(file, new byte[] { 8, 8 });
            File.SetLastWriteTimeUtc(file, DateTime.UtcNow.AddMinutes(5));

            Assert.Null(cache.GetValidMd5(file, "config/opts.txt"));
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Roundtrip_through_disk_preserves_entries()
    {
        var dir = TempDir();
        try
        {
            var file = Path.Combine(dir, "kubejs", "script.js");
            Directory.CreateDirectory(Path.GetDirectoryName(file)!);
            File.WriteAllBytes(file, new byte[] { 7 });

            var first = new PackStateCache { Version = "2.9.242" };
            first.Store(file, "kubejs/script.js", "ccc");
            first.Save(dir);

            var loaded = PackStateCache.Load(dir);
            Assert.Equal("2.9.242", loaded.Version);
            Assert.Equal("ccc", loaded.GetValidMd5(file, "kubejs/script.js"));
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void RetainWanted_drops_stale_entries()
    {
        var dir = TempDir();
        try
        {
            var keep = Path.Combine(dir, "mods", "keep.jar");
            var drop = Path.Combine(dir, "mods", "drop.jar");
            foreach (var f in new[] { keep, drop })
            {
                Directory.CreateDirectory(Path.GetDirectoryName(f)!);
                File.WriteAllBytes(f, new byte[] { 1 });
            }

            var cache = new PackStateCache();
            cache.Store(keep, "mods/keep.jar", "k1");
            cache.Store(drop, "mods/drop.jar", "d1");
            cache.RetainWanted(new[] { "mods/keep.jar" });

            Assert.NotNull(cache.GetValidMd5(keep, "mods/keep.jar"));
            Assert.Null(cache.GetValidMd5(drop, "mods/drop.jar"));
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }

    [Fact]
    public void Load_missing_or_corrupt_returns_empty_cache()
    {
        var dir = TempDir();
        try
        {
            var empty = PackStateCache.Load(dir);
            Assert.Null(empty.Version);
            Assert.False(empty.HasEntries);

            File.WriteAllText(Path.Combine(dir, ".pack_state.json"), "{not json!!");
            var broken = PackStateCache.Load(dir);
            Assert.False(broken.HasEntries);
        }
        finally
        {
            try { Directory.Delete(dir, true); } catch { /* ignore */ }
        }
    }
}
