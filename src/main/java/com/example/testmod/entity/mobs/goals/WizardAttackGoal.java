package com.example.testmod.entity.mobs.goals;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.spells.SpellType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

public class WizardAttackGoal extends Goal {
    private final AbstractSpellCastingMob mob;
    private LivingEntity target;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    private boolean hasLineOfSight;
    private int seeTime = 0;
    private int strafeTime;
    private boolean strafingClockwise;
    private int attackTime = -1;
    private int projectileCount;

    //private final ArrayList<SpellType> singleAttackSpells = new ArrayList<>();
    //private final ArrayList<SpellType> areaAttackSpells = new ArrayList<>();
    private final ArrayList<SpellType> attackSpells = new ArrayList<>();
    private final ArrayList<SpellType> defenseSpells = new ArrayList<>();
    private final ArrayList<SpellType> movementSpells = new ArrayList<>();
    private final ArrayList<SpellType> supportSpells = new ArrayList<>();
    private final ArrayList[] spellList = {
            attackSpells, defenseSpells, movementSpells, supportSpells
    };
    private ArrayList lastSpellCategory = attackSpells;
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

        //spellList.add(SpellType.ELECTROCUTE_SPELL);
        //spellList.add(SpellType.CONE_OF_COLD_SPELL);
        //spellList.add(SpellType.FIRE_BREATH_SPELL);
        //spellList.add(SpellType.BLOOD_SLASH_SPELL);
//        spellList.add(SpellType.TELEPORT_SPELL);
        //spellList.add(SpellType.MAGIC_MISSILE_SPELL);
        attackSpells.add(SpellType.MAGIC_MISSILE_SPELL);
        attackSpells.add(SpellType.MAGIC_MISSILE_SPELL);
        attackSpells.add(SpellType.MAGIC_MISSILE_SPELL);
        attackSpells.add(SpellType.FANG_STRIKE_SPELL);
        attackSpells.add(SpellType.FANG_STRIKE_SPELL);
        attackSpells.add(SpellType.ELECTROCUTE_SPELL);
        defenseSpells.add(SpellType.SHIELD_SPELL);
        movementSpells.add(SpellType.TELEPORT_SPELL);
        supportSpells.add(SpellType.HEAL_SPELL);

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
        hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        //search for projectiles around the mob
        if (mob.tickCount % 3 == 0) {
            projectileCount = mob.level.getEntitiesOfClass(Projectile.class, mob.getBoundingBox().inflate(24), (projectile) -> projectile.getOwner() != mob).size();
        }

        //move closer to target or strafe around
        if (distanceSquared < attackRadiusSqr && seeTime >= 5) {
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            this.mob.getNavigation().stop();
            if (++strafeTime > 25) {
                if (mob.getRandom().nextDouble() < .1)
                {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }

            int strafeDir = strafingClockwise ? 1 : -1;
            if (distanceSquared < attackRadiusSqr * .25f)
                mob.getMoveControl().strafe(-(float) speedModifier, (float) speedModifier * strafeDir);
            else
                mob.getMoveControl().strafe(0, (float) speedModifier * strafeDir);
            mob.lookAt(target, 30, 30);
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        //do attacks
        this.mob.getLookControl().setLookAt(this.target, 45, 45);
        //TestMod.LOGGER.debug("{},{}", mob.getLastHurtByMobTimestamp(), mob.tickCount);
        if (mob.getLastHurtByMobTimestamp() == mob.tickCount - 1) {
            int t = (int) (Mth.lerp(.6f, attackTime, 0) + 1);
            TestMod.LOGGER.debug("Ouch! {}->{}", attackTime, t);
            attackTime = t;
            //attackTime = (int) (Mth.lerp(.25f, attackTime, 0) + 1);
        }
        if (--this.attackTime == 0) {

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

        mob.castSpell(spellType, mob.getRandom().nextInt(3) + 1);
    }

    private SpellType getNextSpellType() {

        NavigableMap<Integer, ArrayList> weightedSpells = new TreeMap<>();
        int attackWeight = getAttackWeight();
        int defenseWeight = getDefenseWeight() - (lastSpellCategory == defenseSpells ? 80 : 0);
        int movementWeight = getMovementWeight() - (lastSpellCategory == movementSpells ? 50 : 0);
        int supportWeight = getSupportWeight() - (lastSpellCategory == supportSpells ? 35 : 0);
        int total = 0;

        if (!attackSpells.isEmpty() && attackWeight > 0) {
            total += attackWeight;
            weightedSpells.put(total, attackSpells);
        }
        if (!defenseSpells.isEmpty() && defenseWeight > 0) {
            total += defenseWeight;
            weightedSpells.put(total, defenseSpells);
        }
        if (!movementSpells.isEmpty() && movementWeight > 0) {
            total += movementWeight;
            weightedSpells.put(total, movementSpells);
        }
        if (!supportSpells.isEmpty() && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, supportSpells);
        }


//        if (spellListIndex == spellList.size() - 1) {
//            spellListIndex = -1;
//        }
//        return spellList.get(++spellListIndex);
        int seed = mob.getRandom().nextInt(total);
        var spellList = weightedSpells.higherEntry(seed).getValue();
        lastSpellCategory = spellList;
        TestMod.LOGGER.info("WizardAttackGoal.getNextSpell weights: A:{} D:{} M:{} S:{} ({}/{})", attackWeight, defenseWeight, movementWeight, supportWeight, seed, total);
        return (SpellType) spellList.get(mob.getRandom().nextInt(spellList.size()));
    }

    private int getAttackWeight() {
        //We want attack to be a common action in any circumstance, but the more "confident" we are the more likely we are to attack (we have health or our target is weak)
        int baseWeight = 80;

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        int distanceWeight = (int) (1 - (distanceSquared / attackRadiusSqr) * -60);

        if (hasLineOfSight)
            return baseWeight + targetHealthWeight + distanceWeight;
        else
            return 0;
    }

    private int getDefenseWeight() {
        //We want defensive spells to be used when we feel "threatened", meaning we aren't confident, or we're actively being attacked
        int baseWeight = -20;

        //https://www.desmos.com/calculator/tqs7dudcmv
        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        int healthWeight = (int) (40 * (Math.pow(-(x / m) * (x - m), 3) / Math.pow(m / 2, 3)) * 8);

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        //this count be finicky due to the fact that projectiles don't stick around for long, so it might be easy to miss them
        int threatWeight = projectileCount * 55;

        return baseWeight + healthWeight + targetHealthWeight + threatWeight;
    }

    private int getMovementWeight() {
        //We want to move if we're in a disadvantageous spot, or we need a better angle on our target

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / attackRadiusSqr, 0, 1);

        int distanceWeight = (int) ((distancePercent) * 50);

        int losWeight = hasLineOfSight ? 0 : 80;

        float healthInverted = 1 - mob.getHealth() / mob.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;

    }

    private int getSupportWeight() {
        //We want to support/buff ourselves if we are weak
        int baseWeight = -15;

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
        int healthWeight = (int) (200 * health);

        return baseWeight + healthWeight;
    }


}