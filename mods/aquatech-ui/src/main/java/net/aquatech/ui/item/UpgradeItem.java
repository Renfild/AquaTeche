package net.aquatech.ui.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeItem extends Item {
    private final UpgradeType type;

    public enum UpgradeType {
        SPEED("speed", "Модуль ускорения", "Увеличивает скорость работы механизма"),
        SPEED_X4("speed_x4", "Модуль ускорения ×4", "Максимально ускоряет работу механизма в 4 раза (прогресс +4 за тик)"),
        EFFICIENCY("efficiency", "Модуль энергоэффективности", "Снижает потребление энергии FE на 50%"),
        OCEAN_BOUNTY("ocean_bounty", "Модуль океанического изобилия", "Авторыболов вылавливает ценную рыбу StarCatcher всех типов для продажи скупщику рыбы");

        private final String id;
        private final String russianName;
        private final String tooltip;

        UpgradeType(String id, String russianName, String tooltip) {
            this.id = id;
            this.russianName = russianName;
            this.tooltip = tooltip;
        }

        public String getId() { return id; }
        public String getRussianName() { return russianName; }
        public String getTooltip() { return tooltip; }
    }

    public UpgradeItem(UpgradeType type, Properties properties) {
        super(properties.stacksTo(16));
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b" + type.getTooltip()));
    }
}
