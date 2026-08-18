// AquaTech: Live sync player stats, coins & playtime between Minecraft server & Web Portal
const SYNC_URL = "https://aquateche.store/api/sync/player";
const SYNC_KEY = "aquatech_internal_sync_key_2026";

function sendStatsToPortal(pName, coins, fish, playtimeHours, privilege, questsDone) {
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
