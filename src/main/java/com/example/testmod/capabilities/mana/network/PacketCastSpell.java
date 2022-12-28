package com.example.testmod.capabilities.mana.network;

import com.example.testmod.capabilities.mana.data.ManaManager;
import com.example.testmod.capabilities.mana.data.PlayerMana;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCastSpell {

    private final int spellId;
    private final int level;
    private final int manaCost;

    public PacketCastSpell(AbstractSpell spell) {
        this.spellId = spell.getID();
        this.level = spell.getLevel();
        this.manaCost = spell.getManaCost();
    }

    public PacketCastSpell(FriendlyByteBuf buf) {
        this.spellId = buf.readInt();
        this.level = buf.readInt();
        this.manaCost = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.spellId);
        buf.writeInt(this.level);
        buf.writeInt(this.manaCost);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Here we are server side
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ManaManager manaManager = ManaManager.get(player.level);
                PlayerMana playerMana = manaManager.getFromPlayerCapability(player);

                if (playerMana.getMana() <= 0) {
                    player.sendMessage(new TextComponent("Out of mana").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                } else if (playerMana.getMana() - manaCost < 0) {
                    player.sendMessage(new TextComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                } else {
                    int newMana = playerMana.getMana() - manaCost;
                    manaManager.setPlayerCurrentMana(player, newMana);
                    Messages.sendToPlayer(new PacketSyncManaToClient(newMana), player);
                }
            }
        });
        return true;
    }
}
