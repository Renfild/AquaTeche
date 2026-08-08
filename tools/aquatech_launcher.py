"""
AquaTech Launcher
- Ocean UI; auto Java 17 + Forge 1.20.1 (installer embedded in exe)
- Auto-downloads vanilla Minecraft client jar, libraries, natives, assets (Mojang CDN)
- Syncs pack from local AquaTech-Client or update URL (sync server) — no CurseForge needed
"""

import os, sys, json, hashlib, subprocess, threading, urllib.request, zipfile, shutil, time, platform, math, random
from concurrent.futures import ThreadPoolExecutor, as_completed
import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from pathlib import Path

# ─── Config ──────────────────────────────────────────────────────────────────
LAUNCHER_VER   = "2.9.6"
MC_VER         = "1.20.1"
FORGE_VER      = "47.4.0"
MCP_VER        = "20230612.114412"  # forge --fml.mcpVersion / client-*-srg.jar folder
# Forge language providers — NOT listed in the slim client version.json, but required
# on the classpath. Without them ModDiscoverer locators stay null and the game
# dies right after the early display window (looks like "opens and closes").
FORGE_LANG_PROVIDERS = (
    "fmlcore",
    "javafmllanguage",
    "lowcodelanguage",
    "mclanguage",
)
SERVER_IP      = "katherine-hydro.tun.ply.gg"
SERVER_PORT    = "31279"
# Warm Play: skip slow CDN cascade when enough mods already on disk
PACK_READY_MIN_JARS = 40
# Pack CDN for friends — website hosts manifest.json; jars download from GitHub Releases URLs inside it.
# (Playit is only the Minecraft server IP, not the modpack CDN.)
DEFAULT_UPDATE_URL = "https://aquatech-7gs.pages.dev/pack"
# If nothing configured — try local sync server (start_sync_server.py default 8765).
# Avoid 8080 first: NVIDIA Broadcast often binds it and returns bogus 404.
LOCAL_SYNC_FALLBACKS = (
    "http://127.0.0.1:8765",
    "http://localhost:8765",
    "http://127.0.0.1:8766",
    "http://127.0.0.1:18080",
    "http://127.0.0.1:8080",
)

# Manifest / pack mirrors (website first, then jsDelivr / GitHub raw).
PACK_CDN_MIRRORS = (
    DEFAULT_UPDATE_URL,
    "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack",
    "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/pack",
)

# Optional GitHub fallback (often 404 if repo/path missing — pack sync prefers update_url / local).
GITHUB_RAW     = "https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/pack"
MANIFEST_URL   = f"{GITHUB_RAW}/manifest.json"
GITHUB_RELEASE = "https://github.com/Renfild/AquaTeche/releases/download/pack-2.9.2"
PACK_FOLDERS   = ("mods", "config", "kubejs", "resourcepacks")
# Player-local files kept even if not in pack manifest (LoliLand-style sync)
SYNC_KEEP_NAMES = {
    "options.txt",
    "optionsof.txt",
    "servers.dat",
    "usercache.json",
    "usernamecache.json",
    "hotbar.nbt",
    "realms_persistence.json",
}
DEV_PACK_DIR   = Path(r"C:\Users\xieto\Desktop\AquaTech\dist\AquaTech-Client")
# Asset mirrors — BMCLAPI first (usually much faster than Mojang for RU/EU)
ASSET_MIRRORS = [
    "https://bmclapi2.bangbang93.com/assets",
    "https://resources.download.minecraft.net",
]
# Lang/icons first so the game can start; sounds continue in background
ASSET_CRITICAL_PREFIXES = (
    "icons/",
    "minecraft/lang/",
    "minecraft/texts/",
    "minecraft/textures/",
    "minecraft/font/",
    "minecraft/sounds.json",
)
ASSET_WORKERS = 64
LIB_WORKERS = 24
SYNC_WORKERS = 10
_JAVA_CACHE: str | None = None


FORGE_INSTALLER_NAME = f"forge-{MC_VER}-{FORGE_VER}-installer.jar"
FORGE_RUNTIME_ZIP = f"forge-runtime-{MC_VER}-{FORGE_VER}.zip"
FORGE_VER_ID = f"{MC_VER}-forge-{FORGE_VER}"
# Official + mirrors. Cloudflare on maven often returns 403 to bare clients.
FORGE_URLS = [
    f"https://maven.minecraftforge.net/net/minecraftforge/forge/{MC_VER}-{FORGE_VER}/{FORGE_INSTALLER_NAME}",
    f"https://files.minecraftforge.net/maven/net/minecraftforge/forge/{MC_VER}-{FORGE_VER}/{FORGE_INSTALLER_NAME}",
    f"https://maven.aliyun.com/repository/public/net/minecraftforge/forge/{MC_VER}-{FORGE_VER}/{FORGE_INSTALLER_NAME}",
    f"https://bmclapi2.bangbang93.com/maven/net/minecraftforge/forge/{MC_VER}-{FORGE_VER}/{FORGE_INSTALLER_NAME}",
    f"https://github.com/Renfild/AquaTeche/releases/download/modpack-latest/{FORGE_INSTALLER_NAME}",
]
FORGE_MIN_BYTES = 1_000_000  # real installer is ~6MB; reject HTML/403 stubs
JAVA_URL       = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse"

GAME_DIR       = Path.home() / "AppData" / "Roaming" / "AquaTech"
JAVA_DIR       = GAME_DIR / "_java17"
CONFIG_PATH    = Path.home() / ".aquatech_launcher.json"

# ─── Palette (deep ocean launcher) ────────────────────────────────────────────
C_BG       = "#061018"
C_SIDE     = "#0A1520"
C_PANEL    = "#0C1620"
C_CARD     = "#101C28"
C_BORDER   = "#1A2C3C"
C_LINE     = "#152433"
C_ACCENT   = "#3DB8C5"
C_ACCENT2  = "#2A9AAB"
C_ACCENT_H = "#56CDD8"
C_GREEN    = "#5ED9A0"
C_RED      = "#E06B6B"
C_WARN     = "#D4A35C"
C_TEXT     = "#E6EEF4"
C_MUTED    = "#8BA0B0"
C_DIM      = "#5A7080"
C_PROG_BG  = "#0A121A"
C_FIELD    = "#0A141C"
C_FIELD_F  = "#12202C"
C_NAV_ACT  = "#132836"

FONT_BRAND  = ("Segoe UI Semibold", 42)
FONT_TITLE  = ("Segoe UI Semibold", 22)
FONT_SUB    = ("Segoe UI", 10)
FONT_LABEL  = ("Segoe UI", 8)
FONT_ENTRY  = ("Segoe UI", 11)
FONT_BTN    = ("Segoe UI Semibold", 12)
FONT_BTN_XL = ("Segoe UI Semibold", 15)
FONT_NAV    = ("Segoe UI Semibold", 11)
FONT_STATUS = ("Segoe UI", 9)
FONT_STEP   = ("Consolas", 9)
FONT_CHIP   = ("Segoe UI", 8)

WIN_W, WIN_H = 980, 640
SIDE_W = 208
HERO_H = 220


def _hex_to_rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def _rgb_to_hex(r: int, g: int, b: int) -> str:
    return f"#{max(0, min(255, int(r))):02x}{max(0, min(255, int(g))):02x}{max(0, min(255, int(b))):02x}"


def _lerp_color(a: str, b: str, t: float) -> str:
    t = max(0.0, min(1.0, t))
    ar, ag, ab = _hex_to_rgb(a)
    br, bg, bb = _hex_to_rgb(b)
    return _rgb_to_hex(ar + (br - ar) * t, ag + (bg - ag) * t, ab + (bb - ab) * t)


def _round_rect_pts(x1, y1, x2, y2, r):
    return [
        x1 + r, y1, x2 - r, y1, x2, y1, x2, y1 + r,
        x2, y2 - r, x2, y2, x2 - r, y2, x1 + r, y2,
        x1, y2, x1, y2 - r, x1, y1 + r, x1, y1,
    ]


