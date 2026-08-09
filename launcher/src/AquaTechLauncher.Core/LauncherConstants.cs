namespace AquaTechLauncher.Core;

public static class LauncherConstants
{
    public const string Version = "2.9.17";
    public const string McVersion = "1.20.1";
    public const string ForgeVersion = "47.4.0";
    public const string McpVersion = "20230612.114412";
    public static string ForgeVersionId => $"{McVersion}-forge-{ForgeVersion}";
    public static string ForgeCoord => $"{McVersion}-{ForgeVersion}";

    public const string ServerHost = "katherine-hydro.tun.ply.gg";
    public const int ServerPort = 31279;
    public const int PackReadyMinJars = 40;

    public const string PortalApiBase = "https://aquatech.santcrail.workers.dev";
    public const string BootstrapManifestUrl =
        "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/bootstrap.json";

    public const string DefaultUpdateUrl =
        "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack";

    public static readonly string[] PackCdnMirrors =
    [
        DefaultUpdateUrl,
        "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/pack",
    ];

    public static readonly string[] PackFolders = ["mods", "config", "kubejs", "resourcepacks"];

    public static readonly HashSet<string> SyncKeepNames = new(StringComparer.OrdinalIgnoreCase)
    {
        "options.txt", "optionsof.txt", "servers.dat", "usercache.json",
        "usernamecache.json", "hotbar.nbt", "realms_persistence.json",
    };

    public static readonly string[] ForgeLangProviders =
        ["fmlcore", "javafmllanguage", "lowcodelanguage", "mclanguage"];

    public const string JavaUrl =
        "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse";

    public static readonly string[] AssetMirrors =
    [
        "https://bmclapi2.bangbang93.com/assets",
        "https://resources.download.minecraft.net",
    ];

    public static string GameDirDefault =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "AquaTech");

    public static string ConfigPath =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".aquatech_launcher.json");

    public static string JavaDir(string gameDir) => Path.Combine(gameDir, "_java17");
}
