package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.horse.SpectralSteed;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SummonHorseSpell extends AbstractSpell {
    public SummonHorseSpell() {
        this(1);
    }

    public SummonHorseSpell(int level) {
        super(SpellType.SUMMON_HORSE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
        this.cooldown = 300;
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
        SpectralSteed horse = new SpectralSteed(world, entity);
        horse.setPos(entity.position());
        world.addFreshEntity(horse);
        super.onCast(world, entity, playerMagicData);
    }
}
