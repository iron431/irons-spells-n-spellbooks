package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.capabilities.magic.*;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;


public class BurningDashSpell extends AbstractSpell {
    //package net.minecraft.client.renderer.entity.layers;
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.FIRE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public BurningDashSpell() {
        this(1);
    }

    public BurningDashSpell(int level) {
        super(SpellType.BURNING_DASH_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, CastData castData) {
        if (castData instanceof ImpulseCastData bdcd) {
            entity.hasImpulse = bdcd.hasImpulse;
            entity.setDeltaMovement(entity.getDeltaMovement().add(bdcd.x, bdcd.y, bdcd.z));
        }

        super.onClientCast(level, entity, castData);
    }

    @Override
    public CastDataSerializable getEmptyCastData() {
        return new ImpulseCastData();
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
        entity.hasImpulse = true;
        float multiplier = (15 + getSpellPower(entity)) / 12f;

        //Direction for Mobs to cast in
        Vec3 forward = entity.getLookAngle();
        if (playerMagicData.getAdditionalCastData() instanceof BurningDashDirectionOverrideCastData) {
            if (world.random.nextBoolean())
                forward = forward.yRot(90);
            else
                forward = forward.yRot(-90);

        }

        //Create Dashing Movement Impulse
        var vec = forward.multiply(3, 1, 3).normalize().add(0, .25, 0).scale(multiplier);
        playerMagicData.setAdditionalCastData(new ImpulseCastData((float) vec.x, (float) vec.y, (float) vec.z, true));
        entity.setDeltaMovement(entity.getDeltaMovement().add(vec));

        //Start Spin Attack
        if (entity.onGround())
            entity.setPos(entity.position().add(0, 1.2, 0));
        startSpinAttack(entity, 10);

        //Deal Shockwave Damage and particles
        world.getEntities(entity, entity.getBoundingBox().inflate(4)).forEach((target) -> {
            if (target.distanceToSqr(entity) < 16) {
                if (DamageSources.applyDamage(target, getDamage(entity), SpellType.BURNING_DASH_SPELL.getDamageSource(entity), SchoolType.FIRE))
                    target.setSecondsOnFire(3);
            }
        });
        MagicManager.spawnParticles(world, ParticleHelper.FIRE, entity.getX(), entity.getY(), entity.getZ(), 75, 1, 0, 1, .08, false);

        playerMagicData.getSyncedData().setSpinAttackType(SpinAttackType.FIRE);
        super.onCast(world, entity, playerMagicData);
    }

    private float getDamage(LivingEntity caster) {
        return 5 + getSpellPower(caster) / 2;
    }

    private void startSpinAttack(LivingEntity entity, int durationInTicks) {
        if (entity instanceof Player player)
            player.startAutoSpinAttack(durationInTicks);
        else if (entity instanceof AbstractSpellCastingMob mob)
            mob.startAutoSpinAttack(durationInTicks);
    }

    public static class BurningDashDirectionOverrideCastData implements CastData {

        @Override
        public void reset() {

        }
    }
}
