package net.aquatech.machines.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeItem extends Item {

    public enum Type {
        SPEED_1(
                "Улучшение скорости (x1)",
                "§7Увеличивает скорость механизма: §a+100% §7(x2)",
                "§8Подходит для: Рыболов MK-2, Экстрактор, Экскаватор"
        ),
        SPEED_4(
                "Улучшение скорости (x4)",
                "§7Увеличивает скорость механизма: §a+300% §7(x4)",
                "§8Подходит для: Рыболов MK-2, Экстрактор, Экскаватор"
        ),
        ENERGY_EFFICIENCY(
                "Улучшение энергоэффективности",
                "§7Снижает потребление энергии: §a-75%",
                "§8Подходит для: Рыболов MK-2, Экстрактор, Экскаватор"
        ),
        FISHING_CORE(
                "Ядро Рыболова",
                "§7Переключает Авто-Рыболов в режим §bловли рыбы",
                "§8Добывает редкую рыбу по ростеру тира установленной удочки"
        );

        private final String title;
        private final String line1;
        private final String line2;

        Type(String title, String line1, String line2) {
            this.title = title;
            this.line1 = line1;
            this.line2 = line2;
        }
    }

    private final Type type;

    public UpgradeItem(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(type.line1));
        tooltip.add(Component.literal(type.line2));
    }
}
