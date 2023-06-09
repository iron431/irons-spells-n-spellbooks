package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.blood.RayOfSiphoningSpell;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellRenderingHelper {
    private static ResourceLocation SOLID = IronsSpellbooks.id("textures/entity/ray/solid.png");
    private static ResourceLocation BEACON = IronsSpellbooks.id("textures/entity/ray/beacon_beam.png");
    private static ResourceLocation STRAIGHT_GLOW = IronsSpellbooks.id("textures/entity/ray/ribbon_glow.png");
    private static ResourceLocation TWISTING_GLOW = IronsSpellbooks.id("textures/entity/ray/twisting_glow.png");

    public static void renderSpellHelper(SyncedSpellData spellData, LivingEntity castingMob, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        switch (spellData.getCastingSpellType()) {
            case RAY_OF_SIPHONING_SPELL -> renderRayOfSiphoning(castingMob, poseStack, bufferSource, partialTicks);
        }
    }

    public static void renderRayOfSiphoning(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {

        poseStack.pushPose();
        poseStack.translate(0, entity.getEyeHeight() * .8f, 0);
        if (entity instanceof AbstractSpellCastingMob mob/* && mob.getTarget() != null*/) {
            //Vec3 dir = mob.getEyePosition().subtract(mob.getTarget().position().add(0, mob.getTarget().getEyeHeight() * .7f, 0));
            Vec3 dir = entity.getLookAngle().normalize();
            var pitch = Math.asin(dir.y);
            var yaw = Math.atan2(dir.x, dir.z);

            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) -pitch * Mth.RAD_TO_DEG));

        } else {
            float f = Mth.rotLerp(entity.yRotO, entity.getYRot(), partialTicks);
            float f1 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-f));
            poseStack.mulPose(Axis.XP.rotationDegrees(f1));
        }


        var pose = poseStack.last();
        Vec3 start = Vec3.ZERO;//caster.getEyePosition(partialTicks);
        Vec3 end;
        //TODO: too expensive?
        Vec3 impact = Utils.raycastForEntity(entity.level(), entity, RayOfSiphoningSpell.getRange(0), true).getLocation();
        float distance = (float) entity.getEyePosition().distanceTo(impact);
        float radius = .12f;
        int r = (int) (255 * .7f);
        int g = (int) (255 * 0f);
        int b = (int) (255 * 0f);
        int a = (int) (255 * 1f);

        float deltaTicks = entity.tickCount + partialTicks;
        float deltaUV = -deltaTicks % 10;
        float max = Mth.frac(deltaUV * 0.2F - (float) Mth.floor(deltaUV * 0.1F));
        float min = -1.0F + max;
        for (int j = 1; j <= distance; j++) {
            Vec3 wiggle = new Vec3(
                    Mth.sin(deltaTicks * .8f) * .02f,
                    Mth.sin(deltaTicks * .8f + 100) * .02f,
                    Mth.cos(deltaTicks * .8f) * .02f
            );
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

    private static void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a, float uvMin, float uvMax) {
        //Bottom
        drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Top
        drawQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Left
        drawQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a, uvMin, uvMax);
        //Right
        drawQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a, uvMin, uvMax);
    }

    private static void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a, float uvMin, float uvMax) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float halfWidth = width * .5f;
        float halfHeight = height * .5f;

        consumer.vertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, uvMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, uvMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, uvMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, uvMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();

    }
}