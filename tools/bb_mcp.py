"""Minimal Blockbench MCP client (stdlib only).

Endpoint: http://localhost:3000/bb-mcp (server runs inside the Blockbench app).
Handles the initialize handshake and SSE/JSON responses.

Usage:
  python tools/bb_mcp.py tools
  python tools/bb_mcp.py call risky_eval '{"code":"return Project.name;"}'
  python tools/bb_mcp.py call create_project '{"format":"java_block","name":"test"}'
"""
from __future__ import annotations

import json
import sys
import urllib.error
import urllib.request

URL = "http://localhost:3000/bb-mcp"
SESSION: str | None = None


def _post(payload: dict, *, notify: bool = False) -> dict | None:
    global SESSION
    body = json.dumps(payload).encode()
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
    }
    if SESSION:
        headers["mcp-session-id"] = SESSION
    req = urllib.request.Request(URL, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=180) as resp:
            sid = resp.headers.get("mcp-session-id")
            if sid:
                SESSION = sid
            raw = resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        raise SystemExit(f"HTTP {e.code}: {e.read()[:400].decode('utf-8','replace')}") from e
    if notify or not raw.strip():
        return None
    if raw.lstrip().startswith("event:") or "\ndata:" in raw:
        for line in raw.splitlines():
            if line.startswith("data:"):
                return json.loads(line[5:].strip())
        return None
    return json.loads(raw)


def handshake() -> None:
    _post({
        "jsonrpc": "2.0",
        "id": 1,
        "method": "initialize",
        "params": {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {"name": "aquatech-bb-mcp", "version": "1.0"},
        },
    })
    _post({"jsonrpc": "2.0", "method": "notifications/initialized"}, notify=True)


def tools() -> list[dict]:
    res = _post({"jsonrpc": "2.0", "id": 2, "method": "tools/list", "params": {}})
    return (res or {}).get("result", {}).get("tools", [])


def call(name: str, args: dict) -> dict:
    res = _post({
        "jsonrpc": "2.0",
        "id": 3,
        "method": "tools/call",
        "params": {"name": name, "arguments": args},
    })
    return res or {}


def main() -> int:
    handshake()
    action = sys.argv[1] if len(sys.argv) > 1 else "tools"
    if action == "tools":
        for t in tools():
            print(f"- {t.get('name')}: {(t.get('description') or '').splitlines()[0][:110]}")
        return 0
    if action == "call":
        name = sys.argv[2]
        args = json.loads(sys.argv[3]) if len(sys.argv) > 3 else {}
        res = call(name, args)
        out = json.dumps(res, ensure_ascii=False, indent=2)
        print(out[:6000])
        return 0
    if action == "call-file":
        from pathlib import Path

        name = sys.argv[2]
        args = json.loads(Path(sys.argv[3]).read_text(encoding="utf-8"))
        res = call(name, args)
        text = json.dumps(res, ensure_ascii=False, indent=2)
        print(text[:6000])
        return 0
    if action == "eval-file":
        from pathlib import Path

        code = Path(sys.argv[2]).read_text(encoding="utf-8")
        res = call("risky_eval", {"code": code})
        text = json.dumps(res, ensure_ascii=False, indent=2)
        print(text[:6000])
        return 0
    if action == "screenshot":
        import base64
        import time
        from pathlib import Path

        res = call("capture_screenshot", {})
        for item in (res.get("result") or {}).get("content", []):
            data = item.get("data")
            if item.get("type") == "image" and data:
                out_dir = Path(__file__).resolve().parents[1] / "scratch" / "bb_screenshots"
                out_dir.mkdir(parents=True, exist_ok=True)
                path = out_dir / f"bb_{time.strftime('%Y%m%d_%H%M%S')}.png"
                path.write_bytes(base64.b64decode(data))
                print(f"saved {path}")
                return 0
        print("no image in response:", json.dumps(res, ensure_ascii=False)[:1500])
        return 1
    print(__doc__)
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
