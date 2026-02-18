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
    private static final int PICKER_Y = 24;
    private static final int PICKER_WIDTH = 100;
    private static final int PICKER_HEIGHT = 64;

    private static final int VALUE_X = 114;
    private static final int VALUE_Y = 24;
    private static final int VALUE_WIDTH = 8;
    private static final int VALUE_HEIGHT = 64;

    float x, y, v;

    int leftPos, topPos;
    public int imageWidth, imageHeight;
    int currentColor, initialColor;
    final Runnable closeCallback;
    final Consumer<Integer> setColor;
    EditBox hexTextInput;

    boolean isMouseSelectingColor, isMouseSelectingValue;

    protected ColorPickerScreen(Runnable closeCallback, Consumer<Integer> setColor, int inititalColor) {
        super(Component.empty());
        this.imageWidth = 130;
        this.imageHeight = 96;
        this.setColor = setColor;
        this.closeCallback = closeCallback;
        this.currentColor = inititalColor & 0x00FFFFFF;
        this.initialColor = currentColor;
        this.setPickerFromColor(this.initialColor);
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

    public void setPos(int x, int y) {
        this.leftPos = x;
        this.topPos = y;
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
        setColor(initialColor);
        setTextHex(initialColor);
        setPickerFromColor(initialColor);
    }

    public void setColor(int color) {
        this.currentColor = color;
        this.setColor.accept(color);
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
            setColor(hexValue.isEmpty() ? 0 : Integer.parseInt(hexValue, 16));
            this.setPickerFromColor(this.currentColor);
        } else {
            this.hexTextInput.setTextColor(ChatFormatting.RED.getColor());
        }
    }

    private void updateValueFromMouse(double mouseY) {
        this.v = (int) Math.clamp(mouseY - topPos - VALUE_Y, 0, VALUE_HEIGHT);
        setColor(colorFromPicker(x, y, v));
        setTextHex(this.currentColor);
    }

    private void updateColorFromMouse(double mouseX, double mouseY) {
        this.x = (int) Math.clamp(mouseX - leftPos - PICKER_X, 0, PICKER_WIDTH - 1);
        this.y = (int) Math.clamp(mouseY - topPos - PICKER_Y, 0, PICKER_HEIGHT - 1);
        setColor(colorFromPicker(x, y, v));
        setTextHex(this.currentColor);
    }

    public int colorFromPicker(float x, float y, float v) {
        float hue = x / PICKER_WIDTH;
        float saturation = 1f - y / PICKER_HEIGHT;
        float brightness = 1f - v / VALUE_HEIGHT;

        return java.awt.Color.getHSBColor(hue, saturation, brightness).getRGB() & 0x00FFFFFF;
    }

    public void setPickerFromColor(
            int color) {

        Color colorUnpacked = new Color(color);
        float[] hsb = java.awt.Color.RGBtoHSB(
                colorUnpacked.red(),
                colorUnpacked.green(),
                colorUnpacked.blue(),
                null);

        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];

        this.x = hue * PICKER_WIDTH;
        this.y = (1f - saturation) * PICKER_HEIGHT;
        this.v = (1f - brightness) * VALUE_HEIGHT;
    }

    /* ----------------------------------- *
     * Rendering
     * -----------------------------------*/
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.hexTextInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blitSprite(IronsSpellbooks.id("color_picker/color_picker_target"), (int) (leftPos + PICKER_X + x) - 5, (int) (topPos + PICKER_Y + y) - 5, 10, 10);
        guiGraphics.blitSprite(IronsSpellbooks.id("color_picker/value_picker"), (int) (leftPos + VALUE_X - 2), (int) (topPos - 4 + VALUE_Y + v), 8, 8);

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
        int x = leftPos + 88;
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
        isMouseSelectingColor = false;
        isMouseSelectingValue = false;
        if (hexTextInput.mouseClicked(mouseX, mouseY, button)) {
            hexTextInput.setFocused(true);
            return true;
        }
        hexTextInput.setFocused(false);
        if (mouseX > leftPos + 64 && mouseY > topPos + 8 && mouseX < leftPos + 64 + 10 && mouseY < topPos + 8 + 10) {
            resetColor();
            return true;
        }
        if (mouseX >= leftPos + PICKER_X && mouseX <= leftPos + PICKER_X + PICKER_WIDTH && mouseY >= topPos + PICKER_Y && mouseY <= topPos + PICKER_Y + PICKER_HEIGHT) {
            this.isMouseSelectingColor = true;
            updateColorFromMouse(mouseX, mouseY);
            return true;
        }
        if (mouseX >= leftPos + VALUE_X && mouseX <= leftPos + VALUE_X + VALUE_WIDTH && mouseY >= topPos + VALUE_Y && mouseY <= topPos + VALUE_Y + VALUE_HEIGHT) {
            this.isMouseSelectingValue = true;
            updateValueFromMouse(mouseY);
            return true;
        }
//        if (mouseX < leftPos || mouseY < topPos || mouseX > leftPos + imageWidth || mouseY > topPos + imageHeight) {
//            this.closeCallback.run();
//            return true;
//        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMouseSelectingColor) {
            updateColorFromMouse(mouseX, mouseY);
            return true;
        } else if (isMouseSelectingValue) {
            updateValueFromMouse(mouseY);
            return true;
        }
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
