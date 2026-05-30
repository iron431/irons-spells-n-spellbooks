package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.selection.SkillSelection;
import io.redspace.skillcasting.api.selection.SkillSelectionEntry;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Clientbound snapshot of the flattened skill bar and selection pointer after equipment refresh.
 */
public final class SkillSelectionSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkillSelectionSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(Skillcasting.id("sync_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSelectionSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(SkillSelectionSyncPacket::write, SkillSelectionSyncPacket::new);

    private final List<SkillSelectionEntry> entries;
    private final int selectedIndex;
    private final String selectedSource;
    private final int lastSelectedIndex;
    private final String lastSelectedSource;

    public SkillSelectionSyncPacket(List<SkillSelectionEntry> entries, SkillSelection selection) {
        this.entries = entries;
        this.selectedIndex = selection.selectedIndex();
        this.selectedSource = selection.selectedSource();
        this.lastSelectedIndex = selection.lastSelectedIndex();
        this.lastSelectedSource = selection.lastSelectedSource();
    }

    public SkillSelectionSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new SkillSelectionEntry(buf.readResourceLocation(), buf.readVarInt(), buf.readUtf()));
        }
        selectedIndex = buf.readVarInt();
        selectedSource = buf.readUtf();
        lastSelectedIndex = buf.readVarInt();
        lastSelectedSource = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (SkillSelectionEntry entry : entries) {
            buf.writeResourceLocation(entry.skillId());
            buf.writeVarInt(entry.level());
            buf.writeUtf(entry.source());
        }
        buf.writeVarInt(selectedIndex);
        buf.writeUtf(selectedSource);
        buf.writeVarInt(lastSelectedIndex);
        buf.writeUtf(lastSelectedSource);
    }

    public static void sendToPlayer(ServerPlayer player, SkillSelectionManager manager, SkillSelection selection) {
        PacketDistributor.sendToPlayer(player, new SkillSelectionSyncPacket(new ArrayList<>(selection.entries()), selection));
    }

    public static void handle(SkillSelectionSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            var data = context.player().getData(io.redspace.skillcasting.registry.SkillcastingAttachments.SKILLCASTING_DATA.get());
            data.selection().replaceEntries(packet.entries);
            data.selection().restorePointer(
                    packet.selectedIndex,
                    packet.selectedSource,
                    packet.lastSelectedIndex,
                    packet.lastSelectedSource);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
