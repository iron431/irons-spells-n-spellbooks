package com.example.testmod.entity.mobs.goals;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class SpellBarrageGoal extends Goal {
    protected static final int interval = 5;
    protected final AbstractSpellCastingMob mob;
    protected LivingEntity target;
    protected final int attackIntervalMin;
    protected final int attackIntervalMax;
    protected final float attackRadius;
    protected final float attackRadiusSqr;
    protected final int projectileCount;
    protected final SpellType spell;
    protected int attackTime;

    protected final int minSpellLevel;
    protected final int maxSpellLevel;

    public SpellBarrageGoal(AbstractSpellCastingMob abstractSpellCastingMob, SpellType spell, int minLevel, int maxLevel, int pAttackIntervalMin, int pAttackIntervalMax, int projectileCount) {
        this.mob = abstractSpellCastingMob;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 20;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.minSpellLevel = minLevel;
        this.maxSpellLevel = maxLevel;
        this.projectileCount = projectileCount;
        this.spell = spell;
        resetAttackTimer();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        target = this.mob.getTarget();
        if (target == null || mob.isCasting())
            return false;

        if (attackTime <= -interval * (projectileCount - 1)) {
            resetAttackTimer();
        }
        TestMod.LOGGER.debug("SpellBarrageGoal ({}) canUse: attackTime: {}, reset threshold: {}", spell, attackTime, -interval * (projectileCount - 1));
        return --attackTime <= 0 && attackTime % interval == 0;

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return false;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.target = null;
        if (attackTime > 0)
            this.attackTime = -projectileCount * interval - 1;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {

        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        if (distanceSquared < attackRadiusSqr) {
            this.mob.getLookControl().setLookAt(this.target, 45, 45);
            mob.initiateCastSpell(spell, mob.getRandom().nextIntBetweenInclusive(minSpellLevel, maxSpellLevel));
        }



    }

    protected void resetAttackTimer() {
        this.attackTime = mob.getRandom().nextIntBetweenInclusive(attackIntervalMin, attackIntervalMax);
    }
}