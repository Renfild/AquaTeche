"""Shim — prefer: python tools/portal/test_portal_login_happy.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"portal/test_portal_login_happy.py"), run_name="__main__")
