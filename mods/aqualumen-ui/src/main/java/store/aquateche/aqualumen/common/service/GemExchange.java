package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.List;

/**
 * Обмен кристаллов (гемов) на монеты и обратно во вкладке «Магазин». Курсы лежат в конфиге; продажа кристалла
 * дешевле покупки, поэтому обмен туда и обратно всегда в минус и деньги из него не вытащить.
 */
public final class GemExchange {

    public static final String OFFER_BUY = "exchange.buy";
    public static final String OFFER_SELL = "exchange.sell";
    /** За одно нажатие не больше, чем влезает в сообщение и в int-счёт. */
    public static final int MAX_PER_ACTION = 1000;

    private GemExchange() {
    }

    /** Две служебные карточки: меню читает из них курсы и рисует панель обмена. */
    public static List<HubSnapshot.Offer> offers() {
        long buy = LumenConfig.COMMON.gemBuyCoins.get();
        long sell = LumenConfig.COMMON.gemSellCoins.get();
        return List.of(
                new HubSnapshot.Offer(OFFER_BUY, "Монеты → кристаллы", "1 кристалл = " + HubEconomy.formatCoins(buy) + " монет",
                        buy, "coins", "", false),
                new HubSnapshot.Offer(OFFER_SELL, "Кристаллы → монеты", "1 кристалл = " + HubEconomy.formatCoins(sell) + " монет",
                        sell, "coins", "", false));
    }

    /** Количество из аргумента действия: целое от 1 до MAX_PER_ACTION, иначе 0. */
    public static int parseAmount(String argument) {
        if (argument == null) return 0;
        try {
            int value = Integer.parseInt(argument.trim());
            return value >= 1 && value <= MAX_PER_ACTION ? value : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static long buyCost(int gems, long coinsPerGem) {
        return (long) gems * coinsPerGem;
    }

    public static long sellPayout(int gems, long coinsPerGem) {
        return (long) gems * coinsPerGem;
    }

    public static void buyGems(ServerPlayer player, String argument) {
        int gems = parseAmount(argument);
        if (gems <= 0) {
            player.sendSystemMessage(Component.literal("Укажите количество кристаллов от 1 до " + MAX_PER_ACTION)
                    .withStyle(ChatFormatting.RED));
            return;
        }
        long cost = buyCost(gems, LumenConfig.COMMON.gemBuyCoins.get());
        if (!HubEconomy.trySpendCoins(player, cost)) {
            player.sendSystemMessage(Component.literal("Не хватает монет: нужно " + HubEconomy.formatCoins(cost)
                    + " ¤, есть " + HubEconomy.formatCoins(HubEconomy.coins(player))).withStyle(ChatFormatting.RED));
            return;
        }
        HubEconomy.grantGems(player, gems);
        player.sendSystemMessage(Component.literal("Обмен: −" + HubEconomy.formatCoins(cost) + " ¤, +" + gems + " кристаллов")
                .withStyle(ChatFormatting.AQUA));
        HubDataService.push(player);
    }

    public static void sellGems(ServerPlayer player, String argument) {
        int gems = parseAmount(argument);
        if (gems <= 0) {
            player.sendSystemMessage(Component.literal("Укажите количество кристаллов от 1 до " + MAX_PER_ACTION)
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (!HubEconomy.trySpendGems(player, gems)) {
            player.sendSystemMessage(Component.literal("Не хватает кристаллов: нужно " + gems).withStyle(ChatFormatting.RED));
            return;
        }
        long payout = sellPayout(gems, LumenConfig.COMMON.gemSellCoins.get());
        HubEconomy.grantCoins(player, payout);
        player.sendSystemMessage(Component.literal("Обмен: −" + gems + " кристаллов, +" + HubEconomy.formatCoins(payout) + " ¤")
                .withStyle(ChatFormatting.GOLD));
        HubDataService.push(player);
    }
}
