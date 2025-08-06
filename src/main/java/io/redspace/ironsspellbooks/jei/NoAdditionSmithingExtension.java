package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.recipe_types.NoAdditionSmithingTransformRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.crafting.Ingredient;

public class NoAdditionSmithingExtension implements ISmithingCategoryExtension<NoAdditionSmithingTransformRecipe> {
    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(NoAdditionSmithingTransformRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getTemplate());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(NoAdditionSmithingTransformRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getBase());

    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(NoAdditionSmithingTransformRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(Ingredient.EMPTY);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(NoAdditionSmithingTransformRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getResult());
    }
}
