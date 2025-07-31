package io.redspace.ironsspellbooks.gui.overlays;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CastBarOverlay implements IGuiOverlay {
    public static CastBarOverlay instance = new CastBarOverlay();

    public final static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/icons.png");
    static final int IMAGE_WIDTH = 54;
    static final int COMPLETION_BAR_WIDTH = 44;
    static final int IMAGE_HEIGHT = 21;

    public void render(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight) {
        if (!ClientMagicData.isCasting() || ClientMagicData.isCasting() && ClientMagicData.getCastType() == CastType.INSTANT)
            return;

        float castCompletionPercent = ClientMagicData.getCastCompletionPercent();
        String castTimeString = Utils.timeFromTicks((1 - castCompletionPercent) * ClientMagicData.getCastDuration(), 1);
        if (ClientMagicData.getCastType() == CastType.CONTINUOUS) {
            castCompletionPercent = 1 - castCompletionPercent;
        }

        int barX, barY;
        barX = screenWidth / 2 - IMAGE_WIDTH / 2;
        barY = screenHeight / 2 + screenHeight / 8;

        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        //RenderSystem.setShaderTexture(0, TEXTURE);

        guiHelper.blit(TEXTURE, barX, barY, 0, IMAGE_HEIGHT * 2, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        guiHelper.blit(TEXTURE, barX, barY, 0, IMAGE_HEIGHT * 3, (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2), IMAGE_HEIGHT);

        int textX, textY;
        var font = gui.getFont();

        textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
        textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;

        guiHelper.drawString(font, castTimeString, textX, textY, 0xFFFFFF);
    }
}
