package io.redspace.ironsspellbooks.entity.spells.echoing_strikes;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EchoingArrowProjectile extends MagicArrowProjectile {
    public static final int SPAWN_DELAY = 15;

    public EchoingArrowProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setInvisible(true);
    }

    Vec3 lastHomingPosition;

    @Override
    public void tick() {
        super.tick();
        var target = getHomingTarget();

        if (tickCount < SPAWN_DELAY) {
            this.setDeltaMovement(Vec3.ZERO);
            if (target != null) {
                lastHomingPosition = target.getBoundingBox().getCenter();
            }
            for (int i = 0; i < 2; i++) {
                Vec3 delta = Utils.getRandomVec3(0.05);
                Vec3 speed = Utils.getRandomVec3(0.15);
                level.addParticle(ParticleHelper.UNSTABLE_ENDER, getX() + delta.x, getY() + delta.y, getZ() + delta.z, speed.x, speed.y, speed.z);
            }
            return;
        }

        if (tickCount == SPAWN_DELAY) {
            this.setInvisible(false);
            this.stopEntityHoming();
            Vec3 trajectory = lastHomingPosition.subtract(this.position()).normalize();
            this.setDeltaMovement(trajectory.scale(2));
            this.playSound(SoundRegistry.ECHOING_STRIKE.get(), 1.5f, Utils.random.nextIntBetweenInclusive(8, 12) * .1f);
            for (int i = 0; i < 15; i++) {
                Vec3 delta = Utils.getRandomVec3(0.05);
                Vec3 speed = Utils.getRandomVec3(0.35).add(trajectory.scale(1 * Utils.random.nextFloat()));
                level.addParticle(ParticleHelper.ENDER_SPARKS, getX() + delta.x, getY() + delta.y, getZ() + delta.z, speed.x, speed.y, speed.z);
            }
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void handleHitDetection() {
        if (tickCount < SPAWN_DELAY) {
            return;
        }
        super.handleHitDetection();
    }

    @Override
    public void travel() {
        if (tickCount < SPAWN_DELAY) {
            return;
        }
        super.travel();
    }

    @Override
    public void trailParticles() {
        if (tickCount < SPAWN_DELAY) {
            return;
        }
        super.trailParticles();
    }
}
