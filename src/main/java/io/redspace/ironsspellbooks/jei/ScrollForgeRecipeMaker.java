package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
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
                    var school = SchoolType.getSchoolFromItem(focusInput);
                    var spells = SpellType.getSpellsFromSchool(school);
                    var scrollOutputs = new ArrayList<ItemStack>();
                    var inkOutputs = new ArrayList<ItemStack>();

                    inkItems.forEach(ink -> {
                        spells.forEach(spell -> {
                            var spellToUse = spell.getSpellForRarity(ink.getRarity());
                            if (spellToUse.getSpellType() != SpellType.NONE_SPELL) {
                                inkOutputs.add(new ItemStack(ink));
                                scrollOutputs.add(getScrollStack(spellToUse.getSpellType(), spellToUse.getLevel(null)));
                            }
                        });
                    });

                    return new ScrollForgeRecipe(inkOutputs, paperInput, focusInput, scrollOutputs);
                });

        return recipes.toList();
    }

    private static ItemStack getScrollStack(SpellType spellType, int spellLevel) {
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
        SpellData.setSpellData(scrollStack, spellType, spellLevel);
        return scrollStack;
    }
}
