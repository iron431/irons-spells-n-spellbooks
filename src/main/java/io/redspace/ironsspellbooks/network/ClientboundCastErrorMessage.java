package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundCastErrorMessage {
    public enum ErrorType {
        COOLDOWN,
        MANA
    }

    public final ErrorType errorType;
    public final String spellId;

    public ClientboundCastErrorMessage(ErrorType errorType, AbstractSpell spell) {
        this.spellId = spell.getSpellId();
        this.errorType = errorType;
    }

    public ClientboundCastErrorMessage(FriendlyByteBuf buf) {
        errorType = buf.readEnum(ErrorType.class);
        spellId = buf.readUtf();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(errorType);
        buf.writeUtf(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientSpellCastHelper.handleCastErrorMessage(this);
        });

        return true;
    }
}
