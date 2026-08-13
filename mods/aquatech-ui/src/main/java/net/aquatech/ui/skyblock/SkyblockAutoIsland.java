package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

/**
 * Soft-depends on SkyblockBuilder: first login without a team gets a personal ocean raft.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkyblockAutoIsland {
    private static final String ALREADY_TAG = AquaTechUI.MOD_ID + ":sb_island_tried";

    private SkyblockAutoIsland() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ModList.get().isLoaded("skyblockbuilder")) {
            return;
        }
        // Next tick: overworld + SB saved data must exist.
        player.getServer().execute(() -> tryAssignIsland(player));
    }

    private static void tryAssignIsland(ServerPlayer player) {
        if (!player.isAlive() || player.hasDisconnected()) {
            return;
        }
        try {
            Class<?> dataClass = Class.forName("de.melanx.skyblockbuilder.data.SkyblockSavedData");
            Method get = dataClass.getMethod("get", net.minecraft.server.level.ServerLevel.class);
            Object data = get.invoke(null, player.serverLevel());
            if (data == null) {
                return;
            }

            Method hasTeam = dataClass.getMethod("hasPlayerTeam", Player.class);
            if (Boolean.TRUE.equals(hasTeam.invoke(data, player))) {
                return;
            }

            String teamName = uniqueTeamName(player, dataClass, data);
            Method createAndJoin = findCreateAndJoin(dataClass);
            Object team;
            if (createAndJoin != null) {
                team = createAndJoin.invoke(data, teamName, player);
            } else {
                Method createTeam = dataClass.getMethod("createTeam", String.class);
                team = createTeam.invoke(data, teamName);
                if (team == null) {
                    AquaTechUI.LOGGER.warn("SkyblockBuilder createTeam returned null for {}", teamName);
                    return;
                }
                Method add = dataClass.getMethod("addPlayerToTeam", String.class, Player.class);
                add.invoke(data, teamName, player);
                Method teleport = dataClass.getMethod("teleportToIsland",
                        Class.forName("de.melanx.skyblockbuilder.data.Team"),
                        ServerPlayer.class);
                teleport.invoke(data, team, player);
            }

            if (team != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§b⚓ Личный плот готов. Ныряй вниз — глубина и давление работают от Y190."));
                AquaTechUI.LOGGER.info("Assigned Skyblock island '{}' to {}", teamName, player.getGameProfile().getName());
            }
        } catch (Throwable t) {
            if (!player.getPersistentData().getBoolean(ALREADY_TAG)) {
                player.getPersistentData().putBoolean(ALREADY_TAG, true);
                // Fallback: let the player use /skyblock create
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§eСоздай плот: §f/skyblock create <имя>"));
                AquaTechUI.LOGGER.warn("Auto island failed for {}: {}", player.getGameProfile().getName(), t.toString());
            }
        }
    }

    private static Method findCreateAndJoin(Class<?> dataClass) {
        for (Method m : dataClass.getMethods()) {
            if (!"createTeamAndJoin".equals(m.getName())) {
                continue;
            }
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && Player.class.isAssignableFrom(p[1])) {
                return m;
            }
        }
        return null;
    }

    private static String uniqueTeamName(ServerPlayer player, Class<?> dataClass, Object data) throws Exception {
        Method exists = dataClass.getMethod("teamExists", String.class);
        String base = sanitize(player.getGameProfile().getName());
        if (base.isEmpty()) {
            base = "raft";
        }
        if (base.length() > 16) {
            base = base.substring(0, 16);
        }
        String name = base;
        int i = 0;
        while (Boolean.TRUE.equals(exists.invoke(data, name)) && i < 50) {
            i++;
            String suffix = String.valueOf(i);
            name = base.substring(0, Math.min(base.length(), 16 - suffix.length())) + suffix;
        }
        if (Boolean.TRUE.equals(exists.invoke(data, name))) {
            UUID id = player.getUUID();
            name = "r" + id.toString().replace("-", "").substring(0, 8);
        }
        return name;
    }

    private static String sanitize(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        return s;
    }
}
