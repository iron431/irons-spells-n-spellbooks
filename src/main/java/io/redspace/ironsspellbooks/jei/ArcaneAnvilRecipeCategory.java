package io.redspace.ironsspellbooks.jei;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArcaneAnvilRecipeCategory implements IRecipeCategory<ArcaneAnvilRecipe> {
    public static final RecipeType<ArcaneAnvilRecipe> ARCANE_ANVIL_RECIPE_RECIPE_TYPE = RecipeType.create(IronsSpellbooks.MODID, "arcane_anvil", ArcaneAnvilRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final String leftSlotName = "leftSlot";
    private final String rightSlotName = "rightSlot";

    public ArcaneAnvilRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createDrawable(IronsSpellbooksJeiPlugin.RECIPE_GUI_VANILLA, 0, 168, 125, 18);
        icon = guiHelper.createDrawableItemStack(new ItemStack(BlockRegistry.ARCANE_ANVIL_BLOCK.get()));
    }

    @Override
    public RecipeType<ArcaneAnvilRecipe> getRecipeType() {
        return ARCANE_ANVIL_RECIPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return BlockRegistry.ARCANE_ANVIL_BLOCK.get().getName();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ArcaneAnvilRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> leftInputs = recipe.leftInputs();
        List<ItemStack> rightInputs = recipe.rightInputs();
        List<ItemStack> outputs = recipe.outputs();

        IRecipeSlotBuilder leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(leftInputs)
                .setSlotName(leftSlotName);

        IRecipeSlotBuilder rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 50, 1)
                .addItemStacks(rightInputs)
                .setSlotName(rightSlotName);

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
                .addItemStacks(outputs);

        if (leftInputs.size() == rightInputs.size()) {
            if (leftInputs.size() == outputs.size()) {
                builder.createFocusLink(leftInputSlot, rightInputSlot, outputSlot);
            }
        } else if (leftInputs.size() == outputs.size() && rightInputs.size() == 1) {
            builder.createFocusLink(leftInputSlot, outputSlot);
        } else if (rightInputs.size() == outputs.size() && leftInputs.size() == 1) {
            builder.createFocusLink(rightInputSlot, outputSlot);
        }
    }
}
