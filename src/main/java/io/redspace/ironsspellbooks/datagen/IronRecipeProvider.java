package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

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

        cauldronBottledInteraction(recipeOutput, ItemRegistry.BLOOD_VIAL, FluidRegistry.BLOOD);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INK_COMMON, FluidRegistry.COMMON_INK);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INK_UNCOMMON, FluidRegistry.UNCOMMON_INK);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INK_RARE, FluidRegistry.RARE_INK);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INK_EPIC, FluidRegistry.EPIC_INK);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INK_LEGENDARY, FluidRegistry.LEGENDARY_INK);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.OAKSKIN_ELIXIR, FluidRegistry.OAKSKIN_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.GREATER_OAKSKIN_ELIXIR, FluidRegistry.GREATER_OAKSKIN_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.EVASION_ELIXIR, FluidRegistry.EVASION_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.GREATER_EVASION_ELIXIR, FluidRegistry.GREATER_EVASION_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.INVISIBILITY_ELIXIR, FluidRegistry.INVISIBILITY_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.GREATER_INVISIBILITY_ELIXIR, FluidRegistry.GREATER_INVISIBILITY_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.GREATER_HEALING_POTION, FluidRegistry.GREATER_HEALING_ELIXIR_FLUID);
        cauldronBottledInteraction(recipeOutput, ItemRegistry.TIMELESS_SLURRY, FluidRegistry.TIMELESS_SLURRY_FLUID);

        // fixme: modded buckets, even with water, wont work
        new FillAlchemistCauldronRecipe.Builder()
                .withInput(Items.WATER_BUCKET)
                .withReturnItem(Items.BUCKET)
                .withFluid(new FluidStack(Fluids.WATER, 1000))
                .withSound(SoundEvents.BUCKET_EMPTY)
                .mustFitAll(false)
                .save(recipeOutput);
        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(Items.BUCKET)
                .withReturnItem(Items.WATER_BUCKET)
                .withFluid(new FluidStack(Fluids.WATER, 1000))
                .withSound(SoundEvents.BUCKET_FILL)
                .save(recipeOutput);


        // Upgrade common ink -> uncommon
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.COMMON_INK, 1000)
                .withReagent(Tags.Items.INGOTS_COPPER)
                .withResult(FluidRegistry.UNCOMMON_INK, 250)
                .save(recipeOutput);
        // Upgrade uncommon ink -> rare
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.UNCOMMON_INK, 1000)
                .withReagent(Tags.Items.INGOTS_IRON)
                .withResult(FluidRegistry.RARE_INK, 250)
                .save(recipeOutput);
        // Upgrade rare ink -> epic
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.RARE_INK, 1000)
                .withReagent(Tags.Items.INGOTS_GOLD)
                .withResult(FluidRegistry.EPIC_INK, 250)
                .save(recipeOutput);
        // Upgrade epic ink -> legendary
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.EPIC_INK, 1000)
                .withReagent(Tags.Items.GEMS_AMETHYST)
                .withResult(FluidRegistry.LEGENDARY_INK, 250)
                .save(recipeOutput);

        //Elixir Recipes
        //oakskin
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(500, Potions.STRONG_HEALING, PotionFluid.BottleType.REGULAR))
                .withReagent(Items.OAK_LOG)
                .withResult(FluidRegistry.OAKSKIN_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.OAKSKIN_ELIXIR_FLUID, 500)
                .withReagent(Items.AMETHYST_SHARD)
                .withResult(FluidRegistry.GREATER_OAKSKIN_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        //evasion
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(1000, PotionRegistry.INSTANT_MANA_THREE, PotionFluid.BottleType.REGULAR))
                .withReagent(Items.ENDER_PEARL)
                .withResult(FluidRegistry.EVASION_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.EVASION_ELIXIR_FLUID, 250)
                .withReagent(Items.DRAGON_BREATH)
                .withResult(FluidRegistry.GREATER_EVASION_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        //invisibility
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(1000, Potions.LONG_INVISIBILITY, PotionFluid.BottleType.REGULAR))
                .withReagent(ItemRegistry.SHRIVING_STONE.get())
                .withResult(FluidRegistry.INVISIBILITY_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.INVISIBILITY_ELIXIR_FLUID, 250)
                .withReagent(Items.AMETHYST_CLUSTER)
                .withResult(FluidRegistry.GREATER_INVISIBILITY_ELIXIR_FLUID, 250)
                .save(recipeOutput);
        // healing
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(1000, Potions.STRONG_HEALING, PotionFluid.BottleType.REGULAR))
                .withReagent(Items.AMETHYST_SHARD)
                .withResult(FluidRegistry.GREATER_HEALING_ELIXIR_FLUID, 250)
                .save(recipeOutput);

        //Soak recipes
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.EVASION_ELIXIR_FLUID, 500)
                .withReagent(Items.OBSIDIAN)
                .withByproduct(Items.CRYING_OBSIDIAN)
                .saveSoak(recipeOutput);

        // Misc
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(250, Potions.MUNDANE, PotionFluid.BottleType.REGULAR))
                .withReagent(Items.ECHO_SHARD)
                .withResult(FluidRegistry.TIMELESS_SLURRY_FLUID, 250)
                .save(recipeOutput);

    }

    /**
     * creates recipe for filling the cauldron via this item, and emptying the cauldron to this item, via a glass bottle
     */
    public static void cauldronBottledInteraction(RecipeOutput output, Holder<Item> item, Holder<Fluid> fluid) {
        cauldronTwoWayInteraction(output, item, Holder.direct(Items.GLASS_BOTTLE), fluid, 250);
    }

    /**
     * creates recipe for filling the cauldron via this item, and emptying the cauldron to this item
     */
    public static void cauldronTwoWayInteraction(RecipeOutput output, Holder<Item> item, Holder<Item> vessel, Holder<Fluid> fluid, int amount) {
        new FillAlchemistCauldronRecipe.Builder()
                .withFluid(fluid, amount)
                .withInput(item.value())
                .withReturnItem(vessel.value())
                .save(output);
        new EmptyAlchemistCauldronRecipe.Builder()
                .withInput(vessel.value())
                .withReturnItem(item.value())
                .withFluid(fluid, amount)
                .save(output);
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
