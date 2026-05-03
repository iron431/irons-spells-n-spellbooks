package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.SwirlingParticleOptions;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BlizzardAoe extends AoeEntity {
    public static final float HEIGHT = 4f;

    public BlizzardAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCircular();
        this.reapplicationDelay = 2;
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
    public void applyEffect(LivingEntity target) {
        Utils.addFreezeTicks(target, 10);
    }

    @Override
    public void tick() {
        super.tick();
        var entities = level.getEntities(this, this.getBoundingBox(), this::canHitEntity);
        var radius = getRadius();
        double strength = this.getDeltaMovement().horizontalDistance() * 0.5;
        for (Entity entity : entities) {
            if (entity.distanceToSqr(this) < radius * radius) {
                Vec3 offset = entity.position().subtract(this.position());
                double dist = offset.horizontalDistance();
                if (dist < 0.1) {
                    continue;
                }
                Vec3 radial = new Vec3(offset.x, 0, offset.z).normalize();
                Vec3 tangent = new Vec3(-radial.z, 0, radial.x);
                Vec3 push = tangent.scale(strength * Mth.PI).add(radial.scale(-strength));
                entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            }
        }
        this.move(MoverType.SELF, getDeltaMovement());
        if (tickCount % 20 == 0) {
            if (level.collidesWithSuffocatingBlock(this, AABB.ofSize(this.position().add(0, 0.5, 0), 1.5, 0.5, 1.5))) {
                Vec3 ground = Utils.moveToRelativeGroundLevel(level, this.position().add(0, 1, 0), 1);
                this.move(MoverType.SELF, ground.subtract(this.position()));
            } else {
                if (Utils.raycastForBlock(level, position(), position().add(0, -0.5, 0), ClipContext.Fluid.NONE).getType() == HitResult.Type.MISS) {
                    this.move(MoverType.SELF, new Vec3(0, -0.5, 0));
                }
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && !DamageSources.isFriendlyFireBetween(this.getOwner(), pTarget);
    }

    @Override
    public void ambientParticles() {
        if (!level.isClientSide) {
            return;
        }
        Vec3 pos = position();
        float radius = getRadius();
        for (int i = 0; i < 8; i++) {
            swirlingParticle(radius, pos, ParticleHelper.SNOWFLAKE);
            swirlingParticle(radius, pos, ParticleHelper.SNOW_DUST);
        }
    }

    private void swirlingParticle(float radius, Vec3 pos, ParticleOptions particle) {
        float diameter = radius * (.1f + .9f * random.nextFloat()) * 2;
        float angularSpeed = 10f * (random.nextFloat() + 0.5f);
        Vec3 center = pos.add(Utils.getRandomVec3(1f)).add(0, 1, 0);
        level.addParticle(new SwirlingParticleOptions(
                particle, new Vec3(0, 1, 0), new Vec3(0, 0, 1), new Vec3(diameter, diameter, angularSpeed), new Vec3(0, 0, 0)
        ), center.x, center.y, center.z, 0, 0, 0);
    }
}
