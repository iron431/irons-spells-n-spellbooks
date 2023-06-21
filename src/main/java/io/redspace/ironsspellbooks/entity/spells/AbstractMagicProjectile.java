package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public abstract class AbstractMagicProjectile extends Projectile implements AntiMagicSusceptible {
    protected static final int EXPIRE_TIME = 15 * 20;

    protected int age;
    protected float damage;
    protected float explosionRadius;

    /**
     * Client Side, called every tick
     */
    public abstract void trailParticles();
    /**
     * Server Side, called alongside onHit()
     */
    public abstract void impactParticles(double x, double y, double z);
    public abstract float getSpeed();
    public abstract Optional<SoundEvent> getImpactSound();

    public AbstractMagicProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(getSpeed()));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public boolean respectsGravity() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (age > EXPIRE_TIME) {
            discard();
            return;
        }
        if (level().isClientSide) {
            trailParticles();


        }
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS  && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            onHit(hitresult);
        }
        setPos(position().add(getDeltaMovement()));
        ProjectileUtil.rotateTowardsMovement(this, 1);
        if (!this.isNoGravity() && respectsGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }

        age++;
    }

    @Override
    protected void onHit(HitResult hitresult) {
//        if (hitresult instanceof EntityHitResult entityHitResult)
//            if (entityHitResult.getEntity().getType() == EntityType.ENDERMAN || (entityHitResult.getEntity() instanceof LivingEntity livingEntity && PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().hasDodgeEffect()))
//                return;
        super.onHit(hitresult);
        double x = xOld;
        double y = yOld;
        double z = zOld;

        if (!level().isClientSide) {
            impactParticles(x, y, z);
            getImpactSound().ifPresent(this::doImpactSound);
        }
    }

    protected void doImpactSound(SoundEvent sound) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, .9f + level().random.nextFloat() * .2f);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void onAntiMagic(PlayerMagicData playerMagicData) {
        this.impactParticles(getX(), getY(), getZ());
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat("Damage", this.getDamage());
        pCompound.putFloat("ExplosionRadius", explosionRadius);


    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.damage = pCompound.getFloat("Damage");
        this.explosionRadius = pCompound.getFloat("ExplosionRadius");

    }

    @Override
    public boolean isOnFire() {
        return false;
    }
}
