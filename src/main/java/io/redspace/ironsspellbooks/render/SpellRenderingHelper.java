package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SpellRenderingHelper {
    public static final ResourceLocation SOLID = IronsSpellbooks.id("textures/entity/ray/solid.png");
    public static final ResourceLocation BEACON = IronsSpellbooks.id("textures/entity/ray/beacon_beam.png");
    public static final ResourceLocation STRAIGHT_GLOW = IronsSpellbooks.id("textures/entity/ray/ribbon_glow.png");
    public static final ResourceLocation TWISTING_GLOW = IronsSpellbooks.id("textures/entity/ray/twisting_glow.png");
    private static final ResourceLocation ELECTROCUTE_SOLID = IronsSpellbooks.id("textures/entity/electric_beams/solid.png");

    public static void renderRayOfSiphoning(Level level, PoseStack poseStack, Vec3 offset, Vec3 rayLine, MultiBufferSource bufferSource, float partialTicks) {
        poseStack.pushPose();
        var pose = poseStack.last();
        Vec3 end;
        float distance = (float) rayLine.length();
        float radius = .12f;
        int r = (int) (255 * .7f);
        int g = (int) (255 * 0f);
        int b = (int) (255 * 0f);
        int a = (int) (255 * 1f);

        float deltaTicks = Minecraft.getInstance().player.tickCount + partialTicks;
        float deltaUV = -deltaTicks % 10;
        float max = Mth.frac(deltaUV * 0.2F - (float) Mth.floor(deltaUV * 0.1F));
        float min = -1.0F + max;

        Vec3 start = Vec3.ZERO;
        float segmentLength = 0.5f;
        float scaleExtension = distance / ((int) (distance / segmentLength) * segmentLength);
        poseStack.scale(1, 1, scaleExtension);
        for (float j = 1; j <= distance; j += segmentLength) {
            Vec3 wiggle = new Vec3(
                    Mth.sin((j * 0.5f + deltaTicks) * .8f) * .02f,
                    Mth.sin((j * 0.5f + deltaTicks) * .8f + 100) * .02f,
                    Mth.cos((j * 0.5f + deltaTicks) * .8f) * .02f
            );
            end = new Vec3(0, 0, Math.min(j, distance)).add(wiggle);
            VertexConsumer inner = bufferSource.getBuffer(RenderType.entityTranslucent(BEACON, true));
            drawHull(start, end, radius, radius, pose, inner, r, g, b, a, min, max);
            start = end;
        }
        start = Vec3.ZERO;
        for (float j = 1; j <= distance; j += segmentLength) {
            Vec3 wiggle = new Vec3(
                    Mth.sin((j * 1f + deltaTicks) * .8f) * .02f,
                    Mth.sin((j * 1f + deltaTicks) * .8f + 100) * .02f,
                    Mth.cos((j * 1f + deltaTicks) * .8f) * .02f
            );
            end = new Vec3(0, 0, Math.min(j, distance)).add(wiggle);
            VertexConsumer outer = bufferSource.getBuffer(RenderType.entityTranslucent(TWISTING_GLOW));
            drawQuad(start, end, radius * 4f, 0, pose, outer, r, g, b, a, min, max);
            drawQuad(start, end, 0, radius * 4f, pose, outer, r, g, b, a, min, max);
            start = end;
        }
        poseStack.popPose();
    }

    public static void renderElectrocute(Level level, PoseStack poseStack, float rangeMultiplier, MultiBufferSource bufferSource, int seed, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, -0.125, 0.25);
        poseStack.scale(1,1,rangeMultiplier);
        var pose = poseStack.last();
        List<Vec3> segments = generateElectrocuteBeams(RandomSource.create(level.getGameTime() + seed));
        float width = .3f;
        float height = width;
        Vec3 start = Vec3.ZERO;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(ELECTROCUTE_SOLID));
        for (int i = 0; i < segments.size() - 1; i += 2) {
            var from = segments.get(i).add(start);
            var to = segments.get(i + 1).add(start);
            drawElectrocuteHull(from, to, width, height, pose, consumer, 0, 156, 255, 30);
            drawElectrocuteHull(from, to, width * .55f, height * .55f, pose, consumer, 63, 178, 255, 30);
        }

        consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magicNoCull(ELECTROCUTE_SOLID));
        for (int i = 0; i < segments.size() - 1; i += 2) {
            var from = segments.get(i).add(start);
            var to = segments.get(i + 1).add(start);
            drawElectrocuteHull(from, to, width * .2f, height * .2f, pose, consumer, 255, 255, 255, 255);
        }

        poseStack.popPose();
    }

    private static List<Vec3> generateElectrocuteBeams(RandomSource random) {
        List<Vec3> beamVectors = new ArrayList<>();
        Vec3 coreStart = Vec3.ZERO;
        int coreLength = random.nextInt(3) + 7;
        for (int core = 0; core < coreLength; core++) {
            float beamWidth = Mth.lerp(core / (float) coreLength, 2, 4f);
            Vec3 coreEnd = coreStart.add(0, 0, 1).add(electrocuteRandomVector(random, .3f).multiply(beamWidth, 1, beamWidth));
            beamVectors.add(coreStart);
            beamVectors.add(coreEnd);
            coreStart = coreEnd;
            beamVectors.addAll(generateElectrocuteBranch(random, coreEnd, random.nextInt(3) + 1, 0.5f, 1));
        }
        return beamVectors;
    }

    private static List<Vec3> generateElectrocuteBranch(RandomSource random, Vec3 origin, int maxLength, float splitChance, int recursionCount) {
        List<Vec3> branchSegments = new ArrayList<>();
        int branches = random.nextInt(maxLength + 1);
        Vec3 branchStart = origin;
        int dir = random.nextBoolean() ? 1 : -1;
        float branchLength = 1.75f / (recursionCount + 1);
        for (int i = 0; i < branches; i++) {
            Vec3 branchEnd = branchStart.add(dir * branchLength, 0, branchLength).add(electrocuteRandomVector(random, .4f));
            branchSegments.add(branchStart);
            branchSegments.add(branchEnd);
            if (random.nextFloat() <= splitChance) {
                branchSegments.addAll(generateElectrocuteBranch(random, branchEnd, maxLength - 1, splitChance * 1.2f, recursionCount + 1));
            }
            branchStart = branchEnd;
        }
        return branchSegments;
    }

    private static Vec3 electrocuteRandomVector(RandomSource random, float radius) {
        double x = random.nextDouble() * 2 * radius - radius;
        double y = random.nextDouble() * 2 * radius - radius;
        double z = random.nextDouble() * 2 * radius - radius;
        return new Vec3(x, y, z);
    }

    private static void drawElectrocuteHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        drawElectrocuteQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        drawElectrocuteQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        drawElectrocuteQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
        drawElectrocuteQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
    }

    private static void drawElectrocuteQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        Matrix4f poseMatrix = pose.pose();
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(r, g, b, a).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(r, g, b, a).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
    }

    public static void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a, float uvMin, float uvMax) {
        //Bottom
        drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Top
        drawQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Left
        drawQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Right
        drawQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a, uvMin, uvMax);
    }

    public static void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a, float uvMin, float uvMax) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float halfWidth = width * .5f;
        float halfHeight = height * .5f;

        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(r, g, b, a).setUv(0f, uvMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(r, g, b, a).setUv(1f, uvMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(r, g, b, a).setUv(1f, uvMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(r, g, b, a).setUv(0f, uvMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0f, 1f, 0f);

    }
}