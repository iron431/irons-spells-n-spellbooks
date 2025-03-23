package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import com.google.common.collect.ImmutableList;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class AlchemistCauldronRecipeRegistry {

    @Deprecated(forRemoval = true)
    public static AlchemistCauldronRecipe registerRecipe(ResourceLocation resourceLocation, AlchemistCauldronRecipe recipe) {
        IronsSpellbooks.LOGGER.warn("Mod {} is trying to register an Alchemist Cauldron recipe, which no longer works!", resourceLocation.getNamespace());
        return recipe;
    }


    @Deprecated(forRemoval = true)
    public static ItemStack getOutput(ItemStack input, ItemStack ingredient, boolean consumeOnSucces) {
        return ItemStack.EMPTY;
    }

    @Deprecated(forRemoval = true)
    public static ItemStack getOutput(ItemStack input, ItemStack ingredient, boolean ignoreCount, boolean consumeOnSucces) {
        return ItemStack.EMPTY;
    }

    @Deprecated(forRemoval = true)
    public static boolean isValidIngredient(ItemStack itemStack) {
        return false;
    }

    @Deprecated(forRemoval = true)
    public static boolean hasOutput(ItemStack input, ItemStack ingredient) {
        return false;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public static AlchemistCauldronRecipe getRecipeForResult(ItemStack result) {
        return null;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public static AlchemistCauldronRecipe getRecipeForInputs(ItemStack base, ItemStack reagent) {
        return null;
    }

    @Deprecated(forRemoval = true)
    public static ImmutableList<AlchemistCauldronRecipe> getRecipes() {
        return ImmutableList.of();
    }
}
