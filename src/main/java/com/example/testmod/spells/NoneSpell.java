package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NoneSpell extends AbstractSpell {
    public NoneSpell() {
        this(0);
    }

    public NoneSpell(int level) {
        super(SpellType.NONE_SPELL,CastType.INSTANT);
        this.level = level;
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.cooldown = 0;
    }

    @Override
    public void onCast(Level world, Player player) {

    }
}
