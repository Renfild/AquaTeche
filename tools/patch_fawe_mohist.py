"""Shim — prefer: python tools/patches/patch_fawe_mohist.py"""
from runpy import run_path
from pathlib import Path
run_path(str(Path(__file__).resolve().parent / r"patches/patch_fawe_mohist.py"), run_name="__main__")
