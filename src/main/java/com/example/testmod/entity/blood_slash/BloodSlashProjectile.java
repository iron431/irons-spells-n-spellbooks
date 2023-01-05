package com.example.testmod.entity.blood_slash;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BloodSlashProjectile extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(BloodSlashProjectile.class, EntityDataSerializers.FLOAT);
    private static final double SPEED = 1d;
    private static final int EXPIRE_TIME = 10 * 20;
    public final int animationSeed;
    private final float maxRadius;
    private EntityDimensions dimensions;
    public AABB oldBB;
    private int age;
    private float damage;
    public int animationTime;

    public BloodSlashProjectile(EntityType<? extends BloodSlashProjectile> entityType, Level level) {
        super(entityType, level);
        animationSeed = level.random.nextInt(9999);

        float initialRadius = 2;
        maxRadius = 4;
        dimensions = EntityDimensions.scalable(initialRadius, 0.5f);

        oldBB = getBoundingBox();
        this.setNoGravity(true);
    }

    public BloodSlashProjectile(EntityType<? extends BloodSlashProjectile> entityType, Level levelIn, LivingEntity shooter) {
        this(entityType, levelIn);
        setOwner(shooter);
        setYRot(shooter.getYRot());
        setXRot(shooter.getXRot());
    }

    public BloodSlashProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.BLOOD_SLASH_PROJECTILE.get(), levelIn, shooter);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(SPEED));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    //TODO: override "doWaterSplashEffect"
    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_RADIUS, 0.5F);

    }

    public void setRadius(float newRadius) {
        if (newRadius <= maxRadius && !this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Mth.clamp(newRadius, 0.0F, maxRadius));
        }
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    public void tick() {
        super.tick();
        if (age > EXPIRE_TIME) {
            discard();
            return;
        }
        //TestMod.LOGGER.info("Increasing Radius");
        oldBB = getBoundingBox();
        setRadius(getRadius() + 0.12f);
        //TODO: replace all this with custom hit detection
        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                onHit(hitresult);
            }
            //spawnParticles();
        }
        List<Entity> collisions = new ArrayList<>();
        collisions.addAll(level.getEntities(this, this.getBoundingBox()));

        collisions = collisions.stream().filter(target ->
                target != getOwner() && target instanceof LivingEntity).collect(Collectors.toList());
        for (Entity entity : collisions) {
            TestMod.LOGGER.info(entity.getName().getString());
        }
        setPos(position().add(getDeltaMovement()));
        spawnParticles();
        age++;
    }

    public EntityDimensions getDimensions(Pose p_19721_) {
        //TestMod.LOGGER.info("Accessing Blood Slash Dimensions. Age: {}", age);
        this.getBoundingBox();
        return EntityDimensions.scalable(this.getRadius() + 2.0F, 0.5F);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_19729_) {
        //TestMod.LOGGER.info("onSynchedDataUpdated");

        if (DATA_RADIUS.equals(p_19729_)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(p_19729_);
    }

    //    private void increaseSize(float increase){
//        var bbOld = this.getBoundingBox();
//        double newWidth = (bbOld.getXsize() + increase) * .5;
//        double halfHeight = bbOld.getYsize() * .5;
//        Vec3 from = bbOld.getCenter().subtract(newWidth, halfHeight, newWidth);
//        Vec3 to = bbOld.getCenter().add(newWidth, halfHeight, newWidth);
//        this.setBoundingBox(new AABB(from.x,from.y,from.z,to.x,to.y,to.z));
//    }
    @Override
    protected void onHit(HitResult hitresult) {
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitresult);
        } else if (hitresult.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitresult);
        }
        double x = hitresult.getLocation().x;
        double y = hitresult.getLocation().y;
        double z = hitresult.getLocation().z;

        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, x, y, z, 50, 0, 0, 0, .5, true);


    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        kill();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (entityHitResult.getEntity() instanceof LivingEntity target) {
            //TODO: deal with the damage
            target.hurt(DamageSource.MAGIC, damage);
            if (getOwner() instanceof LivingEntity livingEntity)
                livingEntity.heal(damage * 0.1f);

        }

    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    //https://forge.gemwire.uk/wiki/Particles
    public void spawnParticles() {
        if (level.isClientSide) {

            float width = (float) getBoundingBox().getXsize();
            float step = .25f;
            float radians = Mth.DEG_TO_RAD * getYRot();
            float speed = .1f;
            for (int i = 0; i < width / step; i++) {
//                double x = getX() + step * (i - width / step / 2);
//                double y = getY();
//                double z = getZ();
//
//                double rotX = x * Math.cos(radians) - z * Math.sin(radians);
//                double rotZ = z * Math.cos(radians) + x * Math.sin(radians);
                double x = getX();
                double y = getY();
                double z = getZ();
                double offset = step * (i - width / step / 2);
                double rotX = offset * Math.cos(radians);
                double rotZ = -offset * Math.sin(radians);

                double dx = Math.random() * speed * 2 - speed;
                double dy = Math.random() * speed * 2 - speed;
                double dz = Math.random() * speed * 2 - speed;
                //TODO: find out how to un-force these particles (the one with that argument seems to not be public)
                level.addParticle(ParticleHelper.BLOOD, false, x + rotX + dx, y + dy,z + rotZ + dz, dx, dy, dz);
            }
            //MagicManager.spawnParticles(level,ParticleRegistry.BLOOD_PARTICLE.get(), x, y, z, 5 + (int)(width * 5), width, 0, width, .1, false);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == getOwner())
            return false;
        return super.canHitEntity(entity);
    }

}
