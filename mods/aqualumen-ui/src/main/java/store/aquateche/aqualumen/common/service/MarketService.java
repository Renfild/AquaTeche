package store.aquateche.aqualumen.common.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import store.aquateche.aqualumen.common.data.HubSnapshot;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lumen Market — player-to-player item trading for server coins.
 * Listings live in the portal database (aquateche.store/api/market); the server
 * mediates every request with the sync key. Seller proceeds land on the portal
 * wallet at buy time and flow back in-game through the regular coin sync.
 */
public final class MarketService {

    private static final AtomicBoolean FETCHING = new AtomicBoolean();
    private static volatile List<HubSnapshot.MarketEntry> cache = List.of();
    private static volatile long cacheAt;

    private MarketService() {
    }

    public static List<HubSnapshot.MarketEntry> cached() {
        refreshAsync();
        return cache;
    }

    public static void refreshAsync() {
        long now = System.currentTimeMillis();
        if (now - cacheAt < 30_000L || !FETCHING.compareAndSet(false, true)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                fetchNow();
            } finally {
                FETCHING.set(false);
            }
        });
    }

    /** Blocking fetch — call from an async thread so a push right after sell/buy/cancel shows fresh lots. */
    private static void fetchNow() {
        try {
            String key = HubDataService.resolveSyncKey();
            if (key == null) {
                return;
            }
            HttpURLConnection conn = get("https://aquateche.store/api/market?limit=40");
            conn.setRequestProperty("X-AquaTech-Server-Key", key);
            if (conn.getResponseCode() != 200) {
                return;
            }
            JsonObject res = parse(conn);
            JsonArray lots = res.getAsJsonArray("lots");
            List<HubSnapshot.MarketEntry> list = new ArrayList<>();
            for (int i = 0; i < lots.size() && i < 40; i++) {
                JsonObject lot = lots.get(i).getAsJsonObject();
                list.add(new HubSnapshot.MarketEntry(
                        lot.get("id").getAsInt(),
                        lot.has("label") ? lot.get("label").getAsString() : lot.get("item_id").getAsString(),
                        lot.get("count").getAsInt(),
                        lot.get("price").getAsLong(),
                        lot.get("seller").getAsString(),
                        lot.has("item_id") ? lot.get("item_id").getAsString() : "",
                        false));
            }
            cache = list;
            cacheAt = System.currentTimeMillis();
        } catch (Throwable ignored) {
        }
    }

    /** Lists the item held in the main hand for {@code price} coins. */
    public static void sell(ServerPlayer player, long price) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Component.literal("§e[Рынок] Возьмите продаваемый предмет в главную руку."));
            return;
        }
        if (price < 1) {
            player.sendSystemMessage(Component.literal("§e[Рынок] Цена должна быть больше нуля: /ah sell <цена>"));
            return;
        }
        CompoundTag tag = held.save(new CompoundTag());
        String nbt = tag.toString();
        String label = held.getHoverName().getString();
        int count = held.getCount();
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString();

        CompletableFuture.runAsync(() -> {
            try {
                String key = HubDataService.resolveSyncKey();
                if (key == null) {
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("op", "sell");
                body.addProperty("nick", player.getGameProfile().getName());
                body.addProperty("item_id", itemId);
                body.addProperty("label", label);
                body.addProperty("nbt", nbt);
                body.addProperty("count", count);
                body.addProperty("price", price);
                JsonObject res = post("https://aquateche.store/api/market", body, key);
                if (res.has("ok") && res.get("ok").getAsBoolean()) {
                    int id = res.get("id").getAsInt();
                    player.getServer().execute(() -> {
                        ItemStack main = player.getMainHandItem();
                        if (!main.isEmpty() && main.getCount() == count) {
                            player.getMainHandItem().setCount(0);
                        }
                        player.sendSystemMessage(Component.literal("§a[Рынок] Лот #" + id + " выставлен: "
                                + label + " ×" + count + " за " + price + " монет."));
                        fetchNow();
                        HubDataService.push(player);
                    });
                } else {
                    String error = res.has("error") ? res.get("error").getAsString() : "ошибка";
                    player.getServer().execute(() -> player.sendSystemMessage(
                            Component.literal("§c[Рынок] Не удалось выставить лот: " + error)));
                }
            } catch (Throwable t) {
                player.getServer().execute(() -> player.sendSystemMessage(
                        Component.literal("§c[Рынок] Сервис рынка недоступен, попробуйте позже.")));
            }
        });
    }

    /** Buys lot {@code id} for the viewing player. */
    public static void buy(ServerPlayer player, int id) {
        long balance = HubEconomy.coins(player);
        HubSnapshot.MarketEntry entry = find(cache, id);
        if (entry != null) {
            if (entry.seller().equalsIgnoreCase(player.getGameProfile().getName())) {
                player.sendSystemMessage(Component.literal("§e[Рынок] Это ваш лот — отмените его: /ah cancel " + id));
                return;
            }
            if (balance < entry.price()) {
                player.sendSystemMessage(Component.literal("§c[Рынок] Не хватает монет: нужно " + entry.price()));
                return;
            }
        }
        CompletableFuture.runAsync(() -> {
            try {
                String key = HubDataService.resolveSyncKey();
                if (key == null) {
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("op", "buy");
                body.addProperty("id", id);
                body.addProperty("buyer", player.getGameProfile().getName());
                JsonObject res = post("https://aquateche.store/api/market", body, key);
                boolean ok = res.has("ok") && res.get("ok").getAsBoolean();
                player.getServer().execute(() -> {
                    if (!ok) {
                        String error = res.has("error") ? res.get("error").getAsString() : "лот недоступен";
                        player.sendSystemMessage(Component.literal("§c[Рынок] " + error));
                        fetchNow();
                        HubDataService.push(player);
                        return;
                    }
                    JsonObject lot = res.getAsJsonObject("lot");
                    long price = lot.get("price").getAsLong();
                    String seller = lot.get("seller").getAsString();
                    if (HubEconomy.trySpendCoins(player, price)) {
                        ItemStack stack = restore(lot);
                        HubEconomy.giveItem(player, stack);
                        player.sendSystemMessage(Component.literal("§a[Рынок] Куплено: " + lot.get("label").getAsString()
                                + " ×" + lot.get("count").getAsInt() + " за " + price + " монет."));
                        if (seller.equalsIgnoreCase(player.getGameProfile().getName())) {
                            player.sendSystemMessage(Component.literal("§7[Рынок] Выкуп собственного лота — монеты вернулись на портал."));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§c[Рынок] Не хватает монет."));
                    }
                    fetchNow();
                    HubDataService.push(player);
                });
            } catch (Throwable t) {
                player.getServer().execute(() -> player.sendSystemMessage(
                        Component.literal("§c[Рынок] Сервис рынка недоступен, попробуйте позже.")));
            }
        });
    }

    /** Cancels the player's own lot and returns the item. */
    public static void cancel(ServerPlayer player, int id) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = HubDataService.resolveSyncKey();
                if (key == null) {
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("op", "cancel");
                body.addProperty("id", id);
                body.addProperty("nick", player.getGameProfile().getName());
                JsonObject res = post("https://aquateche.store/api/market", body, key);
                boolean ok = res.has("ok") && res.get("ok").getAsBoolean();
                player.getServer().execute(() -> {
                    if (ok) {
                        ItemStack stack = restore(res.getAsJsonObject("lot"));
                        HubEconomy.giveItem(player, stack);
                        player.sendSystemMessage(Component.literal("§a[Рынок] Лот #" + id + " отменён, предмет возвращён."));
                    } else {
                        String error = res.has("error") ? res.get("error").getAsString() : "лот не найден";
                        player.sendSystemMessage(Component.literal("§c[Рынок] " + error));
                    }
                    fetchNow();
                    HubDataService.push(player);
                });
            } catch (Throwable t) {
                player.getServer().execute(() -> player.sendSystemMessage(
                        Component.literal("§c[Рынок] Сервис рынка недоступен.")));
            }
        });
    }

    private static HubSnapshot.MarketEntry find(List<HubSnapshot.MarketEntry> list, int id) {
        for (HubSnapshot.MarketEntry entry : list) {
            if (entry.id() == id) {
                return entry;
            }
        }
        return null;
    }

    private static ItemStack restore(JsonObject lot) {
        try {
            CompoundTag tag = TagParser.parseTag(lot.get("nbt").getAsString());
            return ItemStack.of(tag);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    private static HttpURLConnection get(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(5000);
        return conn;
    }

    private static JsonObject post(String url, JsonObject body, String key) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("X-AquaTech-Server-Key", key);
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        if (conn.getResponseCode() != 200) {
            InputStream es = conn.getErrorStream();
            String text = es == null ? "" : new String(es.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject res = new JsonObject();
            res.addProperty("ok", false);
            String marker = "\"error\":\"";
            int at = text.indexOf(marker);
            if (at >= 0) {
                res.addProperty("error", text.substring(at + marker.length(), text.indexOf('"', at + marker.length())));
            }
            return res;
        }
        return parse(conn);
    }

    private static JsonObject parse(HttpURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
