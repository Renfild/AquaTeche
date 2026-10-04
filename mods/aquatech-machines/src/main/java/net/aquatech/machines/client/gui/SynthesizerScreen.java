package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.SynthesizerBlockEntity;
import net.aquatech.machines.inventory.SynthesizerMenu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SynthesizerScreen extends AbstractMachineScreen<SynthesizerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/synthesizer.png");

    public SynthesizerScreen(SynthesizerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, MachineLayout.SYNTHESIZER_ARROW_X, MachineLayout.SYNTHESIZER_ARROW_Y, 24, 17);
    }

    @Override
    protected void renderMachine(GuiGraphics g, float time) {
        MachineGuiFx.arrow(g, texture, MachineLayout.SYNTHESIZER_ARROW_X, MachineLayout.SYNTHESIZER_ARROW_Y, progressFraction(), time);
        float lava = menu.getLavaAmount() / (float) SynthesizerBlockEntity.TANK_CAPACITY;
        MachineGuiFx.tank(g, MachineLayout.SYNTHESIZER_TANK_LAVA_X, MachineLayout.SYNTHESIZER_TANK_LAVA_Y,
                MachineLayout.SYNTHESIZER_TANK_LAVA_W, MachineLayout.SYNTHESIZER_TANK_LAVA_H, lava, 0xFFFF5722, 0xFFFFCC80, 0xFFD84315, time);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        if (inside(mouseX, mouseY, MachineLayout.SYNTHESIZER_TANK_LAVA_X - 2, MachineLayout.SYNTHESIZER_TANK_LAVA_Y - 2,
                MachineLayout.SYNTHESIZER_TANK_LAVA_W + 4, MachineLayout.SYNTHESIZER_TANK_LAVA_H + 4)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§6Резервуар лавы: §f" + menu.getLavaAmount() + " §7/ §f" + SynthesizerBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Расход: §e1,000 mB §8на один цикл синтеза"),
                    Component.literal("§7Поддерживает подачу ведрами и по трубам")
            ), mouseX, mouseY);
        }
        net.minecraft.world.inventory.Slot slot = this.getSlotUnderMouse();
        if (slot != null && !slot.hasItem() && inside(mouseX, mouseY, MachineLayout.SYNTHESIZER_OUTPUT3_X - 3, MachineLayout.SYNTHESIZER_OUTPUT3_Y - 3, 22, 22)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§aГнездо: §fКритический бонус-выход"),
                    Component.literal("§7Дополнительный редкий дроп кристаллизации"),
                    Component.literal("§8Вулканические кристаллы, микрочипы, тир-2 сплавы")
            ), mouseX, mouseY);
        }
    }

    @Override
    protected boolean openJeiRecipes() {
        return net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.showRecipes(
                net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.SYNTHESIZER_TYPE);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §c=== Рецепты Гидротермального Синтезатора ===");
        for (SynthesizerBlockEntity.SynthRecipe r : SynthesizerBlockEntity.RECIPES) {
            printChat(" §7• §f" + r.label());
        }
        printChat("§8[§aИспользуйте для глубинного синтеза вулканических кристаллов и сплавов!§8]");
    }
}
