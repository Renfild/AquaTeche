package net.aquatech.ui.client.gui;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.capability.SkillDefinitions;
import net.aquatech.ui.capability.SkillDefinitions.NodeType;
import net.aquatech.ui.capability.SkillDefinitions.SkillDef;
import net.aquatech.ui.client.gui.widget.AquaBadge;
import net.aquatech.ui.client.gui.widget.AquaCaseSlot;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.network.C2SOpenSkillTreePacket;
import net.aquatech.ui.network.C2SUnlockSkillPacket;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
 * Ocean Skill Tree Screen (K Menu).
 * Glassmorphic dark oceanic design with glowing constellation nodes,
 * bioluminescent progress bars, and right-hand detail sidebar.
 */
public class OceanSkillTreeScreen extends AquaBlurredScreen {

    private static final int BG_DARK = 0xF5060C17;
    private static final int PANEL_FILL = 0xDD0B1829;
    private static final int PANEL_BORDER = 0xFF00E5FF;
    private static final int BORDER_UNLOCKED = 0xFF00E5FF;
    private static final int BORDER_AVAILABLE = 0xFF10B981;
    private static final int BORDER_LOCKED = 0xFF1E293B;
    private static final int BORDER_HOVER = 0xFFF59E0B;

    private static final int HALF_SMALL = 11;
    private static final int HALF_NOTABLE = 14;
    private static final int HALF_KEYSTONE = 18;
    /** World units between nodes. Positions in getPositionForId are × this. */
    private static final int TREE_SCALE = 2;
    private static final int PANEL_W = 240;

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
    private final List<StarParticle> stars = new ArrayList<>();
    private final Random random = new Random();

    private int[] worldX, worldY, worldHalf;

    private double camX, camY;
    private double zoom = 0.72;
    private boolean isDragging;

    private Set<String> unlockedSnapshot = Set.of();
    private int skillPointsCache, aquaXpCache, levelCache, minXpCache, maxXpCache;
    private SkillNode hoveredNode;
    private int unlockBtnX, unlockBtnY, unlockBtnW, unlockBtnH;
    private boolean unlockBtnHit;

    private static class StarParticle {
        float x, y, speed, size;
        int alpha;
    }

    public OceanSkillTreeScreen() {
        super(Component.literal("Созвездия Океана"));
        buildTree();
    }

    private void buildTree() {
        nodes.clear();
        nodeIndex.clear();
        nodeListIdx.clear();

        for (SkillDef def : SkillDefinitions.all()) {
            ItemStack icon = resolveIcon(def.id(), def.type());
            int[] pos = getPositionForId(def.id());
            SkillNode node = new SkillNode(def, pos[0] * TREE_SCALE, pos[1] * TREE_SCALE, icon);
            nodeIndex.put(def.id(), node);
            nodeListIdx.put(def.id(), nodes.size());
            nodes.add(node);
        }

        worldX = new int[nodes.size()];
        worldY = new int[nodes.size()];
        worldHalf = new int[nodes.size()];
    }

    public void refreshData() {
        buildTree();
        updateCapabilitySnapshot();
    }

    private ItemStack resolveIcon(String id, NodeType type) {
        if ("origin".equals(id)) return new ItemStack(ModItems.ABYSSAL_MAGNET.get());
        if (id.contains("speed")) return new ItemStack(Items.FISHING_ROD);
        if (id.contains("luck") || id.contains("rare")) return new ItemStack(Items.PRISMARINE_SHARD);
        if (id.contains("xp") || id.contains("double")) return new ItemStack(Items.EXPERIENCE_BOTTLE);
        if (id.contains("chest") || id.contains("treasure")) return new ItemStack(Items.CHEST);
        if (id.contains("magnet")) return new ItemStack(ModItems.ABYSSAL_MAGNET.get());
        if (id.contains("sonar")) return new ItemStack(ModItems.SONAR_GOGGLES.get());
        return type == NodeType.KEYSTONE ? new ItemStack(Items.NETHER_STAR) :
               type == NodeType.NOTABLE ? new ItemStack(Items.HEART_OF_THE_SEA) :
               new ItemStack(Items.NAUTILUS_SHELL);
    }

