"""Shim — prefer: python tools/patches/patch_iu_no_free_scanner.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"patches/patch_iu_no_free_scanner.py"), run_name="__main__")
