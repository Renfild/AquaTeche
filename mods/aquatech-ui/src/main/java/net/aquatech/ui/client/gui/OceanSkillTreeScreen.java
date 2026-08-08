package net.aquatech.ui.client.gui;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.capability.SkillDefinitions;
import net.aquatech.ui.capability.SkillDefinitions.NodeType;
import net.aquatech.ui.capability.SkillDefinitions.SkillDef;
import net.aquatech.ui.client.render.MachineGuiFx;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.network.C2SOpenSkillTreePacket;
import net.aquatech.ui.network.C2SUnlockSkillPacket;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Ocean skill tree UI. Zoom scales the whole graph uniformly.
 * Detail panel is fixed on the right — never overlays node icons.
 */
public class OceanSkillTreeScreen extends Screen {

    private static final int ORIGIN_BG = 0xFF0A1628;
    private static final int FILL_UNLOCKED = 0xFF0F2A1E;
    private static final int FILL_AVAILABLE = 0xFF0A2230;
    private static final int FILL_LOCKED = 0xFF111827;
    private static final int BORDER_UNLOCKED = 0xFF22C55E;
    private static final int BORDER_AVAILABLE = 0xFF06B6D4;
    private static final int BORDER_LOCKED = 0xFF334155;
    private static final int BORDER_HOVER = 0xFFFBBF24;

    private static final int HALF_SMALL = 9;
    private static final int HALF_NOTABLE = 12;
    private static final int HALF_KEYSTONE = 16;
    private static final int S = 90;
    private static final int PANEL_W = 260;

    public static class SkillNode {
        public final String id, title, description;
        public final int x, y;
        public final NodeType type;
        public final int cost;
        public final String prerequisite;
        public final ItemStack displayItem;

        SkillNode(SkillDef def, int x, int y, ItemStack displayItem) {
            this.id = def.id();
            this.title = def.title();
            this.description = def.effectText();
            this.x = x;
            this.y = y;
            this.type = def.type();
            this.cost = def.cost();
            this.prerequisite = def.prereq();
            this.displayItem = displayItem;
        }
    }

    private final List<SkillNode> nodes = new ArrayList<>();
    private final Map<String, SkillNode> nodeIndex = new HashMap<>();
    private final Map<String, Integer> nodeListIdx = new HashMap<>();
    private final List<BubbleParticle> bubbles = new ArrayList<>();
    private final Random random = new Random();

    private int[] worldX, worldY, worldHalf;

    private double camX, camY;
    private double zoom = 0.55;
    private boolean isDragging;

    private Set<String> unlockedSnapshot = Set.of();
    private int skillPointsCache, aquaXpCache, levelCache, minXpCache, maxXpCache;
    private SkillNode hoveredNode;

    private static class BubbleParticle {
        float x, y, speed, radius;
        int alpha;
    }

    public OceanSkillTreeScreen() {
        super(Component.literal("Созвездия Океана"));
        buildTree();
    }

