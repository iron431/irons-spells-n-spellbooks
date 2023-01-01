package com.example.testmod.spells.lightning;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class ElectrocuteSpell extends AbstractSpell {
    public ElectrocuteSpell() {
        this(1);
    }

    public ElectrocuteSpell(int level) {
        super(SpellType.ELECTROCUTE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 10;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, Player player) {
        float speed = 2.5f;
        Vec3 direction = player.getLookAngle().scale(speed);
        Vec3 origin = player.getEyePosition();
        Fireball fireball = new LargeFireball(world, player, direction.x(), direction.y(), direction.z(), 1);
        fireball.setPos(origin.add(direction));
        world.addFreshEntity(fireball);
    }
}
