package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.SynthesizerBlockEntity;
import net.aquatech.machines.inventory.SynthesizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SynthesizerScreen extends AbstractMachineScreen<SynthesizerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/synthesizer.png");

    public SynthesizerScreen(SynthesizerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 92, 37, 8, 20);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        super.renderBg(g, partialTick, mouseX, mouseY);
        int x = leftPos, y = topPos;

        // 1. Рендеринг лавы в баке (25, 22) 12x50 пикселей
        int lava = menu.getLavaAmount();
        int maxLava = SynthesizerBlockEntity.TANK_CAPACITY;
        int lavaHeight = (int) Math.min(50, ((long) lava * 50) / maxLava);
        if (lavaHeight > 0) {
            // Магматический срез лавы
            g.fill(x + 25, y + 22 + (50 - lavaHeight), x + 37, y + 72, 0xFFFF5722);
            // Левый зеркальный блик стекла
            g.fill(x + 26, y + 22 + (50 - lavaHeight), x + 28, y + 72, 0xFFFFCC80);
            // Правая тень глубины резервуара
            g.fill(x + 34, y + 22 + (50 - lavaHeight), x + 36, y + 72, 0xFFD84315);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        int x = leftPos, y = topPos;

        // Тултип лавового резервуара (24..37, 21..72)
        if (mouseX >= x + 24 && mouseX <= x + 37 && mouseY >= y + 21 && mouseY <= y + 72) {
            List<Component> tip = List.of(
                    Component.literal("§6Резервуар лавы: §f" + menu.getLavaAmount() + " §7/ §f" + SynthesizerBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Расход: §e1,000 mB §8на один цикл синтеза"),
                    Component.literal("§7Поддерживает подачу ведрами и по трубам")
            );
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }

        // Тултип для бонусного выхода (148..168, 35..56)
        if (mouseX >= x + 148 && mouseX <= x + 168 && mouseY >= y + 35 && mouseY <= y + 56) {
            net.minecraft.world.inventory.Slot s = this.getSlotUnderMouse();
            if (s != null && !s.hasItem()) {
                g.renderComponentTooltip(font, List.of(
                        Component.literal("§aГнездо: §fКритический бонус-выход"),
                        Component.literal("§7Дополнительный редкий дроп кристаллизации"),
                        Component.literal("§8Вулканические кристаллы, микрочипы, тир-2 сплавы")
                ), mouseX, mouseY);
            }
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
