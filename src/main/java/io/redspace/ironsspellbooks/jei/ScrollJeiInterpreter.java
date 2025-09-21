package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

public class ScrollJeiInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
//    @Override
//    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
//        if (ISpellContainer.isSpellContainer(ingredient)) {
//            return ISpellContainer.get(ingredient);
//        }
//        return null;
//    }
//
//    @Override
//    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
//        return "null";
//    }

    @Override
    public String apply(ItemStack stack, UidContext uidContext) {
        if (stack.hasTag()) {
            var ss = ISpellContainer.get(stack).getSpellAtIndex(0);
            return String.format("scroll:%s:%d", ss.getSpell().getSpellId(), ss.getLevel());
        }

        return IIngredientSubtypeInterpreter.NONE;
    }
}
