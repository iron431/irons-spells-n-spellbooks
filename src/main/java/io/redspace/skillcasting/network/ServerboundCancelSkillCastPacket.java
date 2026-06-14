package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerboundCancelSkillCastPacket implements CustomPacketPayload {
    public static final Type<ServerboundCancelSkillCastPacket> TYPE = new Type<>(Skillcasting.id("cancel_skill_cast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCancelSkillCastPacket> STREAM_CODEC = StreamCodec.of((buf, packet) -> {
    }, buf -> new ServerboundCancelSkillCastPacket());

    public static void handle(ServerboundCancelSkillCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.INTERRUPTED);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
