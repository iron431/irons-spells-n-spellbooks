package com.example.testmod.entity.mobs;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
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
