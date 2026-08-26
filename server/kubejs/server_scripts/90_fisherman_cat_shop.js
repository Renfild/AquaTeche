// AquaTech Dynamic Fisherman Shop & Lumen Market (F4 / /shop)
// Daily demand: 3 trending fish from the FULL catalog (fish_shop.json), shared
// with the Java hub via config/aqualumen/fish_demand.json (same index math).

const FISH_CATALOG = [
    { id: 'minecraft:cod', name: 'Треска', baseIn: 4, baseOut: 1 },
    { id: 'minecraft:salmon', name: 'Лосось', baseIn: 3, baseOut: 1 },
    { id: 'minecraft:tropical_fish', name: 'Тропическая рыба', baseIn: 2, baseOut: 1 },
    { id: 'minecraft:pufferfish', name: 'Иглобрюх', baseIn: 1, baseOut: 2 },
    { id: 'starcatcher:driftfin', name: 'Дрифтфин', baseIn: 2, baseOut: 2 },
    { id: 'starcatcher:rockgill', name: 'Камнежабр', baseIn: 2, baseOut: 3 },
    { id: 'starcatcher:sunny_sturgeon', name: 'Солнечный осётр', baseIn: 1, baseOut: 4 },
    { id: 'starcatcher:silverfin_pike', name: 'Серебряная щука', baseIn: 1, baseOut: 5 },
    { id: 'starcatcher:carpenjoe', name: 'Карпенджо', baseIn: 1, baseOut: 6 },
    { id: 'starcatcher:hollowbelly_darter', name: 'Пустобрюхий дарт', baseIn: 1, baseOut: 8 },
    { id: 'starcatcher:silverveil_perch', name: 'Вуалевый окунь', baseIn: 1, baseOut: 10 },
    { id: 'starcatcher:elderscale', name: 'Древнечешуйник', baseIn: 1, baseOut: 16 }
];

// Day index matching Java LocalDate.toEpochDay (server local time)
function localDay() {
    let d = new Date();
    return Math.floor(new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime() / 86400000);
}

// Deterministic trend pick from the full fish_shop.json pool (identical to Java)
function computeTrends(day) {
    let pool = [];
    try {
        let shop = JsonIO.read('config/aqualumen/fish_shop.json');
        if (shop && shop.fishes) {
            for (let i = 0; i < shop.fishes.size(); i++) {
                let f = shop.fishes.get(i);
                pool.push({ id: String(f.id), name: String(f.name) });
            }
        }
    } catch (e) {}
    if (pool.length === 0) pool = FISH_CATALOG;
    let n = pool.length;
    let i1 = ((day * 7 + 3) % n + n) % n;
    let i2 = ((day * 13 + 5) % n + n) % n;
    if (i2 === i1) i2 = (i2 + 1) % n;
    let i3 = ((day * 29 + 11) % n + n) % n;
    if (i3 === i1 || i3 === i2) i3 = (i3 + 1) % n;
    if (i3 === i1) i3 = (i3 + 1) % n;
    return [
        { id: pool[i1].id, name: pool[i1].name, mult: 2.0 },
        { id: pool[i2].id, name: pool[i2].name, mult: 1.75 },
        { id: pool[i3].id, name: pool[i3].name, mult: 1.5 }
    ];
}

// Java (FishShopConfig) writes the shared file; fallback = same math here
function getDailyTrends() {
    let day = localDay();
    try {
        let j = JsonIO.read('config/aqualumen/fish_demand.json');
        if (j && j.day == day && j.trends && j.trends.size() >= 2) {
            let out = [];
            for (let i = 0; i < j.trends.size() && i < 3; i++) {
                let t = j.trends.get(i);
                out.push({ id: String(t.id), name: String(t.name), mult: Number(t.mult) });
            }
            return out;
        }
    } catch (e) {}
    return computeTrends(day);
}

function trendMessage() {
    let trends = getDailyTrends();
    let parts = [];
    trends.forEach(t => parts.push('§b' + t.name + ' §e×' + t.mult));
    return Text.gold('§6[Кот-рыболов] §eХит дня! §fКот платит бонус за: ' + parts.join('§f, ') + '§f. Курс обновится в полночь!');
}

function multFor(trends, id) {
    for (let i = 0; i < trends.length; i++) {
        if (trends[i].id === id) return trends[i].mult;
    }
    return 1.0;
}

