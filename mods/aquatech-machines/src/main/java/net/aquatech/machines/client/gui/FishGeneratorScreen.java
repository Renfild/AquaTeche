package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.FishGeneratorBlockEntity;
import net.aquatech.machines.inventory.FishGeneratorMenu;
import net.aquatech.machines.util.FishGeneratorLogic;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Экран Рыбного генератора в том же стиле, что у остальных машин: топка с пламенем, шкала энергии и шкала горения.
 * Слева выведены настоящие числа: выработка топлива в огне и сколько энергии реально ушло авторыболовам.
 * Это не наследник AbstractMachineScreen: у генератора нет прогресса, апгрейдов и ClickArea для JEI.
 */
@Mod.EventBusSubscriber(modid = "aquatech_machines", value = Dist.CLIENT)
public class FishGeneratorScreen extends AbstractContainerScreen<FishGeneratorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/fish_generator.png");
    private static final int BURN_MAIN = 0xFFFF6A1A;
    private static final int BURN_LIGHT = 0xFFFFD24A;
    private static final int BURN_DARK = 0xFFB83A08;
    private static final int TEXT_COLOR = 0x3A2410;

    public FishGeneratorScreen(FishGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = MachineLayout.IMAGE_W;
        this.imageHeight = MachineLayout.IMAGE_H;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, delta);
        renderTooltip(g, mouseX, mouseY);
    }

    private float animationTime(float partialTick) {
        long gameTime = minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        return gameTime + partialTick;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        float time = animationTime(partialTick);
        g.pose().pushPose();
        g.pose().translate(leftPos, topPos, 0);
        boolean burning = menu.getBurnTime() > 0;
        MachineGuiFx.flames(g, TEXTURE, MachineLayout.FISH_GENERATOR_FLAMES_X, MachineLayout.FISH_GENERATOR_FLAMES_Y,
                MachineLayout.FISH_GENERATOR_FLAMES_W, MachineLayout.FISH_GENERATOR_FLAMES_H, burning, time);
        int maxEnergy = menu.getMaxEnergy();
        float energy = maxEnergy <= 0 ? 0f : Math.min(1f, menu.getEnergy() / (float) maxEnergy);
        MachineGuiFx.bar(g, MachineLayout.ENERGY_X, MachineLayout.ENERGY_Y, MachineLayout.ENERGY_W, MachineLayout.ENERGY_H,
                energy, MachineGuiFx.ENERGY_MAIN, MachineGuiFx.ENERGY_LIGHT, MachineGuiFx.ENERGY_DARK, time);
        int total = menu.getBurnTotal();
        float burn = total <= 0 ? 0f : Math.min(1f, menu.getBurnTime() / (float) total);
        MachineGuiFx.bar(g, MachineLayout.BAR2_X, MachineLayout.BAR2_Y, MachineLayout.BAR2_W, MachineLayout.BAR2_H,
                burn, BURN_MAIN, BURN_LIGHT, BURN_DARK, time);
        renderReadout(g);
        g.pose().popPose();
    }

    /** Настоящие числа слева под слотом топлива: выработка и фактическая отдача. */
    private void renderReadout(GuiGraphics g) {
        g.pose().pushPose();
        g.pose().translate(26, 72, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(font, "Даёт: " + menu.getCurrentRate() + " FE/t", 0, 0, TEXT_COLOR, false);
        g.drawString(font, "Отдача: " + menu.getLastTransferred() + " FE/t", 0, 12, 0x7A3F12, false);
        g.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int width = font.width(title);
        g.drawString(font, title, MachineLayout.PLAQUE_CENTER_X - width / 2, MachineLayout.PLAQUE_TEXT_Y, TEXT_COLOR, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        if (inside(mouseX, mouseY, MachineLayout.ENERGY_X - 1, MachineLayout.ENERGY_Y - 2, MachineLayout.ENERGY_W + 2, MachineLayout.ENERGY_H + 4)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§bЭнергия: §f" + menu.getEnergy() + " §7/ §f" + menu.getMaxEnergy() + " FE"),
                    Component.literal("§8Отдано за последний тик: §e" + menu.getLastTransferred() + " FE"),
                    Component.literal("§7Энергию берут только авторыболовы вплотную")
            ), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, MachineLayout.BAR2_X - 1, MachineLayout.BAR2_Y - 2, MachineLayout.BAR2_W + 2, MachineLayout.BAR2_H + 4)
                || inside(mouseX, mouseY, MachineLayout.FISH_GENERATOR_FLAMES_X, MachineLayout.FISH_GENERATOR_FLAMES_Y,
                MachineLayout.FISH_GENERATOR_FLAMES_W, MachineLayout.FISH_GENERATOR_FLAMES_H)) {
            if (menu.getBurnTime() > 0) {
                int seconds = (menu.getBurnTime() + 19) / 20;
                g.renderComponentTooltip(font, List.of(
                        Component.literal("§6Горит ещё §f" + seconds + " с"),
                        Component.literal("§8Вырабатывает: §e" + menu.getCurrentRate() + " FE/t"),
                        Component.literal("§8Осталось из этой единицы: §e" + (long) menu.getBurnTime() * menu.getCurrentRate() + " FE")
                ), mouseX, mouseY);
            } else {
                g.renderComponentTooltip(font, List.of(
                        Component.literal("§7Нет топлива в огне"),
                        Component.literal("§8Дерево 20, уголь 40, блейз 60, блок угля и лава 80 FE/t")
                ), mouseX, mouseY);
            }
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        int left = leftPos + x;
        int top = topPos + y;
        return mouseX >= left && mouseX < left + w && mouseY >= top && mouseY < top + h;
    }

    /** Пока открыт генератор, у топлива в подсказке предмета видно, сколько оно даёт. */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getInstance().screen instanceof FishGeneratorScreen)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        int ticks = FishGeneratorBlockEntity.burnTicksOf(stack);
        if (ticks <= 0) {
            return;
        }
        int rate = FishGeneratorBlockEntity.rateOf(stack);
        event.getToolTip().add(Component.literal("§6Рыбный генератор: §f" + rate + " FE/t, §f"
                + FishGeneratorLogic.totalEnergy(ticks, rate) + " FE §7(" + ticks / 20 + " с)"));
    }
}
