package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.entity.ai.goal.Goal;

public class DebugWizardAttackGoal extends Goal {
    private final AbstractSpellCastingMob mob;
    private final SpellType spellType;
    private final int spellLevel;
    private final int cancelCastAfterTicks;
    private int tickCount = 0;
    private AbstractSpell castingSpell;

    private int castingTicks = 0;

    public DebugWizardAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, SpellType spellType, int spellLevel, int cancelCastAfterTicks) {
        this.mob = abstractSpellCastingMob;
        this.spellType = spellType;
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
            IronsSpellbooks.LOGGER.debug("DebugWizardAttackGoal:  mob.initiateCastSpell:{}({}), pos:{}, isCasting:{}, isClient:{}", spellType, spellLevel, mob.position(), mob.isCasting(), mob.level.isClientSide());
            mob.initiateCastSpell(spellType, spellLevel);
            castingTicks = 0;
        }

        if (mob.isCasting()) {
            castingTicks++;

            if (cancelCastAfterTicks == castingTicks) {
                mob.cancelCast();
            }
        }
    }
}