package io.redspace.ironsspellbooks.entity.spells.firefly_swarm;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.registry.IronsSpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class FireflySwarmProjectile extends PathfinderMob {

    public FireflySwarmProjectile(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public FireflySwarmProjectile(Level level, @Nullable Entity owner, @Nullable Entity target) {
        this(EntityRegistry.FIREFLY_SWARM.get(), level);
        setOwner(owner);
        setTarget(target);
    }

    UUID targetUUID;
    Entity cachedTarget;
    UUID ownerUUID;
    Entity cachedOwner;
    Entity nextTarget;

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new FlyingPathNavigation(this, pLevel);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity target = getTarget();
        if (target != null) {
            this.navigation.moveTo(target, 5);
            //this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 5);
        }
        if(this.tickCount % 8 == 0) {
            if (level.collidesWithSuffocatingBlock(this, this.getBoundingBox().move(0, -1, 0))) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02, 0));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.008, 0));
            }
        }
        if (!this.moveControl.hasWanted()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(.95f, 1, .95f));
        }
        if (this.tickCount % 15 == 0) {
            //Damage tick
            this.level.getEntities(this, this.getBoundingBox().inflate(.75f), this::canHitEntity).forEach(
                    (entity) -> {
                        if (canHitEntity(entity)) {
                            boolean hit = DamageSources.applyDamage(entity, 2, IronsSpellRegistry.FIREFLY_SWARM_SPELL.get().getDamageSource(this, getOwner()), SchoolType.POISON);
                            if (hit) {
                                if (target == null) {
                                    setTarget(entity);
                                } else if (target != entity) {
                                    nextTarget = entity;
                                }
                            }
                        }
                    }
            );
            if (getTarget() == null || getTarget().isDeadOrDying()) {
                setTarget(nextTarget);
                if (nextTarget != null && nextTarget.isRemoved()) {
                    nextTarget = null;
                }
            }
        }
    }

    protected boolean canHitEntity(Entity target) {
        if (!target.isSpectator() && target.isAlive() && target.isPickable()) {
            Entity owner = this.getOwner();
            return owner != target && !DamageSources.isFriendlyFireBetween(owner, target);
        } else {
            return false;
        }
    }


    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public LivingEntity getTarget() {
        return getFireflyTarget() instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public void setTarget(@Nullable Entity target) {
        if (target != null) {
            this.targetUUID = target.getUUID();
            this.cachedTarget = target;
        }
    }

    @Nullable
    public Entity getFireflyTarget() {
        if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
            return this.cachedTarget;
        } else if (this.targetUUID != null && this.level instanceof ServerLevel) {
            this.cachedTarget = ((ServerLevel) this.level).getEntity(this.targetUUID);
            return this.cachedTarget;
        } else {
            return null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.targetUUID != null) {
            pCompound.putUUID("Target", this.targetUUID);
        }
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Target")) {
            this.targetUUID = pCompound.getUUID("Target");
        }
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
    }
}