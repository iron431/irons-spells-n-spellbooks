package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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
    public static final ResourceLocation NOISE = IronsSpellbooks.id("textures/environment/noise_tile.png");

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Tesselator tesselator = Tesselator.getInstance();
        /*
         * Skybox
         */
        float skyDistance = 100;
        renderBox(poseStack, tesselator, skyDistance, 0, 1, GameRenderer::getPositionTexColorShader, SKY_LOCATION, 0xFF454545);
        /*
         * Stars
         */
        float f = ticks + partialTick;
        float scale = .80f; // give buffer so rotated cubes don't clip through main skybox
        int layers = 6;
        Random random = new Random(431);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();
            int j = layers - i - 1;
            float speed = (0.01f + i * i * 0.09f) * .015f;
            float x = (i * 68731 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            float y = (i * 74869 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            float z = (i * 98744 + f * speed * (random.nextFloat() - 0.5f)) % 360;
            poseStack.mulPose(Axis.XP.rotationDegrees(x));
            poseStack.mulPose(Axis.YP.rotationDegrees(y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(z));
            Vector3f rgb = new Vector3f(random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f, random.nextFloat() * 0.5f + 0.5f);
            float intensity = Mth.lerp(j / (float) layers, 0.25f, 0.8f);
            rgb.mul(intensity);
            rgb = new Vector3f(Math.min(rgb.x, 1), Math.min(rgb.y, 1), Math.min(rgb.z, 1));
            RenderSystem.setShaderColor(rgb.x, rgb.y, rgb.z, 1f);
            renderBox(poseStack, tesselator, skyDistance * scale, 0, 4f + 2f * scale, GameRenderer::getPositionTexColorShader, CLOUDS_LOCATION, 0xFF808080);
            poseStack.popPose();
            scale -= 0.04f; // give slight separation between layers to prevent too much zfighting/artifacting
        }
        /*
         * Nebula
         */
        var color = new Vector3f(.1f, .4f, .6f);
        color.mul(0.075f);
        // use ever-enclosing z offset to ensure new planes are always in front of old planes, preventing alpha clipping
        float zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, 0f);
        color = new Vector3f(.6f, .1f, .5f);
        color.mul(0.125f);
        zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, zoff);
        color = new Vector3f(.3f, .3f, .3f);
        color.mul(0.125f);
        zoff = renderNebula(poseStack, color, random, f, skyDistance, tesselator, scale, zoff);

        renderBorderAura(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        return true;
    }

    public void renderBorderAura(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix) {
        PoseStack poseStack = new PoseStack();
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Vec3 cameraPos = camera.getPosition();
        Matrix4f matrix4f1 = new Matrix4f().rotation(quaternionf).translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
        poseStack.mulPose(matrix4f1);
        int traversal = (int) (cameraPos.z / PocketDimensionManager.POCKET_SPACING) * PocketDimensionManager.POCKET_SPACING;
        float HARDCODE_WIDTH = 7.0f;
        float halfWidth = HARDCODE_WIDTH / 2.0f;
        float HARDCODE_X = 4 + halfWidth;
        float HARDCODE_Y = 1;
        float HARDCODE_Z = 4 + halfWidth + traversal;


        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE); //additive
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
//        renderBox(poseStack, Tesselator.getInstance(), 1, 0, 1, GameRenderer::getPositionTexColorShader, SKY_LOCATION, 0xFF454545);
        poseStack.translate(HARDCODE_X, HARDCODE_Y, HARDCODE_Z);
//        renderBox(poseStack, Tesselator.getInstance(), 1, 0, 1, GameRenderer::getPositionTexColorShader, SKY_LOCATION, 0xFF454545);


        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, NOISE);
        float uvScrollMin = ((ticks + partialTick) / 20 / 12) % 1;
        float uvScrollMax = uvScrollMin + 5f / 20 / 12;
        float uvTile = Mth.floor(HARDCODE_WIDTH / 3f); // times for x axis to tile
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 90));

            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int baseColor = 0xFF9911AA;
            bufferbuilder.addVertex(matrix4f, -halfWidth, HARDCODE_Y - 1, halfWidth).setUv(0, uvScrollMax).setColor(baseColor);
            bufferbuilder.addVertex(matrix4f, -halfWidth, HARDCODE_Y + 2, halfWidth).setUv(0, uvScrollMin).setColor(0xFF000000);
            bufferbuilder.addVertex(matrix4f, halfWidth, HARDCODE_Y + 2, halfWidth).setUv(uvTile, uvScrollMin).setColor(0xFF000000);
            bufferbuilder.addVertex(matrix4f, halfWidth, HARDCODE_Y - 1, halfWidth).setUv(uvTile, uvScrollMax).setColor(baseColor);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }
    }

    private static float renderNebula(PoseStack poseStack, Vector3f color, Random random, float f, float skyDistance, Tesselator tesselator, float scale, float zoff) {
        RenderSystem.setShaderColor(color.x, color.y, color.z, 1f);
        int clouds = 15;
        for (int i = 0; i < clouds; i++) {
            float clusterScale = 0.15f + i * 0.003f;
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
                renderPlane(poseStack, tesselator, skyDistance * scale, 0, 1, GameRenderer::getPositionTexColorShader, WISP_LOCATION, clusterScale, 0xFF606060);
                poseStack.popPose();
//                poseStack.scale(0.75f, 0.75f, 0.75f);
                zoff += 0.03f;
            }
            poseStack.popPose();
        }
        return zoff;
    }

    private static void renderBox(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, int color) {
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
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, -skyDistance).setUv(uvMin, uvMin).setColor(color);
            bufferbuilder.addVertex(matrix4f, -skyDistance, -skyDistance, skyDistance).setUv(uvMin, uvMax).setColor(color);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, skyDistance).setUv(uvMax, uvMax).setColor(color);
            bufferbuilder.addVertex(matrix4f, skyDistance, -skyDistance, -skyDistance).setUv(uvMax, uvMin).setColor(color);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }
    }

    private static void renderPlane(PoseStack poseStack, Tesselator tesselator, float skyDistance, float uvMin, float uvMax, Supplier<ShaderInstance> shaderSupplier, ResourceLocation texture, float scale, int color) {
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.setShaderTexture(0, texture);
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMin, uvMin).setColor(color);
        bufferbuilder.addVertex(matrix4f, -skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMin, uvMax).setColor(color);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, skyDistance * scale).setUv(uvMax, uvMax).setColor(color);
        bufferbuilder.addVertex(matrix4f, skyDistance * scale, -skyDistance, -skyDistance * scale).setUv(uvMax, uvMin).setColor(color);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        poseStack.popPose();
    }
}
