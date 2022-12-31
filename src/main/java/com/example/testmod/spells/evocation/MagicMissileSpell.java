package com.example.testmod.spells.evocation;

import com.example.testmod.entity.MagicMissileProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 2;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, Player player) {
        MagicMissileProjectile projectileEntity = new MagicMissileProjectile(world, player);
        projectileEntity.setPos(player.position().add(0, player.getEyeHeight(), 0));
        var vec3 = player.getLookAngle();
        projectileEntity.shouldRender(vec3.x, vec3.y, vec3.z);
        world.addFreshEntity(projectileEntity);
    }
}
