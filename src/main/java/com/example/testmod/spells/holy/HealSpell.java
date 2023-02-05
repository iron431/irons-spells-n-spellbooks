package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

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
        uniqueInfo.add(Component.translatable("ui.testmod.healing", Utils.stringTruncation(getSpellPower(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.heal(getSpellPower(entity));
        int count = 16;
        float radius = 1.25f;
        for (int i = 0; i < count; i++) {
            double x, z;
            double theta = Math.toRadians(360 / count) * i;
            x = Math.cos(theta) * radius;
            z = Math.sin(theta) * radius;
            MagicManager.spawnParticles(world, ParticleTypes.HEART, entity.position().x + x, entity.position().y, entity.position().z + z, 1, 0, 0, 0, 0.1, false);
        }
        super.onCast(world, entity, playerMagicData);
    }

}
