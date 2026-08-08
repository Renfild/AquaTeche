"""Headless launch/update engine + local HTTP API for the web UI (Phase 1)."""
from __future__ import annotations

import json
import threading
import types
import urllib.request
from collections import deque
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse, parse_qs

import aquatech_launcher as L

API_HOST = "127.0.0.1"
API_PORT = 12450


class LauncherEngine:
    """Runs Play/Update without Tk; pushes logs to a ring buffer for the web UI."""

    def __init__(self):
        self.cfg = self._load_cfg()
        self.logs: deque[dict] = deque(maxlen=800)
        self.progress = 0.0
        self.state = "idle"  # idle | busy | ingame | error
        self.status_text = "Готов"
        self._lock = threading.Lock()
        self._running = False
        self._log_seq = 0

    def _load_cfg(self) -> dict:
        cfg = {
            "username": "",
            "game_dir": str(L.GAME_DIR),
            "ram_mb": 4096,
            "update_url": L.DEFAULT_UPDATE_URL,
        }
        try:
            if L.CONFIG_PATH.exists():
                cfg.update(json.loads(L.CONFIG_PATH.read_text("utf-8")))
        except Exception:
            pass
        cfg["update_url"] = L.normalize_update_url(cfg.get("update_url"))
        if "sync_url" in cfg and (
            "tun.ply.gg" in str(cfg.get("sync_url") or "").lower()
            or "playit" in str(cfg.get("sync_url") or "").lower()
        ):
            cfg.pop("sync_url", None)
        try:
            L.CONFIG_PATH.write_text(json.dumps(cfg, indent=2), encoding="utf-8")
        except Exception:
            pass
        return cfg

    def save_cfg(self, patch: dict | None = None) -> dict:
        if patch:
            if "username" in patch:
                self.cfg["username"] = str(patch["username"]).strip()
            if "game_dir" in patch:
                self.cfg["game_dir"] = str(patch["game_dir"]).strip()
            if "update_url" in patch:
                self.cfg["update_url"] = L.normalize_update_url(
                    str(patch["update_url"]).strip().rstrip("/")
                )
            if "ram_mb" in patch:
                try:
                    self.cfg["ram_mb"] = int(patch["ram_mb"])
                except Exception:
                    pass
        try:
            L.CONFIG_PATH.write_text(json.dumps(self.cfg, indent=2), encoding="utf-8")
        except Exception:
            pass
        return dict(self.cfg)

    def log(self, text: str, tag: str = "info"):
        with self._lock:
            self._log_seq += 1
            entry = {
                "id": self._log_seq,
                "t": __import__("time").strftime("%H:%M:%S"),
                "text": text,
                "tag": tag,
            }
            self.logs.append(entry)
            self.status_text = text[:120]

    def set_progress(self, pct: float):
        self.progress = max(0.0, min(100.0, float(pct)))

    def snapshot(self, after_id: int = 0) -> dict:
        with self._lock:
            logs = [e for e in self.logs if e["id"] > after_id]
            return {
                "version": L.LAUNCHER_VER,
                "state": self.state,
                "progress": self.progress,
                "status": self.status_text,
                "cfg": dict(self.cfg),
                "running": self._running,
                "logs": logs,
                "server": f"{L.SERVER_IP}:{L.SERVER_PORT}",
            }

    def _make_shim(self):
        engine = self

        class Shim:
            def __init__(self):
                self._cfg = engine.cfg
                self._running = False

            def _log_line(self, text: str, tag: str = "info"):
                engine.log(text, tag)

            def _set_pct(self, pct: float):
                engine.set_progress(pct)

            def after(self, _ms, fn):
                try:
                    fn()
                except Exception as ex:
                    engine.log(f"UI callback error: {ex}", "err")

            def _done(self, ok: bool, close: bool = False):
                self._running = False
                engine._running = False
                if ok:
                    engine.state = "ingame"
                    engine.set_progress(100)
                else:
                    engine.state = "error"

            def _done_update(self, ok: bool):
                self._running = False
                engine._running = False
                engine.state = "idle" if ok else "error"
                if ok:
                    engine.set_progress(100)
                    engine.log("Сборка обновлена. Можно играть.", "ok")

            def _download_url(self, url: str, dest_path: Path, reporthook=None):
                L.AquaTechLauncher._download_url(self, url, dest_path, reporthook)

        shim = Shim()
        shim._install_forge = types.MethodType(L.AquaTechLauncher._install_forge, shim)
        shim._install_java = types.MethodType(L.AquaTechLauncher._install_java, shim)
        shim._sync_files = types.MethodType(L.AquaTechLauncher._sync_files, shim)
        shim._run_all = types.MethodType(L.AquaTechLauncher._run_all, shim)
        shim._copy_forge_from_minecraft = types.MethodType(
            L.AquaTechLauncher._copy_forge_from_minecraft, shim
        )
        return shim

    def start_play(self) -> tuple[bool, str]:
        if self._running:
            return False, "Уже выполняется"
        nick = (self.cfg.get("username") or "").strip()
        if not nick:
            return False, "Введи никнейм"
        self.save_cfg()
        self._running = True
        self.state = "busy"
        self.set_progress(1)
        self.log("▶ Запуск…", "info")
        shim = self._make_shim()

        def work():
            try:
                shim._run_all(
                    Path(self.cfg["game_dir"]),
                    self.cfg["username"],
                    int(self.cfg.get("ram_mb", 4096)),
                )
            except Exception as ex:
                self.log(f"Критическая ошибка: {ex}", "err")
                self.state = "error"
                self._running = False

        threading.Thread(target=work, daemon=True).start()
        return True, "ok"

    def start_update(self) -> tuple[bool, str]:
        if self._running:
            return False, "Уже выполняется"
        self.save_cfg()
        self._running = True
        self.state = "busy"
        self.set_progress(1)
        self.log("Проверяем обновления сборки…", "info")
        shim = self._make_shim()

        def work():
            try:
                game_dir = Path(self.cfg["game_dir"])
                for sub in ("mods", "config", "kubejs", "resourcepacks"):
                    (game_dir / sub).mkdir(parents=True, exist_ok=True)
                shim._sync_files(game_dir, prefer_remote=True)
                shim._done_update(True)
            except Exception as ex:
                self.log(f"Ошибка обновления: {ex}", "err")
                shim._done_update(False)

        threading.Thread(target=work, daemon=True).start()
        return True, "ok"