class SoftButton(tk.Canvas):
    """Rounded launcher button with hover."""

    def __init__(
        self,
        master,
        text: str,
        command=None,
        *,
        primary: bool = False,
        height: int = 46,
        radius: int = 10,
        font=None,
        **kw,
    ):
        super().__init__(
            master,
            height=height,
            highlightthickness=0,
            bd=0,
            cursor="hand2",
            bg=master.cget("bg") if hasattr(master, "cget") else C_BG,
            **kw,
        )
        self._cmd = command
        self._text = text
        self._primary = primary
        self._h = height
        self._r = radius
        self._font = font or FONT_BTN
        self._enabled = True
        self._hover_t = 0.0
        self._hover_target = 0.0
        self._press = False
        self._anim_id = None

        if primary:
            self._c0, self._c1 = C_ACCENT2, C_ACCENT_H
            self._fg = "#061018"
        else:
            self._c0, self._c1 = C_CARD, C_FIELD_F
            self._fg = C_TEXT

        self.bind("<Configure>", lambda e: self._draw())
        self.bind("<Enter>", self._on_enter)
        self.bind("<Leave>", self._on_leave)
        self.bind("<ButtonPress-1>", self._on_press)
        self.bind("<ButtonRelease-1>", self._on_release)
        self.after(16, self._draw)

    def set_text(self, text: str):
        self._text = text
        self._draw()

    def set_enabled(self, enabled: bool):
        self._enabled = enabled
        self.configure(cursor="hand2" if enabled else "arrow")
        if not enabled:
            self._hover_target = 0.0
        self._draw()

    def set_colors(self, idle: str, hover: str, fg: str | None = None):
        self._c0, self._c1 = idle, hover
        if fg:
            self._fg = fg
        self._draw()

    def _on_enter(self, _=None):
        if self._enabled:
            self._hover_target = 1.0
            self._tick_hover()

    def _on_leave(self, _=None):
        self._hover_target = 0.0
        self._press = False
        self._tick_hover()

    def _on_press(self, _=None):
        if self._enabled:
            self._press = True
            self._draw()

    def _on_release(self, _=None):
        if self._enabled and self._press:
            self._press = False
            self._draw()
            if self._cmd:
                self._cmd()

    def _tick_hover(self):
        diff = self._hover_target - self._hover_t
        if abs(diff) < 0.02:
            self._hover_t = self._hover_target
            self._draw()
            return
        self._hover_t += diff * 0.28
        self._draw()
        self._anim_id = self.after(16, self._tick_hover)

    def _draw(self):
        self.delete("all")
        w = max(self.winfo_width(), 10)
        h = self._h
        fill = _lerp_color(self._c0, self._c1, self._hover_t)
        if not self._enabled:
            fill = _lerp_color(fill, C_BG, 0.45)
        if self._press and self._enabled:
            fill = _lerp_color(fill, "#000000", 0.12)
        pad = 1
        r = self._r
        self.create_polygon(_round_rect_pts(pad, pad, w - pad, h - pad, r), smooth=True, fill=fill, outline="")
        if not self._primary and self._enabled:
            self.create_polygon(
                _round_rect_pts(pad, pad, w - pad, h - pad, r),
                smooth=True, fill="", outline=C_BORDER, width=1,
            )
        fg = self._fg if self._enabled else C_DIM
        self.create_text(w // 2, h // 2, text=self._text, fill=fg, font=self._font)


class NavItem(tk.Canvas):
    """Sidebar navigation row."""

    def __init__(self, master, text: str, command=None, **kw):
        super().__init__(
            master, height=40, highlightthickness=0, bd=0, cursor="hand2",
            bg=C_SIDE, **kw,
        )
        self._text = text
        self._cmd = command
        self._active = False
        self._hover = False
        self.bind("<Configure>", lambda e: self._draw())
        self.bind("<Enter>", self._enter)
        self.bind("<Leave>", self._leave)
        self.bind("<ButtonRelease-1>", self._click)
        self.after(16, self._draw)

    def set_active(self, active: bool):
        self._active = active
        self._draw()

    def _enter(self, _=None):
        self._hover = True
        self._draw()

    def _leave(self, _=None):
        self._hover = False
        self._draw()

    def _click(self, _=None):
        if self._cmd:
            self._cmd()

    def _draw(self):
        self.delete("all")
        w = max(self.winfo_width(), 10)
        h = 40
        if self._active:
            fill = C_NAV_ACT
        elif self._hover:
            fill = _lerp_color(C_SIDE, C_CARD, 0.55)
        else:
            fill = C_SIDE
        self.create_rectangle(0, 0, w, h, fill=fill, outline="")
        if self._active:
            self.create_rectangle(0, 8, 3, h - 8, fill=C_ACCENT, outline="")
        fg = C_TEXT if self._active or self._hover else C_MUTED
        self.create_text(18, h // 2, text=self._text, fill=fg, font=FONT_NAV, anchor="w")


# ─── Helpers ──────────────────────────────────────────────────────────────────
def md5_file(p: Path) -> str:
    if not p.exists(): return ""
    h = hashlib.md5()
    with open(p, "rb") as f:
        for c in iter(lambda: f.read(1 << 16), b""): h.update(c)
    return h.hexdigest()


def _app_dir() -> Path:
    """Folder with the .exe (frozen) or tools/ (dev)."""
    if getattr(sys, "frozen", False):
        return Path(sys.executable).resolve().parent
    return Path(__file__).resolve().parent


def _bundle_dir() -> Path:
    """PyInstaller extract dir (_MEIPASS) or tools/."""
    if getattr(sys, "frozen", False) and hasattr(sys, "_MEIPASS"):
        return Path(sys._MEIPASS)
    return Path(__file__).resolve().parent


def _is_valid_forge_installer(path: Path) -> bool:
    if not path.exists() or path.stat().st_size < FORGE_MIN_BYTES:
        return False
    # ZIP/JAR magic
    try:
        with open(path, "rb") as f:
            return f.read(2) == b"PK"
    except OSError:
        return False


def find_bundled_forge_installer() -> Path | None:
    """Prefer installer shipped inside the exe (_MEIPASS) / next to the launcher."""
    name = FORGE_INSTALLER_NAME
    candidates = [
        _bundle_dir() / name,  # PyInstaller onefile extract — primary
        _app_dir() / name,
        _app_dir() / "releases" / name,
        Path(__file__).resolve().parent / name,
    ]
    for p in candidates:
        if _is_valid_forge_installer(p):
            return p
    return None


def find_bundled_forge_runtime_zip() -> Path | None:
    """Prebuilt Forge client+universal+version.json — skips slow installer processors."""
    name = FORGE_RUNTIME_ZIP
    candidates = [
        _bundle_dir() / name,
        _app_dir() / name,
        _app_dir() / "releases" / name,
        Path(__file__).resolve().parent / name,
        Path(__file__).resolve().parent.parent / "tools" / name,
    ]
    for p in candidates:
        if p.exists() and p.stat().st_size > 1_000_000:
            return p
    return None


def _maven_mirror_urls(url: str) -> list[str]:
    """Prefer BMCLAPI / Aliyun over slow official Maven."""
    urls = []
    prefixes = (
        "https://maven.minecraftforge.net/",
        "https://libraries.minecraft.net/",
        "https://files.minecraftforge.net/maven/",
        "https://maven.aliyun.com/repository/public/",
    )
    path = None
    for pref in prefixes:
        if url.startswith(pref):
            path = url[len(pref):]
            break
    if path:
        urls.append(f"https://bmclapi2.bangbang93.com/maven/{path}")
        urls.append(f"https://maven.aliyun.com/repository/public/{path}")
    urls.append(url)
    # dedupe preserve order
    seen = set()
    out = []
    for u in urls:
        if u not in seen:
            seen.add(u)
            out.append(u)
    return out


def _http_json(url: str, timeout: int = 60) -> dict:
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 AquaTechLauncher/2.6"})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def _java_major_version(java_path: str) -> int | None:
    """Return major version (17, 21, 25...) or None."""
    try:
        exe = java_path
        if exe.lower().endswith("javaw.exe"):
            cand = str(Path(exe).with_name("java.exe"))
            if Path(cand).exists():
                exe = cand
        r = subprocess.run([exe, "-version"], capture_output=True, timeout=5)
        text = (r.stderr or r.stdout or b"").decode("utf-8", errors="replace")
        # examples: java version "17.0.10"  / openjdk version "1.8.0_402" / "25.0.1"
        import re
        m = re.search(r'version "(\d+)(?:\.(\d+))?', text)
        if not m:
            return None
        major = int(m.group(1))
        if major == 1 and m.group(2):
            return int(m.group(2))  # 1.8 -> 8
        return major
    except Exception:
        return None


def find_local_pack_dir(cfg: dict | None = None) -> Path | None:
    """Resolve AquaTech-Client pack folder (local source of truth)."""
    candidates: list[Path] = []
    if cfg and cfg.get("pack_dir"):
        candidates.append(Path(cfg["pack_dir"]))
    # Next to the .exe / next to tools
    candidates.append(_app_dir() / "AquaTech-Client")
    candidates.append(_app_dir().parent / "AquaTech-Client")
    candidates.append(_app_dir() / "dist" / "AquaTech-Client")
    candidates.append(DEV_PACK_DIR)
    for c in candidates:
        try:
            if c.is_dir() and (c / "mods").is_dir():
                return c.resolve()
        except OSError:
            continue
    return None


def _is_loopback_url(url: str) -> bool:
    u = (url or "").lower()
    return "127.0.0.1" in u or "localhost" in u or "[::1]" in u


def _pack_looks_ready(game_dir: Path) -> bool:
    """Heuristic: pack already installed — skip long CDN timeout cascades on Play."""
    mods = game_dir / "mods"
    if not mods.is_dir() or not (game_dir / "kubejs").is_dir():
        return False
    try:
        n = sum(1 for _ in mods.glob("*.jar"))
    except OSError:
        return False
    return n >= PACK_READY_MIN_JARS


def normalize_update_url(url: str | None) -> str:
    """Drop mistaken Playit/Minecraft host from pack CDN field; keep real http(s) CDNs."""
    u = (url or "").strip().rstrip("/")
    if not u:
        return DEFAULT_UPDATE_URL
    low = u.lower()
    # Common mistake: game server IP/port pasted into «URL обновлений»
    if "tun.ply.gg" in low or "playit" in low:
        return DEFAULT_UPDATE_URL
    if SERVER_IP.lower() in low:
        return DEFAULT_UPDATE_URL
    if not (low.startswith("http://") or low.startswith("https://")):
        return DEFAULT_UPDATE_URL
    return u


def resolve_update_base(cfg: dict | None = None, *, allow_local_fallback: bool = True) -> str:
    """CDN base URL for friends (no trailing slash). Website pack folder or sync server."""
    candidates: list[str] = []
    if cfg and cfg.get("update_url"):
        candidates.append(normalize_update_url(str(cfg["update_url"])))
    for p in (_app_dir() / "update_url.txt", _app_dir().parent / "update_url.txt"):
        try:
            if p.is_file():
                candidates.append(normalize_update_url(p.read_text("utf-8").strip().splitlines()[0]))
        except OSError:
            pass
    if DEFAULT_UPDATE_URL:
        candidates.append(DEFAULT_UPDATE_URL.strip())
    candidates.extend(PACK_CDN_MIRRORS)
    if allow_local_fallback:
        candidates.extend(LOCAL_SYNC_FALLBACKS)
    seen: set[str] = set()
    for raw in candidates:
        base = (raw or "").strip().rstrip("/")
        if not base or base in seen:
            continue
        seen.add(base)
        if base.startswith("http://") or base.startswith("https://"):
            return base
    return ""


def purge_pack_extras(game_dir: Path, wanted_rels: set[str], log=None) -> int:
    """Delete files under PACK_FOLDERS that are not listed in the manifest (LoliLand-style)."""
    removed = 0
    wanted_norm = {r.replace("\\", "/").lstrip("/") for r in wanted_rels}
    for folder in PACK_FOLDERS:
        root = game_dir / folder
        if not root.is_dir():
            continue
        for path in list(root.rglob("*")):
            if not path.is_file():
                continue
            if path.name in SYNC_KEEP_NAMES or path.name.startswith("."):
                continue
            rel = path.relative_to(game_dir).as_posix()
            if rel in wanted_norm:
                continue
            try:
                path.unlink()
                removed += 1
                if log:
                    log(f"🗑  убран лишний: {rel}")
            except OSError:
                pass
    return removed


def apply_manifest_sync(
    game_dir: Path,
    manifest: dict,
    *,
    source: str,
    pack: Path | None = None,
    base: str = "",
    verify_hash: bool = False,
    download_url=None,
    log=None,
) -> tuple[int, int, int]:
    """Apply pack manifest to game_dir. Returns (updated, failed, deleted).

    verify_hash=True (Update button): re-check MD5 even when size matches.
    verify_hash=False (Play warm start): skip when size matches (faster).
    """
    def _log(msg: str):
        if log:
            log(msg)

    files = manifest.get("files") or []
    if not files:
        _log("⚠️  Манифест пустой — нечего синхронизировать")
        return 0, 0, 0

    wanted = {item["path"].replace("\\", "/").lstrip("/") for item in files}
    deleted = purge_pack_extras(game_dir, wanted, log=lambda m: _log(m))

    jobs = []
    for item in files:
        rel = item["path"].replace("\\", "/").lstrip("/")
        md5 = (item.get("md5") or "").lower()
        local = game_dir / rel.replace("/", os.sep)
        expect = int(item.get("size") or 0)
        if local.exists():
            same_size = (not expect) or local.stat().st_size == expect
            if same_size and not verify_hash:
                continue
            if same_size and md5 and md5_file(local).lower() == md5:
                continue
            if same_size and not md5 and not verify_hash:
                continue
        jobs.append((item, rel, md5, local, expect))

    if not jobs:
        _log(f"✓ Сборка актуальна ({len(files)} файлов)" + (f", удалено {deleted}" if deleted else ""))
        return 0, 0, deleted

    _log(f"⚡ Обновляем {len(jobs)}/{len(files)} файлов…")
    updated = failed = 0

    def one(job):
        item, rel, md5, local, expect = job
        local.parent.mkdir(parents=True, exist_ok=True)
        try:
            if source == "local" and pack is not None:
                src = pack / rel.replace("/", os.sep)
                if not src.exists():
                    return False
                shutil.copy2(src, local)
            else:
                if download_url is None:
                    return False
                # Prefer per-file CDN URL from manifest (GitHub Releases).
                # `base` is only where manifest.json lives (website), not the jars.
                url = (item.get("url") or "").strip()
                if not url and source == "cdn" and base:
                    url = f"{base.rstrip('/')}/{rel}"
                if not url:
                    url = f"{GITHUB_RAW}/{rel}"
                download_url(url, local)
            if md5:
                got = md5_file(local).lower()
                if got != md5:
                    try:
                        local.unlink(missing_ok=True)
                    except OSError:
                        pass
                    return False
            if expect and local.stat().st_size != expect:
                try:
                    local.unlink(missing_ok=True)
                except OSError:
                    pass
                return False
            return True
        except Exception:
            try:
                local.unlink(missing_ok=True)
            except OSError:
                pass
            return False

    with ThreadPoolExecutor(max_workers=SYNC_WORKERS) as pool:
        for fut in as_completed([pool.submit(one, j) for j in jobs]):
            if fut.result():
                updated += 1
            else:
                failed += 1

    if failed:
        _log(f"✓ Синхронизация: {updated} ок, {failed} ошибок" + (f", −{deleted}" if deleted else ""))
    else:
        _log(f"✓ Синхронизация: {updated}/{len(files)}" + (f", удалено {deleted}" if deleted else ""))
    return updated, failed, deleted


def build_manifest_from_pack(pack_dir: Path, base_url: str = "") -> dict:
    """Scan local pack and build an in-memory manifest (md5 + size)."""
    files = []
    base = base_url.rstrip("/")
    for folder in PACK_FOLDERS:
        root = pack_dir / folder
        if not root.exists():
            continue
        for path in root.rglob("*"):
            if not path.is_file():
                continue
            if path.name.startswith(".") or path.suffix.lower() in (".tmp", ".log", ".bak"):
                continue
            rel = path.relative_to(pack_dir).as_posix()
            if base:
                url = f"{base}/{rel}"
            elif folder == "mods":
                url = f"{GITHUB_RELEASE}/{path.name}"
            else:
                url = f"{GITHUB_RAW}/{rel}"
            files.append({
                "path": rel,
                "md5": md5_file(path),
                "size": path.stat().st_size,
                "url": url,
            })
    return {
        "version": "local",
        "mc_version": MC_VER,
        "forge_version": FORGE_VER,
        "files": files,
    }


def find_java() -> str | None:
    # Minecraft 1.20.1 Forge requires Java 17 exactly (class file / ASM).
    global _JAVA_CACHE
    if _JAVA_CACHE:
        if _JAVA_CACHE == "java" or Path(_JAVA_CACHE).exists():
            return _JAVA_CACHE
        _JAVA_CACHE = None

    required = 17

    def ok(path: str) -> str | None:
        if not path:
            return None
        if path != "java" and not Path(path).exists():
            return None
        ver = _java_major_version(path)
        if ver == required:
            return path
        return None

    # 1. Our bundled JRE (may be nested jdk-17.x-jre/bin/java.exe)
    if JAVA_DIR.exists():
        direct = JAVA_DIR / "bin" / "java.exe"
        hit = ok(str(direct))
        if hit:
            _JAVA_CACHE = hit
            return hit
        for p in JAVA_DIR.rglob("java.exe"):
            if p.parent.name.lower() == "bin":
                hit = ok(str(p))
                if hit:
                    _JAVA_CACHE = hit
                    return hit

    # 2. Scan common install roots for JDK/JRE 17
    roots = [
        Path(r"C:\Program Files\Eclipse Adoptium"),
        Path(r"C:\Program Files\Java"),
        Path(r"C:\Program Files\Microsoft"),
        Path(r"C:\Program Files\Zulu"),
        Path(r"C:\Program Files\Amazon Corretto"),
        Path(r"C:\Program Files\BellSoft"),
    ]
    candidates: list[Path] = []
    for root in roots:
        if not root.exists():
            continue
        for p in root.glob("**/bin/java.exe"):
            if "17" in str(p):
                candidates.insert(0, p)
            else:
                candidates.append(p)
    candidates = [
        Path(r"C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\java.exe"),
        Path(r"C:\Program Files\Eclipse Adoptium\jre-17.0.10.7-hotspot\bin\java.exe"),
        Path(r"C:\Program Files\Java\jdk-17\bin\java.exe"),
        Path(r"C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot\bin\java.exe"),
        Path(r"C:\Program Files\Zulu\zulu-17\bin\java.exe"),
    ] + candidates

    seen = set()
    for c in candidates:
        s = str(c)
        if s in seen:
            continue
        seen.add(s)
        hit = ok(s)
        if hit:
            _JAVA_CACHE = hit
            return hit

    hit = ok("java")
    if hit:
        _JAVA_CACHE = hit
        return hit
    return None


def ensure_launcher_profiles(game_dir: Path) -> None:
    """Forge --installClient refuses to run without launcher_profiles.json."""
    game_dir.mkdir(parents=True, exist_ok=True)
    profiles = game_dir / "launcher_profiles.json"
    if not profiles.exists():
        data = {
            "profiles": {
                "AquaTech": {
                    "name": "AquaTech",
                    "type": "custom",
                    "created": "2024-01-01T00:00:00.000Z",
                    "lastUsed": "2024-01-01T00:00:00.000Z",
                    "icon": "Furnace",
                    "lastVersionId": "1.20.1",
                    "gameDir": str(game_dir),
                }
            },
            "settings": {
                "enableSnapshots": False,
                "enableAdvanced": False,
                "keepLauncherOpen": False,
                "showGameLog": False,
            },
            "version": 3,
        }
        profiles.write_text(json.dumps(data, indent=2), encoding="utf-8")

    # Newer Forge installers also check the MS Store profile name
    ms_profiles = game_dir / "launcher_profiles_microsoft_store.json"
    if not ms_profiles.exists():
        ms_profiles.write_text(
            json.dumps({"profiles": {}, "settings": {}, "version": 3}, indent=2),
            encoding="utf-8",
        )


def find_forge_json(game_dir: Path):
    """Find the Forge version JSON after installation."""
    v_dir = game_dir / "versions"
    if not v_dir.exists(): return None, None
    for d in sorted(v_dir.iterdir(), reverse=True):
        if "forge" in d.name.lower() and "1.20.1" in d.name:
            j = d / f"{d.name}.json"
            if j.exists(): return j, d.name
    return None, None


def _http_download(url: str, dest: Path, timeout: int = 90) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    tmp = dest.with_suffix(dest.suffix + ".part")
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Mozilla/5.0 AquaTechLauncher/2.7",
            "Accept": "*/*",
            "Connection": "close",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp, open(tmp, "wb") as f:
            while True:
                chunk = resp.read(262144)
                if not chunk:
                    break
                f.write(chunk)
        tmp.replace(dest)
    except Exception:
        tmp.unlink(missing_ok=True)
        raise


def _http_download_mirrored(url: str, dest: Path, timeout: int = 90) -> None:
    last = None
    for u in _maven_mirror_urls(url):
        try:
            _http_download(u, dest, timeout=timeout)
            if dest.exists() and dest.stat().st_size > 0:
                return
        except Exception as e:
            last = e
            dest.unlink(missing_ok=True)
    raise RuntimeError(last or "download failed")


def prefetch_version_libraries(game_dir: Path, ver: dict, log=None, workers: int = LIB_WORKERS) -> tuple[int, int]:
    """Download missing Forge/MC libraries in parallel via mirrors. Returns (ok, fail)."""
    libs_dir = game_dir / "libraries"
    jobs = []
    for lib in ver.get("libraries") or []:
        if not _lib_allowed_on_windows(lib):
            continue
        art = (lib.get("downloads") or {}).get("artifact") or {}
        rel = art.get("path") or _artifact_path(lib)
        url = art.get("url")
        if not rel or not url:
            continue
        path = libs_dir / rel.replace("/", os.sep)
        expect = int(art.get("size") or 0)
        if path.exists() and (not expect or path.stat().st_size == expect or path.stat().st_size > 1000):
            continue
        jobs.append((url, path, expect))

    if not jobs:
        return 0, 0

    if log:
        log(f"⚡ Качаем библиотеки Forge параллельно ({len(jobs)} файлов)…")

    ok = fail = 0
    done = [0]

    def one(job):
        url, path, expect = job
        try:
            _http_download_mirrored(url, path)
            return True
        except Exception:
            path.unlink(missing_ok=True)
            return False

    with ThreadPoolExecutor(max_workers=workers) as pool:
        futs = [pool.submit(one, j) for j in jobs]
        for fut in as_completed(futs):
            if fut.result():
                ok += 1
            else:
                fail += 1
            done[0] += 1
            if log and done[0] % 5 == 0:
                log(f"   libs {done[0]}/{len(jobs)}")

    return ok, fail


def _forge_lang_provider_specs() -> list[dict]:
    """Maven artifacts for FML language providers (required on client classpath)."""
    specs = []
    for name in FORGE_LANG_PROVIDERS:
        rel = f"net/minecraftforge/{name}/{MC_VER}-{FORGE_VER}/{name}-{MC_VER}-{FORGE_VER}.jar"
        specs.append({
            "name": f"net.minecraftforge:{name}:{MC_VER}-{FORGE_VER}",
            "downloads": {
                "artifact": {
                    "path": rel,
                    "url": f"https://maven.minecraftforge.net/{rel}",
                }
            },
        })
    return specs


def ensure_forge_language_providers(game_dir: Path, log=None) -> list[str]:
    """Download/ensure fmlcore + language provider jars; return classpath paths.

    The slim Forge client profile omits these, but BootstrapLauncher/FML need them
    or mod locators stay null and Minecraft exits during early discovery.
    """
    libs_dir = game_dir / "libraries"
    paths: list[str] = []
    jobs: list[tuple[str, Path]] = []

    for spec in _forge_lang_provider_specs():
        art = spec["downloads"]["artifact"]
        rel = art["path"]
        path = libs_dir / rel.replace("/", os.sep)
        paths.append(str(path))
        if path.exists() and path.stat().st_size > 100:
            continue
        jobs.append((art["url"], path))

    def one(job):
        url, path = job
        try:
            _http_download_mirrored(url, path, timeout=60)
            return path.exists() and path.stat().st_size > 100
        except Exception as e:
            if log:
                log(f"⚠️  {path.name}: {e}")
            path.unlink(missing_ok=True)
            return False

    if jobs:
        if log:
            log(f"⚡ FML language providers: {len(jobs)}…")
        with ThreadPoolExecutor(max_workers=min(4, max(1, len(jobs)))) as pool:
            list(pool.map(one, jobs))

    missing = [p for p in paths if not Path(p).exists()]
    if missing:
        raise FileNotFoundError(
            "Нет FML language providers (fmlcore/javafmllanguage/…). "
            "Без них игра сразу закрывается. Проверь интернет и нажми Играть ещё раз."
        )
    return paths


def patch_forge_version_json_lang_providers(ver_json: Path) -> None:
    """Ensure version.json libraries list includes language providers."""
    if not ver_json.exists():
        return
    try:
        ver = json.loads(ver_json.read_text(encoding="utf-8"))
    except Exception:
        return
    libs = ver.setdefault("libraries", [])
    have = {lib.get("name") for lib in libs}
    changed = False
    for spec in _forge_lang_provider_specs():
        if spec["name"] in have:
            continue
        libs.append(spec)
        changed = True
    if changed:
        ver_json.write_text(json.dumps(ver, indent=2), encoding="utf-8")


def _minecraft_client_lib_dir(game_dir: Path) -> Path:
    """libraries/net/minecraft/client/1.20.1-<mcp>/ — produced by Forge installer processors."""
    return (
        game_dir / "libraries" / "net" / "minecraft" / "client"
        / f"{MC_VER}-{MCP_VER}"
    )


def ensure_forge_minecraft_srg(game_dir: Path, log=None) -> None:
    """Ensure deobfuscated Minecraft client (srg) + extra jars exist.

    Fast Forge install used to ship only forge-*-client.jar (patch layer). Without
    client-*-srg.jar the game dies right after 'Launching target forgeclient' with:
      ClassNotFoundException: net.minecraft.client.gui.screens.Overlay
    FML auto-loads these jars from libraries/net/minecraft/client/<mcp>/ when present.
    """
    lib = _minecraft_client_lib_dir(game_dir)
    srg = lib / f"client-{MC_VER}-{MCP_VER}-srg.jar"
    extra = lib / f"client-{MC_VER}-{MCP_VER}-extra.jar"
    if srg.exists() and srg.stat().st_size > 1_000_000 and extra.exists() and extra.stat().st_size > 100_000:
        return

    rt_zip = find_bundled_forge_runtime_zip()
    if not rt_zip:
        raise FileNotFoundError(
            "Нет client-*-srg.jar (полный Minecraft для Forge). Переустанови лаунчер 2.7.5+."
        )
    if log:
        log("⚡ Достаём Minecraft SRG/extra из Forge runtime…")
    lib.mkdir(parents=True, exist_ok=True)
    need = {
        f"libraries/net/minecraft/client/{MC_VER}-{MCP_VER}/client-{MC_VER}-{MCP_VER}-srg.jar": srg,
        f"libraries/net/minecraft/client/{MC_VER}-{MCP_VER}/client-{MC_VER}-{MCP_VER}-extra.jar": extra,
    }
    with zipfile.ZipFile(rt_zip, "r") as zf:
        names = set(zf.namelist())
        for arc, dest in need.items():
            if dest.exists() and dest.stat().st_size > 100_000:
                continue
            if arc not in names:
                # try forward/back slash variants
                alt = arc.replace("/", "\\")
                if alt in names:
                    arc = alt
                else:
                    raise FileNotFoundError(f"В runtime zip нет {arc}")
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(zf.read(arc))
    if not srg.exists() or srg.stat().st_size < 1_000_000:
        raise FileNotFoundError("client-*-srg.jar не установился — игра сразу закроется.")


def install_forge_fast(game_dir: Path, log=None) -> bool:
    """Install Forge without running the slow official installer processors.

    Uses prebuilt client/universal jars (embedded zip) + parallel library downloads.
    """
    rt_zip = find_bundled_forge_runtime_zip()
    if not rt_zip:
        return False

    if log:
        log(f"⚡ Быстрая установка Forge (без патчинга, ~{round(rt_zip.stat().st_size/1024/1024,1)} МБ)…")

    try:
        with zipfile.ZipFile(rt_zip, "r") as zf:
            zf.extractall(game_dir)
    except Exception as e:
        if log:
            log(f"⚠️  Не удалось распаковать Forge runtime: {e}")
        return False

    ver_json = game_dir / "versions" / FORGE_VER_ID / f"{FORGE_VER_ID}.json"
    client_jar = (
        game_dir / "libraries" / "net" / "minecraftforge" / "forge"
        / f"{MC_VER}-{FORGE_VER}" / f"forge-{MC_VER}-{FORGE_VER}-client.jar"
    )
    uni_jar = (
        game_dir / "libraries" / "net" / "minecraftforge" / "forge"
        / f"{MC_VER}-{FORGE_VER}" / f"forge-{MC_VER}-{FORGE_VER}-universal.jar"
    )
    if not ver_json.exists() or not client_jar.exists() or not uni_jar.exists():
        if log:
            log("⚠️  Forge runtime zip неполный")
        return False

    try:
        ensure_forge_minecraft_srg(game_dir, log=log)
    except Exception as e:
        if log:
            log(f"⚠️  Minecraft SRG: {e}")
        return False

    patch_forge_version_json_lang_providers(ver_json)
    try:
        ensure_forge_language_providers(game_dir, log=log)
    except Exception as e:
        if log:
            log(f"⚠️  FML providers: {e}")

    ver = json.loads(ver_json.read_text(encoding="utf-8"))
    ok, fail = prefetch_version_libraries(game_dir, ver, log=log)
    if log:
        log(f"✓ libs: +{ok}" + (f", ошибок {fail}" if fail else ""))

    # Point version jar at vanilla client (Forge ignoreList uses the named jar)
    ensure_vanilla_client_jar(game_dir, log=log)
    vanilla = game_dir / "versions" / MC_VER / f"{MC_VER}.jar"
    version_jar = game_dir / "versions" / FORGE_VER_ID / f"{FORGE_VER_ID}.jar"
    if vanilla.exists() and (not version_jar.exists() or version_jar.stat().st_size < 1_000_000):
        shutil.copy2(vanilla, version_jar)

    return find_forge_json(game_dir)[0] is not None


def ensure_vanilla_version_json(game_dir: Path) -> Path:
    """Forge inheritsFrom 1.20.1 — need the vanilla version JSON for libs/assets/args."""
    path = game_dir / "versions" / MC_VER / f"{MC_VER}.json"
    if path.exists() and path.stat().st_size > 1000:
        return path
    path.parent.mkdir(parents=True, exist_ok=True)
    manifest = _http_json("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
    entry = next(v for v in manifest["versions"] if v["id"] == MC_VER)
    data = urllib.request.urlopen(
        urllib.request.Request(entry["url"], headers={"User-Agent": "Mozilla/5.0 AquaTechLauncher/2.5"}),
        timeout=60,
    ).read()
    path.write_bytes(data)
    return path


def ensure_vanilla_client_jar(game_dir: Path, log=None) -> Path:
    """Download official Minecraft 1.20.1 client.jar from Mojang (no CurseForge)."""
    path = game_dir / "versions" / MC_VER / f"{MC_VER}.jar"
    if path.exists() and path.stat().st_size > 10_000_000:
        return path
    ver_json_path = ensure_vanilla_version_json(game_dir)
    ver = json.loads(ver_json_path.read_text(encoding="utf-8"))
    client = ((ver.get("downloads") or {}).get("client") or {})
    url = client.get("url")
    if not url:
        raise RuntimeError("В version JSON нет downloads.client — не скачать Minecraft")
    if log:
        size_mb = round(int(client.get("size") or 0) / 1024 / 1024, 1)
        log(f"📥 Скачиваем Minecraft {MC_VER} (~{size_mb} МБ)…")
    # BMCLAPI mirror first (often faster), then Mojang
    urls = [
        f"https://bmclapi2.bangbang93.com/version/{MC_VER}/client",
        url,
    ]
    last_err = None
    for u in urls:
        try:
            _http_download(u, path)
            if path.exists() and path.stat().st_size > 10_000_000:
                if log:
                    log(f"✓ Minecraft {MC_VER}.jar готов")
                return path
            path.unlink(missing_ok=True)
            last_err = RuntimeError("файл слишком маленький")
        except Exception as e:
            last_err = e
            path.unlink(missing_ok=True)
    raise RuntimeError(f"Не удалось скачать Minecraft {MC_VER}: {last_err}")


def load_merged_version(game_dir: Path, ver_id: str) -> dict:
    """Merge Forge JSON with inheritsFrom parent (vanilla)."""
    path = game_dir / "versions" / ver_id / f"{ver_id}.json"
    child = json.loads(path.read_text(encoding="utf-8"))
    parent = {}
    inherits = child.get("inheritsFrom")
    if inherits:
        ensure_vanilla_version_json(game_dir)
        ppath = game_dir / "versions" / inherits / f"{inherits}.json"
        parent = json.loads(ppath.read_text(encoding="utf-8"))

    merged = dict(parent)
    merged.update({k: v for k, v in child.items() if v is not None and k not in ("arguments", "libraries")})

    # libraries: parent first, then child
    libs = []
    seen = set()
    for lib in list(parent.get("libraries", [])) + list(child.get("libraries", [])):
        name = lib.get("name")
        if not name or name in seen:
            continue
        seen.add(name)
        libs.append(lib)
    merged["libraries"] = libs

    # arguments: concatenate jvm/game from parent then child
    pargs = parent.get("arguments") or {}
    cargs = child.get("arguments") or {}
    merged["arguments"] = {
        "jvm": list(pargs.get("jvm", [])) + list(cargs.get("jvm", [])),
        "game": list(pargs.get("game", [])) + list(cargs.get("game", [])),
    }
    if child.get("mainClass"):
        merged["mainClass"] = child["mainClass"]
    if not merged.get("assetIndex") and parent.get("assetIndex"):
        merged["assetIndex"] = parent["assetIndex"]
    return merged


def _lib_allowed_on_windows(lib: dict) -> bool:
    name = lib.get("name", "")
    # On 64-bit Windows skip x86 / arm64 LWJGL natives
    if "natives-windows-arm64" in name or "natives-windows-x86" in name:
        return False
    rules = lib.get("rules")
    if not rules:
        return True
    allowed = False
    for rule in rules:
        action = rule.get("action") == "allow"
        osr = rule.get("os") or {}
        os_name = osr.get("name")
        if not os_name:
            allowed = action
        elif os_name == "windows":
            allowed = action
    return allowed


def _artifact_path(lib: dict) -> str | None:
    art = (lib.get("downloads") or {}).get("artifact")
    if art and art.get("path"):
        return art["path"]
    # fallback from name group:name:ver
    name = lib.get("name", "")
    parts = name.split(":")
    if len(parts) < 3:
        return None
    group, artifact, version = parts[0], parts[1], parts[2]
    return f"{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.jar"


def ensure_libraries_and_natives(game_dir: Path, ver: dict, natives_dir: Path, log=None) -> list[str]:
    """Download missing jars in parallel, extract natives, return classpath entries."""
    libs_dir = game_dir / "libraries"
    natives_dir.mkdir(parents=True, exist_ok=True)
    cp: list[str] = []
    download_jobs: list[tuple[str, Path]] = []  # url, path
    native_extract: list[Path] = []

    for lib in ver.get("libraries", []):
        if not _lib_allowed_on_windows(lib):
            continue
        downloads = lib.get("downloads") or {}
        name = lib.get("name", "")
        parts = name.split(":")
        is_natives_artifact = len(parts) >= 4 and parts[3].startswith("natives")

        art = downloads.get("artifact")
        rel = _artifact_path(lib)
        if rel:
            path = libs_dir / rel.replace("/", os.sep)
            if not path.exists() and art and art.get("url"):
                download_jobs.append((art["url"], path))
            if is_natives_artifact:
                # Natives jars must be extracted, not put on -cp
                native_extract.append(path)
            elif path.exists() or (art and art.get("url")):
                cp.append(str(path))

        natives_map = lib.get("natives") or {}
        classifier = natives_map.get("windows") or natives_map.get("windows-x86_64")
        classifiers = downloads.get("classifiers") or {}
        if classifier and classifier in classifiers:
            nart = classifiers[classifier]
            nrel = nart.get("path")
            if nrel:
                npath = libs_dir / nrel.replace("/", os.sep)
                if not npath.exists() and nart.get("url"):
                    download_jobs.append((nart["url"], npath))
                native_extract.append(npath)

    # Deduplicate download jobs by path
    seen_paths: set[str] = set()
    uniq_jobs = []
    for url, path in download_jobs:
        key = str(path).lower()
        if key in seen_paths:
            continue
        seen_paths.add(key)
        uniq_jobs.append((url, path))

    if uniq_jobs:
        if log:
            log(f"⚡ Библиотеки: {len(uniq_jobs)} файлов × {LIB_WORKERS} потоков…")

        def one(job):
            url, path = job
            try:
                _http_download_mirrored(url, path, timeout=60)
                return True
            except Exception:
                path.unlink(missing_ok=True)
                return False

        ok = fail = 0
        with ThreadPoolExecutor(max_workers=LIB_WORKERS) as pool:
            for fut in as_completed([pool.submit(one, j) for j in uniq_jobs]):
                if fut.result():
                    ok += 1
                else:
                    fail += 1
        if log:
            log(f"✓ libs +{ok}" + (f", ошибок {fail}" if fail else ""))

    # Filter cp to existing only
    cp = [p for p in cp if Path(p).exists()]

    for npath in native_extract:
        if not npath.exists():
            continue
        try:
            with zipfile.ZipFile(npath, "r") as zf:
                for info in zf.infolist():
                    if info.is_dir():
                        continue
                    name = info.filename.replace("\\", "/")
                    if name.startswith("META-INF"):
                        continue
                    dest = natives_dir / Path(name).name
                    if not dest.exists():
                        dest.write_bytes(zf.read(info))
        except Exception:
            pass

    return cp


def ensure_assets(game_dir: Path, ver: dict, log=None) -> str:
    """Download Minecraft assets fast: parallel + BMCLAPI, critical first, rest in background."""
    assets_dir = game_dir / "assets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    index = ver.get("assetIndex") or {}
    index_id = index.get("id", "5")
    index_path = assets_dir / "indexes" / f"{index_id}.json"

    if not index_path.exists():
        # Check bundled asset index in launcher
        for cand in [
            _bundle_dir() / f"{index_id}.json",
            _app_dir() / f"{index_id}.json",
            Path(__file__).resolve().parent / f"{index_id}.json",
        ]:
            if cand.exists() and cand.stat().st_size > 100_000:
                index_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(cand, index_path)
                break

    if not index_path.exists():
        if log:
            log(f"📥 asset index {index_id}")
        urls = []
        if index.get("url"):
            urls.append(index["url"])
        urls.append(f"https://bmclapi2.bangbang93.com/assets/indexes/{index_id}.json")
        urls.append(f"https://piston-meta.mojang.com/v1/packages/5a5c8f53cee933b4ed2a27bdadbfb4bccfe3b397/{index_id}.json")
        urls.append(f"{GITHUB_RAW}/tools/{index_id}.json")
        last = None
        for u in urls:
            try:
                _http_download(u, index_path)
                if index_path.exists() and index_path.stat().st_size > 100_000:
                    break
            except Exception as e:
                last = e
                index_path.unlink(missing_ok=True)
        if not index_path.exists() and log:
            log(f"⚠️  asset index: {last}")

    objects_dir = assets_dir / "objects"
    objects_dir.mkdir(parents=True, exist_ok=True)

    if not index_path.exists():
        if log:
            log("⚠️  Нет asset index — игра может стартовать без звуков/иконок")
        return index_id

    # Warm path: skip scanning ~3k objects every launch (slow on HDD + Defender)
    ready_marker = assets_dir / "indexes" / f"{index_id}.aquatech_ready"
    if ready_marker.exists():
        if log:
            log(f"✓ assets готовы (кэш {index_id})")
        return index_id

    idx = json.loads(index_path.read_text(encoding="utf-8"))
    objects = idx.get("objects") or {}
    missing = []  # (name, hash, dest, expect)
    for name, meta in objects.items():
        h = meta["hash"]
        dest = objects_dir / h[:2] / h
        expect = int(meta.get("size") or 0)
        if not dest.exists() or (expect and dest.stat().st_size != expect):
            missing.append((name, h, dest, expect))

    if not missing:
        try:
            ready_marker.write_text(str(len(objects)), encoding="utf-8")
        except OSError:
            pass
        if log:
            log(f"✓ assets готовы ({len(objects)} объектов)")
        return index_id

    def _is_critical(name: str) -> bool:
        return name.startswith(ASSET_CRITICAL_PREFIXES)

    critical = [m for m in missing if _is_critical(m[0])]
    rest = [m for m in missing if not _is_critical(m[0])]

    def _download_one(item) -> bool:
        _name, h, dest, _expect = item
        tmp = dest.with_suffix(dest.suffix + ".part")
        for base in ASSET_MIRRORS:
            url = f"{base}/{h[:2]}/{h}"
            try:
                dest.parent.mkdir(parents=True, exist_ok=True)
                req = urllib.request.Request(
                    url,
                    headers={"User-Agent": "Mozilla/5.0 AquaTechLauncher/2.6"},
                )
                with urllib.request.urlopen(req, timeout=45) as resp, open(tmp, "wb") as f:
                    while True:
                        chunk = resp.read(262144)
                        if not chunk:
                            break
                        f.write(chunk)
                tmp.replace(dest)
                return True
            except Exception:
                try:
                    tmp.unlink(missing_ok=True)
                except Exception:
                    pass
                dest.unlink(missing_ok=True)
        return False

    def _download_batch(batch, label: str, progress_log=True) -> tuple[int, int]:
        if not batch:
            return 0, 0
        total = len(batch)
        use_log = log if progress_log else None
        if use_log:
            use_log(f"⚡ {label}: {total} файлов × {ASSET_WORKERS} потоков…")
        ok = fail = 0
        done = 0
        with ThreadPoolExecutor(max_workers=ASSET_WORKERS) as pool:
            futs = [pool.submit(_download_one, item) for item in batch]
            for fut in as_completed(futs):
                if fut.result():
                    ok += 1
                else:
                    fail += 1
                done += 1
                if use_log and (done % 200 == 0 or done == total):
                    use_log(f"   {label} {done}/{total}")
        return ok, fail

    # 1) Critical — wait (lang/icons), usually seconds
    c_ok, c_fail = _download_batch(critical, "assets (важные)")
    if log and critical:
        log(f"✓ важные assets: {c_ok}" + (f", ошибок {c_fail}" if c_fail else ""))

    # 2) Rest (mostly sounds) — background so Play isn't blocked for minutes
    if rest:
        if log:
            log(f"⚡ Остальные assets ({len(rest)}) качаются в фоне — можно играть")

        def _bg():
            try:
                # no UI log from this thread (tk not thread-safe)
                r_ok, r_fail = _download_batch(rest, "assets (фон)", progress_log=False)
                marker = assets_dir / ".aquatech_assets_bg.txt"
                marker.write_text(f"ok={r_ok} fail={r_fail}\n", encoding="utf-8")
                if r_fail == 0:
                    ready_marker.write_text(str(len(objects)), encoding="utf-8")
            except Exception as e:
                try:
                    (assets_dir / ".aquatech_assets_bg.txt").write_text(f"err={e}\n", encoding="utf-8")
                except Exception:
                    pass

        threading.Thread(target=_bg, daemon=True, name="AquaTechAssetsBG").start()
    else:
        try:
            ready_marker.write_text(str(len(objects)), encoding="utf-8")
        except OSError:
            pass
        if log:
            log("✓ assets скачаны")

    return index_id


def ensure_default_russian_options(game_dir: Path):
    """Ensure options.txt defaults to Russian language (ru_ru) on launcher start."""
    options_file = game_dir / "options.txt"
    if not options_file.exists():
        try:
            options_file.write_text("lang:ru_ru\n", encoding="utf-8")
        except OSError:
            pass
        return

    try:
        content = options_file.read_text(encoding="utf-8", errors="ignore")
        if "lang:" not in content:
            content += "\nlang:ru_ru\n"
            options_file.write_text(content, encoding="utf-8")
        elif "lang:en_us" in content:
            content = content.replace("lang:en_us", "lang:ru_ru")
            options_file.write_text(content, encoding="utf-8")
    except Exception:
        pass


def build_launch_cmd(
    game_dir: Path,
    username: str,
    ram_mb: int,
    java: str,
    log=None,
    *,
    auto_join: str | None = None,
) -> list[str]:
    """Build the full Forge launch command (merged inheritsFrom + natives + assets)."""
    ensure_default_russian_options(game_dir)
    ver_json, ver_id = find_forge_json(game_dir)
    if not ver_json:
        raise FileNotFoundError("Forge version JSON not found — run Forge install first")

    ensure_vanilla_version_json(game_dir)
    # Skip re-download if jar already present (warm start)
    vanilla_path = game_dir / "versions" / MC_VER / f"{MC_VER}.jar"
    if not (vanilla_path.exists() and vanilla_path.stat().st_size > 10_000_000):
        ensure_vanilla_client_jar(game_dir, log=log)
    ver = load_merged_version(game_dir, ver_id)

    natives_dir = game_dir / "versions" / ver_id / "natives"
    assets_dir = game_dir / "assets"
    libs_dir = game_dir / "libraries"

    # Assets (critical) + libraries in parallel
    asset_index = "5"
    cp_parts: list[str] = []
    with ThreadPoolExecutor(max_workers=2) as pool:
        fut_assets = pool.submit(ensure_assets, game_dir, ver, log)
        fut_libs = pool.submit(ensure_libraries_and_natives, game_dir, ver, natives_dir, log)
        asset_index = fut_assets.result()
        cp_parts = fut_libs.result()

    inherits = ver.get("inheritsFrom") or MC_VER
    vanilla_jar = game_dir / "versions" / inherits / f"{inherits}.jar"
    version_jar = game_dir / "versions" / ver_id / f"{ver_id}.jar"
    if not vanilla_jar.exists() or vanilla_jar.stat().st_size < 10_000_000:
        raise FileNotFoundError(f"Нет Minecraft {inherits}.jar — автозагрузка не удалась")
    if not version_jar.exists() or version_jar.stat().st_size < 1_000_000:
        shutil.copy2(vanilla_jar, version_jar)
    cp_parts.append(str(version_jar))

    forge_lib = game_dir / "libraries" / "net" / "minecraftforge" / "forge" / f"{MC_VER}-{FORGE_VER}"
    for name in (f"forge-{MC_VER}-{FORGE_VER}-client.jar", f"forge-{MC_VER}-{FORGE_VER}-universal.jar"):
        fjar = forge_lib / name
        if fjar.exists():
            cp_parts.append(str(fjar))

    # Critical: deobfuscated Minecraft (srg) — without it: ClassNotFoundException Overlay
    ensure_forge_minecraft_srg(game_dir, log=log)

    # Critical: fmlcore + language providers (often missing from slim forge json)
    ver_json_path = game_dir / "versions" / ver_id / f"{ver_id}.json"
    patch_forge_version_json_lang_providers(ver_json_path)
    for p in ensure_forge_language_providers(game_dir, log=log):
        cp_parts.append(p)

    # Deduplicate classpath while preserving order
    seen_cp = set()
    cp_unique = []
    for p in cp_parts:
        key = p.lower()
        if key in seen_cp:
            continue
        # Skip raw vanilla jar if somehow included
        if p.replace("\\", "/").endswith(f"/versions/{inherits}/{inherits}.jar"):
            continue
        # Never put LWJGL natives jars on -cp
        if "natives-windows" in key or "natives-linux" in key or "natives-macos" in key:
            continue
        seen_cp.add(key)
        cp_unique.append(p)
    cp_parts = cp_unique

    cp = os.pathsep.join(cp_parts)
    main_class = ver.get("mainClass") or "cpw.mods.bootstraplauncher.BootstrapLauncher"

    def _expand(arg: str) -> str:
        return (arg
            .replace("${natives_directory}", str(natives_dir))
            .replace("${launcher_name}", "AquaTechLauncher")
            .replace("${launcher_version}", LAUNCHER_VER)
            .replace("${classpath}", cp)
            .replace("${library_directory}", str(libs_dir))
            .replace("${classpath_separator}", os.pathsep)
            .replace("${auth_player_name}", username)
            .replace("${version_name}", ver_id)
            .replace("${game_directory}", str(game_dir))
            .replace("${assets_root}", str(assets_dir))
            .replace("${assets_index_name}", asset_index)
            .replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000")
            .replace("${auth_access_token}", "0")
            .replace("${clientid}", "0")
            .replace("${auth_xuid}", "0")
            .replace("${user_type}", "legacy")
            .replace("${version_type}", "release")
            .replace("${resolution_width}", "1280")
            .replace("${resolution_height}", "720")
        )

    def _flatten_args(items) -> list[str]:
        out = []
        for arg in items:
            if isinstance(arg, str):
                expanded = _expand(arg)
                # Drop still-unresolved placeholders (feature args etc.)
                if "${" in expanded:
                    # Also drop the previous token if it was a dangling flag like -cp
                    if out and out[-1] in ("-cp", "-classpath", "-p") and not expanded:
                        out.pop()
                    continue
                out.append(expanded)
            elif isinstance(arg, dict):
                rules = arg.get("rules") or []
                # Any feature-gated argument (demo, quickPlay, custom res) → skip
                if any((rule.get("features") or {}) for rule in rules):
                    continue
                allow = True
                for rule in rules:
                    osr = (rule.get("os") or {}).get("name")
                    if osr and osr != "windows" and rule.get("action") == "allow":
                        allow = False
                    if osr == "windows" and rule.get("action") == "disallow":
                        allow = False
                if not allow:
                    continue
                val = arg.get("value")
                if isinstance(val, str):
                    expanded = _expand(val)
                    if "${" not in expanded:
                        out.append(expanded)
                elif isinstance(val, list):
                    for v in val:
                        if not isinstance(v, str):
                            continue
                        expanded = _expand(v)
                        if "${" not in expanded:
                            out.append(expanded)
        # Remove dangling -cp/-p without following value
        cleaned = []
        i = 0
        while i < len(out):
            if out[i] in ("-cp", "-classpath", "-p"):
                if i + 1 < len(out) and not out[i + 1].startswith("-"):
                    cleaned.append(out[i])
                    cleaned.append(out[i + 1])
                    i += 2
                    continue
                i += 1
                continue
            cleaned.append(out[i])
            i += 1
        return cleaned

    jvm_args = _flatten_args(ver.get("arguments", {}).get("jvm", []))
    game_args = _flatten_args(ver.get("arguments", {}).get("game", []))

    # Guaranteed vanilla identity args if merge missed them
    needed = {
        "--username": username,
        "--version": ver_id,
        "--gameDir": str(game_dir),
        "--assetsDir": str(assets_dir),
        "--assetIndex": asset_index,
        "--uuid": "00000000-0000-0000-0000-000000000000",
        "--accessToken": "0",
        "--userType": "legacy",
        "--versionType": "release",
    }
    for flag, val in needed.items():
        if flag not in game_args:
            game_args.extend([flag, val])

    join_target = (auto_join or "").strip()
    if not join_target:
        join_target = f"{SERVER_IP}:{SERVER_PORT}"
    if join_target and "--quickPlayMultiplayer" not in game_args:
        game_args.extend(["--quickPlayMultiplayer", join_target])
        if log:
            log(f"→ авто-вход на {join_target}")

    java_exec = java
    # Prefer javaw.exe — no black console window for players
    if java_exec.lower().endswith("java.exe") and not java_exec.lower().endswith("javaw.exe"):
        cand = str(Path(java_exec).with_name("javaw.exe"))
        if Path(cand).exists():
            java_exec = cand

    # Sanity: natives must exist or LWJGL crashes instantly
    if not any(natives_dir.glob("*.dll")):
        if log:
            log("⚠️  natives пустые — перекачаем LWJGL…")
        # force re-extract by clearing and re-running libs once
        ensure_libraries_and_natives(game_dir, ver, natives_dir, log=log)
        if not any(natives_dir.glob("*.dll")):
            raise FileNotFoundError(
                f"Нет LWJGL natives в {natives_dir} — игра сразу закроется. "
                "Проверь интернет и нажми Играть ещё раз."
            )

    forge_client = forge_lib / f"forge-{MC_VER}-{FORGE_VER}-client.jar"
    forge_uni = forge_lib / f"forge-{MC_VER}-{FORGE_VER}-universal.jar"
    if not forge_client.exists() or not forge_uni.exists():
        raise FileNotFoundError(
            "Нет forge-client/universal.jar — переустанови Forge (удали versions/*forge* и нажми Играть)."
        )

    cmd = [
        java_exec,
        f"-Xmx{ram_mb}M",
        f"-Xms{min(ram_mb, 2048)}M",
        "-XX:+UseG1GC",
        "-XX:+ParallelRefProcEnabled",
        "-XX:MaxGCPauseMillis=200",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+DisableExplicitGC",
        "-XX:G1NewSizePercent=20",
        "-XX:G1ReservePercent=20",
        "-XX:G1HeapRegionSize=32M",
        "-Dlog4j2.formatMsgNoLookups=true",
        "-Djava.net.preferIPv4Stack=true",
        "-Dforge.logging.console.level=info",
        f"-XX:ErrorFile={game_dir / 'logs' / 'hs_err_pid%p.log'}",
    ] + jvm_args + [main_class] + game_args

    return cmd


# Keep open Minecraft log handles so GC does not close them while the game runs.
_MC_LOG_HANDLES: list = []


def spawn_minecraft(cmd: list[str], game_dir: Path) -> subprocess.Popen:
    """Start Minecraft in its own process group with console capture.

    stdout/stderr go to logs/minecraft_console.log (handle kept open in this
    process). Launcher must stay open or exit only after the game is stable —
    closing inherited log handles on Windows can kill the JVM.
    """
    log_dir = game_dir / "logs"
    log_dir.mkdir(parents=True, exist_ok=True)
    try:
        (log_dir / "last_launch_cmd.txt").write_text(
            "\n".join(cmd), encoding="utf-8", errors="replace"
        )
    except OSError:
        pass

    console_path = log_dir / "minecraft_console.log"
    # binary + line buffering via Text; keep handle alive
    console = open(console_path, "ab", buffering=0)
    _MC_LOG_HANDLES.append(console)
    try:
        console.write(f"\n===== AquaTech launch {time.strftime('%Y-%m-%d %H:%M:%S')} =====\n".encode("utf-8", "replace"))
    except OSError:
        pass

    flags = 0
    if os.name == "nt":
        # New process group + no console window for smooth seamless launch
        flags = 0x00000200 | 0x08000000  # CREATE_NEW_PROCESS_GROUP | CREATE_NO_WINDOW

    return subprocess.Popen(
        cmd,
        cwd=str(game_dir),
        stdout=console,
        stderr=console,
        stdin=subprocess.DEVNULL,
        creationflags=flags,
        close_fds=False if os.name == "nt" else True,
    )


# ─── Launcher GUI ─────────────────────────────────────────────────────────────
class AquaTechLauncher(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(f"AquaTech  ·  {LAUNCHER_VER}")
        self.geometry(f"{WIN_W}x{WIN_H}")
        self.minsize(WIN_W, WIN_H)
        self.resizable(False, False)
        self.configure(bg=C_BG)
        try:
            self.attributes("-alpha", 0.0)
        except tk.TclError:
            pass

        self._cfg = self._load_cfg()
        self._running = False
        self._prog_value = 0.0
        self._prog_display = 0.0
        self._banner_t = 0.0
        self._bubbles: list[dict] = []
        self._page = "play"
        self._nav: dict[str, NavItem] = {}
        self._pages: dict[str, tk.Frame] = {}
        self._apply_window_icon()

        self._build()
        self.after(20, self._fade_in)
        self.after(40, self._anim_banner)
        self.after(40, self._anim_progress)

    def _apply_window_icon(self):
        """Taskbar / title-bar icon (exe icon is set via PyInstaller .ico)."""
        candidates = [
            _bundle_dir() / "aquatech.ico",
            _app_dir() / "aquatech.ico",
            Path(__file__).resolve().parent / "aquatech.ico",
        ]
        for p in candidates:
            if p.is_file():
                try:
                    self.iconbitmap(default=str(p))
                    return
                except tk.TclError:
                    try:
                        self.iconbitmap(str(p))
                        return
                    except tk.TclError:
                        pass

    # ── Config ─────────────────────────────────────────────────────────────
    def _load_cfg(self):
        cfg = {"username": "", "game_dir": str(GAME_DIR), "ram_mb": 4096, "update_url": DEFAULT_UPDATE_URL}
        try:
            if CONFIG_PATH.exists():
                cfg.update(json.loads(CONFIG_PATH.read_text("utf-8")))
        except Exception:
            pass
        cfg["update_url"] = normalize_update_url(cfg.get("update_url"))
        # Drop legacy sync_url pointing at Playit tunnels
        if "sync_url" in cfg and ("tun.ply.gg" in str(cfg.get("sync_url") or "").lower() or "playit" in str(cfg.get("sync_url") or "").lower()):
            cfg.pop("sync_url", None)
        return cfg

    def _save_cfg(self):
        self._cfg["username"] = self._e_nick.get().strip()
        self._cfg["game_dir"] = self._e_dir.get().strip()
        self._cfg["update_url"] = normalize_update_url(self._e_update.get().strip().rstrip("/"))
        try:
            self._cfg["ram_mb"] = int(self._e_ram.get().replace("MB", "").replace("GB", "000").strip())
        except Exception:
            pass
        try:
            CONFIG_PATH.write_text(json.dumps(self._cfg, indent=2), "utf-8")
        except Exception:
            pass
        # Keep UI field in sync after migration
        try:
            self._e_update.delete(0, "end")
            self._e_update.insert(0, self._cfg.get("update_url", "") or "")
        except Exception:
            pass

    # ── Animations ─────────────────────────────────────────────────────────
    def _fade_in(self, step: int = 0):
        alpha = min(1.0, step / 14)
        try:
            self.attributes("-alpha", alpha)
        except tk.TclError:
            return
        if alpha < 1.0:
            self.after(18, lambda: self._fade_in(step + 1))

    def _anim_banner(self):
        if not hasattr(self, "_banner"):
            return
        self._banner_t += 0.032
        self._draw_banner()
        self.after(33, self._anim_banner)

    def _anim_progress(self):
        diff = self._prog_value - self._prog_display
        if abs(diff) > 0.05:
            self._prog_display += diff * 0.18
            self._paint_progress()
        elif abs(diff) > 0:
            self._prog_display = self._prog_value
            self._paint_progress()
        self.after(16, self._anim_progress)

    def _draw_banner(self):
        c = self._banner
        c.delete("anim")
        w = max(c.winfo_width(), WIN_W - SIDE_W)
        h = HERO_H
        t = self._banner_t
        for i, amp in enumerate((7.0, 4.5, 2.5)):
            pts = []
            y0 = h * 0.62 + i * 14
            col = _lerp_color(C_ACCENT, C_PANEL, 0.68 + i * 0.08)
            for x in range(0, w + 10, 10):
                y = y0 + math.sin(x * 0.011 + t + i) * amp + math.sin(x * 0.004 - t * 0.55) * (amp * 0.45)
                pts.extend([x, y])
            if len(pts) >= 4:
                c.create_line(*pts, fill=col, width=1, smooth=True, tags="anim")
        if not self._bubbles:
            random.seed(11)
            for _ in range(18):
                self._bubbles.append({
                    "x": random.uniform(20, max(40, w - 20)),
                    "y": random.uniform(24, h - 30),
                    "r": random.uniform(1.2, 3.2),
                    "sp": random.uniform(0.12, 0.4),
                    "ph": random.uniform(0, 6.28),
                })
        for b in self._bubbles:
            bx = (b["x"] + math.sin(t * 0.7 + b["ph"]) * 8) % max(w, 1)
            by = (b["y"] - t * b["sp"] * 10) % (h - 10)
            a = 0.22 + 0.4 * (0.5 + 0.5 * math.sin(t + b["ph"]))
            col = _lerp_color(C_ACCENT, C_PANEL, 1.0 - a * 0.55)
            c.create_oval(bx - b["r"], by - b["r"], bx + b["r"], by + b["r"],
                          fill=col, outline="", tags="anim")

    def _field(self, parent, label: str, bg: str = C_CARD) -> tk.Entry:
        wrap = tk.Frame(parent, bg=bg)
        wrap.pack(fill="x", pady=(0, 14))
        tk.Label(wrap, text=label.upper(), font=FONT_LABEL, fg=C_DIM, bg=bg,
                 anchor="w").pack(fill="x", pady=(0, 5))
        shell = tk.Frame(wrap, bg=C_BORDER, padx=1, pady=1)
        shell.pack(fill="x")
        inner = tk.Frame(shell, bg=C_FIELD)
        inner.pack(fill="x")
        e = tk.Entry(
            inner, font=FONT_ENTRY, bg=C_FIELD, fg=C_TEXT,
            insertbackground=C_ACCENT, relief="flat", bd=0,
            highlightthickness=0,
        )
        e.pack(fill="x", ipady=10, padx=12)

        def focus_in(_):
            shell.configure(bg=C_ACCENT)
            inner.configure(bg=C_FIELD_F)
            e.configure(bg=C_FIELD_F)

        def focus_out(_):
            shell.configure(bg=C_BORDER)
            inner.configure(bg=C_FIELD)
            e.configure(bg=C_FIELD)

        e.bind("<FocusIn>", focus_in)
        e.bind("<FocusOut>", focus_out)
        return e

    def _show_page(self, name: str):
        self._page = name
        for key, item in self._nav.items():
            item.set_active(key == name)
        for key, frame in self._pages.items():
            if key == name:
                frame.tkraise()

    # ── UI Build ───────────────────────────────────────────────────────────
    def _build(self):
        root = tk.Frame(self, bg=C_BG)
        root.pack(fill="both", expand=True)

        # ── Sidebar
        side = tk.Frame(root, bg=C_SIDE, width=SIDE_W)
        side.pack(side="left", fill="y")
        side.pack_propagate(False)

        brand = tk.Frame(side, bg=C_SIDE)
        brand.pack(fill="x", padx=18, pady=(22, 8))
        tk.Label(brand, text="AQUATECH", font=("Segoe UI Semibold", 16),
                 fg=C_TEXT, bg=C_SIDE, anchor="w").pack(fill="x")
        tk.Label(brand, text="Launcher", font=FONT_SUB,
                 fg=C_DIM, bg=C_SIDE, anchor="w").pack(fill="x", pady=(2, 0))

        chip = tk.Frame(side, bg=C_NAV_ACT)
        chip.pack(fill="x", padx=14, pady=(10, 18))
        tk.Label(chip, text=f"  {SERVER_IP}", font=FONT_CHIP,
                 fg=C_ACCENT, bg=C_NAV_ACT, anchor="w", pady=7).pack(fill="x")

        nav_wrap = tk.Frame(side, bg=C_SIDE)
        nav_wrap.pack(fill="x", padx=10)
        for key, label in (("play", "Игра"), ("settings", "Настройки"), ("log", "Активность")):
            item = NavItem(nav_wrap, label, command=lambda k=key: self._show_page(k))
            item.pack(fill="x", pady=2)
            self._nav[key] = item

        foot = tk.Frame(side, bg=C_SIDE)
        foot.pack(side="bottom", fill="x", padx=18, pady=16)
        tk.Label(foot, text=f"v{LAUNCHER_VER}", font=FONT_CHIP,
                 fg=C_DIM, bg=C_SIDE, anchor="w").pack(fill="x")
        tk.Label(foot, text=f"Minecraft {MC_VER}", font=FONT_CHIP,
                 fg=C_DIM, bg=C_SIDE, anchor="w").pack(fill="x")

        # ── Main stack
        main = tk.Frame(root, bg=C_BG)
        main.pack(side="left", fill="both", expand=True)
        stack = tk.Frame(main, bg=C_BG)
        stack.pack(fill="both", expand=True)
        stack.grid_rowconfigure(0, weight=1)
        stack.grid_columnconfigure(0, weight=1)

        page_play = tk.Frame(stack, bg=C_BG)
        page_set = tk.Frame(stack, bg=C_BG)
        page_log = tk.Frame(stack, bg=C_BG)
        for p in (page_play, page_set, page_log):
            p.grid(row=0, column=0, sticky="nsew")
        self._pages = {"play": page_play, "settings": page_set, "log": page_log}

        self._build_play(page_play)
        self._build_settings(page_set)
        self._build_log(page_log)
        self._show_page("play")

    def _build_play(self, parent: tk.Frame):
        # Hero
        self._banner = tk.Canvas(parent, bg=C_PANEL, height=HERO_H, highlightthickness=0, bd=0)
        self._banner.pack(fill="x")
        main_w = WIN_W - SIDE_W
        for i in range(12):
            col = _lerp_color("#0A1C28", C_PANEL, i / 11)
            self._banner.create_rectangle(0, int(HERO_H * i / 12), main_w, HERO_H, fill=col, outline="", tags="bg")
        # soft teal wash
        for i in range(6):
            col = _lerp_color(C_PANEL, C_ACCENT2, 0.04 + i * 0.02)
            self._banner.create_rectangle(0, HERO_H - 40 + i * 7, main_w, HERO_H, fill=col, outline="", tags="bg")
        self._banner.create_text(
            36, 78, text="AQUATECH", font=FONT_BRAND, fill=C_TEXT, anchor="w", tags="fg",
        )
        self._banner.create_text(
            38, 128,
            text=f"Minecraft {MC_VER}  ·  Forge и assets ставятся сами  ·  сервер {SERVER_IP}",
            font=FONT_SUB, fill=C_MUTED, anchor="w", tags="fg",
        )
        self._banner.create_line(36, HERO_H - 18, main_w - 36, HERO_H - 18, fill=C_LINE, width=1, tags="fg")

        body = tk.Frame(parent, bg=C_BG)
        body.pack(fill="both", expand=True, padx=36, pady=(22, 20))

        # Nick
        nick_wrap = tk.Frame(body, bg=C_BG)
        nick_wrap.pack(fill="x")
        self._e_nick = self._field(nick_wrap, "Никнейм", bg=C_BG)
        self._e_nick.insert(0, self._cfg.get("username", ""))

        # Actions
        btn_row = tk.Frame(body, bg=C_BG)
        btn_row.pack(fill="x", pady=(6, 0))
        self._btn = SoftButton(
            btn_row, "Играть", self._on_play, primary=True,
            height=56, radius=12, font=FONT_BTN_XL,
        )
        self._btn.pack(side="left", fill="x", expand=True, padx=(0, 12))
        self._btn_upd = SoftButton(
            btn_row, "Обновить", self._on_update, primary=False,
            height=56, radius=12, font=FONT_BTN,
        )
        self._btn_upd.pack(side="left", fill="x", expand=True)

        # Progress
        prog_frame = tk.Frame(body, bg=C_BG)
        prog_frame.pack(fill="x", pady=(22, 0))
        top = tk.Frame(prog_frame, bg=C_BG)
        top.pack(fill="x")
        self._status_lbl = tk.Label(
            top, text="Готов к запуску", font=FONT_STATUS, fg=C_MUTED, bg=C_BG, anchor="w",
        )
        self._status_lbl.pack(side="left")
        self._pct_lbl = tk.Label(
            top, text="0%", font=FONT_STATUS, fg=C_ACCENT, bg=C_BG, anchor="e",
        )
        self._pct_lbl.pack(side="right")
        self._prog_canvas = tk.Canvas(
            prog_frame, bg=C_PROG_BG, height=6, highlightthickness=0, bd=0,
        )
        self._prog_canvas.pack(fill="x", pady=(10, 0))

        tip = tk.Label(
            body,
            text="Подсказка: укажи URL обновлений во вкладке Настройки, чтобы тянуть моды со sync-сервера.",
            font=FONT_CHIP, fg=C_DIM, bg=C_BG, anchor="w", wraplength=680, justify="left",
        )
        tip.pack(fill="x", pady=(18, 0))

    def _build_settings(self, parent: tk.Frame):
        wrap = tk.Frame(parent, bg=C_BG)
        wrap.pack(fill="both", expand=True, padx=36, pady=28)

        tk.Label(wrap, text="Настройки", font=FONT_TITLE, fg=C_TEXT, bg=C_BG, anchor="w").pack(fill="x")
        tk.Label(
            wrap, text="Папка игры, память и источник обновлений сборки.",
            font=FONT_SUB, fg=C_MUTED, bg=C_BG, anchor="w",
        ).pack(fill="x", pady=(4, 22))

        card = tk.Frame(wrap, bg=C_CARD, padx=24, pady=22)
        card.pack(fill="x")

        self._e_ram = self._field(card, "Оперативка")
        self._e_ram.insert(0, f"{self._cfg.get('ram_mb', 4096)} MB")

        # Game dir with browse
        dwrap = tk.Frame(card, bg=C_CARD)
        dwrap.pack(fill="x", pady=(0, 14))
        tk.Label(dwrap, text="ПАПКА ИГРЫ", font=FONT_LABEL, fg=C_DIM, bg=C_CARD,
                 anchor="w").pack(fill="x", pady=(0, 5))
        row = tk.Frame(dwrap, bg=C_CARD)
        row.pack(fill="x")
        shell = tk.Frame(row, bg=C_BORDER, padx=1, pady=1)
        shell.pack(side="left", fill="x", expand=True)
        inner = tk.Frame(shell, bg=C_FIELD)
        inner.pack(fill="x")
        self._e_dir = tk.Entry(
            inner, font=("Segoe UI", 10), bg=C_FIELD, fg=C_TEXT,
            insertbackground=C_ACCENT, relief="flat", bd=0,
        )
        self._e_dir.insert(0, self._cfg.get("game_dir", str(GAME_DIR)))
        self._e_dir.pack(fill="x", ipady=10, padx=12)

        def browse():
            d = filedialog.askdirectory()
            if d:
                self._e_dir.delete(0, "end")
                self._e_dir.insert(0, d)

        SoftButton(row, "⋯", browse, primary=False, height=40, width=44).pack(side="left", padx=(8, 0))

        self._e_update = self._field(card, "URL обновлений")
        self._e_update.insert(0, self._cfg.get("update_url", "") or "")

        tk.Label(
            wrap,
            text="Манифест сборки: сайт AquaTech (моды качаются с GitHub Releases).",
            font=FONT_CHIP, fg=C_DIM, bg=C_BG, anchor="w",
        ).pack(fill="x", pady=(14, 0))

    def _build_log(self, parent: tk.Frame):
        wrap = tk.Frame(parent, bg=C_BG)
        wrap.pack(fill="both", expand=True, padx=36, pady=28)

        head = tk.Frame(wrap, bg=C_BG)
        head.pack(fill="x", pady=(0, 12))
        tk.Label(head, text="Активность", font=FONT_TITLE, fg=C_TEXT, bg=C_BG, anchor="w").pack(side="left")
        tk.Label(head, text="лог установки и синхронизации", font=FONT_SUB,
                 fg=C_DIM, bg=C_BG, anchor="e").pack(side="right")

        log_shell = tk.Frame(wrap, bg=C_BORDER, padx=1, pady=1)
        log_shell.pack(fill="both", expand=True)
        log_inner = tk.Frame(log_shell, bg=C_PANEL)
        log_inner.pack(fill="both", expand=True)
        self._log = tk.Text(
            log_inner, font=FONT_STEP, bg=C_PANEL, fg=C_MUTED,
            relief="flat", bd=0, state="disabled",
            selectbackground=C_BORDER, wrap="word", padx=14, pady=12,
        )
        self._log.pack(fill="both", expand=True)
        self._log.tag_config("ok", foreground=C_GREEN)
        self._log.tag_config("err", foreground=C_RED)
        self._log.tag_config("warn", foreground=C_WARN)
        self._log.tag_config("info", foreground=C_ACCENT)
        self._log.tag_config("dim", foreground=C_DIM)

    # ── Progress helpers ────────────────────────────────────────────────────
    def _paint_progress(self):
        if not hasattr(self, "_prog_canvas"):
            return
        w = max(self._prog_canvas.winfo_width(), 10)
        self._prog_canvas.delete("all")
        self._prog_canvas.create_rectangle(0, 0, w, 6, fill=C_PROG_BG, outline="")
        fill = int(w * self._prog_display / 100)
        if fill > 0:
            self._prog_canvas.create_rectangle(0, 0, fill, 6, fill=C_ACCENT, outline="")
            tip = min(w, fill + 14)
            for i in range(7):
                x = fill - 8 + i * 2
                if 0 < x < tip:
                    self._prog_canvas.create_rectangle(
                        x, 0, x + 2, 6,
                        fill=_lerp_color(C_ACCENT, C_ACCENT_H, i / 7),
                        outline="",
                    )
        if hasattr(self, "_pct_lbl"):
            self._pct_lbl.config(text=f"{int(self._prog_display)}%")

    def _set_pct(self, pct: float):
        self._prog_value = max(0.0, min(100.0, float(pct)))

    def _log_line(self, text: str, tag: str = "info"):
        def _ui():
            if hasattr(self, "_log"):
                self._log.config(state="normal")
                ts = time.strftime("%H:%M:%S")
                self._log.insert("end", f"{ts}  ", "dim")
                self._log.insert("end", f"{text}\n", tag)
                self._log.see("end")
                self._log.config(state="disabled")
            if hasattr(self, "_status_lbl"):
                self._status_lbl.config(text=text[:90])
        # Worker threads may call this — marshal to UI thread when needed
        try:
            if threading.current_thread() is threading.main_thread():
                _ui()
            else:
                self.after(0, _ui)
        except Exception:
            _ui()

    # ── Button ─────────────────────────────────────────────────────────────
    def _set_busy(self, busy: bool, play_text: str | None = None):
        if busy:
            self._btn.set_text(play_text or "Подготовка…")
            self._btn.set_enabled(False)
            self._btn_upd.set_enabled(False)
            self._btn_upd.set_text("Обновить")
        else:
            self._btn.set_text("Играть")
            self._btn.set_enabled(True)
            self._btn.set_colors(C_ACCENT2, C_ACCENT_H, "#061018")
            self._btn_upd.set_text("Обновить")
            self._btn_upd.set_enabled(True)
            self._btn_upd.set_colors(C_CARD, C_FIELD_F, C_TEXT)

    def _on_play(self):
        if self._running:
            return
        nick = self._e_nick.get().strip()
        if not nick:
            messagebox.showwarning("AquaTech", "Введи никнейм")
            self._show_page("play")
            return
        self._save_cfg()
        self._running = True
        self._set_busy(True)
        self._show_page("log")
        threading.Thread(target=self._worker, daemon=True).start()

    def _on_update(self):
        if self._running:
            return
        self._save_cfg()
        self._running = True
        self._set_busy(True, "Обновление…")
        self._show_page("log")
        threading.Thread(target=self._worker_update, daemon=True).start()

    def _done(self, ok: bool, close: bool = False):
        self._running = False
        if ok:
            self._set_busy(False)
            self._btn.set_text("В игре")
            # Never auto-close: exiting the launcher used to kill Minecraft on Windows
            # when stdout/stderr were redirected to parent-owned log files.
            if close:
                self.after(2500, self.destroy)
        else:
            self._btn.set_enabled(True)
            self._btn.set_text("Ошибка — ещё раз")
            self._btn.set_colors(C_RED, "#EC8888", "#FFFFFF")
            self._btn_upd.set_enabled(True)
            self._btn_upd.set_text("Обновить")
            self._btn_upd.set_colors(C_CARD, C_FIELD_F, C_TEXT)

    def _done_update(self, ok: bool):
        self._running = False
        self._set_busy(False)
        if ok:
            self._log_line("Сборка обновлена. Можно играть.", "ok")
        else:
            self._btn_upd.set_colors(C_RED, "#EC8888", "#FFFFFF")
            self._btn_upd.set_text("Ошибка обновления")

    # ── Worker ─────────────────────────────────────────────────────────────
    def _worker(self):
        try:
            game_dir = Path(self._cfg["game_dir"])
            username = self._cfg["username"]
            ram_mb = int(self._cfg.get("ram_mb", 4096))
            self._run_all(game_dir, username, ram_mb)
        except Exception as ex:
            self._log_line(f"Критическая ошибка: {ex}", "err")
            self.after(0, lambda: self._done(False))

    def _worker_update(self):
        try:
            game_dir = Path(self._cfg["game_dir"])
            for sub in ("mods", "config", "kubejs", "resourcepacks"):
                (game_dir / sub).mkdir(parents=True, exist_ok=True)
            self._log_line("Проверяем обновления сборки…", "info")
            self._sync_files(game_dir, prefer_remote=True)
            self._set_pct(100)
            self.after(0, lambda: self._done_update(True))
        except Exception as ex:
            self._log_line(f"Ошибка обновления: {ex}", "err")
            self.after(0, lambda: self._done_update(False))

    def _run_all(self, game_dir: Path, username: str, ram_mb: int):
        t0 = time.perf_counter()
        # ── 1. Directories
        for sub in ("mods", "config", "kubejs", "resourcepacks", "logs", "versions", "libraries", "assets"):
            (game_dir / sub).mkdir(parents=True, exist_ok=True)
        ensure_launcher_profiles(game_dir)
        self._set_pct(3)

        # ── 2. Java (cached after first lookup)
        java = find_java()
        if not java:
            self._log_line("Java 17 не найдена — скачиваем Temurin JRE 17…", "warn")
            java = self._install_java(game_dir)
        if not java:
            self._log_line("Нужна Java 17. Minecraft 1.20.1 Forge не стартует на 21/25.", "err")
            self.after(0, lambda: self._done(False))
            return
        self._log_line(f"Java {_java_major_version(java)}", "ok")
        self._set_pct(10)

        # ── 3. Vanilla + Forge (skip work when already installed)
        forge_ready = find_forge_json(game_dir)[0] is not None
        vanilla_jar = game_dir / "versions" / MC_VER / f"{MC_VER}.jar"
        vanilla_ready = vanilla_jar.exists() and vanilla_jar.stat().st_size > 10_000_000
        forge_client = (
            game_dir / "libraries" / "net" / "minecraftforge" / "forge"
            / f"{MC_VER}-{FORGE_VER}" / f"forge-{MC_VER}-{FORGE_VER}-client.jar"
        )
        warm = forge_ready and vanilla_ready and forge_client.exists()

        if warm:
            self._log_line("✓ Minecraft/Forge уже установлены — быстрый старт", "ok")
            ver_json, ver_id = find_forge_json(game_dir)
        else:
            self._log_line(f"Проверяем Minecraft {MC_VER}…", "dim")
            try:
                ensure_vanilla_version_json(game_dir)
                ensure_vanilla_client_jar(game_dir, log=lambda m: self._log_line(m, "info"))
            except Exception as e:
                self._log_line(f"❌ Minecraft {MC_VER}: {e}", "err")
                self.after(0, lambda: self._done(False))
                return
            self._set_pct(18)

            ver_json, ver_id = find_forge_json(game_dir)
            if not ver_json:
                self._log_line("⚙️  Forge не найден — ставим из лаунчера…", "info")
                ok = self._install_forge(game_dir, java)
                if not ok:
                    self._log_line("❌ Forge установить не удалось!", "err")
                    self.after(0, lambda: self._done(False)); return
                ver_json, ver_id = find_forge_json(game_dir)
            if not ver_json:
                self._log_line("❌ Forge версия не найдена после установки!", "err")
                self.after(0, lambda: self._done(False)); return
            self._log_line(f"✓ Forge: {ver_id}", "ok")
        self._set_pct(30)

        # ── 4. Sync mods (local / CDN only on Play — skip slow GitHub 404)
        self._log_line("📦 Синхронизируем сборку...", "info")
        self._sync_files(game_dir, prefer_remote=False, skip_if_ready=warm)
        self._set_pct(88)

        # ── 5. Launch
        self._log_line("🚀 Собираем classpath / natives / assets...", "info")
        try:
            host = (self._cfg.get("server_host") or SERVER_IP).strip()
            port = str(self._cfg.get("server_port") or SERVER_PORT).strip()
            cmd = build_launch_cmd(
                game_dir, username, ram_mb, java,
                log=lambda m: self._log_line(m, "dim"),
                auto_join=f"{host}:{port}",
            )
            proc = spawn_minecraft(cmd, game_dir)
            self._log_line(f"Процесс Minecraft PID {proc.pid} — проверяем…", "dim")

            # If the game dies in the first seconds, surface it instead of "success"
            crashed = False
            exit_code = None
            # Warm: Forge already past heavy install — shorter wait before "success"
            polls = 8 if warm else 16  # ~4s / ~8s
            for _ in range(polls):
                time.sleep(0.5)
                exit_code = proc.poll()
                if exit_code is not None:
                    crashed = True
                    break

            elapsed = time.perf_counter() - t0
            if crashed:
                latest = game_dir / "logs" / "latest.log"
                console = game_dir / "logs" / "minecraft_console.log"
                self._log_line(
                    f"❌ Minecraft сразу закрылся (код {exit_code}) за {elapsed:.1f}с",
                    "err",
                )
                # Prefer console (stderr) — early crashes often never reach latest.log
                for label, path in (("console", console), ("latest", latest)):
                    try:
                        if not path.exists() or path.stat().st_size < 8:
                            continue
                        self._log_line(f"— {label}: {path}", "warn")
                        raw = path.read_bytes()
                        text = raw.decode("utf-8", errors="replace")
                        lines = text.splitlines()
                        for line in lines[-20:]:
                            self._log_line(line[:220], "dim")
                    except OSError:
                        pass
                gdir = str(game_dir).lower().replace("/", "\\")
                if "aquatech-client" in gdir or "aquatbuild" in gdir or "progect" in gdir:
                    self._log_line(
                        "⚠️  Папка игры похожа на сборку клиента. В Настройках поставь: "
                        f"{GAME_DIR}",
                        "warn",
                    )
                self.after(0, lambda: self._done(False))
                return

            self._log_line(
                f"Игра запущена за {elapsed:.1f}с. Лаунчер можно свернуть — Minecraft работает отдельно.",
                "ok",
            )
            self._set_pct(100)
            self.after(0, lambda: self._done(True, close=False))
        except Exception as ex:
            self._log_line(f"❌ Ошибка запуска: {ex}", "err")
            self.after(0, lambda: self._done(False))

def _get_gh_token() -> str:
    candidates = [
        _bundle_dir() / ".gh_token",
        _app_dir() / ".gh_token",
        Path(__file__).resolve().parent / ".gh_token",
        Path(__file__).resolve().parent.parent / ".gh_token",
        Path(r"C:\Users\xieto\Desktop\AquaTech\.gh_token"),
    ]
    for p in candidates:
        if p.is_file():
            try:
                tok = p.read_text(encoding="utf-8").strip()
                if tok:
                    return tok
            except Exception:
                pass
    return ""


    # ── Download helper with browser-like headers (avoids CDN 403) ─────────
    def _download_url(self, url: str, dest_path: Path, reporthook=None):
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            ),
            "Accept": "*/*",
            "Accept-Language": "en-US,en;q=0.9",
            "Referer": "https://files.minecraftforge.net/",
            "Connection": "close",
        }
        if "github" in url.lower() or "githubusercontent.com" in url.lower():
            t = _get_gh_token()
            if t:
                headers["Authorization"] = f"token {t}"
        req = urllib.request.Request(url, headers=headers)
        tmp = dest_path.with_suffix(dest_path.suffix + ".part")
        try:
            with urllib.request.urlopen(req, timeout=120) as resp, open(tmp, "wb") as f:
                total_size = int(resp.headers.get("Content-Length", 0) or 0)
                count = 0
                block_size = 65536
                while True:
                    chunk = resp.read(block_size)
                    if not chunk:
                        break
                    f.write(chunk)
                    count += 1
                    if reporthook:
                        reporthook(count, block_size, total_size)
            tmp.replace(dest_path)
        except Exception:
            tmp.unlink(missing_ok=True)
            raise

    # ── Forge install ──────────────────────────────────────────────────────
    def _install_forge(self, game_dir: Path, java: str) -> bool:
        """Fast path: prebuilt Forge jars + parallel libs. Fallback: official installer."""
        log = lambda m, *_a, **_k: self._log_line(m, "info")

        # 1) Fast install — skips DOWNLOAD_MOJMAPS / jarsplitter / binarypatcher (minutes)
        try:
            if install_forge_fast(game_dir, log=lambda m: self._log_line(m, "info")):
                self._log_line("✓ Forge готов (быстрая установка)", "ok")
                return True
        except Exception as e:
            self._log_line(f"⚠️  Быстрая установка Forge не вышла: {e}", "warn")

        # 2) Fallback — official installer (slow: processors + maven)
        installer = game_dir / FORGE_INSTALLER_NAME

        if installer.exists() and not _is_valid_forge_installer(installer):
            self._log_line("Битый Forge installer — удаляем", "warn")
            installer.unlink(missing_ok=True)

        if not _is_valid_forge_installer(installer):
            bundled = find_bundled_forge_installer()
            if bundled:
                mb = round(bundled.stat().st_size / 1024 / 1024, 2)
                self._log_line(f"Forge installer внутри лаунчера ({mb} МБ) — копируем…", "info")
                shutil.copy2(bundled, installer)
                if not _is_valid_forge_installer(installer):
                    self._log_line("❌ Скопированный Forge installer битый!", "err")
                    return False
            else:
                meipass = getattr(sys, "_MEIPASS", None)
                self._log_line(
                    f"Forge runtime/installer не в exe (MEIPASS={meipass}) — качаем installer…",
                    "warn",
                )

                def reporthook(count, block_size, total_size):
                    if total_size > 0:
                        pct = 10 + int((count * block_size / total_size) * 18)
                        self._set_pct(min(pct, 28))

                last_err = None
                for url in FORGE_URLS:
                    try:
                        self._log_line(f"   → {url.split('/')[2]} …", "dim")
                        self._download_url(url, installer, reporthook)
                        if not _is_valid_forge_installer(installer):
                            installer.unlink(missing_ok=True)
                            raise RuntimeError("скачан файл слишком маленький / не JAR")
                        last_err = None
                        break
                    except Exception as err:
                        last_err = err
                        installer.unlink(missing_ok=True)
                        self._log_line(f"   ✗ {err}", "warn")

                if last_err is not None or not _is_valid_forge_installer(installer):
                    self._log_line(f"Ошибка скачивания Forge: {last_err}", "err")
                    return False

                self._log_line("Forge installer скачан", "ok")

        # Prefetch installer + version libs BEFORE processors (mirrors, parallel)
        try:
            with zipfile.ZipFile(installer, "r") as zf:
                if "install_profile.json" in zf.namelist():
                    prof = json.loads(zf.read("install_profile.json"))
                    prefetch_version_libraries(game_dir, {"libraries": prof.get("libraries") or []},
                                               log=lambda m: self._log_line(m, "dim"))
                if "version.json" in zf.namelist():
                    ver = json.loads(zf.read("version.json"))
                    prefetch_version_libraries(game_dir, ver, log=lambda m: self._log_line(m, "dim"))
        except Exception as e:
            self._log_line(f"prefetch libs: {e}", "dim")

        self._log_line("Ставим Forge через installer (запасной путь, может занять 1–2 мин)…", "info")
        ensure_launcher_profiles(game_dir)
        try:
            java_exec = str(Path(java).with_name("java.exe")) if java.lower().endswith("javaw.exe") else java
            if not Path(java_exec).exists():
                java_exec = java
            flags = getattr(subprocess, "CREATE_NO_WINDOW", 0x08000000) if os.name == "nt" else 0
            result = subprocess.run(
                [java_exec, "-jar", str(installer), "--installClient", str(game_dir)],
                capture_output=True, timeout=300, cwd=str(game_dir),
                creationflags=flags,
            )
            if result.returncode == 0 and find_forge_json(game_dir)[0]:
                self._log_line("Forge установлен", "ok")
                return True

            err = (result.stderr or b"").decode("utf-8", errors="replace")
            out = (result.stdout or b"").decode("utf-8", errors="replace")
            combined = (err + "\n" + out)[-800:]
            self._log_line(f"Прямая установка: {combined[-200:]}", "warn")

            mc_dir = Path.home() / "AppData" / "Roaming" / ".minecraft"
            ensure_launcher_profiles(mc_dir)
            self._log_line("Пробуем через .minecraft…", "info")
            result2 = subprocess.run(
                [java_exec, "-jar", str(installer), "--installClient", str(mc_dir)],
                capture_output=True, timeout=300, cwd=str(mc_dir),
                creationflags=flags,
            )
            if result2.returncode == 0:
                self._copy_forge_from_minecraft(mc_dir, game_dir)
                if find_forge_json(game_dir)[0]:
                    self._log_line("Forge установлен", "ok")
                    return True

            err2 = (result2.stderr or b"").decode("utf-8", errors="replace")[-300:]
            out2 = (result2.stdout or b"").decode("utf-8", errors="replace")[-300:]
            self._log_line(f"Forge installer ошибка: {err2 or out2 or combined}", "err")
            return False
        except subprocess.TimeoutExpired:
            self._log_line("Forge installer завис (>5 мин)", "err")
            return False
        except Exception as e:
            self._log_line(f"Forge install exception: {e}", "err")
            return False

    def _copy_forge_from_minecraft(self, mc_dir: Path, game_dir: Path):
        """Copy Forge version JSON/jar + missing libraries into AquaTech game dir."""
        src_ver, ver_id = find_forge_json(mc_dir)
        if not src_ver:
            return
        dst_ver_dir = game_dir / "versions" / ver_id
        dst_ver_dir.mkdir(parents=True, exist_ok=True)
        for item in (mc_dir / "versions" / ver_id).iterdir():
            target = dst_ver_dir / item.name
            if item.is_file():
                shutil.copy2(item, target)
            elif item.is_dir() and not target.exists():
                shutil.copytree(item, target)

        # Copy libraries referenced by the version JSON
        with open(src_ver, "r", encoding="utf-8") as f:
            ver = json.load(f)
        for lib in ver.get("libraries", []):
            art = (lib.get("downloads") or {}).get("artifact")
            if not art or "path" not in art:
                continue
            rel = art["path"].replace("/", os.sep)
            src = mc_dir / "libraries" / rel
            dst = game_dir / "libraries" / rel
            if src.exists():
                dst.parent.mkdir(parents=True, exist_ok=True)
                if not dst.exists():
                    shutil.copy2(src, dst)

    # ── Java install ───────────────────────────────────────────────────────
    def _install_java(self, game_dir: Path) -> str | None:
        jre_zip = game_dir / "_java17.zip"
        self._log_line("📥 Скачиваем Java 17 JRE (~50 МБ)...", "warn")
        try:
            def reporthook(count, block_size, total_size):
                if total_size > 0:
                    pct = 3 + int((count * block_size / total_size) * 6)
                    self._set_pct(min(pct, 9))
            self._download_url(JAVA_URL, jre_zip, reporthook)
        except Exception as e:
            self._log_line(f"❌ Java download error: {e}", "err")
            return None
        try:
            with zipfile.ZipFile(str(jre_zip), "r") as z:
                z.extractall(str(JAVA_DIR))
            jre_zip.unlink(missing_ok=True)
            # Find java.exe inside extracted dir
            for p in JAVA_DIR.rglob("java.exe"):
                return str(p)
        except Exception as e:
            self._log_line(f"❌ Java extract error: {e}", "err")
        return None

    # ── Sync files ─────────────────────────────────────────────────────────
    def _sync_files(self, game_dir: Path, prefer_remote: bool = False, skip_if_ready: bool = False):
        """Install AquaTech pack into the game folder.

        Order (Play):
          1) Local AquaTech-Client — for you while developing
          2) update_url CDN (Playit / local sync server) — for friends
          3) Localhost sync fallbacks (short timeouts only)

        Order (Update button / prefer_remote=True):
          1) update_url CDN first (full probe)
          2) Local pack
          3) GitHub
        """
        manifest = None
        source = None
        pack = None
        # Do not auto-append all LOCAL_SYNC_FALLBACKS into resolve — we probe selectively
        base = resolve_update_base(self._cfg, allow_local_fallback=False)
        configured_remote = bool((self._cfg.get("update_url") or "").strip()) or bool(base)

        def try_one(cand: str, timeout: float) -> bool:
            nonlocal manifest, source, base
            url = f"{cand.rstrip('/')}/manifest.json"
            self._log_line(f"🌐 CDN: {url}", "info")
            try:
                req = urllib.request.Request(
                    url,
                    headers={"User-Agent": f"Mozilla/5.0 AquaTechLauncher/{LAUNCHER_VER}"},
                )
                with urllib.request.urlopen(req, timeout=timeout) as r:
                    manifest = json.loads(r.read())
                source = "cdn"
                base = cand.rstrip("/")
                return True
            except Exception as e:
                self._log_line(f"⚠️  CDN {cand}: {e}", "dim")
                return False

        def try_cdn(*, include_local_fallbacks: bool) -> bool:
            bases: list[str] = []
            if base:
                bases.append(base.rstrip("/"))
            if include_local_fallbacks:
                for fb in LOCAL_SYNC_FALLBACKS:
                    b = fb.rstrip("/")
                    if b not in bases:
                        bases.append(b)
            for cand in bases:
                # Loopback dead ports must fail fast; remote (Playit) gets a bit more
                timeout = 0.6 if _is_loopback_url(cand) else (8.0 if prefer_remote else 3.0)
                if try_one(cand, timeout):
                    return True
            return False

        def try_local() -> bool:
            nonlocal manifest, source, pack
            pack = find_local_pack_dir(self._cfg)
            if not pack:
                return False
            self._log_line(f"📦 Локальная сборка: {pack}", "info")
            manifest = build_manifest_from_pack(pack, base_url=base)
            source = "local"
            return True

        def try_github() -> bool:
            nonlocal manifest, source
            try:
                headers = {"User-Agent": f"Mozilla/5.0 AquaTechLauncher/{LAUNCHER_VER}"}
                t = _get_gh_token()
                if t:
                    headers["Authorization"] = f"token {t}"
                req = urllib.request.Request(MANIFEST_URL, headers=headers)
                with urllib.request.urlopen(req, timeout=10) as r:
                    if r.status >= 400:
                        return False
                    manifest = json.loads(r.read())
                source = "github"
                self._log_line("🌐 Сборка с GitHub", "info")
                return True
            except Exception as e:
                self._log_line(f"⚠️  GitHub manifest: {e}", "dim")
                return False

        # Warm Play: pack already on disk — only quick local/remote check, no 40s fallback cascade
        if skip_if_ready and not prefer_remote and _pack_looks_ready(game_dir):
            if try_local():
                pass  # apply below
            elif configured_remote and base and try_one(base, 2.0):
                pass
            else:
                self._log_line(
                    "✓ Сборка уже на месте — sync пропущен (кнопка «Обновить» для проверки CDN)",
                    "ok",
                )
                return

        if prefer_remote:
            ok = try_cdn(include_local_fallbacks=True) or try_local() or try_github()
        else:
            # Play: local first; remote URL once; GitHub fallback
            ok = try_local()
            if not ok:
                if configured_remote:
                    ok = try_cdn(include_local_fallbacks=_is_loopback_url(base or "")) or try_github()
                else:
                    ok = try_cdn(include_local_fallbacks=True) or try_github()

        if not ok or not manifest:
            if _pack_looks_ready(game_dir):
                self._log_line("✓ Моды уже стоят — продолжаем без CDN", "ok")
                return
            self._log_line(
                "⚠️  Сборка модов не найдена. Запусти sync-сервер (start_sync_server.bat) "
                "или укажи URL обновлений / положи AquaTech-Client рядом с exe.",
                "warn",
            )
            self._log_line(
                "Minecraft/Forge всё равно скачаются — без модов будет ванильный Forge.",
                "dim",
            )
            return

        apply_manifest_sync(
            game_dir,
            manifest,
            source=source or "cdn",
            pack=pack,
            base=base or "",
            verify_hash=prefer_remote,
            download_url=self._download_url,
            log=lambda m: self._log_line(
                m,
                "ok" if m.startswith("✓") else ("warn" if m.startswith("⚠️") else ("dim" if m.startswith("🗑") else "info")),
            ),
        )


# ─── Entry ────────────────────────────────────────────────────────────────────
def run_web_ui() -> None:
    """Phase 1: local API + WebView2 shell around the existing launch engine."""
    from launcher_bridge import API_HOST, API_PORT, start_api_server, wait_api_ready
    import webview

    # Persist WebView2 profile — second launch is faster than ephemeral temp profiles
    ud = Path.home() / "AppData" / "Local" / "AquaTech" / "WebView2"
    try:
        ud.mkdir(parents=True, exist_ok=True)
        os.environ.setdefault("WEBVIEW2_USER_DATA_FOLDER", str(ud))
    except OSError:
        pass

    start_api_server(API_PORT)
    if not wait_api_ready(API_PORT):
        raise RuntimeError("Не удалось поднять локальный API лаунчера")
    webview.create_window(
        f"AquaTech  ·  {LAUNCHER_VER}",
        f"http://{API_HOST}:{API_PORT}/",
        width=WIN_W,
        height=WIN_H,
        min_size=(WIN_W, WIN_H),
        background_color=C_BG,
        text_select=False,
    )
    webview.start()


if __name__ == "__main__":
    ui = "web"
    if "--ui=tk" in sys.argv:
        ui = "tk"
    elif "--ui=web" in sys.argv:
        ui = "web"

    if ui == "web":
        try:
            run_web_ui()
        except Exception as ex:
            # Fallback for machines without WebView2 / pywebview
            try:
                import tkinter.messagebox as mb
                mb.showwarning(
                    "AquaTech",
                    f"Web UI недоступен ({ex}).\nОткрываю классический интерфейс.\n"
                    "Можно запустить с флагом --ui=tk",
                )
            except Exception:
                pass
            app = AquaTechLauncher()
            app.mainloop()
    else:
        app = AquaTechLauncher()
        app.mainloop()
