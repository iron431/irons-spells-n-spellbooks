package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GenericFollowOwnerGoal extends Goal {

    private final PathfinderMob entity;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;
    private final OwnerGetter ownerGetter;
    private final float teleportDistance;

    public GenericFollowOwnerGoal(PathfinderMob entity, OwnerGetter ownerGetter, double pSpeedModifier, float pStartDistance, float pStopDistance, boolean pCanFly, float teleportDistance) {
        this.entity = entity;
        this.ownerGetter = ownerGetter;
        this.level = entity.level();
        this.speedModifier = pSpeedModifier;
        this.navigation = entity.getNavigation();
        this.startDistance = pStartDistance;
        this.stopDistance = pStopDistance;
        this.canFly = pCanFly;
        this.teleportDistance = teleportDistance * teleportDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(entity.getNavigation() instanceof GroundPathNavigation) && !(entity.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
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
                    if(canFly && !entity.onGround()){
                        Vec3 vec3 = owner.position();
                        this.entity.getMoveControl().setWantedPosition(vec3.x, vec3.y + 2, vec3.z, this.speedModifier);

                    }else{
                        this.navigation.moveTo(this.owner, this.speedModifier);

                    }
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
            this.entity.moveTo((double) pX + 0.5D, (double) pY + (canFly && !entity.onGround() ? 3 : 0), (double) pZ + 0.5D, this.entity.getYRot(), this.entity.getXRot());
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
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
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