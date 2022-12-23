package com.example.testmod.capabilities.mana.network;

import com.example.testmod.capabilities.mana.data.ManaManager;
import com.example.testmod.capabilities.mana.data.PlayerMana;
import com.example.testmod.capabilities.mana.data.PlayerManaProvider;
import com.example.testmod.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCastSpell {

    private final int spellId;

    public PacketCastSpell(int spellId) {
        this.spellId = spellId;
    }

    public PacketCastSpell(FriendlyByteBuf buf) {
        spellId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {

        //TODO: Using spellId as the mana value for initial testing.  Need to make it real

        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ManaManager manaManager = ManaManager.get(player.level);
                PlayerMana playerMana = manaManager.getFromPlayerCapability(player);

                if (playerMana.getMana() <= 0) {
                    player.sendMessage(new TranslatableComponent("Out of mana").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                } else if (playerMana.getMana() - spellId < 0) {
                    player.sendMessage(new TranslatableComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                } else {
                    int newMana = playerMana.getMana() - spellId;
                    manaManager.setPlayerCurrentMana(player, newMana);
                    Messages.sendToPlayer(new PacketSyncManaToClient(newMana), player);
                }
            }
        });
        return true;
    }
}
