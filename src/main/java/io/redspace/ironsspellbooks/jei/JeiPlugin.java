package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilScreen;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.stream.IntStream;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    public static final String TEXTURE_GUI_PATH = "textures/gui/";
    public static final String TEXTURE_GUI_VANILLA = TEXTURE_GUI_PATH + "gui_vanilla.png";
    public static final String TEXTURE_SCROLL_FORGE = TEXTURE_GUI_PATH + "scroll_forge.png";
    public static final ResourceLocation RECIPE_GUI_VANILLA = new ResourceLocation(IronsSpellbooks.MODID, TEXTURE_GUI_VANILLA);
    public static final ResourceLocation SCROLL_FORGE_GUI = new ResourceLocation(IronsSpellbooks.MODID, TEXTURE_SCROLL_FORGE);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(IronsSpellbooks.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ItemRegistry.SCROLL.get(), SCROLL_INTERPRETER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new ArcaneAnvilRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ScrollForgeRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        registration.addRecipes(ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE, ArcaneAnvilRecipeMaker.getRecipes(vanillaRecipeFactory, ingredientManager));
        registration.addRecipes(ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE, ScrollForgeRecipeMaker.getRecipes(vanillaRecipeFactory, ingredientManager));

        Arrays.stream(SpellType.values()).forEach(spellType -> {
            if (spellType.isEnabled() && spellType != SpellType.NONE_SPELL)
                IntStream.rangeClosed(spellType.getMinLevel(), spellType.getMaxLevel())
                        .forEach((spellLevel) -> {
                            var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
                            SpellData.setSpellData(scrollStack, spellType, spellLevel);
                            registration.addIngredientInfo(scrollStack, VanillaTypes.ITEM_STACK, Component.translatable(String.format("%s.guide", spellType.getComponentId())));
                        });
        });
        registration.addItemStackInfo(new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get()), Component.translatable("item.irons_spellbooks.lightning_bottle.guide"));
        registration.addItemStackInfo(new ItemStack(ItemRegistry.BLOOD_VIAL.get()), Component.translatable("item.irons_spellbooks.blood_vial.guide"));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ArcaneAnvilScreen.class, 102, 48, 22, 15, ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE);
        registration.addRecipeClickArea(ScrollForgeScreen.class, 1, 1, 76, 14, ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ArcaneAnvilMenu.class, MenuRegistry.ARCANE_ANVIL_MENU.get(), ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE, 0, 2, 3, 36);
        registration.addRecipeTransferHandler(ScrollForgeMenu.class, MenuRegistry.SCROLL_FORGE_MENU.get(), ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE, 0, 3, 4, 36);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ARCANE_ANVIL_BLOCK.get()), ArcaneAnvilRecipeCategory.ARCANE_ANVIL_RECIPE_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.SCROLL_FORGE_BLOCK.get()), ScrollForgeRecipeCategory.SCROLL_FORGE_RECIPE_RECIPE_TYPE);
    }

    private static final IIngredientSubtypeInterpreter<ItemStack> SCROLL_INTERPRETER = (stack, context) -> {
        //IronsSpellbooks.LOGGER.debug("SCROLL_INTERPRETER: stack.tag:{} context:{}", stack.getTag(), context);

        if (stack.hasTag()) {
            var spellData = SpellData.getSpellData(stack);
            return String.format("scroll:%d:%d", spellData.getLegacySpellId(), spellData.getLevel());
        }

        return IIngredientSubtypeInterpreter.NONE;
    };
}
