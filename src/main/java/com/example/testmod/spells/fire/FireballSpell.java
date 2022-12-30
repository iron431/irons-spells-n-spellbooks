package com.example.testmod.spells.fire;

import com.example.testmod.TestMod;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.chat.TranslatableComponent;
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
        this.baseManaCost = 40;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.cooldown = 20;

        //TODO: remove this after testing
        this.baseManaCost = 2;
        this.cooldown = 200;
    }

    @Override
    public void onCast(ItemStack stack, Level world, Player player) {
        float speed = 2.5f;
        Vec3 direction = player.getLookAngle().scale(speed);
        Vec3 origin = player.getEyePosition();
        Fireball fireball = new LargeFireball(world, player, direction.x(), direction.y(), direction.z(), getSpellPower());
        fireball.setPos(origin.add(direction));
        world.addFreshEntity(fireball);
    }
}
