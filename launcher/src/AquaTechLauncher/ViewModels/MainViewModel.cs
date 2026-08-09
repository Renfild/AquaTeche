using System.Collections.ObjectModel;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Media;
using Avalonia.Platform.Storage;
using Avalonia.Threading;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using AquaTechLauncher.Core;

namespace AquaTechLauncher.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    private readonly PlayOrchestrator _orch = new();
    private LauncherConfig _cfg = LauncherConfig.Load();
    private bool _busy;
    private bool? _tcpOnline;
    private int? _tcpMs;
    private int? _portalPlayers;

    public MainViewModel()
    {
        Username = _cfg.Username;
        RamText = $"{_cfg.RamMb} MB";
        GameDir = _cfg.GameDir;
        VersionLabel = $"v{LauncherConstants.Version}";
        McLabel = $"Minecraft {LauncherConstants.McVersion}";
        ServerAddress = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        OnlinePlayersText = "Проверяем…";
        TopPlayersText = "Топ загружается…";
        _ = StartupAsync();
    }

    [ObservableProperty] private string _page = "play";
    [ObservableProperty] private string _username = "";
    [ObservableProperty] private string _ramText = "4096 MB";
    [ObservableProperty] private string _gameDir = "";
    [ObservableProperty] private string _statusText = "Готов к запуску";
    [ObservableProperty] private string _pctText = "0%";
    [ObservableProperty] private double _progress;
    [ObservableProperty] private string _playButtonText = "Играть";
    [ObservableProperty] private bool _actionsEnabled = true;
    [ObservableProperty] private string _serverStatus = "Проверяем сервер…";
    [ObservableProperty] private IBrush _serverDot = Brush("#A78BFA");
    [ObservableProperty] private string _serverAddress = "";
    [ObservableProperty] private string _versionLabel = "";
    [ObservableProperty] private string _mcLabel = "";
    [ObservableProperty] private string _onlinePlayersText = "";
    [ObservableProperty] private string _topPlayersText = "";

    public ObservableCollection<LogLine> LogLines { get; } = [];

    public bool IsPlayPage => Page == "play";
    public bool IsSettingsPage => Page == "settings";
    public bool IsLogPage => Page == "log";

    partial void OnPageChanged(string value)
    {
        OnPropertyChanged(nameof(IsPlayPage));
        OnPropertyChanged(nameof(IsSettingsPage));
        OnPropertyChanged(nameof(IsLogPage));
    }

    [RelayCommand] private void ShowPlay() => Page = "play";
    [RelayCommand] private void ShowSettings() => Page = "settings";
    [RelayCommand] private void ShowLog() => Page = "log";

    [RelayCommand]
    private async Task PlayAsync()
    {
        if (_busy) return;
        SaveCfgFromUi();
        if (string.IsNullOrWhiteSpace(_cfg.Username))
        {
            StatusText = "Введи никнейм";
            Page = "play";
            return;
        }

        SetBusy(true);
        Page = "log";
        try
        {
            await Task.Run(() => _orch.PlayAsync(_cfg, UiLog, UiProgress));
            PlayButtonText = "В игре";
            ActionsEnabled = true;
            MinimizeMainWindow();
        }
        catch (Exception ex)
        {
            UiLog($"Критическая ошибка: {ex.Message}", "err");
            PlayButtonText = "Ошибка — ещё раз";
            ActionsEnabled = true;
        }
        finally
        {
            _busy = false;
        }
    }

    [RelayCommand]
    private async Task CopyIpAsync()
    {
        var ip = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop
            && desktop.MainWindow?.Clipboard is { } clip)
        {
            await clip.SetTextAsync(ip);
            StatusText = "IP скопирован";
        }
    }

    [RelayCommand]
    private async Task BrowseGameDirAsync()
    {
        if (Application.Current?.ApplicationLifetime is not IClassicDesktopStyleApplicationLifetime desktop)
            return;
        var win = desktop.MainWindow;
        if (win == null) return;
        var folders = await win.StorageProvider.OpenFolderPickerAsync(new FolderPickerOpenOptions
        {
            Title = "Папка игры AquaTech",
            AllowMultiple = false,
        });
        if (folders.Count > 0 && folders[0].TryGetLocalPath() is { } path)
            GameDir = path;
    }

    private async Task StartupAsync()
    {
        _ = RefreshPingLoopAsync();
        _ = RefreshPortalStatsLoopAsync();
        try
        {
            UiLog("Проверяем обновление лаунчера…", "dim");
            var (updated, msg) = await LauncherSelfUpdate.CheckAndApplyAsync(m => UiLog(m, "info"), UiProgress);
            if (updated)
            {
                UiLog($"Перезапуск на v{msg}…", "ok");
                Dispatcher.UIThread.Post(() =>
                {
                    if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
                        desktop.Shutdown();
                });
                return;
            }
            if (!string.IsNullOrWhiteSpace(msg))
                UiLog(msg, "dim");
            StatusText = "Готов к запуску";
            Progress = 0;
            PctText = "0%";
        }
        catch (Exception ex)
        {
            UiLog($"Автообновление: {ex.Message}", "warn");
        }
    }

    private void SetBusy(bool busy, string? playText = null)
    {
        _busy = busy;
        ActionsEnabled = !busy;
        PlayButtonText = busy ? (playText ?? "Подготовка…") : "Играть";
    }

    private void SaveCfgFromUi()
    {
        _cfg.Username = Username.Trim();
        _cfg.GameDir = string.IsNullOrWhiteSpace(GameDir) ? LauncherConstants.GameDirDefault : GameDir.Trim();
        _cfg.UpdateUrl = LauncherConstants.DefaultUpdateUrl;
        var ramRaw = new string(RamText.Where(char.IsDigit).ToArray());
        _cfg.RamMb = int.TryParse(ramRaw, out var ram) && ram >= 1024 ? ram : 4096;
        RamText = $"{_cfg.RamMb} MB";
        GameDir = _cfg.GameDir;
        _cfg.Save();
    }

    private void UiLog(string text, string tag)
    {
        Dispatcher.UIThread.Post(() =>
        {
            LogLines.Add(new LogLine(DateTime.Now.ToString("HH:mm:ss"), text, TagBrush(tag)));
            while (LogLines.Count > 500) LogLines.RemoveAt(0);
            StatusText = text.Length > 90 ? text[..90] : text;
        });
    }

    private void UiProgress(double pct)
    {
        Dispatcher.UIThread.Post(() =>
        {
            Progress = Math.Clamp(pct, 0, 100);
            PctText = $"{(int)Progress}%";
        });
    }

    private void ApplyUnifiedStatus()
    {
        // TCP ping is the truth for "can we reach the server" (Playit tunnels often
        // fail Minecraft query APIs while TCP connect still works).
        if (_tcpOnline == true)
        {
            ServerStatus = _tcpMs is null ? "Онлайн" : $"Онлайн · {_tcpMs} мс";
            ServerDot = Brush("#34D399");
            OnlinePlayersText = _portalPlayers is > 0
                ? $"Онлайн · {_portalPlayers} игр."
                : (_tcpMs is null ? "Сервер онлайн" : $"Онлайн · {_tcpMs} мс");
        }
        else if (_tcpOnline == false)
        {
            ServerStatus = "Недоступен";
            ServerDot = Brush("#FB7185");
            OnlinePlayersText = "Сервер оффлайн";
        }
        else
        {
            ServerStatus = "Проверяем сервер…";
            ServerDot = Brush("#A78BFA");
            OnlinePlayersText = "Проверяем…";
        }
    }

    private async Task RefreshPingLoopAsync()
    {
        var ct = CancellationToken.None;
        while (true)
        {
            try
            {
                var (online, ms) = await ServerPing.PingAsync(_cfg.EffectiveHost, _cfg.EffectivePort);
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    _tcpOnline = online;
                    _tcpMs = ms;
                    ApplyUnifiedStatus();
                });
            }
            catch
            {
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    _tcpOnline = false;
                    _tcpMs = null;
                    ApplyUnifiedStatus();
                });
            }

            try { await Task.Delay(20000, ct); }
            catch { break; }
        }
    }

    private async Task RefreshPortalStatsLoopAsync()
    {
        while (true)
        {
            try
            {
                var statusTask = PortalApi.FetchServerStatusAsync();
                var topTask = PortalApi.FetchTopPlayersAsync("likes", 5);
                await Task.WhenAll(statusTask, topTask);
                var status = statusTask.Result;
                var top = topTask.Result;

                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    _portalPlayers = status is { Online: true } ? status.PlayersOnline : null;
                    ApplyUnifiedStatus();

                    if (top.Count == 0)
                        TopPlayersText = "Топ: пока нет данных с портала.";
                    else
                    {
                        var bits = top.Take(5).Select(p => $"{p.Nick} ({p.Likes}❤)");
                        TopPlayersText = "Топ: " + string.Join(" · ", bits);
                    }
                });
            }
            catch
            {
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    TopPlayersText = "Топ временно недоступен.";
                });
            }

            try { await Task.Delay(60000); }
            catch { break; }
        }
    }

    private static void MinimizeMainWindow()
    {
        Dispatcher.UIThread.Post(() =>
        {
            if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop
                && desktop.MainWindow is { } w)
            {
                w.WindowState = WindowState.Minimized;
            }
        }, DispatcherPriority.Background);
    }

    private static IBrush Brush(string hex) => new SolidColorBrush(Color.Parse(hex));

    private static IBrush TagBrush(string tag) => tag switch
    {
        "ok" => Brush("#34D399"),
        "err" => Brush("#FB7185"),
        "warn" => Brush("#F9A8D4"),
        "dim" => Brush("#C4B5FD"),
        _ => Brush("#E879F9"),
    };
}

public sealed class LogLine(string time, string text, IBrush color)
{
    public string Time { get; } = time;
    public string Text { get; } = text;
    public IBrush Color { get; } = color;
}
