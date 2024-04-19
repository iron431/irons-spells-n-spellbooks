package io.redspace.ironsspellbooks.api.events;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * ModifySpellLevelEvent is fired on the server and client whenever a spell's level is queried via {@link AbstractSpell#getLevel(int, LivingEntity)}. Compared to modifying the level at spellcast ({@link SpellOnCastEvent#setSpellLevel(int)}), this level will affect the tooltip and mana cost. <br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
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

    @Override
    public boolean isCancelable() {
        return false;
    }
}
