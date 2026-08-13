package net.aquatech.ui.network;

import net.aquatech.ui.client.ClientItemActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: start StarCatcher-style fishing mini-game on client. */
public class S2CStartRhythmHookPacket {

    private final int seed;
    private final int fishHp;
    private final float spotSize;
    private final float yellowPad;
    private final float pointerSpeed;
    private final float decay;
    private final boolean elite;
    private final boolean treasure;

    public S2CStartRhythmHookPacket(int seed, int fishHp, float spotSize, float yellowPad,
                                    float pointerSpeed, float decay, boolean elite, boolean treasure) {
        this.seed = seed;
        this.fishHp = fishHp;
        this.spotSize = spotSize;
        this.yellowPad = yellowPad;
        this.pointerSpeed = pointerSpeed;
        this.decay = decay;
        this.elite = elite;
        this.treasure = treasure;
    }

    public S2CStartRhythmHookPacket(FriendlyByteBuf buf) {
        this.seed = buf.readInt();
        this.fishHp = buf.readVarInt();
        this.spotSize = buf.readFloat();
        this.yellowPad = buf.readFloat();
        this.pointerSpeed = buf.readFloat();
        this.decay = buf.readFloat();
        this.elite = buf.readBoolean();
        this.treasure = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(seed);
        buf.writeVarInt(fishHp);
        buf.writeFloat(spotSize);
        buf.writeFloat(yellowPad);
        buf.writeFloat(pointerSpeed);
        buf.writeFloat(decay);
        buf.writeBoolean(elite);
        buf.writeBoolean(treasure);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        int s = seed;
        int hp = fishHp;
        float ss = spotSize;
        float yp = yellowPad;
        float sp = pointerSpeed;
        float dc = decay;
        boolean el = elite;
        boolean tr = treasure;
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientItemActions.startRhythmHook(s, hp, ss, yp, sp, dc, el, tr)));
        return true;
    }
}
