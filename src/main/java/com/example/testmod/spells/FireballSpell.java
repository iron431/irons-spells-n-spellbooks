package com.example.testmod.spells;

import com.mojang.math.Vector3d;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballSpell extends Spell{
    public FireballSpell(){
        level=1;
        baseManaCost=20;
        manaCostPerLevel=5;
        baseSpellPower=1;
        spellPowerPerLevel=1;
    }
    @Override
    public InteractionResultHolder<ItemStack> onUse(ItemStack stack, Level level, Player player){
        double speed = 1.5;
        Vec3 direction = player.getLookAngle().scale(speed);
        Vec3 origin = player.getEyePosition();
        Level world = player.getLevel();
        Fireball fireball = new LargeFireball(world,player,direction.x(),direction.y(),direction.z(),2);
        fireball.setPos(origin.add(direction));

        world.addFreshEntity(fireball);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS,stack);
    }
}
