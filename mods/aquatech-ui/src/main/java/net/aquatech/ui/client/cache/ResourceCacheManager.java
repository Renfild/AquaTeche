package net.aquatech.ui.client.cache;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Async Local Resource & Personalization Cache Manager for AquaTech (Forge 1.20.1).
 * Downloads remote textures (cloaks, banners, web icons) in the background,
 * caches them locally on disk, and registers dynamic OpenGL textures.
 */
public final class ResourceCacheManager {

    private static final ResourceCacheManager INSTANCE = new ResourceCacheManager();
    private static final Path CACHE_DIR = Path.of(System.getProperty("user.home"), ".aquatech", "cache", "textures");

    private final HttpClient httpClient;
    private final Map<String, ResourceLocation> loadedTextures = new ConcurrentHashMap<>();

    private ResourceCacheManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        try {
            Files.createDirectories(CACHE_DIR);
        } catch (Exception e) {
            net.aquatech.ui.AquaTechUI.LOGGER.warn("[ResourceCache] Cannot create cache dir {}: {}", CACHE_DIR, e.getMessage());
        }
    }

    public static ResourceCacheManager getInstance() {
        return INSTANCE;
    }

    // Checks memory cache, then disk, then fetches from URL.
    public CompletableFuture<ResourceLocation> getOrFetchTexture(String resourceKey, String imageUrl) {
        if (loadedTextures.containsKey(resourceKey)) {
            return CompletableFuture.completedFuture(loadedTextures.get(resourceKey));
        }

        Path localFile = CACHE_DIR.resolve(resourceKey + ".png");
        if (Files.exists(localFile)) {
            return loadTextureFromPath(resourceKey, localFile);
        }

        return downloadAndCache(resourceKey, imageUrl, localFile);
    }

    private CompletableFuture<ResourceLocation> downloadAndCache(String key, String url, Path localPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        try (InputStream in = response.body()) {
                            byte[] bytes = in.readAllBytes();
                            Files.write(localPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            return loadTextureFromPath(key, localPath);
                        } catch (Exception e) {
                            return CompletableFuture.failedFuture(e);
                        }
                    }
                    return CompletableFuture.failedFuture(new RuntimeException("HTTP " + response.statusCode()));
                });
    }

    private CompletableFuture<ResourceLocation> loadTextureFromPath(String key, Path localPath) {
        CompletableFuture<ResourceLocation> future = new CompletableFuture<>();
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            try (InputStream in = Files.newInputStream(localPath)) {
                NativeImage nativeImg = NativeImage.read(in);
                DynamicTexture texture = new DynamicTexture(nativeImg);
                ResourceLocation loc = mc.getTextureManager().register("aquatech_cache_" + key, texture);

                loadedTextures.put(key, loc);
                future.complete(loc);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
