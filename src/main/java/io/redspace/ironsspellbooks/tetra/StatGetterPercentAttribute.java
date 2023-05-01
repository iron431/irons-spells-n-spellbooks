package io.redspace.ironsspellbooks.tetra;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.StatGetterAttribute;

public class StatGetterPercentAttribute extends StatGetterAttribute {
    public StatGetterPercentAttribute(Attribute attribute) {
        super(attribute);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return 100 * (super.getValue(player, itemStack) - 1);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return 100 * (super.getValue(player, itemStack, slot) - 1);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return 100 * (super.getValue(player, itemStack, slot, improvement) - 1);
    }
}
