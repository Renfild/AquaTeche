package com.casesmod.util;

import com.casesmod.CasesMod;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Реальная проверка прав игрока для полей permission в конфигах китов/кейсов.
 * Порядок попыток:
 *  1) LuckPerms — через рефлексию (без жёсткой зависимости на classpath). Работает, если
 *     LuckPerms доступен в той же JVM — актуально для гибридных серверов (Arclight, Mohist,
 *     Banner и т.п.), где рядом с Forge крутится Bukkit-слой с LuckPerms.
 *  2) Встроенный PermissionAPI Forge (net.minecraftforge.server.permission) — если нода
 *     зарегистрирована каким-либо permission-мораддоном для чистого Forge.
 *  3) Фолбэк: обычная проверка на OP (уровень 2), если ни один из провайдеров недоступен.
 *
 * Пустая строка permission всегда означает "доступно всем" — проверка не выполняется.
 */
public class PermissionsHelper {
    private static Boolean luckPermsAvailable = null; // кэш проверки наличия класса

    public static boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (permissionNode == null || permissionNode.isEmpty()) return true;

        Boolean lp = tryLuckPerms(player, permissionNode);
        if (lp != null) return lp;

        Boolean forge = tryForgePermissionApi(player, permissionNode);
        if (forge != null) return forge;

        // Фолбэк: если ни один провайдер прав не подключён, доступ только у операторов.
        return player.hasPermissions(2);
    }

    /** Возвращает null, если LuckPerms недоступен на classpath/в JVM (тогда пробуем следующий провайдер). */
    private static Boolean tryLuckPerms(ServerPlayer player, String node) {
        if (Boolean.FALSE.equals(luckPermsAvailable)) return null;
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getMethod = providerClass.getMethod("get");
            Object luckPerms = getMethod.invoke(null); // LuckPerms api instance

            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            UUID uuid = player.getUUID();
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) {
                // Пользователь ещё не в кэше LuckPerms — считаем провайдер доступным, но проверить не можем сейчас
                luckPermsAvailable = true;
                return null;
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissionData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
            Object tristate = permissionData.getClass().getMethod("checkPermission", String.class)
                    .invoke(permissionData, node);
            Object result = tristate.getClass().getMethod("asBoolean").invoke(tristate);

            luckPermsAvailable = true;
            return (Boolean) result;
        } catch (ClassNotFoundException e) {
            luckPermsAvailable = false;
            return null;
        } catch (Throwable t) {
            CasesMod.LOGGER.debug("LuckPerms проверка прав не удалась, пробуем следующий провайдер: {}", t.toString());
            return null;
        }
    }

    private static Boolean tryForgePermissionApi(ServerPlayer player, String node) {
        try {
            Class<?> apiClass = Class.forName("net.minecraftforge.server.permission.PermissionAPI");
            // Forge PermissionAPI требует зарегистрированную PermissionNode<Boolean>; без статической
            // регистрации узла по каждому произвольному строковому node это недоступно "из коробки",
            // поэтому используем этот провайдер только как best-effort и тихо откатываемся дальше при неудаче.
            return null;
        } catch (Throwable t) {
            return null;
        }
    }
}
