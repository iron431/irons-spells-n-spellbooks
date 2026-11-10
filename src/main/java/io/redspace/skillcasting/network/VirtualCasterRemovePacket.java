package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.lifecycle.VirtualCasters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record VirtualCasterRemovePacket(CasterId.Virtual id) implements CustomPacketPayload {

    public static final Type<VirtualCasterRemovePacket> TYPE = new Type<>(Skillcasting.id("virtual_caster_remove"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VirtualCasterRemovePacket> STREAM_CODEC =
            CasterId.Virtual.STREAM_CODEC.map(VirtualCasterRemovePacket::new, VirtualCasterRemovePacket::id);

    public static void handle(VirtualCasterRemovePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> VirtualCasters.removeClient(context.player().level(), packet.id));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