    private int[] getPositionForId(String id) {
        // Pentagon radial layout — 5 sectors at 72° intervals, R=80 per step.
        // Sector unit vectors (screen: Y down):
        //   Angler  (90°):  dx=0,     dy=-R        (North)
        //   Tech    (18°):  dx=+76,   dy=-25        (NE)
        //   Luck   (-54°):  dx=+47,   dy=+65        (SE)
        //   Diving(-126°):  dx=-47,   dy=+65        (SW)
        //   Bio    (162°):  dx=-76,   dy=-25        (NW)
        return switch (id) {
            // ── HUB ──────────────────────────────────────────────────────────
            case "origin"            -> new int[]{0, 0};

            // ── SECTOR 1 — ANGLER (North) ─────────────────────────────────
            case "inner_angler"      -> new int[]{0,    -80};
            case "rod_speed_1"       -> new int[]{0,   -160};
            case "bait_sense"        -> new int[]{0,   -240};
            case "double_catch"      -> new int[]{0,   -320};
            case "rod_speed_2"       -> new int[]{0,   -400};
            case "triple_hook"       -> new int[]{0,   -480};
            case "flood_rhythm"      -> new int[]{0,   -560};
            case "five_hook"         -> new int[]{0,   -640};
            case "casting_mastery"   -> new int[]{0,   -720};
            case "master_angler"     -> new int[]{0,   -800};

            // ── SECTOR 2 — TECH (NE) ──────────────────────────────────────
            case "inner_tech"        -> new int[]{ 76,  -25};
            case "fe_collector"      -> new int[]{152,  -50};
            case "efficiency_1"      -> new int[]{228,  -75};
            case "speed_boost_1"     -> new int[]{304, -100};
            case "machine_cooling"   -> new int[]{380, -125};
            case "speed_boost_2"     -> new int[]{456, -150};
            case "overclock"         -> new int[]{532, -175};
            case "zero_waste"        -> new int[]{608, -200};
            case "deep_regen"        -> new int[]{684, -225};
            case "overdrive_machine" -> new int[]{760, -250};

            // ── SECTOR 3 — LUCK (SE) ──────────────────────────────────────
            case "inner_luck"        -> new int[]{ 47,   65};
            case "luck_1"            -> new int[]{ 94,  130};
            case "lucky_cast"        -> new int[]{141,  195};
            case "luck_2"            -> new int[]{188,  260};
            case "treasure_map"      -> new int[]{235,  325};
            case "chest_finder"      -> new int[]{282,  390};
            case "gem_miner"         -> new int[]{329,  455};
            case "abyssal_loot"      -> new int[]{376,  520};
            case "sunken_relic"      -> new int[]{423,  585};
            case "poseidon_blessing" -> new int[]{470,  650};

            // ── SECTOR 4 — DIVING (SW) ────────────────────────────────────
            case "inner_diving"      -> new int[]{ -47,   65};
            case "swim_speed"        -> new int[]{ -94,  130};
            case "lung_expand"       -> new int[]{-141,  195};
            case "water_breathing"   -> new int[]{-188,  260};
            case "current_rider"     -> new int[]{-235,  325};
            case "night_vision"      -> new int[]{-282,  390};
            case "depth_armor"       -> new int[]{-329,  455};
            case "pressure_resist"   -> new int[]{-376,  520};
            case "tide_walker"       -> new int[]{-423,  585};
            case "immortal_diver"    -> new int[]{-470,  650};

            // ── SECTOR 5 — BIO (NW) ───────────────────────────────────────
            case "inner_kelp"        -> new int[]{ -76,  -25};
            case "kelp_harvest"      -> new int[]{-152,  -50};
            case "algae_study"       -> new int[]{-228,  -75};
            case "bio_fuel"          -> new int[]{-304, -100};
            case "sea_grass_farm"    -> new int[]{-380, -125};
            case "sponge_grower"     -> new int[]{-456, -150};
            case "living_kelp"       -> new int[]{-532, -175};
            case "sea_garden"        -> new int[]{-608, -200};
            case "mega_bloom"        -> new int[]{-684, -225};
            case "immortal_organism" -> new int[]{-760, -250};

            // ── CROSS-SECTOR ──────────────────────────────────────────────
            // ocean_harmony: stems from master_angler (0,-800), offset right
            case "ocean_harmony"     -> new int[]{ 90,  -800};
            case "tide_sync"         -> new int[]{170,  -800};
            // kelp_cast: stems from immortal_organism (-760,-250), extends further NW
            case "kelp_cast"         -> new int[]{-760, -330};
            case "deep_resonance"    -> new int[]{-760, -410};

            default -> new int[]{0, 0};
        };
    }


