using System.ComponentModel;
using System.Runtime.CompilerServices;
using Avalonia;
using Avalonia.Animation;
using Avalonia.Animation.Easings;
using Avalonia.Controls;
using Avalonia.Interactivity;
using Avalonia.Styling;
using Avalonia.Media;
using AquaTechLauncher.ViewModels;

namespace AquaTechLauncher.Views;

public partial class MainWindow : Window
{
    private MainViewModel? _vm;

    public MainWindow()
    {
        InitializeComponent();
        DataContextChanged += OnDataContextChanged;
        if (DataContext is MainViewModel vm) HookVm(vm);
    }

    private void OnDataContextChanged(object? sender, EventArgs e)
    {
        if (DataContext is MainViewModel vm) HookVm(vm);
    }

    private void HookVm(MainViewModel vm)
    {
        if (!ReferenceEquals(_vm, vm))
        {
            _vm = vm;
            vm.PropertyChanged += VmOnPropertyChanged;
        }
    }

    private void VmOnPropertyChanged(object? sender, PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(MainViewModel.Page))
            FadePagesHost();
    }

    /// <summary>Soft crossfade-ish fade-in when the active page swaps (Apple-style restraint).</summary>
    private async void FadePagesHost()
    {
        var host = this.FindControl<Panel>("PagesHost");
        if (host == null) return;
        var fade = new Animation
        {
            Duration = TimeSpan.FromMilliseconds(200),
            Easing = new SineEaseOut(),
            Children =
            {
                new KeyFrame
                {
                    Cue = new Cue(0.0),
                    Setters = { new Setter { Property = Visual.OpacityProperty, Value = 0d } },
                },
                new KeyFrame
                {
                    Cue = new Cue(1.0),
                    Setters = { new Setter { Property = Visual.OpacityProperty, Value = 1d } },
                },
            },
        };
        host.Opacity = 0;
        await fade.RunAsync(host);
        host.Opacity = 1;
    }

    private void LoginNick_LostFocus(object? sender, RoutedEventArgs e)
    {
        if (DataContext is MainViewModel vm)
            _ = vm.CheckNickCommand.ExecuteAsync(null);
    }
}
