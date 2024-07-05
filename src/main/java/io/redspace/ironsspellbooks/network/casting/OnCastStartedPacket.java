package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class OnCastStartedPacket implements CustomPacketPayload {
    private final String spellId;
    private final int spellLevel;
    private final UUID castingEntityId;
    public static final CustomPacketPayload.Type<OnCastStartedPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "on_cast_started"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OnCastStartedPacket> STREAM_CODEC = CustomPacketPayload.codec(OnCastStartedPacket::write, OnCastStartedPacket::new);

    public OnCastStartedPacket(UUID castingEntityId, String spellId, int spellLevel) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.castingEntityId = castingEntityId;
    }

    public OnCastStartedPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        spellLevel = buf.readInt();
        castingEntityId = buf.readUUID();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(spellLevel);
        buf.writeUUID(castingEntityId);
    }

    public static void handle(OnCastStartedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastStarted(packet.castingEntityId, packet.spellId, packet.spellLevel);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
