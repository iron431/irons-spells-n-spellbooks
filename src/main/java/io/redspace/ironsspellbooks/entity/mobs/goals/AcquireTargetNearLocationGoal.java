package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class AcquireTargetNearLocationGoal<T extends LivingEntity> extends TargetGoal {
    private Vec3 targetSearchPos;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    /**
     * This filter is applied to the Entity search. Only matching entities will be targeted.
     */
    protected TargetingConditions targetConditions;

    public AcquireTargetNearLocationGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, Vec3 targetSearchPos, @Nullable Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pMustSee, pMustReach);
        this.targetType = pTargetType;
        this.randomInterval = reducedTickDelay(pRandomInterval);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
        this.targetSearchPos = targetSearchPos;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            //irons_spellbooks.LOGGER.debug("AcquireTargetNearLocationGoal.canUse: false");
            return false;
        } else {
            this.findTarget();
            //irons_spellbooks.LOGGER.debug("AcquireTargetNearLocationGoal.canUse: {}", this.target);
            return this.target != null;
        }
    }

    protected AABB getTargetSearchArea(double pTargetDistance) {
        return this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
    }

    protected void findTarget() {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            var targetSearchArea = this.getTargetSearchArea(this.getFollowDistance());
            //irons_spellbooks.LOGGER.debug("AcquireTargetNearLocationGoal.findTarget.1 {}", targetSearchArea);
            var entitiesOfClass = this.mob.level().getEntitiesOfClass(this.targetType, targetSearchArea, (potentialTarget) -> {
                //irons_spellbooks.LOGGER.debug("AcquireTargetNearLocationGoal.findTarget.2 {}", potentialTarget.getName().getString());
                return true;
            });
            this.target = this.mob.level().getNearestEntity(entitiesOfClass, this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            //irons_spellbooks.LOGGER.debug("AcquireTargetNearLocationGoal.findTarget.6");
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity pTarget) {
        this.target = pTarget;
    }
}