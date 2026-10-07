package net.aquatech.ui.item;

import net.aquatech.ui.fishing.LeyFightService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Удочка Лея: ловит только Старого Лея и ничего больше. ПКМ забрасывает леску в его сторону, дальше схватку ведёт
 * {@link LeyFightService}, а клиент читает ПКМ и клавиши A/D сам, пока идёт схватка.
 */
public class LeyRodItem extends Item {

    public LeyRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            LeyFightService.tryStart(serverPlayer);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Ловит только Старого Лея, раз в неделю."));
        tooltip.add(Component.literal("§eПКМ§7 в сторону Лея: забросить леску."));
        tooltip.add(Component.literal("§eДержите ПКМ§7: мотать, пока полоска в зелёной зоне."));
        tooltip.add(Component.literal("§eA / D§7: тянуть против рывка Лея."));
    }
}
