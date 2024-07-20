package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class CauldronPlatformHelper {
    public static final Predicate<ItemStack> IS_WATER = (itemStack) -> itemStack.has(DataComponents.POTION_CONTENTS) && itemStack.get(DataComponents.POTION_CONTENTS).is(Potions.WATER);

    public static boolean itemMatches(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static boolean isBrewingIngredient(ItemStack stack, Level level) {
        return level.potionBrewing().isIngredient(stack);
    }

    /**
     * @param base    Base is the existing item attempting to be transformed (ie water bottle)
     * @param reagent Base is the acting brewing ingredient (ie nether wart)
     * @return Returns brewing result (without affects input itemstacks) or ItemStack.EMPTY
     */
    public static ItemStack getNonDestructiveBrewingResult(ItemStack base, ItemStack reagent, Level level) {
        return level.potionBrewing().hasPotionMix(base, reagent) ? level.potionBrewing().mix(reagent, base) : ItemStack.EMPTY;
    }
}
