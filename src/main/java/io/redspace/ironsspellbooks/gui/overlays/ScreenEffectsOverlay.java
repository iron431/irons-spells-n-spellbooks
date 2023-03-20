package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class ScreenEffectsOverlay extends GuiComponent {
    public final static ResourceLocation MAGIC_AURA_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/enchanted_ward_vignette.png");
    public final static ResourceLocation HEARSTOP_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/heartstop.png");
    static final int IMAGE_WIDTH = 256;
    static final int IMAGE_HEIGHT = 256;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        //screenWidth = gui.getMinecraft().getWindow().getScreenWidth();
        //screenHeight = gui.getMinecraft().getWindow().getScreenHeight();
        Player player = Minecraft.getInstance().player;
        if (player.hasEffect(MobEffectRegistry.HEARTSTOP.get())) {

            // setupRenderer(1, 0, 0, .25f, HEARSTOP_TEXTURE);
            //gui.blit(poseStack, 0, 0, 0, 0, screenWidth, screenHeight);
            renderOverlay(HEARSTOP_TEXTURE, .5f, 1, 1, 0.5f, screenWidth, screenHeight);

        }

        if (player.hasEffect(MobEffectRegistry.ENCHANTED_WARD.get())) {
            //setupRenderer(0, .25f, 1, 1, MAGIC_AURA_TEXTURE);
//            gui.blit(poseStack, 0, 0, 0, 0, screenWidth, screenHeight);
            renderOverlay(MAGIC_AURA_TEXTURE, 1, .5f, 0, 1, screenWidth, screenHeight);
        }

//        gui.blit(poseStack, barX, barY, 0, IMAGE_HEIGHT * 3, (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2), IMAGE_HEIGHT);
//
//        int textX, textY;
//        var textColor = ChatFormatting.WHITE;
//        var font = gui.getFont();
//
//
//
//        textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
//        textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;
//
//        gui.getFont().draw(poseStack, castTimeString, textX, textY, textColor.getColor());
    }

    private static void setupRenderer(float r, float g, float b, float a, ResourceLocation texture) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, b, g, a);
        RenderSystem.setShaderTexture(0, texture);
    }

    private static void renderOverlay(ResourceLocation texture, float r, float g, float b, float a, int screenWidth, int screenHeight) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(r, g, b, a);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, (double) screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double) screenWidth, (double) screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double) screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }
}
