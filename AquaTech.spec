# -*- mode: python ; coding: utf-8 -*-


a = Analysis(
    ['tools\\aquatech_launcher.py'],
    pathex=[],
    binaries=[],
    datas=[('docs', 'docs'), ('tools/aquatech.ico', 'tools'), ('tools/aquatech_icon.png', 'tools')],
    hiddenimports=[],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=['PyQt5', 'PyQt6', 'PySide2', 'PySide6', 'numpy', 'scipy', 'pandas', 'matplotlib', 'cryptography', 'bcrypt', 'mako', 'jinja2', 'win32com', 'docutils', 'setuptools'],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='AquaTech',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=['tools\\aquatech.ico'],
)
