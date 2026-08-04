package net.aquatech.ui.item;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbyssalMagnetItem extends Item {

    private static final Map<UUID, Boolean> MAGNET_CACHE = new ConcurrentHashMap<>();

    public AbyssalMagnetItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            MAGNET_CACHE.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player == null || player.level().isClientSide) return;
        if (player.tickCount % 5 != 0) return;

        UUID id = player.getUUID();
        boolean hasMagnet;
        if (player.tickCount % 20 == 0 || !MAGNET_CACHE.containsKey(id)) {
            hasMagnet = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof AbyssalMagnetItem) {
                    hasMagnet = true;
                    break;
                }
            }
            MAGNET_CACHE.put(id, hasMagnet);
        } else {
            hasMagnet = Boolean.TRUE.equals(MAGNET_CACHE.get(id));
        }

        if (!hasMagnet) return;

        AABB area = player.getBoundingBox().inflate(8.0D);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity item : items) {
            if (!item.isAlive() || item.hasPickUpDelay()) continue;
            Vec3 vector = new Vec3(
                    player.getX() - item.getX(),
                    player.getY() + 0.5D - item.getY(),
                    player.getZ() - item.getZ()
            ).normalize().scale(0.35D);
            item.setDeltaMovement(item.getDeltaMovement().add(vector));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§9Автоматически притягивает выловленные предметы в радиусе 8 блоков"));
    }
}
