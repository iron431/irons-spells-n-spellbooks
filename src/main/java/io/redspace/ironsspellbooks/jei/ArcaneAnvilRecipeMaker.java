package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * - Upgrade scroll: (scroll level x) + (scroll level x) = (scroll level x+1)
 * - Imbue Weapon:   weapon + scroll = imbued weapon with spell/level of scroll
 * - Upgrade item:   item + upgrade orb =
 **/
public final class ArcaneAnvilRecipeMaker {
    private ArcaneAnvilRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<ArcaneAnvilRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return Stream.of(
                        getScrollRecipes(vanillaRecipeFactory, ingredientManager),
                        getImbueRecipes(vanillaRecipeFactory, ingredientManager),
                        getUpgradeRecipes(vanillaRecipeFactory, ingredientManager))
                .flatMap(x -> x)
                .toList();
    }

    private static Stream<ArcaneAnvilRecipe> getScrollRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return SpellRegistry.getEnabledSpells().stream()
                .sorted(Comparator.comparing(AbstractSpell::getSpellId))
                .flatMap(spell -> IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel()).mapToObj(i -> new ArcaneAnvilRecipe(spell, i)));
        /*.filter(ArcaneAnvilRecipe::isValid)*///Filter out any blank recipes created where min and max spell level are equal
    }

    private static Stream<ArcaneAnvilRecipe> getImbueRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return getVisibleItems().stream()
                .filter(item -> Utils.canImbue(new ItemStack(item)))
                .map(item -> new ArcaneAnvilRecipe(new ItemStack(item), (AbstractSpell) null));
    }

    private static Stream<ArcaneAnvilRecipe> getUpgradeRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof UpgradeOrbItem)
                .flatMap(upgradeOrb ->
                        getVisibleItems().stream()
                                .filter(item -> Utils.canBeUpgraded(new ItemStack(item)))
                                .map(item -> new ArcaneAnvilRecipe(new ItemStack(item), List.of(new ItemStack(upgradeOrb)))));
    }

    public static List<Item> getVisibleItems() {
        return ForgeRegistries.ITEMS.getValues().stream().filter(item -> CreativeModeTabs.allTabs().stream().anyMatch(tab -> tab.contains(new ItemStack(item)))).toList();
    }

//    private static ArcaneAnvilRecipe enumerateScrollCombinations(AbstractSpell spell) {
//        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
//
//        var leftInputs = new ArrayList<ItemStack>();
//        var rightInputs = new ArrayList<ItemStack>();
//        var outputs = new ArrayList<ItemStack>();
//
//        IntStream.range(spell.getMinLevel(), spell.getMaxLevel())
//                .forEach((spellLevel) -> {
//                    leftInputs.add(getScrollStack(scrollStack, spell, spellLevel));
//                    rightInputs.add(getScrollStack(scrollStack, spell, spellLevel));
//                    outputs.add(getScrollStack(scrollStack, spell, spellLevel + 1));
//                });
//
//        return new ArcaneAnvilRecipe(leftInputs, rightInputs, outputs);
//    }

    private static ItemStack getScrollStack(ItemStack stack, AbstractSpell spell, int spellLevel) {
        var scrollStack = stack.copy();
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }
}
