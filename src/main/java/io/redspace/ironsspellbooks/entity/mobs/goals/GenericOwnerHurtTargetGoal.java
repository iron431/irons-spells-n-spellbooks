package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class GenericOwnerHurtTargetGoal extends TargetGoal {
    private final Mob entity;
    private final OwnerGetter owner;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public GenericOwnerHurtTargetGoal(Mob entity, OwnerGetter ownerGetter) {
        super(entity, false);
        this.entity = entity;
        this.owner = ownerGetter;
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
            //mob.getLastHurtByMobTimestamp() == mob.tickCount - 1
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();


            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && !(this.ownerLastHurt instanceof MagicSummon summon && summon.getSummoner() == owner);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
//        IronsSpellbooks.LOGGER.debug("GenericOwnerHurtTargetGoal.start");
//        IronsSpellbooks.LOGGER.debug("Brain before: {}",this.mob.getBrain().getMemories().keySet().stream()
//                .map(key -> key + "=" + this.mob.getBrain().getMemories().get(key))
//                .collect(Collectors.joining(", ", "{", "}")));
//        this.mob.getBrain().setMemoryWithExpiry(MemoryModuleType.NEAREST_ATTACKABLE, this.ownerLastHurt, 200L);
//        IronsSpellbooks.LOGGER.debug("Brain After: {}",this.mob.getBrain().getMemories().keySet().stream()
//                .map(key -> key + "=" + this.mob.getBrain().getMemories().get(key))
//                .collect(Collectors.joining(", ", "{", "}")));

        LivingEntity owner = this.owner.get();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }

        super.start();
    }
}