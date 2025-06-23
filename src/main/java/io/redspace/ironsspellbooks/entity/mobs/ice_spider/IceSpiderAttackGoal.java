package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.world.entity.LivingEntity;

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
        if (currentAttack != null) {
            if (currentAttack.animationId.contains("bite")) {
                mob.playSound(SoundRegistry.ICE_SPIDER_BITE.get());
            }
        }
        mob.playSound(SoundRegistry.ICE_SPIDER_SWING.get(), 1, Utils.random.nextIntBetweenInclusive(9, 11) * .1f);
    }

    @Override
    public void playImpactSound() {
        //todo: custom sound
//        mob.playSound(SoundRegistry.KEEPER_SWORD_IMPACT.get(), 1, Mth.randomBetweenInclusive(mob.getRandom(), 9, 13) * .1f);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }


    @Override
    // change access modifier
    public void doMovement(double distanceSquared) {
        super.doMovement(distanceSquared);
    }
}
