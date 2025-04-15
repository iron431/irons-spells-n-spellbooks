package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;

public class IceSpiderAttackGoal extends GenericAnimatedWarlockAttackGoal<IceSpiderEntity> {
    public IceSpiderAttackGoal(IceSpiderEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
    }

    @Override
    public void tick() {
        wantsToMelee = !mob.wantsToCastSpells;
        super.tick();
    }

    @Override
    public void handleAttackLogic(double distanceSquared) {
        if (mob.getGrappleTargetUUID() != null) {
            //pause attacking while we are biting our enemy
            return;
        }
        super.handleAttackLogic(distanceSquared);
    }

    @Override
    public void playSwingSound() {
        //todo: spider sounds
    }
}