function openFishermanShop(player) {
    try {
        let serverPlayer = player.minecraftPlayer || player;
        let MerchantOffers = Java.loadClass('net.minecraft.world.item.trading.MerchantOffers');
        let MerchantOffer = Java.loadClass('net.minecraft.world.item.trading.MerchantOffer');
        let ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');
        let BuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
        let ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');
        let Component = Java.loadClass('net.minecraft.network.chat.Component');
        let MerchantMenu = Java.loadClass('net.minecraft.world.inventory.MerchantMenu');
        let SimpleMenuProvider = Java.loadClass('net.minecraft.world.SimpleMenuProvider');
        let Merchant = Java.loadClass('net.minecraft.world.item.trading.Merchant');
        let SoundEvents = Java.loadClass('net.minecraft.sounds.SoundEvents');

        let offers = new MerchantOffers();
        let trends = getDailyTrends();

        function addTrade(inId, inCount, outId, outCount, in2Id, in2Count) {
            let inItem = BuiltInRegistries.ITEM.get(new ResourceLocation(inId));
            let outItem = BuiltInRegistries.ITEM.get(new ResourceLocation(outId));
            if (!inItem || !outItem) return;
            let inStack = new ItemStack(inItem, inCount);
            let outStack = new ItemStack(outItem, outCount);
            let in2Stack = ItemStack.EMPTY;
            if (in2Id && in2Count) {
                let in2Item = BuiltInRegistries.ITEM.get(new ResourceLocation(in2Id));
                if (in2Item) in2Stack = new ItemStack(in2Item, in2Count);
            }
            offers.add(new MerchantOffer(inStack, in2Stack, outStack, 9999, 1, 0.05));
        }

        // 1. Динамическая скупка: весь каталог + бонус трендам дня
        FISH_CATALOG.forEach(f => {
            let mult = multFor(trends, f.id);
            let outCount = Math.max(f.baseOut + 1, Math.round(f.baseOut * mult));
            addTrade(f.id, f.baseIn, 'minecraft:emerald', outCount);
        });

        // 2. Скупка приготовленной рыбы
        addTrade('minecraft:cooked_cod', 3, 'minecraft:emerald', 1);
        addTrade('minecraft:cooked_salmon', 2, 'minecraft:emerald', 1);

        // 3. Покупка снаряжения и предметов рыбака
        addTrade('minecraft:emerald', 3, 'minecraft:fishing_rod', 1);
        addTrade('minecraft:emerald', 5, 'starcatcher:fisherman_hat_white', 1);
        addTrade('minecraft:emerald', 8, 'starcatcher:tackle_box', 1);
        addTrade('minecraft:emerald', 12, 'aquatech_ui:sea_prism_shard', 1);

        let traderProxy = new JavaAdapter(Merchant, {
            player: serverPlayer,
            setTradingPlayer: function(p) { this.player = p; },
            getTradingPlayer: function() { return this.player || serverPlayer; },
            getOffers: function() { return offers; },
            overrideOffers: function(o) { offers = o; },
            notifyTrade: function(offer) { },
            notifyTradeUpdated: function(stack) { },
            getVillagerXp: function() { return 0; },
            overrideXp: function(xp) { },
            showProgressBar: function() { return false; },
            getNotifyTradeSound: function() { return SoundEvents.VILLAGER_YES; },
            isClientSide: function() { return false; }
        });

        serverPlayer.openMenu(new SimpleMenuProvider(
            (id, inv, p) => new MerchantMenu(id, inv, traderProxy),
            Component.literal("§6Скупщик рыбы §8· §e[Динамический курс]")
        ));

        serverPlayer.sendMerchantOffers(serverPlayer.containerMenu.containerId, offers, 1, 0, false, false);
        player.tell(trendMessage());
    } catch (err) {
        console.error('[AquaTech] Ошибка открытия рынка рыбака: ' + err);
    }
}

// 1. Открытие по правому клику на кота или NPC "Рыбак"
ItemEvents.entityInteracted(event => {
    let player = event.player;
    let target = event.target;
    let hand = event.hand;

    if (hand != 'MAIN_HAND' || !target) return;

    let targetType = String(target.type);
    let targetName = String(target.name ? target.name.string : (target.customName ? target.customName.string : ''));

    let isFisherman = targetName.toLowerCase().includes('рыбак') ||
                      targetName.toLowerCase().includes('fisher') ||
                      targetType === 'easy_npc:cat' ||
                      (targetType === 'minecraft:cat' && targetName.length > 0);

    if (!isFisherman) return;
    event.cancel();
    openFishermanShop(player);
});

// 2. Команды /shop, /market, /fishmarket
ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;

    event.register(
        Commands.literal('shop')
            .executes(ctx => {
                let player = ctx.source.player;
                if (player) openFishermanShop(player);
                return 1;
            })
    );

    event.register(
        Commands.literal('market')
            .executes(ctx => {
                let player = ctx.source.player;
                if (player) openFishermanShop(player);
                return 1;
            })
    );

    event.register(
        Commands.literal('fishmarket')
            .executes(ctx => {
                let player = ctx.source.player;
                if (player) openFishermanShop(player);
                return 1;
            })
    );
});

// 3. Оповещения о дневном курсе: при входе (раз в сутки), в полночь и на старте сервера
let lastTrendDay = -1;

ServerEvents.tick(event => {
    let day = localDay();
    if (day === lastTrendDay) return;
    let first = lastTrendDay === -1;
    lastTrendDay = day;
    event.server.scheduleInTicks(first ? 200 : 60, () => {
        event.server.players.forEach(p => p.tell(trendMessage()));
        if (!first) console.info('[Кот-рыболов] Сменился дневной курс рыбы');
    });
});

PlayerEvents.loggedIn(event => {
    let p = event.player;
    if (!p) return;
    let day = localDay();
    if (p.persistentData.catTrendDay == day) return;
    p.persistentData.catTrendDay = day;
    event.server.scheduleInTicks(80, () => {
        try { if (p) p.tell(trendMessage()); } catch (e) {}
    });
});
