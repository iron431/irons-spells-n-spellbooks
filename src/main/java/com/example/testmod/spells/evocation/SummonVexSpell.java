package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
    protected void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        for (int i = 0; i < this.level; i++) {
//            Vex vex = EntityType.VEX.create(world);
//            vex.setOwner(player);
//            vex.setBoundOrigin(blockpos);
        }
    }
}
