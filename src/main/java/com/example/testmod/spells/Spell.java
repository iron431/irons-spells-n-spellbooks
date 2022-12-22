package com.example.testmod.spells;

import io.netty.util.concurrent.SucceededFuture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Spell {

    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public int getSpellPower() {
        return baseSpellPower + spellPowerPerLevel * (level - 1);
    }

    //returns true/false for success/failure to cast
    public boolean attemptCast(ItemStack stack, Level world, Player player) {
        //fill with all casting criteria... for now just mana #
        boolean canCast = getManaDeleteMe(player) >= getManaCost();
        if (canCast) {
            this.onCast(stack, world, player);
            return true;
        } else {
            return false;
        }
    }

    public abstract void onCast(ItemStack stack, Level world, Player player);

    private int getManaDeleteMe(Player player) {
        //mana not implemented yet
        player.sendMessage(new TextComponent(getManaCost() + ""), player.getUUID());
        return 1000;
    }
}
