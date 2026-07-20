package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.IDrinkPotions;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class WizardAttackGoal<T extends PathfinderMob & IDrinkPotions> extends Goal {

    protected LivingEntity target;
    protected final double speedModifier;
    protected final int spellAttackIntervalMin;
    protected final int spellAttackIntervalMax;
    protected float spellcastingRange;
    protected float spellcastingRangeSqr;

    protected boolean hasLineOfSight;
    protected int seeTime = 0;
    protected int strafeTime;
    protected boolean strafingClockwise;
    protected int spellAttackDelay = -1;

    protected boolean isFlying;
    protected boolean allowFleeing;
    protected int fleeCooldown;

    @Nullable SingleUseSpellHandler singleSpellHandler = null;

    protected final ArrayList<AbstractSpell> attackSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> defenseSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> movementSpells = new ArrayList<>();
    protected final ArrayList<AbstractSpell> supportSpells = new ArrayList<>();
    protected ArrayList<AbstractSpell> lastSpellCategory = attackSpells;

    protected float minSpellQuality = .1f;
    protected float maxSpellQuality = .4f;

    protected boolean drinksPotions;
    protected final T mob;

    public WizardAttackGoal(T mob, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Flag.TARGET));
        this.mob = mob;
        this.speedModifier = pSpeedModifier;
        this.spellAttackIntervalMin = pAttackIntervalMin;
        this.spellAttackIntervalMax = pAttackIntervalMax;
        this.spellcastingRange = 20;
        this.spellcastingRangeSqr = spellcastingRange * spellcastingRange;
        allowFleeing = true;
    }

    public <G extends WizardAttackGoal<T>> G setSpells(List<AbstractSpell> attackSpells, List<AbstractSpell> defenseSpells, List<AbstractSpell> movementSpells, List<AbstractSpell> supportSpells) {
        this.attackSpells.clear();
        this.defenseSpells.clear();
        this.movementSpells.clear();
        this.supportSpells.clear();

        this.attackSpells.addAll(attackSpells);
        this.defenseSpells.addAll(defenseSpells);
        this.movementSpells.addAll(movementSpells);
        this.supportSpells.addAll(supportSpells);

        return (G) this;
    }

    public <G extends WizardAttackGoal<T>> G setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return (G) this;
    }

    public <G extends WizardAttackGoal<T>> G setSingleUseSpell(AbstractSpell abstractSpell, int minDelay, int maxDelay, int minLevel, int maxLevel) {
        this.singleSpellHandler = new SingleUseSpellHandler(abstractSpell,
                Utils.random.nextIntBetweenInclusive(minDelay, maxDelay),
                Utils.random.nextIntBetweenInclusive(minLevel, maxLevel));
        return (G) this;
    }

    public <G extends WizardAttackGoal<T>> G setIsFlying() {
        isFlying = true;
        return (G) this;
    }

    public <G extends WizardAttackGoal<T>> G setDrinksPotions() {
        drinksPotions = true;
        return (G) this;
    }

    public <G extends WizardAttackGoal<T>> G setAllowFleeing(boolean allowFleeing) {
        this.allowFleeing = allowFleeing;
        return (G) this;
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
            return mob.canAttack(target);
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.spellAttackDelay = -1;
        this.mob.setAggressive(false);
        this.mob.getMoveControl().strafe(0, 0);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }
        //default mage movement
        doMovement(distanceSquared);

        //do attacks
        if (mob.getLastHurtByMobTimestamp() == mob.tickCount - 1) {
            spellAttackDelay = (int) (Mth.lerp(.6f, spellAttackDelay, 0) + 1);
        }

        //default attack timer
        handleAttackLogic(distanceSquared);

        if (singleSpellHandler != null) {
            singleSpellHandler.tick();
        }
    }

    protected void handleAttackLogic(double distanceSquared) {
        if (seeTime < -50) {
            return;
        }
        if (isCasting()) {
            ActiveCast activeCast = SkillcastingData.get(mob).getActiveCast();
            if (activeCast != null && activeCast.context().skill().value().shouldAIStopCasting(activeCast.context(), mob, target)) {
                interruptCast();
                return;
            }
            maybeCancelContinuousCastOnDamage();
        } else if (!isActing() && --this.spellAttackDelay == 0) {
            resetSpellAttackTimer(distanceSquared);
            doSpellAction();
        } else if (this.spellAttackDelay < 0) {
            resetSpellAttackTimer(distanceSquared);
        }
    }

    private void maybeCancelContinuousCastOnDamage() {
        if (isCasting() && mob.getLastHurtByMobTimestamp() == mob.tickCount - 1 &&
                SkillcastingData.get(mob).getActiveCastType() == CastType.CONTINUOUS &&
                mob.getRandom().nextFloat() > mob.getHealth() / mob.getMaxHealth()) {
            interruptCast();
        }
    }

    public boolean isActing() {
        return isCasting() || mob.isDrinkingPotion();
    }

    public boolean isCasting() {
        return SkillcastingData.get(mob).isCasting();
    }

    public void interruptCast() {
        SkillcastingManager.cancelCast(CasterRef.entity(mob), CastEndReason.INTERRUPTED);
    }

    protected void resetSpellAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.spellcastingRange;
        float r = mob.getRandom().nextFloat();
        this.spellAttackDelay = (int) Math.max(1, Mth.lerp((f + r) / 2f, this.spellAttackIntervalMin, spellAttackIntervalMax));
    }

    protected void doMovement(double distanceSquared) {
        double speed = (isCasting() ? .5f : 1f) * movementSpeed();
        mob.lookAt(target, 30, 30);
        //make distance (flee), move into range, or strafe around
        float fleeDist = .275f;
        float ss = getStrafeMultiplier();
        if (allowFleeing && distanceSquared < spellcastingRangeSqr * (fleeDist * fleeDist)) {
            // fixme: fleeing is scuffed
            if (!isActing() && --fleeCooldown <= 0) {
                Vec3 flee = DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
                if (flee != null) {
                    this.mob.getNavigation().moveTo(flee.x, flee.y, flee.z, speed * 1.5);
                } else {
                    mob.getMoveControl().strafe(-(float) speed * ss, (float) speed * ss);
                }
                fleeCooldown = 20;
            }
            seeTime = 0;
        } else if (distanceSquared < spellcastingRangeSqr && seeTime >= 20) {
            this.mob.getNavigation().stop();
            if (++strafeTime > 25) {
                if (mob.getRandom().nextDouble() < .1) {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }
            float strafeForward = (distanceSquared * 6 < spellcastingRangeSqr ? -1 : .5f) * .2f * (float) speedModifier;
            int strafeDir = strafingClockwise ? 1 : -1;
            mob.getMoveControl().strafe(strafeForward * ss, (float) speed * strafeDir * ss);
            if (mob.horizontalCollision && mob.getRandom().nextFloat() < .1f) {
                tryJump();
            }
        } else {
            // no los or we are completely out of range, path towards target
            if (mob.tickCount % 5 == 0) {
                if (isFlying) {
                    this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2, target.getZ(), speedModifier);
                } else {
                    this.mob.getNavigation().moveTo(this.target, speedModifier);
                }
            }
        }
    }

    protected double movementSpeed() {
        //fixme: move control already reads speed attribute, we should not be basing speed modifier based on it as well
        return speedModifier/* * mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2*/;
    }

    protected void tryJump() {
        Vec3 nextBlock = new Vec3(mob.xxa, 0, mob.zza).normalize();

        BlockPos blockpos = BlockPos.containing(mob.position().add(nextBlock));
        BlockState blockstate = this.mob.level.getBlockState(blockpos);
        VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
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
        if (singleSpellHandler != null && singleSpellHandler.attemptCastSpell(mob)) {
            return;
        }
        var spell = getNextSpellType();
        if (spell == null) {
            return;
        }
        int spellLevel = (int) (spell.getMaxLevel() * Mth.lerp(mob.getRandom().nextFloat(), minSpellQuality, maxSpellQuality));
        spellLevel = Math.max(spellLevel, 1);
        SkillcastingUtils.attemptInitiateMobCast(mob, SkillRegistry.holder(spell), spellLevel, null);
        fleeCooldown = 7;
    }

    protected @Nullable AbstractSpell getNextSpellType() {
        NavigableMap<Integer, ArrayList<AbstractSpell>> weightedSpells = new TreeMap<>();
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

        if (total > 0) {
            int seed = mob.getRandom().nextInt(total);
            var spellList = weightedSpells.higherEntry(seed).getValue();
            lastSpellCategory = spellList;
            if (drinksPotions && spellList == supportSpells) {
                if (supportSpells.isEmpty() || mob.getRandom().nextFloat() < .5f) {
                    mob.startDrinkingPotion();
                    return null;
                }
            }
            return spellList.get(mob.getRandom().nextInt(spellList.size()));
        } else {
            return null;
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
        if (!hasLineOfSight || target == null) {
            return 0;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        int distanceWeight = (int) (1 - (distanceSquared / spellcastingRangeSqr) * -60);

        return baseWeight + targetHealthWeight + distanceWeight;
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

        return baseWeight + healthWeight + targetHealthWeight;
    }

    protected int getMovementWeight() {
        if (target == null) {
            return 0;
        }
        //We want to move if we're in a disadvantageous spot, or we need a better angle on our target
        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / spellcastingRangeSqr, 0, 1);

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

        //If our target is close we should probably not drink a potion right in front of them
        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / spellcastingRangeSqr, 0, 1);
        int distanceWeight = (int) ((1 - distancePercent) * -75);

        return baseWeight + healthWeight + distanceWeight;
    }

    @Override
    public boolean isInterruptable() {
        return !isActing();
    }

    public float getStrafeMultiplier() {
        return 1f;
    }
}