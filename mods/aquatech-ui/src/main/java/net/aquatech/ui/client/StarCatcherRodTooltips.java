package net.aquatech.ui.client;

import net.aquatech.ui.fishing.AquaTechFishingRodItem;
import net.aquatech.ui.fishing.FishingLootHandler;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.fishing.RodDurability;
import net.aquatech.ui.fishing.RodLootRanges;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "aquatech_ui", value = Dist.CLIENT)
public final class StarCatcherRodTooltips {
    private StarCatcherRodTooltips() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!FishingRodCompat.isSupportedRod(stack)) return;

        boolean shift = ClientItemActions.hasShiftDown();
        var tip = event.getToolTip();

        tip.add(Component.empty());

        int left = RodDurability.remaining(stack);
        int max = RodDurability.maxUses(stack);

        if (FishingRodCompat.isFishOnlyRod(stack)) {
            tip.add(Component.literal("AquaTech").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
            tip.add(Component.literal("Только рыба StarCatcher").withStyle(ChatFormatting.AQUA));
            tip.add(Component.literal("Осталось уловов: " + left + " / " + max)
                    .withStyle(left <= max / 10 ? ChatFormatting.RED : ChatFormatting.YELLOW));
            if (shift) {
                tip.add(Component.empty());
                tip.add(Component.literal("Ловит рыбу мода StarCatcher.").withStyle(ChatFormatting.GRAY));
                tip.add(Component.literal("Не даёт IU-ресурсы.").withStyle(ChatFormatting.GRAY));
                tip.add(Component.literal("Нельзя ставить в авторыбалку.").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tip.add(Component.literal("Зажми SHIFT — подробности").withStyle(ChatFormatting.DARK_GRAY));
            }
            return;
        }

        AquaTechFishingRodItem.RodType type = FishingRodCompat.resolveRodType(stack);
        if (type == null) return;

        int rate = FishingLootHandler.readRateMultiplier(stack);
        tip.add(Component.literal("AquaTech · Ресурсы").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tip.add(Component.literal("Рейт: ×" + rate + (rate <= 1 ? " (база)" : ""))
                .withStyle(rate > 1 ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        tip.add(Component.literal("Осталось уловов: " + left + " / " + max)
                .withStyle(left <= max / 10 ? ChatFormatting.RED : ChatFormatting.YELLOW));

        if (shift) {
            tip.add(Component.empty());
            tip.add(Component.literal("Улов").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
            for (Component line : lootLines(type)) {
                tip.add(line);
            }
            tip.add(Component.empty());
            int lo = RodLootRanges.min(type) * Math.max(1, rate);
            int hi = RodLootRanges.max(type) * Math.max(1, rate);
            tip.add(Component.literal("За заброс: " + lo + "–" + hi + " шт.")
                    .withStyle(ChatFormatting.GOLD));
            tip.add(Component.literal("База 2–4, умножается рейтом.").withStyle(ChatFormatting.DARK_GRAY));
            tip.add(Component.empty());
            tip.add(Component.literal("Рейт").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE));
            tip.add(Component.literal("Вставь rate_x2…x64 в слот наживки").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("через ящик снастей StarCatcher.").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("Или в слот рейта авторыбалки.").withStyle(ChatFormatting.GRAY));
        } else {
            tip.add(Component.literal("Зажми SHIFT — таблица улова").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static Component[] lootLines(AquaTechFishingRodItem.RodType type) {
        return switch (type) {
            case IRON -> new Component[]{
                    line("Руда (шанс): железо, титан, иттрий, шпинель, олово…"),
                    line("Дуб / саженцы / земля; глина / гравий / песок"),
                    line("1–3 предмета за улов, не всё сразу")
            };
            case GOLD -> new Component[]{
                    line("Руда (шанс): вольфрам, хром, алюминий, медь, золото…"),
                    line("Кремень; сапфир / топаз; ведро лавы"),
                    line("1–3 предмета за улов")
            };
            case DIAMOND -> new Component[]{
                    line("Алмаз, руда олова/меди, призмарин (шанс)"),
                    line("Титан / алюминий / кобальт (руда)"),
                    line("Нефть IU (шанс)")
            };
            case PRISMARINE -> new Component[]{
                    line("Призмарин / кристаллы / руда олова"),
                    line("Латекс, гевея, торф, нефть"),
                    line("Редкие руды IU (шанс)")
            };
            case NETHERITE -> new Component[]{
                    line("Незерит-лом, кварц, медная руда"),
                    line("Платина / вольфрам (руда)"),
                    line("Минералы и нефть IU")
            };
            default -> new Component[]{line("Ресурсы Industrial Upgrade")};
        };
    }

    private static Component line(String text) {
        return Component.literal("• " + text).withStyle(ChatFormatting.GRAY);
    }
}
