package io.redspace.ironsspellbooks.entity.spells;

import io.netty.util.internal.UnstableApi;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractMagicProjectile extends Projectile implements AntiMagicSusceptible, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> DATA_CURSOR_HOMING = SynchedEntityData.defineId(AbstractMagicProjectile.class, EntityDataSerializers.BOOLEAN);
    // todo: also working on blocks would be cool
    private static final EntityDataAccessor<Boolean> DATA_RICOCHET = SynchedEntityData.defineId(AbstractMagicProjectile.class, EntityDataSerializers.BOOLEAN);
    /**
     * Indicates remaining targets able to be pierced. Default: 0 (No piercing). -1 indicates infinite piercing. Positive values indicate amount of pierce-ings left
     */
    private static final EntityDataAccessor<Integer> DATA_PIERCE_LEVEL = SynchedEntityData.defineId(AbstractMagicProjectile.class, EntityDataSerializers.INT);

    protected static final int EXPIRE_TIME = 15 * 20;
    protected float damage;
    protected float explosionRadius;

    @Nullable
    protected Entity cachedHomingTarget;
    @Nullable
    protected UUID homingTargetUUID;

    /**
     * Client Side, called every tick
     */
    public abstract void trailParticles();

    /**
     * Server Side, called alongside onHit()
     */
    public abstract void impactParticles(double x, double y, double z);

    public abstract float getSpeed();

    public abstract Optional<Holder<SoundEvent>> getImpactSound();

    public AbstractMagicProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(getSpeed()));
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        var owner = getOwner();
        return super.canHitEntity(pTarget) && pTarget != owner && (owner == null || !owner.isAlliedTo(pTarget));
    }

    @Override
    public void checkDespawn() {
        if (this.level instanceof ServerLevel serverLevel && !serverLevel.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(this.chunkPosition().toLong())) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        // prevent first-tick flicker due to deltaMoveOld being "uninitialized" on our first tick
        if (tickCount == 1) {
            deltaMovementOld = getDeltaMovement();
        }
        if (tickCount > EXPIRE_TIME) {
            discard();
            return;
        }
        if (level.isClientSide) {
            trailParticles();
        }
        handleEntityHoming();
        handleCursorHoming();
        handleHitDetection();
        travel();
        deltaMovementOld = getDeltaMovement();
        rotateWithMotion();
    }

    protected void rotateWithMotion() {
        var motion = getDeltaMovement();
        double speed = motion.horizontalDistance();
        this.setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(motion.y, speed) * Mth.RAD_TO_DEG));
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            // handle first tick/null rotation state
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        } else {
            this.xRotO = enforceRotationContinuity(this.xRotO, this.getXRot());
            this.yRotO = enforceRotationContinuity(this.yRotO, this.getYRot());
        }
    }


    /**
     * @return Modifies currentRotation to be continuous with targetRotation. <br>
     * Ie, -179 and 179 are two degrees apart, but using these raw values as rot and rotO will cause artifacts
     * <br>
     * This should enforce these values to be continuous
     */
    protected static float enforceRotationContinuity(float currentRotation, float targetRotation) {
        while (targetRotation - currentRotation < -180.0F) {
            currentRotation -= 360.0F;
        }

        while (targetRotation - currentRotation >= 180.0F) {
            currentRotation += 360.0F;
        }

        return currentRotation;
    }

    public Vec3 deltaMovementOld = Vec3.ZERO;

    public void handleHitDetection() {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult instanceof EntityHitResult entityHitResult) {
            // fix dumb hit location of entity hit results
            hitresult = new EntityHitResult(entityHitResult.getEntity(), entityHitResult.getEntity().getBoundingBox().clip(this.position(), this.position().add(this.getDeltaMovement())).orElse(this.position()));
        }
        if (hitresult.getType() != HitResult.Type.MISS && !NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(this, hitresult)).isCanceled()) {
            onHit(hitresult);
        }
    }

    public void travel() {
        setPos(position().add(getDeltaMovement()));
        Vec3 motion = this.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        this.setXRot(Mth.wrapDegrees(xRot));
        this.setYRot(Mth.wrapDegrees(yRot));
        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - getDefaultGravity(), vec34.z);
        }
    }

    public void stopEntityHoming() {
        homingTargetUUID = null;
        cachedHomingTarget = null;
    }

    protected void handleEntityHoming() {
        if (homingTargetUUID == null) {
            return;
        }
        var target = getHomingTarget();
        if (target == null) {
            homingTargetUUID = null;
            return;
        }
        if (target.isRemoved()) {
            stopEntityHoming();
            return;
        }
        Vec3 wantedPos = target.getBoundingBox().getCenter().add(target.getDeltaMovement());
        Vec3 newMotion = homeTowards(wantedPos, 0.22f);
        if (newMotion.dot(wantedPos.subtract(this.position())) < -.25 && this.tickCount > 10) {
            // after a decent bit into our flight, if we are significantly past our target, lose tracking
            stopEntityHoming();
        }
    }

    protected void handleCursorHoming() {
        var cursorHoming = isCursorHoming();
        if (!cursorHoming) {
            return;
        }
        float maxRange = 48;
        var owner = getOwner();
        if (owner == null || position().distanceToSqr(owner.position()) > maxRange * maxRange) {
            setCursorHoming(false);
            return;
        }
        Vec3 start = owner.getEyePosition();
        Vec3 end = start.add(owner.getForward().scale(maxRange));
        HitResult hitresult = Utils.raycastForEntity(level, owner, start, end, true, 0.5f, entity -> Utils.canHitWithRaycast(entity) && !DamageSources.isFriendlyFireBetween(entity, owner));
        Vec3 target = hitresult instanceof EntityHitResult entityHit ? entityHit.getEntity().getBoundingBox().getCenter() : hitresult.getLocation();
        homeTowards(target, 0.18f);
    }

    /**
     * Pull motion of projectile towards a global position. A strength of 1.0 will point the projectile towards the destination in one tick.
     *
     * @return New Motion
     */
    protected Vec3 homeTowards(Vec3 target, float strength) {
        var speed = this.getDeltaMovement().length();
        var currentMotion = this.getDeltaMovement().normalize();
        var wantedMotion = target.subtract(this.position()).normalize();
        var newMotion = Utils.slerp(strength, currentMotion, wantedMotion).scale(speed);
        this.setDeltaMovement(newMotion);
        return newMotion;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult hitresult) {
        super.onHit(hitresult);
        if (canRicochet()) {
            doRicochet(hitresult);
        }
        if (!level.isClientSide) {
            var vec = hitresult.getLocation();
            impactParticles(vec.x, vec.y, vec.z);
            getImpactSound().ifPresent(this::doImpactSound);
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return super.shouldBeSaved() && !Objects.equals(getRemovalReason(), RemovalReason.UNLOADED_TO_CHUNK);
    }

    protected void doImpactSound(Holder<SoundEvent> sound) {
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, .9f + Utils.random.nextFloat() * .2f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_CURSOR_HOMING, false);
        pBuilder.define(DATA_RICOCHET, false);
        pBuilder.define(DATA_PIERCE_LEVEL, 0);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.impactParticles(getX(), getY(), getZ());
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.getDamage());
        if (explosionRadius != 0) {
            tag.putFloat("ExplosionRadius", explosionRadius);
        }
        if (getPierceLevel() != 0) {
            tag.putInt("PierceLevel", getPierceLevel());
        }
        if (this.homingTargetUUID != null) {
            tag.putUUID("homingTarget", homingTargetUUID);
        }
        if (canRicochet()) {
            tag.putBoolean("ricochet", true);
        }
        tag.putInt("Age", tickCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        if (tag.contains("ExplosionRadius")) {
            this.explosionRadius = tag.getFloat("ExplosionRadius");
        }
        if (tag.contains("PierceLevel")) {
            this.setPierceLevel(tag.getInt("PierceLevel"));
        }
        if (tag.contains("homingTarget", 11)) {
            this.homingTargetUUID = tag.getUUID("homingTarget");
        }
        if (tag.contains("ricochet")) {
            setCanRicochet(tag.getBoolean("ricochet"));
        }
        this.tickCount = tag.getInt("Age");
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!shouldPierceShields() && (pResult.getEntity() instanceof ShieldPart || pResult.getEntity() instanceof AbstractShieldEntity)) {
            // simulate block impact (likely destroying projectile) due to magic shield impact
            this.onHitBlock(new BlockHitResult(pResult.getEntity().position(), Direction.fromYRot(this.getYRot()), pResult.getEntity().blockPosition(), false));
        }
    }

    /**
     * Useful for {@link Projectile#onHit(HitResult)}, will discard if block impact, or {@link AbstractMagicProjectile#pierceOrDiscard()} on entity impact
     */
    @UnstableApi
    public void discardHelper(HitResult hitresult) {
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            pierceOrDiscard();
        } else {
            discard();
        }
    }

    @UnstableApi
    public void pierceOrDiscard() {
        int p = getPierceLevel();
        if (p > 0) {
            setPierceLevel(p - 1);
        } else if (p == 0) {
            discard();
        }
    }

    @UnstableApi
    public void doRicochet(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Vec3 deltaMovement = getDeltaMovement();
            Vec3 vec = deltaMovement.normalize();
            Entity owner = getOwner();
            Entity hit = entityHitResult.getEntity();
            List<Entity> potentialTargets = level.getEntities(this, this.getBoundingBox().inflate(3).expandTowards(deltaMovement.scale(12)),
                    entity -> entity != hit && (
                            (owner == null || !Utils.shouldHealEntity(owner, entity))
                                    || entity.getClass() == hit.getClass()
                    ) && entity.getBoundingBox().getCenter().subtract(position()).normalize().dot(vec) > 0.6 && Utils.hasLineOfSight(level, this, entity, false));
            if (potentialTargets.isEmpty()) {
                return;
            }
            Entity target = potentialTargets.get(this.getId() % potentialTargets.size()); // use deterministic random to keep client and server in sync
            setDeltaMovement(target.getBoundingBox().getCenter().subtract(this.position()).normalize().scale(deltaMovement.length()));
        } else {
            //todo: block ricochet?
        }
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

    public int getPierceLevel() {
        return entityData.get(DATA_PIERCE_LEVEL);
    }

    public void setPierceLevel(int pierceLevel) {
        entityData.set(DATA_PIERCE_LEVEL, pierceLevel);
    }

    public void setInfinitePiercing() {
        setPierceLevel(-1);
    }

    @Nullable
    public Entity getHomingTarget() {
        if (this.cachedHomingTarget != null && !this.cachedHomingTarget.isRemoved()) {
            return this.cachedHomingTarget;
        } else if (this.homingTargetUUID != null && this.level instanceof ServerLevel) {
            this.cachedHomingTarget = ((ServerLevel) this.level).getEntity(this.homingTargetUUID);
            return this.cachedHomingTarget;
        } else {
            return null;
        }
    }

    public void setHomingTarget(LivingEntity entity) {
        this.homingTargetUUID = entity.getUUID();
        this.cachedHomingTarget = entity;
        setCursorHoming(false);
    }

    public boolean isCursorHoming() {
        return entityData.get(DATA_CURSOR_HOMING);
    }

    public void setCursorHoming(boolean cursorHoming) {
        entityData.set(DATA_CURSOR_HOMING, cursorHoming);
        if (cursorHoming) {
            stopEntityHoming();
        }
    }

    @UnstableApi
    public boolean canRicochet() {
        return entityData.get(DATA_RICOCHET);
    }

    @UnstableApi
    public void setCanRicochet(boolean ricochet) {
        entityData.set(DATA_RICOCHET, ricochet);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /**
     * Whether the projectile should treat magic shields as a block impact
     */
    protected boolean shouldPierceShields() {
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        var owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        var homingTarget = getHomingTarget();
        buffer.writeInt(homingTarget == null ? 0 : homingTarget.getId());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        Entity owner = this.level.getEntity(additionalData.readInt());
        if (owner != null) {
            this.setOwner(owner);
        }
        Entity homingTarget = this.level.getEntity(additionalData.readInt());
        if (homingTarget != null) {
            this.cachedHomingTarget = homingTarget;
            this.homingTargetUUID = homingTarget.getUUID();
        }
    }
}
