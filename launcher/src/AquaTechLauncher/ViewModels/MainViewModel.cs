using System.Collections.ObjectModel;
using System.Diagnostics;
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
    private int? _onlinePlayers;
    private int? _maxPlayers;

    public MainViewModel()
    {
        Username = string.IsNullOrWhiteSpace(_cfg.Username) ? "Player" : _cfg.Username;
        LoginNick = _cfg.Username;
        RamText = $"{_cfg.RamMb} MB";
        GameDir = _cfg.GameDir;
        VersionLabel = $"v{LauncherConstants.Version}";
        McLabel = $"Minecraft {LauncherConstants.McVersion} · Forge {LauncherConstants.ForgeVersion}";
        ServerAddress = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        OnlinePlayersText = "Проверяем сервер…";
        NeedsAuth = false;
        AuthChecking = false;
        _ = StartupAsync();
    }

    [ObservableProperty] private string _page = "play";
    [ObservableProperty] private string _username = "Player";
    [ObservableProperty] private string _ramText = "4096 MB";
    [ObservableProperty] private string _gameDir = "";
    [ObservableProperty] private string _statusText = "Готов к запуску";
    [ObservableProperty] private string _pctText = "0%";
    [ObservableProperty] private double _progress;
    [ObservableProperty] private string _playButtonText = "Играть";
    [ObservableProperty] private bool _actionsEnabled = true;
    [ObservableProperty] private string _serverStatus = "Проверяем сервер…";
    [ObservableProperty] private IBrush _serverDot = Brush("#64748B");
    [ObservableProperty] private string _serverAddress = "";
    [ObservableProperty] private string _versionLabel = "";
    [ObservableProperty] private string _mcLabel = "";
    [ObservableProperty] private string _onlinePlayersText = "";
    [ObservableProperty] private bool _needsAuth;
    [ObservableProperty] private bool _authChecking;
    [ObservableProperty] private bool _showLoginForm;
    [ObservableProperty] private bool _isLoggedIn;
    [ObservableProperty] private string _loginNick = "";
    [ObservableProperty] private string _loginPassword = "";
    [ObservableProperty] private string _authError = "";
    [ObservableProperty] private bool _authBusy;
    [ObservableProperty] private string _accountBadge = "Гость";
    [ObservableProperty] private string _coinsText = "0 💰";

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
    private void SetRam(string mb)
    {
        if (int.TryParse(mb, out var val))
        {
            RamText = $"{val} MB";
            _cfg.RamMb = val;
            _cfg.Save();
        }
    }

    [RelayCommand]
    private void ToggleLoginForm()
    {
        UiSounds.Play(UiSounds.Kind.Auth);
        ShowLoginForm = !ShowLoginForm;
        AuthError = "";
    }

    [RelayCommand]
    private void Logout()
    {
        _cfg.PortalSession = null;
        _cfg.Save();
        HttpDownload.SetPortalSession(null);
        LoginPassword = "";
        IsLoggedIn = false;
        AccountBadge = "Гость";
        CoinsText = "0 💰";
        ShowLoginForm = false;
        AuthError = "";
        StatusText = "Вышли из аккаунта";
        UiLog("Сессия сброшена", "warn");
    }

    [RelayCommand]
    private void OpenRegister()
    {
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = $"{LauncherConstants.PortalApiBase}/register.html",
                UseShellExecute = true,
            });
        }
        catch
        {
            AuthError = "Не удалось открыть сайт регистрации";
        }
    }

    [RelayCommand]
    private void OpenWebsite()
    {
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = LauncherConstants.PortalApiBase,
                UseShellExecute = true,
            });
        }
        catch
        {
            /* ignore */
        }
    }

    [RelayCommand]
    private async Task SubmitLoginAsync()
    {
        if (AuthBusy) return;
        AuthBusy = true;
        AuthError = "";
        UiSounds.Play(UiSounds.Kind.Auth);
        try
        {
            var (ok, nick, session, err) = await PortalApi.LoginAsync(LoginNick.Trim(), LoginPassword);
            if (!ok || nick == null || session == null)
            {
                AuthError = string.IsNullOrWhiteSpace(err) ? "Неверный логин или пароль" : err;
                return;
            }
            EnterApp(nick, session);
        }
        finally
        {
            AuthBusy = false;
        }
    }

    [RelayCommand]
    private async Task PlayAsync()
    {
        if (_busy) return;
        UiSounds.Play(UiSounds.Kind.Play);
        SaveCfgFromUi();
        if (string.IsNullOrWhiteSpace(_cfg.Username))
        {
            StatusText = "Введи никнейм перед запуском";
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
        UiSounds.Play(UiSounds.Kind.Copy);
        var ip = $"{LauncherConstants.ServerHost}:{LauncherConstants.ServerPort}";
        if (Application.Current?.ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop
            && desktop.MainWindow?.Clipboard is { } clip)
        {
            await clip.SetTextAsync(ip);
            StatusText = "IP скопирован в буфер обмена";
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

    private void EnterApp(string nick, string session)
    {
        Username = nick;
        LoginNick = nick;
        LoginPassword = "";
        _cfg.Username = nick;
        _cfg.PortalSession = session;
        _cfg.Save();
        HttpDownload.SetPortalSession(session);
        IsLoggedIn = true;
        AccountBadge = "Игрок";
        ShowLoginForm = false;
        AuthError = "";
        StatusText = "Аккаунт подключен";
        UiLog($"Вход в аккаунт: {nick}", "ok");
    }

    private async Task StartupAsync()
    {
        try
        {
            if (!string.IsNullOrWhiteSpace(_cfg.PortalSession))
            {
                var (ok, nick, session, _) = await PortalApi.TryRestoreSessionAsync(_cfg.PortalSession);
                if (ok && nick != null && session != null)
                {
                    EnterApp(nick, session);
                }
                else
                {
                    _cfg.PortalSession = null;
                    _cfg.Save();
                    HttpDownload.SetPortalSession(null);
                }
            }
        }
        catch
        {
            /* offline fallback */
        }

        _ = RefreshPingLoopAsync();

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
        if (_tcpOnline == true)
        {
            ServerStatus = _tcpMs is null ? "Онлайн" : $"Онлайн · {_tcpMs} мс";
            ServerDot = Brush("#22C55E");
            OnlinePlayersText = _onlinePlayers is not null
                ? $"Игроков онлайн: {_onlinePlayers} / {(_maxPlayers ?? 100)}"
                : (_tcpMs is null ? "Сервер онлайн" : $"Онлайн · {_tcpMs} мс");
        }
        else if (_tcpOnline == false)
        {
            ServerStatus = "Недоступен";
            ServerDot = Brush("#EF4444");
            OnlinePlayersText = "Сервер оффлайн";
        }
        else
        {
            ServerStatus = "Проверяем сервер…";
            ServerDot = Brush("#64748B");
            OnlinePlayersText = "Проверяем…";
        }
    }

    private async Task RefreshPingLoopAsync()
    {
        while (true)
        {
            try
            {
                var status = await ServerPing.QueryStatusAsync(_cfg.EffectiveHost, _cfg.EffectivePort);
                await Dispatcher.UIThread.InvokeAsync(() =>
                {
                    _tcpOnline = status.Online;
                    _tcpMs = status.LatencyMs;
                    if (status.Online)
                    {
                        _onlinePlayers = status.OnlinePlayers;
                        _maxPlayers = status.MaxPlayers;
                    }
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

            try { await Task.Delay(15000); }
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
        "ok" => Brush("#22C55E"),
        "err" => Brush("#EF4444"),
        "warn" => Brush("#F59E0B"),
        "dim" => Brush("#94A3B8"),
        _ => Brush("#06B6D4"),
    };
}

public sealed class LogLine(string time, string text, IBrush color)
{
    public string Time { get; } = time;
    public string Text { get; } = text;
    public IBrush Color { get; } = color;
}
