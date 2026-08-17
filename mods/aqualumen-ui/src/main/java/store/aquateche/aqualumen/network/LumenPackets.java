package store.aquateche.aqualumen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import store.aquateche.aqualumen.client.LumenClient;
import store.aquateche.aqualumen.common.ServerEvents;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.common.service.HubActionHandler;

import java.util.function.Supplier;

public final class LumenPackets {

    private LumenPackets() {
    }

    /** Sent once per join so the server knows this client can render the rich hub. */
    public static final class ClientHello {
        private final String clientVersion;

        public ClientHello(String clientVersion) {
            this.clientVersion = clientVersion;
        }

        public ClientHello(FriendlyByteBuf buf) {
            this.clientVersion = buf.readUtf(32);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(clientVersion, 32);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                ServerEvents.markModded(sender);
            }
            ctx.setPacketHandled(true);
        }
    }

    /** Client intent. The payload is an identifier only, never a computed result. */
    public static final class HubAction {
        private final String action;
        private final String argument;

        public HubAction(String action, String argument) {
            this.action = action;
            this.argument = argument;
        }

        public HubAction(FriendlyByteBuf buf) {
            this.action = buf.readUtf(48);
            this.argument = buf.readUtf(64);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(action, 48);
            buf.writeUtf(argument, 64);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                HubActionHandler.handle(sender, action, argument);
            }
            ctx.setPacketHandled(true);
        }
    }

    /** Server state for the hub. {@code openScreen} distinguishes an open request from a refresh. */
    public static final class HubSync {
        private final HubSnapshot snapshot;
        private final boolean openScreen;

        public HubSync(HubSnapshot snapshot, boolean openScreen) {
            this.snapshot = snapshot;
            this.openScreen = openScreen;
        }

        public HubSync(FriendlyByteBuf buf) {
            this.snapshot = HubSnapshot.read(buf);
            this.openScreen = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            snapshot.write(buf);
            buf.writeBoolean(openScreen);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LumenClient.acceptSync(snapshot, openScreen));
            ctx.setPacketHandled(true);
        }
    }
}
