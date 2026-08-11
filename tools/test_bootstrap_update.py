"""Shim — prefer: python tools/launcher_tests/test_bootstrap_update.py"""
from pathlib import Path
from runpy import run_path

run_path(str(Path(__file__).resolve().parent / r"launcher_tests/test_bootstrap_update.py"), run_name="__main__")
