package net.aquatech.ui.event;

import net.aquatech.ui.fishing.FishingRodCompat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "aquatech_ui", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarCatcherEnchantmentHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (isAquaTechOrStarCatcherRod(left) || isAquaTechOrStarCatcherRod(right)) {
            Map<Enchantment, Integer> rightEnchants = EnchantmentHelper.getEnchantments(right);
            if (rightEnchants.containsKey(Enchantments.MENDING) || rightEnchants.containsKey(Enchantments.UNBREAKING) || isAquaTechOrStarCatcherRod(right)) {
                event.setCanceled(true);
                event.setOutput(ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        if (event.getEntity() != null) {
            for (ItemStack hand : event.getEntity().getHandSlots()) {
                if (isAquaTechOrStarCatcherRod(hand)) {
                    stripForbiddenEnchantments(hand);
                }
            }
        }
    }

    private static boolean isAquaTechOrStarCatcherRod(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return FishingRodCompat.getRodId(stack) != null;
    }

    public static void stripForbiddenEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (enchants.containsKey(Enchantments.MENDING) || enchants.containsKey(Enchantments.UNBREAKING)) {
            enchants.remove(Enchantments.MENDING);
            enchants.remove(Enchantments.UNBREAKING);
            EnchantmentHelper.setEnchantments(enchants, stack);
        }
    }
}
