# -*- coding: utf-8 -*-
"""Inject aquatech grantxp command rewards + Lightman's coin rewards into chapter capstones."""
import re

# Capstone id -> (aqua_xp_amount, coin_item, coin_count, cmd_reward_id, coin_reward_id)
CAPSTONES = {
    "1000000000000010": (150, "lightmanscurrency:coin_copper", 8, "5AQTCMD00000001", "5AQTCOIN0000001"),
    "1200000000000009": (200, "lightmanscurrency:coin_copper", 16, "5AQTCMD00000002", "5AQTCOIN0000002"),
    "1300000000000009": (250, "lightmanscurrency:coin_iron", 8, "5AQTCMD00000003", "5AQTCOIN0000003"),
    "1400000000000009": (250, "lightmanscurrency:coin_iron", 12, "5AQTCMD00000004", "5AQTCOIN0000004"),
    "1500000000000009": (300, "lightmanscurrency:coin_iron", 16, "5AQTCMD00000005", "5AQTCOIN0000005"),
    "1600000000000009": (300, "lightmanscurrency:coin_gold", 4, "5AQTCMD00000006", "5AQTCOIN0000006"),
    "1700000000000009": (350, "lightmanscurrency:coin_gold", 6, "5AQTCMD00000007", "5AQTCOIN0000007"),
    "1800000000000009": (400, "lightmanscurrency:coin_gold", 8, "5AQTCMD00000008", "5AQTCOIN0000008"),
    "1900000000000009": (400, "lightmanscurrency:coin_gold", 12, "5AQTCMD00000009", "5AQTCOIN0000009"),
    "1A00000000000009": (500, "lightmanscurrency:coin_emerald", 4, "5AQTCMD0000000A", "5AQTCOIN000000A"),
    "1B00000000000025": (500, "lightmanscurrency:coin_emerald", 6, "5AQTCMD0000000B", "5AQTCOIN000000B"),
    "1C00000000000010": (600, "lightmanscurrency:coin_diamond", 4, "5AQTCMD0000000C", "5AQTCOIN000000C"),
    "1E00000000000006": (1000, "lightmanscurrency:coin_diamond", 8, "5AQTCMD0000000D", "5AQTCOIN000000D"),
}

CHAPTERS = [
    "01_kickstarter", "02_catch", "03_atoll", "04_roost", "05_swarm",
    "06_kinetics", "07_steam", "08_power", "09_industry", "10_depths",
    "11_me", "12_dreadnought", "13_horizon_raids",
]


def inject_rewards(path, capstone_id, aqua_xp, coin_item, coin_count, cmd_id, coin_id):
    text = open(path, encoding="utf-8").read()
    if f'id: "{cmd_id}"' in text:
        print("already has cmd reward", path, capstone_id)
        return

    # Find the rewards array of this specific quest by matching id then rewards: [
    # Insert new rewards before the closing of rewards]
    pattern = rf'(id: "{re.escape(capstone_id)}"\s*\n\s*rewards: \[)'
    m = re.search(pattern, text)
    if not m:
        # Some chapters have rewards before id or different whitespace
        # Try finding rewards block after id within ~20 lines
        idx = text.find(f'id: "{capstone_id}"')
        if idx < 0:
            print("capstone not found", path, capstone_id)
            return
        # Look for rewards: [ after this id (within the same quest object)
        chunk = text[idx:idx + 800]
        rm = re.search(r'rewards: \[', chunk)
        if not rm:
            print("no rewards array", path, capstone_id)
            return
        insert_at = idx + rm.end()
    else:
        insert_at = m.end()

    cmd_block = f"""
\t\t\t\t{{
\t\t\t\t\tcommand: "/aquatech grantxp @p {aqua_xp}"
\t\t\t\t\televate_perms: true
\t\t\t\t\tid: "{cmd_id}"
\t\t\t\t\tsilent: true
\t\t\t\t\ttype: "command"
\t\t\t\t}}"""

    # Only add coin reward if this exact coin reward id isn't already present
    # and if chapter doesn't already reward this exact coin as first reward of capstone
    coin_block = ""
    if f'id: "{coin_id}"' not in text:
        coin_block = f"""
\t\t\t\t{{
\t\t\t\t\tcount: {coin_count}
\t\t\t\t\tid: "{coin_id}"
\t\t\t\t\titem: "{coin_item}"
\t\t\t\t\ttype: "item"
\t\t\t\t}}"""

    insertion = cmd_block + coin_block
    text = text[:insert_at] + insertion + text[insert_at:]
    open(path, "w", encoding="utf-8", newline="\n").write(text)
    print("injected", path, capstone_id, f"+{aqua_xp}xp", coin_item)


for chapter in CHAPTERS:
    path = rf"server\config\ftbquests\quests\chapters\{chapter}.snbt"
    text = open(path, encoding="utf-8").read()
    for cid, (xp, coin, cnt, cmd_id, coin_id) in CAPSTONES.items():
        if f'id: "{cid}"' in text:
            inject_rewards(path, cid, xp, coin, cnt, cmd_id, coin_id)
            break
    else:
        print("no matching capstone in", path)

print("done")
