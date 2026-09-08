using System.Diagnostics;
using System.Runtime.InteropServices;

namespace AquaTechLauncher.Core;

public static class SystemDiagnostics
{
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
    private struct MEMORYSTATUSEX
    {
        public uint dwLength;
        public uint dwMemoryLoad;
        public ulong ullTotalPhys;
        public ulong ullAvailPhys;
        public ulong ullTotalPageFile;
        public ulong ullAvailPageFile;
        public ulong ullTotalVirtual;
        public ulong ullAvailVirtual;
        public ulong ullAvailExtendedVirtual;

        public static MEMORYSTATUSEX Create()
        {
            return new MEMORYSTATUSEX { dwLength = (uint)Marshal.SizeOf(typeof(MEMORYSTATUSEX)) };
        }
    }

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GlobalMemoryStatusEx(ref MEMORYSTATUSEX lpBuffer);

    public record MemoryAssessment(int TotalPhysicalMb, int AvailableMb, int ConfiguredMb, string? Warning, int RecommendedMb);

    public static MemoryAssessment CheckMemory(int configuredRamMb)
    {
        int totalMb = 8192;
        int availMb = 4096;

        try
        {
            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
            {
                var stat = MEMORYSTATUSEX.Create();
                if (GlobalMemoryStatusEx(ref stat))
                {
                    totalMb = (int)(stat.ullTotalPhys / (1024 * 1024));
                    availMb = (int)(stat.ullAvailPhys / (1024 * 1024));
                }
            }
            else
            {
                var memInfo = GC.GetGCMemoryInfo();
                if (memInfo.TotalAvailableMemoryBytes > 0)
                {
                    totalMb = (int)(memInfo.TotalAvailableMemoryBytes / (1024 * 1024));
                    availMb = totalMb / 2;
                }
            }
        }
        catch
        {
            /* fallback to defaults */
        }

        // Recommended memory: around 45% - 60% of physical RAM, capped at 6144 MB for optimal Forge GC
        int recommended = Math.Clamp((int)(totalMb * 0.5), 3584, 6144);
        if (totalMb <= 4096)
        {
            recommended = 3072;
        }

        string? warning = null;
        if (configuredRamMb < 3072)
        {
            warning = $"Выделено мало ОЗУ ({configuredRamMb} МБ). Для модов AquaTech рекомендуется от {recommended} МБ во избежание зависаний.";
        }
        else if (configuredRamMb > (int)(totalMb * 0.88))
        {
            warning = $"Выделено слишком много ОЗУ ({configuredRamMb} МБ из {totalMb} МБ физической памяти). Windows и видеокарта могут испытывать дефицит памяти.";
        }

        return new MemoryAssessment(totalMb, availMb, configuredRamMb, warning, recommended);
    }

    public record CrashAnalysis(string Summary, string Recommendation, string? FileSnippet);

