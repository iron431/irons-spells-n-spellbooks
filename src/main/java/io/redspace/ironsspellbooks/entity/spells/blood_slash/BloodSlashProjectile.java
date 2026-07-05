package io.redspace.ironsspellbooks.entity.spells.blood_slash;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BloodSlashProjectile extends AbstractMagicProjectile {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(BloodSlashProjectile.class, EntityDataSerializers.FLOAT);
    private static final float SPEED = 1f;
    public final int animationSeed;
    private final float maxRadius;
    public AABB oldBB;
    private int age;
    public int animationTime;
    private List<Entity> victims;

    public BloodSlashProjectile(EntityType<? extends BloodSlashProjectile> entityType, Level level) {
        super(entityType, level);
        animationSeed = Utils.random.nextInt(9999);

        setRadius(.6f);
        maxRadius = 3;
        oldBB = getBoundingBox();
        victims = new ArrayList<>();
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

    @Override
    public void trailParticles() {
        float width = (float) getBoundingBox().getXsize();
        float step = .25f;
        float radians = Mth.DEG_TO_RAD * getYRot();
        float speed = .1f;
        for (int i = 0; i < width / step; i++) {
            double x = getX();
            double y = getY();
            double z = getZ();
            double offset = step * (i - width / step / 2);
            double rotX = offset * Math.cos(radians);
            double rotZ = -offset * Math.sin(radians);

            double dx = Math.random() * speed * 2 - speed;
            double dy = Math.random() * speed * 2 - speed;
            double dz = Math.random() * speed * 2 - speed;
            level.addParticle(ParticleHelper.BLOOD, false, x + rotX + dx, y + dy, z + rotZ + dz, dx, dy, dz);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return SPEED;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_RADIUS, 0.5F);
    }

    @Override
    public void handleHitDetection() {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitresult);
        }
        if(!level.isClientSide) {
            var entities = level.getEntities(this, this.getBoundingBox()).stream().filter(target -> canHitEntity(target) && !victims.contains(target)).collect(Collectors.toSet());
            if (!entities.isEmpty()) {
                Entity last = null;
                for (Entity entity : entities) {
                    damageEntity(entity);
                    MagicManager.spawnParticles(level, ParticleHelper.BLOOD, entity.getX(), entity.getY(), entity.getZ(), 50, 0, 0, 0, .5, true);
                    last = entity;
                    if (entity instanceof ShieldPart || entity instanceof AbstractShieldEntity) {
                        discard();
                        return;
                    }
                }
                tryRedirectFromEntityRicochet(new EntityHitResult(last));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        oldBB = getBoundingBox();
        setRadius(getRadius() + 0.12f);
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


    public EntityDimensions getDimensions(Pose p_19721_) {
        //irons_spellbooks.LOGGER.info("Accessing Blood Slash Dimensions. Age: {}", age);
        this.getBoundingBox();
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_19729_) {
        //irons_spellbooks.LOGGER.info("onSynchedDataUpdated");

        if (DATA_RADIUS.equals(p_19729_)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(p_19729_);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();
    }

    private void damageEntity(Entity entity) {
        if (!victims.contains(entity)) {
            DamageSources.applyDamage(entity, damage, SpellRegistry.BLOOD_SLASH_SPELL.get().getDamageSource(this, getOwner()));
            victims.add(entity);
        }
    }
}
