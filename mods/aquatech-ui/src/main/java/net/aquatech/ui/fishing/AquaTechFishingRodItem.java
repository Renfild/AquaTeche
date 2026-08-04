package net.aquatech.ui.fishing;

import net.aquatech.ui.client.ClientItemActions;
import net.aquatech.ui.inventory.TackleBoxMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AquaTechFishingRodItem extends FishingRodItem {
    private final RodType rodType;

    public enum RodType {
        NOVICE("novice", "Удочка новичка", 100),
        IRON("iron", "Железная удочка", 250),
        GOLD("gold", "Золотая удочка", 150),
        DIAMOND("diamond", "Алмазная удочка", 1000),
        EMERALD("emerald", "Изумрудная удочка", 800),
        NETHERITE("netherite", "Незеритовая удочка", 2000),
        PRISMARINE("prismarine", "Призмариновая удочка", 600),
        THERMAL("thermal", "Термальная удочка", 1200),
        KINETIC("kinetic", "Кинетическая удочка", 1000),
        ENDER("ender", "Эндер-удочка", 1500),
        ABYSSAL("abyssal", "Абиссальная удочка", 3000);

        private final String id;
        private final String russianName;
        private final int durability;

        RodType(String id, String russianName, int durability) {
            this.id = id;
            this.russianName = russianName;
            this.durability = durability;
        }

        public String getId() {
            return id;
        }

        public String getRussianName() {
            return russianName;
        }

        public int getDurability() {
            return durability;
        }
    }

    public AquaTechFishingRodItem(RodType rodType, Item.Properties properties) {
        super(properties.durability(rodType.getDurability()));
        this.rodType = rodType;
    }

    public RodType getRodType() {
        return rodType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (id, inv, p) -> new TackleBoxMenu(id, inv, itemstack),
                        Component.literal("Снасти: " + rodType.getRussianName())
                ), buffer -> buffer.writeItem(itemstack));
            }
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        // Standard fishing cast with sound effect
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        AtomicBoolean shift = new AtomicBoolean(false);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> shift.set(ClientItemActions.hasShiftDown()));
        tooltipComponents.add(Component.literal("§bIU-ресурсы и нефть — с улова AquaTech (без кирки)."));
        if (shift.get()) {
            tooltipComponents.add(Component.literal("§eТАБЛИЦА УЛОВА (0% рыбного мусора):"));
            switch (this.rodType) {
                case NOVICE -> {
                    tooltipComponents.add(Component.literal("§7• Рыба/дерево/ламинария + IU медь/олово/латекс"));
                    tooltipComponents.add(Component.literal("§7• Торф, селитра, кальций, саженец гевеи (шанс)"));
                }
                case IRON -> {
                    tooltipComponents.add(Component.literal("§7• IU пластины + олово/медь"));
                    tooltipComponents.add(Component.literal("§7• Торф/селитра/гевея — чаще, чем у новичка"));
                }
                case GOLD -> {
                    tooltipComponents.add(Component.literal("§7• Бронза, синтетический каучук"));
                    tooltipComponents.add(Component.literal("§7• Банки пчёл IU (редко) + смола"));
                }
                case DIAMOND, EMERALD, NETHERITE, PRISMARINE -> {
                    tooltipComponents.add(Component.literal("§7• Нефть IU (вёдра crude) — замена станка-качалки"));
                    tooltipComponents.add(Component.literal("§7• Пчёлы, смола, каучук, торф культивированный"));
                }
                case THERMAL, KINETIC, ENDER -> {
                    tooltipComponents.add(Component.literal("§7• Больше crude + мазут/industrial oil"));
                    tooltipComponents.add(Component.literal("§7• Минеральные кристаллы IU (редко)"));
                }
                case ABYSSAL -> {
                    tooltipComponents.add(Component.literal("§7• Motor oil + широкий набор минералов IU"));
                    tooltipComponents.add(Component.literal("§7• Эхо/реликвии бездны"));
                }
            }
            int lo = RodLootRanges.min(this.rodType);
            int hi = RodLootRanges.max(this.rodType);
            tooltipComponents.add(Component.literal("§6Общий улов: §f" + lo + " - " + hi + " предметов / заброс"));
            tooltipComponents.add(Component.literal("§8(×0.70–1.25 от мини-игры Tide Tension)"));
        } else {
            tooltipComponents.add(Component.literal("§8[Зажми §eSHIFT§8 для таблицы вероятностей]"));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}
