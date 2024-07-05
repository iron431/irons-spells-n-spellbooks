package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class OnClientCastPacket implements CustomPacketPayload {
    String spellId;
    int level;
    CastSource castSource;
    ICastData castData;
    public static final CustomPacketPayload.Type<OnClientCastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "on_client_cast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OnClientCastPacket> STREAM_CODEC = CustomPacketPayload.codec(OnClientCastPacket::write, OnClientCastPacket::new);

    public OnClientCastPacket(String spellId, int level, CastSource castSource, ICastData castData) {
        this.spellId = spellId;
        this.level = level;
        this.castSource = castSource;
        this.castData = castData;
    }

    public OnClientCastPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        level = buf.readInt();
        castSource = buf.readEnum(CastSource.class);
        if (buf.readBoolean()) {
            var tmp = SpellRegistry.getSpell(spellId).getEmptyCastData();
            tmp.readFromBuffer(buf);
            castData = tmp;
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(level);
        buf.writeEnum(castSource);
        if (castData instanceof ICastDataSerializable castDataSerializable) {
            buf.writeBoolean(true);
            castDataSerializable.writeToBuffer(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static void handle(OnClientCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientboundOnClientCast(packet.spellId, packet.level, packet.castSource, packet.castData);
            //DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSpellCastHelper.handleClientboundOnClientCast(spellId, level, castSource, castData));
        });

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}