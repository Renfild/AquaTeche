"""Shim — prefer: python tools/portal/set_github_cf_secret.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"portal/set_github_cf_secret.py"), run_name="__main__")
