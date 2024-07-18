package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;


public class WitherSkullProjectile extends AbstractMagicProjectile {
    public WitherSkullProjectile(EntityType<? extends AbstractMagicProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    float speed = 1f;

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
    protected void onHit(HitResult hitResult) {

        if (!this.level().isClientSide) {
            var entities = level().getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.distanceToSqr(hitResult.getLocation());
                if (distance < explosionRadius * explosionRadius && canHitEntity(entity)) {
                    float damage = (float) (this.damage * (1 - distance / (explosionRadius * explosionRadius)));
                    var spell = SpellRegistry.WITHER_SKULL_SPELL.get();
                    DamageSources.applyDamage(entity, damage, spell.getDamageSource(this, getOwner()));
                }
            }

            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 0.0F, false, Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }


    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
