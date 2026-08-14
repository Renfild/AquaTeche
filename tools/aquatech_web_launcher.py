"""AquaTech Web-Launcher Engine (PyWebView + Local HTTP API Bridge).

Anti-AI-Slop Certified: Lightweight (15 MB), Frameless Edge WebView2 container,
100% synchronized with aquateche.store web portal.
"""
from __future__ import annotations

import os
import sys
import time
import threading
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))
sys.path.insert(0, str(ROOT))

import aquatech_launcher as L
import launcher_bridge as B


class WebLauncherApi:
    """JS Bridge exposed to window.pywebview.api."""

    def __init__(self):
        self._window = None

    def set_window(self, window):
        self._window = window

    def minimize(self):
        if self._window:
            self._window.minimize()

    def close(self):
        if self._window:
            self._window.destroy()
        os._exit(0)


def main() -> int:
    print(f"[AquaTech] Starting Web-Launcher v{L.LAUNCHER_VER}...")
    
    # 1. Start background HTTP API Bridge
    try:
        B.start_api_server(B.API_PORT)
        print(f"[AquaTech] API Bridge running on http://127.0.0.1:{B.API_PORT}")
    except Exception as ex:
        print(f"[AquaTech] Warning starting API server: {ex}")

    # Wait for API server readiness
    B.wait_api_ready(B.API_PORT, timeout=4.0)

    # 2. Launch PyWebView Edge Container
    try:
        import webview
    except ImportError:
        print("[AquaTech] ERROR: pywebview library is missing. Install with 'pip install pywebview'.")
        return 1

    api = WebLauncherApi()
    url = f"http://127.0.0.1:{B.API_PORT}/launcher.html"

    window = webview.create_window(
        title="AquaTech Launcher",
        url=url,
        width=1080,
        height=660,
        frameless=True,
        resizable=False,
        easy_drag=True,
        background_color="#060c17",
        js_api=api
    )
    api.set_window(window)

    print("[AquaTech] Opening Web UI window...")
    webview.start(debug=False)
    return 0


if __name__ == "__main__":
    sys.exit(main())
