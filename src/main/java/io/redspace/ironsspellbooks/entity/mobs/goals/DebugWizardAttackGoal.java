package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.util.Unit;
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
            CasterRef casterRef = CasterRef.entity(mob);
            var castContext = SkillcastingManager.buildCastContext(casterRef, SkillRegistry.holder(spell), spellLevel, CastSource.EMPTY);
            castContext.set(SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
            castContext.set(SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);
            SkillcastingManager.initiateCast(casterRef, castContext);
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
