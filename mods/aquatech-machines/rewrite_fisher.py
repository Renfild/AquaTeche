# -*- coding: utf-8 -*-
# Fisher BE v2: слот удочки [0], слот апгрейда [1], выход [2].
# Базовый режим — ресурсный лут по тиру удочки; с апгрейдом «Ядро Рыболова» — рыба по ростеру.
code = '''package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.registry.ModItems;
import net.aquatech.machines.util.FisherLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Рыболов MK-2: удочка StarCatcher в слоте — машина ловит ресурсный лут тира.
 * С апгрейдом «Ядро Рыболова» (8x8 Avaritia) — ловит рыбу по ростеру тира.
 */
public class FisherBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_ROD = 0;
    public static final int SLOT_UPGRADE = 1;
    public static final int SLOT_OUTPUT = 2;

    public FisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHER.get(), pos, state, 3, 200000, 1024, 100, 40, 200000);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    private int rodTier() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 0;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(rod.getItem());
        return FisherLoot.tierOf(id);
    }

    private boolean hasFishUpgrade() {
        return !items.getStackInSlot(SLOT_UPGRADE).isEmpty()
                && items.getStackInSlot(SLOT_UPGRADE).is(ModItems.FISHING_CORE.get());
    }

    @Override
    protected boolean hasWork() {
        return rodTier() > 0;
    }

    @Override
    protected void craftOnce() {
        int tier = rodTier();
        if (tier <= 0 || level == null) return;
        ItemStack loot = hasFishUpgrade()
                ? FisherLoot.roll(tier, level.getRandom())
                : FisherLoot.rollResources(tier, level.getRandom());
        if (loot == null || loot.isEmpty()) return;
        ItemStack rest = items.insertItem(SLOT_OUTPUT, loot, false);
        if (!rest.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity drop =
                    new net.minecraft.world.entity.item.ItemEntity(level, worldPosition.getX() + 0.5,
                            worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, rest);
            level.addFreshEntity(drop);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.FisherMenu(id, inv, this);
    }
}
'''
open("src/main/java/net/aquatech/machines/block/entity/FisherBlockEntity.java", "w", encoding="utf-8", newline="\n").write(code)
print("fisher v2")
