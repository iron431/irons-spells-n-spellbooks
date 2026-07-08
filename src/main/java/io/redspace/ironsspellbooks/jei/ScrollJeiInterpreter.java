package io.redspace.ironsspellbooks.jei;

import io.redspace.skillcasting.data.ISkillContainer;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ScrollJeiInterpreter implements ISubtypeInterpreter<ItemStack> {
    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        if (ISkillContainer.isSkillContainer(ingredient)) {
            return ISkillContainer.get(ingredient);
        }
        return null;
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "null";
    }
}
