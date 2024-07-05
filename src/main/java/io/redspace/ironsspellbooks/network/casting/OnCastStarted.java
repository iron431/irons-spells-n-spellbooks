package io.redspace.ironsspellbooks.network.ported.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;
import java.util.function.Supplier;

public class OnCastStarted implements CustomPacketPayload {
    private final String spellId;
    private final int spellLevel;
    private final UUID castingEntityId;
    public static final CustomPacketPayload.Type<OnCastStarted> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "on_cast_started"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OnCastStarted> STREAM_CODEC = CustomPacketPayload.codec(OnCastStarted::write, OnCastStarted::new);

    public OnCastStarted(UUID castingEntityId, String spellId, int spellLevel) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.castingEntityId = castingEntityId;
    }

    public OnCastStarted(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        spellLevel = buf.readInt();
        castingEntityId = buf.readUUID();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(spellLevel);
        buf.writeUUID(castingEntityId);
    }

    public static void handle(OnCastStarted packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastStarted(packet.castingEntityId, packet.spellId, packet.spellLevel);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
