package io.redspace.ironsspellbooks.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AlchemistCauldronRecipeCategory implements IRecipeCategory<AlchemistCauldronJeiRecipe> {
    public static final RecipeType<AlchemistCauldronJeiRecipe> ALCHEMIST_CAULDRON_RECIPE_TYPE = RecipeType.create(IronsSpellbooks.MODID, "alchemist_cauldron", AlchemistCauldronJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable cauldron_block_icon;
    private final String inputSlotName = "inputSlot";
    private final String catalystSlotName = "catalystSlot";
    private final String outputSlotName = "outputSlot";
    private final int paddingBottom = 20;

    public AlchemistCauldronRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(JeiPlugin.ALCHEMIST_CAULDRON_GUI, 0, 0, 125, 18)
                .addPadding(0, paddingBottom, 0, 0)
                .build();
        cauldron_block_icon = guiHelper.createDrawableItemStack(new ItemStack(BlockRegistry.ALCHEMIST_CAULDRON.get()));
    }

    @Override
    public RecipeType<AlchemistCauldronJeiRecipe> getRecipeType() {
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
        return cauldron_block_icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemistCauldronJeiRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.inputs();
        List<ItemStack> catalysts = recipe.catalysts();
        List<ItemStack> outputs = recipe.outputs();

        IRecipeSlotBuilder leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(inputs)
                .setSlotName(inputSlotName);

        IRecipeSlotBuilder rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 54, 1)
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
    public void draw(@NotNull AlchemistCauldronJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiHelper, double mouseX, double mouseY) {
        Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(inputSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        Optional<ItemStack> rightStack = recipeSlotsView.findSlotByName(catalystSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        Optional<ItemStack> outputStack = recipeSlotsView.findSlotByName(outputSlotName)
                .flatMap(IRecipeSlotView::getDisplayedItemStack);

        guiHelper.pose().pushPose();
        {
            guiHelper.pose().translate((getWidth() / 2) - 8 * 1.4f, (getHeight() / 2) - 2, 0);
            guiHelper.pose().scale(1.4f, 1.4f, 1.4f);
            cauldron_block_icon.draw(guiHelper);
        }
        guiHelper.pose().popPose();

        if (leftStack.isPresent() && leftStack.get().is(ItemRegistry.SCROLL.get())) {
            var inputText = String.format("%s%%", (int) (ServerConfigs.SCROLL_RECYCLE_CHANCE.get() * 100));

            var font = Minecraft.getInstance().font;
            int y = (getHeight() / 2);
            int x = (getWidth() - font.width(inputText)) * 3 / 4;
            guiHelper.drawString(font, inputText, x, y, Math.min(ServerConfigs.SCROLL_RECYCLE_CHANCE.get(), 1d) == 1d ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor());
        }
    }
}
