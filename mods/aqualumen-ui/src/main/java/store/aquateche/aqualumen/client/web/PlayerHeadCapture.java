package store.aquateche.aqualumen.client.web;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Grabs the local player's actual skin face (base + hat layer) as a data URL
 * so the hub avatar shows the real skin instead of a web-service default.
 */
public final class PlayerHeadCapture {

    private static volatile String cached;
    private static volatile String cachedFor;
    private static volatile long cachedAt;
    private static final java.util.Map<String, String> BY_NICK = new java.util.concurrent.ConcurrentHashMap<>();

    private PlayerHeadCapture() {
    }

    public static synchronized String dataUrl(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "";
        }
        long now = System.currentTimeMillis();
        if (cached != null && playerName.equals(cachedFor) && now - cachedAt < 60_000L) {
            return cached;
        }
        String grabbed = grab();
        if (grabbed != null && !grabbed.isBlank()) {
            cached = grabbed;
            cachedFor = playerName;
            cachedAt = now;
            return grabbed;
        }
        return cached == null ? "" : cached;
    }

    /** Face of any currently online player, cached per nick; "" when not resolvable. */
    public static String dataUrlFor(String nick) {
        if (nick == null || nick.isBlank()) {
            return "";
        }
        String hit = BY_NICK.get(nick);
        if (hit != null) {
            return hit;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return "";
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(nick);
        if (info == null || info.getSkinLocation() == null) {
            return "";
        }
        String face = grabFace(info);
        if (!face.isBlank()) {
            BY_NICK.put(nick, face);
        }
        return face;
    }

    private static String grabFace(PlayerInfo info) {
        try {
            ResourceLocation loc = info.getSkinLocation();
            for (int size : new int[]{64, 128, 512}) {
                NativeImage skin = tryDownload(loc, size);
                if (skin == null) {
                    continue;
                }
                int s = size / 64;
                int face = 8 * s;
                NativeImage head = new NativeImage(face, face, true);
                boolean anyPixel = false;
                for (int y = 0; y < face; y++) {
                    for (int x = 0; x < face; x++) {
                        int base = skin.getPixelRGBA(8 * s + x, 8 * s + y);
                        int hat = skin.getPixelRGBA(40 * s + x, 8 * s + y);
                        int a = (hat >>> 24) & 0xFF;
                        int px = a > 16 ? hat : base;
                        head.setPixelRGBA(x, y, px);
                        if (((px >>> 24) & 0xFF) > 16) {
                            anyPixel = true;
                        }
                    }
                }
                skin.close();
                if (!anyPixel) {
                    head.close();
                    continue;
                }
                File tmp = File.createTempFile("aqlumen_head", ".png");
                head.writeToFile(tmp);
                byte[] png = Files.readAllBytes(tmp.toPath());
                Files.deleteIfExists(tmp.toPath());
                head.close();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
            }
            return "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String grab() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) {
                return "";
            }
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info == null || info.getSkinLocation() == null) {
                return "";
            }
            ResourceLocation loc = info.getSkinLocation();
            for (int size : new int[]{64, 128, 512}) {
                NativeImage skin = tryDownload(loc, size);
                if (skin == null) {
                    continue;
                }
                int s = size / 64;
                int face = 8 * s;
                NativeImage head = new NativeImage(face, face, true);
                boolean anyPixel = false;
                for (int y = 0; y < face; y++) {
                    for (int x = 0; x < face; x++) {
                        int base = skin.getPixelRGBA(8 * s + x, 8 * s + y);
                        int hat = skin.getPixelRGBA(40 * s + x, 8 * s + y);
                        int a = (hat >>> 24) & 0xFF;
                        int px = a > 16 ? hat : base;
                        head.setPixelRGBA(x, y, px);
                        if (((px >>> 24) & 0xFF) > 16) {
                            anyPixel = true;
                        }
                    }
                }
                skin.close();
                if (!anyPixel) {
                    head.close();
                    continue;
                }
                File tmp = File.createTempFile("aqlumen_head", ".png");
                head.writeToFile(tmp);
                byte[] png = Files.readAllBytes(tmp.toPath());
                Files.deleteIfExists(tmp.toPath());
                head.close();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
            }
            return "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static NativeImage tryDownload(ResourceLocation loc, int size) {
        try {
            var tm = Minecraft.getInstance().getTextureManager();
            var texture = tm.getTexture(loc);
            if (texture == null) {
                return null;
            }
            RenderSystem.bindTexture(texture.getId());
            NativeImage img = new NativeImage(size, size, false);
            img.downloadTexture(0, false);
            return img;
        } catch (Throwable t) {
            return null;
        }
    }
}
