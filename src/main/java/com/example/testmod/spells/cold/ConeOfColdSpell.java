package com.example.testmod.spells.cold;


import com.example.testmod.entity.ConeOfColdProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ConeOfColdSpell extends AbstractSpell {
    public ConeOfColdSpell() {
        this(1);
    }

    public ConeOfColdSpell(int level) {
        super(SpellType.CONE_OF_COLD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 150;
        this.baseManaCost = 2;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, Player player) {
        ConeOfColdProjectile coneOfColdProjectile = new ConeOfColdProjectile(world, player);
        coneOfColdProjectile.setPos(player.position().add(0, player.getEyeHeight() * .7, 0));
        coneOfColdProjectile.setDamage(getSpellPower());
        world.addFreshEntity(coneOfColdProjectile);
    }
}
