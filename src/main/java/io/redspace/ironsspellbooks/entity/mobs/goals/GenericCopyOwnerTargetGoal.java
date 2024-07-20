package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.function.Supplier;

public class GenericCopyOwnerTargetGoal extends TargetGoal {
    private final Supplier<LivingEntity> ownerGetter;

    public GenericCopyOwnerTargetGoal(PathfinderMob pMob, Supplier<LivingEntity> ownerGetter) {
        super(pMob, false);
        this.ownerGetter = ownerGetter;

    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        return ownerGetter.get() instanceof Mob owner && owner.getTarget() != null && !(owner.getTarget() instanceof IMagicSummon summon && summon.getSummoner() == owner);
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