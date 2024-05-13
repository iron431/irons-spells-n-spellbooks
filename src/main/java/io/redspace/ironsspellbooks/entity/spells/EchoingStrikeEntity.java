package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;

public class EchoingStrikeEntity extends AoeEntity {
    public EchoingStrikeEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCircular();
    }

    public EchoingStrikeEntity(Level level, LivingEntity owner, float damage, float radius) {
        this(EntityRegistry.ECHOING_STRIKE.get(), level);
        setOwner(owner);
        this.setRadius(radius);
        this.setDamage(damage);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        // Effect handling is done in tick
        return;
    }

    public final int waitTime = 20;

    @Override
    public void tick() {
        if (tickCount == waitTime) {
            this.playSound(SoundRegistry.ECHOING_STRIKE.get(), 1, Utils.random.nextIntBetweenInclusive(8, 12) * .1f);
            if (!level.isClientSide) {
                var center = this.getBoundingBox().getCenter();
                MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, center.x, center.y, center.z, 25, 0, 0, 0, .18, false);
                MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSchoolType().getTargetingColor(), this.getRadius() * .9f), center.x, center.y, center.z, 1, 0, 0, 0, 0, true);
                float explosionRadius = getRadius();
                var explosionRadiusSqr = explosionRadius * explosionRadius;
                var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
                var losCenter = Utils.moveToRelativeGroundLevel(level, center, 2);
                losCenter = Utils.raycastForBlock(level, losCenter, losCenter.add(0, 3, 0), ClipContext.Fluid.NONE).getLocation().add(losCenter).scale(.5f);
                for (Entity entity : entities) {
                    double distanceSqr = entity.distanceToSqr(center);
                    if (distanceSqr < explosionRadiusSqr && canHitEntity(entity) && Utils.hasLineOfSight(level, losCenter, entity.getBoundingBox().getCenter(), true)) {
                        double p = Mth.clamp((1 - distanceSqr / explosionRadiusSqr) + .4f, 0, 1);
                        float damage = (float) (this.damage * p);
                        DamageSources.applyDamage(entity, damage, SpellRegistry.ECHOING_STRIKES_SPELL.get().getDamageSource(this, getOwner()));
                    }
                }
            }
        } else if (tickCount > waitTime) {
            discard();
        }
        if (level.isClientSide && tickCount < waitTime / 2) {
            Vec3 position = this.getBoundingBox().getCenter();
            for (int i = 0; i < 3; i++) {
                Vec3 vec3 = Utils.getRandomVec3(1f);
                vec3 = vec3.multiply(vec3).multiply(Mth.sign(vec3.x), Mth.sign(vec3.y), Mth.sign(vec3.z)).scale(this.getRadius()).add(position);
                Vec3 motion = position.subtract(vec3).scale(.125f);
                level.addParticle(ParticleHelper.UNSTABLE_ENDER, vec3.x, vec3.y - .5, vec3.z, motion.x, motion.y, motion.z);
            }
        }
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
    }

    @Override
    public void ambientParticles() {
        return;
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