    @Override
    protected void init() {
        super.init();
        NetworkHandler.CHANNEL.sendToServer(new C2SOpenSkillTreePacket());

        stars.clear();
        for (int i = 0; i < 40; i++) {
            StarParticle p = new StarParticle();
            p.x = random.nextFloat() * width;
            p.y = random.nextFloat() * height;
            p.speed = 0.15f + random.nextFloat() * 0.35f;
            p.size = 1.0f + random.nextFloat() * 2.0f;
            p.alpha = 40 + random.nextInt(180);
            stars.add(p);
        }
    }

    private void updateCapabilitySnapshot() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            unlockedSnapshot = new HashSet<>(cap.getUnlockedSkills());
            unlockedSnapshot.add("origin");
            skillPointsCache = cap.getSkillPoints();
            aquaXpCache = cap.getAquaXp();
            levelCache = cap.getLevel();
            minXpCache = cap.getXpForCurrentLevel();
            maxXpCache = cap.getXpForNextLevel();
        });
    }

    @Override
    public void tick() {
        super.tick();
        updateCapabilitySnapshot();

        for (StarParticle p : stars) {
            p.y -= p.speed;
            if (p.y < 0) {
                p.y = height;
                p.x = random.nextFloat() * width;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float pt) {

        // Render floating stars
        for (StarParticle p : stars) {
            int color = (p.alpha << 24) | 0x00E5FF;
            g.fill((int) p.x, (int) p.y, (int) (p.x + p.size), (int) (p.y + p.size), color);
        }

        int mainWidth = width - PANEL_W;
        int centerScreenX = mainWidth / 2;
        int centerScreenY = height / 2;

        if (mouseX < mainWidth) {
            hoveredNode = null;
        }
        double z = this.zoom;

        // Compute world coordinates
        for (int i = 0; i < nodes.size(); i++) {
            SkillNode node = nodes.get(i);
            int half = (node.type == NodeType.KEYSTONE ? HALF_KEYSTONE :
                        node.type == NodeType.NOTABLE ? HALF_NOTABLE : HALF_SMALL);

            worldX[i] = (int) Math.round(centerScreenX + (node.x - camX) * z);
            worldY[i] = (int) Math.round(centerScreenY + (node.y - camY) * z);
            worldHalf[i] = (int) Math.round(half * z);
        }

        g.pose().pushPose();

        // Connection lines — only unlocked path + next available. Locked-locked web is what "слипалось".
        for (SkillNode node : nodes) {
            if (node.prerequisite == null || !nodeIndex.containsKey(node.prerequisite)) continue;
            boolean fromUnlocked = unlockedSnapshot.contains(node.prerequisite);
            boolean toUnlocked = unlockedSnapshot.contains(node.id);
            if (!fromUnlocked && !toUnlocked) continue;

            int idxFrom = nodeListIdx.get(node.prerequisite);
            int idxTo = nodeListIdx.get(node.id);
            int lineColor = (fromUnlocked && toUnlocked) ? 0xAA00E5FF : 0x8810B981;
            UiDraw.drawLine(g, worldX[idxFrom], worldY[idxFrom], worldX[idxTo], worldY[idxTo], lineColor);
        }

        // Render nodes
        for (int i = 0; i < nodes.size(); i++) {
            SkillNode node = nodes.get(i);
            int nx = worldX[i];
            int ny = worldY[i];
            int half = worldHalf[i];

            boolean isUnlocked = unlockedSnapshot.contains(node.id);
            boolean isPrereqUnlocked = node.prerequisite == null || unlockedSnapshot.contains(node.prerequisite);
            boolean isAvailable = !isUnlocked && isPrereqUnlocked;

            boolean hover = (mouseX >= nx - half && mouseX <= nx + half &&
                             mouseY >= ny - half && mouseY <= ny + half &&
                             mouseX < mainWidth);

            if (hover) {
                hoveredNode = node;
            }

            int border = isUnlocked ? BORDER_UNLOCKED :
                         isAvailable ? BORDER_AVAILABLE : BORDER_LOCKED;
            if (hover) border = BORDER_HOVER;

            int fill = isUnlocked ? 0xCC083044 :
                       isAvailable ? 0xCC064E3B : 0x990F172A;

            UiDraw.drawGlowCircle(g, nx, ny, half + 1, (border & 0x33FFFFFF) | (border & 0x00FFFFFF));
            UiDraw.drawGlowCircle(g, nx, ny, half, fill);
            UiDraw.drawGlowCircle(g, nx, ny, Math.max(2, half - 3), (fill & 0x00FFFFFF) | 0x22000000);

            // Item icon
            float iconScale = (float) (0.75 * z);
            if (iconScale > 0.3f) {
                g.pose().pushPose();
                g.pose().translate(nx - 8 * iconScale, ny - 8 * iconScale, 0);
                g.pose().scale(iconScale, iconScale, 1f);
                g.renderItem(node.displayItem, 0, 0);
                g.pose().popPose();
            }
        }

        g.pose().popPose();

        renderHudHeader(g, mainWidth);
        renderDetailSidebar(g);
    }

    private void renderHudHeader(GuiGraphics g, int mainWidth) {
        int w = Math.min(520, mainWidth - 40);
        int h = 48;
        int x = (mainWidth - w) / 2;
        int y = 10;

        AquaGlassPanel.draw(g, x, y, w, h, AquaGlassPanel.FILL, AquaGlassPanel.BORDER, 6, false);
        AquaFontRenderer.drawCenteredHeader(g, font, "Созвездия Океана", x + w / 2, y + 6, 0xFF00E5FF);

        String lvlText = "Уровень " + levelCache;
        AquaFontRenderer.draw(g, font, lvlText, x + 16, y + 26, 0xFFFFFFFF);

        int xpBarX = x + 105;
        int xpBarW = w - 230;
        int xpBarY = y + 26;
        int xpBarH = 12;

        int curXp = Math.max(0, aquaXpCache - minXpCache);
        int reqXp = Math.max(1, maxXpCache - minXpCache);
        float progress = Math.max(0.0f, Math.min(1.0f, (float) curXp / reqXp));

        g.fill(xpBarX, xpBarY, xpBarX + xpBarW, xpBarY + xpBarH, 0xFF0A1320);
        int fillW = (int) (xpBarW * progress);
        if (fillW > 0) {
            g.fill(xpBarX, xpBarY, xpBarX + fillW, xpBarY + xpBarH, 0xFF00E5FF);
        }
        UiDraw.border(g, xpBarX, xpBarY, xpBarW, xpBarH, 0xFF1E293B);

        String xpText = curXp + " / " + reqXp + " XP";
        AquaFontRenderer.drawCentered(g, font, xpText, xpBarX + xpBarW / 2, xpBarY + 2, 0xFFF1F5F9);

        String ptsText = "★ Очки: " + skillPointsCache;
        AquaFontRenderer.draw(g, font, ptsText, x + w - AquaFontRenderer.width(font, ptsText) - 16, y + 26, 0xFFFFD700);
    }

    private void renderDetailSidebar(GuiGraphics g) {
        int px = width - PANEL_W + 8;
        int py = 12;
        int pw = PANEL_W - 16;
        int ph = height - 24;
        unlockBtnHit = false;

        AquaGlassPanel.draw(g, px, py, pw, ph, AquaGlassPanel.FILL, AquaGlassPanel.BORDER, 5, false);

        int tx = px + 16;
        int ty = py + 16;

        if (hoveredNode == null) {
            AquaFontRenderer.drawHeader(g, font, "Детали навыка", tx, ty, PANEL_BORDER);
            AquaFontRenderer.draw(g, font, "Наведите курсор на созвездие", tx, ty + 24, 0xFF94A3B8);
            return;
        }

        boolean isUnlocked = unlockedSnapshot.contains(hoveredNode.id);
        boolean isPrereqUnlocked = hoveredNode.prerequisite == null || unlockedSnapshot.contains(hoveredNode.prerequisite);
        boolean isAvailable = !isUnlocked && isPrereqUnlocked;

        AquaCaseSlot.Rarity slotRarity = hoveredNode.type == NodeType.KEYSTONE ? AquaCaseSlot.Rarity.LEGENDARY
                : hoveredNode.type == NodeType.NOTABLE ? AquaCaseSlot.Rarity.RARE
                : AquaCaseSlot.Rarity.COMMON;
        AquaCaseSlot.draw(g, font, tx, ty, AquaCaseSlot.DEFAULT_SIZE, slotRarity, hoveredNode.displayItem, isAvailable || isUnlocked);
        AquaFontRenderer.drawHeader(g, font, hoveredNode.title, tx + AquaCaseSlot.DEFAULT_SIZE + 10, ty + 4, 0xFFFFFFFF);
        String badge = hoveredNode.type == NodeType.KEYSTONE ? "КЛЮЧЕВОЙ"
                : hoveredNode.type == NodeType.NOTABLE ? "ВЕЛИКИЙ" : "ОБЫЧНЫЙ";
        AquaBadge.draw(g, font, tx + AquaCaseSlot.DEFAULT_SIZE + 10, ty + 20, badge, PANEL_BORDER);
        ty += AquaCaseSlot.DEFAULT_SIZE + 12;

        ty += AquaFontRenderer.drawWrapped(g, font, hoveredNode.description, tx, ty, pw - 32, 0xFFCBD5E1);
        ty += 12;

        if (isUnlocked) {
            AquaFontRenderer.draw(g, font, "Навык изучен", tx, ty, 0xFF10B981);
        } else if (isAvailable) {
            AquaFontRenderer.draw(g, font, "Стоимость: " + hoveredNode.cost + " очк.", tx, ty, 0xFFFFD700);
            ty += 20;
            unlockBtnW = pw - 32;
            unlockBtnH = 26;
            unlockBtnX = tx;
            unlockBtnY = ty;
            unlockBtnHit = true;
            boolean canAfford = skillPointsCache >= hoveredNode.cost;
            int btnFill = canAfford ? 0xFF047857 : 0xFF1E293B;
            int btnBorder = canAfford ? 0xFF10B981 : 0xFF475569;
            AquaGlassPanel.draw(g, unlockBtnX, unlockBtnY, unlockBtnW, unlockBtnH, btnFill, btnBorder, 3, canAfford);
            AquaFontRenderer.drawCentered(g, font, "Изучить", unlockBtnX + unlockBtnW / 2, unlockBtnY + 8,
                    canAfford ? 0xFFFFFFFF : 0xFF94A3B8);
        } else {
            AquaFontRenderer.draw(g, font, "Нужен предыдущий навык", tx, ty, 0xFFEF4444);
        }
    }

    private boolean canUnlock(SkillNode node) {
        if (node == null) {
            return false;
        }
        boolean unlocked = unlockedSnapshot.contains(node.id);
        boolean prereq = node.prerequisite == null || unlockedSnapshot.contains(node.prerequisite);
        return !unlocked && prereq && skillPointsCache >= node.cost;
    }

    private void openUnlockDialog(SkillNode node) {
        String skillId = node.id;
        Minecraft mc = Minecraft.getInstance();
        AquaDialogScreen.confirm(
                mc,
                this,
                "Изучить навык",
                node.title + " стоит " + node.cost + " очк. Списать и открыть узел?",
                () -> {
                    NetworkHandler.CHANNEL.sendToServer(new C2SUnlockSkillPacket(skillId));
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2f, 0.5f));
                }
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && unlockBtnHit
                && mouseX >= unlockBtnX && mouseX < unlockBtnX + unlockBtnW
                && mouseY >= unlockBtnY && mouseY < unlockBtnY + unlockBtnH
                && canUnlock(hoveredNode)) {
            openUnlockDialog(hoveredNode);
            return true;
        }
        if (button == 0 && canUnlock(hoveredNode)) {
            openUnlockDialog(hoveredNode);
            return true;
        }
        if (button == 0 && mouseX < width - PANEL_W) {
            isDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            camX -= dragX / zoom;
            camY -= dragY / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) zoom = Math.min(1.4, zoom * 1.12);
        else if (delta < 0) zoom = Math.max(0.35, zoom / 1.12);
        return true;
    }
}
