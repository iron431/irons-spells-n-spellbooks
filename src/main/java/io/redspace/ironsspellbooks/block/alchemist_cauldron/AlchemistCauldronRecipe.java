package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

public class AlchemistCauldronRecipe {

    //Base is the item inside the cauldron being acted on
    //Ingredient is what is added to the cauldron and acts on the base
    //Result is the new item created inside the cauldron, consuming the base and ingredient
    private final ItemStack inputStack, ingredientStack, resultStack;
    private int requiredBaseCount = 1;
    private int resultLimitCount = 4;

    public AlchemistCauldronRecipe(ItemStack inputStack, ItemStack ingredientStack, ItemStack resultStack) {
        this.inputStack = inputStack;
        this.ingredientStack = ingredientStack;
        this.resultStack = resultStack;
    }

    public AlchemistCauldronRecipe(Item input, Item ingredient, Item result) {
        this(new ItemStack(input), new ItemStack(ingredient), new ItemStack(result));
    }

    public AlchemistCauldronRecipe(Potion input, Item ingredient, Item result) {
        this(PotionUtils.setPotion(new ItemStack(Items.POTION), input), new ItemStack(ingredient), new ItemStack(result));
    }


    public AlchemistCauldronRecipe setBaseRequirement(int i) {
        this.requiredBaseCount = i;
        return this;
    }

    public AlchemistCauldronRecipe setResultLimit(int i) {
        this.resultLimitCount = i;
        return this;
    }

    public ItemStack createOutput(ItemStack input, ItemStack ingredient, boolean ignoreCount, boolean consumeOnSuccess) {
        if (CauldronPlatformHelper.itemMatches(input, this.inputStack) && CauldronPlatformHelper.itemMatches(ingredient, this.ingredientStack)) {
            if (ignoreCount || input.getCount() >= this.requiredBaseCount) {
                ItemStack result = this.resultStack.copy();
                result.setCount(this.resultLimitCount);
                if (consumeOnSuccess) {
                    input.shrink(this.requiredBaseCount);
                    ingredient.shrink(1);
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getInput() {
        var i = inputStack.copy();
        i.setCount(this.requiredBaseCount);
        return i;
    }

    public ItemStack getIngredient() {
        return ingredientStack.copy();
    }

    public ItemStack getResult() {
        var i = resultStack.copy();
        i.setCount(this.resultLimitCount);
        return i;
    }
}
