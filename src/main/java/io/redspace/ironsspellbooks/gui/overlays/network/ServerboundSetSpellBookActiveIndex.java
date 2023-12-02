package io.redspace.ironsspellbooks.gui.overlays.network;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundSetSpellBookActiveIndex {
    private final int selectedIndex;

    public ServerboundSetSpellBookActiveIndex(int selectedIndex) {
        //convert objects into bytes then re-read them into objects
        this.selectedIndex = selectedIndex;
    }

    public ServerboundSetSpellBookActiveIndex(FriendlyByteBuf buf) {
        selectedIndex = buf.readInt();

    }

    public void toBytes(FriendlyByteBuf buf) {

        buf.writeInt(selectedIndex);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                var spellbookStack = Utils.getPlayerSpellbookStack(serverPlayer);
                if (spellbookStack != null) {
                    SpellBookData.getSpellBookData(spellbookStack).setActiveSpellIndex(selectedIndex, spellbookStack);
                }
            }
        });
        return true;
    }
}
