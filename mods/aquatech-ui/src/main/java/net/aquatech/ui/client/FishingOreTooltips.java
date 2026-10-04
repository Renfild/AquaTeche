package net.aquatech.ui.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Подсказка «где ловится» у ресурсов: с какой удочки предмет падает впервые и с каких ещё. Данные берёт из
 * {@link RodDropTable}, который собирается из настоящих пулов дропа (tools/build_rod_drop_table.py), поэтому тир в
 * подсказке всегда совпадает с игрой. Заодно вычищает старые описания жил Industrial Upgrade.
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui", value = Dist.CLIENT)
public final class FishingOreTooltips {

    /** Названия ресурсных удочек по тиру (индекс = тир), как в названиях предметов. */
    private static final String[] ROD_NAMES = {
            "",
            "Бамбуковая удочка", "Скромная удочка", "Старая добрая удочка", "Удочка натуралиста", "Слизневая удочка",
            "Ледяная удочка", "Удочка Ловца Звёзд", "Лазурная удочка", "Удочка из акульего зуба", "Обсидиановая удочка",
            "Удочка из светящихся ягод", "Магматическая удочка", "Альфа-удочка"
    };
    private static final int MAX_TIER = 13;
    private static final int LISTED_TIERS = 5;

    private FishingOreTooltips() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // 1. Старые описания жил, списки руд в скобках и подсказки про Shift
        event.getToolTip().removeIf(component -> {
            String text = component.getString().toLowerCase();
            return text.contains("жила") || text.contains("жилах") || text.contains("жиле")
                    || text.contains("жилы") || text.contains("жилу") || text.contains("жил")
                    || text.contains("ищите") || text.contains("камни")
                    || text.contains("добывается") || text.contains("удерживайте")
                    || text.contains("shift") || text.contains("подробной информации")
                    || text.contains("можно найти") || text.contains("генерируется")
                    || (text.startsWith("[") && text.endsWith("]"));
        });

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;
        int[] tiers = RodDropTable.tiersOf(id.toString());
        if (tiers == null || tiers.length == 0) return;

        List<Component> lines = describe(tiers);
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.literal("AquaTech · Рыбалка").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        event.getToolTip().addAll(lines);
    }

    /** Строки подсказки: самая ранняя ресурсная удочка и список тиров, с которых предмет падает. */
    static List<Component> describe(int[] sortedTiers) {
        List<Integer> resource = new ArrayList<>();
        boolean bone = false;
        boolean sky = false;
        for (int tier : sortedTiers) {
            if (tier == RodDropTable.BONER) bone = true;
            else if (tier == RodDropTable.SKY) sky = true;
            else resource.add(tier);
        }
        List<Component> out = new ArrayList<>();
        if (!resource.isEmpty()) {
            int first = resource.get(0);
            out.add(Component.literal("Ловится удочкой: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(ROD_NAMES[first] + " (Т-" + first + ")")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            out.add(Component.literal(tierList(resource)).withStyle(ChatFormatting.GRAY));
        }
        if (bone) {
            out.add(Component.literal(resource.isEmpty() ? "Ловится удочкой: " : "Также: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("Костяная удочка (мобы)").withStyle(ChatFormatting.YELLOW)));
        }
        if (sky) {
            out.add(Component.literal(resource.isEmpty() && !bone ? "Ловится удочкой: " : "Также: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("Небесная удочка (рыба)").withStyle(ChatFormatting.YELLOW)));
        }
        return out;
    }

    /** «С Т-2 и выше», если дроп идёт на всех старших тирах, иначе список («Выпадает на Т-2, Т-3, Т-5…»). */
    static String tierList(List<Integer> tiers) {
        int first = tiers.get(0);
        boolean everyHigher = tiers.size() == MAX_TIER - first + 1;
        if (everyHigher) {
            return first >= MAX_TIER ? "Только на Т-" + MAX_TIER : "С Т-" + first + " и выше";
        }
        StringBuilder text = new StringBuilder("Выпадает на ");
        int shown = Math.min(LISTED_TIERS, tiers.size());
        for (int i = 0; i < shown; i++) {
            text.append(i == 0 ? "" : ", ").append("Т-").append(tiers.get(i));
        }
        if (tiers.size() > shown) {
            text.append(" и ещё ").append(tiers.size() - shown);
        }
        return text.toString();
    }
}
