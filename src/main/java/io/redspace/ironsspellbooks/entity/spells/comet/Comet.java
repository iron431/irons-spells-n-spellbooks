package io.redspace.ironsspellbooks.entity.spells.comet;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Comet extends AbstractMagicProjectile {
    public Comet(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public Comet(Level pLevel, LivingEntity pShooter) {
        this(EntityRegistry.COMET.get(), pLevel);
        this.setOwner(pShooter);
    }

    public void shoot(Vec3 rotation, float innaccuracy) {
        Vec3 offset = Utils.getRandomVec3(1).normalize().scale(innaccuracy);
        super.shoot(rotation.add(offset));
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        for (int i = 0; i < 2; i++) {
            Vec3 random = Utils.getRandomVec3(.1);
            this.level.addParticle(ParticleHelper.UNSTABLE_ENDER, d0 - random.x, d1 + 0.5D - random.y, d2 - random.z, random.x * .5f, random.y * .5f, random.z * .5f);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 25, 0, 0, 0, .18, false);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SpellRegistry.STARFALL_SPELL.get().getSchoolType().getTargetingColor(), 1.25f), x, y, z, 1, 0, 0, 0, 0, true);
    }

    @Override
    public float getSpeed() {
        return 1.85f;
    }

    @Override
    protected void doImpactSound(SoundEvent sound) {
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, .8f, 1.35f + Utils.random.nextFloat() * .3f);
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.GENERIC_EXPLODE);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level.isClientSide) {
            impactParticles(xOld, yOld, zOld);
            getImpactSound().ifPresent(this::doImpactSound);
            float explosionRadius = getExplosionRadius();
            var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.distanceToSqr(hitResult.getLocation());
                if (distance < explosionRadius * explosionRadius && canHitEntity(entity)) {
                    DamageSources.applyDamage(entity, damage, SpellRegistry.STARFALL_SPELL.get().getDamageSource(this, getOwner()));
                }
            }
            this.discard();
        }
    }

}
