package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class CastBarOverlay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");
    static final int IMAGE_WIDTH = 54;
    static final int COMPLETION_BAR_WIDTH = 44;
    static final int IMAGE_HEIGHT = 21;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
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

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        gui.blit(poseStack, barX, barY, 0, IMAGE_HEIGHT * 2, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        gui.blit(poseStack, barX, barY, 0, IMAGE_HEIGHT * 3, (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2), IMAGE_HEIGHT);

        int textX, textY;
        var textColor = ChatFormatting.WHITE;
        var font = gui.getFont();


        textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
        textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;

        gui.getFont().draw(poseStack, castTimeString, textX, textY, textColor.getColor());
    }
}
