package io.redspace.ironsspellbooks.api.backwards_compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidHelper {

    public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
        return a.isFluidEqual(b) && FluidStack.areFluidStackTagsEqual(a, b);
    }

    public static FluidStack copyWithAmount(FluidStack fluidStack, int amount) {
        var stack = fluidStack.copy();
        stack.setAmount(amount);
        return stack;
    }

//    public static boolean hasPotionContentions(FluidStack stack) {
//        return
//    }

    public static boolean hasPotionContents(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains("Potion");
    }

    public static boolean isWater(ItemStack stack) {
        return hasPotionContents(stack) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    public static boolean isBrewingIngredient(ItemStack stack, Level level) {
        return BrewingRecipeRegistry.isValidIngredient(stack);
    }

    /**
     * @param base    Base is the existing item attempting to be transformed (ie water bottle)
     * @param reagent Reagent is the acting brewing ingredient (ie nether wart)
     * @return Returns brewing result (without affecting input itemstacks) or ItemStack.EMPTY
     */
    public static ItemStack getNonDestructiveBrewingResult(ItemStack base, ItemStack reagent, Level level) {
        return BrewingRecipeRegistry.getOutput(base, reagent);
    }

    public static boolean hasPotionContents(FluidStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains("Potion");
    }

    public static Potion getPotionContents(FluidStack stack) {
        return hasPotionContents(stack) ? PotionUtils.getPotion(stack.getOrCreateTag()) : Potions.EMPTY;
    }

    public static void setPotionContents(FluidStack stack, Potion potion) {
        stack.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
    }

    public static ItemStack createItemStack(Item item, Holder<Potion> potion) {
        var stack = new ItemStack(item);
        PotionUtils.setPotion(stack, potion.get());
        return stack;
    }
}
