package io.redspace.ironsspellbooks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class Vec3StreamCodec {
    public static final StreamCodec<FriendlyByteBuf, Vec3> CODEC = StreamCodec.of(
            FriendlyByteBuf::writeVec3,
            FriendlyByteBuf::readVec3
    );
}
