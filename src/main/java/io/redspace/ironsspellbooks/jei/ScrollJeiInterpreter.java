package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ScrollJeiInterpreter implements ISubtypeInterpreter<ItemStack> {
    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        if (ISpellContainer.isSpellContainer(ingredient)) {
            return ISpellContainer.get(ingredient);
        }
        return null;
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "null";
    }
}
