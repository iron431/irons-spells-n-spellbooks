package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ModTags;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * - Upgrade scroll: (scroll level x) + (scroll level x) = (scroll level x+1)
 * - Imbue Weapon:   weapon + scroll = imbued weapon with spell/level of scroll
 * - Upgrade item:   item + upgrade orb =
 **/
public final class ScrollForgeRecipeMaker {
    private record FocusToSchool(Item item, SchoolType schoolType) {
        public FocusToSchool(Item item, SchoolType schoolType) {
            this.item = item;
            this.schoolType = schoolType;
        }
    }

    private ScrollForgeRecipeMaker() {
        //private constructor prevents anyone from instantiating this class
    }

    public static List<ScrollForgeRecipe> getRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        var inkItems = ForgeRegistries.ITEMS.getValues().stream().filter(item -> item instanceof InkItem).map(item -> (InkItem) item).toList();
        var recipes = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(ModTags.SCHOOL_FOCUS))
                .map(item -> {
                    var paperInput = new ItemStack(Items.PAPER);
                    var focusInput = new ItemStack(item);
                    var school = SchoolRegistry.getSchoolFromFocus(focusInput);
                    var spells = SpellRegistry.getSpellsForSchool(school);
                    var scrollOutputs = new ArrayList<ItemStack>();
                    var inkOutputs = new ArrayList<ItemStack>();

                    inkItems.forEach(ink -> {
                        //var string = new StringBuilder();
                        //SpellRegistry.REGISTRY.get().getValues().forEach((AbstractSpell)-> string.append(AbstractSpell.getSpellId()).append(", "));
                        for (AbstractSpell spell : spells) {
                            if (spell.isEnabled()) {
                                var spellLevel = spell.getMinLevelForRarity(ink.getRarity());
                                if (spellLevel > 0 && spell != SpellRegistry.none()) {
                                    inkOutputs.add(new ItemStack(ink));
                                    scrollOutputs.add(getScrollStack(spell, spellLevel));
                                }
                            }
                        }
                    });

                    return new ScrollForgeRecipe(inkOutputs, paperInput, focusInput, scrollOutputs);
                });

        return recipes.toList();
    }

    private static ItemStack getScrollStack(AbstractSpell spell, int spellLevel) {
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }
}
