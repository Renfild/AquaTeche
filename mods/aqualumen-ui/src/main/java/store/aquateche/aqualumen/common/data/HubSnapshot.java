package store.aquateche.aqualumen.common.data;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * Read only view of everything the hub screen shows. Built on the server, sent to the client,
 * never trusted back: the client only replies with action identifiers.
 */
public record HubSnapshot(Profile profile,
                          Wallet wallet,
                          Season season,
                          List<TopEntry> tops,
                          List<Offer> store,
                          List<CaseEntry> cases,
                          List<KitEntry> kits,
                          List<WarpEntry> warps,
                          List<FishEntry> fishes,
                          ServerInfo server,
                          CaseResult caseResult,
                          List<MarketEntry> market,
                          List<EventQuest> quests,
                          String eventLine) {

    public record Profile(String name, String rank, int rankColor, int level, float levelProgress,
                          long playtimeMinutes, int kills, int deaths, int quests, int friendsOnline) {
    }

    public record Wallet(long coins, long gems, int dailyStreak, boolean dailyAvailable) {
    }

    public record Season(String title, int tier, int maxTier, float tierProgress, boolean premium, int claimable, List<Integer> claimedTiers) {
    }

    public record TopEntry(int place, String player, String value, boolean self) {
    }

    public record Offer(String id, String title, String subtitle, long price, String currency, String badge, boolean owned) {
    }

    public record CaseEntry(String id, String title, int cost, int count, String rarity, List<LootInfo> loot) {
    }

    public record LootInfo(String label, String rarity, int weight, String item) {
    }

    /** One-shot result of the last opened case, consumed by the hub reel animation. */
    public record CaseResult(String caseId, String label, String rarity, int amount, String type) {
    }

    public record MarketEntry(int id, String label, int count, long price, String seller, String itemId, boolean self) {
    }

    /** Контракт дня: живёт в aquatech_ui (OceanEventsService), сюда приходит только витрина. */
    public record EventQuest(int idx, String desc, int goal, int progress, long reward, boolean claimed) {
    }

    public record KitEntry(String id, String title, String description, String badge, String command) {
    }

    public record WarpEntry(String id, String title, String description, String tag, String command) {
    }

    public record FishEntry(String id, String name, int count, long priceCoins, String rarity, String tag, float demand) {
    }

    public record ServerInfo(String name, int online, int slots, float tps, String build) {
    }

    private static void writeSafe(FriendlyByteBuf buf, String str) {
        buf.writeUtf(str != null ? str : "");
    }

    public void write(FriendlyByteBuf buf) {
        writeSafe(buf, profile.name());
        writeSafe(buf, profile.rank());
        buf.writeInt(profile.rankColor());
        buf.writeVarInt(profile.level());
        buf.writeFloat(profile.levelProgress());
        buf.writeVarLong(profile.playtimeMinutes());
        buf.writeVarInt(profile.kills());
        buf.writeVarInt(profile.deaths());
        buf.writeVarInt(profile.quests());
        buf.writeVarInt(profile.friendsOnline());

        buf.writeVarLong(wallet.coins());
        buf.writeVarLong(wallet.gems());
        buf.writeVarInt(wallet.dailyStreak());
        buf.writeBoolean(wallet.dailyAvailable());

        writeSafe(buf, season.title());
        buf.writeVarInt(season.tier());
        buf.writeVarInt(season.maxTier());
        buf.writeFloat(season.tierProgress());
        buf.writeBoolean(season.premium());
        buf.writeVarInt(season.claimable());
        buf.writeCollection(season.claimedTiers() != null ? season.claimedTiers() : List.of(), FriendlyByteBuf::writeVarInt);

        buf.writeCollection(tops, (b, e) -> {
            b.writeVarInt(e.place());
            writeSafe(b, e.player());
            writeSafe(b, e.value());
            b.writeBoolean(e.self());
        });
        buf.writeCollection(store, (b, o) -> {
            writeSafe(b, o.id());
            writeSafe(b, o.title());
            writeSafe(b, o.subtitle());
            b.writeVarLong(o.price());
            writeSafe(b, o.currency());
            writeSafe(b, o.badge());
            b.writeBoolean(o.owned());
        });
        buf.writeCollection(cases, (b, c) -> {
            writeSafe(b, c.id());
            writeSafe(b, c.title());
            b.writeVarInt(c.cost());
            b.writeVarInt(c.count());
            writeSafe(b, c.rarity());
            b.writeCollection(c.loot(), (b2, l) -> {
                writeSafe(b2, l.label());
                writeSafe(b2, l.rarity());
                b2.writeVarInt(l.weight());
                writeSafe(b2, l.item());
            });
        });
        buf.writeCollection(kits, (b, k) -> {
            writeSafe(b, k.id());
            writeSafe(b, k.title());
            writeSafe(b, k.description());
            writeSafe(b, k.badge());
            writeSafe(b, k.command());
        });
        buf.writeCollection(warps, (b, w) -> {
            writeSafe(b, w.id());
            writeSafe(b, w.title());
            writeSafe(b, w.description());
            writeSafe(b, w.tag());
            writeSafe(b, w.command());
        });
        buf.writeCollection(fishes, (b, f) -> {
            writeSafe(b, f.id());
            writeSafe(b, f.name());
            b.writeVarInt(f.count());
            b.writeVarLong(f.priceCoins());
            writeSafe(b, f.rarity());
            writeSafe(b, f.tag());
            b.writeFloat(f.demand());
        });

        writeSafe(buf, server.name());
        buf.writeVarInt(server.online());
        buf.writeVarInt(server.slots());
        buf.writeFloat(server.tps());
        writeSafe(buf, server.build());
        if (caseResult != null) {
            buf.writeBoolean(true);
            writeSafe(buf, caseResult.caseId());
            writeSafe(buf, caseResult.label());
            writeSafe(buf, caseResult.rarity());
            buf.writeVarInt(caseResult.amount());
            writeSafe(buf, caseResult.type());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeCollection(market, (b, m) -> {
            b.writeVarInt(m.id());
            writeSafe(b, m.label());
            b.writeVarInt(m.count());
            b.writeVarLong(m.price());
            writeSafe(b, m.seller());
            writeSafe(b, m.itemId());
            b.writeBoolean(m.self());
        });

        writeSafe(buf, eventLine == null ? "" : eventLine);
        buf.writeVarInt(quests == null ? 0 : quests.size());
        for (EventQuest q : quests == null ? List.<EventQuest>of() : quests) {
            buf.writeVarInt(q.idx());
            writeSafe(buf, q.desc());
            buf.writeVarInt(q.goal());
            buf.writeVarInt(q.progress());
            buf.writeVarLong(q.reward());
            buf.writeBoolean(q.claimed());
        }
    }

    public static HubSnapshot read(FriendlyByteBuf buf) {
        Profile profile = new Profile(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readVarInt(), buf.readFloat(),
                buf.readVarLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        Wallet wallet = new Wallet(buf.readVarLong(), buf.readVarLong(), buf.readVarInt(), buf.readBoolean());
        Season season = new Season(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(),
                buf.readBoolean(), buf.readVarInt(), buf.readList(FriendlyByteBuf::readVarInt));

        List<TopEntry> tops = buf.readList(b ->
                new TopEntry(b.readVarInt(), b.readUtf(), b.readUtf(), b.readBoolean()));
        List<Offer> store = buf.readList(b ->
                new Offer(b.readUtf(), b.readUtf(), b.readUtf(), b.readVarLong(), b.readUtf(), b.readUtf(), b.readBoolean()));
        List<CaseEntry> cases = buf.readList(b -> {
            String id = b.readUtf();
            String title = b.readUtf();
            int cost = b.readVarInt();
            int count = b.readVarInt();
            String rarity = b.readUtf();
            List<LootInfo> loot = b.readList(b2 ->
                    new LootInfo(b2.readUtf(), b2.readUtf(), b2.readVarInt(), b2.readUtf()));
            return new CaseEntry(id, title, cost, count, rarity, loot);
        });
        List<KitEntry> kits = buf.readList(b ->
                new KitEntry(b.readUtf(), b.readUtf(), b.readUtf(), b.readUtf(), b.readUtf()));
        List<WarpEntry> warps = buf.readList(b ->
                new WarpEntry(b.readUtf(), b.readUtf(), b.readUtf(), b.readUtf(), b.readUtf()));
        List<FishEntry> fishes = buf.readList(b ->
                new FishEntry(b.readUtf(), b.readUtf(), b.readVarInt(), b.readVarLong(), b.readUtf(), b.readUtf(), b.readFloat()));

        ServerInfo server = new ServerInfo(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readUtf());
        CaseResult caseResult = buf.readBoolean()
                ? new CaseResult(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readUtf())
                : null;
        List<MarketEntry> market = buf.readList(b ->
                new MarketEntry(b.readVarInt(), b.readUtf(), b.readVarInt(), b.readVarLong(), b.readUtf(), b.readUtf(), b.readBoolean()));
        String eventLine = buf.readUtf();
        List<EventQuest> quests = buf.readList(b ->
                new EventQuest(b.readVarInt(), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readVarLong(), b.readBoolean()));
        return new HubSnapshot(profile, wallet, season, tops, store, cases, kits, warps, fishes, server, caseResult, market, quests, eventLine);
    }
}
