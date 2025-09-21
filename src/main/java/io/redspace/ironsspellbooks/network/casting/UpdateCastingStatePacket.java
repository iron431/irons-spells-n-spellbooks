package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class UpdateCastingStatePacket implements CustomPacketPayload {

    private final String spellId;
    private final int spellLevel;
    private final int castTime;
    private final CastSource castSource;

    private final String castingEquipmentSlot;

    public UpdateCastingStatePacket(String spellId, int spellLevel, int castTime, CastSource castSource, String castingEquipmentSlot) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.castTime = castTime;
        this.castSource = castSource;
        this.castingEquipmentSlot = castingEquipmentSlot;
    }

    public UpdateCastingStatePacket(FriendlyByteBuf buf) {
        this.spellId = buf.readUtf();
        this.spellLevel = buf.readInt();
        this.castTime = buf.readInt();
        this.castSource = buf.readEnum(CastSource.class);
        this.castingEquipmentSlot = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.spellId);
        buf.writeInt(this.spellLevel);
        buf.writeInt(this.castTime);
        buf.writeEnum(this.castSource);
        buf.writeUtf(this.castingEquipmentSlot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientMagicData.setClientCastState(spellId, spellLevel, castTime, castSource, castingEquipmentSlot));
        return true;
    }
}
