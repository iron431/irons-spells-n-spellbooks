package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class DebugWizardAttackGoal extends Goal {
    private final PathfinderMob mob;
    private final AbstractSpellCastingMob spellCastingMob;
    private final AbstractSpell spell;
    private final int spellLevel;
    private final int cancelCastAfterTicks;
    private int tickCount = 0;
    private int castingTicks = 0;

    public DebugWizardAttackGoal(Mob abstractSpellCastingMob, AbstractSpell spell, int spellLevel, int cancelCastAfterTicks) {
        if (abstractSpellCastingMob instanceof PathfinderMob pathfinderMob && abstractSpellCastingMob instanceof AbstractSpellCastingMob castingMob) {
            this.mob = pathfinderMob;
            this.spellCastingMob = castingMob;
        } else {
            throw new IllegalStateException("Unable to add " + this.getClass().getSimpleName() + "to entity, must extend PathfinderMob.");
        }
        this.spell = spell;
        this.spellLevel = spellLevel;
        this.cancelCastAfterTicks = cancelCastAfterTicks;
    }

    public boolean canUse() {
        return true;
    }

    public boolean canContinueToUse() {
        return true;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        if (tickCount++ % 200 == 0) {
            spellCastingMob.attemptInitiateCastSpell(spell, spellLevel, null);
            castingTicks = 0;
        }

        if (spellCastingMob.isCasting()) {
            castingTicks++;

            if (cancelCastAfterTicks == castingTicks) {
                spellCastingMob.cancelCast();
            }
        }
    }
}
