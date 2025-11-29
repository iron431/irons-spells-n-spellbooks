package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.function.Supplier;

public class GenericCopyOwnerTargetGoal extends TargetGoal {
    private final Supplier<Entity> ownerGetter;

    public GenericCopyOwnerTargetGoal(PathfinderMob pMob, Supplier<Entity> ownerGetter) {
        super(pMob, false);
        this.ownerGetter = ownerGetter;

    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (!(ownerGetter.get() instanceof Mob owner)) {
            return false;
        }
        var target = owner.getTarget();
        if (target == null) {
            return false;
        }
        return canAttack(target, TargetingConditions.DEFAULT) && !(target instanceof IMagicSummon summon && summon.getSummoner() == owner);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        var target = ((Mob) ownerGetter.get()).getTarget();
        mob.setTarget(target);
        this.mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, 200L);

        super.start();
    }
}