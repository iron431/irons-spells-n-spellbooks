package io.redspace.ironsspellbooks.api.events;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

public class SpellTeleportEvent extends EntityTeleportEvent {
    private final AbstractSpellSkill spell;

    public SpellTeleportEvent(AbstractSpellSkill spell, Entity entity, double targetX, double targetY, double targetZ) {
        super(entity, targetX, targetY, targetZ);
        this.spell = spell;
    }

    public AbstractSpellSkill getSpell() {
        return spell;
    }
}
