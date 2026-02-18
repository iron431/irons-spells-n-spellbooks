package io.redspace.ironsspellbooks.block.transmog_table;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.statue.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/color_picker.png");

    private static final int PICKER_X = 8;
    private static final int PICKER_Y = 20;
    private static final int PICKER_WIDTH = 100;
    private static final int PICKER_HEIGHT = 64;

    int leftPos, topPos;
    int imageWidth, imageHeight;
    int currentColor, initialColor;
    final Runnable closeCallback;
    final Consumer<Integer> setColor;
    EditBox hexTextInput;

    boolean isMouseDown;

    protected ColorPickerScreen(Runnable closeCallback, Consumer<Integer> setColor, int inititalColor) {
        super(Component.empty());
        this.imageWidth = 116;
        this.imageHeight = 96;
        this.setColor = setColor;
        this.closeCallback = closeCallback;
        this.currentColor = inititalColor & 0x00FFFFFF;
        this.initialColor = currentColor;
        String hex = "#" + Integer.toHexString(this.currentColor);
        this.hexTextInput = new EditBox(Minecraft.getInstance().font, 7, 7, 52, 12, Component.literal(hex));
        this.hexTextInput.setMaxLength(7);
        this.hexTextInput.setValue(hex);
//        this.hexTextInput.setBordered(false);
    }

    @Override
    public void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.hexTextInput.setX(leftPos + 7);
        this.hexTextInput.setY(topPos + 7);
    }

    @Override
    public void onClose() {
    }

    /* ----------------------------------- *
     * Helpers
     * -----------------------------------*/
    public void resetColor() {
        this.currentColor = this.initialColor;
        this.setColor.accept(this.currentColor);
        this.setTextHex(this.currentColor);
    }

    public void setTextHex(int color) {
        String hex = "#" + Integer.toHexString(color);
        this.hexTextInput.setValue(hex);
    }

    public void onTextInputChanged() {
        if (!hexTextInput.getValue().startsWith("#")) {
            hexTextInput.setValue("#" + hexTextInput.getValue());
        }
        String hexValue = hexTextInput.getValue().substring(1);
        boolean isValidHex = hexValue.matches("^[0-9a-fA-F]+$");
        if (isValidHex) {
            this.hexTextInput.setTextColor(-1);
            this.currentColor = hexValue.isEmpty() ? 0 : Integer.parseInt(hexValue, 16);
            this.setColor.accept(this.currentColor);
        } else {
            this.hexTextInput.setTextColor(ChatFormatting.RED.getColor());
        }
    }

    /* ----------------------------------- *
     * Rendering
     * -----------------------------------*/
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.hexTextInput.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500);
        drawPreviewBox(guiGraphics);
        guiGraphics.pose().popPose();
    }

    private void drawPreviewBox(GuiGraphics guiGraphics) {
        int width = 20;
        int height = 10;
        int x = leftPos + 68;
        int y = topPos + 8;
        Color color = new Color(this.currentColor);
        guiGraphics.setColor(color.red() / 255f, color.green() / 255f, color.blue() / 255f, 1f);
        guiGraphics.blitSprite(IronsSpellbooks.id("color_picker/white"), x, y, width, height);
        guiGraphics.setColor(1f, 1f, 1f, 1f);
    }

    /* ----------------------------------- *
     * UI Interaction
     * -----------------------------------*/
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hexTextInput.mouseClicked(mouseX, mouseY, button)) {
            hexTextInput.setFocused(true);
            return true;
        }
        hexTextInput.setFocused(false);
        if (mouseX > leftPos + 98 && mouseY > topPos + 8 && mouseX < leftPos + 98 + 10 && mouseY < topPos + 8 + 10) {
            resetColor();
            return true;
        }
        if (mouseX < leftPos || mouseY < topPos || mouseX > leftPos + imageWidth || mouseY > topPos + imageHeight) {
            this.closeCallback.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hexTextInput.keyPressed(keyCode, scanCode, modifiers)) {
            onTextInputChanged();
            return true;
        } else if (this.hexTextInput.isFocused() && this.hexTextInput.isVisible() && keyCode != 256) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (hexTextInput.isFocused() && hexTextInput.charTyped(codePoint, modifiers)) {
            onTextInputChanged();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
