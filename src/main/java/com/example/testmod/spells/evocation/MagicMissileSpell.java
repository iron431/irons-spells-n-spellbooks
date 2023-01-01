package com.example.testmod.spells.evocation;

import com.example.testmod.entity.MagicMissileProjectile;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 0;
    }

    @Override
    public void onCast(Level world, Player player) {
        MagicMissileProjectile magicMissileProjectile = new MagicMissileProjectile(world, player);
        magicMissileProjectile.setPos(player.position().add(0, player.getEyeHeight(), 0));
        magicMissileProjectile.shoot(player.getLookAngle());
        magicMissileProjectile.setDamage(getSpellPower());
        world.addFreshEntity(magicMissileProjectile);
    }
}
