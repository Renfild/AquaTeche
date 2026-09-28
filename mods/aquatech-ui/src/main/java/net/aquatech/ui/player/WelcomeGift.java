package net.aquatech.ui.player;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Бесплатный стартовый кейс новичку на первом входе. Ключи кейсов лежат в persistentData
 * игрока под тегом aqualumen_case_keys -> {caseId: count} — та же структура, что читает
 * aqualumen HubActionHandler.caseKeyCount/grantCaseKey. Модули связаны только через NBT,
 * без compile-зависимости (см. AGENTS.md).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class WelcomeGift {

    private static final String TAG_GIVEN = "aquatech_ui:welcome_case_given";
    private static final String CASE_KEYS_TAG = "aqualumen_case_keys";
    private static final String STARTER_CASE_ID = "starter";

    private WelcomeGift() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_GIVEN)) return;
        data.putBoolean(TAG_GIVEN, true);

        CompoundTag keys = data.getCompound(CASE_KEYS_TAG);
        keys.putInt(STARTER_CASE_ID, keys.getInt(STARTER_CASE_ID) + 1);
        data.put(CASE_KEYS_TAG, keys);

        player.sendSystemMessage(Component.literal(
                "§6[AquaTech] §fТебе подарен §bКейс I: Первопроходец Океана§f — открой его в "
                        + "§eF4 §fна вкладке §bКейсы§f!"));
    }

    /** Тестовая команда /aquatech debug: убирает флаг выдачи и сам подаренный ключ. */
    public static void resetGift(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(TAG_GIVEN);
        CompoundTag keys = data.getCompound(CASE_KEYS_TAG);
        keys.putInt(STARTER_CASE_ID, 0);
        data.put(CASE_KEYS_TAG, keys);
    }
}
