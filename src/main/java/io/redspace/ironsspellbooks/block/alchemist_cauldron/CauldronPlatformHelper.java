package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import java.util.function.Predicate;

public class CauldronPlatformHelper {
    public static final Predicate<ItemStack> IS_WATER = (itemStack) -> PotionUtils.getPotion(itemStack) == Potions.WATER;

    public static boolean itemMatches(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
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
}
