package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class WizardRecoverGoal extends Goal {

    protected final PathfinderMob mob;
    protected final IMagicEntity spellCastingMob;
    protected final int minDelay, maxDelay;
    protected int delay = 15;

    public WizardRecoverGoal(IMagicEntity mob) {
        this(mob, 50, 120);
    }

    public WizardRecoverGoal(IMagicEntity mob, int minDelay, int maxDelay) {
        this.spellCastingMob = mob;
        if (mob instanceof PathfinderMob m) {
            this.mob = m;
        }else throw new IllegalStateException("Unable to add " + this.getClass().getSimpleName() + "to entity, must extend PathfinderMob.");

        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }

    @Override
    public boolean canUse() {
        // Delay only gets decremented if the first conditions are true, so it's smart.
        //IronsSpellbooks.LOGGER.debug("WizardRecoverGoal {} {} {} {}", mob.getTarget(), mob.isDrinkingPotion(), mob.isCasting(), delay);
        return mob.getTarget() == null && mob.getHealth() < mob.getMaxHealth() && !spellCastingMob.isDrinkingPotion() && !spellCastingMob.isCasting() && --delay <= 0;
    }

    @Override
    public void start() {
        spellCastingMob.startDrinkingPotion();
        delay = mob.getRandom().nextIntBetweenInclusive(minDelay, maxDelay);
        //IronsSpellbooks.LOGGER.debug("WizardRecoverGoal begin {}", delay);
    }
}