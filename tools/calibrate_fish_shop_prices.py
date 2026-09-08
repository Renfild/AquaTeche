#!/usr/bin/env python3
"""Calibrate fish_shop.json prices based on rarity and high-tier rod progression."""
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SHOP_PATH = ROOT / "server" / "config" / "aqualumen" / "fish_shop.json"

LEGENDARY_PRICES = {
    "starcatcher:aurora": 2400,
    "starcatcher:elderscale": 2800,
    "starcatcher:vesani": 3200,
    "starcatcher:ward": 3500,
    "starcatcher:suneater": 4200,
    "starcatcher:voidbiter": 4800,
    "starcatcher:missingno": 5500,
}

def get_calibrated_price(item: dict) -> int:
    item_id = item.get("id", "")
    rarity = item.get("rarity", "")
    
    if item_id in LEGENDARY_PRICES:
        return LEGENDARY_PRICES[item_id]
        
    if rarity == "Легенда":
        return 2500
    elif rarity == "Эпический":
        h = sum(ord(c) for c in item_id)
        return 450 + (h % 31) * 10
    elif rarity == "Редкий":
        h = sum(ord(c) for c in item_id)
        return 120 + (h % 11) * 10
    else: # Обычный
        h = sum(ord(c) for c in item_id)
        return 30 + (h % 6) * 5

def main():
    if not SHOP_PATH.is_file():
        print(f"Error: {SHOP_PATH} not found")
        return 1
        
    shutil.copy2(SHOP_PATH, SHOP_PATH.with_suffix(".json.bak"))
    
    with open(SHOP_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)
        
    fishes = data.get("fishes", [])
    for fish in fishes:
        old_p = fish.get("priceCoins", 0)
        new_p = get_calibrated_price(fish)
        fish["priceCoins"] = new_p
        
    with open(SHOP_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
        
    print(f"Calibrated {len(fishes)} fishes in {SHOP_PATH}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
