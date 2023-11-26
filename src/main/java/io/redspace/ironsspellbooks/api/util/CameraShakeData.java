package io.redspace.ironsspellbooks.api.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CameraShakeData {

    final int duration;
    final float radius;
    int tickCount;
    final Vec3 origin;

    public CameraShakeData(int duration, Vec3 origin, float radius) {
        this.duration = duration;
        this.origin = origin;
        this.radius = radius;
    }

    public void serializeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(duration);
        buf.writeInt(tickCount);
        buf.writeInt((int) (origin.x * 10));
        buf.writeInt((int) (origin.y * 10));
        buf.writeInt((int) (origin.z * 10));
        buf.writeInt((int) (radius * 10));
    }

    public static CameraShakeData deserializeFromBuffer(FriendlyByteBuf buf) {
        int duration = buf.readInt();
        int tickCount = buf.readInt();
        Vec3 origin = new Vec3(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
        float radius = buf.readInt() / 10f;
        CameraShakeData data = new CameraShakeData(duration, origin, radius);
        data.tickCount = tickCount;
        return data;
    }
}
