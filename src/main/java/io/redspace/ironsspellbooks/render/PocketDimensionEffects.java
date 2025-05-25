package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;
import java.util.function.Supplier;

public class PocketDimensionEffects extends DimensionSpecialEffects {
    public PocketDimensionEffects() {
        super(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    public static final ResourceLocation SKY_LOCATION = IronsSpellbooks.id("textures/environment/pocket_dimension_sky.png");
    public static final ResourceLocation CLOUDS_LOCATION = IronsSpellbooks.id("textures/environment/pocket_clouds.png");
    public static final ResourceLocation WISP_LOCATION = IronsSpellbooks.id("textures/environment/single_cloud.png");

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Tesselator tesselator = Tesselator.getInstance();
        float skyDistance = 100;
        renderBox(poseStack, tesselator, skyDistance, 0, 16, GameRenderer::getPositionTexColorShader, SKY_LOCATION);
        float f = ticks + partialTick;
        float scale = .80f; // give buffer so rotated cubes don't clip through main skybox
        int layers = 8;
        Random random = new Random(431);
        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();
            int j = layers - i - 1;
            float speed = (0.01f + i * i * 0.09f) * .025f;
            float x = (i * 68731 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            float y = (i * 74869 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            float z = (i * 98744 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            poseStack.mulPose(Axis.XP.rotationDegrees(x));
            poseStack.mulPose(Axis.YP.rotationDegrees(y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(z));
            Vector3f rgb = new Vector3f(1, 1, 1);//new Vector3f(random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f);
            RenderSystem.setShaderColor(rgb.x, rgb.y, rgb.z, Mth.lerp(j / (float) layers, 0.05f, 1f));
            renderBox(poseStack, tesselator, skyDistance * scale, 0, 1f / (scale * scale), GameRenderer::getPositionTexColorShader, CLOUDS_LOCATION);
            poseStack.popPose();
            scale -= 0.04f; // give slight separation between layers to prevent too much zfighting/artifacting
        }
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        var color = new Vector3f(.1f, .4f, .6f);
        color.mul(0.5f);
        RenderSystem.setShaderColor(color.x, color.y, color.z, 1f);
        int clouds = 20;
        float zoff = 0; // use ever-enclosing z offset to ensure new planes are always in front of old planes, preventing alpha clipping
        for (int i = 0; i < clouds; i++) {
            float clusterScale = 0.05f + i * 0.002f;
            poseStack.pushPose();
            int count = (i + 1);
            float speed = 0.005f;
            float x = (random.nextInt(360) + f * speed) % 360;
            float y = (random.nextInt(360) + f * speed) % 360;
            float z = (random.nextInt(360) + f * speed) % 360;
            poseStack.mulPose(Axis.XP.rotationDegrees(x));
            poseStack.mulPose(Axis.YP.rotationDegrees(y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(z));
            for (int j = 0; j < count; j++) {
                Vector3f offset = new Vector3f(
                        (random.nextFloat() - 0.5f),
                        0, // y is distal axis
                        (random.nextFloat() - 0.5f)
                );
                offset.mul(skyDistance * 0.25f * (1 + j * .025f));
                poseStack.pushPose();
                poseStack.translate(offset.x, zoff, offset.z);
                renderPlane(poseStack, tesselator, skyDistance * scale, 0, 1, GameRenderer::getPositionTexColorShader, WISP_LOCATION, clusterScale);
                poseStack.popPose();
//                poseStack.scale(0.75f, 0.75f, 0.75f);
                zoff += 0.05f;
            }
            poseStack.popPose();
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

    private static void renderPlane(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, float scale) {
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.setShaderTexture(0, texture);
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMin, uvMin).setColor(-14145496);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMin, uvMax).setColor(-14145496);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMax, uvMax).setColor(-14145496);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMax, uvMin).setColor(-14145496);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        poseStack.popPose();
    }
}
