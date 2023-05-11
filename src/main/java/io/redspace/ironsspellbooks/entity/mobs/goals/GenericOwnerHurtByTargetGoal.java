package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class GenericOwnerHurtByTargetGoal extends TargetGoal {
    private final Mob entity;
    private final OwnerGetter owner;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public GenericOwnerHurtByTargetGoal(Mob entity, OwnerGetter getOwner) {
        super(entity, false);
        this.entity = entity;
        this.owner = getOwner;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity owner = this.owner.get();
        if (owner == null) {
            return false;
        } else {
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            if (ownerLastHurtBy == null || ownerLastHurtBy.isAlliedTo(mob))
                return false;
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && !(this.ownerLastHurtBy instanceof MagicSummon summon && summon.getSummoner() == owner);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        this.mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, this.ownerLastHurtBy, 200L);
        LivingEntity owner = this.owner.get();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }

        super.start();
    }

}