package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.mixin.LivingEntityAccessor;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class VoltStrikeEffect extends MagicMobEffect implements ISyncedMobEffect {
    public VoltStrikeEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        var level = livingEntity.level;
        if (level.isClientSide) {
            return true;
        }
        List<Entity> list = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(.25, .5, .25));
        boolean hit = false;
        UUID ignore = null;
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (DamageSources.applyDamage(entity, amplifier, SpellRegistry.VOLT_STRIKE_SPELL.get().getDamageSource(livingEntity))) {
                    //Guarantee that the entity receives i-frames, since we are damaging every tick
                    entity.invulnerableTime = 20;
                    hit = true;
                    ignore = entity.getUUID();
                }
            }
        }
        if (!hit &&
                (
//                        Utils.raycastForBlock(level, livingEntity.position(), livingEntity.position().add(livingEntity.getDeltaMovement()), ClipContext.Fluid.NONE).getType() == HitResult.Type.BLOCK
                        !level.noCollision(livingEntity.getBoundingBox().move(livingEntity.getDeltaMovement()).move(livingEntity.getDeltaMovement().normalize().scale(0.1)).deflate(0.1))
                )) {
            hit = true;
        }
        if (hit) {
            float explosionRadius = 4;
            var explosionRadiusSqr = explosionRadius * explosionRadius;
            var entities = level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(explosionRadius));
            Vec3 losPoint = Utils.raycastForBlock(level, livingEntity.position(), livingEntity.position().add(0, 1, 0), ClipContext.Fluid.NONE).getLocation();
            for (Entity entity : entities) {
                double distanceSqr = entity.distanceToSqr(livingEntity.position());
                if (ignore != entity.getUUID() && distanceSqr < explosionRadiusSqr && entity.canBeHitByProjectile() && Utils.hasLineOfSight(level, losPoint, entity.getBoundingBox().getCenter(), true)) {
                    double p = (1 - distanceSqr / explosionRadiusSqr);
                    float damage = (float) (amplifier * p * 0.5);
                    DamageSources.applyDamage(entity, damage, SpellRegistry.VOLT_STRIKE_SPELL.get().getDamageSource(livingEntity));
                }
            }
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().normalize().scale(-0.5).add(0, 0.5, 0));
            livingEntity.hurtMarked = true;

            var x = livingEntity.getX();
            var y = livingEntity.getY() + 1;
            var z = livingEntity.getZ();
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, x, y, z, 25, .08, .08, .08, 0.3, false);
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, x, y, z, 75, .1, .1, .1, .5, false);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(new Vector3f(.7f, 1f, 1f), explosionRadius * 2), x, y + .15f, z, 1, 0, 0, 0, 0, true);
            level.playSound(null, x, y, z, SoundEvents.TRIDENT_THUNDER.value(), livingEntity.getSoundSource(), 4, 0.8f);
            return false;
        }
        livingEntity.fallDistance = 0;
        return true;
    }

    @Override
    public void clientTick(LivingEntity entity, MobEffectInstance instance) {
        var level = entity.level;
        for (int i = 0; i < 2; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.ELECTRIC_SPARKS, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
        for (int i = 0; i < 4; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.ELECTRICITY, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
        ((LivingEntityAccessor) pLivingEntity).setLivingEntityFlagInvoker(4, true);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectRemoved(pLivingEntity, pAmplifier);
        ((LivingEntityAccessor) pLivingEntity).setLivingEntityFlagInvoker(4, false);
    }
}
