package io.redspace.ironsspellbooks.jei;

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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AlchemistCauldronRecipeCategory implements IRecipeCategory<AlchemistCauldronJeiRecipe> {
    public static final RecipeType<AlchemistCauldronJeiRecipe> ALCHEMIST_CAULDRON_RECIPE_TYPE = RecipeType.create(IronsSpellbooks.MODID, "alchemist_cauldron", AlchemistCauldronJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable cauldron_block_icon;
    private final String inputSlotName = "itemIn";
    private final String fluidInputSlotName = "fluidIn";
    private final String outputSlotNameBase = "outputSlot";
    private final String byproductSlotName = "byproductSlot";
    private final int paddingBottom = 20;

    public AlchemistCauldronRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(JeiPlugin.ALCHEMIST_CAULDRON_GUI, 0, 0, 125, 19)
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
        int fluidRenderHeight = 16;
        IRecipeSlotBuilder itemInput = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(Arrays.stream(recipe.itemIn().getItems()).toList())
                .setSlotName(inputSlotName);

        IRecipeSlotBuilder fluidInput = builder.addSlot(RecipeIngredientRole.INPUT, 54, 1 + 16 - fluidRenderHeight)
                .addFluidStack(recipe.fluidIn().getFluid(), recipe.fluidIn().getAmount(), recipe.fluidIn().getComponentsPatch())
                .setFluidRenderer(recipe.fluidIn().getAmount(), false, 16, fluidRenderHeight)
                .setSlotName(fluidInputSlotName);

        if (!recipe.results().isEmpty()) {
            int width = 16 / recipe.results().size();
            int diff = 16 - width * recipe.results().size();
            int xpos = 108;
            int maxCap = recipe.results().stream().mapToInt(FluidStack::getAmount).max().getAsInt();
            for (int i = 0; i < recipe.results().size(); i++) {
                int w = width + (i == 0 ? diff : 0);
                var stack = recipe.results().get(i);
                IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, xpos, 1 + 16 - fluidRenderHeight)
                        .addFluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch())
                        .setFluidRenderer(maxCap, false, w, fluidRenderHeight)
                        .setSlotName(outputSlotNameBase + i);
                xpos += w;
            }
        }
        if (!recipe.resultByproduct().isEmpty()) {
            int ypos = recipe.results().isEmpty() ? 1 : 17;
            IRecipeSlotBuilder byproductSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, ypos)
                    .addItemStacks(List.of(recipe.resultByproduct()))
                    .setSlotName(byproductSlotName);
        }

    }


    @Override
    public void draw(@NotNull AlchemistCauldronJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiHelper, double mouseX, double mouseY) {
        Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(inputSlotName)
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
