package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SyncSelectionPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSelectionPacket> TYPE =
            new CustomPacketPayload.Type<>(Skillcasting.id("sync_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSelectionPacket> STREAM_CODEC =
            SkillSelectionManager.STREAM_CODEC.map(SyncSelectionPacket::new, SyncSelectionPacket::manager);

    private final SkillSelectionManager manager;

    public SyncSelectionPacket(SkillSelectionManager manager) {
        this.manager = manager;
    }

    public SkillSelectionManager manager() {
        return manager;
    }

    public static void sendToPlayer(ServerPlayer player, SkillSelectionManager manager) {
        PacketDistributor.sendToPlayer(player, new SyncSelectionPacket(manager));
    }

    public static void handle(SyncSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            SkillcastingData.get(context.player()).selectionManager().replaceFrom(packet.manager);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
