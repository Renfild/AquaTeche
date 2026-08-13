package net.aquatech.ui.event;

import net.aquatech.ui.fishing.AquaTechFishingRodItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.Collections;
import java.util.List;

/**
 * Fired on the Forge EVENT_BUS after AquaTech awards a fishing catch to the player inventory.
 * Other mods (e.g. CasesMod quests) can listen without hard-depending on fishing internals.
 */
public class AquaFishCaughtEvent extends Event {
    private final ServerPlayer player;
    private final AquaTechFishingRodItem.RodType rodType;
    private final List<ItemStack> drops;
    private final float lootScale;
    private final int quality;

    public AquaFishCaughtEvent(ServerPlayer player, AquaTechFishingRodItem.RodType rodType,
                               List<ItemStack> drops, float lootScale, int quality) {
        this.player = player;
        this.rodType = rodType;
        this.drops = drops;
        this.lootScale = lootScale;
        this.quality = quality;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AquaTechFishingRodItem.RodType getRodType() {
        return rodType;
    }

    /** Drops that were awarded (copies). */
    public List<ItemStack> getDrops() {
        return Collections.unmodifiableList(drops);
    }

    public float getLootScale() {
        return lootScale;
    }

    public int getQuality() {
        return quality;
    }
}