ENGINE = LauncherEngine()


def ui_dir() -> Path:
    candidates = [
        L._bundle_dir() / "launcher_ui",
        L._app_dir() / "launcher_ui",
        Path(__file__).resolve().parent.parent / "launcher_ui",
        Path(__file__).resolve().parent / "launcher_ui",
    ]
    for c in candidates:
        if (c / "index.html").is_file():
            return c
    return candidates[-1]


class ApiHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        return  # quiet

    def _cors(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Cache-Control", "no-store")

    def _json(self, code: int, payload: dict):
        raw = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self._cors()
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def _read_json(self) -> dict:
        n = int(self.headers.get("Content-Length") or 0)
        if n <= 0:
            return {}
        return json.loads(self.rfile.read(n).decode("utf-8"))

    def do_OPTIONS(self):
        self.send_response(204)
        self._cors()
        self.end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        if path in ("/", "/index.html"):
            return self._static("index.html", "text/html; charset=utf-8")
        if path.startswith("/assets/"):
            # Files live under launcher_ui/assets/… (not launcher_ui/<name>)
            rel = path.lstrip("/")  # assets/app.css
            return self._static(rel, None)
        if path == "/api/status":
            qs = parse_qs(parsed.query)
            after = int((qs.get("after") or ["0"])[0] or 0)
            return self._json(200, ENGINE.snapshot(after))
        if path == "/api/health":
            return self._json(200, {"ok": True, "version": L.LAUNCHER_VER})
        self._json(404, {"error": "not found"})

    def do_POST(self):
        path = urlparse(self.path).path
        body = self._read_json()
        if path == "/api/browse_dir":
            import tkinter as tk
            from tkinter import filedialog
            res = {"ok": False, "dir": ""}
            done = threading.Event()

            def open_dialog():
                try:
                    root = tk.Tk()
                    root.withdraw()
                    root.attributes("-topmost", True)
                    selected = filedialog.askdirectory(title="Выберите папку для AquaTech")
                    root.destroy()
                    if selected:
                        res["ok"] = True
                        res["dir"] = selected
                except Exception:
                    pass
                done.set()

            threading.Thread(target=open_dialog, daemon=True).start()
            done.wait(timeout=60.0)
            return self._json(200, res)
        if path == "/api/config":
            cfg = ENGINE.save_cfg(body)
            return self._json(200, {"ok": True, "cfg": cfg})
        if path == "/api/play":
            if body:
                ENGINE.save_cfg(body)
            ok, msg = ENGINE.start_play()
            return self._json(200 if ok else 400, {"ok": ok, "message": msg})
        if path == "/api/update":
            if body:
                ENGINE.save_cfg(body)
            ok, msg = ENGINE.start_update()
            return self._json(200 if ok else 400, {"ok": ok, "message": msg})
        self._json(404, {"error": "not found"})

    def _static(self, name: str, content_type: str | None):
        base = ui_dir().resolve()
        # Prevent path traversal; compare resolved paths (Windows-safe)
        try:
            target = (base / name).resolve()
            target.relative_to(base)
        except Exception:
            self._json(404, {"error": "file"})
            return
        if not target.is_file():
            self._json(404, {"error": "file", "path": name})
            return
        data = target.read_bytes()
        if content_type is None:
            if name.endswith(".css"):
                content_type = "text/css; charset=utf-8"
            elif name.endswith(".js"):
                content_type = "application/javascript; charset=utf-8"
            elif name.endswith(".png"):
                content_type = "image/png"
            elif name.endswith(".svg"):
                content_type = "image/svg+xml"
            else:
                content_type = "application/octet-stream"
        self.send_response(200)
        self._cors()
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)


def start_api_server(port: int = API_PORT) -> ThreadingHTTPServer:
    # allow_reuse so relaunches work
    ThreadingHTTPServer.allow_reuse_address = True
    httpd = ThreadingHTTPServer((API_HOST, port), ApiHandler)
    t = threading.Thread(target=httpd.serve_forever, daemon=True)
    t.start()
    return httpd


def wait_api_ready(port: int = API_PORT, timeout: float = 5.0) -> bool:
    import time

    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(f"http://{API_HOST}:{port}/api/health", timeout=0.5) as r:
                if r.status == 200:
                    return True
        except Exception:
            time.sleep(0.1)
    return False
