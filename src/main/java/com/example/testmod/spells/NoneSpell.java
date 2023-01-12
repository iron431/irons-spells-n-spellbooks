package com.example.testmod.spells;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class NoneSpell extends AbstractSpell {
    public NoneSpell() {
        this(0);
    }

    public NoneSpell(int level) {
        super(SpellType.NONE_SPELL);
        this.level = level;
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.cooldown = 0;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {

    }
}
