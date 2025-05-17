package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AlchemistCauldronRecipeMaker {
    public static List<AlchemistCauldronJeiRecipe> recipes = List.of();

    private AlchemistCauldronRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    static List<AlchemistCauldronJeiRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        recipes = Stream.of(
                        getScrollRecipes(vanillaRecipeFactory, itemFinder),
                        getCauldronRecipes(vanillaRecipeFactory, itemFinder),
                        getPotionRecipes(vanillaRecipeFactory, itemFinder))
                .flatMap(Function.identity())
                .toList();
        return recipes;
    }

    private static Stream<AlchemistCauldronJeiRecipe> getScrollRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        return Arrays.stream(SpellRarity.values())
                .map(AlchemistCauldronRecipeMaker::enumerateSpellsForRarity);
    }

    private static Stream<AlchemistCauldronJeiRecipe> getCauldronRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        if (Minecraft.getInstance().level == null) {
            return Stream.of();
        }
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        return manager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get()).stream().map(RecipeHolder::value).map(
                recipe -> new AlchemistCauldronJeiRecipe(recipe.reagent(), recipe.fluidIn(), recipe.results(), recipe.byproduct().orElse(ItemStack.EMPTY))
        );
    }

    private static Stream<Item> getBrewingReagents(PotionBrewing potionBrewing) {
        return Stream.concat(
                potionBrewing.containerMixes.stream(),
                potionBrewing.potionMixes.stream()
        ).map(PotionBrewing.Mix::ingredient).flatMap(i -> Arrays.stream(i.getItems())).map(ItemStack::getItem).distinct();
    }

    private static Stream<AlchemistCauldronJeiRecipe> getPotionRecipes(IVanillaRecipeFactory vanillaRecipeFactory, JeiPlugin.ItemFinder itemFinder) {
        if (!ServerConfigs.ALLOW_CAULDRON_BREWING.get()) {
            return Stream.of();
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return Stream.of();
        }
        PotionBrewing potionBrewing = Minecraft.getInstance().level.potionBrewing();
        Stream<ItemStack> brewablePotions = BuiltInRegistries.POTION.holders().flatMap(potion -> Stream.of(
                PotionContents.createItemStack(Items.POTION, potion),
                PotionContents.createItemStack(Items.SPLASH_POTION, potion),
                PotionContents.createItemStack(Items.LINGERING_POTION, potion)));
//        var allIngredients =
        return brewablePotions.flatMap(potion -> getBrewingReagents(potionBrewing)
                .filter(reagent -> potionBrewing.hasMix(potion, reagent.getDefaultInstance())).map(
                        reagent ->
                                new AlchemistCauldronJeiRecipe(
                                        Ingredient.of(reagent),
                                        PotionFluid.from(potion),
                                        List.of(PotionFluid.from(level.potionBrewing().mix(reagent.getDefaultInstance(), potion))),
                                        ItemStack.EMPTY
                                )
                ));
    }

    private static AlchemistCauldronJeiRecipe enumerateSpellsForRarity(SpellRarity spellRarity) {

        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());

        Stream<ItemStack> scrolls = SpellRegistry.getEnabledSpells().stream().flatMap(
                spell -> IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel())
                        .filter(spellLevel -> spell.getRarity(spellLevel) == spellRarity)
                        .mapToObj(i -> getScrollStack(scrollStack, spell, i)));
        FluidStack ink = new FluidStack(InkItem.getInkForRarity(spellRarity).fluid(), 250);
        FluidStack water = new FluidStack(Fluids.WATER, 250);

        return new AlchemistCauldronJeiRecipe(Ingredient.of(scrolls), water, List.of(ink), ItemStack.EMPTY);
    }


    private static ItemStack getScrollStack(ItemStack stack, AbstractSpell spell, int spellLevel) {
        var scrollStack = stack.copy();
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }

    private static boolean isIngredient(ItemStack itemStack) {
        try {
            return Minecraft.getInstance().level.potionBrewing().isIngredient(itemStack);
        } catch (RuntimeException | LinkageError e) {
            IronsSpellbooks.LOGGER.error("Failed to check if item is a potion reagent {}.", itemStack.toString(), e);
            return false;
        }
    }
}
