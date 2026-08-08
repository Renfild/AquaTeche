"""Serve dist/AquaTech-Client so friends can click Update in the launcher.

Usage:
  1) python tools/publish_client_pack.py   # refresh pack + manifest
  2) python tools/start_sync_server.py     # keep running while friends play
  3) Open Playit.gg TCP tunnel -> THIS port (default 8765)
  4) Put the public URL into friends' launcher «URL обновлений»
     Example: http://katherine-hydro.tun.ply.gg:12345
     (or write it into dist/releases/update_url.txt next to the .exe)

Note: port 8080 is often taken by NVIDIA Broadcast on Windows — we use 8765.
"""
from __future__ import annotations

import argparse
import socket
import http.server
import socketserver
from pathlib import Path

# 8080 is commonly occupied by NVIDIA Broadcast — avoid it
DEFAULT_PORT = 8765
FALLBACK_PORTS = (8765, 8766, 8767, 18080, 28080)

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / "dist" / "AquaTech-Client"


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(DIRECTORY), **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def log_message(self, fmt, *args):
        print(f"[sync] {self.address_string()} {fmt % args}")


def lan_ips() -> list[str]:
    ips: list[str] = []
    try:
        hostname = socket.gethostname()
        for info in socket.getaddrinfo(hostname, None, socket.AF_INET):
            ip = info[4][0]
            if not ip.startswith("127.") and ip not in ips:
                ips.append(ip)
    except OSError:
        pass
    return ips


def port_free(port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            s.bind(("", port))
            return True
        except OSError:
            return False


def pick_port(preferred: int | None = None) -> int:
    ordered: list[int] = []
    if preferred is not None:
        ordered.append(preferred)
    for p in FALLBACK_PORTS:
        if p not in ordered:
            ordered.append(p)
    for p in ordered:
        if port_free(p):
            return p
    raise SystemExit(
        "Не удалось найти свободный порт среди: "
        + ", ".join(str(x) for x in ordered)
        + "\nЗакрой NVIDIA Broadcast / другой сервер или укажи --port"
    )


def write_local_update_url(port: int) -> None:
    """So AquaTechLauncher next to releases picks up the local sync URL."""
    line = f"http://127.0.0.1:{port}\n"
    for path in (
        ROOT / "dist" / "releases" / "update_url.txt",
        ROOT / "dist" / "update_url.txt",
    ):
        try:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(line, encoding="utf-8")
            print(f"Wrote {path} -> {line.strip()}")
        except OSError as e:
            print(f"Warn: cannot write {path}: {e}")


def run_server(port: int | None = None):
    if not DIRECTORY.is_dir():
        raise SystemExit(f"Pack folder missing: {DIRECTORY}\nRun: python tools/publish_client_pack.py")
    if not (DIRECTORY / "manifest.json").is_file():
        raise SystemExit(f"manifest.json missing in {DIRECTORY}\nRun: python tools/publish_client_pack.py")

    chosen = pick_port(port if port is not None else DEFAULT_PORT)
    write_local_update_url(chosen)

    print("=" * 56)
    print(" AquaTech Sync Server")
    print(f" Serving: {DIRECTORY}")
    print(f" Port:    {chosen}")
    if port is not None and chosen != port:
        print(f" (requested {port} busy — using {chosen})")
    elif chosen != DEFAULT_PORT and port is None:
        print(f" (default {DEFAULT_PORT} busy — using {chosen})")
    print("=" * 56)
    print()
    print("Local test URL:")
    print(f"  http://127.0.0.1:{chosen}/manifest.json")
    for ip in lan_ips():
        print(f"  http://{ip}:{chosen}/manifest.json")
    print()
    print("Friends need the PUBLIC URL from Playit (TCP -> this port).")
    print("Put it in the launcher field «URL обновлений».")
    print()
    print("Ctrl+C to stop.")
    print()

    # Allow address reuse after Ctrl+C restarts
    socketserver.ThreadingTCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("", chosen), Handler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[AquaTech Sync Server] Stopped.")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="AquaTech client pack sync server")
    ap.add_argument("--port", type=int, default=None, help=f"Preferred port (default {DEFAULT_PORT})")
    args = ap.parse_args()
    run_server(args.port)
