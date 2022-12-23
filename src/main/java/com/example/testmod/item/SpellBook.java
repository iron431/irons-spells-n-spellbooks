package com.example.testmod.item;

import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import com.example.testmod.spells.Spell;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.TransientEntitySectionManager;

public class SpellBook extends Item {
    public SpellBook(){
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        tempSpell = new BurningDashSpell();
    }
    public Spell tempSpell;
    boolean firstUse;

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        Spell currentSpell;

        //if(player.isCrouching())
        //    currentSpell = new FireballSpell();
        //else
        //    currentSpell = new BurningDashSpell();
        currentSpell = tempSpell;

        if(currentSpell.attemptCast(this.getDefaultInstance(),level,player)){
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS,item);
        }

        return super.use(level, player, hand);
    }
    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_){
        tempSpell.tick();
        super.inventoryTick(p_41404_,p_41405_,p_41406_,p_41407_,p_41408_);
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
