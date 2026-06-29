package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public class WitherSkullProjectile extends AbstractMagicProjectile {
    public WitherSkullProjectile(EntityType<? extends AbstractMagicProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.setExplosionRadius(2);
    }

    public float speed = 1f;

    public WitherSkullProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.WITHER_SKULL_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    @Deprecated(forRemoval = true)
    public WitherSkullProjectile(LivingEntity shooter, Level level, float speed, float damage) {
        super(EntityRegistry.WITHER_SKULL_PROJECTILE.get(), level);
        setOwner(shooter);
        this.speed = speed;
        this.damage = damage;
        this.explosionRadius = 2;
        this.shoot(shooter.getLookAngle());
        this.setNoGravity(true);
    }

    @Override
    public void trailParticles() {
        var vec3 = this.getBoundingBox().getCenter();
        level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        if (!this.level().isClientSide) {
            Entity directHit = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
            var entities = level().getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            var damageSource = SpellRegistry.WITHER_SKULL_SPELL.get().getDamageSource(this, getOwner());
            for (Entity entity : entities) {
                if (entity == directHit) {
                    DamageSources.applyDamage(entity, damage, damageSource);
                } else {
                    double distanceSqr = entity.distanceToSqr(hitResult.getLocation());
                    if (distanceSqr < explosionRadius * explosionRadius && canHitEntity(entity)) {
                        float entityRadius = explosionRadius + entity.getBbWidth();
                        float damage = (float) (this.damage * (1 - distanceSqr / (entityRadius * entityRadius)));
                        DamageSources.applyDamage(entity, damage, damageSource);
                    }
                }
            }

            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 0.0F, false, Level.ExplosionInteraction.NONE);
            this.discardHelper(hitResult);
        }
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
