package com.example.testmod.spells.fire;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TeleportSpell extends AbstractSpell {
    public TeleportSpell() {
        this(1);
    }

    public TeleportSpell(int level) {
        super(SpellType.TELEPORT);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 70;
        this.cooldown = 200;
    }

    @Override
    public void onCast(ItemStack stack, Level world, Player player) {

    }
}
