package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class WizardRecoverGoal extends Goal {

    protected final AbstractSpellCastingMob mob;
    protected final int minDelay, maxDelay;
    protected int delay = 15;

    public WizardRecoverGoal(AbstractSpellCastingMob mob) {
        this(mob, 50, 120);
    }

    public WizardRecoverGoal(AbstractSpellCastingMob mob, int minDelay, int maxDelay) {
        this.mob = mob;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }

    @Override
    public boolean canUse() {
        // Delay only gets decremented if the first conditions are true, so it's smart.
        //IronsSpellbooks.LOGGER.debug("WizardRecoverGoal {} {} {} {}", mob.getTarget(), mob.isDrinkingPotion(), mob.isCasting(), delay);
        return mob.getTarget() == null && mob.getHealth() < mob.getMaxHealth() && !mob.isDrinkingPotion() && !mob.isCasting() && --delay <= 0;
    }

    @Override
    public void start() {
        mob.startDrinkingPotion();
        delay = mob.getRandom().nextIntBetweenInclusive(minDelay, maxDelay);
        //IronsSpellbooks.LOGGER.debug("WizardRecoverGoal begin {}", delay);
    }
}