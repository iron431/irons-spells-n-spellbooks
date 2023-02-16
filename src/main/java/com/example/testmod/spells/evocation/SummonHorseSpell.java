package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.config.ServerConfigs;
import com.example.testmod.entity.mobs.horse.SpectralSteed;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        int summonTime = 20 * 60 * 3;
        Vec3 spawn = entity.position();
        Vec3 forward = entity.getForward().normalize().scale(1.5f);
        spawn.add(forward.x, 0.15f, forward.z);
        horse.setPos(spawn);
        horse.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, false));
        setAttributes(horse, getSpellPower(entity));

        //Remove pre-existing horses
        world.getEntitiesOfClass(SpectralSteed.class, entity.getBoundingBox().inflate(100), (spectralSteed) -> spectralSteed.getSummoner() == entity).forEach((SpectralSteed::onUnSummon));
        world.addFreshEntity(horse);
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }

    private void setAttributes(AbstractHorse horse, float power) {
        int maxPower = baseSpellPower + (ServerConfigs.getSpellConfig(SpellType.SUMMON_HORSE_SPELL).MAX_LEVEL - 1) * spellPowerPerLevel;
        float quality = power / (float) maxPower;

        float minSpeed = .2f;
        float maxSpeed = .45f;

        float minJump = .6f;
        float maxJump = 1f;

        float minHealth = 10;
        float maxHealth = 40;

        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(quality, minSpeed, maxSpeed));
        horse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Mth.lerp(quality, minJump, maxJump));
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Mth.lerp(quality, minHealth, maxHealth));
        horse.setHealth(horse.getMaxHealth());
    }
}
