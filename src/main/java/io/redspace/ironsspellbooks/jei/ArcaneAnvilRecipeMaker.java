package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

    private ArcaneAnvilRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<ArcaneAnvilRecipe> getRecipes() {
        return Stream.of(
                        getScrollRecipes(),
                        getImbueRecipes(),
                        getUpgradeRecipes())
                .flatMap(x -> x)
                .toList();
    }

    private static Stream<ArcaneAnvilRecipe> getScrollRecipes() {
        return Arrays.stream(SpellType.values())
                .sorted(Comparator.comparing(Enum::name))
                .map(ArcaneAnvilRecipeMaker::enumerateScrollCombinations);
    }

    private static Stream<ArcaneAnvilRecipe> getImbueRecipes() {
        return Stream.empty();
    }

    private static Stream<ArcaneAnvilRecipe> getUpgradeRecipes() {
        return Stream.empty();
    }

    private static ArcaneAnvilRecipe enumerateScrollCombinations(SpellType spellType) {
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());

        var leftInputs = new ArrayList<ItemStack>();
        var rightInputs = new ArrayList<ItemStack>();
        var outputs = new ArrayList<ItemStack>();

        IntStream.range(spellType.getMinLevel(), spellType.getMaxLevel())
                .forEach((spellLevel) -> {
                    leftInputs.add(getScrollStack(scrollStack, spellType, spellLevel));
                    rightInputs.add(getScrollStack(scrollStack, spellType, spellLevel));
                    outputs.add(getScrollStack(scrollStack, spellType, spellLevel + 1));
                });

        return new ArcaneAnvilRecipe(leftInputs, rightInputs, outputs);
    }

    private static ItemStack getScrollStack(ItemStack stack, SpellType spellType, int spellLevel) {
        var scrollStack = stack.copy();
        SpellData.setSpellData(scrollStack, spellType, spellLevel);
        return scrollStack;
    }
}
