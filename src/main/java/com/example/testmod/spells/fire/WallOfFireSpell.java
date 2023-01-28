package com.example.testmod.spells.fire;

import com.example.testmod.capabilities.magic.PlayerMagicData;
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
        var vec = Utils.getTargetBlock(world, entity, ClipContext.Fluid.ANY, 10).getLocation();

    }

    @Override
    public void onCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
//        for (Vec3 vec : spawnAnchors) {
//            MagicManager.spawnParticles(level, ParticleTypes.SMOKE, vec.x, vec.y + 1, vec.z, 100, 0, 2, 0, .01, true);
//            TestMod.LOGGER.debug(vec.toString());
//
//        }
    }
}
