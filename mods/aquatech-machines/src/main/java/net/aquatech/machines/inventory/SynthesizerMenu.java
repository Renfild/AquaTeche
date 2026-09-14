package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.SynthesizerBlockEntity;
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

public class SynthesizerMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public SynthesizerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public SynthesizerMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.SYNTHESIZER.get(), id, inv, be, makeSynthData((SynthesizerBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0] Заливка жидкости (ведро лавы или емкость с лавой)
        addSlot(new SlotItemHandler(blockEntity.getItems(), SynthesizerBlockEntity.SLOT_FLUID_IN, 44, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.is(Items.LAVA_BUCKET)
                        || stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
            }
        });

        // [1] Выход пустой тары
        addSlot(output(SynthesizerBlockEntity.SLOT_FLUID_OUT, 44, 51));

        // [2] Твердый реагент A
        addSlot(new SlotItemHandler(blockEntity.getItems(), SynthesizerBlockEntity.SLOT_INPUT_A, 69, 25));

        // [3] Твердая матрица B
        addSlot(new SlotItemHandler(blockEntity.getItems(), SynthesizerBlockEntity.SLOT_INPUT_B, 69, 51));

        // [4, 5] Выход продукции
        addSlot(output(SynthesizerBlockEntity.SLOT_OUTPUT_1, 127, 25));
        addSlot(output(SynthesizerBlockEntity.SLOT_OUTPUT_2, 127, 51));

        // [6] Критический бонус-выход
        addSlot(output(SynthesizerBlockEntity.SLOT_OUTPUT_3, 151, 38));

        // [7] Скорость, [8] Батарея, [9] Энергоэффективность (Выносное крыло апгрейдов)
        addSlot(speedUpgradeSlot(SynthesizerBlockEntity.SLOT_SPEED, 185, 19));
        addSlot(batterySlot(SynthesizerBlockEntity.SLOT_BATTERY, 185, 41));
        addSlot(efficiencyUpgradeSlot(SynthesizerBlockEntity.SLOT_EFF, 185, 63));
    }

    public int getPressure() {
        return data.get(6);
    }

    public int getLavaAmount() {
        return data.get(7);
    }

    public static ContainerData makeSynthData(SynthesizerBlockEntity be) {
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
                        case 6 -> be.getPressure();
                        case 7 -> be.getLavaTank().getFluidAmount();
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
