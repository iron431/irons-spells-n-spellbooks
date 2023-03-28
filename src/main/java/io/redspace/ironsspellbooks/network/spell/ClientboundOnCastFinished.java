package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundOnCastFinished {

    private final SpellType spellType;
    private final UUID castingEntityId;

    public ClientboundOnCastFinished(UUID castingEntityId, SpellType spellType) {
        this.spellType = spellType;
        this.castingEntityId = castingEntityId;
    }

    public ClientboundOnCastFinished(FriendlyByteBuf buf) {
        spellType = buf.readEnum(SpellType.class);
        castingEntityId = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(spellType);
        buf.writeUUID(castingEntityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastFinished(castingEntityId, spellType);
        });
        return true;
    }
}
