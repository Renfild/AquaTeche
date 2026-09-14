package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.ExtractorBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ExtractorMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public ExtractorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ExtractorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.EXTRACTOR.get(), id, inv, be, makeData((ExtractorBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0] Входной слот сырья
        addSlot(new SlotItemHandler(blockEntity.getItems(), ExtractorBlockEntity.SLOT_INPUT, 45, 37));
        // [1] Выход готовой продукции
        addSlot(output(ExtractorBlockEntity.SLOT_OUTPUT, 115, 35));
        // [2] Апгрейд скорости (выносное крыло)
        addSlot(speedUpgradeSlot(ExtractorBlockEntity.SLOT_SPEED, 185, 19));
        // [3] Слот батареи / FE-накопителя (выносное крыло)
        addSlot(batterySlot(ExtractorBlockEntity.SLOT_BATTERY, 185, 41));
        // [4] Апгрейд энергоэффективности (выносное крыло)
        addSlot(efficiencyUpgradeSlot(ExtractorBlockEntity.SLOT_EFF, 185, 63));
    }

    public BlockPos getPos() {
        return pos;
    }
}
