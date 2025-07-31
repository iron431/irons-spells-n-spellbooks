package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;
import java.util.function.Supplier;

public class GenericOwnerHurtTargetGoal extends TargetGoal {
    private final Supplier<Entity> owner;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public GenericOwnerHurtTargetGoal(Mob entity, Supplier<Entity> ownerGetter) {
        super(entity, false);
        this.owner = ownerGetter;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (!(this.owner.get() instanceof LivingEntity owner)) {
            return false;
        } else {
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();

            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && !(this.ownerLastHurt instanceof IMagicSummon summon && summon.getSummoner() == owner);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        Entity owner = this.owner.get();
        if (owner instanceof LivingEntity livingOwner) {
            this.timestamp = livingOwner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}