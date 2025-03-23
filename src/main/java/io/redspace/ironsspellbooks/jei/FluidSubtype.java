package io.redspace.ironsspellbooks.jei;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidSubtype implements IIngredientTypeWithSubtypes<Fluid, FluidStack> {
    @Override
    public Class<? extends FluidStack> getIngredientClass() {
        return FluidStack.class;
    }

    @Override
    public Class<? extends Fluid> getIngredientBaseClass() {
        return Fluid.class;
    }

    @Override
    public Fluid getBase(FluidStack ingredient) {
        return ingredient.getFluid();
    }
}
