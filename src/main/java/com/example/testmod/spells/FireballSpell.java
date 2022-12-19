package com.example.testmod.spells;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Fireball extends Spell{
    public Fireball(){
        level=1;
        baseManaCost=20;
        manaCostPerLevel=5;
        baseSpellPower=1;
        spellPowerPerLevel=1;
    }
    @Override
    public InteractionResultHolder<ItemStack> onUse(ItemStack stack, Level level, Player player){
        Vec3 direction = player.getLookAngle();
        Vec3 origin = player.getEyePosition();
        net.minecraft.world.entity.projectile.Fireball fireball = new LargeFireball(level,player,direction.x(),direction.y(),direction.z(),getSpellPower());
        fireball.setPos(origin);
        //level.addFreshEntity(fireball);
        player.getLevel().addFreshEntity(fireball);
        System.out.println("fireball cast");
        System.out.println(origin);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS,stack);
    }
}
