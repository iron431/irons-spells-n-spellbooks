package io.redspace.ironsspellbooks.api.events;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

public class SpellTeleportEvent extends EntityTeleportEvent {
    private final AbstractSpell spell;

    public SpellTeleportEvent(AbstractSpell spell, Entity entity, double targetX, double targetY, double targetZ) {
        super(entity, targetX, targetY, targetZ);
        this.spell = spell;
    }

    public AbstractSpell getSpell() {
        return spell;
    }
}
