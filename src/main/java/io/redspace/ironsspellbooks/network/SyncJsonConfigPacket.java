package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

public class SyncJsonConfigPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncJsonConfigPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJsonConfigPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncJsonConfigPacket::toBytes, SyncJsonConfigPacket::new);

    final int size;
    final byte[] bytes;

    public SyncJsonConfigPacket(byte[] bytes) throws IOException {
        this.size = bytes.length;
        this.bytes = bytes;
    }

    public SyncJsonConfigPacket(FriendlyByteBuf buf) {
        this.size = buf.readInt();
        this.bytes = new byte[size];
        buf.readBytes(this.bytes, 0, this.size);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(size);
        buf.writeBytes(bytes);
    }

    public static void handle(SyncJsonConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IronsSpellbooks.CONFIG_MANAGER.buildConfigManager(packet.bytes);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
