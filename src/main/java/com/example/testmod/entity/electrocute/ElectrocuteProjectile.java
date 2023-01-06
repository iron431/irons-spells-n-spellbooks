package com.example.testmod.entity.electrocute;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ElectrocuteProjectile extends AbstractConeProjectile {
    public ElectrocuteProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level){
        super(entityType,level);
    }

    public ElectrocuteProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.ELECTROCUTE_PROJECTILE.get(), level, entity);
    }


    @Override
    public void spawnParticles() {
        var owner = getOwner();
        if (!level.isClientSide || owner == null) {
            return;
        }
        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation.scale(0.5f));

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .8f;
        double z = pos.z;

        double speed = .6;
        for (int i = 0; i < 5; i++) {
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            Vec3 randomVec = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(ParticleHelper.ELECTRICITY, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }


    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        entity.hurt(DamageSource.MAGIC, damage);
    }

}
