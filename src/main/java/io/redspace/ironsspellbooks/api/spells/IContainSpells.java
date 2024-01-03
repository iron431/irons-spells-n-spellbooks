package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.world.item.ItemStack;

public interface IContainSpells {
    ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack);
}
