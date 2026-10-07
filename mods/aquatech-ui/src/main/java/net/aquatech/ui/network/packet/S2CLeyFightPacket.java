package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.fishing.LeyFightClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: состояние схватки со Старым Леем для шкал на экране. Раз в пару тиков, пока схватка идёт. */
public class S2CLeyFightPacket {

    public static final byte MODE_END = 0;
    public static final byte MODE_WAITING = 1;
    public static final byte MODE_FIGHT = 2;

    private final byte mode;
    private final float tension;
    private final float stamina;
    private final byte phase;
    private final byte surgeDir;

    public S2CLeyFightPacket(byte mode, float tension, float stamina, byte phase, byte surgeDir) {
        this.mode = mode;
        this.tension = tension;
        this.stamina = stamina;
        this.phase = phase;
        this.surgeDir = surgeDir;
    }

    public S2CLeyFightPacket(FriendlyByteBuf buf) {
        this.mode = buf.readByte();
        this.tension = buf.readFloat();
        this.stamina = buf.readFloat();
        this.phase = buf.readByte();
        this.surgeDir = buf.readByte();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(mode);
        buf.writeFloat(tension);
        buf.writeFloat(stamina);
        buf.writeByte(phase);
        buf.writeByte(surgeDir);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        byte m = mode;
        float t = tension;
        float s = stamina;
        byte p = phase;
        byte d = surgeDir;
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> LeyFightClient.apply(m, t, s, p, d)));
        ctx.setPacketHandled(true);
        return true;
    }
}
