package com.example.testmod.entity.mobs.goals;

import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.ai.goal.Goal;

@SuppressWarnings("FieldCanBeLocal")
public class WizardDebugAttackGoal extends Goal {
    private final AbstractSpellCastingMob mob;
    private final SpellType spellType = SpellType.LIGHTNING_LANCE_SPELL;
    private final int spellLevel = 1;
    private int tickCount = 0;

    public WizardDebugAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob) {
        this.mob = abstractSpellCastingMob;
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
            mob.castSpell(spellType, spellLevel);
        }
    }
}