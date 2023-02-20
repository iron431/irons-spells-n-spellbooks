package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class GreaterHealSpell extends AbstractSpell {
    public GreaterHealSpell() {
        this(1);
    }

    final float twoPi = 6.283f;

    public GreaterHealSpell(int level) {
        super(SpellType.GREATER_HEAL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 200;
        this.baseManaCost = 30;
        this.cooldown = 400;
        uniqueInfo.add(Component.translatable("ui.testmod.greater_healing"));
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
        entity.heal(entity.getMaxHealth());
        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public void onClientCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        var random = level.random;
        //Copied from arrow because these particles use their motion for color??
        int i = PotionUtils.getColor(Potion.byName("healing"));
        double d0 = (double) (i >> 16 & 255) / 255.0D;
        double d1 = (double) (i >> 8 & 255) / 255.0D;
        double d2 = (double) (i >> 0 & 255) / 255.0D;

        for (int j = 0; j < 30; ++j) {
            level.addParticle(ParticleTypes.ENTITY_EFFECT, entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), d0, d1, d2);
        }

        super.onClientCastComplete(level, entity, playerMagicData);
    }
}
