package io.redspace.ironsspellbooks.entity.spells.firefly_swarm;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class FireflySwarmProjectile extends PathfinderMob implements AntiMagicSusceptible/*, NoKnockbackProjectile*/ {

    public FireflySwarmProjectile(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public FireflySwarmProjectile(Level level, @Nullable Entity owner, @Nullable Entity target, float damage) {
        this(EntityRegistry.FIREFLY_SWARM.get(), level);
        setOwner(owner);
        setTarget(target);
        this.damage = damage;
    }


    static final int maxLife = 10 * 20;
    public static final float radius = 2f;
    UUID targetUUID;
    Entity cachedTarget;
    UUID ownerUUID;
    Entity cachedOwner;
    Entity nextTarget;
    float damage;

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new FlyingPathNavigation(this, pLevel);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            for (int i = 0; i < 2; i++) {
                var motion = Utils.getRandomVec3(.05f).add(this.getDeltaMovement());
                var spawn = Utils.getRandomVec3(.25f);
                level.addParticle(ParticleHelper.FIREFLY, getX() + spawn.x, getY() + this.getBbHeight() * .5f + spawn.z, getZ() + spawn.z, motion.x, motion.y, motion.z);
            }
        }
        super.tick();
        if (this.tickCount > maxLife) {
            this.discard();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        LivingEntity target = getTarget();
        if (target != null) {
            this.navigation.moveTo(target, 7);
            //this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 5);
        }
        if (this.tickCount % 8 == 0) {
            if (level.collidesWithSuffocatingBlock(this, this.getBoundingBox().move(0, -1, 0))) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.02, 0));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.008, 0));
            }
        }
        if (!this.moveControl.hasWanted()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(.95f, 1, .95f));
        }
        if ((this.tickCount & 7) == 0) {
            float fade = 1 - Mth.clamp((tickCount - maxLife + 40) / (float) (maxLife), 0, 1f);
            this.playSound(SoundRegistry.FIREFLY_SWARM_IDLE.get(), .25f * fade, .95f + Utils.random.nextFloat() * .1f);
        }
        if (this.tickCount % 15 == 0) {
            //Damage tick
            float inflate = radius - this.getBbWidth() * .5f;
            this.level.getEntities(this, this.getBoundingBox().inflate(inflate), this::canHitEntity).forEach(
                    (entity) -> {
                        if (canHitEntity(entity)) {
                            boolean hit = DamageSources.applyDamage(entity, damage, SpellRegistry.FIREFLY_SWARM_SPELL.get().getDamageSource(this, getOwner()));
                            if (hit) {
                                this.playSound(SoundRegistry.FIREFLY_SWARM_ATTACK.get(), .75f, .9f + Utils.random.nextFloat() * .2f);
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
        pCompound.putInt("Age", this.tickCount);
        pCompound.putFloat("Damage", this.damage);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Target")) {
            this.targetUUID = pCompound.getUUID("Target");
        }
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
        this.tickCount = pCompound.getInt("Age");
        this.damage = pCompound.getFloat("Damage");
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}