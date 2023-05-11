package io.redspace.ironsspellbooks.entity.mobs.summoned_frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class SummonedFrog extends Frog implements MagicSummon {
    public SummonedFrog(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected LivingEntity cachedSummoner;
    protected UUID summonerUUID;

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        var brain = (Brain<Frog>) super.makeBrain(pDynamic);
        brain.removeAllBehaviors();
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)));
        initTongueActivity(brain);
        initJumpActivity(brain);

        return brain;
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        //this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2f, true));
        this.goalSelector.addGoal(7, new FrogFollowerOwnerGoal(this, this::getSummoner, 0.9f, 15, 5, 25));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new GenericOwnerHurtByTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(2, new GenericOwnerHurtTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(3, new GenericCopyOwnerTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(4, (new GenericHurtByTargetGoal(this, (entity) -> entity == getSummoner())).setAlertOthers());

    }



    @Override
    public LivingEntity getSummoner() {
        return OwnerHelper.getAndCacheOwner(level, cachedSummoner, summonerUUID);
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
        this.onRemovedHelper(this, MobEffectRegistry.POLAR_BEAR_TIMER.get());
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
    public boolean doHurtTarget(Entity pEntity) {
        return Utils.doMeleeAttack(this, pEntity, SpellType.SUMMON_POLAR_BEAR_SPELL.getDamageSource(this, getSummoner()), null);
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || this.isAlliedHelper(pEntity);
    }

    @Override
    public void onUnSummon() {
        if (!level.isClientSide) {
            MagicManager.spawnParticles(level, ParticleTypes.POOF, getX(), getY(), getZ(), 25, .4, .8, .4, .03, false);
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (shouldIgnoreDamage(pSource))
            return false;
        return super.hurt(pSource, pAmount);
    }


    class SummonAttackablesSensor extends NearestVisibleLivingEntitySensor{

        @Override
        protected boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget) {
            return false;
        }

        @Override
        protected MemoryModuleType<LivingEntity> getMemory() {
            return  MemoryModuleType.NEAREST_ATTACKABLE;
        }
    }
    class FrogFollowerOwnerGoal extends Goal {

        private final PathfinderMob entity;
        private LivingEntity owner;
        private final LevelReader level;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;
        private final OwnerGetter ownerGetter;
        private final float teleportDistance;

        public FrogFollowerOwnerGoal(PathfinderMob entity, OwnerGetter ownerGetter, double pSpeedModifier, float pStartDistance, float pStopDistance, float teleportDistance) {
            this.entity = entity;
            this.ownerGetter = ownerGetter;
            this.level = entity.level;
            this.speedModifier = pSpeedModifier;
            this.navigation = entity.getNavigation();
            this.startDistance = pStartDistance;
            this.stopDistance = pStopDistance;
            this.teleportDistance = teleportDistance * teleportDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = this.ownerGetter.get();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.entity.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else {
                return !(this.entity.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.entity.getPathfindingMalus(BlockPathTypes.WATER);
            this.entity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.entity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            this.entity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.entity.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (!this.entity.isLeashed() && !this.entity.isPassenger()) {
                    if (this.entity.distanceToSqr(this.owner) >= teleportDistance) {
                        this.teleportToOwner();
                    } else {
                        this.navigation.moveTo(this.owner, this.speedModifier);
                    }

                }
            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = this.owner.blockPosition();

            for (int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean maybeTeleportTo(int pX, int pY, int pZ) {
            if (Math.abs((double) pX - this.owner.getX()) < 2.0D && Math.abs((double) pZ - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
                return false;
            } else {
                this.entity.moveTo((double) pX + 0.5D, pY, (double) pZ + 0.5D, this.entity.getYRot(), this.entity.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pPos) {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pPos.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pPos.below());
                if (blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pPos.subtract(this.entity.blockPosition());
                    return this.level.noCollision(this.entity, this.entity.getBoundingBox().move(blockpos));
                }
            }
        }

        private int randomIntInclusive(int pMin, int pMax) {
            return this.entity.getRandom().nextInt(pMax - pMin + 1) + pMin;
        }
    }
//    private static void initIdleActivity(Brain<Frog> pBrain) {
//        pBrain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), UniformInt.of(30, 60))), Pair.of(0, new AnimalMakeLove(EntityType.FROG, 1.0F)), Pair.of(1, new FollowTemptation((p_218585_) -> {
//            return 1.25F;
//        })), Pair.of(2, new StartAttacking<>(pTarget -> true, (p_218605_) -> {
//            return p_218605_.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
//        })), Pair.of(3, new TryFindLand(6, 1.0F)), Pair.of(4, new RunOne<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(new RandomStroll(1.0F), 1), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 1), Pair.of(new Croak(), 3), Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(5, 20)), 2))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)));
//    }
//
//    private static void initSwimActivity(Brain<Frog> pBrain) {
//        pBrain.addActivityWithConditions(Activity.SWIM, ImmutableList.of(Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), UniformInt.of(30, 60))), Pair.of(1, new FollowTemptation((p_218574_) -> {
//            return 1.25F;
//        })), Pair.of(2, new StartAttacking<>(pTarget -> true, (p_218601_) -> {
//            return p_218601_.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
//        })), Pair.of(3, new TryFindLand(8, 1.5F)), Pair.of(5, new GateBehavior<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of(Pair.of(new RandomSwim(0.75F), 1), Pair.of(new RandomStroll(1.0F, true), 1), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 1), Pair.of(new RunIf<>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT)));
//    }
//
//    private static void initLaySpawnActivity(Brain<Frog> pBrain) {
//        pBrain.addActivityWithConditions(Activity.LAY_SPAWN, ImmutableList.of(Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(EntityType.PLAYER, 6.0F), UniformInt.of(30, 60))), Pair.of(1, new StartAttacking<>(FrogAi::canAttack, (p_218597_) -> {
//            return p_218597_.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
//        })), Pair.of(2, new TryFindLandNearWater(8, 1.0F)), Pair.of(3, new TryLaySpawnOnWaterNearLand(Blocks.FROGSPAWN, MemoryModuleType.IS_PREGNANT)), Pair.of(4, new RunOne<>(ImmutableList.of(Pair.of(new RandomStroll(1.0F), 2), Pair.of(new SetWalkTargetFromLookTarget(1.0F, 3), 1), Pair.of(new Croak(), 2), Pair.of(new RunIf<>(Entity::isOnGround, new DoNothing(5, 20)), 1))))), ImmutableSet.of(Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT)));
//    }
//
    private static void initJumpActivity(Brain<Frog> pBrain) {
        pBrain.addActivityWithConditions(Activity.LONG_JUMP, ImmutableList.of(Pair.of(0, new LongJumpMidJump(UniformInt.of(100, 140), SoundEvents.FROG_STEP)), Pair.of(1, new LongJumpToPreferredBlock<>(UniformInt.of(100, 140), 2, 4, 1.5F, (p_218593_) -> {
            return SoundEvents.FROG_LONG_JUMP;
        }, BlockTags.FROG_PREFER_JUMP_TO, 0.5F, (p_218583_) -> {
            return p_218583_.is(Blocks.LILY_PAD);
        }))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initTongueActivity(Brain<Frog> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.TONGUE, 0, ImmutableList.of(new StopAttackingIfTargetInvalid<>(), new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT)), MemoryModuleType.ATTACK_TARGET);
    }
}
