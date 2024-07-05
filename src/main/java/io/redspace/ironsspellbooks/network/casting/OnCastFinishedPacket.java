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

public class OnCastFinishedPacket implements CustomPacketPayload {
    private final String spellId;
    private final UUID castingEntityId;
    private final boolean cancelled;
    public static final CustomPacketPayload.Type<io.redspace.ironsspellbooks.network.casting.OnCastFinishedPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "on_cast_finished"));
    public static final StreamCodec<RegistryFriendlyByteBuf, io.redspace.ironsspellbooks.network.casting.OnCastFinishedPacket> STREAM_CODEC = CustomPacketPayload.codec(io.redspace.ironsspellbooks.network.casting.OnCastFinishedPacket::write, io.redspace.ironsspellbooks.network.casting.OnCastFinishedPacket::new);

    public OnCastFinishedPacket(UUID castingEntityId, String spellId, boolean cancelled) {
        this.spellId = spellId;
        this.castingEntityId = castingEntityId;
        this.cancelled = cancelled;
    }

    public OnCastFinishedPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        castingEntityId = buf.readUUID();
        cancelled = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeUUID(castingEntityId);
        buf.writeBoolean(cancelled);
    }

    public static void handle(io.redspace.ironsspellbooks.network.casting.OnCastFinishedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastFinished(packet.castingEntityId, packet.spellId, packet.cancelled);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
