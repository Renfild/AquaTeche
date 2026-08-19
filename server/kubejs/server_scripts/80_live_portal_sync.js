// AquaTech: Live sync player stats, coins & playtime between Minecraft server & Web Portal
const SYNC_URL = "https://aquateche.store/api/sync/player";

// Sync key lives in config/aquatech_sync_key.json on the server only (gitignored).
// java.nio is blocked by the KubeJS sandbox, so we read through the JsonIO binding.
function loadSyncKey() {
    try {
        let raw = JsonIO.read('config/aquatech_sync_key.json');
        if (!raw) return '';
        if (raw.get) {
            let v = raw.get('key');
            return v ? String(v.getAsString ? v.getAsString() : v).trim() : '';
        }
        return raw.key ? String(raw.key).trim() : '';
    } catch (e) {
        console.error('[portal-sync] cannot read config/aquatech_sync_key.json: ' + e);
        return '';
    }
}
const SYNC_KEY = loadSyncKey();
if (SYNC_KEY) console.info('[portal-sync] sync key loaded');

function sendStatsToPortal(pName, coins, fish, playtimeHours, privilege, questsDone) {
    if (!SYNC_KEY) return;
    let Thread = Java.type('java.lang.Thread');
    let Runnable = Java.type('java.lang.Runnable');
    let URL = Java.type('java.net.URL');

    let task = new Runnable({
        run: function() {
            try {
                let url = new URL(SYNC_URL);
                let con = url.openConnection();
                con.setRequestMethod('POST');
                con.setRequestProperty('Content-Type', 'application/json; charset=utf-8');
                con.setRequestProperty('X-AquaTech-Server-Key', SYNC_KEY);
                con.setRequestProperty('User-Agent', 'AquaTech-Server/2.9.50');
                con.setDoOutput(true);
                con.setConnectTimeout(4000);
                con.setReadTimeout(4000);

                let payload = JSON.stringify({
                    nick: pName,
                    coins: Number(coins) || 0,
                    fish: Number(fish) || 0,
                    playtime_hours: Number(playtimeHours) || 0,
                    privilege: String(privilege || "Игрок"),
                    quests_done: Number(questsDone) || 0
                });

                let os = con.getOutputStream();
                os.write(new java.lang.String(payload).getBytes('UTF-8'));
                os.flush();
                os.close();

                let code = con.getResponseCode();
                con.disconnect();
            } catch (err) {
                // Silently ignore if portal is restarting
            }
        }
    });

    new Thread(task).start();
}

PlayerEvents.loggedIn(event => {
    let player = event.player;
    if (!player) return;
    let pName = player.username;
    
    event.server.scheduleInTicks(60, callback => {
        try {
            let ticks = player.stats.playTime || 0;
            let hours = Math.floor(ticks / 72000);
            let fish = player.stats.fishCaught || player.persistentData.fishCaught || 0;
            let coins = player.persistentData.coins || 0;
            sendStatsToPortal(pName, coins, fish, hours, "Игрок", 0);
        } catch (e) {}
    });
});

PlayerEvents.loggedOut(event => {
    let player = event.player;
    if (!player) return;
    let pName = player.username;
    try {
        let ticks = player.stats.playTime || 0;
        let hours = Math.floor(ticks / 72000);
        let fish = player.stats.fishCaught || player.persistentData.fishCaught || 0;
        let coins = player.persistentData.coins || 0;
        sendStatsToPortal(pName, coins, fish, hours, "Игрок", 0);
    } catch (e) {}
});

// NOTE: KubeJS 2001.6.5 has no fishing event binding (ItemEvents.fishCaught does
// not exist). The fish counter stays on vanilla stats / aquatech_ui until a
// proper hook is added to the mod itself.

