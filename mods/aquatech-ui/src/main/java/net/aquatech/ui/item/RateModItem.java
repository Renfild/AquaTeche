package net.aquatech.ui.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Quantity multiplier for resource rods (StarCatcher bait slot). ~10k catches of durability. */
public class RateModItem extends Item {
    public static final int MAX_CATCHES = 10_000;

    private final RateTier tier;

    public enum RateTier {
        X2(2, "rate_x2", "Рейт ×2", "Удваивает количество добычи с ресурсной удочки"),
        X4(4, "rate_x4", "Рейт ×4", "Увеличивает добычу в 4 раза"),
        X8(8, "rate_x8", "Рейт ×8", "Увеличивает добычу в 8 раз"),
        X16(16, "rate_x16", "Рейт ×16", "Увеличивает добычу в 16 раз"),
        X32(32, "rate_x32", "Рейт ×32", "Увеличивает добычу в 32 раза"),
        X64(64, "rate_x64", "Рейт ×64", "Увеличивает добычу в 64 раза");

        private final int multiplier;
        private final String id;
        private final String russianName;
        private final String tooltip;

        RateTier(int multiplier, String id, String russianName, String tooltip) {
            this.multiplier = multiplier;
            this.id = id;
            this.russianName = russianName;
            this.tooltip = tooltip;
        }

        public int getMultiplier() { return multiplier; }
        public String getId() { return id; }
        public String getRussianName() { return russianName; }
        public String getTooltip() { return tooltip; }
    }

    public RateModItem(RateTier tier, Properties properties) {
        super(properties.stacksTo(1).durability(MAX_CATCHES));
        this.tier = tier;
    }

    public RateTier getTier() {
        return tier;
    }

    public int getMultiplier() {
        return tier.getMultiplier();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int left = Math.max(0, MAX_CATCHES - stack.getDamageValue());
        tooltip.add(Component.literal("§a" + tier.getTooltip()));
        tooltip.add(Component.literal("§7Вставь в §fслот наживки§7 (ящик снастей)."));
        tooltip.add(Component.literal("§eЗапас: §f" + left + "§7/§f" + MAX_CATCHES + " §7уловов"));
        tooltip.add(Component.literal("§7Или в §fслот рейта§7 авторыбалки."));
        tooltip.add(Component.literal("§8База улова без рейта: 2–4 шт."));
    }
}
