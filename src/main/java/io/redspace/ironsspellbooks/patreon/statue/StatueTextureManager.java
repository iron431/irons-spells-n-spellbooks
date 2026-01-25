package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.platform.NativeImage;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StatueTextureManager {

    public static final UUID TEST_UUID = uuidFromUndashed("c3adad79e88a4f15bd61c58766d725e9");
    public static final UUID TEST_UUID2 = uuidFromUndashed("afb939b1f2684ebcb1f1261fad41bc33");
    public static final UUID TEST_UUID3 = uuidFromUndashed("93b459bece4f4700b457c1aa91b3b687");
    private static final ConcurrentHashMap<UUID, StatueTextureHolder> TEXTURES = new ConcurrentHashMap<>();
    public static final StatueTextureHolder NULL = new StatueTextureHolder(PatreonPermissions.None, IronsSpellbooks.id(""), false);

    public static void _debugClear(){
        TEXTURES.clear();
    }
    static {
        //TODO: remove after testing
        createTexture(TEST_UUID);
        createTexture(TEST_UUID2);
        createTexture(TEST_UUID3);
    }

    public static UUID uuidFromUndashed(String s) {
        if (s == null || s.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string");
        }

        String dashed = s.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );

        return UUID.fromString(dashed);
    }

    public static @Nullable File export(String name, NativeImage image) {
        try {
            if (!name.endsWith(".png")) {
                name += ".png";
            }
            Path path = Path.of("screenshots/irons_spellbooks").resolve(name);
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
            }
            File file = path.toFile();
            image.writeToFile(file);
            return file;
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.debug(e.getMessage());
            return null;
        }

    }

    public static ResourceLocation resourceLocationFromUuid(UUID uuid) {
        return IronsSpellbooks.id(uuid.toString());
    }

    //    public static @Nullable StatueTextureHolder getTexture(UUID uuid) {
//        return TEXTURES.get(uuid);
//    }
    public static StatueTextureHolder lookupUUID(@NotNull UUID playerUuid) {
        if (TEXTURES.containsKey(playerUuid)) {
            return TEXTURES.get(playerUuid);
        } else {
            TEXTURES.put(playerUuid, NULL);
            createTextureAsync(playerUuid);
        }
        return NULL;
    }

    private static void createTextureAsync(UUID playerUuid) {
        CompletableFuture.runAsync(() -> {
            createTexture(playerUuid);
        }, Util.backgroundExecutor());
    }

    private static void createTexture(UUID playerUuid) {
        var sessionService = Minecraft.getInstance().getMinecraftSessionService();
        ProfileResult profileResult = sessionService.fetchProfile(playerUuid, true);
        if (profileResult == null) {
            //TODO: error handling on returns?
            return;
        }
        GameProfile gameProfile = profileResult.profile();
        MinecraftProfileTextures playerTextures = sessionService.getTextures(gameProfile);
        if (playerTextures.skin() == null) {
            return;
        }
        NativeImage skinTexture = downloadSkin(gameProfile, playerTextures.skin().getUrl());
//        skinTexture = steve();

        PlayerSkin.Model modelType = PlayerSkin.Model.byName(playerTextures.skin().getMetadata("model"));
        skinTexture = transformTexture(skinTexture);
        ResourceLocation textureId = resourceLocationFromUuid(playerUuid);
        Minecraft.getInstance().getTextureManager().register(textureId, new DynamicTexture(skinTexture));
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(playerUuid);
        TEXTURES.put(playerUuid, new StatueTextureHolder(permissions, textureId, modelType == PlayerSkin.Model.SLIM));
    }

    public static NativeImage steve() {
        try {
            return NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png")).get().open());
        } catch (Exception ignored) {
            throw new RuntimeException();
        }
    }

    private static NativeImage normalizeValues(NativeImage texture, float fMin, float fMax) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        NativeImage textureOut = texture.mappedCopy(i -> i);
        for (int color : textureOut.getPixelsRGBA()) {
            if (color == 0) {
                continue;
            }
            int v = Color.rgba(color).value();
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }
        if (max <= min) {
            return textureOut;
        }

        for (int x = 0; x < textureOut.getWidth(); x++) {
            for (int y = 0; y < textureOut.getHeight(); y++) {
                Color color = Color.rgba(textureOut.getPixelRGBA(x, y));
                if (color.empty()) {
                    continue;
                }
                Color normalized = new Color(
                        (int) Math.clamp(Mth.lerp((color.red() - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255),
                        (int) Math.clamp(Mth.lerp((color.green() - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255),
                        (int) Math.clamp(Mth.lerp((color.blue() - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255)
                );
                textureOut.setPixelRGBA(x, y, normalized.toRgba());
            }
        }
        return textureOut;
    }

    private static Color stonePalette(Color color, int min, int max) {
        int v = color.luminance();
        float f = (v - min) / (float) (max - min);
        // andesite palette. raw colors taken from andesite texture, scaled for additional contrast
        Color a = new Color(0x68686A).scale(0.95f);
        Color b = new Color(0xabab9a).scale(1.1f);
//        f = Mth.sin(Mth.HALF_PI * f);
//        f *= f;

        return Color.lerp(f, a, b);
    }

    public static NativeImage transformTexture(NativeImage skinTexture) {
        //todo: implement palette-izer
        skinTexture = normalizeValues(skinTexture, 0, 1);
        var pixelData = skinTexture.getPixelsRGBA();
        IntArraySet packedColors = new IntArraySet();
        for (int i : pixelData) {
            if (i != 0) {
                packedColors.add(i);
            }
        }
        if (packedColors.isEmpty()) {
            return skinTexture;
        }
        MeanShiftCluster.MeanShiftClusterResult meanShiftCluster = MeanShiftCluster.meanShiftCluster(new ArrayList<>(packedColors));
        IronsSpellbooks.LOGGER.debug("MSC cluster count : {}", meanShiftCluster.clusters().size());

        NativeImage stoneOverlay = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/stone")).contents().getOriginalImage();
        stoneOverlay = normalizeValues(stoneOverlay, 0.75f, 1f);
        for (int x = 0; x < skinTexture.getWidth(); x++) {
            for (int y = 0; y < skinTexture.getHeight(); y++) {
                int rgba = skinTexture.getPixelRGBA(x, y);
                if (rgba == 0 || false) {
                    continue;
                }
                Color replacement = meanShiftCluster.lookupTable().computeIfAbsent(rgba, Color::new);
//                replacement = new Color(rgba);
                Color color = Color.rgba(replacement.packedARGB());
                color = stonePalette(color, 0, 255);
                Color stoneSample = Color.rgba(stoneOverlay.getPixelRGBA(x % stoneOverlay.getWidth(), y % stoneOverlay.getHeight()));
                color = color.multiply(stoneSample);
                skinTexture.setPixelRGBA(x, y, color.toRgba());
            }
        }

        return skinTexture;
    }

    @Nullable
    private static NativeImage downloadSkin(GameProfile gameProfile, String skinUrl) {
        //todo: look at {@link HttpTexture:99}
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(skinUrl).openConnection();
            c.setConnectTimeout(6000);
            c.setReadTimeout(6000);
            try (InputStream in = c.getInputStream()) {
                return NativeImage.read(in);
            }
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Failed to download skin for player {} at url {}", gameProfile.getName(), skinUrl);
            return null;
        }
    }

}
