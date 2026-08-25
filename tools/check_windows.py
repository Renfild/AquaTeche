import subprocess

ps_script = """
Get-Process | Where-Object { $_.ProcessName -match 'java|cmd|powershell' } | Select-Object Id, ProcessName, MainWindowTitle, Path | Format-Table -AutoSize
"""

res = subprocess.run(['powershell', '-NoProfile', '-Command', ps_script], capture_output=True, text=True, errors='ignore')
print(res.stdout)
