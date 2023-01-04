package com.example.testmod.entity.blood_slash;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

//https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
//https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
//https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
//https://github.com/maximumpower55/Aura

public class BloodSlashProjectile extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(BloodSlashProjectile.class, EntityDataSerializers.FLOAT);
    private static final double SPEED = 1d;
    private static final int EXPIRE_TIME = 10 * 20;
    public final int animationSeed;
    private final float maxRadius;
    private EntityDimensions dimensions;
    public AABB oldDimensions;
    private int age;
    private float damage;

    public BloodSlashProjectile(EntityType<? extends BloodSlashProjectile> entityType, Level level) {
        super(entityType, level);
        animationSeed = level.random.nextInt(9999);

        float initialRadius = 2;
        maxRadius = 6;
        dimensions = EntityDimensions.scalable(initialRadius, 0.5f);

        oldDimensions = getBoundingBox();
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
        oldDimensions = getBoundingBox();
        setRadius(getRadius() + 0.2f);
        //TODO: replace all this with custom hit detection
        if (!level.isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                onHit(hitresult);
            }
            //spawnParticles();
        }
        List<Entity> collisions = Lists.newArrayList();
        collisions.addAll(level.getEntities(this, this.getBoundingBox()));

        collisions = collisions.stream().filter(target ->
                target != getOwner() && target instanceof LivingEntity).collect(Collectors.toList());
        for (Entity entity : collisions) {
            TestMod.LOGGER.info(entity.getName().getString());
        }
        setPos(position().add(getDeltaMovement()));
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

        MagicManager.spawnParticles(level, ParticleTypes.REVERSE_PORTAL, x, y, z, 50, 0, 0, 0, .5, true);


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
        if (!level.isClientSide) {
            double x = getX();
            double y = getY();
            double z = getZ();
            MagicManager.spawnParticles(level, ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, .1, true);

        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity == getOwner())
            return false;
        return super.canHitEntity(entity);
    }

}
