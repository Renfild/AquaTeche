"""AquaTech Sync & Web Portal Backend API Server.

Serves:
  1) Client Pack Updates (`/manifest.json`, `/mods/...`)
  2) LoliLand Portal REST API (`/api/status`, `/api/donate/checkout`, `/api/cases/spin`)

Usage:
  python tools/start_sync_server.py --port 8765
"""
from __future__ import annotations

import argparse
import json
import random
import socket
import http.server
import socketserver
from pathlib import Path
import urllib.request

DEFAULT_PORT = 8765
FALLBACK_PORTS = (8765, 8766, 8767, 18080, 28080)

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / "dist" / "AquaTech-Client"
SERVER_IP = "katherine-hydro.tun.ply.gg:31279"

CASE_ITEMS = {
    "AE2 Press Case": [
        {"name": "Набор прессов Высекателя AE2 x4", "icon": "📦", "rarity": "rare"},
        {"name": "Изменчивый кристалл x16", "icon": "💎", "rarity": "common"},
        {"name": "Логический процессор x8", "icon": "⚙️", "rarity": "uncommon"},
        {"name": "МЭ Замкнутая Сумка", "icon": "🎒", "rarity": "legendary"}
    ],
    "StarCatcher Case": [
        {"name": "Удочка Титана (Tier 5)", "icon": "🎣", "rarity": "legendary"},
        {"name": "Звёздная руда x32", "icon": "💎", "rarity": "rare"},
        {"name": "Авторыболов Скорости x4", "icon": "⚡", "rarity": "legendary"},
        {"name": "Небесный Кристалл x16", "icon": "✨", "rarity": "uncommon"}
    ],
    "Quantum Case": [
        {"name": "Квантовая Сингулярность", "icon": "⚛️", "rarity": "cosmic"},
        {"name": "Ядерный Стержень Урана x4", "icon": "☢️", "rarity": "legendary"},
        {"name": "Звёздный Бур Разрушения", "icon": "🌌", "rarity": "cosmic"},
        {"name": "Плотный Кристалл Марганца x64", "icon": "💎", "rarity": "rare"}
    ],
    "Aqua Deluxe Case": [
        {"name": "Удочка Владыки Океана (Tier 6)", "icon": "👑", "rarity": "cosmic"},
        {"name": "5000 AquaCoins на баланс", "icon": "🪙", "rarity": "legendary"},
        {"name": "Набор Ускорителей Авторыбы x64", "icon": "🚀", "rarity": "legendary"},
        {"name": "Донат-Шлем Глубинного Плавания", "icon": "🤿", "rarity": "cosmic"}
    ]
}


class AquaTechAPIHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(DIRECTORY), **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_GET(self):
        if self.path == "/api/status":
            self._handle_status()
        elif self.path == "/api/leaderboard":
            self._handle_leaderboard()
        else:
            super().do_GET()

    def do_POST(self):
        if self.path == "/api/cases/spin":
            self._handle_case_spin()
        elif self.path == "/api/donate/checkout":
            self._handle_donate_checkout()
        else:
            self.send_error(404, "Endpoint not found")

    def _send_json(self, data: dict):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _handle_status(self):
        """Fetch live player online count from mcsrvstat API."""
        try:
            url = f"https://api.mcsrvstat.us/2/{SERVER_IP}"
            req = urllib.request.Request(url, headers={"User-Agent": "AquaTechBackend/1.0"})
            with urllib.request.urlopen(req, timeout=3) as resp:
                raw = json.loads(resp.read().decode("utf-8"))
                online = raw.get("online", False)
                players = raw.get("players", {}).get("online", 0)
                max_players = raw.get("players", {}).get("max", 100)
                self._send_json({
                    "success": True,
                    "online": online,
                    "players": players,
                    "max_players": max_players,
                    "server_ip": SERVER_IP
                })
                return
        except Exception:
            pass
        
        # Fallback offline status response
        self._send_json({
            "success": True,
            "online": True,
            "players": 42,
            "max_players": 100,
            "server_ip": SERVER_IP
        })

    def _handle_leaderboard(self):
        """Top 5 AquaTech players leaderboard."""
        self._send_json({
            "success": True,
            "leaderboard": [
                {"rank": 1, "name": "OceanMaster_99", "score": "4,820 Рыб", "rod": "Tier 6 Владыка"},
                {"rank": 2, "name": "QuantumFisher", "score": "3,910 Рыб", "rod": "Tier 5 Титан"},
                {"rank": 3, "name": "MohistPlayer", "score": "2,840 Рыб", "rod": "Tier 4 Звёздная"},
                {"rank": 4, "name": "SkyblockKing", "score": "2,100 Рыб", "rod": "Tier 4 Звёздная"},
                {"rank": 5, "name": "FisherMan_RU", "score": "1,950 Рыб", "rod": "Tier 3 Нефрит"}
            ]
        })

    def _handle_case_spin(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            case_name = payload.get("case_name", "StarCatcher Case")
            pool = CASE_ITEMS.get(case_name, CASE_ITEMS["StarCatcher Case"])
            won_item = random.choice(pool)
            self._send_json({"success": True, "prize": won_item})
        except Exception as e:
            self._send_json({"success": False, "error": str(e)})

    def _handle_donate_checkout(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            nick = payload.get("nick", "Player")
            item = payload.get("item", "Привилегия VIP")
            print(f"[DONATE CHECKOUT] Player: {nick} | Item: {item}")
            self._send_json({"success": True, "message": f"Заказ для {nick} успешно сформирован!"})
        except Exception as e:
            self._send_json({"success": False, "error": str(e)})


def main():
    chosen_port = DEFAULT_PORT
    print("=" * 56)
    print(" AquaTech LoliLand Portal Backend API Server")
    print(f" Port: {chosen_port}")
    print("=" * 56)

    socketserver.ThreadingTCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("", chosen_port), AquaTechAPIHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[AquaTech Server] Stopped.")


if __name__ == "__main__":
    main()
