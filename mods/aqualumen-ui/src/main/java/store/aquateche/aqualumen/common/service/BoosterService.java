package store.aquateche.aqualumen.common.service;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.common.service.BoosterLogic.Activation;
import store.aquateche.aqualumen.common.service.BoosterLogic.Active;
import store.aquateche.aqualumen.common.service.BoosterLogic.Stock;
import store.aquateche.aqualumen.common.service.BoosterLogic.Tier;

/**
 * Stores boosters in the player's persistent data under {@link #TAG}: {small:int, large:int, mult:double, until:long}.
 * aquatech_ui adds to small/large (same tag, no compile dependency); this class owns activation and the sale bonus.
 */
public final class BoosterService {

    public static final String TAG = "aqualumen_boosters";
    private static final String PREFIX = "§e[Бустер скупщика] ";

    private BoosterService() {
    }

    private static Stock stock(CompoundTag tag) {
        return new Stock(Math.max(0, tag.getInt("small")), Math.max(0, tag.getInt("large")));
    }

    private static Active active(CompoundTag tag) {
        return tag.contains("until") ? new Active(tag.getDouble("mult"), tag.getLong("until")) : Active.NONE;
    }

    private static void write(ServerPlayer player, Stock stock, Active active) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("small", stock.small());
        tag.putInt("large", stock.large());
        tag.putDouble("mult", active.mult());
        tag.putLong("until", active.until());
        player.getPersistentData().put(TAG, tag);
    }

    /** Adds boosters to the player's stock (admin command; aquatech_ui writes the same tag for prizes). */
    public static void grant(ServerPlayer player, Tier tier, int amount) {
        if (amount <= 0) {
            return;
        }
        CompoundTag tag = player.getPersistentData().getCompound(TAG);
        write(player, stock(tag).plus(tier, amount), active(tag));
        send(player, "§a+" + amount + " " + tier.label() + " ×" + trim(tier.mult()) + " · " + (tier.durationMs() / 60_000L)
                + " мин §7(/booster)");
    }

    /** Extra coins the running booster adds to a sale; 0 when none runs. */
    public static long bonusFor(ServerPlayer player, long baseCoins) {
        Active active = active(player.getPersistentData().getCompound(TAG));
        return BoosterLogic.bonus(baseCoins, BoosterLogic.multiplierAt(active, System.currentTimeMillis()));
    }

    public static void announceBonus(ServerPlayer player, long bonus) {
        if (bonus <= 0L) {
            return;
        }
        Active active = active(player.getPersistentData().getCompound(TAG));
        long left = active.remainingMs(System.currentTimeMillis());
        player.sendSystemMessage(Component.literal("§e[Бустер] §7×" + trim(active.mult())
                + " добавил §6+" + bonus + " §7· осталось " + BoosterLogic.formatRemaining(left)));
    }

    public static int status(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().getCompound(TAG);
        Stock stock = stock(tag);
        Active active = active(tag);
        long now = System.currentTimeMillis();
        if (active.isActive(now)) {
            send(player, "§aИдёт ×" + trim(active.mult()) + ", осталось " + BoosterLogic.formatRemaining(active.remainingMs(now)) + ".");
        } else {
            send(player, "§7Активного бустера нет.");
        }
        player.sendSystemMessage(Component.literal("§7В запасе: §a" + Tier.SMALL.label() + " ×" + trim(Tier.SMALL.mult()) + " · "
                + (Tier.SMALL.durationMs() / 60_000L) + " мин §f×" + stock.small() + "§7, §b" + Tier.LARGE.label() + " ×" + trim(Tier.LARGE.mult()) + " · "
                + (Tier.LARGE.durationMs() / 60_000L) + " мин §f×" + stock.large()));
        player.sendSystemMessage(Component.literal("§7Включить: §f/booster use малый §7или §f/booster use большой"));
        return 1;
    }

    public static int use(ServerPlayer player, String tierName) {
        Tier tier = BoosterLogic.parse(tierName);
        if (tier == null) {
            send(player, "§cНужен вид: §f/booster use малый §cили §f/booster use большой");
            return 0;
        }
        CompoundTag tag = player.getPersistentData().getCompound(TAG);
        long now = System.currentTimeMillis();
        Active before = active(tag);
        Activation result = BoosterLogic.activate(stock(tag), before, tier, now);
        switch (result.result()) {
            case OK -> {
                write(player, result.stock(), result.active());
                send(player, "§aВключён ×" + trim(tier.mult()) + " на " + (tier.durationMs() / 60_000L)
                        + " минут. §7Продажа рыбы платит больше.");
                return 1;
            }
            case ALREADY_ACTIVE -> {
                send(player, "§cУже идёт ×" + trim(before.mult()) + ", осталось "
                        + BoosterLogic.formatRemaining(before.remainingMs(now)) + ". §fБустер не потрачен.");
                return 0;
            }
            default -> {
                send(player, "§cНет бустера «" + tier.label() + "» в запасе. §fИх дают за турнир и сундук из глубин.");
                return 0;
            }
        }
    }

    /** Action-bar timer for every player whose booster is running. Called once per second from the server tick. */
    public static void tickActionBar(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CompoundTag tag = player.getPersistentData().getCompound(TAG);
            if (!tag.contains("until")) {
                continue;
            }
            Active active = active(tag);
            if (active.isActive(now)) {
                player.displayClientMessage(Component.literal("§6Бустер ×" + trim(active.mult()) + " · "
                        + BoosterLogic.formatRemaining(active.remainingMs(now))), true);
            } else {
                CompoundTag cleaned = tag.copy();
                cleaned.remove("until");
                cleaned.remove("mult");
                player.getPersistentData().put(TAG, cleaned);
                send(player, "§7Бустер ×" + trim(active.mult()) + " закончился.");
            }
        }
    }

    private static void send(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX + message));
    }

    private static String trim(double mult) {
        return mult == Math.rint(mult) ? String.valueOf((long) mult) : String.valueOf(mult);
    }
}
