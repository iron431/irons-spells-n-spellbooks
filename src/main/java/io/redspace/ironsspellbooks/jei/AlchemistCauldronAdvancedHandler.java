package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public class AlchemistCauldronAdvancedHandler implements ISimpleRecipeManagerPlugin<AlchemistCauldronJeiRecipe> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        var stack = input.getCastIngredient(VanillaTypes.ITEM_STACK);
        if (stack == null || Minecraft.getInstance().level == null) {
            return false;
        }
        if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && stack.has(DataComponents.POTION_CONTENTS)) {
            return true;
        }
        var m = Minecraft.getInstance().level.getRecipeManager();
        return m.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get()).stream().anyMatch(empty -> empty.value().input().test(stack));
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        var stack = output.getCastIngredient(VanillaTypes.ITEM_STACK);
        if (stack == null || Minecraft.getInstance().level == null) {
            return false;
        }
        if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && stack.has(DataComponents.POTION_CONTENTS)) {
            return true;
        }
        var m = Minecraft.getInstance().level.getRecipeManager();
        return m.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get()).stream().anyMatch(empty -> ItemStack.isSameItemSameComponents(stack, empty.value().result()));
    }

    @Override
    public List<AlchemistCauldronJeiRecipe> getRecipesForInput(ITypedIngredient<?> input) {
        var stack = input.getCastIngredient(VanillaTypes.ITEM_STACK);
        if (stack == null || Minecraft.getInstance().level == null) {
            return List.of();
        }
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        var fluidConversion = manager.getRecipeFor(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get(), new SingleRecipeInput(stack), Minecraft.getInstance().level).map(RecipeHolder::value).map(FillAlchemistCauldronRecipe::result);
        if (fluidConversion.isEmpty()) {
            fluidConversion = Optional.of(PotionFluid.from(stack));
        }
        return fluidConversion.map(inputFluid -> AlchemistCauldronRecipeMaker.recipes.stream()
                        .filter(recipe -> FluidStack.isSameFluidSameComponents(recipe.fluidIn(), inputFluid)).toList())
                .orElse(List.of());
    }

    @Override
    public List<AlchemistCauldronJeiRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
        var stack = output.getCastIngredient(VanillaTypes.ITEM_STACK);
        if (stack == null || Minecraft.getInstance().level == null) {
            return List.of();
        }
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        var fluidConversion = manager.getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get()).stream().map(RecipeHolder::value).filter(emptyAlchemistCauldronRecipe -> ItemStack.isSameItemSameComponents(emptyAlchemistCauldronRecipe.result(), stack))
                .map(EmptyAlchemistCauldronRecipe::fluid).findFirst();
        if (fluidConversion.isEmpty()) {
            if (ServerConfigs.ALLOW_CAULDRON_BREWING.get() && !PotionFluid.from(stack).isEmpty()) {
                fluidConversion = Optional.of(PotionFluid.from(stack));
            }
        }
        return fluidConversion.map(outputFluid -> AlchemistCauldronRecipeMaker.recipes.stream()
                        .filter(recipe -> recipe.results().stream().anyMatch(result -> FluidStack.isSameFluidSameComponents(result, outputFluid))).toList())
                .orElse(List.of());
    }

    @Override
    public List<AlchemistCauldronJeiRecipe> getAllRecipes() {
        return AlchemistCauldronRecipeMaker.recipes;
    }
}
