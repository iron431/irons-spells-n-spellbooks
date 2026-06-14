package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.client.ClientInputEvents;
import io.redspace.skillcasting.client.ClientSkillCastHelper;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CastStopPacket(CasterId casterId) implements CustomPacketPayload {

    public static final Type<CastStopPacket> TYPE = new Type<>(Skillcasting.id("cast_stop_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastStopPacket> STREAM_CODEC = CustomPacketPayload.codec(CastStopPacket::write, CastStopPacket::fromBuf);

    private static CastStopPacket fromBuf(RegistryFriendlyByteBuf buf) {
        CasterId id = CasterId.STREAM_CODEC.decode(buf);
        return new CastStopPacket(id);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
    }

    public static void handle(CastStopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            data.endActiveCast();
            var localPlayer = context.player();
            if (localPlayer != null && packet.casterId().equals(CasterRef.entity(localPlayer).id())) {
                ClientSkillCastHelper.setSuppressRightClicks(false);
                if (ClientInputEvents.isUseKeyDown()) {
                    ClientInputEvents.hasReleasedSinceCasting = false;
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
