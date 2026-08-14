package net.aquatech.ui.client.cache;

import com.mojang.blaze3d.platform.NativeImage;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Downloads PNG textures (avatars, portal icons) to disk, then registers DynamicTexture on the client thread.
 */
public final class ResourceCacheManager {

    private static final ResourceCacheManager INSTANCE = new ResourceCacheManager();
    private static final Path CACHE_DIR = Path.of(System.getProperty("user.home"), ".aquatech", "cache", "textures");
    private static final int MAX_BYTES = 512 * 1024;
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "aquateche.store",
            "www.aquateche.store",
            "crafatar.com",
            "mc-heads.net"
    );

    private final HttpClient httpClient;
    private final Map<String, ResourceLocation> loadedTextures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ResourceLocation>> inflight = new ConcurrentHashMap<>();
    private final Map<String, Long> failedAtMs = new ConcurrentHashMap<>();
    private static final long FAIL_COOLDOWN_MS = 60_000L;

    private ResourceCacheManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[ResourceCache] cannot create {}: {}", CACHE_DIR, e.toString());
        }
    }

    public static ResourceCacheManager getInstance() {
        return INSTANCE;
    }

    public ResourceLocation peek(String resourceKey) {
        String key = sanitizeKey(resourceKey);
        return key == null ? null : loadedTextures.get(key);
    }

    public void prefetchPlayerAvatar(java.util.UUID uuid) {
        if (uuid == null) {
            return;
        }
        String hex = uuid.toString().replace("-", "");
        getOrFetchTexture("avatar_" + hex, "https://crafatar.com/avatars/" + hex + "?size=64&overlay=true");
    }

    public CompletableFuture<ResourceLocation> getOrFetchTexture(String resourceKey, String imageUrl) {
        String key = sanitizeKey(resourceKey);
        if (key == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("bad cache key"));
        }
        if (!isAllowedUrl(imageUrl)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("url host not allowed"));
        }
        ResourceLocation hit = loadedTextures.get(key);
        if (hit != null) {
            return CompletableFuture.completedFuture(hit);
        }
        Long failed = failedAtMs.get(key);
        if (failed != null && System.currentTimeMillis() - failed < FAIL_COOLDOWN_MS) {
            return CompletableFuture.failedFuture(new RuntimeException("cache cooldown"));
        }
        return inflight.computeIfAbsent(key, k -> {
            Path localFile = CACHE_DIR.resolve(k + ".png");
            CompletableFuture<ResourceLocation> chain;
            if (Files.exists(localFile)) {
                chain = loadTextureFromPath(k, localFile);
            } else {
                chain = downloadAndCache(k, imageUrl, localFile);
            }
            return chain.whenComplete((loc, err) -> {
                inflight.remove(k);
                if (err != null) {
                    failedAtMs.put(k, System.currentTimeMillis());
                } else {
                    failedAtMs.remove(k);
                }
            });
        });
    }

    public static boolean isAllowedUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(imageUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            return ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String sanitizeKey(String resourceKey) {
        if (resourceKey == null) {
            return null;
        }
        String key = resourceKey.trim().toLowerCase(Locale.ROOT);
        if (key.length() > 64 || !key.matches("[a-z0-9_]+")) {
            return null;
        }
        return key;
    }

    private CompletableFuture<ResourceLocation> downloadAndCache(String key, String url, Path localPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "AquaTech/1.0")
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenCompose(response -> {
                    if (response.statusCode() != 200) {
                        return CompletableFuture.failedFuture(new RuntimeException("HTTP " + response.statusCode()));
                    }
                    byte[] body = response.body();
                    if (body == null || body.length < 24 || body.length > MAX_BYTES) {
                        return CompletableFuture.failedFuture(new RuntimeException("bad png size"));
                    }
                    if (body[0] != (byte) 0x89 || body[1] != 0x50 || body[2] != 0x4E || body[3] != 0x47) {
                        return CompletableFuture.failedFuture(new RuntimeException("not a png"));
                    }
                    try {
                        Files.write(localPath, body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (Exception e) {
                        return CompletableFuture.failedFuture(e);
                    }
                    return loadTextureFromPath(key, localPath);
                });
    }

    private CompletableFuture<ResourceLocation> loadTextureFromPath(String key, Path localPath) {
        CompletableFuture<ResourceLocation> future = new CompletableFuture<>();
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            try (java.io.InputStream in = Files.newInputStream(localPath)) {
                NativeImage nativeImg = NativeImage.read(in);
                DynamicTexture texture = new DynamicTexture(nativeImg);
                ResourceLocation loc = mc.getTextureManager().register("aquatech_cache_" + key, texture);
                loadedTextures.put(key, loc);
                future.complete(loc);
            } catch (Exception e) {
                AquaTechUI.LOGGER.debug("[ResourceCache] load {}: {}", key, e.toString());
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
