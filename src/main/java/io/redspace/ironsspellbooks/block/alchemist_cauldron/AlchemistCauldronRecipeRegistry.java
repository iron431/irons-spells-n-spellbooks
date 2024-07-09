package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import com.google.common.collect.ImmutableList;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.List;

public class AlchemistCauldronRecipeRegistry {
    private static final List<AlchemistCauldronRecipe> recipes = new ArrayList<>();

    static {
        //No cool recipes for right now :(
        //IronsSpellbooks.LOGGER.debug("creating custom cauldron recipes");
        //recipes.add(new AlchemistCauldronRecipe(ItemRegistry.BLOOD_VIAL.get(), ItemRegistry.HOGSKIN.get(), ItemRegistry.ARCANE_ESSENCE.get()).setBaseRequirement(4).setResultLimit(1));
        //recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_EPIC.get(), Items.OBSIDIAN, Items.CRYING_OBSIDIAN));
        //recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_LEGENDARY.get(), Items.BLUE_ORCHID, Items.DANDELION).setBaseRequirement(2).setResultLimit(2));
//        recipes.add(new AlchemistCauldronRecipe(PotionRegistry.INSTANT_MANA_ONE.get(), Items.FLOWERING_AZALEA_LEAVES, ItemRegistry.CASTERS_TEA.get()));
        recipes.add(new AlchemistCauldronRecipe(Potions.STRONG_HEALING, Items.OAK_LOG, ItemRegistry.OAKSKIN_ELIXIR.get()).setBaseRequirement(2).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.OAKSKIN_ELIXIR.get(), Items.AMETHYST_SHARD, ItemRegistry.GREATER_OAKSKIN_ELIXIR.get()).setBaseRequirement(2).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(Potions.STRONG_HEALING, Items.AMETHYST_SHARD, ItemRegistry.GREATER_HEALING_POTION.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(Potions.INVISIBILITY, ItemRegistry.SHRIVING_STONE.get(), ItemRegistry.INVISIBILITY_ELIXIR.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(Potions.LONG_INVISIBILITY, ItemRegistry.SHRIVING_STONE.get(), ItemRegistry.INVISIBILITY_ELIXIR.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INVISIBILITY_ELIXIR.get(), Items.AMETHYST_CLUSTER, ItemRegistry.GREATER_INVISIBILITY_ELIXIR.get()));
        recipes.add(new AlchemistCauldronRecipe(PotionRegistry.INSTANT_MANA_THREE, Items.ENDER_PEARL, ItemRegistry.EVASION_ELIXIR.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.EVASION_ELIXIR.get(), Items.DRAGON_BREATH, ItemRegistry.GREATER_EVASION_ELIXIR.get()));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_COMMON.get(), Items.COPPER_INGOT, ItemRegistry.INK_UNCOMMON.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_UNCOMMON.get(), Items.IRON_INGOT, ItemRegistry.INK_RARE.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_RARE.get(), Items.GOLD_INGOT, ItemRegistry.INK_EPIC.get()).setBaseRequirement(4).setResultLimit(1));
        recipes.add(new AlchemistCauldronRecipe(ItemRegistry.INK_EPIC.get(), Items.AMETHYST_SHARD, ItemRegistry.INK_LEGENDARY.get()).setBaseRequirement(4).setResultLimit(1));

    }

    /**
     * If any modder is crazy enough to want to use this, do it during FMLCommonSetup Event
     */
    public static AlchemistCauldronRecipe addRecipe(AlchemistCauldronRecipe recipe) {
        recipes.add(recipe);
        return recipe;
    }


    /**
     * Searches through registered recipes, and returns the resulting item or ItemStack.EMPTY if there are no matches.
     * It is expected for input to have a consolidated count, and the result can have a count > 1
     */
    public static ItemStack getOutput(ItemStack input, ItemStack ingredient, boolean consumeOnSucces) {
        if (input.isEmpty() || ingredient.isEmpty()) return ItemStack.EMPTY;
        for (AlchemistCauldronRecipe recipe : recipes) {
            ItemStack result = recipe.createOutput(input, ingredient, consumeOnSucces);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns if any cauldron recipe has this as the ingredient
     */
    public static boolean isValidIngredient(ItemStack itemStack) {
        for (AlchemistCauldronRecipe recipe : recipes) {
            if (ItemStack.isSameItemSameComponents(recipe.getIngredient(), itemStack))
                return true;
        }
        return false;
    }

    /**
     * Returns if this combination of items (the count of input matters) yields a result
     */
    public static boolean hasOutput(ItemStack input, ItemStack ingredient) {
        return !getOutput(input, ingredient, false).isEmpty();
    }

    /**
     * Returns an immutable list of all "registered" recipes
     */
    public static ImmutableList<AlchemistCauldronRecipe> getRecipes() {
        return ImmutableList.copyOf(recipes);
    }
}
