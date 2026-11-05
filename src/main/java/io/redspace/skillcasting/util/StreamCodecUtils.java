package io.redspace.skillcasting.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class StreamCodecUtils {

    public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new);

    public static final StreamCodec<ByteBuf, Vec2> VEC2 = StreamCodec.composite(
            ByteBufCodecs.FLOAT, v -> v.x,
            ByteBufCodecs.FLOAT, v -> v.y,
            Vec2::new);
}
