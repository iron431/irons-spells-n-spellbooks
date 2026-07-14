package io.redspace.skillcasting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class StreamCodecUtils {

    public static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT = StreamCodec.of(ByteBufCodecs.INT::encode, ByteBufCodecs.INT::decode);

    public static final StreamCodec<RegistryFriendlyByteBuf, Float> FLOAT = StreamCodec.of(ByteBufCodecs.FLOAT::encode, ByteBufCodecs.FLOAT::decode);

    public static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID = StreamCodec.of(
            (buf, uuid) -> buf.writeUUID(uuid),
            buf -> buf.readUUID());

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3 =
            StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec2> VEC2 = StreamCodec.composite(
            ByteBufCodecs.FLOAT, v -> v.x,
            ByteBufCodecs.FLOAT, v -> v.y,
            Vec2::new);
}
