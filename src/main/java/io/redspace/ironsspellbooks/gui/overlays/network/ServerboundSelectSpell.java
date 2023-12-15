package io.redspace.ironsspellbooks.gui.overlays.network;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelSelection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundSelectSpell {
    private final SpellWheelSelection spellWheelSelection;

    public ServerboundSelectSpell(SpellWheelSelection spellWheelSelection) {
        this.spellWheelSelection = spellWheelSelection;
    }

    public ServerboundSelectSpell(FriendlyByteBuf buf) {
        var spellWheelSelection = new SpellWheelSelection();
        spellWheelSelection.readFromBuffer(buf);
        this.spellWheelSelection = spellWheelSelection;
    }

    public void toBytes(FriendlyByteBuf buf) {
        spellWheelSelection.writeToBuffer(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
//                var spellbookStack = Utils.getPlayerSpellbookStack(serverPlayer);
//                if (spellbookStack != null) {
//                    SpellBookData.getSpellBookData(spellbookStack).setActiveSpellIndex(selectedIndex, spellbookStack);
//                }
            }
        });
        return true;
    }
}
