package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import store.aquateche.aqualumen.common.data.HubSnapshot;

import java.util.List;
import java.util.Locale;

/**
 * F4 store SKUs. Site catalog slugs (sailor/skipper/…) map to the same LuckPerms groups.
 * Rub-price on the portal is gems in-game 1:1 for ranks.
 */
public final class StoreCatalog {

    public record Product(String id, String title, String subtitle, long price, String currency,
                          String kind, String payload) {
    }

    private static final List<Product> PRODUCTS = List.of(
            new Product("rank.sailor", "Моряк", "Префикс, 2 дома", 99, "gems", "lp_group", "sailor"),
            new Product("rank.skipper", "Шкипер", "Приоритет входа, кит", 249, "gems", "lp_group", "skipper"),
            new Product("rank.captain", "Капитан", "/fly на приватах", 499, "gems", "lp_group", "captain"),
            new Product("rank.admiral", "Адмирал", "/nick, 10 домов", 899, "gems", "lp_group", "admiral"),
            new Product("rank.legend", "Легенда", "Максимум домов и /hat", 1499, "gems", "lp_group", "legend"),
            new Product("rank.vip", "VIP", "/wb /ec /fly", 199, "gems", "lp_group", "vip"),
            new Product("kit.starter", "Набор Starter", "Стартовый набор F4", 120, "coins", "kit", "starter"),
            new Product("coins.500", "500 монет", "Обмен кристаллов", 50, "gems", "coins", "500"),
            new Product("case.ocean", "Океанский кейс", "Открыть за монеты", 0, "coins", "case", "ocean")
    );

    private static final String[] FLEET = {"sailor", "skipper", "captain", "admiral", "legend", "vip"};

    private StoreCatalog() {
    }

    public static List<HubSnapshot.Offer> offers(ServerPlayer player) {
        String rank = HubDataService.resolveRank(player).name().toLowerCase(Locale.ROOT);
        return PRODUCTS.stream().map(p -> {
            boolean owned = "lp_group".equals(p.kind) && rank.contains(p.payload);
            long price = p.price;
            if ("case".equals(p.kind)) {
                CaseConfig.CaseDef def = CaseConfig.find(p.payload);
                if (def != null) {
                    price = def.costCoins;
                }
            }
            return new HubSnapshot.Offer(p.id, p.title, p.subtitle, price, p.currency, owned ? "есть" : "", owned);
        }).toList();
    }

    public static void buy(ServerPlayer player, String offerId) {
        if (offerId == null || offerId.isBlank()) {
            player.sendSystemMessage(Component.literal("Нет такого товара").withStyle(ChatFormatting.RED));
            return;
        }
        Product product = find(offerId.trim());
        if (product == null) {
            player.sendSystemMessage(Component.literal("Нет такого товара").withStyle(ChatFormatting.RED));
            return;
        }
        if ("case".equals(product.kind)) {
            HubActionHandler.openCasePublic(player, product.payload);
            return;
        }
        long price = product.price;
        boolean paid = "gems".equals(product.currency)
                ? HubEconomy.trySpendGems(player, price)
                : HubEconomy.trySpendCoins(player, price);
        if (!paid) {
            player.sendSystemMessage(Component.literal("Не хватает " + ("gems".equals(product.currency) ? "кристаллов" : "монет"))
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (!fulfill(player, product.kind, product.payload)) {
            refund(player, product);
            player.sendSystemMessage(Component.literal("Выдача не прошла, средства возвращены").withStyle(ChatFormatting.RED));
            return;
        }
        player.sendSystemMessage(Component.literal("Куплено: " + product.title).withStyle(ChatFormatting.GREEN));
        HubDataService.push(player);
    }

    public static Product find(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        for (Product p : PRODUCTS) {
            if (p.id.equalsIgnoreCase(key) || p.payload.equalsIgnoreCase(key)) {
                return p;
            }
        }
        return null;
    }

    public static boolean fulfill(ServerPlayer player, String kind, String payload) {
        return switch (kind == null ? "" : kind) {
            case "lp_group" -> grantGroup(player, payload);
            case "kit" -> KitConfig.grantKit(player, payload);
            case "coins" -> {
                HubEconomy.grantCoins(player, parseLong(payload, 500));
                yield true;
            }
            case "gems" -> {
                HubEconomy.grantGems(player, (int) parseLong(payload, 10));
                yield true;
            }
            case "item" -> {
                giveItem(player, payload);
                yield true;
            }
            case "skin" -> applySkinUrl(player, payload);
            case "skin_clear" -> clearSkin(player);
            default -> false;
        };
    }

    private static void refund(ServerPlayer player, Product product) {
        if ("gems".equals(product.currency)) {
            HubEconomy.grantGems(player, (int) Math.min(product.price, Integer.MAX_VALUE));
        } else {
            HubEconomy.grantCoins(player, product.price);
        }
    }

    private static boolean grantGroup(ServerPlayer player, String group) {
        MinecraftServer server = player.getServer();
        if (server == null || group == null || group.isBlank()) {
            return false;
        }
        String name = player.getGameProfile().getName();
        CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        for (String fleet : FLEET) {
            server.getCommands().performPrefixedCommand(src, "lp user " + name + " parent remove " + fleet);
        }
        server.getCommands().performPrefixedCommand(src, "lp user " + name + " parent add " + group);
        server.getCommands().performPrefixedCommand(src, "lp user " + name + " parent switchprimarygroup " + group);
        return true;
    }

    private static void giveItem(ServerPlayer player, String spec) {
        String id = spec;
        int count = 1;
        int colon = spec.lastIndexOf(':');
        if (colon > spec.indexOf(':')) {
            try {
                count = Integer.parseInt(spec.substring(colon + 1));
                id = spec.substring(0, colon);
            } catch (NumberFormatException ignored) {
                count = 1;
            }
        }
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
        if (item == null || item == Items.AIR) {
            item = Items.PRISMARINE_SHARD;
        }
        HubEconomy.giveItem(player, new ItemStack(item, Math.max(1, count)));
    }

    private static boolean allowedSkinUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.startsWith("https://aquateche.store/api/skins/")
                || url.contains(".workers.dev/api/skins/");
    }

    private static boolean applySkinUrl(ServerPlayer player, String url) {
        if (!allowedSkinUrl(url)) {
            return false;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        String name = player.getGameProfile().getName();
        CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(src, "skin set " + name + " " + url);
        return true;
    }

    private static boolean clearSkin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        String name = player.getGameProfile().getName();
        CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(src, "skin clear " + name);
        return true;
    }

    private static long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
