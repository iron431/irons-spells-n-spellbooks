package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Supplier;

public class GenericFollowOwnerGoal extends Goal {
    private final PathfinderMob mob;
    @Nullable
    private LivingEntity owner;
    private Supplier<LivingEntity> ownerGetter;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private float teleportDistance;
    private boolean canFly;

    public GenericFollowOwnerGoal(PathfinderMob pTamable, Supplier<LivingEntity> ownerGetter, double pSpeedModifier, float pStartDistance, float pStopDistance, boolean canFly, float teleportDistance) {
        this.mob = pTamable;
        this.ownerGetter = ownerGetter;
        this.speedModifier = pSpeedModifier;
        this.navigation = pTamable.getNavigation();
        this.startDistance = pStartDistance;
        this.stopDistance = pStopDistance;
        this.teleportDistance = teleportDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(pTamable.getNavigation() instanceof GroundPathNavigation) && !(pTamable.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
        this.canFly = canFly;
    }

    @Override
    public boolean canUse() {
        IronsSpellbooks.LOGGER.debug("genericownerfollwer canuse");
        LivingEntity livingentity = this.ownerGetter.get();
        if (livingentity == null) {
            return false;
        } else if (this.mob.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return !(this.mob.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean flag = this.shouldTryTeleportToOwner();
        if (!flag) {
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (flag) {
                this.tryToTeleportToOwner();
            } else {
                if (canFly && !mob.onGround()) {
                    Vec3 vec3 = owner.position();
                    this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y + 2, vec3.z, this.speedModifier);
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }

    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.ownerGetter.get();
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingentity = this.ownerGetter.get();
        return livingentity != null && mob.distanceToSqr(livingentity) >= teleportDistance * teleportDistance;
    }

    private void teleportToAroundBlockPos(BlockPos pPos) {
        for (int i = 0; i < 10; i++) {
            int j = mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int k = mob.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = mob.getRandom().nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(pPos.getX() + j, pPos.getY() + l, pPos.getZ() + k)) {
                    return;
                }
            }
        }
    }

    private boolean maybeTeleportTo(int pX, int pY, int pZ) {
        if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
            return false;
        } else {
            mob.moveTo((double) pX + 0.5, (double) pY, (double) pZ + 0.5, mob.getYRot(), mob.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pPos) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(mob, pPos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = mob.level().getBlockState(pPos.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pPos.subtract(mob.blockPosition());
                return mob.level().noCollision(mob, mob.getBoundingBox().move(blockpos));
            }
        }
    }
}