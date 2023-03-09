package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class MobCastingHelper {
    private TeleportSpell teleportSpell;
    private LivingEntity mob;

    public MobCastingHelper(LivingEntity mob) {
        this.mob = mob;
    }

    public void initSpell(SpellType spellType, int level) {
        switch (spellType) {
            case TELEPORT_SPELL -> teleportSpell = (TeleportSpell) AbstractSpell.getSpell(spellType, level);
        }
    }

    public void teleportBehindTarget(Entity target, int distance) {

    }

}
