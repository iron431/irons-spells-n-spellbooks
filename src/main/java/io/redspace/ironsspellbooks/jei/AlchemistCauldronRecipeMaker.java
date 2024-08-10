package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipeRegistry;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.CauldronPlatformHelper;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AlchemistCauldronRecipeMaker {
    private AlchemistCauldronRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<AlchemistCauldronJeiRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return Stream.of(
                        getScrollRecipes(vanillaRecipeFactory, ingredientManager),
                        getCustomRecipes(vanillaRecipeFactory, ingredientManager),
                        getPotionRecipes(vanillaRecipeFactory, ingredientManager))
                .flatMap(x -> x)
                .toList();
    }

    private static Stream<AlchemistCauldronJeiRecipe> getScrollRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return Arrays.stream(SpellRarity.values())
                .map(AlchemistCauldronRecipeMaker::enumerateSpellsForRarity);
    }

    private static Stream<AlchemistCauldronJeiRecipe> getCustomRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        List<ItemStack> reagents = ingredientManager.getAllItemStacks().stream()
                .filter(AlchemistCauldronRecipeRegistry::isValidIngredient)
                .toList();
        return reagents.stream().flatMap(reagentStack ->
                AlchemistCauldronRecipeRegistry.getRecipes().stream().filter((recipe) -> CauldronPlatformHelper.itemMatches(reagentStack, recipe.getIngredient())).map(recipe ->
                {
                    ItemStack result = recipe.getResult();
                    if (result.getCount() == 4) {
                        result.setCount(1);
                    }
                    return new AlchemistCauldronJeiRecipe(
                            List.of(reagentStack),
                            List.of(result),
                            List.of(recipe.getInput()));
                }));
    }

    private static Stream<AlchemistCauldronJeiRecipe> getPotionRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        if (!ServerConfigs.ALLOW_CAULDRON_BREWING.get()) {
            return Stream.of();
        }

        List<ItemStack> potionReagents = ingredientManager.getAllItemStacks().stream()
                .filter(AlchemistCauldronRecipeMaker::isIngredient)
                .toList();
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return Stream.of();
        }
        return potionReagents.stream().flatMap(reagentStack ->
                ingredientManager.getAllItemStacks().stream().filter((itemStack) -> itemStack.getItem() instanceof PotionItem && BrewingRecipeRegistry.hasOutput(itemStack, reagentStack)).map(baseItem ->
                        new AlchemistCauldronJeiRecipe(
                                List.of(reagentStack),
                                List.of(BrewingRecipeRegistry.getOutput(baseItem, reagentStack)),
                                List.of(baseItem)
                        ))
        );
    }

    private static AlchemistCauldronJeiRecipe enumerateSpellsForRarity(SpellRarity spellRarity) {

        var inputs = new ArrayList<ItemStack>();
        var catalysts = new ArrayList<ItemStack>();
        var outputs = new ArrayList<ItemStack>();
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());

        SpellRegistry.getEnabledSpells().forEach(spell -> {
            IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel())
                    .filter(spellLevel -> spell.getRarity(spellLevel) == spellRarity)
                    .forEach(filteredLevel -> {
                        inputs.add(getScrollStack(scrollStack, spell, filteredLevel));
                    });
        });
        ItemStack ink = new ItemStack(InkItem.getInkForRarity(spellRarity));
        ItemStack waterBottle = new ItemStack(Items.POTION);
        PotionUtils.setPotion(waterBottle, Potions.WATER);
        inputs.forEach((itemStack -> {
            catalysts.add(waterBottle);
            outputs.add(ink);
        }));

        return new AlchemistCauldronJeiRecipe(inputs, outputs, catalysts);
    }


    private static ItemStack getScrollStack(ItemStack stack, AbstractSpell spell, int spellLevel) {
        var scrollStack = stack.copy();
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }

    private static boolean isIngredient(ItemStack itemStack) {
        try {
            return CauldronPlatformHelper.isBrewingIngredient(itemStack, Minecraft.getInstance().level);
        } catch (RuntimeException | LinkageError e) {
            IronsSpellbooks.LOGGER.error("Failed to check if item is a potion reagent {}.", itemStack.toString(), e);
            return false;
        }
    }
}
