package com.casesmod.client.gui;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Sends aquatech_ui C2SOpenContainerPacket when the mod is present. */
public final class AquaContainerOverlay {

    private AquaContainerOverlay() {
    }

    public static void openVault() {
        request("STORAGE_VAULT");
    }

    public static void openLimiters() {
        request("BLOCK_LIMITERS");
    }

    public static void openLook() {
        request("PERSONALIZATION");
    }

    private static void request(String typeName) {
        try {
            Class<?> pktCls = Class.forName("net.aquatech.ui.network.packet.C2SOpenContainerPacket");
            Class<?> typeCls = Class.forName("net.aquatech.ui.network.packet.C2SOpenContainerPacket$ContainerType");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object type = Enum.valueOf((Class<Enum>) typeCls, typeName);
            Constructor<?> ctor = pktCls.getConstructor(typeCls);
            Object packet = ctor.newInstance(type);
            Class<?> nh = Class.forName("net.aquatech.ui.network.NetworkHandler");
            Field channelField = nh.getField("CHANNEL");
            Object channel = channelField.get(null);
            Method sendToServer = channel.getClass().getMethod("sendToServer", Object.class);
            sendToServer.invoke(channel, packet);
        } catch (Throwable ignored) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.connection.sendUnsignedCommand("aquatech " + commandFor(typeName));
            }
        }
    }

    private static String commandFor(String typeName) {
        return switch (typeName) {
            case "STORAGE_VAULT" -> "vault";
            case "BLOCK_LIMITERS" -> "limiters";
            default -> "look";
        };
    }
}
