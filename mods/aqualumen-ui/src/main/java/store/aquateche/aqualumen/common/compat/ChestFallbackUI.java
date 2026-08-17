package store.aquateche.aqualumen.common.compat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.common.service.HubActionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback hub for clients without the mod (plain vanilla players joining a Mohist server).
 * Same information architecture as the rich screen, rendered with item icons in a 3 row chest.
 */
public final class ChestFallbackUI {

    private static final int SIZE = 27;

    private ChestFallbackUI() {
    }

    public static void open(ServerPlayer player, HubSnapshot snapshot) {
        SimpleContainer container = build(snapshot);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, owner) -> new FallbackMenu(id, inventory, container),
                Component.translatable("gui.aqualumen.hub")));
    }

    private static SimpleContainer build(HubSnapshot snapshot) {
        SimpleContainer container = new SimpleContainer(SIZE);

        container.setItem(10, icon(Items.PLAYER_HEAD, "\u00a7b" + snapshot.profile().name(),
                "\u00a77\u0420\u0430\u043d\u0433: " + snapshot.profile().rank(),
                "\u00a77\u0423\u0440\u043e\u0432\u0435\u043d\u044c: " + snapshot.profile().level(),
                "\u00a77\u041d\u0430\u0438\u0433\u0440\u0430\u043d\u043e: " + snapshot.profile().playtimeMinutes() + " \u043c\u0438\u043d"));
        container.setItem(12, icon(Items.EMERALD, "\u00a7a\u041c\u0430\u0433\u0430\u0437\u0438\u043d",
                "\u00a77\u041c\u043e\u043d\u0435\u0442\u044b: " + snapshot.wallet().coins(),
                "\u00a77\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b: " + snapshot.wallet().gems()));
        container.setItem(13, icon(Items.ENDER_CHEST, "\u00a7d\u041a\u0435\u0439\u0441\u044b",
                "\u00a77\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e: " + snapshot.cases().size()));
        container.setItem(14, icon(Items.WRITTEN_BOOK, "\u00a76\u0411\u043e\u0435\u0432\u043e\u0439 \u043f\u0440\u043e\u043f\u0443\u0441\u043a",
                "\u00a77\u0423\u0440\u043e\u0432\u0435\u043d\u044c " + snapshot.season().tier() + "/" + snapshot.season().maxTier()));
        container.setItem(16, icon(Items.GOLDEN_APPLE, "\u00a7e\u0422\u043e\u043f\u044b",
                "\u00a77\u041e\u043d\u043b\u0430\u0439\u043d: " + snapshot.server().online() + "/" + snapshot.server().slots()));
        container.setItem(22, icon(Items.BARRIER, "\u00a7c\u0417\u0430\u043a\u0440\u044b\u0442\u044c"));

        ItemStack filler = icon(Items.CYAN_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < SIZE; slot++) {
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, filler.copy());
            }
        }
        return container;
    }

    private static ItemStack icon(net.minecraft.world.item.Item item, String name, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name).withStyle(style -> style.withItalic(false)));
        if (lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(Component.literal(line));
            }
            net.minecraft.nbt.ListTag tag = new net.minecraft.nbt.ListTag();
            for (Component line : lines) {
                tag.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(line)));
            }
            stack.getOrCreateTagElement("display").put("Lore", tag);
        }
        return stack;
    }

    /** Read only chest: clicks are converted to hub actions, items can never be taken out. */
    private static final class FallbackMenu extends ChestMenu {

        private FallbackMenu(int containerId, Inventory inventory, Container container) {
            super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
        }

        @Override
        public void clicked(int slotId, int button, ClickType clickType, Player player) {
            if (slotId < 0 || slotId >= SIZE || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            switch (slotId) {
                case 12 -> HubActionHandler.handle(serverPlayer, "store.buy", "fallback.menu");
                case 13 -> HubActionHandler.handle(serverPlayer, "case.open", "case.common");
                case 14 -> HubActionHandler.handle(serverPlayer, "pass.claim", "current");
                case 16 -> HubActionHandler.handle(serverPlayer, "hub.refresh", "");
                case 22 -> serverPlayer.closeContainer();
                default -> {
                    // decorative slot
                }
            }
            serverPlayer.sendSystemMessage(Component.translatable("msg.aqualumen.fallback").withStyle(ChatFormatting.DARK_GRAY));
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
