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
                          ServerInfo server) {

    public record Profile(String name, String rank, int rankColor, int level, float levelProgress,
                          long playtimeMinutes, int kills, int deaths, int quests, int friendsOnline) {
    }

    public record Wallet(long coins, long gems, int dailyStreak, boolean dailyAvailable) {
    }

    public record Season(String title, int tier, int maxTier, float tierProgress, boolean premium, int claimable) {
    }

    public record TopEntry(int place, String player, String value, boolean self) {
    }

    public record Offer(String id, String title, String subtitle, long price, String currency, String badge, boolean owned) {
    }

    public record CaseEntry(String id, String title, int count, String rarity) {
    }

    public record ServerInfo(String name, int online, int slots, float tps, String build) {
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(profile.name());
        buf.writeUtf(profile.rank());
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

        buf.writeUtf(season.title());
        buf.writeVarInt(season.tier());
        buf.writeVarInt(season.maxTier());
        buf.writeFloat(season.tierProgress());
        buf.writeBoolean(season.premium());
        buf.writeVarInt(season.claimable());

        buf.writeCollection(tops, (b, e) -> {
            b.writeVarInt(e.place());
            b.writeUtf(e.player());
            b.writeUtf(e.value());
            b.writeBoolean(e.self());
        });
        buf.writeCollection(store, (b, o) -> {
            b.writeUtf(o.id());
            b.writeUtf(o.title());
            b.writeUtf(o.subtitle());
            b.writeVarLong(o.price());
            b.writeUtf(o.currency());
            b.writeUtf(o.badge());
            b.writeBoolean(o.owned());
        });
        buf.writeCollection(cases, (b, c) -> {
            b.writeUtf(c.id());
            b.writeUtf(c.title());
            b.writeVarInt(c.count());
            b.writeUtf(c.rarity());
        });

        buf.writeUtf(server.name());
        buf.writeVarInt(server.online());
        buf.writeVarInt(server.slots());
        buf.writeFloat(server.tps());
        buf.writeUtf(server.build());
    }

    public static HubSnapshot read(FriendlyByteBuf buf) {
        Profile profile = new Profile(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readVarInt(), buf.readFloat(),
                buf.readVarLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        Wallet wallet = new Wallet(buf.readVarLong(), buf.readVarLong(), buf.readVarInt(), buf.readBoolean());
        Season season = new Season(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(),
                buf.readBoolean(), buf.readVarInt());

        List<TopEntry> tops = buf.readList(b ->
                new TopEntry(b.readVarInt(), b.readUtf(), b.readUtf(), b.readBoolean()));
        List<Offer> store = buf.readList(b ->
                new Offer(b.readUtf(), b.readUtf(), b.readUtf(), b.readVarLong(), b.readUtf(), b.readUtf(), b.readBoolean()));
        List<CaseEntry> cases = buf.readList(b ->
                new CaseEntry(b.readUtf(), b.readUtf(), b.readVarInt(), b.readUtf()));

        ServerInfo server = new ServerInfo(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readUtf());
        return new HubSnapshot(profile, wallet, season, tops, store, cases, server);
    }
}
