package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class IronRecipeProvider extends RecipeProvider {
    public IronRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        quadRingSalvageRecipe(recipeOutput, ItemRegistry.FIREWARD_RING.get(), Ingredient.of(ItemRegistry.CINDER_ESSENCE.get()));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.FROSTWARD_RING.get(), Ingredient.of(ItemRegistry.ICE_CRYSTAL.get()));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.POISONWARD_RING.get(), Ingredient.of(ItemRegistry.NATURE_RUNE.get()));
        quadRingSalvageRecipe(recipeOutput, ItemRegistry.COOLDOWN_RING.get(), Ingredient.of(Tags.Items.INGOTS_COPPER));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.CAST_TIME_RING.get(), Ingredient.of(Items.AMETHYST_SHARD));
        simpleNecklaceSalvageRecipe(recipeOutput, ItemRegistry.HEAVY_CHAIN.get(), Ingredient.of(Items.CHAIN), Ingredient.of(Items.CHAIN));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.EMERALD_STONEPLATE_RING.get(), Ingredient.of(Items.EXPERIENCE_BOTTLE));
        simpleNecklaceSalvageRecipe(recipeOutput, ItemRegistry.CONJURERS_TALISMAN.get(), Ingredient.of(Items.SKELETON_SKULL), Ingredient.of(Items.STRING));
        simpleNecklaceSalvageRecipe(recipeOutput, ItemRegistry.CONCENTRATION_AMULET.get(), Ingredient.of(ItemRegistry.MITHRIL_INGOT.get()), Ingredient.of(Items.CHAIN));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.AFFINITY_RING.get(), Ingredient.of(Items.BUCKET));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.EXPULSION_RING.get(), Ingredient.of(Items.WIND_CHARGE));
        simpleRingSalvageRecipe(recipeOutput, ItemRegistry.VISIBILITY_RING.get(), Ingredient.of(Items.SPYGLASS));
    }

    protected void simpleRingSalvageRecipe(RecipeOutput output, Item result, Ingredient modifier) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .define('M', modifier)
                .define('X', ItemRegistry.MITHRIL_SCRAP.get())
                .pattern("M ")
                .pattern(" X")
                .unlockedBy("mithril_scrap", has(ItemRegistry.MITHRIL_SCRAP.get()))
                .save(output);
    }

    protected void simpleNecklaceSalvageRecipe(RecipeOutput output, Item result, Ingredient modifier, Ingredient strap) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .define('M', modifier)
                .define('X', ItemRegistry.MITHRIL_SCRAP.get())
                .define('S', strap)
                .pattern(" S ")
                .pattern("SXS")
                .pattern(" M ")
                .unlockedBy("mithril_scrap", has(ItemRegistry.MITHRIL_SCRAP.get()))
                .save(output);
    }

    protected void quadRingSalvageRecipe(RecipeOutput output, Item result, Ingredient modifier) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .define('M', modifier)
                .define('X', ItemRegistry.MITHRIL_SCRAP.get())
                .pattern(" M ")
                .pattern("MXM")
                .pattern(" M ")
                .unlockedBy("mithril_scrap", has(ItemRegistry.MITHRIL_SCRAP.get()))
                .save(output);
    }

}
