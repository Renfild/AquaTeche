package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.config.LumenConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Серверная трансляция редких наград: чат всем + звук + вебхук Discord + запись в лог.
 *
 * Редкий дроп происходит у одного игрока в его интерфейсе, поэтому о нём никто не узнаёт.
 * Здесь такое событие становится общим поводом — и заодно бесплатным контентом: игроки сами
 * снимают момент. Поэтому announce включается по умолчанию, а не спрятан за флагом.
 */
public final class RareDropAnnounce {

    private static final Map<String, Integer> RARITY_RANK = Map.of(
            "common", 0,
            "rare", 1,
            "epic", 2,
            "legendary", 3
    );
    private static final AtomicLong LAST_ANNOUNCE_MS = new AtomicLong(0L);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private RareDropAnnounce() {
    }

    public static int rank(String rarity) {
        return RARITY_RANK.getOrDefault(rarity == null ? "" : rarity.trim().toLowerCase(java.util.Locale.ROOT), 0);
    }

    public static boolean qualifies(String rarity) {
        return rank(rarity) >= rank(threshold());
    }

    private static String threshold() {
        String raw = LumenConfig.COMMON.announceMinRarity.get();
        return rank(raw) > 0 ? raw.trim().toLowerCase(java.util.Locale.ROOT) : "epic";
    }

    /**
     * @param rewardLabel человекочитаемое название награды
     * @param rarity       редкость выпавшей награды (common/rare/epic/legendary)
     * @param pity         награда выдана гарантом
     */
    public static void caseDrop(MinecraftServer server, ServerPlayer winner, String caseTitle,
                                String rewardLabel, String rarity, int amount, boolean pity) {
        if (server == null || winner == null || !LumenConfig.COMMON.announceCaseDrops.get()) {
            return;
        }
        if (!qualifies(rarity)) {
            return;
        }
        boolean legendary = rank(rarity) >= rank("legendary");
        long now = System.currentTimeMillis();
        long cooldownMs = Math.max(0, LumenConfig.COMMON.announceCooldownSeconds.get()) * 1000L;
        long last = LAST_ANNOUNCE_MS.get();
        if (!legendary && now - last < cooldownMs) {
            return;
        }
        LAST_ANNOUNCE_MS.set(now);

        ChatFormatting color = legendary ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE;
        String star = legendary ? "§6§l★" : "§d✦";
        String who = winner.getGameProfile().getName();
        String qty = amount > 1 ? " §7×" + amount : "";
        String pityTag = pity ? " §8(гарант)" : "";

        String line = star + " §f" + who + " §7открыл §b" + caseTitle + " §fи вытащил "
                + color + rewardLabel + color + qty + pityTag;
        Component message = Component.literal(line);
        server.getPlayerList().broadcastSystemMessage(message, false);
        AquaLumenUI.LOGGER.info("Rare drop: {} got {} x{} from {} ({}{})",
                who, rewardLabel, amount, caseTitle, rarity, pity ? ", pity" : "");

        playSound(server, legendary);
        sendDiscord(who, caseTitle, rewardLabel + qty, rarity, amount, pity);
    }

    private static void playSound(MinecraftServer server, boolean legendary) {
        if (!LumenConfig.COMMON.announceSound.get()) {
            return;
        }
        SoundEvent sound = legendary ? SoundEvents.AMETHYST_BLOCK_RESONATE : SoundEvents.AMETHYST_BLOCK_CHIME;
        float volume = legendary ? 0.9F : 0.65F;
        float pitch = legendary ? 1.0F : 1.5F;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    private static void sendDiscord(String who, String caseTitle, String reward, String rarity,
                                    int amount, boolean pity) {
        String url = LumenConfig.COMMON.announceDiscordWebhook.get();
        if (url == null || url.isBlank() || !url.startsWith("https://")) {
            return;
        }
        String plain = who + " открыл " + caseTitle + " и вытащил " + reward
                + " [" + rarity + "]" + (pity ? " (гарант)" : "");
        int color = rank(rarity) >= rank("legendary") ? 0xF5C25B : 0xA44EE8;
        String json = "{\"username\":\"AquaTech\",\"content\":\"" + escape(plain) + "\","
                + "\"embeds\":[{\"title\":\"" + escape(reward) + "\",\"description\":\"" + escape(caseTitle) + "\","
                + "\"color\":" + color + ",\"footer\":{\"text\":\"" + escape("редкость: " + rarity) + "\"},"
                + "\"fields\":[{\"name\":\"игрок\",\"value\":\"" + escape(who) + "\",\"inline\":true},"
                + "{\"name\":\"количество\",\"value\":\"" + amount + "\",\"inline\":true}]}]}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(err -> {
                    AquaLumenUI.LOGGER.warn("Announce webhook failed: {}", String.valueOf(err.getMessage()));
                    return null;
                });
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    /** Rare tiers used by the hub UI, in ascending order. */
    public static List<String> tiers() {
        return List.of("common", "rare", "epic", "legendary");
    }
}
