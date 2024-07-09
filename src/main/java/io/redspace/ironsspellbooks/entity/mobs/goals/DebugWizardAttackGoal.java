package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class DebugWizardAttackGoal extends Goal {
    private final PathfinderMob mob;
    protected final IMagicEntity spellCastingMob;
    private final AbstractSpell spell;
    private final int spellLevel;
    private final int cancelCastAfterTicks;
    private int tickCount = 0;
    private AbstractSpell castingSpell;

    private int castingTicks = 0;

    public DebugWizardAttackGoal(IMagicEntity abstractSpellCastingMob, AbstractSpell spell, int spellLevel, int cancelCastAfterTicks) {
        this.spellCastingMob = abstractSpellCastingMob;
        if (abstractSpellCastingMob instanceof PathfinderMob m) {
            this.mob = m;
        }else throw new IllegalStateException("Unable to add " + this.getClass().getSimpleName() + "to entity, must extend PathfinderMob.");
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
            IronsSpellbooks.LOGGER.debug("DebugWizardAttackGoal:  mob.initiateCastSpell:{}({}), pos:{}, isCasting:{}, isClient:{}", spell.getSpellId(), spellLevel, mob.position(), spellCastingMob.isCasting(), mob.level.isClientSide());
            spellCastingMob.initiateCastSpell(spell, spellLevel);
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