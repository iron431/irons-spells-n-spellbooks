package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

/*
 * Would it be more efficient to sync the config itself, and not the json? Most certainly.
 * Is it dangerous to send just the json? Not really.
 * - A maximally configured spell is ~450 bytes.
 * - Assuming double the vanilla spells (200) which are all maximally configured, this is a size of 90,000 bytes
 * - Packet max size is orders of magnitude larger
 */
public class SyncJsonConfigPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncJsonConfigPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJsonConfigPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncJsonConfigPacket::toBytes, SyncJsonConfigPacket::new);

    public final Map<ResourceLocation, byte[]> data;

    public SyncJsonConfigPacket(Map<ResourceLocation, byte[]> bytes) {
        this.data = bytes;
    }

    public SyncJsonConfigPacket(FriendlyByteBuf buf) {
        this.data = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            var id = buf.readResourceLocation();
            var bytes = new byte[buf.readInt()];
            buf.readBytes(bytes, 0, bytes.length);
            this.data.put(id, bytes);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(data.size());
        for (var entry : data.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeInt(entry.getValue().length);
            buf.writeBytes(entry.getValue());
        }
    }

    public static void handle(SyncJsonConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            for (AbstractSpell spell : SpellRegistry.REGISTRY) {
                spell.resetRarityWeights();
            }
            SpellConfigManager.INSTANCE.handleClientSync(packet);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
