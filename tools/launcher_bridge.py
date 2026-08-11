"""Headless launch/update engine + local HTTP API for the web UI (Phase 1)."""
from __future__ import annotations

import json
import os
import threading
import types
import urllib.error
import urllib.request
from collections import deque
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlencode, urlparse

import aquatech_launcher as L

API_HOST = "127.0.0.1"
API_PORT = 12450

PORTAL_BASES = [
    b.strip().rstrip("/")
    for b in (
        os.environ.get("AQUATECH_PORTAL_BASE", ""),
        # workers.dev works while aquateche.store NS is still propagating
        "https://aquatech.santcrail.workers.dev",
        "https://aquateche.store",
    )
    if b and str(b).strip()
]

PORTAL_DEFAULT_BASE = (
    PORTAL_BASES[0] if PORTAL_BASES else "https://aquatech.santcrail.workers.dev"
)


def _friendly_portal_error(ex: BaseException) -> str:
    msg = str(ex)
    if "getaddrinfo failed" in msg or "11001" in msg or "Name or service not known" in msg:
        return (
            "aquateche.store не резолвится (DNS ещё не готов). "
            "Обнови лаунчер или зайди через «Войти через сайт» на workers.dev."
        )
    return msg


def _portal_post(path: str, payload: dict, headers: dict[str, str], timeout: int = 12) -> tuple[dict, str]:
    raw = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    last_err: BaseException | None = None
    for base in PORTAL_BASES:
        url = f"{base}{path}"
        req = urllib.request.Request(url, data=raw, headers=headers, method="POST")
        try:
            with urllib.request.urlopen(req, timeout=timeout) as r:
                return json.loads(r.read().decode("utf-8")), base
        except urllib.error.HTTPError as ex:
            try:
                return json.loads(ex.read().decode("utf-8")), base
            except Exception:
                last_err = ex
        except (urllib.error.URLError, OSError, TimeoutError) as ex:
            last_err = ex
            continue
    raise last_err or RuntimeError("portal unreachable")


def _portal_login_page_url(base: str, port: int = API_PORT) -> str:
    qs = urlencode({"launcher": "1", "port": str(port)})
    return f"{base.rstrip('/')}/login.html?{qs}"


