package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.api.backwards_compat.FluidHelper;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Optional;

public class AlchemistCauldronAdvancedHandler implements IRecipeManagerPlugin {
    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        return List.of(AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE);
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (!(recipeCategory instanceof AlchemistCauldronRecipeCategory cauldronRecipeCategory)) {
            return List.of();
        }
        var ingredient = focus.getTypedValue();
        if (focus.getRole() == RecipeIngredientRole.INPUT) {
            if (isHandledInput(ingredient)) {
                // safe cast due to instanceof
                return (List<T>) getRecipesForInput(ingredient);
            }
        } else if (focus.getRole() == RecipeIngredientRole.OUTPUT) {
            if (isHandledOutput(ingredient)) {
                // safe cast due to instanceof
                return (List<T>) getRecipesForOutput(ingredient);
            }
        }
        return List.of();
    }

    //    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        var stack = input.getIngredient(VanillaTypes.ITEM_STACK).orElse(null);
        if (stack == null || Minecraft.getInstance().level == null) {
            return false;
        }
        if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && FluidHelper.hasPotionContents(stack)) {
            return true;
        }
        var m = Minecraft.getInstance().level.getRecipeManager();
        return m.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get()).stream().anyMatch(empty -> empty.input().test(stack));
    }

    //    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        var stack = output.getIngredient(VanillaTypes.ITEM_STACK).orElse(null);
        if (stack == null || Minecraft.getInstance().level == null) {
            return false;
        }
        if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && FluidHelper.hasPotionContents(stack)) {
            return true;
        }
        var m = Minecraft.getInstance().level.getRecipeManager();
        return m.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get()).stream().anyMatch(empty -> ItemStack.isSameItemSameTags(stack, empty.result()));
    }

    //    @Override
    public List<AlchemistCauldronJeiRecipe> getRecipesForInput(ITypedIngredient<?> input) {
        var stackopt = input.getIngredient(VanillaTypes.ITEM_STACK);
        if (stackopt.isEmpty() || Minecraft.getInstance().level == null) {
            return List.of();
        }
        var stack = stackopt.get();
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        var fluidConversion = manager.getRecipeFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get(), new SimpleContainer(stack), Minecraft.getInstance().level)
//                .map(RecipeHolder::value)
                .map(FillAlchemistCauldronRecipe::result);
        if (fluidConversion.isEmpty()) {
            fluidConversion = Optional.of(PotionFluid.from(stack));
        }
        return fluidConversion.map(inputFluid -> AlchemistCauldronRecipeMaker.recipes.stream()
                        .filter(recipe -> FluidHelper.isSameFluidSameComponents(recipe.fluidIn(), inputFluid)).toList())
                .orElse(List.of());
    }

    //    @Override
    public List<AlchemistCauldronJeiRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
        var stackopt = output.getIngredient(VanillaTypes.ITEM_STACK);
        if (stackopt.isEmpty() || Minecraft.getInstance().level == null) {
            return List.of();
        }
        var stack = stackopt.get();
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        var fluidConversion = manager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get())
                .stream()
//                .map(RecipeHolder::value)
                .filter(emptyAlchemistCauldronRecipe -> ItemStack.isSameItemSameTags(emptyAlchemistCauldronRecipe.result(), stack))
                .map(EmptyAlchemistCauldronRecipe::fluid).findFirst();
        if (fluidConversion.isEmpty()) {
            if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && !PotionFluid.from(stack).isEmpty()) {
                fluidConversion = Optional.of(PotionFluid.from(stack));
            }
        }
        return fluidConversion.map(outputFluid -> AlchemistCauldronRecipeMaker.recipes.stream()
                        .filter(recipe -> recipe.results().stream().anyMatch(result -> FluidHelper.isSameFluidSameComponents(result, outputFluid))).toList())
                .orElse(List.of());
    }

    public List<AlchemistCauldronJeiRecipe> getAllRecipes() {
        return AlchemistCauldronRecipeMaker.recipes;
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof AlchemistCauldronRecipeCategory) {
            // safe cast due to instanceof
            return (List<T>) getAllRecipes();
        } else {
            return List.of();
        }
    }
}

