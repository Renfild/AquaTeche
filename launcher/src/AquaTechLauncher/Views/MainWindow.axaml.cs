using Avalonia.Controls;
using Avalonia.Interactivity;
using AquaTechLauncher.ViewModels;

namespace AquaTechLauncher.Views;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
    }

    private void LoginNick_LostFocus(object? sender, RoutedEventArgs e)
    {
        if (DataContext is MainViewModel vm)
            _ = vm.CheckNickCommand.ExecuteAsync(null);
    }
}
