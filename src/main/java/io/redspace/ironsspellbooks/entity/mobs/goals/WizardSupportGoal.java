package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.SupportMob;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public class WizardSupportGoal<T extends PathfinderMob & SupportMob & IMagicEntity> extends Goal {
    protected final T mob;
    protected LivingEntity target;
    protected final double speedModifier;
    protected final int attackIntervalMin;
    protected final int attackIntervalMax;
    protected final float attackRadius;
    protected final float attackRadiusSqr;
    protected boolean shortCircuitTemp = false;

    protected boolean hasLineOfSight;
    protected int seeTime = 0;
    protected int attackTime = 0;

    protected boolean isFlying;

    protected final ArrayList<AbstractSpell> healingSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> buffSpells = new ArrayList<>();
    //protected final ArrayList<SpellType> movementSpells = new ArrayList<>();
    //protected final ArrayList<SpellType> supportSpells = new ArrayList<>();

    protected float minSpellQuality = .1f;
    protected float maxSpellQuality = .3f;

    public WizardSupportGoal(T abstractSpellCastingMob, double pSpeedModifier, int pAttackInterval) {
        this(abstractSpellCastingMob, pSpeedModifier, pAttackInterval, pAttackInterval);
    }

    public WizardSupportGoal(T abstractSpellCastingMob, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.mob = abstractSpellCastingMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 20;
        this.attackRadiusSqr = attackRadius * attackRadius;

    }

    public WizardSupportGoal<T> setSpells(List<AbstractSpell> healingSpells, List<AbstractSpell> buffSpells) {
        this.healingSpells.clear();
        this.buffSpells.clear();

        this.healingSpells.addAll(healingSpells);
        this.buffSpells.addAll(buffSpells);

        return this;
    }

    public WizardSupportGoal<T> setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return this;
    }

    public WizardSupportGoal<T> setIsFlying() {
        isFlying = true;
        return this;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getSupportTarget();
        if (livingentity != null && livingentity.isAlive() && Utils.shouldHealEntity(mob, livingentity)) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone() && Utils.shouldHealEntity(mob, target);
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
        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        doMovement(distanceSquared);

        handleAttackLogic(distanceSquared);

    }

    protected void handleAttackLogic(double distanceSquared) {
        if (--this.attackTime == 0) {

            if (!mob.isCasting()) {
                mob.lookAt(target, 180, 180);
                doSpellAction();
            }

            resetAttackTimer(distanceSquared);
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.tick.2: attackTime.1: {}", attackTime);
        }
        if (mob.isCasting()) {
            var spellData = MagicData.getPlayerMagicData(mob).getCastingSpell();
            if (target.isDeadOrDying() || spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target))
                mob.cancelCast();

        }
    }

    protected void resetAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
        this.attackTime = (int)(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
    }

    protected void doMovement(double distanceSquared) {
        float movementDebuff = mob.isCasting() ? .2f : 1f;
        double effectiveSpeed = movementDebuff * speedModifier;

        //move closer to target or strafe around
        if (distanceSquared < attackRadiusSqr && seeTime >= 5) {
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            this.mob.getNavigation().stop();
            mob.lookAt(target, 30, 30);
        } else {
            if (isFlying)
                this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2, target.getZ(), speedModifier);
            else
                this.mob.getNavigation().moveTo(this.target, effectiveSpeed);
        }
    }

    protected void doSpellAction() {
        int spellLevel = (int) (getNextSpellType().getMaxLevel() * Mth.lerp(mob.getRandom().nextFloat(), minSpellQuality, maxSpellQuality));
        spellLevel = Math.max(spellLevel, 1);
        var abstractSpell = getNextSpellType();

        //Make sure cast is valid
        if (!abstractSpell.shouldAIStopCasting(spellLevel, mob, target))
            mob.initiateCastSpell(abstractSpell, spellLevel);
        mob.setSupportTarget(null);
    }

    protected AbstractSpell getNextSpellType() {
        float shouldBuff = 0;
        if (!buffSpells.isEmpty() && target instanceof Mob mob && mob.isAggressive()) {
            shouldBuff = target.getHealth() / target.getMaxHealth();
        }
        return getSpell(mob.getRandom().nextFloat() > shouldBuff ? healingSpells : buffSpells);
    }

    protected AbstractSpell getSpell(List<AbstractSpell> spells) {
        if (spells.isEmpty())
            return SpellRegistry.none();
        return spells.get(mob.getRandom().nextInt(spells.size()));
    }

    @Override
    public void start() {
        super.start();
    }
}