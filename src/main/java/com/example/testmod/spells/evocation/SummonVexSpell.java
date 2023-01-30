package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.SummonedVex;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

public class SummonVexSpell extends AbstractSpell {
    public SummonVexSpell() {
        this(1);
    }

    public SummonVexSpell(int level) {
        super(SpellType.SUMMON_VEX_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
        this.cooldown = 300;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        for (int i = 0; i < this.level; i++) {
            SummonedVex vex = new SummonedVex(world, entity);
            vex.setPos(entity.getEyePosition());
            vex.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(vex.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
            world.addFreshEntity(vex);
        }
    }
}
