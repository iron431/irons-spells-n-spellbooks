package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class PocketDimensionEffects extends DimensionSpecialEffects {
    public PocketDimensionEffects() {
        super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    public static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    public static final ResourceLocation CLOUDS_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Tesselator tesselator = Tesselator.getInstance();
        float skyDistance = 32;
        renderBox(poseStack, tesselator, skyDistance, 0, 16, GameRenderer::getPositionTexColorShader, END_SKY_LOCATION);
        float f = ticks + partialTick;
        float scale = .75f;
        int layers = 5;
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            int j = layers - i - 1;
            float speed = 0.03f + i * 0.01f;
            float x = (i * 68731 + f * speed) % 360;
            float y = (i * 74869 + f * speed) % 360;
            float z = (i * 98744 + f * speed) % 360;
            poseStack.mulPose(Axis.XP.rotationDegrees(x));
            poseStack.mulPose(Axis.YP.rotationDegrees(y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(z));
            RenderSystem.setShaderColor(1f, 1f, 1f, Mth.lerp(j / (float) layers, 0.25f, .8f));
            renderBox(poseStack, tesselator, skyDistance * scale, 0, scale, GameRenderer::getPositionTexColorShader, CLOUDS_LOCATION);
            poseStack.popPose();
            scale *= .9f;
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        return true;
    }

    private static void renderBox(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture) {
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.setShaderTexture(0, texture);
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            if (i == 1) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            if (i == 2) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }

            if (i == 3) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            }

            if (i == 4) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            if (i == 5) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }

            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, -skyDistance).setUv(uvMin, uvMin).setColor(-14145496);
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, skyDistance).setUv(uvMin, uvMax).setColor(-14145496);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, skyDistance).setUv(uvMax, uvMax).setColor(-14145496);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, -skyDistance).setUv(uvMax, uvMin).setColor(-14145496);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }
    }
}
