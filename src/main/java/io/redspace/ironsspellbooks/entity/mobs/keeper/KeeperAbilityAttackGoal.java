package io.redspace.ironsspellbooks.entity.mobs.keeper;

import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.keeper.ability.KeeperAbilities;
import io.redspace.ironsspellbooks.entity.mobs.keeper.ability.KeeperAbilityType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KeeperAbilityAttackGoal extends WarlockAttackGoal {
    final KeeperEntity keeper;

    @Nullable KeeperAbilityType nextAbility;

    public KeeperAbilityAttackGoal(KeeperEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
        keeper = abstractSpellCastingMob;
        nextAbility = randomizeNextAttack(0);
        this.wantsToMelee = true;
    }

    @Override
    protected float meleeBias() {
        return 1f;
    }

    @Override
    public boolean isActing() {
        return super.isActing() || keeper.isUsingAbility();
    }

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        //Handling Animation hit frames
        mob.getLookControl().setLookAt(target);
        if (keeper.isUsingAbility()) {
            return;
        }
        /* todo: attack combos
        if keeper has queued ability
            immediately trigger queued ability
            reset queue
            reset next
            reset delay
        else continue
         */
        if (--meleeAttackDelay == 0 && nextAbility != null) {
            var meleeRange = meleeRange();
            float rangeSqr = meleeRange * nextAbility.getRangeMultiplier();
            rangeSqr *= rangeSqr;
            if (distanceSquared < rangeSqr && hasLineOfSight) {
                doMeleeAction();
            }
        } else if (meleeAttackDelay < 0) {
            resetMeleeAttackInterval(distanceSquared);
            nextAbility = randomizeNextAttack(Mth.sqrt((float) distanceSquared));
        }
    }

    private KeeperAbilityType randomizeNextAttack(float distance) {
        var meleeRange = meleeRange();
        List<KeeperAbilityType> attackList;
        if (distance < meleeRange * 1.5f) {
            attackList = KeeperAbilities.STANDARD_ATTACKS;
        } else if (mob.getRandom().nextFloat() < .25f && distance > meleeRange * 2.5f) {
            return KeeperAbilities.LUNGE;
        } else {
            attackList = KeeperAbilities.ALL_ATTACKS;
        }
        return attackList.get(mob.getRandom().nextInt(attackList.size()));
    }

    @Override
    protected void doMeleeAction() {
        keeper.activateAbility(nextAbility);
        nextAbility = null;
    }

    @Override
    protected void doMovement(double distanceSquared) {
        var meleeRange = meleeRange();
        if (target.isDeadOrDying()) {
            this.mob.getNavigation().stop();
        } else if (distanceSquared > meleeRange * meleeRange) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier * 1.3f);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() || isActing();
    }

    @Override
    public void stop() {
        super.stop();
        this.nextAbility = null;
        keeper.stopActiveAbility();
    }
}