package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.aquatech.machines.inventory.BaseMachineMenu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

/**
 * Общий экран механизма в стиле «Латунь»: широкая панель с табличкой и подвесным инвентарём.
 * Фон со статикой лежит в текстуре (tools/build_machine_guis.py), здесь рисуется динамика: энергия, вторая шкала,
 * стрелка и то, что добавляет конкретная машина. Клик по стрелке (или по пруду у рыболовов) открывает рецепты JEI
 * или справку в чат.
 */
public abstract class AbstractMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {

    /** Ёмкость обычного мана-пула Botania: по ней рисуется шкала маны рядом с машиной. */
    protected static final int MANA_POOL_CAPACITY = 1_000_000;

    protected final ResourceLocation texture;
    protected final int clickX;
    protected final int clickY;
    protected final int clickW;
    protected final int clickH;

    protected AbstractMachineScreen(T menu, Inventory inv, Component title, ResourceLocation texture,
                                    int clickX, int clickY, int clickW, int clickH) {
        super(menu, inv, title);
        this.texture = texture;
        this.clickX = clickX;
        this.clickY = clickY;
        this.clickW = clickW;
        this.clickH = clickH;
        this.imageWidth = MachineLayout.IMAGE_W;
        this.imageHeight = MachineLayout.IMAGE_H;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, delta);
        renderTooltip(g, mouseX, mouseY);
    }

    protected float animationTime(float partialTick) {
        long gameTime = minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        return gameTime + partialTick;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        float time = animationTime(partialTick);
        g.pose().pushPose();
        g.pose().translate(leftPos, topPos, 0);
        renderMachine(g, time);
        MachineGuiFx.bar(g, MachineLayout.ENERGY_X, MachineLayout.ENERGY_Y, MachineLayout.ENERGY_W, MachineLayout.ENERGY_H,
                energyFraction(), MachineGuiFx.ENERGY_MAIN, MachineGuiFx.ENERGY_LIGHT, MachineGuiFx.ENERGY_DARK, time);
        renderSecondBar(g, time);
        g.pose().popPose();
    }

    /** Динамика конкретной машины в координатах GUI (начало в левом верхнем углу окна). */
    protected abstract void renderMachine(GuiGraphics g, float time);

    /** Вторая шкала: по умолчанию прогресс цикла. Мана-машины подменяют её шкалой маны. */
    protected void renderSecondBar(GuiGraphics g, float time) {
        MachineGuiFx.bar(g, MachineLayout.BAR2_X, MachineLayout.BAR2_Y, MachineLayout.BAR2_W, MachineLayout.BAR2_H,
                progressFraction(), MachineGuiFx.PROGRESS_MAIN, MachineGuiFx.PROGRESS_LIGHT, MachineGuiFx.PROGRESS_DARK, time);
    }

    protected float progressFraction() {
        int max = menu.getMaxProgress();
        return max <= 0 ? 0f : Math.min(1f, menu.getProgress() / (float) max);
    }

    protected float energyFraction() {
        int max = menu.getMaxEnergy();
        return max <= 0 ? 0f : Math.min(1f, menu.getEnergy() / (float) max);
    }

    protected boolean isWorking() {
        return menu.getProgress() > 0;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        int width = font.width(title);
        g.drawString(font, title, MachineLayout.PLAQUE_CENTER_X - width / 2, MachineLayout.PLAQUE_TEXT_Y, 0x3A2410, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inside(mouseX, mouseY, clickX, clickY, clickW, clickH)) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.6f, 1.0f);
            }
            if (!openJeiRecipes()) {
                onProgressBarClicked();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        double left = leftPos + x;
        double top = topPos + y;
        return mouseX >= left && mouseX < left + w && mouseY >= top && mouseY < top + h;
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        if (inside(mouseX, mouseY, MachineLayout.ENERGY_X - 1, MachineLayout.ENERGY_Y - 2, MachineLayout.ENERGY_W + 2, MachineLayout.ENERGY_H + 4)) {
            BaseMachineBlockEntity be = menu.getBlockEntity();
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§bЭнергия: §f" + menu.getEnergy() + " §7/ §f" + menu.getMaxEnergy() + " FE"),
                    Component.literal("§8Расход: §e" + be.energyPerTickNow() + " FE/t")
            ), mouseX, mouseY);
        }
        if (inside(mouseX, mouseY, MachineLayout.BAR2_X - 1, MachineLayout.BAR2_Y - 2, MachineLayout.BAR2_W + 2, MachineLayout.BAR2_H + 4)) {
            secondBarTooltip(g, mouseX, mouseY);
        }
        if (hasUpgradeWing()) {
            Slot hovered = getSlotUnderMouse();
            if (hovered instanceof SlotItemHandler && !hovered.hasItem()) {
                BaseMachineBlockEntity be = menu.getBlockEntity();
                int index = hovered.getSlotIndex();
                if (index == be.speedSlotIndex()) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§eГнездо: §fУлучшение скорости"),
                            Component.literal("§8Ускоряет рабочий цикл механизма"),
                            Component.literal("§7Подходит: §6Картридж Ускорения")
                    ), mouseX, mouseY);
                } else if (index == be.batterySlotIndex()) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§bГнездо: §fАккумулятор энергии"),
                            Component.literal("§8Заряжает внутренний буфер FE от батарей"),
                            Component.literal("§7Подходит: §bЭнергоячейки, батареи, редстоун")
                    ), mouseX, mouseY);
                } else if (index == be.effSlotIndex()) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§aГнездо: §fЭнергоэффективность"),
                            Component.literal("§8Снижает расход FE/t на 75%"),
                            Component.literal("§7Подходит: §aКартридж Энергоэффективности")
                    ), mouseX, mouseY);
                }
            }
        }
    }

    protected void secondBarTooltip(GuiGraphics g, int mouseX, int mouseY) {
        g.renderComponentTooltip(font, List.of(
                Component.literal("§6Цикл: §f" + Math.round(progressFraction() * 100) + "%")
        ), mouseX, mouseY);
    }

    /** Есть ли справа стойка апгрейдов (у MK-1 и генератора её нет). */
    protected boolean hasUpgradeWing() {
        return true;
    }

    protected boolean openJeiRecipes() {
        return false;
    }

    protected abstract void onProgressBarClicked();

    protected void printChat(String line) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal(line));
        }
    }
}
