package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = true)
public interface IPresetSpellContainer {
    void initializeSpellContainer(ItemStack itemStack);
}
