package store.aquateche.aqualumen.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.service.HubDataService;

public final class ModRegistries {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AquaLumenUI.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AquaLumenUI.MODID);

    /** Right click opens the hub. Given on first join, non droppable in most server setups. */
    public static final RegistryObject<Item> HUB_COMPASS = ITEMS.register("hub_compass",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)) {
                @Override
                public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                    if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                        HubDataService.open(serverPlayer);
                    }
                    return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
                }
            });

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.aqualumen.tab"))
                    .icon(() -> new ItemStack(HUB_COMPASS.get()))
                    .displayItems((params, output) -> output.accept(HUB_COMPASS.get()))
                    .build());

    private ModRegistries() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
