package io.redspace.ironsspellbooks.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AlchemistCauldronRecipeCategory implements IRecipeCategory<AlchemistCauldronRecipe> {
    public static final RecipeType<AlchemistCauldronRecipe> ALCHEMIST_CAULDRON_RECIPE_TYPE = RecipeType.create(IronsSpellbooks.MODID, "alchemist_cauldron", AlchemistCauldronRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final String inputSlotName = "inputSlot";
    private final String catalystSlotName = "catalystSlot";
    private final String outputSlotName = "outputSlot";
    private final int paddingBottom = 15;

    public AlchemistCauldronRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(JeiPlugin.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
                .addPadding(0, paddingBottom, 0, 0)
                .build();
        icon = guiHelper.createDrawableItemStack(new ItemStack(BlockRegistry.ALCHEMIST_CAULDRON.get()));
    }

    @Override
    public RecipeType<AlchemistCauldronRecipe> getRecipeType() {
        return ALCHEMIST_CAULDRON_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return BlockRegistry.ALCHEMIST_CAULDRON.get().getName();
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemistCauldronRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.inputs();
        List<ItemStack> catalysts = recipe.catalysts();
        List<ItemStack> outputs = recipe.outputs();

        IRecipeSlotBuilder leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(inputs)
                .setSlotName(inputSlotName);

        IRecipeSlotBuilder rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 50, 1)
                .addItemStacks(catalysts)
                .setSlotName(catalystSlotName);

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
                .addItemStacks(outputs)
                .setSlotName(outputSlotName);

        if (inputs.size() == catalysts.size()) {
            if (inputs.size() == outputs.size()) {
                builder.createFocusLink(leftInputSlot, rightInputSlot, outputSlot);
            }
        } else if (inputs.size() == outputs.size() && catalysts.size() == 1) {
            builder.createFocusLink(leftInputSlot, outputSlot);
        } else if (catalysts.size() == outputs.size() && inputs.size() == 1) {
            builder.createFocusLink(rightInputSlot, outputSlot);
        }
    }

    @Override
    public void draw(@NotNull AlchemistCauldronRecipe recipe, IRecipeSlotsView recipeSlotsView, @NotNull PoseStack poseStack, double mouseX, double mouseY) {
        Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(inputSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        Optional<ItemStack> rightStack = recipeSlotsView.findSlotByName(catalystSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        Optional<ItemStack> outputStack = recipeSlotsView.findSlotByName(outputSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        if (leftStack.isEmpty() || rightStack.isEmpty() || outputStack.isEmpty()) {
            return;
        }

        var minecraft = Minecraft.getInstance();
        drawRecipe(minecraft, poseStack, leftStack.get(), rightStack.get(), outputStack.get());
    }

    private void drawRecipe(Minecraft minecraft, PoseStack poseStack, ItemStack inputStack, ItemStack catalystStack, ItemStack outputStack) {
        var inputSpellData = SpellData.getSpellData(inputStack);
        var inputText = String.format("L%d", inputSpellData.getLevel());
        var inputColor = inputSpellData.getSpell().getRarity().getChatFormatting().getColor().intValue();

        var outputSpellData = SpellData.getSpellData(outputStack);
        var outputText = String.format("L%d", outputSpellData.getLevel());
        var outputColor = outputSpellData.getSpell().getRarity().getChatFormatting().getColor().intValue();

        int y = (getHeight() / 2) + (paddingBottom / 2) + (minecraft.font.lineHeight / 2) - 4;

        //Left Item
        int x = 3;
        minecraft.font.drawShadow(poseStack, inputText, x, y, inputColor);

        //Right Item
        x += 50;
        minecraft.font.drawShadow(poseStack, inputText, x, y, inputColor);

        //Output Item
        int outputWidth = minecraft.font.width(outputText);
        minecraft.font.drawShadow(poseStack, outputText, getWidth() - (outputWidth + 3), y, outputColor);
    }
}
