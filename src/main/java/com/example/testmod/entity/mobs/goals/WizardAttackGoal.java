package com.example.testmod.entity.mobs.goals;

import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.spells.SpellType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;

public class WizardAttackGoal extends Goal {
    private final AbstractSpellCastingMob mob;
    private LivingEntity target;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    private int seeTime = 0;
    private int attackTime = -1;

    private final ArrayList<SpellType> spellList = new ArrayList<>();
    private int spellListIndex = -1;

    public WizardAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int pAttackInterval) {
        this(abstractSpellCastingMob, pSpeedModifier, pAttackInterval, pAttackInterval);
    }

    public WizardAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.mob = abstractSpellCastingMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 20;
        this.attackRadiusSqr = attackRadius * attackRadius;

        spellList.add(SpellType.MAGIC_MISSILE_SPELL);
        spellList.add(SpellType.CONE_OF_COLD_SPELL);
        spellList.add(SpellType.FIRE_BREATH_SPELL);
        spellList.add(SpellType.BLOOD_SLASH_SPELL);
        spellList.add(SpellType.TELEPORT_SPELL);

    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            //TestMod.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(distanceSquared > (double) attackRadiusSqr) && seeTime >= 5) {
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.mob.getLookControl().setLookAt(this.target, 45, 45);
        if (--this.attackTime == 0) {
            if (!hasLineOfSight) {
                return;
            }

            float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);

            if (!mob.isCasting())
                doAction();

            this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.2: attackTime.1: {}", attackTime);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceSquared) / (double) this.attackRadius, (double) this.attackIntervalMin, (double) this.attackIntervalMax));
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.3: attackTime.2: {}", attackTime);
        }
    }

    private void doAction() {
        var spellType = getNextSpellType();

        if (spellType == SpellType.TELEPORT_SPELL) {
            mob.setTeleportLocationBehindTarget(15);
        }

        mob.castSpell(spellType, 1);
    }

    private SpellType getNextSpellType() {
        if (spellListIndex == spellList.size() - 1) {
            spellListIndex = -1;
        }
        return spellList.get(++spellListIndex);
    }
}