    // =========================================================================
    // Layout — coordinates only; titles/effects from SkillDefinitions
    // =========================================================================
    @SuppressWarnings("ConstantConditions")
    private void buildTree() {
        nodes.clear();
        nodeIndex.clear();
        nodeListIdx.clear();

        add("origin", 0, 0, Items.HEART_OF_THE_SEA);

        // Hub ring
        add("inner_angler", 0, -S, Items.FISHING_ROD);
        add("inner_tech", S, 0, Items.REDSTONE);
        add("inner_luck", (int) (S * 0.7), (int) (S * 0.7), Items.GOLD_NUGGET);
        add("inner_diving", -(int) (S * 0.7), (int) (S * 0.7), Items.PRISMARINE_CRYSTALS);
        add("inner_kelp", -S, 0, Items.KELP);

        // Sector 1 — Angler (North)
        add("rod_speed_1", 0, -2 * S, Items.STRING);
        add("bait_sense", 0, -3 * S, Items.WHEAT_SEEDS);
        add("double_catch", -S, -4 * S, Items.IRON_INGOT);
        add("rod_speed_2", S, -4 * S, Items.FEATHER);
        add("triple_hook", 0, -5 * S, Items.DIAMOND);
        add("flood_rhythm", -S, -6 * S, Items.TROPICAL_FISH);
        add("five_hook", S, -6 * S, Items.DIAMOND_SWORD);
        add("casting_mastery", 0, -7 * S, Items.ARROW);
        add("master_angler", 0, -8 * S, Items.FISHING_ROD);

        // Sector 2 — Tech (East)
        add("fe_collector", 2 * S, 0, Items.REDSTONE_BLOCK);
        add("efficiency_1", 3 * S, 0,
                new ItemStack(ModItems.UPGRADES.get(net.aquatech.ui.item.UpgradeItem.UpgradeType.EFFICIENCY).get()));
        add("speed_boost_1", 4 * S, -S, new ItemStack(ModItems.AUTO_FISHER_ITEM.get()));
        add("machine_cooling", 4 * S, S, Items.ICE);
        add("speed_boost_2", 5 * S, -S, new ItemStack(ModItems.OCEAN_FILTER_ITEM.get()));
        add("overclock", 5 * S, S, Items.REDSTONE_TORCH);
        add("zero_waste", 6 * S, 0, Items.SLIME_BALL);
        add("deep_regen", 7 * S, 0, Items.NETHER_STAR);
        add("overdrive_machine", 8 * S, 0, Items.BEACON);

        // Sector 3 — Luck (SE diagonal)
        add("luck_1", 2 * S, 2 * S, Items.GOLD_INGOT);
        add("lucky_cast", 3 * S, 2 * S, Items.GOLD_NUGGET);
        add("luck_2", 3 * S, 3 * S, Items.EMERALD);
        add("treasure_map", 4 * S, 3 * S, Items.MAP);
        add("chest_finder", 4 * S, 4 * S, Items.ENDER_CHEST);
        add("gem_miner", 5 * S, 4 * S, Items.DIAMOND_BLOCK);
        add("abyssal_loot", 5 * S, 5 * S, Items.ECHO_SHARD);
        add("sunken_relic", 6 * S, 5 * S, Items.TOTEM_OF_UNDYING);
        add("poseidon_blessing", 7 * S, 6 * S, Items.TRIDENT);

        // Sector 4 — Diving (SW diagonal)
        add("swim_speed", -2 * S, 2 * S, Items.TURTLE_HELMET);
        add("lung_expand", -3 * S, 2 * S, Items.PUFFERFISH);
        add("water_breathing", -3 * S, 3 * S, Items.HEART_OF_THE_SEA);
        add("current_rider", -4 * S, 3 * S, Items.SOUL_SAND);
        add("night_vision", -4 * S, 4 * S, new ItemStack(ModItems.SONAR_GOGGLES.get()));
        add("depth_armor", -5 * S, 4 * S, Items.NETHERITE_CHESTPLATE);
        add("pressure_resist", -5 * S, 5 * S, Items.SHIELD);
        add("tide_walker", -6 * S, 5 * S, Items.WATER_BUCKET);
        add("immortal_diver", -7 * S, 6 * S, Items.TOTEM_OF_UNDYING);

        // Sector 5 — Bio (West)
        add("kelp_harvest", -2 * S, 0, Items.DRIED_KELP);
        add("algae_study", -3 * S, -S, Items.OAK_LEAVES);
        add("bio_fuel", -3 * S, S, Items.DRIED_KELP_BLOCK);
        add("sea_grass_farm", -4 * S, 0, Items.SEAGRASS);
        add("sponge_grower", -5 * S, -S, Items.WET_SPONGE);
        add("living_kelp", -5 * S, S, Items.GRASS_BLOCK);
        add("sea_garden", -6 * S, 0, Items.COMPOSTER);
        add("mega_bloom", -7 * S, 0, Items.CHORUS_FLOWER);
        add("immortal_organism", -8 * S, 0, Items.GOLDEN_APPLE);

        // Cross-sector
        add("ocean_harmony", 2 * S, -5 * S, Items.PRISMARINE_CRYSTALS);
        add("tide_sync", 3 * S, -4 * S, Items.CLOCK);
        add("kelp_cast", -4 * S, -3 * S, Items.KELP);
        add("deep_resonance", -3 * S, -5 * S, Items.NETHER_STAR);

        for (int i = 0; i < nodes.size(); i++) {
            SkillNode sn = nodes.get(i);
            nodeIndex.put(sn.id, sn);
            nodeListIdx.put(sn.id, i);
        }
    }

