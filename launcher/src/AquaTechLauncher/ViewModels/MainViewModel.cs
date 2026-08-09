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
    private CancellationTokenSource? _pingCts;

    public MainViewModel()
    {
        Username = _cfg.Username;
        RamText = $"{_cfg.RamMb} MB";
        GameDir = _cfg.GameDir;
        VersionLabel = $"v{LauncherConstants.Version}";
        McLabel = $"Minecraft {LauncherConstants.McVersion}";
        ServerAddress = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        OnlinePlayersText = "Онлайн: …";
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
    [ObservableProperty] private string _updateButtonText = "Обновить";
    [ObservableProperty] private bool _actionsEnabled = true;
    [ObservableProperty] private string _serverStatus = "Проверяем сервер…";
    [ObservableProperty] private IBrush _serverDot = Brush("#5A7080");
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
    private async Task UpdateAsync()
    {
        if (_busy) return;
        SaveCfgFromUi();
        SetBusy(true, "Обновление…");
        Page = "log";
        try
        {
            await Task.Run(() => _orch.UpdateAsync(_cfg, UiLog, UiProgress));
            UpdateButtonText = "Обновить";
            PlayButtonText = "Играть";
            ActionsEnabled = true;
        }
        catch (Exception ex)
        {
            UiLog($"Ошибка обновления: {ex.Message}", "err");
            UpdateButtonText = "Ошибка обновления";
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
        if (busy)
        {
            PlayButtonText = playText ?? "Подготовка…";
            UpdateButtonText = "Обновить";
        }
        else
        {
            PlayButtonText = "Играть";
            UpdateButtonText = "Обновить";
        }
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

    private async Task RefreshPingLoopAsync()
    {
        _pingCts = new CancellationTokenSource();
        var ct = _pingCts.Token;
        while (!ct.IsCancellationRequested)
        {
            try
            {
                var (online, ms) = await ServerPing.PingAsync(_cfg.EffectiveHost, _cfg.EffectivePort);
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    if (online)
                    {
                        ServerStatus = ms is null ? "Онлайн" : $"Онлайн · {ms} мс";
                        ServerDot = Brush("#34D399");
                    }
                    else
                    {
                        ServerStatus = "Недоступен";
                        ServerDot = Brush("#FB7185");
                    }
                });
            }
            catch
            {
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    ServerStatus = "Недоступен";
                    ServerDot = Brush("#FB7185");
                });
            }

            try { await Task.Delay(45000, ct); }
            catch (OperationCanceledException) { break; }
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
                    if (status == null)
                        OnlinePlayersText = "Онлайн: —";
                    else if (!status.Online)
                        OnlinePlayersText = "Сервер оффлайн";
                    else
                        OnlinePlayersText = status.PlayersMax > 0
                            ? $"Онлайн: {status.PlayersOnline}/{status.PlayersMax}"
                            : $"Онлайн: {status.PlayersOnline}";

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
                    OnlinePlayersText = "Онлайн: —";
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
        "warn" => Brush("#F5C542"),
        "dim" => Brush("#6A8496"),
        _ => Brush("#2DE2E6"),
    };
}

public sealed class LogLine(string time, string text, IBrush color)
{
    public string Time { get; } = time;
    public string Text { get; } = text;
    public IBrush Color { get; } = color;
}
