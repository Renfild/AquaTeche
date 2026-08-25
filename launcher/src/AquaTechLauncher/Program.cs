using Avalonia;
using System;
using System.IO;
using System.Runtime.InteropServices;

namespace AquaTechLauncher;

sealed class Program
{
    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern bool FreeConsole();

    [STAThread]
    public static void Main(string[] args)
    {
        if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
        {
            try { FreeConsole(); } catch { /* ignore */ }
        }

        var logDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "AquaTech");
        Directory.CreateDirectory(logDir);
        var crashLog = Path.Combine(logDir, "launcher_crash.log");

        try
        {
            AppDomain.CurrentDomain.UnhandledException += (s, e) =>
            {
                File.AppendAllText(crashLog, $"[{DateTime.Now}] UNHANDLED: {e.ExceptionObject}\n");
            };

            BuildAvaloniaApp().StartWithClassicDesktopLifetime(args);
        }
        catch (Exception ex)
        {
            File.AppendAllText(crashLog, $"[{DateTime.Now}] MAIN EXCEPTION: {ex}\n");
        }
    }

    public static AppBuilder BuildAvaloniaApp()
        => AppBuilder.Configure<App>()
            .UsePlatformDetect()
#if DEBUG
            .WithDeveloperTools()
#endif
            .LogToTrace();
}
