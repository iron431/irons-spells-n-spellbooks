package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.world.item.ItemStack;

public interface IPresetSpellContainer {
    ISpellContainer initializeSpellContainer(ItemStack itemStack);
}
