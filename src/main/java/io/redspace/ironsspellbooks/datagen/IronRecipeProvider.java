package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
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

        schoolArmorSmithing(recipeOutput, SchoolRegistry.FIRE.get(), "pyromancer");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.ICE.get(), "cryomancer");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.LIGHTNING.get(), "electromancer");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.HOLY.get(), "priest");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.BLOOD.get(), "cultist");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.ENDER.get(), "shadowwalker");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.EVOCATION.get(), "archevoker");
        schoolArmorSmithing(recipeOutput, SchoolRegistry.NATURE.get(), "plagued");

        upgradeOrbRecipe(recipeOutput, ItemRegistry.FIRE_RUNE.get(), ItemRegistry.FIRE_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.ICE_RUNE.get(), ItemRegistry.ICE_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.LIGHTNING_RUNE.get(), ItemRegistry.LIGHTNING_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.EVOCATION_RUNE.get(), ItemRegistry.EVOCATION_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.HOLY_RUNE.get(), ItemRegistry.HOLY_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.ENDER_RUNE.get(), ItemRegistry.ENDER_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.BLOOD_RUNE.get(), ItemRegistry.BLOOD_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.NATURE_RUNE.get(), ItemRegistry.NATURE_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.COOLDOWN_RUNE.get(), ItemRegistry.COOLDOWN_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.PROTECTION_RUNE.get(), ItemRegistry.PROTECTION_UPGRADE_ORB.get());
        upgradeOrbRecipe(recipeOutput, ItemRegistry.MANA_RUNE.get(), ItemRegistry.MANA_UPGRADE_ORB.get());

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
        cauldronBottledInteraction(recipeOutput, ItemRegistry.ICE_VENOM_VIAL, FluidRegistry.ICE_VENOM_FLUID);
        //todo: reimplement ice spider lure mechanics
//        cauldronBottledInteraction(recipeOutput, ItemRegistry.ICE_SPIDER_PHEROMONES, FluidRegistry.ICE_SPIDER_PHEROMONE_FLUID);

        // fixme: modded buckets, even with water, wont work
        //  update: is this what the #c:buckets/water tag is for?
        //          not sure how to return the correct bucket after the fact
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
        BrewAlchemistCauldronRecipe.builder()
                .withInput(FluidRegistry.BLOOD, 1000)
                .withReagent(ItemRegistry.HOGSKIN.get())
                .withByproduct(ItemRegistry.BLOODY_VELLUM)
                .saveSoak(recipeOutput);

        // Misc
        BrewAlchemistCauldronRecipe.builder()
                .withInput(PotionFluid.of(250, Potions.MUNDANE, PotionFluid.BottleType.REGULAR))
                .withReagent(Items.ECHO_SHARD)
                .withResult(FluidRegistry.TIMELESS_SLURRY_FLUID, 250)
                .save(recipeOutput);
        BrewAlchemistCauldronRecipe.builder()
                .withInput(new FluidStack(Fluids.WATER, 250))
                .withReagent(ItemRegistry.ICY_FANG.get())
                .withResult(FluidRegistry.ICE_VENOM_FLUID, 250)
                .save(recipeOutput);
        //todo: reimplement ice spider lure mechanics
//        BrewAlchemistCauldronRecipe.builder()
//                .withInput(new FluidStack(FluidRegistry.ICE_VENOM_FLUID, 250))
//                .withReagent(Items.PORKCHOP)
//                .withResult(FluidRegistry.ICE_SPIDER_PHEROMONE_FLUID, 1000)
//                .save(recipeOutput);


    }

    /**
     * creates smithing recipe for school rune + wizard armor = school armor, for boots, leggings, chestplate, helmet
     */
    public static void schoolArmorSmithing(RecipeOutput output, SchoolType school, String armorName) {
        var base = new TagKey<?>[]{ModTags.BASE_WIZARD_BOOTS, ModTags.BASE_WIZARD_LEGGINGS, ModTags.BASE_WIZARD_CHESTPLATE, ModTags.BASE_WIZARD_HELMET};
        var slots = new ArmorItem.Type[]{ArmorItem.Type.BOOTS, ArmorItem.Type.LEGGINGS, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.HELMET};
//        var armors = new Item[]{ItemRegistry.WIZARD_BOOTS.get(), ItemRegistry.WIZARD_LEGGINGS.get(), ItemRegistry.WIZARD_CHESTPLATE.get(), ItemRegistry.WIZARD_HELMET.get()};
//        var slots = new ArmorItem.Type[]{ArmorItem.Type.BOOTS, ArmorItem.Type.LEGGINGS, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.HELMET};
        ResourceLocation schoolId = SchoolRegistry.REGISTRY.getKey(school);
        for (int i = 0; i < 4; i++) {
            var tag = (TagKey<Item>) base[i];
            Ingredient baseArmor = Ingredient.of(tag);
//            if (baseArmor.hasNoItems()) continue;
//            Item armorItem = baseArmor.getItems()[0].getItem();
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(schoolId.getNamespace(), String.format("%s_%s", armorName, slots[i].getName()));
            Item rune = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(schoolId.getNamespace(), String.format("%s_rune", schoolId.getPath())));
            ItemStack result = BuiltInRegistries.ITEM.get(itemId).getDefaultInstance();
            Item essence = ItemRegistry.ARCANE_ESSENCE.get();
            output.accept(itemId.withSuffix("_smithing"),
                    new SmithingTransformRecipe(Ingredient.of(rune), baseArmor, Ingredient.of(essence), result),
                    null
            );
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                    .requires(baseArmor)
                    .requires(rune)
                    .requires(essence)
                    .unlockedBy("unlocked", has(tag))
                    .save(output, itemId.withSuffix("_crafting"));
        }
    }

    /**
     * creates smithing recipe for school rune + wizard armor = school armor, for boots, leggings, chestplate, helmet
     */
    public static void upgradeOrbRecipe(RecipeOutput output, Item rune, Item result) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .define('R', rune)
                .define('O', ItemRegistry.UPGRADE_ORB.get())
                .pattern("RRR")
                .pattern("ROR")
                .pattern("RRR")
                .unlockedBy("orb", has(ItemRegistry.UPGRADE_ORB.get()))
                .save(output);
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
