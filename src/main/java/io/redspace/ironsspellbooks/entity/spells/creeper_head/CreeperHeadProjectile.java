package io.redspace.ironsspellbooks.entity.spells.creeper_head;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.evocation.ChainCreeperSpell;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class CreeperHeadProjectile extends AbstractMagicProjectile {
    protected boolean chainOnKill;

    protected int chainCount;
    protected float speed;

    public CreeperHeadProjectile(EntityType<? extends CreeperHeadProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setExplosionRadius(5);
        chainOnKill = false;

    }

    public CreeperHeadProjectile(Level level, @Nullable Entity owner) {
        this(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(owner);
    }

    @Deprecated(forRemoval = true)
    public CreeperHeadProjectile(@Nullable Entity shooter, Level level, float speed, float damage) {
        super(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(shooter);
        this.speed = speed;
        this.damage = damage;
        this.setRadius(5f);
        this.shoot(shooter.getLookAngle());
    }

    @Deprecated(forRemoval = true)
    public CreeperHeadProjectile(@Nullable Entity shooter, Level level, Vec3 speed, float damage) {
        super(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(shooter);
        this.damage = damage;
        this.setRadius(5f);
        this.speed = (float) speed.length();
        this.shoot(speed);
    }

    public void setChainOnKill(boolean chain) {
        chainOnKill = chain;
    }

    public void setChainCount(int count) {
        chainCount = count;
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
            float explosionRadius = getRadius();
            var entities = level().getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.position().distanceTo(hitResult.getLocation());
                if (distance < explosionRadius && canHitEntity(entity)) {
                    //Prevent duplicate chains
                    if (entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
                        break;
                    }
                    float damage = (float) (this.damage * (1 - Math.pow(distance / (explosionRadius), 2)));
                    DamageSources.applyDamage(entity, damage, SpellRegistry.LOB_CREEPER_SPELL.get().getDamageSource(this, getOwner()));
                    if (chainOnKill && entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
                        ChainCreeperSpell.summonCreeperRing(this.level(), this.getOwner() instanceof LivingEntity livingOwner ? livingOwner : null, livingEntity.getEyePosition(), this.damage * .85f, this.chainCount);
                    }
                }
            }
            var x = getX();
            var y = getY();
            var z = getZ();
            MagicManager.spawnParticles(level, ParticleTypes.EXPLOSION, x, y, z, 3, 0.1, 0.1, 0.1, 0.3, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(1, 1, 1, explosionRadius * 1.2f), x, y, z, 1, 0, 0, 0, 0, true);
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 3, Utils.random.nextFloat() * .2f + .9f);
            this.discardHelper(hitResult);
        }
    }
}
