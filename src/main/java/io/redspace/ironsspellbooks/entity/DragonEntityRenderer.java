package io.redspace.ironsspellbooks.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DragonEntityRenderer extends EntityRenderer<DragonEntity> {
    public DragonEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEntity pEntity) {
        return null;
    }

    @Override
    public void render(DragonEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        var vertexConsumer = buffer.getBuffer(RenderType.lines());
        AABB aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, 1.0F, 1.0F, 1.0F, 1.0F);
        if (entity.isMultipartEntity()) {
            double d0 = -Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double d1 = -Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double d2 = -Mth.lerp(partialTicks, entity.zOld, entity.getZ());

            for (PartEntity<?> enderdragonpart : entity.getParts()) {
                poseStack.pushPose();
                double d3 = d0 + Mth.lerp(partialTicks, enderdragonpart.xOld, enderdragonpart.getX());
                double d4 = d1 + Mth.lerp(partialTicks, enderdragonpart.yOld, enderdragonpart.getY());
                double d5 = d2 + Mth.lerp(partialTicks, enderdragonpart.zOld, enderdragonpart.getZ());
                poseStack.translate(d3, d4, d5);
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, enderdragonpart.getBoundingBox().move(-enderdragonpart.getX(), -enderdragonpart.getY(), -enderdragonpart.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                poseStack.popPose();
            }
        }

//        if (entity instanceof LivingEntity) {
//            float f = 0.01F;
//            LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb.minX, (double) (entity.getEyeHeight() - 0.01F), aabb.minZ, aabb.maxX, (double) (entity.getEyeHeight() + 0.01F), aabb.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
//        }

        float lineVectorLength = 1.25f * entity.getBbWidth();
        Vec3 vec3 = entity.getViewVector(partialTicks);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        vertexConsumer.vertex(matrix4f, 0.0F, entity.getEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(matrix3f, (float) vec3.x, (float) vec3.y, (float) vec3.z).endVertex();
        vertexConsumer.vertex(matrix4f, (float) (vec3.x * lineVectorLength), (float) ((double) entity.getEyeHeight() + vec3.y * lineVectorLength), (float) (vec3.z * lineVectorLength)).color(0, 0, 255, 255).normal(matrix3f, (float) vec3.x, (float) vec3.y, (float) vec3.z).endVertex();


        //Vec3 bodyVector = projectVector(Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot));
        //vertexConsumer.vertex(matrix4f, 0.0F, 1, 0.0F).color(0, 255, 0, 255).normal(matrix3f, (float) bodyVector.x, (float) bodyVector.y, (float) bodyVector.z).endVertex();
        //vertexConsumer.vertex(matrix4f, (float) (bodyVector.x * lineVectorLength), (float) ((double) 1 + bodyVector.y * lineVectorLength), (float) (bodyVector.z * lineVectorLength)).color(0, 255, 0, 255).normal(matrix3f, (float) bodyVector.x, (float) bodyVector.y, (float) bodyVector.z).endVertex();

        poseStack.pushPose();
        float hipHeight = 2f;
        float hipWidth = 1.5f;
        float bodyRot = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float bodyRad = bodyRot * Mth.DEG_TO_RAD;
        poseStack.translate(0, hipHeight, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyRot));
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, new AABB(-hipWidth, -hipWidth, -hipWidth * .5, hipWidth, hipWidth * .5, hipWidth), 0.6F, 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
        Vec3 rightHip = new Vec3(-Mth.cos(-bodyRad) * hipWidth, hipHeight, Mth.sin(-bodyRad) * hipWidth);
        Vec3 rightFootEffector = Utils.moveToRelativeGroundLevel(entity.level, entity.position().add(rightHip), 7).subtract(entity.position());
        //vertexConsumer.vertex(matrix4f, (float) rightHip.x, (float) rightHip.y, (float) rightHip.z).color(1f, 1f, 1f, 1f).normal(matrix3f, (float) bodyVector.x, (float) bodyVector.y, (float) bodyVector.z).endVertex();
        //vertexConsumer.vertex(matrix4f, (float) rightFootEffector.x, (float) rightFootEffector.y, (float) rightFootEffector.z).color(1f, 1f, 1f, 1f).normal(matrix3f, (float) bodyVector.x, (float) bodyVector.y, (float) bodyVector.z).endVertex();

        /**
         * simple ik test
         */
        var restriction = .01f; //limits max length to prevent legs from fully extending/popping
        var lengthUpperLeg = 2; //can be preset, or calculated
        var lengthLowerLeg = 2; //can be preset, or calculated
        var lengthToEffector = Mth.clamp(rightFootEffector.subtract(rightHip).length(), restriction, lengthLowerLeg + lengthUpperLeg - restriction); //clamp length to effector by the max possible length of our leg
        //use cosine rule to get desired angles for each joint
        var angleHip = (float) Math.acos(Mth.clamp(
                (lengthLowerLeg * lengthLowerLeg - lengthUpperLeg * lengthUpperLeg - lengthToEffector * lengthToEffector) / (-2 * lengthUpperLeg * lengthToEffector)
                , -1, 1));
        var angleKnee = (float) Math.acos(Mth.clamp(
                (lengthToEffector * lengthToEffector - lengthLowerLeg * lengthLowerLeg - lengthUpperLeg * lengthUpperLeg) / (-2 * lengthUpperLeg * lengthLowerLeg)
                , -1, 1));

        //Project vectors from joints based on the calculated angles
        Vec3 rightKnee = rightHip.add(projectVector(Mth.HALF_PI + angleHip, bodyRad).scale(lengthUpperLeg));
        Vec3 rightFoot = rightKnee.add(projectVector(-Mth.HALF_PI + angleHip + angleKnee, bodyRad).scale(lengthLowerLeg));

        drawLine(rightHip, rightKnee, poseStack, vertexConsumer, new Vector3f(1f, 1f, 1f));
        drawLine(rightKnee, rightFoot, poseStack, vertexConsumer, new Vector3f(1f, 1f, 1f));
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabbAround(rightHip, 0.125f), 0F, 1.0F, 0.0F, 1.0F);
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabbAround(rightKnee, 0.125f), 0F, 1.0F, 0.0F, 1.0F);
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabbAround(rightFoot, 0.25f), 0F, 1.0F, 0.0F, 1.0F);
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabbAround(rightFootEffector, 0.125f), 1F, 0F, 0.0F, 1.0F);


