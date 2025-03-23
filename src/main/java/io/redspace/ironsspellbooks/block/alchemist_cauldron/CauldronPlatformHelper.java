package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

@Deprecated(forRemoval = true)
public class CauldronPlatformHelper {
    @Deprecated(forRemoval = true)
    public static final Predicate<ItemStack> IS_WATER = (itemStack) -> itemStack.has(DataComponents.POTION_CONTENTS) && itemStack.get(DataComponents.POTION_CONTENTS).is(Potions.WATER);

    @Deprecated(forRemoval = true)
    public static boolean itemMatches(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    @Deprecated(forRemoval = true)
    public static boolean isBrewingIngredient(ItemStack stack, Level level) {
        return false;
    }

    @Deprecated(forRemoval = true)
    public static ItemStack getNonDestructiveBrewingResult(ItemStack base, ItemStack reagent, Level level) {
        return ItemStack.EMPTY;
    }
}
