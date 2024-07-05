package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CastErrorPacket implements CustomPacketPayload {
    public enum ErrorType {
        COOLDOWN,
        MANA
    }

    public final CastErrorPacket.ErrorType errorType;
    public final String spellId;

    public static final CustomPacketPayload.Type<CastErrorPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cast_error"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastErrorPacket> STREAM_CODEC = CustomPacketPayload.codec(CastErrorPacket::write, CastErrorPacket::new);

    public CastErrorPacket(CastErrorPacket.ErrorType errorType, AbstractSpell spell) {
        this.spellId = spell.getSpellId();
        this.errorType = errorType;
    }

    public CastErrorPacket(FriendlyByteBuf buf) {
        errorType = buf.readEnum(CastErrorPacket.ErrorType.class);
        spellId = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(errorType);
        buf.writeUtf(spellId);
    }

    public static void handle(CastErrorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleCastErrorMessage(packet);
            return true;
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
