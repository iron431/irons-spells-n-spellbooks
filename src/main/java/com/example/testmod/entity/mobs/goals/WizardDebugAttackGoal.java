package com.example.testmod.entity.mobs.goals;

import com.example.testmod.entity.mobs.AbstractSpellCastingMob;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.ai.goal.Goal;

public class WizardDebugAttackGoal extends Goal {
    private final AbstractSpellCastingMob mob;
    private final SpellType spellType;
    private final int spellLevel;
    private int tickCount = 0;

    public WizardDebugAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, SpellType spellType, int spellLevel) {
        this.mob = abstractSpellCastingMob;
        this.spellType = spellType;
        this.spellLevel = spellLevel;
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
            if (spellType == SpellType.TELEPORT_SPELL) {
                mob.setTeleportLocationBehindTarget(15);
            }

            mob.initiateCastSpell(spellType, spellLevel);
        }
    }
}