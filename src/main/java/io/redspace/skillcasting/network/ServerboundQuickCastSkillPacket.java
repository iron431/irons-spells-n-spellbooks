package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundQuickCastSkillPacket(int slot) implements CustomPacketPayload {
    public static final Type<ServerboundQuickCastSkillPacket> TYPE = new Type<>(Skillcasting.id("quick_cast_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundQuickCastSkillPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.slot),
            buf -> new ServerboundQuickCastSkillPacket(buf.readVarInt()));

    public static void handle(ServerboundQuickCastSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillcastingManager.attemptInitiateFromQuickCastSlot(CasterRef.entity(player), packet.slot);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
