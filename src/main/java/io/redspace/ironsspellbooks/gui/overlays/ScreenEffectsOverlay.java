package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;


public class ScreenEffectsOverlay implements LayeredDraw.Layer {
    public static final ScreenEffectsOverlay instance = new ScreenEffectsOverlay();

    public final static ResourceLocation MAGIC_AURA_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/enchanted_ward_vignette.png");
    public final static ResourceLocation HEARTSTOP_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/heartstop.png");

    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (player.hasEffect(MobEffectRegistry.HEARTSTOP)) {
            setupRenderer(1, 0, 0, .25f, HEARTSTOP_TEXTURE);
            //gui.blit(poseStack, 0, 0, 0, 0, screenWidth, screenHeight);
            renderOverlay(HEARTSTOP_TEXTURE, .5f, 1, 1, 0.5f, screenWidth, screenHeight);
        }
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
        //FIXME: 1.21: code this from scratch
//        RenderSystem.disableDepthTest();
//        RenderSystem.depthMask(false);
//        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, texture);
//        RenderSystem.setShaderColor(r, g, b, a);
//        Tesselator tesselator = Tesselator.getInstance();
//        BufferBuilder bufferbuilder = tesselator.getBuilder();
//        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        bufferbuilder.vertex(0.0D, (double) screenHeight, -90.0D).setUv(0.0F, 1.0F);
//        bufferbuilder.vertex((double) screenWidth, (double) screenHeight, -90.0D).setUv(1.0F, 1.0F);
//        bufferbuilder.vertex((double) screenWidth, 0.0D, -90.0D).setUv(1.0F, 0.0F);
//        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).setUv(0.0F, 0.0F);
//        tesselator.end();
//        RenderSystem.depthMask(true);
//        RenderSystem.enableDepthTest();
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.defaultBlendFunc();
    }
}
