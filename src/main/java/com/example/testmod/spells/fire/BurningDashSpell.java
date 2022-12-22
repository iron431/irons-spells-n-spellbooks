package com.example.testmod.spells.fire;

import com.example.testmod.spells.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class BurningDashSpell extends Spell {
    //package net.minecraft.client.renderer.entity.layers;
    public BurningDashSpell(){
        level=1;
        baseManaCost=20;
        manaCostPerLevel=5;
        baseSpellPower=1;
        spellPowerPerLevel=1;
    }

    @Override
    public void onCast(ItemStack stack, Level world, Player player){
        int spellLevel = this.level;//default is the riptide enchantment level
        //in degrees
        float rx = player.getYRot();
        float ry = player.getXRot();

        float vecX = -Mth.sin(rx * ((float)Math.PI / 180F)) * Mth.cos(ry * ((float)Math.PI / 180F));
        float vecY = -Mth.sin(ry * ((float)Math.PI / 180F));
        float vecZ = Mth.cos(rx * ((float)Math.PI / 180F)) * Mth.cos(ry * ((float)Math.PI / 180F));
        float magnitude = Mth.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
        float amplifier = 1.2f * (1+((spellLevel-1)*.25f));

        vecX *= amplifier/magnitude;
        vecY *= amplifier/magnitude;
        vecZ *= amplifier/magnitude;

        if(player.isOnGround())
            player.move(MoverType.SELF, new Vec3(0.0D, 0.75d, 0.0D));
        player.startAutoSpinAttack(10);
        player.push(vecX,vecY*.5d,vecZ);
        //player.spin
        SoundEvent soundevent;
        if (spellLevel >= 3) {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
        } else if (spellLevel == 2) {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
        } else {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
        }
        world.playSound((Player)null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
