package com.example.testmod.gui.network;

import com.example.testmod.TestMod;
import com.example.testmod.item.SpellBook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketChangeSelectedSpell {
    private final int selectedIndex;

    public PacketChangeSelectedSpell(int selectedIndex) {
        //convert objects into bytes then re-read them into objects
        this.selectedIndex = selectedIndex;
    }

    public PacketChangeSelectedSpell(FriendlyByteBuf buf) {
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

                if (mainHandStack.getItem() instanceof SpellBook spellBook) {
                    spellBook.getSpellBookData(mainHandStack).setActiveSpellIndex(selectedIndex);
                    TestMod.LOGGER.info("Setting Spell Mainhand");

                } else if (offHandStack.getItem() instanceof SpellBook spellBook) {
                    spellBook.getSpellBookData(offHandStack).setActiveSpellIndex(selectedIndex);
                    TestMod.LOGGER.info("Setting Spell Offhand");

                }
            }

        });
        return true;
    }
}
