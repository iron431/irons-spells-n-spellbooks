package com.example.testmod.entity.wither_skull;

import com.example.testmod.damage.DamageSources;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class CreeperHeadProjectile extends WitherSkull {
    public CreeperHeadProjectile(EntityType<? extends WitherSkull> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected float damage;

    public CreeperHeadProjectile(LivingEntity shooter, Level level, float speed, float damage) {
        super(EntityRegistry.CREEPER_HEAD_PROJECTILE.get(), level);
        setOwner(shooter);

        Vec3 power = shooter.getLookAngle().normalize().scale(speed);

        this.xPower = power.x;
        this.yPower = power.y;
        this.zPower = power.z;
        setDeltaMovement(xPower, yPower, zPower);
        this.damage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
    }

    @Override
    public void tick() {
        //super.tick();
//        if (!this.isNoGravity()) {
//            Vec3 vec34 = this.getDeltaMovement();
//            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
//        }
        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                onHit(hitresult);
            }
        } else {
            this.level.addParticle(this.getTrailParticle(), position().x, position().y + 0.25D, position().z, 0.0D, 0.0D, 0.0D);
        }
        ProjectileUtil.rotateTowardsMovement(this, 1);
        setPos(position().add(getDeltaMovement()));

        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }


        this.baseTick();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level.isClientSide) {
            float explosionRadius = 3;
            var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.distanceToSqr(hitResult.getLocation());
                if (distance < explosionRadius * explosionRadius) {
                    float damage = (float) (this.damage * (1 - distance / (explosionRadius * explosionRadius)));
                    DamageSources.applyDamage(entity, damage, SpellType.LOB_CREEPER_SPELL.getDamageSource(), SchoolType.EVOCATION, getOwner());
                }
            }

            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 0.0F, false, Explosion.BlockInteraction.NONE);
            this.discard();
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