    public static CrashAnalysis AnalyzeCrash(string gameDir, int exitCode)
    {
        var logsDir = Path.Combine(gameDir, "logs");
        var crashReportsDir = Path.Combine(gameDir, "crash-reports");

        string combinedText = "";

        // 1. Check latest crash report
        try
        {
            if (Directory.Exists(crashReportsDir))
            {
                var newestCrash = new DirectoryInfo(crashReportsDir)
                    .GetFiles("crash-*.txt")
                    .OrderByDescending(f => f.LastWriteTimeUtc)
                    .FirstOrDefault();

                if (newestCrash != null && (DateTime.UtcNow - newestCrash.LastWriteTimeUtc).TotalMinutes < 5)
                {
                    combinedText += "\n" + File.ReadAllText(newestCrash.FullName);
                }
            }
        }
        catch { }

        // 2. Read tail of latest.log and minecraft_console.log
        try
        {
            var latestLog = Path.Combine(logsDir, "latest.log");
            if (File.Exists(latestLog))
            {
                var lines = File.ReadLines(latestLog).TakeLast(80);
                combinedText += "\n" + string.Join("\n", lines);
            }

            var consoleLog = Path.Combine(logsDir, "minecraft_console.log");
            if (File.Exists(consoleLog))
            {
                var lines = File.ReadLines(consoleLog).TakeLast(80);
                combinedText += "\n" + string.Join("\n", lines);
            }
        }
        catch { }

        // Analysis Patterns
        if (combinedText.Contains("OutOfMemoryError", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("insufficient memory", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("Could not reserve enough space", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Нехватка оперативной памяти (Java Out of Memory)",
                "Увеличь выделение ОЗУ в настройках лаунчера (рекомендуется 4096–6144 МБ) и закрой ресурсоёмкие вкладки браузера.",
                ExtractSnippet(combinedText, "OutOfMemoryError", 3));
        }

        if (combinedText.Contains("ig75icd64.dll", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("ig9icd64.dll", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("ig4icd64.dll", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Сбой графического драйвера Intel HD Graphics",
                "Устаревший драйвер встроенной видеокарты Intel. Обнови драйвер с официального сайта Intel либо в параметрах Windows назначь запуск игры на дискретную видеокарту (NVIDIA/AMD).",
                "intel_opengl_driver_crash");
        }

        if (combinedText.Contains("atio6axx.dll", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("atig6pxx.dll", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Сбой видеокарты AMD Radeon (OpenGL atio6axx)",
                "Сбой в графической библиотеке AMD. Обнови видеодрайвер через AMD Software: Adrenalin Edition или отключи шейдеры Oculus в настройках графики.",
                "amd_opengl_driver_crash");
        }

        if (combinedText.Contains("nvoglv64.dll", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Сбой видеокарты NVIDIA GeForce (OpenGL nvoglv64)",
                "Сбой видеодрайвера NVIDIA. Обнови драйвер через GeForce Experience или отключи активный шейдерпак.",
                "nvidia_opengl_driver_crash");
        }

        if (combinedText.Contains("GLFW error 65542", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("Pixel format not accelerated", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("The driver does not appear to support OpenGL", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Аппаратный OpenGL не поддерживается драйвером",
                "Установлен базовый драйвер Microsoft без поддержки OpenGL 3.2+. Установи официальные драйверы для твоей видеокарты с сайта NVIDIA, AMD или Intel.",
                "glfw_opengl_unsupported");
        }

        if (combinedText.Contains("ZipException", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("invalid END header", StringComparison.OrdinalIgnoreCase) ||
            combinedText.Contains("CorruptGZIPFileException", StringComparison.OrdinalIgnoreCase))
        {
            return new CrashAnalysis(
                "Повреждён один из jar-файлов модов или библиотек",
                "Файл сборки повреждён при загрузке. Нажми кнопку «Починить сборку» в лаунчере для полной проверки и исправления файлов.",
                ExtractSnippet(combinedText, "ZipException", 3));
        }

        if (exitCode == -1073741819) // 0xC0000005 ACCESS_VIOLATION
        {
            return new CrashAnalysis(
                "Ошибка нарушения доступа (Access Violation 0xC0000005)",
                "Часто вызывается конфликтом сторонних оверлеев (Discord Overlay, RivaTuner Statistics Server, MSI Afterburner) или антивирусом. Попробуй временно закрыть оверлеи.",
                "0xC0000005_access_violation");
        }

        if (exitCode == -1073740791) // 0xC0000409 STATUS_STACK_BUFFER_OVERRUN
        {
            return new CrashAnalysis(
                "Сбой графического конвейера (0xC0000409)",
                "Критический сбой видеобуфера OpenGL (часто вызван несовместимостью мода Oculus с шейдерами на данной видеокарте). Попробуй удалить файл options.txt в папке игры.",
                "0xC0000409_buffer_overrun");
        }

        return new CrashAnalysis(
            $"Minecraft завершился с кодом ошибки {exitCode}",
            "Если ошибка повторяется, нажми «Починить сборку» в лаунчере или отправь лог `logs/latest.log` в поддержку в Discord.",
            null);
    }

    private static string? ExtractSnippet(string text, string pattern, int contextLines)
    {
        try
        {
            var lines = text.Split('\n');
            for (int i = 0; i < lines.Length; i++)
            {
                if (lines[i].Contains(pattern, StringComparison.OrdinalIgnoreCase))
                {
                    int start = Math.Max(0, i - 1);
                    int count = Math.Min(lines.Length - start, contextLines + 2);
                    return string.Join("\n", lines.Skip(start).Take(count)).Trim();
                }
            }
        }
        catch { }
        return null;
    }
}
