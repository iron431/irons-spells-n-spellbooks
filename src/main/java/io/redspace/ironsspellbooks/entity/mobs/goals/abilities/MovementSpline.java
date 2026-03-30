package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public record MovementSpline(NavigableMap<Integer, Vec3> keyframes) {

    public Vec3 getInterpolatedPosition(int tick) {
        if (keyframes.isEmpty()) {
            return Vec3.ZERO;
        }
        Vec3 exact = keyframes.get(tick);
        if (exact != null) {
            return exact;
        }

        var lowerEntry = keyframes.floorEntry(tick);
        var higherEntry = keyframes.ceilingEntry(tick);

        // clamp outside bounds. assume both cannot be null at once
        if (lowerEntry == null) {
            return higherEntry.getValue();
        }
        if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        int lowerTick = lowerEntry.getKey();
        int higherTick = higherEntry.getKey();

        float f = (tick - lowerTick) / (float) (higherTick - lowerTick);

        Vec3 p1 = lowerEntry.getValue();
        Vec3 p2 = higherEntry.getValue();

        // Neighboring control points
        Vec3 p0 = getPrevious(lowerEntry);
        Vec3 p3 = getNext(higherEntry);

        return catmullRom(p0, p1, p2, p3, f);
    }

    private Vec3 getPrevious(Map.Entry<Integer, Vec3> entry) {
        var prev = keyframes.lowerEntry(entry.getKey());
        return prev != null ? prev.getValue() : entry.getValue();
    }

    private Vec3 getNext(Map.Entry<Integer, Vec3> entry) {
        var next = keyframes.higherEntry(entry.getKey());
        return next != null ? next.getValue() : entry.getValue();
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        Vec3 term1 = p1.scale(2.0f);
        Vec3 term2 = p2.subtract(p0).scale(t);
        Vec3 term3 = p0.scale(2.0f)
                .subtract(p1.scale(5.0f))
                .add(p2.scale(4.0f))
                .subtract(p3)
                .scale(t2);
        Vec3 term4 = p3
                .subtract(p2.scale(3.0f))
                .add(p1.scale(3.0f))
                .subtract(p0)
                .scale(t3);

        return term1
                .add(term2)
                .add(term3)
                .add(term4)
                .scale(0.5f);
    }

    public static Map<String, MovementSpline> parseSplinesFromAnimationFile(ResourceLocation filePointer) {
        try {
            //todo: permanently cache/rasterize these, or use approach that is not bound to the client instance (possible? not sure if assets exist serverside)
            JsonElement reader = JsonParser.parseReader(new JsonReader(Minecraft.getInstance().getResourceManager().openAsReader(filePointer)));
            JsonObject json = reader.getAsJsonObject();
            JsonObject animations = json.getAsJsonObject("animations");
            Map<String, MovementSpline> splines = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
                try {
                    NavigableMap<Integer, Vec3> spline = new TreeMap<>();
                    String animationName = entry.getKey();
                    JsonObject animation = entry.getValue().getAsJsonObject();
                    JsonObject boneKeyframes = animation.getAsJsonObject("bones");
                    for (Map.Entry<String, JsonElement> keyframeSet : boneKeyframes.entrySet()) {
                        String boneName = keyframeSet.getKey();
                        if (!boneName.equals("root")) {
                            continue;
                        }
                        JsonObject positionKeyframes = keyframeSet.getValue().getAsJsonObject().getAsJsonObject("position");
                        for (Map.Entry<String, JsonElement> keyframe : positionKeyframes.entrySet()) {
                            String timestampString = keyframe.getKey();
                            int timestamp;
                            try {
                                timestamp = (int) (Double.parseDouble(timestampString) * 20);
                            } catch (Exception ignored) {
                                continue;
                            }
                            JsonElement keyframeValue = keyframe.getValue();
                            JsonArray vector;
                            if (keyframeValue.isJsonObject() && keyframeValue.getAsJsonObject().has("post")) {
                                vector = keyframeValue.getAsJsonObject().getAsJsonObject("post").get("vector").getAsJsonArray();
                            } else if (keyframeValue.isJsonObject() && keyframeValue.getAsJsonObject().has("vector")) {
                                vector = keyframeValue.getAsJsonObject().get("vector").getAsJsonArray();
                            } else {
                                vector = keyframeValue.getAsJsonArray();
                            }
                            if (vector.size() != 3) {
                                throw new RuntimeException();
                            }
                            float[] f = new float[3];
                            for (int i = 0; i < 3; i++) {
                                f[i] = vector.get(i).getAsFloat() / 16f;
                                if (i == 2) {
                                    // negative z is forward
                                    f[i] = -f[i];
                                }
                            }
                            spline.put(timestamp, new Vec3(f[0], f[1], f[2]));
                        }
                        // only one root
                        break;
                    }
                    splines.put(animationName, new MovementSpline(spline));
                } catch (Exception e) {
                    IronsSpellbooks.LOGGER.error("Failed to parsed animation {}: {}", entry.getKey(), e.getMessage());
                }
            }
            return splines;
        } catch (Exception e) {
            return Map.of();
        }
    }
}