package com.example.testmod.spells.fire;

import ca.weblite.objc.Message;
import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.network.PacketAddMotionToClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.logging.Logger;


public class BurningDashSpell extends AbstractSpell {
    //package net.minecraft.client.renderer.entity.layers;

    public BurningDashSpell() {
        this(1);
    }

    public BurningDashSpell(int level) {
        super(SpellType.BURNING_DASH_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
        this.cooldown = 20;
    }

    @Override
    public void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        ClientMagicData.lastSpinAttack = ClientMagicData.SpinAttackType.FIRE;
//
        float amplifier = 0.5f + (level - 1) * .25f;
        Vec3 vec = player.getLookAngle().normalize().scale(amplifier);
//
        if (player.isOnGround())
            player.move(MoverType.SELF, new Vec3(0.0D, 0.75d, 0.0D));

        player.startAutoSpinAttack(10 + level);
        Messages.sendToPlayer(new PacketAddMotionToClient(vec,true),(ServerPlayer)player);
        /*
        //in degrees
        float rx = player.getYRot();
        float ry = player.getXRot();

        float vecX = -Mth.sin(rx * ((float) Math.PI / 180F)) * Mth.cos(ry * ((float) Math.PI / 180F));
        float vecY = -Mth.sin(ry * ((float) Math.PI / 180F));
        float vecZ = Mth.cos(rx * ((float) Math.PI / 180F)) * Mth.cos(ry * ((float) Math.PI / 180F));
        float magnitude = Mth.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
        float amplifier = 1.2f * (1 + ((spellLevel - 1) * .25f));

        vecX *= amplifier / magnitude;
        vecY *= amplifier / magnitude;
        vecZ *= amplifier / magnitude;

        player.startAutoSpinAttack(10);
        player.push(vecX, vecY * .5d, vecZ);

        SoundEvent soundevent;
        if (spellLevel >= 3) {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
        } else if (spellLevel == 2) {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
        } else {
            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
        }
        world.playSound((Player) null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);*/

    }
}