//        renderRotatedBox(poseStack, new Vec3(0, 2, 0), 1f, Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD, vertexConsumer);
    }

    protected final AABB aabbAround(Vec3 vec3, float radius) {
        return new AABB(vec3.x - radius, vec3.y - radius, vec3.z - radius, vec3.x + radius, vec3.y + radius, vec3.z + radius);
    }

    protected final Vec3 projectVector(float pYRot) {
        float f1 = -pYRot * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        return new Vec3(f3, 0, f2);
    }

    protected final Vec3 projectVector(float pXRot, float pYRot) {
        float f = pXRot;
        float f1 = -pYRot;
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3((double) (f3 * f4), (double) (-f5), (double) (f2 * f4));
    }

    protected final void renderRotatedBox(PoseStack poseStack, Vec3 origin, float length, float width, float height, float heading, VertexConsumer vertexConsumer, Vector3f color) {
        poseStack.pushPose();
        poseStack.translate(origin.x, origin.y, origin.z);
        length *= .5f;
        width *= .5f;
        height *= .5f;
        float sin = Mth.sin(heading);
        float cos = Mth.cos(heading);
        //xy plane, topview context
        Vec2 topRight = new Vec2(cos * length, cos * width);
        Vec2 topLeft = new Vec2(-topRight.x, topRight.y);
        Vec2 bottomLeft = new Vec2(-topRight.x, -topRight.y);
        Vec2 bottomRight = new Vec2(topRight.x, -topRight.y);
        //bottom Face
        drawLine(topLeft, -height, topRight, -height, poseStack, vertexConsumer, color);
        drawLine(topRight, -height, bottomRight, -height, poseStack, vertexConsumer, color);
        drawLine(bottomRight, -height, bottomLeft, -height, poseStack, vertexConsumer, color);
        drawLine(bottomLeft, -height, topLeft, -height, poseStack, vertexConsumer, color);

        //top Face
        drawLine(topLeft, height, topRight, height, poseStack, vertexConsumer, color);
        drawLine(topRight, height, bottomRight, height, poseStack, vertexConsumer, color);
        drawLine(bottomRight, height, bottomLeft, height, poseStack, vertexConsumer, color);
        drawLine(bottomLeft, height, topLeft, height, poseStack, vertexConsumer, color);

        //corners
        drawLine(topLeft, -height, topLeft, height, poseStack, vertexConsumer, color);
        drawLine(topRight, -height, topRight, height, poseStack, vertexConsumer, color);
        drawLine(bottomRight, -height, bottomRight, height, poseStack, vertexConsumer, color);
        drawLine(bottomLeft, -height, bottomLeft, height, poseStack, vertexConsumer, color);
        poseStack.popPose();

    }

    protected final void drawLine(Vec3 a, Vec3 b, PoseStack poseStack, VertexConsumer vertexConsumer, Vector3f color) {
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        Vec3 d = b.subtract(a);
        vertexConsumer.vertex(matrix4f,
                (float) a.x,
                (float) a.y,
                (float) a.z
        ).color(color.x, color.y, color.z, 1f).normal(matrix3f, (float) d.x, (float) d.y, (float) d.z).endVertex();
        vertexConsumer.vertex(matrix4f,
                (float) b.x,
                (float) b.y,
                (float) b.z
        ).color(color.x, color.y, color.z, 1f).normal(matrix3f, (float) -d.x, (float) -d.y, (float) -d.z).endVertex();
    }

    protected final void drawLine(Vec2 a, float heightA, Vec2 b, float heightB, PoseStack poseStack, VertexConsumer vertexConsumer, Vector3f color) {
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        Vec3 a3 = new Vec3(a.x, heightA, a.y);
        Vec3 b3 = new Vec3(b.x, heightB, b.y);
        Vec3 d = b3.subtract(a3);
        vertexConsumer.vertex(matrix4f,
                (float) a3.x,
                (float) a3.y,
                (float) a3.z
        ).color(color.x, color.y, color.z, 1f).normal(matrix3f, (float) d.x, (float) d.y, (float) d.z).endVertex();
        vertexConsumer.vertex(matrix4f,
                (float) b3.x,
                (float) b3.y,
                (float) b3.z
        ).color(color.x, color.y, color.z, 1f).normal(matrix3f, (float) -d.x, (float) -d.y, (float) -d.z).endVertex();
    }
}
