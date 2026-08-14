from pathlib import Path

TOOLS = Path(__file__).resolve().parents[2] / "tools"
SHIMS = {
    "smoke_portal_and_versions.py": "portal/smoke_portal_and_versions.py",
    "set_github_cf_secret.py": "portal/set_github_cf_secret.py",
    "test_portal_login_happy.py": "portal/test_portal_login_happy.py",
    "test_portal_login_cookie.py": "portal/test_portal_login_cookie.py",
    "patch_fawe_mohist.py": "patches/patch_fawe_mohist.py",
    "patch_iu_no_free_scanner.py": "patches/patch_iu_no_free_scanner.py",
    "get_cf_token.py": "portal/get_cf_token.py",
}

for name, rel in SHIMS.items():
    p = TOOLS / name
    if p.exists():
        print("skip", name)
        continue
    p.write_text(
        f'"""Shim — prefer: python tools/{rel}"""\n'
        "from runpy import run_path\n"
        "from pathlib import Path\n"
        f"run_path(str(Path(__file__).resolve().parent / r\"{rel}\"), run_name=\"__main__\")\n",
        encoding="utf-8",
    )
    print("shim", name)
