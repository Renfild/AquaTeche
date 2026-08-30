# Renders brand_wordmark.png (Montserrat + Segoe UI). Called from build_fancymenu_layouts.mjs.
Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"
$out = $args[0]
if (-not $out) { throw "usage: render_fancymenu_wordmark.ps1 <png>" }

$w = 464
$h = 88
$bmp = New-Object System.Drawing.Bitmap $w, $h, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$g.Clear([System.Drawing.Color]::Transparent)

$segoe = New-Object System.Drawing.FontFamily "Segoe UI"
$px = [System.Drawing.GraphicsUnit]::Pixel
$bold = [System.Drawing.FontStyle]::Bold
$reg = [System.Drawing.FontStyle]::Regular
$titleFont = New-Object System.Drawing.Font $segoe, 34, $bold, $px
$subFont = New-Object System.Drawing.Font $segoe, 15, $reg, $px
$fmt = [System.Drawing.StringFormat]::GenericTypographic

$bAqua = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(243, 251, 255))
$bTech = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(102, 255, 255))
$bSub = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(143, 176, 194))

$aquaSize = $g.MeasureString("Aqua", $titleFont, [System.Drawing.PointF]::new(0, 0), $fmt)
$g.DrawString("Aqua", $titleFont, $bAqua, 2, 4, $fmt)
$g.DrawString("Tech", $titleFont, $bTech, [single](2 + $aquaSize.Width + 1), 4, $fmt)

$subtitle = [string]::Concat(
  [char]0x041E, [char]0x043A, [char]0x0435, [char]0x0430, [char]0x043D,
  [char]0x0441, [char]0x043A, [char]0x0438, [char]0x0439, " ",
  [char]0x0441, [char]0x0435, [char]0x0440, [char]0x0432, [char]0x0435, [char]0x0440
)
$g.DrawString($subtitle, $subFont, $bSub, 2, 54, $fmt)

$dir = Split-Path -Parent $out
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()
$titleFont.Dispose()
$subFont.Dispose()
$bAqua.Dispose()
$bTech.Dispose()
$bSub.Dispose()
$fmt.Dispose()
Write-Output "wrote $out"
