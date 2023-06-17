package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.SupportMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public class WizardSupportGoal<T extends AbstractSpellCastingMob & SupportMob> extends Goal {
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

    protected final ArrayList<SpellType> healingSpells = new ArrayList<>();
    protected final ArrayList<SpellType> buffSpells = new ArrayList<>();
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

    public WizardSupportGoal setSpells(List<SpellType> healingSpells, List<SpellType> buffSpells) {
        this.healingSpells.clear();
        this.buffSpells.clear();

        this.healingSpells.addAll(healingSpells);
        this.buffSpells.addAll(buffSpells);

        return this;
    }

    public WizardSupportGoal setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return this;
    }

    public WizardSupportGoal setIsFlying() {
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
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceSquared) / (double) this.attackRadius, (double) this.attackIntervalMin, (double) this.attackIntervalMax));
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.tick.3: attackTime.2: {}", attackTime);
        }
        if (mob.isCasting()) {
            var pmg = PlayerMagicData.getPlayerMagicData(mob);
            if (target.isDeadOrDying() || AbstractSpell.getSpell(pmg.getCastingSpellId(), pmg.getCastingSpellLevel()).shouldAIStopCasting(mob, target))
                mob.cancelCast();

        }
    }

    protected void resetAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
        this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
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
        var spellType = getNextSpellType();

        //Make sure cast is valid
        if (!AbstractSpell.getSpell(spellType, spellLevel).shouldAIStopCasting(mob, target))
            mob.initiateCastSpell(spellType, spellLevel);

    }

    protected SpellType getNextSpellType() {

        float bias = target.getHealth() / target.getMaxHealth();
        if (buffSpells.isEmpty())
            bias *= 0;
        return getSpell(mob.getRandom().nextFloat() > bias ? healingSpells : buffSpells);
    }

    protected SpellType getSpell(List<SpellType> spells) {
        if (spells.size() < 1)
            return SpellType.NONE_SPELL;
        return spells.get(mob.getRandom().nextInt(spells.size()));
    }

    @Override
    public void start() {
        super.start();
    }
}