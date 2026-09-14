package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.CentrifugeBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class CentrifugeMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public CentrifugeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public CentrifugeMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.CENTRIFUGE.get(), id, inv, be, makeCentrifugeData((CentrifugeBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0] Заливка морской воды (ведро или емкость с водой)
        addSlot(new SlotItemHandler(blockEntity.getItems(), CentrifugeBlockEntity.SLOT_RAW_WATER_IN, 42, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.is(Items.WATER_BUCKET)
                        || stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
            }
        });

        // [1] Выход пустого ведра
        addSlot(output(CentrifugeBlockEntity.SLOT_RAW_WATER_OUT, 42, 51));

        // [2] Пустая тара под дистиллят (ведро, колба, емкость)
        addSlot(new SlotItemHandler(blockEntity.getItems(), CentrifugeBlockEntity.SLOT_DISTILL_EMPTY, 156, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.is(Items.BUCKET) || stack.is(Items.GLASS_BOTTLE)
                        || stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
            }
        });

        // [3] Наполненный дистиллят
        addSlot(output(CentrifugeBlockEntity.SLOT_DISTILL_FULL, 156, 51));

        // [4, 5, 6, 7] Сетка 2×2 под минералы и соли
        addSlot(output(CentrifugeBlockEntity.SLOT_MINERAL_0, 97, 27));
        addSlot(output(CentrifugeBlockEntity.SLOT_MINERAL_1, 116, 27));
        addSlot(output(CentrifugeBlockEntity.SLOT_MINERAL_2, 97, 49));
        addSlot(output(CentrifugeBlockEntity.SLOT_MINERAL_3, 116, 49));

        // [8] Скорость, [9] Энергоэффективность, [10] Батарея (В правом выносном крыле)
        addSlot(speedUpgradeSlot(CentrifugeBlockEntity.SLOT_SPEED, 185, 19));
        addSlot(batterySlot(CentrifugeBlockEntity.SLOT_BATTERY, 185, 41));
        addSlot(efficiencyUpgradeSlot(CentrifugeBlockEntity.SLOT_EFF, 185, 63));
    }

    public int getRawWaterAmount() {
        return data.get(6);
    }

    public int getDistillateAmount() {
        return data.get(7);
    }

    public static ContainerData makeCentrifugeData(CentrifugeBlockEntity be) {
        return new ContainerData() {
            private final int[] cache = new int[8];

            @Override
            public int get(int index) {
                if (be != null && be.getLevel() != null && !be.getLevel().isClientSide) {
                    return switch (index) {
                        case 0 -> be.getProgressValue();
                        case 1 -> be.getMaxProgressValue();
                        case 2 -> be.getEnergy() & 0xFFFF;
                        case 3 -> (be.getEnergy() >>> 16) & 0xFFFF;
                        case 4 -> be.getMaxEnergy() & 0xFFFF;
                        case 5 -> (be.getMaxEnergy() >>> 16) & 0xFFFF;
                        case 6 -> be.getRawWaterTank().getFluidAmount();
                        case 7 -> be.getDistillateTank().getFluidAmount();
                        default -> 0;
                    };
                }
                return (index >= 0 && index < cache.length) ? cache[index] : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < cache.length) {
                    cache[index] = value;
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    public BlockPos getPos() {
        return pos;
    }
}
