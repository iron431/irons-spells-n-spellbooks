package io.redspace.ironsspellbooks.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class PotionFluidInterpreter implements ISubtypeInterpreter<FluidStack> {
    @Override
    public @Nullable Object getSubtypeData(FluidStack ingredient, UidContext context) {
        return ingredient.get(DataComponents.POTION_CONTENTS);
    }

    @Override
    public String getLegacyStringSubtypeInfo(FluidStack ingredient, UidContext context) {
        return "null";
    }
}
