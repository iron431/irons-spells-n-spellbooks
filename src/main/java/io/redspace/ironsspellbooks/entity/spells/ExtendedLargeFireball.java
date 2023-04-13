package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ExtendedLargeFireball extends LargeFireball implements AntiMagicSusceptible {
    private int explosionPower;
    private float damage;

    public ExtendedLargeFireball(EntityType<? extends LargeFireball> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ExtendedLargeFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, int pExplosionPower) {
        this(EntityRegistry.LARGE_FIREBALL_PROJECTILE.get(), pLevel);
        this.setOwner(pShooter);
        this.explosionPower = pExplosionPower;
        double d0 = Math.sqrt(pOffsetX * pOffsetX + pOffsetY * pOffsetY + pOffsetZ * pOffsetZ);
        if (d0 != 0.0D) {
            this.xPower = pOffsetX / d0 * 0.1D;
            this.yPower = pOffsetY / d0 * 0.1D;
            this.zPower = pOffsetZ / d0 * 0.1D;
        }
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
//
//    @Override
//    protected void onHitEntity(EntityHitResult pResult) {
//        Entity entity = pResult.getEntity();
//        DamageSources.applyDamage(entity, damage, SpellType.FIREBALL_SPELL.getDamageSource(this, getOwner()), SchoolType.FIRE);
//    }

    @Override
    protected void onHit(HitResult hitResult) {

        if (!this.level.isClientSide) {
            float explosionRadius = explosionPower;
            var entities = level.getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            for (Entity entity : entities) {
                double distance = entity.distanceToSqr(hitResult.getLocation());
                if (distance < explosionRadius * explosionRadius) {
                    double p = (1 - Math.pow(Math.sqrt(distance) / (explosionRadius), 3));
                    float damage = (float) (this.damage * p);
                    DamageSources.applyDamage(entity, damage, SpellType.FIREBALL_SPELL.getDamageSource(this, getOwner()), SchoolType.FIRE);
                }
            }
            boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner());
            this.level.explode(null, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, flag, flag ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            this.markHurt();
            Entity entity = pSource.getEntity();
            if (entity != null) {
//                if (!this.level.isClientSide) {
//                    Vec3 vec3 = entity.getLookAngle();
//                    this.setDeltaMovement(vec3);
//                    this.xPower = vec3.x * 0.1D;
//                    this.yPower = vec3.y * 0.1D;
//                    this.zPower = vec3.z * 0.1D;
//                    this.setOwner(entity);
//                }

                return true;
            } else {
                return false;
            }
        }
    }
    @Override
    public void onAntiMagic(PlayerMagicData playerMagicData) {
        this.discard();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putByte("SpellExplosionPower", (byte) this.explosionPower);
        pCompound.putFloat("Damage", this.damage);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.explosionPower = pCompound.getByte("SpellExplosionPower");
        this.damage = pCompound.getFloat("Damage");
    }
}
