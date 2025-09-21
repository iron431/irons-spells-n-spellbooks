package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluidInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {
//    @Override
//    public @Nullable Object getSubtypeData(FluidStack ingredient, UidContext context) {
//        return ingredient.get(DataComponents.POTION_CONTENTS);
//    }
//
//    @Override
//    public String getLegacyStringSubtypeInfo(FluidStack ingredient, UidContext context) {
//        return "null";
//    }

    @Override
    public String apply(FluidStack stack, UidContext uidContext) {
        if (stack.hasTag()) {
            var potionname = BuiltInRegistries.POTION.getKey(FluidHelper.getPotionContents(stack)).toString();
            var bottlename = PotionFluid.BottleType.get(stack).getSerializedName();
            return String.format("fluid:%s:%s", potionname, bottlename);
        }

        return IIngredientSubtypeInterpreter.NONE;
    }
}
