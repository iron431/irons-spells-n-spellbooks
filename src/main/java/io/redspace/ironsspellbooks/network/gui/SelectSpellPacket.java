package io.redspace.ironsspellbooks.network.gui;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SelectSpellPacket implements CustomPacketPayload {
    private final SpellSelection spellSelection;

    public SelectSpellPacket(SpellSelection spellSelection) {
        this.spellSelection = spellSelection;
    }

    public SelectSpellPacket(FriendlyByteBuf buf) {
        var tmpSpellSelection = new SpellSelection();
        tmpSpellSelection.readFromBuffer(buf);
        this.spellSelection = tmpSpellSelection;
    }

    public void toBytes(FriendlyByteBuf buf) {
        spellSelection.writeToBuffer(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                if (Log.SPELL_SELECTION) {
                    IronsSpellbooks.LOGGER.debug("ServerboundSelectSpell.handle {}", spellSelection);
                }
                MagicData.getPlayerMagicData(serverPlayer).getSyncedData().setSpellSelection(spellSelection);
            }
        });
        return true;
    }
}
