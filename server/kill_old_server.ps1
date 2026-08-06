# Stop previous AquaTech Mohist Java processes only
Get-CimInstance Win32_Process -Filter "name='java.exe'" -ErrorAction SilentlyContinue |
  Where-Object { $_.CommandLine -match 'Mohist-1.20.1\.jar' } |
  ForEach-Object {
    Write-Host "    Killing PID $($_.ProcessId)"
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
  }
