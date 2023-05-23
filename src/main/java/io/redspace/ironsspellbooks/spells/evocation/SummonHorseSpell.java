package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.SummonedHorse;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 50;

    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_MIRROR_MOVE);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        int summonTime = 20 * 60 * 10;
        Vec3 spawn = entity.position();
        Vec3 forward = entity.getForward().normalize().scale(1.5f);
        spawn.add(forward.x, 0.15f, forward.z);

        //Teleport pre-existing or create new horse
        var horses = world.getEntitiesOfClass(SummonedHorse.class, entity.getBoundingBox().inflate(100), (summonedHorse) -> summonedHorse.getSummoner() == entity);
        SummonedHorse horse = horses.size() > 0 ? horses.get(0) : new SummonedHorse(world, entity);

        horse.setPos(spawn);
        horse.removeEffectNoUpdate(MobEffectRegistry.SUMMON_HORSE_TIMER.get());
        horse.forceAddEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, false), null);
        setAttributes(horse, getSpellPower(entity));

        world.addFreshEntity(horse);
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_HORSE_TIMER.get(), summonTime, 0, false, false, true));

        super.onCast(world, entity, playerMagicData);
    }

    private void setAttributes(AbstractHorse horse, float power) {
        int maxPower = baseSpellPower + (ServerConfigs.getSpellConfig(SpellType.SUMMON_HORSE_SPELL).maxLevel() - 1) * spellPowerPerLevel;
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
