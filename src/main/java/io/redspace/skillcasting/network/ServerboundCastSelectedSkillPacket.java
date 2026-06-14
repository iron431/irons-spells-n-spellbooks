package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundCastSelectedSkillPacket implements CustomPacketPayload {
    public static final Type<ServerboundCastSelectedSkillPacket> TYPE = new Type<>(Skillcasting.id("cast_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCastSelectedSkillPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundCastSelectedSkillPacket::write, ServerboundCastSelectedSkillPacket::new);

    public ServerboundCastSelectedSkillPacket() {
    }

    public ServerboundCastSelectedSkillPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public static void handle(ServerboundCastSelectedSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillcastingManager.attemptInitiateFromSelection(CasterRef.entity(player));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