def _resolve_portal_base(prefer: str | None = None) -> str:
    order: list[str] = []
    if prefer:
        order.append(prefer.rstrip("/"))
    for base in PORTAL_BASES:
        if base not in order:
            order.append(base)

    headers = {"User-Agent": f"Mozilla/5.0 AquaTechLauncherBridge/{L.LAUNCHER_VER}"}
    for base in order:
        probe = f"{base}/api/catalog"
        req = urllib.request.Request(probe, headers=headers, method="GET")
        try:
            with urllib.request.urlopen(req, timeout=6) as r:
                if r.status < 500:
                    return base
        except (urllib.error.URLError, OSError, TimeoutError):
            continue
    return PORTAL_DEFAULT_BASE


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
        self.pack_check = {
            "local": None,
            "remote": None,
            "update_available": False,
            "checking": True,
        }
        self.launcher_check = {
            "local": L.LAUNCHER_VER,
            "remote": None,
            "update_available": False,
            "checking": True,
            "hint": "",
        }
        self._portal_base = PORTAL_DEFAULT_BASE
        self.start_pack_check()
        self.start_launcher_check()
        self._revalidate_portal_session()

    def _portal_headers(self) -> dict[str, str]:
        return {
            "User-Agent": f"Mozilla/5.0 AquaTechLauncherBridge/{L.LAUNCHER_VER}",
            "Content-Type": "application/json",
            "x-aquatech-launcher": "1",
        }

    def _save_portal_session(self, session: str, nick: str | None = None) -> None:
        self.cfg["portal_session"] = str(session)
        if nick:
            self.cfg["username"] = str(nick).strip() or self.cfg.get("username", "")
        try:
            L.normalize_server_cfg(L.normalize_game_dir(self.cfg))
            L.CONFIG_PATH.write_text(json.dumps(self.cfg, indent=2), encoding="utf-8")
        except Exception:
            pass

    def _clear_portal_session(self) -> None:
        self.cfg["portal_session"] = ""
        try:
            L.CONFIG_PATH.write_text(json.dumps(self.cfg, indent=2), encoding="utf-8")
        except Exception:
            pass

    def _revalidate_portal_session(self) -> None:
        session = str(self.cfg.get("portal_session") or "").strip()
        if not session:
            return

        def work():
            ok = self.portal_validate_session(session).get("ok")
            if not ok:
                self._clear_portal_session()
                self.log("Сессия портала истекла — войди снова.", "warn")

        threading.Thread(target=work, daemon=True, name="aquatech-portal-revalidate").start()

    def portal_validate_session(self, session: str | None = None) -> dict:
        sid = str(session or self.cfg.get("portal_session") or "").strip()
        if not sid:
            return {"ok": False, "message": "Нет сессии"}

        raw = json.dumps({"session": sid}, ensure_ascii=False).encode("utf-8")
        try:
            data, base = _portal_post("/api/launcher/session", {"session": sid}, self._portal_headers())
            self._portal_base = base
        except Exception as ex:
            return {"ok": False, "message": _friendly_portal_error(ex)}

        if not (data or {}).get("ok"):
            return {"ok": False, "message": data.get("error") or "Сессия недействительна"}

        nick = (data.get("user") or {}).get("nick")
        if nick:
            self.cfg["username"] = nick
        return {"ok": True, "user": data.get("user"), "session": sid}

    def portal_logout(self) -> dict:
        self._clear_portal_session()
        self.log("Выход из аккаунта портала.", "info")
        return {"ok": True}

    def portal_browser_login(self) -> dict:
        import webbrowser

        base = _resolve_portal_base(self._portal_base)
        self._portal_base = base
        url = _portal_login_page_url(base)
        try:
            webbrowser.open(url)
            self.log(f"Открыт вход: {base}", "info")
            return {"ok": True, "url": url, "base": base}
        except Exception as ex:
            return {"ok": False, "message": str(ex)}

    def portal_callback(self, session: str, nick: str = "") -> dict:
        session = str(session or "").strip()
        if not session:
            return {"ok": False, "message": "Пустая сессия"}
        verified = self.portal_validate_session(session)
        if not verified.get("ok"):
            return verified
        user_nick = nick or (verified.get("user") or {}).get("nick") or ""
        self._save_portal_session(session, user_nick)
        self.log(f"Вход выполнен: {user_nick or 'игрок'}", "ok")
        return {"ok": True, "user": verified.get("user"), "session": session}

    def _load_cfg(self) -> dict:
        cfg = {
            "username": "",
            "game_dir": str(L.GAME_DIR),
            "ram_mb": 4096,
            "update_url": L.DEFAULT_UPDATE_URL,
            "portal_session": "",
            "auto_connect": True,
            "server_host": L.SERVER_IP,
            "server_port": L.SERVER_PORT,
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
        L.normalize_server_cfg(L.normalize_game_dir(cfg))
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
            if "auto_connect" in patch:
                self.cfg["auto_connect"] = bool(patch["auto_connect"])
            if "ram_mb" in patch:
                try:
                    self.cfg["ram_mb"] = int(patch["ram_mb"])
                except Exception:
                    pass
        L.normalize_server_cfg(L.normalize_game_dir(self.cfg))
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

    def start_pack_check(self):
        with self._lock:
            prev = dict(self.pack_check)
            self.pack_check = {
                "local": prev.get("local"),
                "remote": prev.get("remote"),
                "update_available": bool(prev.get("update_available")),
                "checking": True,
            }

        def work():
            info = L.check_pack_update_available(self.cfg)
            with self._lock:
                self.pack_check = {**info, "checking": False}

        threading.Thread(target=work, daemon=True, name="aquatech-pack-check").start()

    def start_launcher_check(self):
        with self._lock:
            self.launcher_check = {
                "local": L.LAUNCHER_VER,
                "remote": None,
                "update_available": False,
                "checking": True,
                "hint": "",
            }

        def work():
            info = L.check_launcher_update_available()
            with self._lock:
                self.launcher_check = {**info, "checking": False}
            if info.get("update_available"):
                self.log(
                    f"Доступен лаунчер {info.get('remote')} (сейчас {info.get('local')}). "
                    f"{info.get('hint') or ''}",
                    "warn",
                )

        threading.Thread(target=work, daemon=True, name="aquatech-launcher-check").start()

    def portal_login(self, nick: str, password: str) -> dict:
        payload = {"nick": str(nick).strip(), "password": str(password)}
        if not payload["nick"] or not payload["password"]:
            return {"ok": False, "message": "Некорректный ввод"}

        headers = self._portal_headers()

        try:
            data, base = _portal_post("/api/login", payload, headers)
            self._portal_base = base
        except Exception as ex:
            return {"ok": False, "message": _friendly_portal_error(ex)}

        if not (data or {}).get("ok"):
            return {"ok": False, "message": data.get("error") or data.get("message") or "Ошибка входа"}

        session = data.get("session") or ""
        user_nick = (data.get("user") or {}).get("nick") or payload["nick"]
        self._save_portal_session(session, user_nick)

        return {"ok": True, "user": data.get("user"), "session": session}

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
                "pack": dict(self.pack_check),
                "launcher": dict(self.launcher_check),
                "logs": logs,
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
                    engine.start_pack_check()

            def _download_url(self, url: str, dest_path: Path, reporthook=None):
                L.AquaTechLauncher._download_url(self, url, dest_path, reporthook)

        shim = Shim()
        for name in (
            "_install_forge",
            "_install_java",
            "_sync_files",
            "_run_all",
            "_copy_forge_from_minecraft",
        ):
            fn = getattr(L.AquaTechLauncher, name, None)
            if fn is None:
                raise RuntimeError(f"AquaTechLauncher.{name} missing — broken build")
            setattr(shim, name, types.MethodType(fn, shim))
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
            finally:
                self._running = False

        threading.Thread(target=work, daemon=True, name="aquatech-play").start()
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
                try:
                    shim._done_update(False)
                except Exception:
                    self.state = "error"
            finally:
                self._running = False

        threading.Thread(target=work, daemon=True, name="aquatech-update").start()
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
        if path == "/api/portal_callback":
            qs = parse_qs(parsed.query)
            session = (qs.get("session") or [""])[0]
            nick = (qs.get("nick") or [""])[0]
            res = ENGINE.portal_callback(session, nick)
            html = (
                "<!DOCTYPE html><html lang='ru'><head><meta charset='utf-8'>"
                "<title>AquaTech</title></head><body style='font-family:Segoe UI,sans-serif;"
                "background:#061018;color:#e6eef4;padding:2rem'>"
                + (
                    "<h1>Вход выполнен</h1><p>Можно закрыть вкладку и вернуться в лаунчер.</p>"
                    if res.get("ok")
                    else f"<h1>Ошибка</h1><p>{res.get('message') or 'Не удалось войти'}</p>"
                )
                + "</body></html>"
            )
            raw = html.encode("utf-8")
            self.send_response(200 if res.get("ok") else 400)
            self._cors()
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(raw)))
            self.end_headers()
            self.wfile.write(raw)
            return
        self._json(404, {"error": "not found"})

    def do_POST(self):
        path = urlparse(self.path).path
        try:
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
            if path == "/api/portal_login":
                nick = (body or {}).get("nick") if body else None
                password = (body or {}).get("password") if body else None
                res = ENGINE.portal_login(nick or "", password or "")
                return self._json(200 if res.get("ok") else 401, res)
            if path == "/api/portal_validate":
                res = ENGINE.portal_validate_session()
                return self._json(200 if res.get("ok") else 401, res)
            if path == "/api/portal_logout":
                return self._json(200, ENGINE.portal_logout())
            if path == "/api/portal_browser":
                res = ENGINE.portal_browser_login()
                return self._json(200 if res.get("ok") else 400, res)
            self._json(404, {"error": "not found"})
        except Exception as ex:
            try:
                ENGINE.log(f"API error: {ex}", "err")
            except Exception:
                pass
            self._json(500, {"ok": False, "message": str(ex)})

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
