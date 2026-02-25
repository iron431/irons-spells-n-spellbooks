package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilScreen;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.recipe_types.NoAdditionSmithingTransformRecipe;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    public static final ResourceLocation RECIPE_GUI_VANILLA = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/gui_vanilla.png");
    public static final ResourceLocation ALCHEMIST_CAULDRON_GUI = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/jei_alchemist_cauldron.png");
    public static final ResourceLocation SCROLL_FORGE_GUI = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/scroll_forge.png");

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ItemRegistry.SCROLL.get(), new ScrollJeiInterpreter());
        registration.registerSubtypeInterpreter(ItemRegistry.FURLED_MAP.get(), new FurledMapJeiInterpreter());
        registration.registerSubtypeInterpreter(ItemRegistry.ANCIENT_FURLED_MAP.get(), new FurledMapJeiInterpreter());
        registration.registerSubtypeInterpreter(new FluidSubtype(), FluidRegistry.POTION_FLUID.get(), new PotionFluidInterpreter());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new ArcaneAnvilRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ScrollForgeRecipeCategory(guiHelper));
        registration.addRecipeCategories(new AlchemistCauldronRecipeCategory(guiHelper));
    }

    static class ItemFinder {
        Collection<ItemStack> allItemStacks; // hold on to result because jei does work to discover it
        Set<ArmorItem> ironsArmorItems;
        Set<TieredItem> ironsTieredItems;
        Set<InkItem> inkItems;
        Set<Item> imbueable;
        Set<Item> upgradeable;

        ItemFinder(IIngredientManager ingredientManager) {
            this.allItemStacks = ingredientManager.getAllItemStacks();
            ironsArmorItems = new HashSet<>();
            ironsTieredItems = new HashSet<>();
            inkItems = new HashSet<>();
            imbueable = new HashSet<>();
            upgradeable = new HashSet<>();
            allItemStacks.forEach(stack -> {
                var item = stack.getItem();
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IronsSpellbooks.MODID)) {
                    if (item instanceof ArmorItem armorItem) {
                        ironsArmorItems.add(armorItem);
                    } else if (item instanceof TieredItem tieredItem) {
                        ironsTieredItems.add(tieredItem);
                    }
                }
                if (item instanceof InkItem inkItem) {
                    inkItems.add(inkItem);
                }
                if (Utils.canImbue(stack)) {
                    imbueable.add(item);
                }
                if (Utils.canBeUpgraded(stack)) {
                    upgradeable.add(item);
                }
            });
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        ItemFinder itemFinder = new ItemFinder(ingredientManager);
        registration.addRecipes(ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE, ArcaneAnvilRecipeMaker.getRecipes(vanillaRecipeFactory, itemFinder));
        registration.addRecipes(ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE, ScrollForgeRecipeMaker.getRecipes(vanillaRecipeFactory, itemFinder));
        registration.addRecipes(AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE, AlchemistCauldronRecipeMaker.getRecipes(vanillaRecipeFactory, itemFinder));
        registration.addRecipes(RecipeTypes.ANVIL, VanillaAnvilRecipeMaker.getAnvilRepairRecipes(vanillaRecipeFactory, itemFinder));
//        registration.addRecipes(RecipeTypes.SMITHING, VanillaAnvilRecipeMaker.getCustomSmithingRecipes(vanillaRecipeFactory, itemFinder));
        SpellRegistry.REGISTRY.stream().forEach(spell -> {
            if (spell.isEnabled() && spell != SpellRegistry.none()) {
                var list = new ArrayList<ItemStack>();
                IntStream.rangeClosed(spell.getMinLevel(), spell.getMaxLevel())
                        .forEach((spellLevel) -> {
                            var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
                            ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
                            list.add(scrollStack);
                        });
                registration.addIngredientInfo(list, VanillaTypes.ITEM_STACK, Component.translatable(String.format("%s.guide", spell.getComponentId())));
            }
        });
        registration.addItemStackInfo(new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get()), Component.translatable("item.irons_spellbooks.lightning_bottle.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.BLOOD_VIAL.get()), Component.translatable("item.irons_spellbooks.blood_vial.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get()), Component.translatable("item.irons_spellbooks.frozen_bone.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.HOGSKIN.get()), Component.translatable("item.irons_spellbooks.hogskin.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.DRAGONSKIN.get()), Component.translatable("item.irons_spellbooks.dragonskin.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.RUINED_BOOK.get()), Component.translatable("item.irons_spellbooks.ruined_book.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.CINDER_ESSENCE.get()), Component.translatable("item.irons_spellbooks.cinder_essence.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.LIGHTNING_ROD_STAFF.get()), Component.translatable("item.irons_spellbooks.lightning_rod.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.CURSED_DOLL_SPELLBOOK.get()), Component.translatable("item.irons_spellbooks.cursed_doll_spell_book.guide"));
        registration.addIngredientInfo(ItemRegistry.FURLED_MAP.get(), Component.translatable("item.irons_spellbooks.furled_map.guide"));
        registration.addIngredientInfo(ItemRegistry.ANCIENT_FURLED_MAP.get(), Component.translatable("item.irons_spellbooks.furled_map.guide"));
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ArcaneAnvilScreen.class, 102, 48, 22, 15, ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE);
        registration.addRecipeClickArea(ScrollForgeScreen.class, 1, 1, 76, 14, ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ArcaneAnvilMenu.class, MenuRegistry.ARCANE_ANVIL_MENU.get(), ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE, 0, 2, 3, 36);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ARCANE_ANVIL_BLOCK.get()), ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.SCROLL_FORGE_BLOCK.get()), ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ALCHEMIST_CAULDRON.get()), AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE, new AlchemistCauldronAdvancedHandler());
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getSmithingCategory().addExtension(NoAdditionSmithingTransformRecipe.class, new NoAdditionSmithingExtension());
    }
}
