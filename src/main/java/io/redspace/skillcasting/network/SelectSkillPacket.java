package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serverbound selection change. Server applies the index against its authoritative selection.
 */
public final class SelectSkillPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectSkillPacket> TYPE =
            new CustomPacketPayload.Type<>(Skillcasting.id("select_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSkillPacket> STREAM_CODEC =
            CustomPacketPayload.codec(SelectSkillPacket::write, SelectSkillPacket::new);

    private final int index;

    public SelectSkillPacket(int index) {
        this.index = index;
    }

    public SelectSkillPacket(FriendlyByteBuf buf) {
        this.index = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
    }

    /** Client convenience for HUD wheel selection. */
    public static void send(int index) {
        if (Minecraft.getInstance().hasSingleplayerServer() || Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new SelectSkillPacket(index));
        }
    }

    public static void handle(SelectSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillcastingManager.select(player, packet.index);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
