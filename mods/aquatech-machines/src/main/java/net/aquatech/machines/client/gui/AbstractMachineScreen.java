package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.BaseMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Общий экран механизма LuminousUI:
 * - Рендерит индикатор энергии (12x50) и шкалу прогресса (24x17);
 * - Показывает информативные тултипы при наведении;
 * - По клику на стрелку прогресса выводит подробный каталог ресурсов в игровой чат.
 */
public abstract class AbstractMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {

    protected final ResourceLocation texture;
    protected final int progressX;
    protected final int progressY;
    protected final int energyX;
    protected final int energyY;

    protected AbstractMachineScreen(T menu, Inventory inv, Component title, ResourceLocation texture,
                                    int progressX, int progressY, int energyX, int energyY) {
        super(menu, inv, title);
        this.texture = texture;
        this.progressX = progressX;
        this.progressY = progressY;
        this.energyX = energyX;
        this.energyY = energyY;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, delta);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.blit(texture, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // При наличии выносного крыла апгрейдов - рендерим его из UV (0, 166)
        if (hasUpgradeWing()) {
            g.blit(texture, x + 175, y + 12, 0, 166, 34, 73, 256, 256);
        }

        // Прогресс: лево-право по strip (176,52)
        int progress = menu.getScaledProgress(24);
        if (progress > 0) {
            g.blit(texture, x + progressX, y + progressY, 176, 52, progress, 17, 256, 256);
        }

        // Энергия: низ-верх по strip (176,0) 12x50
        int max = menu.getMaxEnergy();
        int energyVal = menu.getEnergy();
        int energyHeight = max <= 0 ? 0 : (int) Math.min(50, ((long) energyVal * 50) / max);
        if (energyHeight > 0) {
            g.blit(texture, x + energyX, y + energyY + (50 - energyHeight), 176, 50 - energyHeight, 12, energyHeight, 256, 256);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
        if (hasUpgradeWing()) {
            if (mouseX >= guiLeft + 175 && mouseX <= guiLeft + 209 && mouseY >= guiTop + 12 && mouseY <= guiTop + 85) {
                return false;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xDEE3EA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            int px = leftPos + progressX;
            int py = topPos + progressY;
            if (mouseX >= px && mouseX <= px + 24 && mouseY >= py && mouseY <= py + 17) {
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.6f, 1.0f);
                }
                boolean opened = openJeiRecipes();
                if (!opened) {
                    onProgressBarClicked();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        int ex = leftPos + energyX;
        int ey = topPos + energyY;
        if (mouseX >= ex && mouseX <= ex + 12 && mouseY >= ey && mouseY <= ey + 50) {
            List<Component> tip = List.of(
                    Component.literal("§bЭнергия: §f" + menu.getEnergy() + " §7/ §f" + menu.getMaxEnergy() + " FE"),
                    Component.literal("§8Расход: §e60 FE/t §8(со скидкой: §a15 FE/t§8)")
            );
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }

        // Подсказки для пустых сокетов выносного крыла апгрейдов
        if (hasUpgradeWing()) {
            net.minecraft.world.inventory.Slot hovered = this.getSlotUnderMouse();
            if (hovered != null && !hovered.hasItem()) {
                if (hovered.x == 185 && hovered.y == 19) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§eГнездо: §fУлучшение скорости"),
                            Component.literal("§8Ускоряет рабочий цикл механизма на 25%"),
                            Component.literal("§7Подходит: §6Картридж Ускорения")
                    ), mouseX, mouseY);
                } else if (hovered.x == 185 && hovered.y == 41) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§bГнездо: §fАккумулятор энергии"),
                            Component.literal("§8Заряжает внутренний буфер FE от батарей"),
                            Component.literal("§7Подходит: §bЭнергоячейки, батареи, редстоун")
                    ), mouseX, mouseY);
                } else if (hovered.x == 185 && hovered.y == 63) {
                    g.renderComponentTooltip(font, List.of(
                            Component.literal("§aГнездо: §fЭнергоэффективность"),
                            Component.literal("§8Снижает расход FE/t на 25% (до 75%)"),
                            Component.literal("§7Подходит: §aКартридж Энергоэффективности")
                    ), mouseX, mouseY);
                }
            }
        }
    }

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
