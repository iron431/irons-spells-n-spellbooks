package com.example.testmod.gui.network;

import com.example.testmod.capabilities.mana.client.ClientManaData;
import com.example.testmod.capabilities.mana.data.ManaManager;
import com.example.testmod.capabilities.mana.data.PlayerMana;
import com.example.testmod.capabilities.mana.network.PacketSyncManaToClient;
import com.example.testmod.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketInscribeSpell {
    private final int spellId;
    private final int level;
    private final int slot;

    public PacketInscribeSpell(int spellId,int level, int slot){
        this.spellId=spellId;
        this.level = level;
        this.slot = slot;
    }
    public PacketInscribeSpell(FriendlyByteBuf buf){
        spellId=buf.readInt();
        level=buf.readInt();
        slot=buf.readInt();
    }
    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(spellId);
        buf.writeInt(level);
        buf.writeInt(slot);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            //ItemStack spellbookStack = ctx.getSender().getItemBySlot();
            ServerPlayer player = ctx.getSender();
            if (player != null) {
//                ManaManager manaManager = ManaManager.get(player.level);
//                PlayerMana playerMana = manaManager.getFromPlayerCapability(player);
//
//                if (playerMana.getMana() <= 0) {
//                    player.sendMessage(new TranslatableComponent("Out of mana").withStyle(ChatFormatting.RED), Util.NIL_UUID);
//                } else if (playerMana.getMana() - manaCost < 0) {
//                    player.sendMessage(new TranslatableComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
//                } else {
//                    int newMana = playerMana.getMana() - manaCost;
//                    manaManager.setPlayerCurrentMana(player, newMana);
//                    Messages.sendToPlayer(new PacketSyncManaToClient(newMana), player);
//                }
            }
        });
        return true;
    }

}
