package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RemoveRecastPacket implements CustomPacketPayload {

    private final String spellId;
    public static final CustomPacketPayload.Type<RemoveRecastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "remove_recast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveRecastPacket> STREAM_CODEC = CustomPacketPayload.codec(RemoveRecastPacket::write, RemoveRecastPacket::new);

    public RemoveRecastPacket(String spellId) {
        this.spellId = spellId;
    }

    public RemoveRecastPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
    }

    public static void handle(RemoveRecastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientMagicData.getRecasts().removeRecast(packet.spellId);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}