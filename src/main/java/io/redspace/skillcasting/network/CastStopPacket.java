package io.redspace.skillcasting.network;

import io.netty.buffer.ByteBuf;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.client.ClientInputEvents;
import io.redspace.skillcasting.client.ClientSkillCastHelper;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CastStopPacket(CasterId casterId, CastEndReason castEndReason) implements CustomPacketPayload {

    public static final Type<CastStopPacket> TYPE = new Type<>(Skillcasting.id("cast_stop_packet"));

    private static final StreamCodec<ByteBuf, CastEndReason> CAST_END_REASON = ByteBufCodecs.idMapper(i -> CastEndReason.values()[i], CastEndReason::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, CastStopPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, CastStopPacket::casterId,
            CAST_END_REASON, CastStopPacket::castEndReason,
            CastStopPacket::new);

    public static void handle(CastStopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            ActiveCast activeCast = data.getActiveCast();
            if (activeCast == null) {
                return;
            }
            data.endActiveCast();
            activeCast.context().skill().value().onClientCastComplete(activeCast.context(), packet.castEndReason);

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
