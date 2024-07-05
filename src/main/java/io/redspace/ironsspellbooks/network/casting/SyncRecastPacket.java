package io.redspace.ironsspellbooks.network.ported.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncRecastPacket implements CustomPacketPayload {

    private final RecastInstance recastInstance;
    public static final CustomPacketPayload.Type<SyncRecastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_recast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecastPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncRecastPacket::write, SyncRecastPacket::new);

    public SyncRecastPacket(RecastInstance recastInstance) {
        this.recastInstance = recastInstance;
    }

    public SyncRecastPacket(FriendlyByteBuf buf) {
        recastInstance = new RecastInstance();
        recastInstance.readFromBuffer(buf);
    }

    public void write(FriendlyByteBuf buf) {
        if (recastInstance != null) {
            recastInstance.writeToBuffer(buf);
        }
    }

    public static void handle(SyncRecastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientMagicData.getRecasts().forceAddRecast(packet.recastInstance);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}