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
        UpdateUrl = _cfg.UpdateUrl;
        ServerChip = LauncherConstants.ServerHost;
        VersionLabel = $"v{LauncherConstants.Version}";
        McLabel = $"Minecraft {LauncherConstants.McVersion}";
        ServerAddress = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        _ = RefreshPingLoopAsync();
    }

    [ObservableProperty] private string _page = "play";
    [ObservableProperty] private string _username = "";
    [ObservableProperty] private string _ramText = "4096 MB";
    [ObservableProperty] private string _gameDir = "";
    [ObservableProperty] private string _updateUrl = "";
    [ObservableProperty] private string _statusText = "Готов к запуску";
    [ObservableProperty] private string _pctText = "0%";
    [ObservableProperty] private double _progress;
    [ObservableProperty] private string _playButtonText = "Играть";
    [ObservableProperty] private string _updateButtonText = "Обновить";
    [ObservableProperty] private bool _actionsEnabled = true;
    [ObservableProperty] private IBrush _playButtonBg = Brush("#2A9AAB");
    [ObservableProperty] private string _serverStatus = "Проверяем сервер…";
    [ObservableProperty] private IBrush _serverDot = Brush("#5A7080");
    [ObservableProperty] private string _serverChip = "";
    [ObservableProperty] private string _serverAddress = "";
    [ObservableProperty] private string _versionLabel = "";
    [ObservableProperty] private string _mcLabel = "";

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

    [RelayCommand]
    private void ShowPlay() => Page = "play";

    [RelayCommand]
    private void ShowSettings() => Page = "settings";

    [RelayCommand]
    private void ShowLog() => Page = "log";

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
            PlayButtonBg = Brush("#E06B6B");
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
            PlayButtonBg = Brush("#2A9AAB");
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
            PlayButtonBg = Brush("#2A9AAB");
            UpdateButtonText = "Обновить";
        }
    }

    private void SaveCfgFromUi()
    {
        _cfg.Username = Username.Trim();
        _cfg.GameDir = string.IsNullOrWhiteSpace(GameDir) ? LauncherConstants.GameDirDefault : GameDir.Trim();
        _cfg.UpdateUrl = LauncherConfig.NormalizeUpdateUrl(UpdateUrl);
        var ramRaw = new string(RamText.Where(char.IsDigit).ToArray());
        _cfg.RamMb = int.TryParse(ramRaw, out var ram) && ram >= 1024 ? ram : 4096;
        RamText = $"{_cfg.RamMb} MB";
        UpdateUrl = _cfg.UpdateUrl;
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
                        ServerDot = Brush("#3DDB8A");
                    }
                    else
                    {
                        ServerStatus = "Недоступен";
                        ServerDot = Brush("#E06B6B");
                    }
                });
            }
            catch
            {
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    ServerStatus = "Недоступен";
                    ServerDot = Brush("#E06B6B");
                });
            }

            try { await Task.Delay(45000, ct); }
            catch (OperationCanceledException) { break; }
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
        "ok" => Brush("#5ED9A0"),
        "err" => Brush("#E06B6B"),
        "warn" => Brush("#D4A35C"),
        "dim" => Brush("#5A7080"),
        _ => Brush("#3DB8C5"),
    };
}

public sealed class LogLine(string time, string text, IBrush color)
{
    public string Time { get; } = time;
    public string Text { get; } = text;
    public IBrush Color { get; } = color;
}
