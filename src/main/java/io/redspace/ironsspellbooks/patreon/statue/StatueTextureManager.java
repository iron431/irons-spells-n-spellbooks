package io.redspace.ironsspellbooks.patreon.statue;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.platform.NativeImage;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class StatueTextureManager {

    public static final UUID TEST_UUID = uuidFromUndashed("c3adad79e88a4f15bd61c58766d725e9");
    public static final UUID TEST_UUID2 = uuidFromUndashed("afb939b1f2684ebcb1f1261fad41bc33");
    public static final UUID TEST_UUID3 = uuidFromUndashed("93b459bece4f4700b457c1aa91b3b687");
    private static final HashMap<UUID, StatueTextureHolder> TEXTURES = new HashMap<>();

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

    public static ResourceLocation resourceLocationFromUuid(UUID uuid) {
        return IronsSpellbooks.id(uuid.toString());
    }

    public static @Nullable StatueTextureHolder getTexture(UUID uuid) {
        return TEXTURES.get(uuid);
    }

    public static void createTexture(UUID playerUuid) {
        //todo: prefilter non-permitted
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(playerUuid);

        // todo: asynchronous handling
        var sessionService = Minecraft.getInstance().getMinecraftSessionService();
        ProfileResult profileResult = sessionService.fetchProfile(playerUuid, true);
        if (profileResult == null) {
            //TODO: error handling?
            return;
        }
        GameProfile gameProfile = profileResult.profile();
        MinecraftProfileTextures playerTextures = sessionService.getTextures(gameProfile);
        if (playerTextures.skin() == null) {
            return;
        }
        NativeImage skinTexture = downloadSkin(gameProfile, playerTextures.skin().getUrl());
        if (skinTexture == null) {
            return;
        }
        PlayerSkin.Model modelType = PlayerSkin.Model.byName(playerTextures.skin().getMetadata("model"));
        skinTexture = transformTexture(skinTexture);
        ResourceLocation textureId = resourceLocationFromUuid(playerUuid);
        Minecraft.getInstance().getTextureManager().register(textureId, new DynamicTexture(skinTexture));
        TEXTURES.put(playerUuid, new StatueTextureHolder(permissions, textureId, modelType == PlayerSkin.Model.SLIM));
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
                        (int) Math.clamp(Mth.lerp((color.red - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255),
                        (int) Math.clamp(Mth.lerp((color.green - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255),
                        (int) Math.clamp(Mth.lerp((color.blue - min) / (float) (max - min), fMin * 255, fMax * 255), 0, 255)
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
        Color a = new Color(0x68686A).scale(0.85f);
        Color b = new Color(0xabab9a).scale(1.1f);

//        f = Mth.sin(Mth.HALF_PI * f);
//        f *= f;

        return Color.lerp(f, a, b);
    }

    private static NativeImage transformTexture(NativeImage skinTexture) {
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
        MeanShiftClusterResult meanShiftCluster = meanShiftCluster(new ArrayList<>(packedColors));
        IronsSpellbooks.LOGGER.debug("MSC cluster count : {}", meanShiftCluster.clusters.size());

        NativeImage stoneOverlay = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/stone")).contents().getOriginalImage();
        stoneOverlay = normalizeValues(stoneOverlay, 0.75f, 1f);
        for (int x = 0; x < skinTexture.getWidth(); x++) {
            for (int y = 0; y < skinTexture.getHeight(); y++) {
                int rgba = skinTexture.getPixelRGBA(x, y);
                if (rgba == 0 || false) {
                    continue;
                }
                Color replacement = meanShiftCluster.lookupTable.computeIfAbsent(rgba, Color::new);
//                replacement = new Color(rgba);
                Color color = Color.rgba(replacement.packedARGB);
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

    record Color(int packedARGB, int red, int green, int blue) {
        Color(int color) {
            this(color, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        }

        Color(int r, int g, int b) {
            this((0xFF << 24) | (r << 16) | (g << 8) | b, r, g, b);
        }

        int value() {
            return Math.max(red, Math.max(green, blue));
        }

        int luminance() {
            return (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue); // Rec. 709 luminance
        }

        int alpha() {
            return packedARGB >> 24;
        }

        Color packedValue() {
            int v = value();
            return new Color(v, v, v);
        }

        boolean empty() {
            return packedARGB == 0;
        }

        Color multiply(Color other) {
            return new Color(this.red * other.red / 255, this.green * other.green / 255, this.blue * other.blue / 255);
        }

        static Color rgba(int rgba) {
            int alpha = (rgba >> 24) & 0xFF;
            int r = (rgba) & 0xFF;
            int g = (rgba >> 8) & 0xFF;
            int b = (rgba >> 16) & 0xFF;
            return new Color((alpha << 24) | (r << 16) | (g << 8) | b);
        }

        int toRgba() {
            return (alpha() << 24) | (blue << 16) | (green << 8) | red;
        }

        static Color lerp(float f, Color a, Color b) {
            return new Color(
                    (int) Math.clamp(Mth.lerp(f, a.red, b.red), 0, 255),
                    (int) Math.clamp(Mth.lerp(f, a.green, b.green), 0, 255),
                    (int) Math.clamp(Mth.lerp(f, a.blue, b.blue), 0, 255)
            );
        }

        Color scale(float scalar) {
            return new Color(
                    (int) Math.clamp(this.red * scalar, 0, 255),
                    (int) Math.clamp(this.green * scalar, 0, 255),
                    (int) Math.clamp(this.blue * scalar, 0, 255)
            );
        }
    }

    record MeanShiftClusterResult(List<Color> clusters, Map<Integer, Color> lookupTable) {
    }

    public static Vec3 rgbIntToOKLab(int rgb) {
        // Extract 8-bit components
        double r = ((rgb >> 16) & 0xFF) / 255.0;
        double g = ((rgb >> 8) & 0xFF) / 255.0;
        double b = (rgb & 0xFF) / 255.0;
        r = Math.sqrt(r);
        g = Math.sqrt(g);
        b = Math.sqrt(b);

        // sRGB → linear RGB
        r = (r <= 0.04045) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.04045) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.04045) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        // Linear RGB → LMS
        double l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b;
        double m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b;
        double s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b;

        // Nonlinear transform
        l = Math.cbrt(l);
        m = Math.cbrt(m);
        s = Math.cbrt(s);

        // LMS → OKLab
        double L = 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s;
        double A = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s;
        double B = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s;

        return new Vec3(L, A, B);
    }

    public static int oklabToRgbInt(Vec3 lab) {
        double L = lab.x;
        double A = lab.y;
        double B = lab.z;

        // OKLab → LMS
        double l = L + 0.3963377774 * A + 0.2158037573 * B;
        double m = L - 0.1055613458 * A - 0.0638541728 * B;
        double s = L - 0.0894841775 * A - 1.2914855480 * B;

        // Cube
        l = l * l * l;
        m = m * m * m;
        s = s * s * s;

        // LMS → linear RGB
        double r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s;
        double g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s;
        double b = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s;

        // Linear RGB → sRGB
        r = (r <= 0.0031308) ? 12.92 * r : 1.055 * Math.pow(r, 1.0 / 2.4) - 0.055;
        g = (g <= 0.0031308) ? 12.92 * g : 1.055 * Math.pow(g, 1.0 / 2.4) - 0.055;
        b = (b <= 0.0031308) ? 12.92 * b : 1.055 * Math.pow(b, 1.0 / 2.4) - 0.055;
        r *= r;
        g *= g;
        b *= b;

        // Clamp and pack
        int ri = (int) Math.round(Math.min(1.0, Math.max(0.0, r)) * 255);
        int gi = (int) Math.round(Math.min(1.0, Math.max(0.0, g)) * 255);
        int bi = (int) Math.round(Math.min(1.0, Math.max(0.0, b)) * 255);

        return (0xFF << 24) + (ri << 16) + (gi << 8) + bi;
    }

    private static MeanShiftClusterResult meanShiftCluster(List<Integer> colorsI) {
        double bandwidth = 10 / 255.0;         // color similarity radius
        double bandwidthSqr = bandwidth * bandwidth;
        double merge = 1 / 255.0;
        double mergeSqr = merge * merge;
        double epsilon = 0.5 / 255.0;            // convergence threshold
        int maxIterations = 50;

        //todo: fastutil
        List<Vec3> clusters = new ArrayList<>(); // oklabspace
        List<Vec3> colors = new ArrayList<>(); // oklabspace
        HashMap<Integer, Color> lookupTable = new HashMap<>();

        for (int i : colorsI) {
            colors.add(rgbIntToOKLab(i));
        }
        for (int i = 0; i < colorsI.size(); i++) {
            int colorI = colorsI.get(i);
            Vec3 currentColor = colors.get(i);

            for (int j = 0; j < maxIterations; j++) {
                int count = 0;
                Vec3 sum = Vec3.ZERO;

                for (Vec3 color : colors) {
                    if (currentColor.distanceToSqr(color) < bandwidthSqr) {
                        sum = sum.add(color);
                        count++;
                    }
                }
                if (count == 0) {
                    break;
                }
                Vec3 average = sum.scale(1.0 / count);
                if (currentColor.distanceToSqr(average) < epsilon * epsilon) {
                    break;
                }
                currentColor = average;
            }
            boolean unique = true;
            Vec3 assignment = currentColor;
            for (Vec3 existingCluster : clusters) {
                if (currentColor.distanceToSqr(existingCluster) < mergeSqr) {
                    unique = false;
                    assignment = existingCluster;
                    break;
                }
            }
            if (unique) {
                clusters.add(currentColor);
            }
            lookupTable.put(colorI, new Color(oklabToRgbInt(assignment)));
        }
        List<Color> clustersI = new ArrayList<>(clusters.size());
        for (Vec3 oklab : clusters) {
            clustersI.add(new Color(oklabToRgbInt(oklab)));
        }
        return new MeanShiftClusterResult(clustersI, lookupTable);
    }
}
