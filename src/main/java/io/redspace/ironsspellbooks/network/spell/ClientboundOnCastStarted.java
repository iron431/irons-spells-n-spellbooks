package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundOnCastStarted {

    private String spellId;
    private int spellLevel;
    private UUID castingEntityId;

    public ClientboundOnCastStarted(UUID castingEntityId, String spellId, int spellLevel) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.castingEntityId = castingEntityId;
    }

    public ClientboundOnCastStarted(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        spellLevel = buf.readInt();
        castingEntityId = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(spellLevel);
        buf.writeUUID(castingEntityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastStarted(castingEntityId, spellId, spellLevel);
        });
        return true;
    }
}
