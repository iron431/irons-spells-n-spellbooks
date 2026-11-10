package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.lifecycle.VirtualCasterManager;
import io.redspace.skillcasting.util.StreamCodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record VirtualCasterAddPacket(CasterId.Virtual id, Vec3 position, Vec3 forward, int ownerEntityId) implements CustomPacketPayload {

    public static final Type<VirtualCasterAddPacket> TYPE = new Type<>(Skillcasting.id("virtual_caster_add"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VirtualCasterAddPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.Virtual.STREAM_CODEC, VirtualCasterAddPacket::id,
            StreamCodecUtils.VEC3, VirtualCasterAddPacket::position,
            StreamCodecUtils.VEC3, VirtualCasterAddPacket::forward,
            ByteBufCodecs.INT, VirtualCasterAddPacket::ownerEntityId,
            VirtualCasterAddPacket::new);

    public static void handle(VirtualCasterAddPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> VirtualCasterManager.addClient(context.player().level(), packet.id, packet.position, packet.forward, packet.ownerEntityId));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
