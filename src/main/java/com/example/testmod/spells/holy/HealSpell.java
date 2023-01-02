package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HealSpell extends AbstractSpell {
    public HealSpell() {
        this(1);
    }

    final float twoPi = 6.283f;

    public HealSpell(int level) {
        super(SpellType.HEAL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
        this.cooldown = 400;
    }

    @Override
    protected void onCast(Level world, Player player) {
        player.heal(getSpellPower());
        int count = 30;
        int radius = 2;
        for (int i = 0; i < count; i++) {
            double x, z;
            double theta = Math.toRadians(360 / count) * i;
            x = Math.cos(theta) * radius;
            z = Math.sin(theta) * radius;
            //TODO: find out if this is a packet and if it should not be spammed
            MagicManager.spawnParticles(world, ParticleTypes.HEART, player.position().x + x, player.position().y, player.position().z + z, 1, 0, 0, 0, 0.1, false);
        }
    }
}
