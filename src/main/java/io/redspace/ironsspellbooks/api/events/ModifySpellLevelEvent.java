package io.redspace.ironsspellbooks.api.events;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ModifySpellLevelEvent extends Event {

    final AbstractSpell spell;
    final LivingEntity caster;
    final int baseLevel;
    int totalLevel;

    public ModifySpellLevelEvent(AbstractSpell spell, LivingEntity caster, int baseLevel, int totalLevel) {
        this.spell = spell;
        this.caster = caster;
        this.baseLevel = baseLevel;
        this.totalLevel = totalLevel;
    }

    /**
     * @return Returns the original level of the spell which is being cast with no modifiers
     */
    public int getBaseLevel() {
        return baseLevel;
    }

    /**
     * @return Returns the running modified level of the spell
     */
    public int getLevel() {
        return totalLevel;
    }

    /**
     * Sets the running modified level of the spell
     */
    public void setLevel(int level) {
        this.totalLevel = level;
    }

    /**
     * Adds to the running modified level of the spell
     */
    public void addLevels(int levels) {
        this.totalLevel += levels;
    }

    /**
     * @return Returns the spell type associated with the level query
     */
    public AbstractSpell getSpell() {
        return spell;
    }

    /**
     * @return Returns the entity which is associated with the spell cast (can be null)
     */
    public @Nullable LivingEntity getEntity() {
        return this.caster;
    }
}
