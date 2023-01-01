package com.example.testmod.spells.fire;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TeleportSpell extends AbstractSpell {
    public TeleportSpell() {
        this(1);
    }

    public TeleportSpell(int level) {
        super(SpellType.TELEPORT_SPELL, SpellType.TELEPORT_SPELL.getCastType());
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 70;
        this.cooldown = 200;
    }

    @Override
    public void onCast(Level world, Player player) {

    }
}
