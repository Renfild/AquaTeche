"""Shim — prefer: python tools/portal/smoke_portal_and_versions.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"portal/smoke_portal_and_versions.py"), run_name="__main__")
