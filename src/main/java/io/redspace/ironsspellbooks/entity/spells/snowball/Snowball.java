package io.redspace.ironsspellbooks.entity.spells.snowball;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Snowball extends AbstractMagicProjectile {
    public Snowball(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public Snowball(Level level, LivingEntity shooter) {
        this(EntityRegistry.SNOWBALL.get(), level);
        setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        for (int i = 0; i < 4; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            this.level.addParticle(ParticleHelper.SNOW_DUST, d0 - random.x, d1 + 0.5D - random.y, d2 - random.z, random.x * .5f, random.y * .5f, random.z * .5f);

        }
        Vec3 random = Utils.getRandomVec3(.2);
        this.level.addParticle(ParticleHelper.SNOWFLAKE, d0 - random.x, d1 + 0.5D - random.y, d2 - random.z, random.x * .5f, random.y * .5f, random.z * .5f);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, x, y, z, 50, 0.5, 0.5, 0.5, .2, true);
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, x, y, z, 50, 0.5, 0.5, 0.5, .2, false);
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    protected void onHit(HitResult hitresult) {
        super.onHit(hitresult);
        createFrostField(Utils.moveToRelativeGroundLevel(level, hitresult.getLocation(), 2));
        float explosionRadius = getExplosionRadius();
        var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
        for (Entity entity : entities) {
            double distance = entity.distanceToSqr(hitresult.getLocation());
            if (entity instanceof LivingEntity livingEntity && distance < explosionRadius * explosionRadius && canHitEntity(entity) && !DamageSources.isFriendlyFireBetween(getOwner(), entity)) {
                if (Utils.hasLineOfSight(level, hitresult.getLocation(), entity.position().add(0, entity.getEyeHeight() * .5f, 0), true)) {
//                    double p = (1 - Math.pow(Math.sqrt(distance) / (explosionRadius), 3));
//                    entity.setTicksFrozen(entity.getTicksFrozen() + (int) (entity.getTicksRequiredToFreeze() * 2 * p));
                    livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, (int) getDamage()));
                }
            }
        }
        this.discardHelper(hitresult);
    }

    public void createFrostField(Vec3 location) {
        if (!level.isClientSide) {
            FrostField fire = new FrostField(level);
            fire.setOwner(getOwner());
            fire.setDuration((int) getDamage());
            fire.setRadius(getExplosionRadius());
            fire.setCircular();
            fire.moveTo(location);
            level.addFreshEntity(fire);
        }
    }

    @Override
    protected void doImpactSound(Holder<SoundEvent> sound) {
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, 0.7f + Utils.random.nextFloat() * .2f);
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(SoundRegistry.ICE_SPIKE_EMERGE);
    }

}
