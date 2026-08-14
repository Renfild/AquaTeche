"""Shim — prefer: python tools/portal/get_cf_token.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"portal/get_cf_token.py"), run_name="__main__")
