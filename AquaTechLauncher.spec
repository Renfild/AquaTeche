# -*- mode: python ; coding: utf-8 -*-
# onedir: much faster cold start than onefile (no unpack to %TEMP% + Defender rescan each launch)

a = Analysis(
    ['tools\\aquatech_launcher.py'],
    pathex=['tools'],
    binaries=[],
    datas=[
        # Forge installer (fallback) + prebuilt runtime (fast path — skips 5min processors)
        ('tools\\forge-1.20.1-47.4.0-installer.jar', '.'),
        ('tools\\forge-runtime-1.20.1-47.4.0.zip', '.'),
        ('tools\\5.json', '.'),
        ('.gh_token', '.'),
        ('tools\\aquatech.ico', '.'),
        ('launcher_ui', 'launcher_ui'),
    ],
    hiddenimports=[
        'launcher_bridge',
        'webview',
        'webview.platforms.winforms',
        'webview.platforms.edgechromium',
    ],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[
        'PyQt5',
        'PyQt6',
        'PySide2',
        'PySide6',
        'gi',
        'gtk',
        'qtpy',
        'cefpython3',
        # Heavy unused deps that hooks sometimes pull in — bloat + slow import
        'numpy',
        'scipy',
        'pandas',
        'matplotlib',
        'PIL',
        'Pillow',
        'cv2',
        'torch',
        'tensorflow',
        'IPython',
        'notebook',
        'pytest',
    ],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    [],
    exclude_binaries=True,
    name='AquaTechLauncher',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    # UPX can corrupt embedded JARs — keep Forge installer intact
    upx=False,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon='tools\\aquatech.ico',
)

coll = COLLECT(
    exe,
    a.binaries,
    a.datas,
    name='AquaTechLauncher',
)
