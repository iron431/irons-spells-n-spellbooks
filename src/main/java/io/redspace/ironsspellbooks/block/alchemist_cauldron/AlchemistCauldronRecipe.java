package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

@Deprecated(forRemoval = true)
public class AlchemistCauldronRecipe {

    public AlchemistCauldronRecipe(ItemStack inputStack, ItemStack ingredientStack, ItemStack resultStack) {
    }

    public AlchemistCauldronRecipe(Item input, Item ingredient, Item result) {
    }

    public AlchemistCauldronRecipe(Holder<Potion> input, Item ingredient, Item result) {
    }


    public AlchemistCauldronRecipe setBaseRequirement(int i) {
        return this;
    }

    public AlchemistCauldronRecipe setResultLimit(int i) {
        return this;
    }

    public ItemStack createOutput(ItemStack input, ItemStack ingredient, boolean ignoreCount, boolean consumeOnSuccess) {
        return ItemStack.EMPTY;
    }

    public ItemStack getInput() {
        return ItemStack.EMPTY;
    }

    public ItemStack getIngredient() {
        return ItemStack.EMPTY;
    }

    public ItemStack getResult() {
        return ItemStack.EMPTY;
    }
}
