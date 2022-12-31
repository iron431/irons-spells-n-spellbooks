package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CastTimeDisplay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    static final int IMAGE_WIDTH = 54;
    static final int COMPLETION_BAR_WIDTH = 44;
    static final int IMAGE_HEIGHT = 21;
    static int screenHeight;
    static int screenWidth;

    @SubscribeEvent
    public static void onPostRender(RenderGameOverlayEvent.Text e) {

        if (!ClientMagicData.isCasting || ClientMagicData.castDurationRemaining <= 0)
            return;

        float castCompletionPercent = ClientMagicData.getCastCompletionPercent();

        Gui GUI = Minecraft.getInstance().gui;
        PoseStack stack = e.getMatrixStack();
        screenWidth = e.getWindow().getGuiScaledWidth();
        screenHeight = e.getWindow().getGuiScaledHeight();

        int barX, barY;
        barX = screenWidth / 2 - IMAGE_WIDTH / 2;
        barY = screenHeight / 2 + screenHeight / 8;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        GUI.blit(stack, barX, barY, 0, IMAGE_HEIGHT * 2, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        GUI.blit(stack, barX, barY, 0, IMAGE_HEIGHT * 3, (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2), IMAGE_HEIGHT);

        int textX, textY;
        var textColor = ChatFormatting.WHITE;
        var font = GUI.getFont();
        String castTimeString = Utils.TimeFromTicks((1 - castCompletionPercent) * ClientMagicData.castDuration, 1);
        textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
        textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;

        //GUI.getFont().drawShadow(stack, castTimeString, textX, textY, textColor.getColor());
        GUI.getFont().draw(stack, castTimeString, textX, textY, textColor.getColor());
    }
}
