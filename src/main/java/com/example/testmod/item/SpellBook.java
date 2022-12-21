package com.example.testmod.item;

import com.example.testmod.spells.FireballSpell;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class SpellBook extends Item {
    public SpellBook(){
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
    }
    boolean firstUse;
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        FireballSpell fireball = new FireballSpell();
        if(!level.isClientSide){
            //player.sendMessage(new TextComponent("right clicked"),player.getUUID());
            fireball.onUse(this.getDefaultInstance(),level,player);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS,item);
        //return super.use(level, player, hand);
    }

    /*
    @Override
    public void releaseUsing(ItemStack p_41412_, Level p_41413_, LivingEntity p_41414_, int p_41415_) {
        System.out.println("released");
        super.releaseUsing(p_41412_, p_41413_, p_41414_, p_41415_);
    }
     */

    //on using tick
    /*
    @SubscribeEvent
    public void pickupItem(EntityItemPickupEvent event){
        Minecraft.getInstance().player.sendMessage(new TextComponent("picked up item"),Minecraft.getInstance().player.getUUID());
    }
    */


}
