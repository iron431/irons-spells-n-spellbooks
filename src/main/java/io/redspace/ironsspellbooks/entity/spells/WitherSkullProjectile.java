package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public class WitherSkullProjectile extends AbstractMagicProjectile {
    public WitherSkullProjectile(EntityType<? extends WitherSkullProjectile> pEntityType, Level pLevel) {
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
        this.setRadius(2);
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
    protected float getBaseSpeed() {
        return speed;
    }

    @Override
    public Optional<PlayableSound> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        if (!this.level().isClientSide) {
            Entity directHit = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
            var explosionRadius = getRadius();
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
}
