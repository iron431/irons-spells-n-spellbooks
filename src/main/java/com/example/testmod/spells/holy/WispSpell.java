package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class WispSpell extends AbstractSpell {
    public WispSpell() {
        this(1);
    }

    public WispSpell(int level) {
        super(SpellType.WISP_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 30;
        this.cooldown = 400;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {

    }
}
