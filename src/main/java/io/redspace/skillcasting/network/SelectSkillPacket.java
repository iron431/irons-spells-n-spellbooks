package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.selection.SkillSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serverbound selection change. The client applies the selection locally before sending; the server
 * validates preconditions and persists the selection without an acknowledgement sync.
 */
public final class SelectSkillPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectSkillPacket> TYPE =
            new CustomPacketPayload.Type<>(Skillcasting.id("select_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSkillPacket> STREAM_CODEC =
            SkillSelection.STREAM_CODEC.map(SelectSkillPacket::new, SelectSkillPacket::selection);

    private final SkillSelection selection;

    public SelectSkillPacket(SkillSelection selection) {
        this.selection = selection;
    }

    public SkillSelection selection() {
        return selection;
    }

    /** Client convenience for HUD selection changes. */
    public static void send(SkillSelection selection) {
        if (Minecraft.getInstance().hasSingleplayerServer() || Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new SelectSkillPacket(selection));
        }
    }

    public static void handle(SelectSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillcastingManager.select(player, packet.selection);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
