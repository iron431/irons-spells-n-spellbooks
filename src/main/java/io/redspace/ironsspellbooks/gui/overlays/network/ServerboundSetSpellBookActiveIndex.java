package io.redspace.ironsspellbooks.gui.overlays.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
                //This could be simplified by passing in a hand too
                var mainHandStack = serverPlayer.getMainHandItem();
                var offHandStack = serverPlayer.getOffhandItem();

                if (mainHandStack.getItem() instanceof SpellBook) {
                    SpellBookData.getSpellBookData(mainHandStack).setActiveSpellIndex(selectedIndex, mainHandStack);
                    IronsSpellbooks.LOGGER.info("Setting Spell Mainhand");
                } else if (offHandStack.getItem() instanceof SpellBook) {
                    SpellBookData.getSpellBookData(offHandStack).setActiveSpellIndex(selectedIndex, offHandStack);
                    IronsSpellbooks.LOGGER.info("Setting Spell Offhand");
                }
            }
        });
        return true;
    }
}
