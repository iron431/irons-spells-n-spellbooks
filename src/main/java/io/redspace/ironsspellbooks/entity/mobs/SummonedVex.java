package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class SummonedVex extends Vex implements MagicSummon {
    //private static final EntityDataAccessor<Optional<UUID>> DATA_ID_SUMMONER_UUID = SynchedEntityData.defineId(SummonedVex.class, EntityDataSerializers.OPTIONAL_UUID);

    protected LivingEntity cachedSummoner;
    protected UUID summonerUUID;

    public SummonedVex(EntityType<? extends Vex> pEntityType, Level pLevel) {
        super(EntityRegistry.SUMMONED_VEX.get(), pLevel);
        xpReward = 0;
    }

    public SummonedVex(Level pLevel, LivingEntity owner) {
        this(EntityRegistry.SUMMONED_VEX.get(), pLevel);
        setSummoner(owner);
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new VexChargeAttackGoal());
        this.goalSelector.addGoal(7, new GenericFollowOwnerGoal(this, this::getSummoner, .65f, 35, 10, true, 50));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(16, new VexRandomMoveGoal());

        this.targetSelector.addGoal(1, new GenericOwnerHurtByTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(2, new GenericOwnerHurtTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(3, new GenericCopyOwnerTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(4, (new GenericHurtByTargetGoal(this, (entity) -> entity == getSummoner())).setAlertOthers());

    }

    @Override
    public boolean isPreventingPlayerRest(Player pPlayer) {
        return !this.isAlliedTo(pPlayer);
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        return Utils.doMeleeAttack(this, pEntity, SpellRegistry.SUMMON_VEX_SPELL.get().getDamageSource(this, getSummoner()));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (shouldIgnoreDamage(pSource))
            return false;
        return super.hurt(pSource, pAmount);
    }

    @Override
    public LivingEntity getSummoner() {
        return OwnerHelper.getAndCacheOwner(level(), cachedSummoner, summonerUUID);
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        this.onDeathHelper();
        super.die(pDamageSource);
    }

    @Override
    public void onRemovedFromWorld() {
        this.onRemovedHelper(this, MobEffectRegistry.VEX_TIMER.get());
        super.onRemovedFromWorld();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.summonerUUID = OwnerHelper.deserializeOwner(compoundTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        OwnerHelper.serializeOwner(compoundTag, summonerUUID);
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || this.isAlliedHelper(pEntity);
    }

    public void onUnSummon() {
        if (!level().isClientSide) {
            MagicManager.spawnParticles(level(), ParticleTypes.POOF, getX(), getY(), getZ(), 25, .4, .8, .4, .03, false);
            discard();
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    class VexChargeAttackGoal extends Goal {
        public VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = SummonedVex.this.getTarget();
            if (livingentity != null && livingentity.isAlive() && !SummonedVex.this.getMoveControl().hasWanted() && SummonedVex.this.random.nextInt(reducedTickDelay(7)) == 0) {
                return SummonedVex.this.distanceToSqr(livingentity) > 4.0D;
            } else {
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return SummonedVex.this.getMoveControl().hasWanted() && SummonedVex.this.isCharging() && SummonedVex.this.getTarget() != null && SummonedVex.this.getTarget().isAlive();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            LivingEntity livingentity = SummonedVex.this.getTarget();
            if (livingentity != null) {
                Vec3 vec3 = livingentity.getEyePosition();
                SummonedVex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
            }

            SummonedVex.this.setIsCharging(true);
            SummonedVex.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            SummonedVex.this.setIsCharging(false);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            LivingEntity livingentity = SummonedVex.this.getTarget();
            if (livingentity != null) {
                if (SummonedVex.this.getBoundingBox().intersects(livingentity.getBoundingBox())) {
                    SummonedVex.this.doHurtTarget(livingentity);
                    SummonedVex.this.setIsCharging(false);
                } else {
                    double d0 = SummonedVex.this.distanceToSqr(livingentity);
                    if (d0 < 9.0D) {
                        Vec3 vec3 = livingentity.getEyePosition();
                        SummonedVex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
                    }
                }

            }
        }
    }

    class VexRandomMoveGoal extends Goal {
        /**
         * Copy of private random move goal in vex
         */
        public VexRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return !SummonedVex.this.getMoveControl().hasWanted() && SummonedVex.this.random.nextInt(reducedTickDelay(7)) == 0;
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return false;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            BlockPos blockpos = SummonedVex.this.getBoundOrigin();
            if (blockpos == null) {
                blockpos = SummonedVex.this.blockPosition();
            }

            for (int i = 0; i < 3; ++i) {
                BlockPos blockpos1 = blockpos.offset(SummonedVex.this.random.nextInt(15) - 7, SummonedVex.this.random.nextInt(11) - 5, SummonedVex.this.random.nextInt(15) - 7);
                if (SummonedVex.this.level().isEmptyBlock(blockpos1)) {
                    SummonedVex.this.moveControl.setWantedPosition((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 0.25D);
                    if (SummonedVex.this.getTarget() == null) {
                        SummonedVex.this.getLookControl().setLookAt((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}
