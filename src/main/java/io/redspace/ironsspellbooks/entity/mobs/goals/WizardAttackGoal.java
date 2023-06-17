package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class WizardAttackGoal extends Goal {
    protected final AbstractSpellCastingMob mob;
    protected LivingEntity target;
    protected final double speedModifier;
    protected final int attackIntervalMin;
    protected final int attackIntervalMax;
    protected final float attackRadius;
    protected final float attackRadiusSqr;
    protected boolean shortCircuitTemp = false;

    protected boolean hasLineOfSight;
    protected int seeTime = 0;
    protected int strafeTime;
    protected boolean strafingClockwise;
    protected int attackTime = -1;
    protected int projectileCount;

    protected SpellType singleUseSpell = SpellType.NONE_SPELL;
    protected int singleUseDelay;
    protected int singleUseLevel;

    protected boolean isFlying;

    protected final ArrayList<SpellType> attackSpells = new ArrayList<>();
    protected final ArrayList<SpellType> defenseSpells = new ArrayList<>();
    protected final ArrayList<SpellType> movementSpells = new ArrayList<>();
    protected final ArrayList<SpellType> supportSpells = new ArrayList<>();
    protected ArrayList<SpellType> lastSpellCategory = attackSpells;

    protected float minSpellQuality = .1f;
    protected float maxSpellQuality = .3f;

    protected boolean drinksPotions;

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
        defenseSpells.add(SpellType.EVASION_SPELL);
        movementSpells.add(SpellType.TELEPORT_SPELL);
        supportSpells.add(SpellType.HEAL_SPELL);

    }

    public WizardAttackGoal setSpells(List<SpellType> attackSpells, List<SpellType> defenseSpells, List<SpellType> movementSpells, List<SpellType> supportSpells) {
        this.attackSpells.clear();
        this.defenseSpells.clear();
        this.movementSpells.clear();
        this.supportSpells.clear();

        this.attackSpells.addAll(attackSpells);
        this.defenseSpells.addAll(defenseSpells);
        this.movementSpells.addAll(movementSpells);
        this.supportSpells.addAll(supportSpells);

        return this;
    }

    public WizardAttackGoal setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return this;
    }

    public WizardAttackGoal setSingleUseSpell(SpellType spellType, int minDelay, int maxDelay, int minLevel, int maxLevel) {
        this.singleUseSpell = spellType;
        this.singleUseDelay = mob.level.random.nextIntBetweenInclusive(minDelay, maxDelay);
        this.singleUseLevel = mob.level.random.nextIntBetweenInclusive(minLevel, maxLevel);

        return this;
    }

    public WizardAttackGoal setIsFlying() {
        isFlying = true;
        return this;
    }

    public WizardAttackGoal setDrinksPotions() {
        drinksPotions = true;
        return this;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
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
        this.mob.setAggressive(false);
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

//        //search for projectiles around the mob
//        if (mob.tickCount % 3 == 0) {
//            projectileCount = mob.level.getEntitiesOfClass(Projectile.class, mob.getBoundingBox().inflate(24), (projectile) -> projectile.getOwner() != mob && !projectile.isOnGround()).size();
//        }

        //default mage movement
        doMovement(distanceSquared);

        //do attacks
        //this.mob.getLookControl().setLookAt(this.target, 45, 45);
        //irons_spellbooks.LOGGER.debug("{},{}", mob.getLastHurtByMobTimestamp(), mob.tickCount);
        if (mob.getLastHurtByMobTimestamp() == mob.tickCount - 1) {
            int t = (int) (Mth.lerp(.6f, attackTime, 0) + 1);
            //Ironsspellbooks.logger.debug("Ouch! {}->{}", attackTime, t);
            attackTime = t;
            //attackTime = (int) (Mth.lerp(.25f, attackTime, 0) + 1);
        }

        //default attack timer
            handleAttackLogic(distanceSquared);

        singleUseDelay--;

    }

    protected void handleAttackLogic(double distanceSquared) {
        if (--this.attackTime == 0) {

            if (!mob.isCasting() && !mob.isDrinkingPotion()) {
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
        double speed = mob.isCasting() ? .2f : 1f * speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2;

        //move closer to target or strafe around
        if (distanceSquared < attackRadiusSqr && seeTime >= 5) {
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            this.mob.getNavigation().stop();
            if (++strafeTime > 25) {
                if (mob.getRandom().nextDouble() < .1) {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }

            int strafeDir = strafingClockwise ? 1 : -1;
            if (distanceSquared < attackRadiusSqr * .5f)
                mob.getMoveControl().strafe(-(float) speed, (float) speed * strafeDir);
            else
                mob.getMoveControl().strafe(0, (float) speed * strafeDir);
            if (mob.horizontalCollision && mob.getRandom().nextFloat() < .1f)
                tryJump();

            mob.lookAt(target, 30, 30);
        } else {
            if (isFlying)
                this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2, target.getZ(), speedModifier);
            else
                this.mob.getNavigation().moveTo(this.target, speed);
        }
    }

    protected void tryJump() {
        //mob.getJumpControl().jump();
        Vec3 nextBlock = new Vec3(mob.xxa, 0, mob.zza).normalize();
        IronsSpellbooks.LOGGER.debug("{}", nextBlock);

        BlockPos blockpos = new BlockPos(mob.position().add(nextBlock));
        BlockState blockstate = this.mob.level.getBlockState(blockpos);
        VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
        //IronsSpellbooks.LOGGER.debug("{}", mob.getDeltaMovement());
        IronsSpellbooks.LOGGER.debug("{}", blockstate.getBlock().getName().getString());
        if (!voxelshape.isEmpty() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
            BlockPos blockposAbove = blockpos.above();
            BlockState blockstateAbove = this.mob.level.getBlockState(blockposAbove);
            VoxelShape voxelshapeAbove = blockstateAbove.getCollisionShape(this.mob.level, blockposAbove);
            if (voxelshapeAbove.isEmpty()) {
                this.mob.getJumpControl().jump();
                //boost to get over the edge
                mob.setXxa(mob.xxa * 5);
                mob.setZza(mob.zza * 5);
            }

        }
    }

    protected void doSpellAction() {
        if (!mob.hasUsedSingleAttack && singleUseSpell != SpellType.NONE_SPELL && singleUseDelay <= 0) {
            mob.hasUsedSingleAttack = true;
            mob.initiateCastSpell(singleUseSpell, singleUseLevel);
        } else {

            int spellLevel = (int) (getNextSpellType().getMaxLevel() * Mth.lerp(mob.getRandom().nextFloat(), minSpellQuality, maxSpellQuality));
            spellLevel = Math.max(spellLevel, 1);
            var spellType = getNextSpellType();

            //Make sure cast is valid
            if (!AbstractSpell.getSpell(spellType, spellLevel).shouldAIStopCasting(mob, target))
                mob.initiateCastSpell(spellType, spellLevel);
        }

    }

    protected SpellType getNextSpellType() {


        NavigableMap<Integer, ArrayList> weightedSpells = new TreeMap<>();
        int attackWeight = getAttackWeight();
        int defenseWeight = getDefenseWeight() - (lastSpellCategory == defenseSpells ? 100 : 0);
        int movementWeight = getMovementWeight() - (lastSpellCategory == movementSpells ? 50 : 0);
        int supportWeight = getSupportWeight() - (lastSpellCategory == supportSpells ? 100 : 0);
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
        if ((!supportSpells.isEmpty() || drinksPotions) && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, supportSpells);
        }


//        if (spellListIndex == spellList.size() - 1) {
//            spellListIndex = -1;
//        }
//        return spellList.get(++spellListIndex);
        if (total > 0) {
            int seed = mob.getRandom().nextInt(total);
            var spellList = weightedSpells.higherEntry(seed).getValue();
            lastSpellCategory = spellList;
            //IronsSpellbooks.LOGGER.debug("WizardAttackGoal.getNextSpell weights: A:{} D:{} M:{} S:{} ({}/{})", attackWeight, defenseWeight, movementWeight, supportWeight, seed, total);
            if (drinksPotions && spellList == supportSpells) {
                if (supportSpells.isEmpty() || mob.getRandom().nextFloat() < .5f) {
                    IronsSpellbooks.LOGGER.debug("Drinking Potion");
                    mob.startDrinkingPotion();
                    return SpellType.NONE_SPELL;
                }
            }
            return (SpellType) spellList.get(mob.getRandom().nextInt(spellList.size()));
        } else {
            //IronsSpellbooks.LOGGER.debug("WizardAttackGoal.getNextSpell weights: A:{} D:{} M:{} S:{} (no spell)", attackWeight, defenseWeight, movementWeight, supportWeight);
            return SpellType.NONE_SPELL;
        }

    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }


    protected int getAttackWeight() {
        //We want attack to be a common action in any circumstance, but the more "confident" we are the more likely we are to attack (we have health or our target is weak)
        int baseWeight = 80;

        if (target == null) {
            return baseWeight;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        int distanceWeight = (int) (1 - (distanceSquared / attackRadiusSqr) * -60);

        if (hasLineOfSight)
            return baseWeight + targetHealthWeight + distanceWeight;
        else
            return 0;
    }

    protected int getDefenseWeight() {
        //We want defensive spells to be used when we feel "threatened", meaning we aren't confident, or we're actively being attacked
        int baseWeight = -20;

        if (target == null) {
            return baseWeight;
        }

        //https://www.desmos.com/calculator/tqs7dudcmv
        //https://www.desmos.com/calculator/7skhcvpic0
        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        //int healthWeight = (int) (50 * (Math.pow(-(x / m) * (x - m), 3) / Math.pow(m / 2, 3)) * 8);
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        //this count be finicky due to the fact that projectiles don't stick around for long, so it might be easy to miss them
        int threatWeight = projectileCount * 95;

        return baseWeight + healthWeight + targetHealthWeight + threatWeight;
    }

    protected int getMovementWeight() {
        if (target == null) {
            return 0;
        }
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

    protected int getSupportWeight() {
        //We want to support/buff ourselves if we are weak
        int baseWeight = -15;

        if (target == null) {
            return baseWeight;
        }

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
        int healthWeight = (int) (200 * health);

        return baseWeight + healthWeight;
    }

}