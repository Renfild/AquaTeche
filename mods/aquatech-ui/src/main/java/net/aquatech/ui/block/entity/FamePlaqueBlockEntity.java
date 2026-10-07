package net.aquatech.ui.block.entity;

import net.aquatech.ui.fishing.FameLogic;
import net.aquatech.ui.fishing.FameService;
import net.aquatech.ui.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Привязанный вид рыбы и копия рекорда для клиента. Рекорд подтягивается с сервера раз в две секунды. */
public class FamePlaqueBlockEntity extends BlockEntity {

    private static final int REFRESH_TICKS = 40;

    /** Максимальное место, до которого оператор может прокрутить автоматический режим. */
    public static final int MAX_RANK = 12;

    private String species = "";
    /** Автоматический режим: показывает рекорд на этом месте по весу среди всех видов; 0 = вид привязан вручную. */
    private int rank = 1;
    private String holder = "";
    private int grams;
    private int cm;

    public FamePlaqueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FAME_PLAQUE.get(), pos, state);
    }

    public int rank() {
        return rank;
    }

    /** Включает автоматический режим на заданное место (по кругу 1..MAX_RANK). */
    public void setRank(int newRank) {
        rank = newRank;
        species = "";
        refresh(true);
    }

    public String species() {
        return species;
    }

    public String holder() {
        return holder;
    }

    public int grams() {
        return grams;
    }

    public int cm() {
        return cm;
    }

    /** Привязывает вид (пустая строка очищает) и сразу подтягивает рекорд. */
    public void bind(String newSpecies) {
        rank = 0;
        species = newSpecies;
        refresh(true);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FamePlaqueBlockEntity plaque) {
        if ((plaque.rank > 0 || !plaque.species.isEmpty()) && (level.getGameTime() + pos.asLong()) % REFRESH_TICKS == 0) {
            plaque.refresh(false);
        }
    }

    private void refresh(boolean force) {
        if (rank > 0) {
            String auto = FameService.speciesAtRank(rank);
            if (!auto.equals(species)) {
                species = auto;
                force = true;
            }
        }
        FameLogic.Record record = species.isEmpty() ? null : FameService.get(species);
        String newHolder = record == null ? "" : record.name;
        int newGrams = record == null ? 0 : record.grams;
        int newCm = record == null ? 0 : record.cm;
        if (!force && newHolder.equals(holder) && newGrams == grams && newCm == cm) {
            return;
        }
        holder = newHolder;
        grams = newGrams;
        cm = newCm;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Species", species);
        tag.putInt("Rank", rank);
        tag.putString("Holder", holder);
        tag.putInt("Grams", grams);
        tag.putInt("Cm", cm);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        species = tag.getString("Species");
        rank = tag.contains("Rank") ? tag.getInt("Rank") : 0;
        holder = tag.getString("Holder");
        grams = tag.getInt("Grams");
        cm = tag.getInt("Cm");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
