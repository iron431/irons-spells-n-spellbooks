package com.example.testmod.spells.fire;

import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballSpell extends AbstractSpell {
    public FireballSpell() {
        this(1);
    }

    public FireballSpell(int level) {
        super(SpellType.FIREBALL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 60;
        this.baseManaCost = 50;
        this.cooldown = 300;
    }

    @Override
    public void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        float speed = 2.5f;
        Vec3 direction = player.getLookAngle().scale(speed);
        Vec3 origin = player.getEyePosition();
        Fireball fireball = new LargeFireball(world, player, direction.x(), direction.y(), direction.z(), getSpellPower());
        fireball.setPos(origin.add(direction));
        world.addFreshEntity(fireball);
    }
}
