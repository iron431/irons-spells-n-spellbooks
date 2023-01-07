package com.example.testmod.entity.cone_of_cold;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConeOfColdProjectile extends AbstractConeProjectile {
    public ConeOfColdProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level){
        super(entityType,level);
    }

    public ConeOfColdProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.CONE_OF_COLD_PROJECTILE.get(), level, entity);
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
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            Vec3 randomVec = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(Math.random() > .1 ? ParticleTypes.SNOWFLAKE : ParticleHelper.SNOWFLAKE, x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }


    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        TestMod.LOGGER.debug("ConeOfColdProjectile.onHitEntity: {}", entityHitResult.getEntity().getName().getString());
        var entity = entityHitResult.getEntity();
        entity.hurt(DamageSource.MAGIC, damage);
        entity.setTicksFrozen(entity.getTicksFrozen() + 80);
    }

}
