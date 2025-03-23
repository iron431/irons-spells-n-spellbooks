package io.redspace.ironsspellbooks.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record AlchemistCauldronJeiRecipe(Ingredient itemIn, FluidStack fluidIn, List<FluidStack> results, ItemStack resultByproduct) {

}
