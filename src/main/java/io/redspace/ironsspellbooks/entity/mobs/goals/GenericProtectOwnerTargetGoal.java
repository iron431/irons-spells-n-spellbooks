package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Supplier;

public class GenericProtectOwnerTargetGoal extends TargetGoal {
    private final Supplier<Entity> owner;
    private int intervalToCheck;
    private final int maxIntensity = 100; // tick delay at minimum intensity
    /**
     * integer that slowly decays after repeated failed checks, meaning we can check less frequently
     */
    private int currentIntensity;

    public GenericProtectOwnerTargetGoal(Mob entity, Supplier<Entity> getOwner) {
        super(entity, false);
        this.owner = getOwner;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (!(this.owner.get() instanceof LivingEntity owner)) {
            return false;
        } else {
            if (--intervalToCheck <= 0) {
                var entities = owner.level.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(16, 8, 16), potentionalAggressor -> potentionalAggressor.getTarget() != null &&
                        (potentionalAggressor.getTarget().getUUID().equals(owner.getUUID())
                                || (potentionalAggressor.getTarget() instanceof IMagicSummon summon
                                && summon.getSummoner() != null && summon.getSummoner().getUUID().equals(owner.getUUID())))
                        && Utils.hasLineOfSight(mob.level, mob.getEyePosition(), potentionalAggressor.getEyePosition(), false)
                );
                if (entities.isEmpty()) {
                    currentIntensity = Math.max(0, currentIntensity - 10);
                    return false;
                } else {
                    mob.setTarget(entities.stream().min(Comparator.comparingDouble(o -> o.distanceToSqr(owner))).orElse(entities.getFirst()));
                    return true;
                }
            } else {
                int i = owner.getLastHurtByMobTimestamp();
                int tick = owner.tickCount;
                int combatIntervalModifier = Math.clamp((tick - i) / 5, 0, 200);
                int intensityModifier = maxIntensity - currentIntensity;
                intervalToCheck = 20 + combatIntervalModifier + intensityModifier;
            }
        }
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        currentIntensity = maxIntensity;
        super.start();
    }

}