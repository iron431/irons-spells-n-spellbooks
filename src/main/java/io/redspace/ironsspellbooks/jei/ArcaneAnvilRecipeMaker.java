package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
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

    public static List<ArcaneAnvilJeiRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        var visibleItems = getVisibleItems();
        return Stream.of(
                        getScrollRecipes(visibleItems),
                        getImbueRecipes(visibleItems),
                        getUpgradeRecipes(visibleItems),
                        getAffinityAttuneRecipes(visibleItems))
                .flatMap(x -> x)
                .toList();
    }

    private static Stream<ArcaneAnvilJeiRecipe> getScrollRecipes(List<Item> visibleItems) {
        if (!ServerConfigs.SPEC.isLoaded() || ServerConfigs.SCROLL_MERGING.get()) {
            return SpellRegistry.getEnabledSpells().stream()
                    .sorted(Comparator.comparing(AbstractSpell::getSpellId))
                    .flatMap(spell -> IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel() - 1).mapToObj(i -> new ArcaneAnvilJeiRecipe(spell, i)));
        } else {
            return Stream.empty();
        }
    }

    private static Stream<ArcaneAnvilJeiRecipe> getImbueRecipes(List<Item> visibleItems) {
        return visibleItems.stream()
                .filter(item -> Utils.canImbue(new ItemStack(item)))
                .map(item -> new ArcaneAnvilJeiRecipe(item, (AbstractSpell) null));
    }

    private static Stream<ArcaneAnvilJeiRecipe> getUpgradeRecipes(List<Item> visibleItems) {
        var upgradable = visibleItems.stream().filter(item -> Utils.canBeUpgraded(new ItemStack(item))).toList();
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item.components().has(ComponentRegistry.UPGRADE_ORB_TYPE.get()))
                .flatMap(upgradeOrb -> upgradable.stream()
                        .map(item -> new ArcaneAnvilJeiRecipe(item, upgradeOrb)));
    }

    private static Stream<ArcaneAnvilJeiRecipe> getAffinityAttuneRecipes(List<Item> visibleItems) {
        return SpellRegistry.getEnabledSpells().stream()
                .sorted(Comparator.comparing(AbstractSpell::getSpellId))
                .map(ArcaneAnvilJeiRecipe::new);
    }

    public static List<Item> getVisibleItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> CreativeModeTabs.allTabs().stream().anyMatch(tab -> tab.contains(new ItemStack(item)))).toList();
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
