package com.casesmod.event;

import com.casesmod.data.QuestDefinition;
import com.casesmod.data.QuestManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Автоматически отслеживает прогресс квестов по игровым событиям:
 *  - MINE_BLOCK    -> BlockEvent.BreakEvent (ломание блока игроком)
 *  - KILL_MOB      -> LivingDeathEvent (смерть моба от руки игрока)
 *  - COLLECT_ITEM  -> EntityItemPickupEvent (подбор предмета с земли)
 *
 * При достижении требуемого количества игроку приходит сообщение в чат
 * с предложением зайти в меню (/menu -> Квесты) и забрать награду.
 */
@Mod.EventBusSubscriber(modid = "casesmod")
public class QuestEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == null || event.getPlayer().level().isClientSide) return;
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        addProgressAndNotify(sp, "MINE_BLOCK", blockId.toString(), 1);
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer sp)) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        addProgressAndNotify(sp, "KILL_MOB", mobId.toString(), 1);
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer sp)) return;

        ItemStack stack = event.getItem().getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        addProgressAndNotify(sp, "COLLECT_ITEM", itemId.toString(), stack.getCount());
    }

    private static void addProgressAndNotify(ServerPlayer player, String type, String target, int amount) {
        // Собираем список квестов, которые ещё не выполнены, чтобы понять, кто из них
        // как раз достиг требуемого количества именно этим событием (для уведомления "квест готов!").
        for (QuestDefinition q : QuestManager.INSTANCE.getQuests()) {
            if (!q.type.equalsIgnoreCase(type) || !q.target.equalsIgnoreCase(target)) continue;
            if (QuestManager.INSTANCE.isClaimed(player.getUUID(), q.id)) continue;
            boolean wasComplete = QuestManager.INSTANCE.isComplete(player.getUUID(), q);
            if (wasComplete) continue;

            QuestManager.INSTANCE.addProgress(player.getUUID(), type, target, amount);

            if (QuestManager.INSTANCE.isComplete(player.getUUID(), q)) {
                player.sendSystemMessage(Component.literal(
                        "§a✔ Квест выполнен: §f" + q.displayName + " §7— зайдите в §e/menu §7за наградой!"));
            }
        }
    }
}
