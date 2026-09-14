package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.FisherBlockEntity;
import net.aquatech.machines.inventory.FisherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FisherScreen extends AbstractMachineScreen<FisherMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/fisher.png");

    public FisherScreen(FisherMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 74, 36, 8, 20);
    }

    @Override
    protected boolean openJeiRecipes() {
        return net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.showRecipes(
                net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.FISHER_TYPE);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §6=== Добыча Авто-Рыболова MK-2 ===");
        if (!(menu.getBlockEntity() instanceof FisherBlockEntity fisher)) {
            return;
        }

        ItemStack rod = fisher.getItems().getStackInSlot(FisherBlockEntity.SLOT_ROD);
        if (rod.isEmpty()) {
            printChat(" §c• Удочка не установлена! Вставьте удочку StarCatcher в левый верхний слот.");
            printChat(" §7(Тир удочки определяет доступный пул ресурсов и рыбы)");
            return;
        }

        int tier = fisher.rodTier();
        int rate = fisher.activeRate();
        boolean hasCore = fisher.hasFishCore();

        printChat(" §eУдочка: §b" + rod.getHoverName().getString() + " §7(Тир: §a" + tier + "§7, Множитель улова: §a×" + rate + "§7)");

        if (hasCore) {
            printChat(" §dРежим: §bЛовля Рыбы §7(активно Ядро Рыболова)");
            printChat(" §7Ловит виды рыб из каталога вплоть до §aТира " + tier + "§7.");
            printChat(" §7(Редкости: Обычный, Редкий [Т3+], Эпический [Т6+], Легендарный [Т8+])");
        } else {
            printChat(" §eРежим: §6Добыча Ресурсов §7(установите Ядро Рыболова для ловли рыбы)");
            printChat(" §aДоступный пул руды и материалов для Тира " + tier + ":");

            List<String> ores = new ArrayList<>(List.of("Железо", "Медь", "Уголь", "Олово (IU)"));
            if (tier >= 3) ores.addAll(List.of("Редстоун", "Лазурит", "Шпинель (IU)", "Стронций (IU)", "Барий (IU)"));
            if (tier >= 5) ores.addAll(List.of("Серебро (IU)", "Никель (IU)", "Алюминий (IU)", "Обсидиан"));
            if (tier >= 7) ores.addAll(List.of("Вольфрам (IU)", "Хром (IU)", "Сапфир (IU)", "Топаз (IU)"));
            if (tier >= 9) ores.addAll(List.of("Титан (IU)", "Кобальт (IU)", "Алмазная руда", "Нержавеющая сталь", "Рубин (IU)"));
            if (tier >= 11) ores.addAll(List.of("Платина (IU)", "Инконель (IU)", "Древние обломки"));
            if (tier >= 13) ores.addAll(List.of("Иридий (IU)", "Осмиридий (IU)", "Звезда Незера"));

            printChat(" §f" + String.join("§7, §f", ores));
        }

        printChat("§8[§7Множитель улова удочки ×" + rate + " умножает каждую единицу улова!§8]");
    }
}
