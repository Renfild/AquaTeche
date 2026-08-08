---
name: minecraft-gui-pixelart
description: Expert Minecraft Pixel Art GUI designer and generator skill. Provides authoritative standards for 18x18px slot grids, 9-slice frame scaling, minimalist tech frames, radar sonar widgets, liquid gauges, HTML/Canvas pixel previewers, and Forge/Fabric 1.20.1 texture export.
---

# Minimalist Minecraft Pixel Art GUI Skill

This skill provides step-by-step guidance and exact pixel standards for creating minimalist, high-contrast Minecraft UI/UX containers (e.g. Auto-Fisher, Abyssal Case, Auto-Sorter) with floating title headers, sonar/radar widgets, upgrade slots, and clean player inventory grids.

---

## 1. Minimalist Tech Design Principles (Auto-Fisher Style)

| Element | Pixel Specs | Color Hex Codes | Notes |
| :--- | :--- | :--- | :--- |
| **Top Title Badge** | $90 \times 16$ px (Floating) | Border: `#00e5ff`, Text: `#ffffff` | Overhangs top frame border |
| **Tech Panel Background** | Dark Blue-Black | `#040b13` / `#06121e` | High contrast, ultra-clean |
| **Tech Panel Borders** | 2px Crisp Pixel Outline | `#0a314d` (outer), `#00e5ff` (glow) | Dual-line neon accent |
| **Central Radar / Sonar** | $50 \times 50$ px | `#00e5ff` cyan pixels | Animated pulsing sonar rings |
| **Upgrade Slots ($2\times2$)** | $18 \times 18$ px per slot | Inner: `#030910`, Outline: `#0a3a5c` | For Speed, Luck, Filter modules |
| **Vertical Status Meter** | $14 \times 64$ px (Right side) | `#00e5ff` gradient fill | Shows depth, energy or progress |
| **Player Inventory Grid** | Classic Vanilla Grey ($176 \times 96$ px) | Panel: `#c6c6c6`, Border: `#373737` | Standard 9x3 + 9 hotbar slots |

---

## 2. Advanced Slot & Logic Architecture

### Auto-Fisher / Processing Container Logic:
1. **Tool Slot (Left)**: Holds Fishing Rod or Key Card. Accepts durability metadata.
2. **Sonar Radar (Center)**: Displays active scan animation when powered.
3. **Upgrades Grid ($2\times2$ Right)**:
   - Slot 1: **Speed Upgrade** (Reduces catch tick delay).
   - Slot 2: **Luck Upgrade** (Increases treasure roll table).
   - Slot 3: **Magnet Upgrade** (Auto-collects drops).
   - Slot 4: **Energy/Fluid Upgrade** (Power efficiency).
4. **Depth / Fluid Meter (Far Right)**: Displays current operating depth or water buffer.

---

## 3. Standard Layout Bounds ($176 \times 176$ px Canvas)

```
+----------------------------------------+
|           [ АВТО-РЫБОЛОВ ]             |  <-- Floating Header
|+--------------------------------------+|
|| [Rod]  (( (RADAR) ))   [Up1] [Up2]   ||  <-- Tech Machine Area
||                        [Up3] [Up4]   ||
|+--------------------------------------+|
|+--------------------------------------+|
|| [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ||  <-- Player Inventory (9x3)
|| [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ||
|| [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ||
||                                      ||
|| [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]  ||  <-- Player Hotbar (9x1)
|+--------------------------------------+|
+----------------------------------------+
```

---

## 4. Forge 1.20.1 Render Code Pattern

```java
@Override
protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    int x = (this.width - this.imageWidth) / 2;
    int y = (this.height - this.imageHeight) / 2;

    // Render Minimalist Tech Container Frame
    guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

    // Render Animated Sonar Radar Wave if machine is active
    if (this.menu.isMiningActive()) {
        int progress = this.menu.getRadarProgress(); // 0 to 16
        guiGraphics.blit(TEXTURE, x + 72, y + 24, 176, 0, 16, 16);
    }
}
```
