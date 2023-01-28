package com.example.testmod.spells.fire;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wall_of_fire.WallOfFireEntity;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;


public class WallOfFireSpell extends AbstractSpell {
    public WallOfFireSpell() {
        this(1);
    }

    public WallOfFireSpell(int level) {
        super(SpellType.WALL_OF_FIRE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        var vec = Utils.getTargetBlock(world, entity, ClipContext.Fluid.ANY, 15).getLocation();
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.castingEntity != null && playerMagicData.castingEntity instanceof WallOfFireEntity fireWall) {
            fireWall.addAnchor(vec);
        } else {
            WallOfFireEntity fireWall = new WallOfFireEntity(world, entity);
            fireWall.setPos(vec);
            world.addFreshEntity(fireWall);
            playerMagicData.discardCastingEntity();
            playerMagicData.castingEntity = fireWall;
            fireWall.addAnchor(vec);
        }
    }

    @Override
    public void onCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
//        for (Vec3 vec : spawnAnchors) {
//            TestMod.LOGGER.debug(vec.toString());
//
//        }
        if (playerMagicData.castingEntity instanceof WallOfFireEntity fireWall)
            fireWall.createShield();
        //playerMagicData.forgetCastingEntity();
    }
}
