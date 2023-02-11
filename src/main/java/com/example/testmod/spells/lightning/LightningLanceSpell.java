package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.lightning_lance.LightningLanceProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LightningLanceSpell extends AbstractSpell {
    public LightningLanceSpell(int level) {
        super(SpellType.LIGHTNING_LANCE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 50;
        this.baseManaCost = 10;
        this.cooldown = 100;
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
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        //TODO: not this
        entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, castTime * 2, 0, false, false, false));
        super.onServerPreCast(level, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        LightningLanceProjectile firebolt = new LightningLanceProjectile(level, entity);
        firebolt.setPos(entity.position().add(0, entity.getEyeHeight() - firebolt.getBoundingBox().getYsize() * .5f, 0));
        firebolt.shoot(entity.getLookAngle());
        //firebolt.setDamage(getSpellPower(entity));
        level.addFreshEntity(firebolt);
        super.onCast(level, entity, playerMagicData);
    }

    @Override
    public void onCastServerComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.removeEffect(MobEffects.LEVITATION);
        super.onCastServerComplete(level, entity, playerMagicData);
    }
}
