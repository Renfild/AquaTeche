package store.aquateche.aqualumen.common.service;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import store.aquateche.aqualumen.AquaLumenUI;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mohist /pay goes through Bukkit Essentials. That plugin can credit a display-name
 * account while AquaTech hub reads Vault/scoreboard — buyer is charged, seller F4 stays
 * empty. We take {@code /pay <nick> <amount>} first and move coins via {@link HubEconomy}.
 */
public final class PayCommandHook {

    private static final Pattern PAY = Pattern.compile(
            "^/?(?:essentials:)?pay\\s+(\\S{2,16})\\s+([\\d\\s.,]+)\\s*$",
            Pattern.CASE_INSENSITIVE);
    private static final AtomicBoolean BUKKIT = new AtomicBoolean();

    private PayCommandHook() {
    }

    public static void tryRegisterBukkit() {
        if (BUKKIT.get()) {
            return;
        }
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkit.getMethod("getPluginManager").invoke(null);
            Object plugin = pluginManager.getClass().getMethod("getPlugin", String.class)
                    .invoke(pluginManager, "Essentials");
            if (plugin == null) {
                Object[] plugins = (Object[]) pluginManager.getClass().getMethod("getPlugins").invoke(pluginManager);
                if (plugins != null && plugins.length > 0) {
                    plugin = plugins[0];
                }
            }
            if (plugin == null) {
                return;
            }
            ClassLoader loader = pluginManager.getClass().getClassLoader();
            Class<?> listenerClass = Class.forName("org.bukkit.event.Listener");
            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerCommandPreprocessEvent");
            Class<?> priorityClass = Class.forName("org.bukkit.event.EventPriority");
            Class<?> executorClass = Class.forName("org.bukkit.plugin.EventExecutor");
            Class<?> pluginClass = Class.forName("org.bukkit.plugin.Plugin");
            Object listener = Proxy.newProxyInstance(loader, new Class<?>[]{listenerClass}, (p, m, a) ->
                    m.getDeclaringClass() == Object.class ? invokeObject(p, m, a) : null);
            Object executor = Proxy.newProxyInstance(loader, new Class<?>[]{executorClass}, executorHandler(eventClass));
            Object lowest = enumConstant(priorityClass, "LOWEST");
            Method register = pluginManager.getClass().getMethod(
                    "registerEvent",
                    Class.class, listenerClass, priorityClass, executorClass, pluginClass, boolean.class);
            register.invoke(pluginManager, eventClass, listener, lowest, executor, plugin, false);
            BUKKIT.set(true);
            AquaLumenUI.LOGGER.info("[AquaLumen] /pay hooked onto Bukkit (hub wallet)");
        } catch (Throwable t) {
            AquaLumenUI.LOGGER.debug("[AquaLumen] Bukkit /pay hook skipped: {}", t.toString());
        }
    }

    public static void onForgeCommand(CommandEvent event) {
        if (BUKKIT.get() || event.isCanceled()) {
            return;
        }
        String raw = event.getParseResults().getReader().getString();
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception ignored) {
            return;
        }
        if (tryHandle(player, raw)) {
            event.setCanceled(true);
        }
    }

    static boolean tryHandle(ServerPlayer player, String raw) {
        if (player == null || raw == null) {
            return false;
        }
        String line = raw.trim();
        if (line.startsWith("/")) {
            line = line.substring(1);
        }
        Matcher match = PAY.matcher(line);
        if (!match.matches()) {
            return false;
        }
        String nick = match.group(1);
        long amount = parseAmount(match.group(2));
        if (amount <= 0L) {
            player.sendSystemMessage(Component.literal("§eСумма должна быть больше нуля."));
            return true;
        }
        HubEconomy.transferCoins(player, nick, amount);
        return true;
    }

    static long parseAmount(String raw) {
        if (raw == null) {
            return -1L;
        }
        String s = raw.trim().replace("\u00a0", "").replace(" ", "");
        if (s.matches("\\d{1,3}(,\\d{3})+(\\.\\d+)?")) {
            s = s.replace(",", "");
        } else if (s.matches("\\d{1,3}(\\.\\d{3})+(,\\d+)?")) {
            s = s.replace(".", "").replace(',', '.');
        } else {
            s = s.replace(",", "");
        }
        int dot = s.indexOf('.');
        if (dot >= 0) {
            s = s.substring(0, dot);
        }
        if (s.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static InvocationHandler executorHandler(Class<?> eventClass) {
        return (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObject(proxy, method, args);
            }
            if (!"execute".equals(method.getName()) || args == null || args.length < 2) {
                return null;
            }
            Object event = args[1];
            if (!eventClass.isInstance(event)) {
                return null;
            }
            handleBukkit(event);
            return null;
        };
    }

    private static void handleBukkit(Object event) {
        try {
            String message = String.valueOf(event.getClass().getMethod("getMessage").invoke(event));
            Matcher match = PAY.matcher(message.trim());
            if (!match.matches()) {
                return;
            }
            event.getClass().getMethod("setCancelled", boolean.class).invoke(event, true);
            Object bukkitPlayer = event.getClass().getMethod("getPlayer").invoke(event);
            UUID id = (UUID) bukkitPlayer.getClass().getMethod("getUniqueId").invoke(bukkitPlayer);
            String nick = match.group(1);
            long amount = parseAmount(match.group(2));
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player == null) {
                    return;
                }
                if (amount <= 0L) {
                    player.sendSystemMessage(Component.literal("§eСумма должна быть больше нуля."));
                    return;
                }
                HubEconomy.transferCoins(player, nick, amount);
            });
        } catch (Throwable t) {
            AquaLumenUI.LOGGER.debug("[AquaLumen] /pay Bukkit handle: {}", t.toString());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> type, String name) {
        return Enum.valueOf((Class) type, name);
    }

    private static Object invokeObject(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "PayCommandHook";
            default -> null;
        };
    }
}
