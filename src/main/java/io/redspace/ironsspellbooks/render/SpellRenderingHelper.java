package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.spells.CastingMobAimingData;
import io.redspace.ironsspellbooks.spells.blood.RayOfSiphoningSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

    public static void renderSpellHelper(SyncedSpellData spellData, LivingEntity castingMob, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        if (SpellRegistry.RAY_OF_SIPHONING_SPELL.get().getSpellId().equals(spellData.getCastingSpellId())) {
            renderRayOfSiphoning(castingMob, poseStack, bufferSource, partialTicks);
        }
    }

    public static void renderRayOfSiphoning(Level level, PoseStack poseStack, Vec3 offset, Vec3 rayLine, MultiBufferSource bufferSource, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, -0.125, 0.5);

//        poseStack.translate(offset.x, offset.y, offset.z);
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

//        var dir = rayLine.normalize();
//        float dx = (float) dir.x;
//        float dz = (float) dir.z;
//        //angle = atan o/a
//        float yRot = (float) Mth.atan2(dz, dx) - 1.5707f; // for some reason, we are rotated 90 degrees the wrong way. subtracting 2 pi here.
//        float dxz = Mth.sqrt(dx * dx + dz * dz);
//        float dy = (float) dir.y;
//        float xRot = (float) Mth.atan2(dy, dxz);
//        poseStack.mulPose(Axis.YP.rotation(-yRot));
//        poseStack.mulPose(Axis.XP.rotation(-xRot));
        Vec3 start = Vec3.ZERO;
        float segmentLength = 0.5f;
        float scaleExtension = distance / ((int) (distance / segmentLength) * segmentLength);
        poseStack.scale(1, 1, scaleExtension);
        for (float j = 1; j <= distance; j += segmentLength) {
            Vec3 wiggle = new Vec3(
                    Mth.sin((j * 2 + deltaTicks) * .8f) * .02f,
                    Mth.sin((j * 2 + deltaTicks) * .8f + 100) * .02f,
                    Mth.cos((j * 2 + deltaTicks) * .8f) * .02f
            );
            end = new Vec3(0, 0, Math.min(j, distance)).add(wiggle);
            VertexConsumer inner = bufferSource.getBuffer(RenderType.entityTranslucent(BEACON, true));
            drawHull(start, end, radius, radius, pose, inner, r, g, b, a, min, max);
            start = end;
        }
        start = Vec3.ZERO;
        for (float j = 1; j <= distance; j += segmentLength) {
            Vec3 wiggle = new Vec3(
                    Mth.sin((j * 2 + deltaTicks) * .8f) * .02f,
                    Mth.sin((j * 2 + deltaTicks) * .8f + 100) * .02f,
                    Mth.cos((j * 2 + deltaTicks) * .8f) * .02f
            );
            end = new Vec3(0, 0, Math.min(j, distance)).add(wiggle);
            VertexConsumer outer = bufferSource.getBuffer(RenderType.entityTranslucent(TWISTING_GLOW));
            drawQuad(start, end, radius * 4f, 0, pose, outer, r, g, b, a, min, max);
            drawQuad(start, end, 0, radius * 4f, pose, outer, r, g, b, a, min, max);
            start = end;
        }
        poseStack.popPose();
    }

    public static void renderRayOfSiphoning(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {

        poseStack.pushPose();
        poseStack.translate(0, entity.getEyeHeight() * .8f, 0);

        var pose = poseStack.last();
        Vec3 end;
        Vec3 rayEndPos;
        if (entity instanceof Mob mob && MagicData.getPlayerMagicData(mob).getAdditionalCastData() instanceof CastingMobAimingData aimingData) {
            rayEndPos = RaycastBuilder.begin(entity.level, entity)
                    .start(entity.getEyePosition())
                    .end(entity.getEyePosition().add(aimingData.getAimPosition(partialTicks).subtract(entity.getEyePosition(partialTicks)).normalize().scale(RayOfSiphoningSpell.getRange(0))))
                    .checkForBlocks(true)
                    .build()
                    .getLocation();
        } else {
            rayEndPos = RaycastBuilder.begin(entity.level(), entity)
                    .range(RayOfSiphoningSpell.getRange(0))
                    .checkForBlocks(true)
                    .build()
                    .getLocation();
        }
        float distance = (float) entity.getEyePosition().distanceTo(rayEndPos);
        float radius = .12f;
        int r = (int) (255 * .7f);
        int g = (int) (255 * 0f);
        int b = (int) (255 * 0f);
        int a = (int) (255 * 1f);

        float deltaTicks = entity.tickCount + partialTicks;
        float deltaUV = -deltaTicks % 10;
        float max = Mth.frac(deltaUV * 0.2F - (float) Mth.floor(deltaUV * 0.1F));
        float min = -1.0F + max;

        var dir = rayEndPos.subtract(entity.getEyePosition(partialTicks)).normalize();

        //y rotation is a triangle of x and z axis
        float dx = (float) dir.x;
        float dz = (float) dir.z;
        //angle = atan o/a
        float yRot = (float) Mth.atan2(dz, dx) - 1.5707f; // for some reason, we are rotated 90 degrees the wrong way. subtracting 2 pi here.
        //IronsSpellbooks.LOGGER.debug("yRot: {}", yRot);
        //x rotation is a triangle of xz and y axis
        float dxz = Mth.sqrt(dx * dx + dz * dz);
        float dy = (float) dir.y;
        //angle = atan o/a
        float xRot = (float) Mth.atan2(dy, dxz);
        //IronsSpellbooks.LOGGER.debug("xRot: {}", xRot);
        poseStack.mulPose(Axis.YP.rotation(-yRot));
        poseStack.mulPose(Axis.XP.rotation(-xRot));
        Vec3 start = Vec3.ZERO;
        for (float j = 1; j <= distance; j += .5f) {
            Vec3 wiggle = new Vec3(
                    Mth.sin(deltaTicks * .8f) * .02f,
                    Mth.sin(deltaTicks * .8f + 100) * .02f,
                    Mth.cos(deltaTicks * .8f) * .02f
            );
            //end = dir.scale(Math.min(j, distance)).add(wiggle);
            end = new Vec3(0, 0, Math.min(j, distance)).add(wiggle);
            VertexConsumer inner = bufferSource.getBuffer(RenderType.entityTranslucent(BEACON, true));
            drawHull(start, end, radius, radius, pose, inner, r, g, b, a, min, max);
            //drawHull(start, end, .25f, .25f, pose, outer, r / 2, g / 2, b / 2, a / 2);
            VertexConsumer outer = bufferSource.getBuffer(RenderType.entityTranslucent(TWISTING_GLOW));
            drawQuad(start, end, radius * 4f, 0, pose, outer, r, g, b, a, min, max);
            drawQuad(start, end, 0, radius * 4f, pose, outer, r, g, b, a, min, max);
            start = end;

        }
        poseStack.popPose();
    }

    public static void renderElectrocute(Level level, PoseStack poseStack, Vec3 offset, Vec3 direction, MultiBufferSource bufferSource, int seed, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, -0.125, 0.5);

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