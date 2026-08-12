#!/usr/bin/env python3
import pathlib
import zipfile

def main():
    s = pathlib.Path('dist/releases/AquaTechLauncher')
    d = pathlib.Path('dist/releases/AquaTechLauncher.zip')
    if not s.is_dir():
        raise SystemExit(f"Missing build directory {s}")
    d.unlink(missing_ok=True)
    with zipfile.ZipFile(d, 'w', zipfile.ZIP_DEFLATED) as z:
        for p in s.rglob('*'):
            if p.is_file():
                z.write(p, p.relative_to(s).as_posix())
    print(f"Created {d} ({d.stat().st_size / 1024 / 1024:.2f} MB)")

if __name__ == '__main__':
    main()
