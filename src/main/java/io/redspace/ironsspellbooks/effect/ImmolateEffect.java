package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class ImmolateEffect extends MagicMobEffect implements ISyncedMobEffect {

    public static final int STACKS_REQUIRED = 3;
    public static final int STACKS_REQUIRED_AMPLIFIER = STACKS_REQUIRED - 1;

    /**
     * Weak map tracking Afflicted to Afflictor in order to maintain explosion killcredit
     */
    private static final Map<LivingEntity, Entity> EFFECT_CREDIT = new WeakHashMap<>();
    /**
     * Weak map tracking specific instances of the effect that should explode at a delay. Maps instance to timestamp
     */
    private static final Map<MobEffectInstance, Integer> DELAYED_INSTANCES = new WeakHashMap<>();

    public ImmolateEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    public static MobEffectInstance addImmolateStack(LivingEntity entity, @Nullable Entity afflicter) {
        MobEffectInstance previous = entity.getEffect(MobEffectRegistry.IMMOLATE);
        MobEffectInstance inst;
        if (previous != null) {
            inst = new MobEffectInstance(MobEffectRegistry.IMMOLATE, 20 * 15, previous.getAmplifier() + 1, previous.isAmbient(), previous.isVisible(), previous.showIcon());
        } else {
            inst = new MobEffectInstance(MobEffectRegistry.IMMOLATE, 20 * 15, 0, false, false, true);
        }
        if (afflicter != null) {
            EFFECT_CREDIT.put(entity, afflicter);
        }
        entity.addEffect(inst);
        return inst;
    }

    @Override
    public void clientTick(LivingEntity livingEntity, MobEffectInstance instance) {
        int amplifier = instance.getAmplifier();
        ParticleOptions particle = ParticleTypes.SMOKE;
        if (amplifier >= 1) {
            particle = ParticleHelper.FIRE;
        }
        var random = livingEntity.getRandom();
        for (int i = 0; i < 2; i++) {
            Vec3 motion = new Vec3(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            );
            motion = motion.scale(.04f);
            livingEntity.level.addParticle(particle, livingEntity.getRandomX(.4f), livingEntity.getRandomY(), livingEntity.getRandomZ(.4f), motion.x, motion.y, motion.z);
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        var self = livingEntity.getEffect(MobEffectRegistry.IMMOLATE);
        if (DELAYED_INSTANCES.containsKey(self) && !(DELAYED_INSTANCES.get(self) - duration > 4)) {
            return true;
        }
        float explosionRadius = 6;
        var level = livingEntity.level;
        if (level.isClientSide) {
            return true;
        }
        @Nullable Entity attacker = EFFECT_CREDIT.remove(livingEntity);
        double baseDamage = damageFor(attacker);

        var source = new DamageSource(level.damageSources().damageTypes.getHolderOrThrow(ISSDamageTypes.FIRE_MAGIC), attacker);
        var explosionRadiusSqr = explosionRadius * explosionRadius;
        var entities = level.getEntities(null, livingEntity.getBoundingBox().inflate(explosionRadius));
        Vec3 losPoint = Utils.raycastForBlock(level, livingEntity.position(), livingEntity.position().add(0, 1, 0), ClipContext.Fluid.NONE).getLocation();
        for (Entity entity : entities) {
            double distanceSqr = entity.distanceToSqr(livingEntity.position());
            if (distanceSqr < explosionRadiusSqr && entity.canBeHitByProjectile() && !DamageSources.isFriendlyFireBetween(attacker, entity) && Utils.hasLineOfSight(level, losPoint, entity.getBoundingBox().getCenter(), true)) {
                double p = (1 - distanceSqr / explosionRadiusSqr);
                float damage = (float) (baseDamage * p);
                if (entity.hurt(source, damage) && entity instanceof LivingEntity livingVictim) {
                    var inst = addImmolateStack(livingVictim, attacker);
                    DELAYED_INSTANCES.put(inst, inst.getDuration());
                }
            }
        }
        PacketDistributor.sendToPlayersTrackingEntity(livingEntity, new FieryExplosionParticlesPacket(livingEntity.getBoundingBox().getCenter(), 1.5f));
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.GENERIC_EXPLODE.value(), livingEntity.getSoundSource(), 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        return false;
    }

    public static double damageFor(@Nullable Entity entity) {
        double baseDamage = 10.0;
        if (entity instanceof LivingEntity livingAttacker) {
            baseDamage = baseDamage * livingAttacker.getAttributeValue(AttributeRegistry.SPELL_POWER) * livingAttacker.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER);
        }
        return baseDamage;
    }

    static int duration;

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        ImmolateEffect.duration = duration;
        return amplifier >= STACKS_REQUIRED_AMPLIFIER;
    }
}
