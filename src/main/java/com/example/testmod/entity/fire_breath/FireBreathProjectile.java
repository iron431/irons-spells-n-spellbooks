package com.example.testmod.entity.fire_breath;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.common.returnsreceiver.qual.This;

public class FireBreathProjectile extends AbstractConeProjectile {
    public FireBreathProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireBreathProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.FIRE_BREATH_PROJECTILE.get(), level, entity);
    }

    @Override
    public void tick() {
        if (!level.isClientSide && getOwner() != null)
            if (dealDamageActive) {
                float range = 15 * Mth.DEG_TO_RAD;
                for (int i = 0; i < 3; i++) {
                    Vec3 cast = getOwner().getLookAngle().normalize().xRot(level.random.nextFloat() * range * 2 - range).yRot(level.random.nextFloat() * range * 2 - range);
                    HitResult hitResult = level.clip(new ClipContext(getOwner().getEyePosition(), getOwner().getEyePosition().add(cast.scale(10)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        Vec3 pos = hitResult.getLocation().subtract(cast.scale(.5));
                        BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);
                        if(level.getBlockState(blockPos).isAir())
                            level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
                    }
                }
            }
        super.tick();
    }


    @Override
    public void spawnParticles() {
        var owner = getOwner();
        if (!level.isClientSide || owner == null) {
            return;
        }
        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation);

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .8f;
        double z = pos.z;

        double speed = .6;
        for (int i = 0; i < 10; i++) {
            double offset = .05;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            Vec3 randomVec = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(Math.random() > .1 ? ParticleTypes.FLAME : ParticleTypes.SMOKE, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        entity.hurt(DamageSource.MAGIC, damage);
        entity.setSecondsOnFire(4);
    }
}