    private void add(String id, int x, int y, Item item) {
        add(id, x, y, new ItemStack(item));
    }

    private void add(String id, int x, int y, ItemStack stack) {
        SkillDef def = SkillDefinitions.get(id);
        if (def == null) {
            throw new IllegalStateException("Missing SkillDefinitions entry: " + id);
        }
        nodes.add(new SkillNode(def, x, y, stack));
    }

    @Override
    protected void init() {
        super.init();
        NetworkHandler.CHANNEL.sendToServer(new C2SOpenSkillTreePacket());
        refreshData();
        bubbles.clear();
        for (int i = 0; i < 14; i++) {
            BubbleParticle b = new BubbleParticle();
            b.x = random.nextFloat() * this.width;
            b.y = random.nextFloat() * this.height;
            b.speed = 0.2F + random.nextFloat() * 0.4F;
            b.radius = 2.0F + random.nextFloat() * 3.5F;
            b.alpha = 20 + random.nextInt(50);
            bubbles.add(b);
        }
        rebuildLayout();
    }

    public void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(this::cacheFromCapability);
        }
    }

    private void cacheFromCapability(AquaSkillCapability cap) {
        unlockedSnapshot = new HashSet<>(cap.getUnlockedSkills());
        skillPointsCache = cap.getSkillPoints();
        aquaXpCache = cap.getAquaXp();
        levelCache = cap.getLevel();
        minXpCache = cap.getXpForCurrentLevel();
        maxXpCache = cap.getXpForNextLevel();
    }

    private void rebuildLayout() {
        int count = nodes.size();
        worldX = new int[count];
        worldY = new int[count];
        worldHalf = new int[count];
        for (int i = 0; i < count; i++) {
            SkillNode sn = nodes.get(i);
            worldX[i] = sn.x;
            worldY[i] = sn.y;
            worldHalf[i] = switch (sn.type) {
                case SMALL -> HALF_SMALL;
                case NOTABLE -> HALF_NOTABLE;
                case KEYSTONE -> HALF_KEYSTONE;
            };
        }
    }

    private double screenToWorldX(double sx) {
        return (sx - width / 2.0 - camX) / zoom;
    }

    private double screenToWorldY(double sy) {
        return (sy - height / 2.0 - camY) / zoom;
    }

    private boolean isUnlocked(String id) {
        return "origin".equals(id) || unlockedSnapshot.contains(id);
    }

    private boolean canAfford(SkillNode node) {
        return skillPointsCache >= node.cost;
    }

    private boolean canUnlock(SkillNode node) {
        if (isUnlocked(node.id) || node.cost <= 0) return false;
        if (!canAfford(node)) return false;
        return node.prerequisite == null || "origin".equals(node.prerequisite) || unlockedSnapshot.contains(node.prerequisite);
    }

    private SkillNode findNearestHover(double wmx, double wmy) {
        SkillNode best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            int half = worldHalf[i];
            double dx = wmx - worldX[i];
            double dy = wmy - worldY[i];
            double dist = dx * dx + dy * dy;
            if (dist <= (half + 2) * (half + 2.0) && dist < bestDist) {
                bestDist = dist;
                best = nodes.get(i);
            }
        }
        return best;
    }

    // =========================================================================
    // Render
    // =========================================================================
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        g.fill(0, 0, width, height, ORIGIN_BG);
        float t = MachineGuiFx.time(pt);
        MachineGuiFx.scanShimmer(g, 0, 0, width, t, false);

        for (BubbleParticle b : bubbles) {
            b.y -= b.speed;
            if (b.y < -10) {
                b.y = height + 10;
                b.x = random.nextFloat() * Math.max(1, width - PANEL_W - 20);
            }
            g.fill((int) b.x, (int) b.y, (int) (b.x + b.radius), (int) (b.y + b.radius), (b.alpha << 24) | 0x0284C7);
        }

        double pivotX = width / 2.0 + camX;
        double pivotY = height / 2.0 + camY;

        double wmx = screenToWorldX(mouseX);
        double wmy = screenToWorldY(mouseY);
        // Ignore hover when cursor is over the detail panel
        boolean overPanel = mouseX >= width - PANEL_W - 8;
        hoveredNode = overPanel ? null : findNearestHover(wmx, wmy);

        g.pose().pushPose();
        g.pose().translate(pivotX, pivotY, 0);
        g.pose().scale((float) zoom, (float) zoom, 1f);

        for (int i = 0; i < nodes.size(); i++) {
            SkillNode node = nodes.get(i);
            if (node.prerequisite == null) continue;
            Integer pi = nodeListIdx.get(node.prerequisite);
            if (pi == null) continue;
            boolean active = isUnlocked(node.id) && isUnlocked(node.prerequisite);
            UiDraw.drawSkillLink(g, worldX[pi], worldY[pi], worldX[i], worldY[i],
                    active ? 0xFF22C55E : 0x440284C7);
        }

        for (int i = 0; i < nodes.size(); i++) {
            SkillNode node = nodes.get(i);
            int nx = worldX[i], ny = worldY[i], half = worldHalf[i];
            boolean unlocked = isUnlocked(node.id);
            boolean available = canUnlock(node);
            boolean hover = hoveredNode == node;

            int border = unlocked ? BORDER_UNLOCKED : (available ? BORDER_AVAILABLE : BORDER_LOCKED);
            int fill = unlocked ? FILL_UNLOCKED : (available ? FILL_AVAILABLE : FILL_LOCKED);
            if (hover) border = BORDER_HOVER;

            UiDraw.drawSkillNode(g, nx, ny, half, border, fill);
            if (node.type == NodeType.KEYSTONE) {
                UiDraw.drawSkillNode(g, nx, ny, half + 3, (border & 0x55FFFFFF), 0);
            }
            if (hover) {
                UiDraw.drawSkillNode(g, nx, ny, half + 5, 0x44FBBF24, 0);
            }

            float iconScale = 0.85f;
            g.pose().pushPose();
            g.pose().translate(nx - 8 * iconScale, ny - 8 * iconScale, 0);
            g.pose().scale(iconScale, iconScale, 1f);
            g.renderItem(node.displayItem, 0, 0);
            g.pose().popPose();
        }

        g.pose().popPose();

        renderHud(g);
        renderDetailPanel(g);
        super.render(g, mouseX, mouseY, pt);
    }

    private void renderHud(GuiGraphics g) {
        int cx = width / 2;
        int maxRight = width - PANEL_W - 16;
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);

        // HUD panel background (taller to fit all elements)
        int barHalf = Math.min(210, maxRight / 2 - 20);
        g.fill(cx - barHalf, 7, Math.min(cx + barHalf, maxRight), 52, 0xFF07131E);
        g.fill(cx - barHalf, 7, Math.min(cx + barHalf, maxRight), 8, 0xFF0284C7);
        g.drawCenteredString(font, "≈ СОЗВЕЗДИЯ ОКЕАНА ≈", cx, 13, UiDraw.COLOR_PRIMARY);

        // Row 2: level label on left, points on right — Y=21 (ABOVE the bar)
        g.drawString(font, "Ур: " + levelCache, cx - barHalf + 6, 21, UiDraw.COLOR_TEXT);
        String pointsLabel = "Очки: §a" + skillPointsCache;
        g.drawString(font, pointsLabel, Math.min(cx + barHalf - font.width(pointsLabel) - 4, maxRight - font.width(pointsLabel) - 6), 21, UiDraw.COLOR_TEXT);

        // Row 3: XP bar — Y=30-37
        int xpW = Math.min(250, barHalf * 2 - 40);
        int filled = Math.max(0, Math.min(xpW,
                (aquaXpCache - minXpCache) * xpW / Math.max(1, maxXpCache - minXpCache)));
        int barX = cx - xpW / 2;
        g.fill(barX, 30, barX + xpW, 37, 0xFF1E293B);
        g.fill(barX, 30, barX + filled, 37, UiDraw.COLOR_ACCENT);
        // XP value centered in bar
        String xpText = (aquaXpCache - minXpCache) + "/" + (maxXpCache - minXpCache) + " xp";
        g.drawCenteredString(font, "§8" + xpText, cx, 31, 0xFF64748B);

        // Row 4: skill count — Y=41
        int total = nodes.size();
        int learnedShow = unlockedSnapshot.size() + (unlockedSnapshot.contains("origin") ? 0 : 1);
        learnedShow = Math.min(total, learnedShow);
        g.drawString(font, "§8" + learnedShow + "/" + total + " навыков", cx - 36, 41, 0xFF64748B);

        g.pose().popPose();
    }

    private void renderDetailPanel(GuiGraphics g) {
        int px = width - PANEL_W - 8;
        int py = 56;
        int ph = height - py - 28;

        g.pose().pushPose();
        g.pose().translate(0, 0, 450);

        g.fill(px, py, px + PANEL_W, py + ph, 0xFF0F172A);
        UiDraw.border(g, px, py, PANEL_W, ph, 0xFF38BDF8);
        g.fill(px + 1, py + 1, px + PANEL_W - 1, py + 2, 0xFF0284C7);

        int tx = px + 12;
        int ty = py + 12;

        if (hoveredNode == null) {
            g.drawString(font, "§bДетали навыка", tx, ty, UiDraw.COLOR_PRIMARY);
            g.drawString(font, "§8Наведите курсор на узел", tx, ty + 20, 0xFF94A3B8);
            g.pose().popPose();
            return;
        }

        SkillNode node = hoveredNode;
        boolean unlocked = isUnlocked(node.id);
        boolean available = canUnlock(node);

        g.renderItem(node.displayItem, tx, ty);
        g.drawString(font, node.title, tx + 22, ty + 2, UiDraw.COLOR_PRIMARY);

        SkillDef def = SkillDefinitions.get(node.id);
        String typeLabel = def != null ? def.typeLabel() : "Базовый";
        String costStr = node.cost <= 0 ? "бесплатно" : (node.cost + " очк.");
        g.drawString(font, "§8" + typeLabel + "  ·  §f" + costStr, tx + 22, ty + 14, 0xFF64748B);

        int y = ty + 36;
        g.drawString(font, "§7Эффект:", tx, y, 0xFF94A3B8);
        y += 14;
        List<FormattedCharSequence> lines = font.split(Component.literal(node.description), PANEL_W - 24);
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, tx, y, 0xFFE2E8F0);
            y += 12;
        }

        y += 8;
        if (node.prerequisite != null && !isUnlocked(node.prerequisite)) {
            SkillNode pre = nodeIndex.get(node.prerequisite);
            String pn = pre != null ? pre.title : node.prerequisite;
            List<FormattedCharSequence> req = font.split(Component.literal("§cТребует: " + pn), PANEL_W - 24);
            for (FormattedCharSequence line : req) {
                g.drawString(font, line, tx, y, 0xFFEF4444);
                y += 12;
            }
            y += 4;
        }

        String status;
        int sc;
        if (unlocked) {
            status = "✓ ИЗУЧЕНО";
            sc = 0xFF22C55E;
        } else if (available) {
            status = "[ЛКМ] Изучить за " + node.cost + " очк.";
            sc = 0xFF06B6D4;
        } else if (!canAfford(node)) {
            status = "Нужно " + node.cost + " очк. (есть " + skillPointsCache + ")";
            sc = 0xFFEF4444;
        } else {
            status = "Сначала изучите предыдущий узел";
            sc = 0xFFEF4444;
        }
        g.drawString(font, status, tx, Math.min(y + 4, py + ph - 20), sc);

        g.pose().popPose();
    }

    // =========================================================================
    // Input
    // =========================================================================
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            if (mx >= width - PANEL_W - 8) {
                return true; // absorb clicks on panel
            }
            isDragging = true;
            double wx = screenToWorldX(mx);
            double wy = screenToWorldY(my);
            SkillNode hit = findNearestHover(wx, wy);
            if (hit != null) {
                Minecraft.getInstance().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance
                                .forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
                NetworkHandler.CHANNEL.sendToServer(new C2SUnlockSkillPacket(hit.id));
                    return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn == 0) isDragging = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (isDragging) {
            camX += dx;
            camY += dy;
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        double oldZoom = zoom;
        if (delta > 0) zoom = Math.min(2.0, zoom + 0.1);
        else zoom = Math.max(0.15, zoom - 0.1);

        double zoomRatio = zoom / oldZoom;
        double pivotX = width / 2.0 + camX;
        double pivotY = height / 2.0 + camY;
        camX += (pivotX - mx) * (1 - zoomRatio);
        camY += (pivotY - my) * (1 - zoomRatio);
        return true;
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        rebuildLayout();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
