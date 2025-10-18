package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CameraShakeData {

    final int duration;
    final float radius;
    int tickCount = 0;
    final int id;
    final Vec3 origin;
    final ResourceKey<Level> dimension;

    @Deprecated(forRemoval = true)
    public CameraShakeData(int duration, Vec3 origin, float radius) {
        this(null, duration, origin, radius);
        IronsSpellbooks.LOGGER.warn("Addon creating camera shake without specifying dimension! Adding to overworld.");
    }

    public CameraShakeData(@NotNull Level level, int duration, Vec3 origin, float radius) {
        this(level == null ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld")) : level.dimension(),
                CameraShakeManager.getNextId(),
                duration,
                origin,
                radius);
    }

    private CameraShakeData(ResourceKey<Level> level, int id, int duration, Vec3 origin, float radius) {
        this.dimension = level;
        this.id = id;
        this.duration = duration;
        this.origin = origin;
        this.radius = radius;
    }

    public void serializeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(duration);
        buf.writeInt(tickCount);
        buf.writeInt((int) (origin.x * 10));
        buf.writeInt((int) (origin.y * 10));
        buf.writeInt((int) (origin.z * 10));
        buf.writeInt((int) (radius * 10));
        buf.writeResourceKey(dimension);
    }

    public static CameraShakeData deserializeFromBuffer(FriendlyByteBuf buf) {
        int id = buf.readInt();
        int duration = buf.readInt();
        int tickCount = buf.readInt();
        Vec3 origin = new Vec3(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
        float radius = buf.readInt() / 10f;
        ResourceKey<Level> dimension = buf.readResourceKey(Registries.DIMENSION);
        CameraShakeData data = new CameraShakeData(dimension, id, duration, origin, radius);
        data.tickCount = tickCount;
        return data;
    }
}
