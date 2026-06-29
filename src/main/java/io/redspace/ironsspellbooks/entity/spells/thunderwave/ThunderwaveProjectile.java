package io.redspace.ironsspellbooks.entity.spells.thunderwave;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ThunderwaveProjectile extends AbstractMagicProjectile {
    private static final float LIGHTNING_HEIGHT = 2.5f;
    private static final float STRIKE_RADIUS = 2f;

    public ThunderwaveProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThunderwaveProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.THUNDERWAVE_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 80) {
            // todo: testing
            discard();
            return;
        }
        if (!level.isClientSide) {
            if (tickCount % 10 == 0) {
                strikeLightning();
            }
            Vec3 horizontalMotion = getDeltaMovement().multiply(1, 0, 1);
            Vec3 position = position().add(horizontalMotion);
            Vec3 trailOrigin = position.subtract(horizontalMotion.scale(Math.min(tickCount * 2, 10)));
            int count = getRandom().nextIntBetweenInclusive(2, 5);
            for (int i = 0; i < count; i++) {
                if (random.nextFloat() <= .33f) {
                    Vec3 destination = position.add(0, LIGHTNING_HEIGHT * i / (float) count, 0);
                    MagicManager.spawnParticles(level, new ZapParticleOption(destination), trailOrigin.x, trailOrigin.y, trailOrigin.z, 1, 0, 0, 0, 0.05, true);
                }
            }
        } else {
            int count = 2;
            for (int i = 0; i < count; i++) {
                Vec3 forward = this.getDeltaMovement();
                Vec3 randomPos = Utils.getRandomVec3(.1f).multiply(0, 6, 0).subtract(forward);
                forward = forward.scale(2);
                Vec3 randomSpeed = Utils.getRandomVec3(0.0);
                level.addParticle(ParticleHelper.ELECTRICITY, getX() + randomPos.x, getY() + i / (float) count * LIGHTNING_HEIGHT + randomPos.y, getZ() + randomPos.z,
                        forward.x + randomSpeed.x, 0 + randomSpeed.y, forward.z + randomSpeed.z);
            }
        }
    }

    @Override
    public void travel() {
        Vec3 motion = this.getDeltaMovement();
        move(MoverType.SELF, motion);
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        this.setXRot(Mth.wrapDegrees(xRot));
        this.setYRot(Mth.wrapDegrees(yRot));
        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - getDefaultGravity(), vec34.z);
        }
        // todo: die on standstill. maybe even just collision
        if (this.horizontalCollision) {
            discard();
        }
    }

    @Override
    public float maxUpStep() {
        return 1.6f;
    }

    private void strikeLightning() {
        Vec3 horizontalMotion = getDeltaMovement().multiply(1, 0, 1);
        Vec3 position = position().add(horizontalMotion.scale(3));
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, position.x, position.y, position.z, 10, .2f, .2f, .2f, .2, true);
//        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(0f,0.6f,1f, 3f), position.x, position.y, position.z, 1, 0,0,0,0, true);
        float radius = 3;
        for (int i = 0; i < 5; i++) {
            MagicManager.spawnParticles(level, new ZapParticleOption(position.add((random.nextFloat() - 0.5) * radius * 2, (random.nextFloat()) * radius, (random.nextFloat() - 0.5) * radius* 2)),
                    position.x, position.y, position.z, 1, 0,0,0,0, true);
        }
//        playSound(SoundRegistry.SMALL_LIGHTNING_STRIKE.get(), 1.5f, .85f + random.nextFloat() * .3f);

        level.getEntities(this, new AABB(position, position).inflate(STRIKE_RADIUS), this::canDamageEntity).forEach(target ->
                DamageSources.applyDamage(target, damage, SpellRegistry.THUNDERWAVE_SPELL.get().getDamageSource(this, getOwner()))
        );
    }

    protected boolean canDamageEntity(@NotNull Entity target) {
        return target instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(target, getOwner()) && target != getOwner();
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        return false;
    }

    @Override
    public boolean collidesWithBlocks() {
        return false;
    }

    @Override
    public void trailParticles() {
        Vec3 pos = position().add(getDeltaMovement());
        Vec3 jitter = Utils.getRandomVec3(0.2f);
        level.addParticle(ParticleHelper.ELECTRICITY, pos.x, pos.y, pos.z, jitter.x, jitter.y, jitter.z);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, x, y, z, 25, .15, .15, .15, .5, true);
    }

    @Override
    public float getSpeed() {
        return 0.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
