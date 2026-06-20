package io.redspace.skillcasting.api;

import io.netty.util.internal.UnstableApi;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AbstractSkillProjectile extends Projectile implements ISkillProjectile, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> DATA_CURSOR_HOMING = SynchedEntityData.defineId(AbstractSkillProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_RICOCHET = SynchedEntityData.defineId(AbstractSkillProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PIERCE_LEVEL = SynchedEntityData.defineId(AbstractSkillProjectile.class, EntityDataSerializers.INT);

    protected static final int EXPIRE_TIME = 15 * 20;

    protected float damage;
    protected float radius;

    @Nullable
    protected Entity cachedHomingTarget;
    @Nullable
    protected UUID homingTargetUUID;

    /**
     * Client Side, called every tick
     */
    public void trailParticles() {
    }

    /**
     * Server Side, called alongside onHit()
     */
    public void impactParticles(double x, double y, double z) {
    }

    public Optional<PlayableSound> getImpactSound() {
        return Optional.empty();
    }

    public AbstractSkillProjectile(EntityType<? extends AbstractSkillProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getPierceLevel() {
        return entityData.get(DATA_PIERCE_LEVEL);
    }

    @Override
    public void setPierceLevel(int pierceLevel) {
        entityData.set(DATA_PIERCE_LEVEL, pierceLevel);
    }

    @Override
    public void setProjectileSpeed(float speed) {
        if (this.getDeltaMovement().lengthSqr() > 0.001) {
            this.setDeltaMovement(this.getDeltaMovement().normalize().scale(speed));
        }
    }

    /**
     * fixme: this no longer works with the projectile speed pipeline
     */
    @Deprecated(forRemoval = true)
    public void shoot(Vec3 trajectory) {
        this.setDeltaMovement(trajectory);
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

    @Override
    public void setHomingTarget(@NotNull Entity entity) {
        this.homingTargetUUID = entity.getUUID();
        this.cachedHomingTarget = entity;
        setCursorHoming(false);
    }

    public boolean isCursorHoming() {
        return entityData.get(DATA_CURSOR_HOMING);
    }

    @Override
    public void setCursorHoming(boolean cursorHoming) {
        entityData.set(DATA_CURSOR_HOMING, cursorHoming);
        if (cursorHoming) {
            stopEntityHoming();
        }
    }

    public int getRicochetLevel() {
        return entityData.get(DATA_RICOCHET);
    }

    @Override
    public void setRicochetLevel(int ricochetLevel) {
        entityData.set(DATA_RICOCHET, ricochetLevel);
    }

    public void setInfinitePiercing() {
        setPierceLevel(-1);
    }

    @UnstableApi
    public void setInfiniteRicocheting() {
        setRicochetLevel(-1);
    }

    protected static Optional<PlayableSound> impactSound(Holder<SoundEvent> sound) {
        return Optional.of(PlayableSound.of(sound, 2f, 0.9f, 1.1f));
    }

    protected static Optional<PlayableSound> impactSound(SoundEvent sound) {
        return Optional.of(PlayableSound.of(sound, 2f, 0.9f, 1.1f));
    }

    public boolean canRicochet() {
        return !getType().is(ModTags.CANT_RICOCHET) && getRicochetLevel() != 0;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        var owner = getOwner();
        return super.canHitEntity(pTarget) && pTarget != owner && (owner == null || !owner.isAlliedTo(pTarget));
    }

    @Override
    public void checkDespawn() {
        if (this.level instanceof ServerLevel serverLevel && !serverLevel.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(this.chunkPosition().toLong())) {
            // prevent projectiles shot off into the distance from being saved to disk. i think.
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
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
     * Ie, -179 and 179 are two degrees apart, but interpolating between them yields an incorrect result
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

    public float getHitDetectionInflation() {
        return 0.3f;
    }

    public void handleHitDetection() {
        Vec3 position = position();
        Vec3 destination = position.add(getDeltaMovement());
        HitResult blockCollision = level.clip(new ClipContext(position, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (collidesWithBlocks() && blockCollision.getType() != HitResult.Type.MISS) {
            destination = blockCollision.getLocation();
        }
        List<HitResult> entities = raycastForEntitiesAlongPath(destination, position);
        for (HitResult hitResult : entities) {
            if (!(hitResult instanceof EntityHitResult entityHitResult)) {
                continue;
            }
            if (entityHitResult.getType() != HitResult.Type.MISS && !NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(this, entityHitResult)).isCanceled()) {
                onHit(entityHitResult);
            }
            if (this.isRemoved()) {
                break;
            }
        }
        if (blockCollision.getType() != HitResult.Type.MISS) {
            onHit(blockCollision);
        }
    }

    /**
     * @return A list of hit results long the vector between destination and position, sorted by distance to the current position
     */
    protected List<HitResult> raycastForEntitiesAlongPath(Vec3 destination, Vec3 position) {
        AABB range = this.getBoundingBox().expandTowards(destination.subtract(position)).inflate(0.1);
        List<HitResult> hits = new ArrayList<>();
        List<Entity> hitEntities = new ArrayList<>(); // prevents large hitbox entities from registering multiple hits in one tick
        List<? extends Entity> entities = level.getEntities(this, range, this::canHitEntity);
        for (Entity target : entities) {
            if (hitEntities.contains(target)) {
                continue;
            }
            HitResult hit = Utils.checkEntityIntersecting(target, position, destination, getHitDetectionInflation());
            if (hit.getType() != HitResult.Type.MISS) {
                hits.add(hit);
                hitEntities.add(target);
            }
        }

        if (!hits.isEmpty()) {
            hits.sort(Comparator.comparingDouble(o -> o.getLocation().distanceToSqr(position)));
        }
        return hits;
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
        // todo: expose parameter
        float maxRange = 48;
        var owner = getOwner();
        if (owner == null || position().distanceToSqr(owner.position()) > maxRange * maxRange) {
            setCursorHoming(false);
            return;
        }
        Vec3 start = owner.getEyePosition();
        Vec3 end = start.add(owner.getForward().scale(maxRange));
        HitResult hitresult = RaycastBuilder.begin(level, owner)
                .start(start)
                .end(end)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .filter(entity -> Utils.canHitWithRaycast(entity) && !DamageSources.isFriendlyFireBetween(entity, owner))
                .build();
        Vec3 target = hitresult instanceof EntityHitResult entityHit ? entityHit.getEntity().getBoundingBox().getCenter() : hitresult.getLocation();
        // todo: expose parameter
        homeTowards(target, 0.18f);
    }

    /**
     * Pull motion of projectile towards a global position.
     *
     * @param strength percent interpolation per tick. A strength of 1.0 will take 1 tick to point directly at the target
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

    public boolean collidesWithBlocks() {
        return true;
    }

    @Override
    protected void onHit(@NotNull HitResult hitresult) {
        super.onHit(hitresult);
        if (!level.isClientSide && hitresult.getType() != HitResult.Type.MISS) {
            if (hitresult.getType() != HitResult.Type.BLOCK || collidesWithBlocks()) {
                var vec = hitresult.getLocation();
                impactParticles(vec.x, vec.y, vec.z);
                getImpactSound().ifPresent(sound -> level.playSound(null, vec.x, vec.y, vec.z, sound.soundEventHolder(), SoundSource.NEUTRAL, sound.volume(), sound.samplePitch(level.random)));
            }
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return super.shouldBeSaved() && !Objects.equals(getRemovalReason(), RemovalReason.UNLOADED_TO_CHUNK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_CURSOR_HOMING, false);
        pBuilder.define(DATA_RICOCHET, 0);
        pBuilder.define(DATA_PIERCE_LEVEL, 0);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.getDamage());
        if (radius != 0) {
            tag.putFloat("Radius", radius);
        }
        if (getPierceLevel() != 0) {
            tag.putInt("PierceLevel", getPierceLevel());
        }
        if (this.homingTargetUUID != null) {
            tag.putUUID("homingTarget", homingTargetUUID);
        }
        if (getRicochetLevel() != 0) {
            tag.putInt("RicochetLevel", getRicochetLevel());
        }
        tag.putInt("Age", tickCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        if (tag.contains("Radius")) {
            this.radius = tag.getFloat("Radius");
        }
        if (tag.contains("PierceLevel")) {
            this.setPierceLevel(tag.getInt("PierceLevel"));
        }
        if (tag.contains("homingTarget", 11)) {
            this.homingTargetUUID = tag.getUUID("homingTarget");
        }
        if (tag.contains("RicochetLevel")) {
            setRicochetLevel(tag.getInt("RicochetLevel"));
        }
        this.tickCount = tag.getInt("Age");
    }

    /**
     * Performs any post-entity hit handling, such as piercing or ricocheting. If no continuations are available (all exhausted), projectile is discarded based on <code>discardWhenExhausted</code>
     */
    protected void consumeEntityImpact(EntityHitResult hit, boolean discardWhenExhausted) {
        if (this.isRemoved()) {
            return;
        }
        if (tryRedirectFromEntityRicochet(hit)) {
            return;
        }
        if (discardWhenExhausted) {
            // a pierce only pipeline might be useful, but not discarding on impact is effectively just piercing
            // mainly here for future expansion structuring
            pierceOrDiscard();
        }
    }

    @Deprecated
    protected float getBaseSpeed() {
        return 1f;
    }

    /**
     * Useful for {@link Projectile#onHit(HitResult)}, will discard if block impact, or {@link AbstractSkillProjectile#consumeEntityImpact(EntityHitResult hit, boolean discardWhenExhausted)} on entity impact
     */
    @UnstableApi
    public void discardHelper(HitResult hitresult) {
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            consumeEntityImpact((EntityHitResult) hitresult, true);
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

    /**
     * @return true if velocity was updated toward a new entity target. Consumes ricochet charges.
     */
    private boolean tryRedirectFromEntityRicochet(EntityHitResult entityHitResult) {
        if (!canRicochet()) {
            return false;
        }
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 vec = deltaMovement.normalize();
        Entity owner = getOwner();
        Entity hit = entityHitResult.getEntity();
        List<Entity> potentialTargets = level.getEntities(this, this.getBoundingBox().inflate(3).expandTowards(vec.scale(16)),
                entity -> entity != hit && (
                        (owner == null || !Utils.shouldHealEntity(owner, entity))
                                || entity.getClass() == hit.getClass()
                ) && entity.canBeHitByProjectile() && entity.getBoundingBox().getCenter().subtract(position()).normalize().dot(vec) > 0.6 && Utils.hasLineOfSight(level, this, entity, false));
        if (potentialTargets.isEmpty()) {
            return false;
        }
        potentialTargets.sort(Comparator.comparing(entity -> entity.distanceToSqr(this)));
        Entity target = potentialTargets.get((this.getId() % potentialTargets.size()) % 3); // use deterministic random to keep client and server in sync. limit to closest 3.
        setDeltaMovement(target.getBoundingBox().getCenter().subtract(this.position()).normalize().scale(deltaMovement.length()));
        consumeRicochetCharge();
        return true;
    }

    private void consumeRicochetCharge() {
        int r = getRicochetLevel();
        if (r > 0) {
            setRicochetLevel(r - 1);
            //todo: ye or ne? ne for now, cringe hidden mechanic
//            damage *= 0.85f;
//            explosionRadius *= 0.85f;
        }
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
