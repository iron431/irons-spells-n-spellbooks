package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TestProjectileSpell extends AbstractSpell{

    public TestProjectileSpell() {
        super(SpellType.TEST_SPELL,new TranslatableComponent("INCOMING!"));
    }

    @Override
    public void onCast(ItemStack stack, Level world, Player player) {

    }
}
