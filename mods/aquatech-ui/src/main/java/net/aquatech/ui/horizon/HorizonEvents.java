package net.aquatech.ui.horizon;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.entity.WorkingMachineTracker;
import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.S2CSyncSkillsPacket;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Tracks daily Horizon contracts and greets players on login.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HorizonEvents {

    private HorizonEvents() {
    }

    private static long dayKey(ServerPlayer player) {
        return player.level().getDayTime() / 24000L;
    }

    private static void sync(ServerPlayer player, AquaSkillCapability cap) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncSkillsPacket(cap));
    }

    private static boolean holdingAquaRod(ServerPlayer player) {
        return FishingRodCompat.isSupportedRod(player.getMainHandItem())
                || FishingRodCompat.isSupportedRod(player.getOffhandItem());
    }

    private static int countCopperCoins(ServerPlayer player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && "lightmanscurrency".equals(id.getNamespace())
                    && "coin_copper".equals(id.getPath())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var server = player.getServer();
        if (server == null) return;
        // Never send custom S2C during handshake — Mohist desyncs with fieldSize/EOFException.
        server.tell(new net.minecraft.server.TickTask(
                server.getTickCount() + NetworkHandler.LOGIN_READY_DELAY_TICKS,
                () -> {
                    if (player.hasDisconnected()) return;
                    if (!NetworkHandler.canReceivePlayPackets(player)) return;
                    player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                        cap.ensureDaily(dayKey(player));
                        sync(player, cap);
                        HorizonRoute.DailyContract c = cap.currentContract();
                        player.displayClientMessage(Component.literal(
                                "§b≋ Горизонт " + cap.getHorizonTier() + ": §e" + HorizonRoute.tierName(cap.getHorizonTier())), false);
                        if (!cap.isDailyClaimed()) {
                            player.displayClientMessage(Component.literal(
                                    "§7Контракт дня: §f" + c.title + " §a" + cap.getDailyProgress() + "§7/§a" + c.target
                                            + " §8(/aquatech daily)"), false);
                        } else {
                            player.displayClientMessage(Component.literal(
                                    "§8Контракт дня сдан. Завтра — новый."), false);
                        }
                        player.displayClientMessage(Component.literal(
                                "§8Варпы: §7/warp pier · market · atoll · harbor"), false);
                        if (StormEvent.isActive()) {
                            player.displayClientMessage(Component.literal("§9⚡ Шторм Горизонта активен — редкий улов ×2"), false);
                        }
                    });
                }));
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (event.isCanceled()) return; // Tide Tension awards FISH progress on success
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!holdingAquaRod(player)) return;
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            cap.ensureDaily(dayKey(player));
            if (cap.currentContract() == HorizonRoute.DailyContract.FISH) {
                int before = cap.getDailyProgress();
                cap.addDailyProgress(1);
                if (cap.getDailyProgress() != before) {
                    maybeSyncProgress(player, cap);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var state = event.getState();
        boolean kelp = state.getBlock() instanceof KelpBlock
                || state.getBlock() instanceof KelpPlantBlock
                || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS);
        if (!kelp) return;
        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            cap.ensureDaily(dayKey(player));
            if (cap.currentContract() == HorizonRoute.DailyContract.KELP) {
                int before = cap.getDailyProgress();
                cap.addDailyProgress(1);
                if (cap.getDailyProgress() != before) {
                    maybeSyncProgress(player, cap);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            cap.ensureDaily(dayKey(player));
            if (cap.isDailyClaimed()) return;

            HorizonRoute.DailyContract c = cap.currentContract();
            int before = cap.getDailyProgress();

            if (c == HorizonRoute.DailyContract.DEPTH) {
                PressureBridge.PressureInfo info = PressureBridge.fromPlayer(player);
                if (info.effective() >= 8) {
                    cap.addDailyProgress(1);
                }
            } else if (c == HorizonRoute.DailyContract.MACHINE) {
                if (nearWorkingMachine(player)) {
                    cap.addDailyProgress(1);
                }
            } else if (c == HorizonRoute.DailyContract.MARKET) {
                int coins = countCopperCoins(player);
                if (coins > cap.getDailyProgress()) {
                    cap.addDailyProgress(coins - cap.getDailyProgress());
                }
            }

            if (cap.getDailyProgress() != before) {
                maybeSyncProgress(player, cap);
            }
        });
    }

    private static boolean nearWorkingMachine(ServerPlayer player) {
        return WorkingMachineTracker.hasWorkingNear(player.level(), player.blockPosition(), 6, 3);
    }

    private static void maybeSyncProgress(ServerPlayer player, AquaSkillCapability cap) {
        int p = cap.getDailyProgress();
        if (cap.isDailyComplete() || p == 1 || p % 5 == 0) {
            sync(player, cap);
            if (cap.isDailyComplete()) {
                player.displayClientMessage(Component.literal(
                        "§a✓ Контракт готов — §e/aquatech daily §aчтобы сдать"), true);
            }
        }
    }
}
