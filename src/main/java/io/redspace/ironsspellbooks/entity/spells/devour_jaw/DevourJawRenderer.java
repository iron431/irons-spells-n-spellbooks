package io.redspace.ironsspellbooks.entity.spells.devour_jaw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DevourJawRenderer extends EntityRenderer<DevourJaw> {

    private final DevourJawModel model;

    public DevourJawRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new DevourJawModel(pContext.bakeLayer(ModelLayers.EVOKER_FANGS));
    }

    public void render(DevourJaw entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        if (entity.tickCount < entity.waitTime)
            return;
        float f = entity.tickCount + partialTicks;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.scale(-1, -1, 1);
        poseStack.scale(1.85f, 1.85f, 1.85f);
        this.model.setupAnim(entity, f, 0.0F, 0.0F, entity.getYRot(), entity.getXRot());
        VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, multiBufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DevourJaw pEntity) {
        return IronsSpellbooks.id("textures/entity/devour_jaw.png");
    }

    static class DevourJawModel extends EvokerFangsModel<DevourJaw> {
        private final ModelPart root;
        private final ModelPart base;
        private final ModelPart upperJaw;
        private final ModelPart lowerJaw;

        public DevourJawModel(ModelPart pRoot) {
            super(pRoot);
            this.root = pRoot;
            this.base = pRoot.getChild("base");
            this.upperJaw = pRoot.getChild("upper_jaw");
            this.lowerJaw = pRoot.getChild("lower_jaw");
        }

        @Override
        public void setupAnim(DevourJaw entity, float time, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            time -= entity.waitTime;
            float interval = entity.warmupTime - entity.waitTime;

            float f = Mth.clamp(time / interval, 0, 1);
            f = 1 - f * f * f * f;
            this.upperJaw.zRot = (float) Math.PI - f * 0.35F * (float) Math.PI;
            this.lowerJaw.zRot = (float) Math.PI + f * 0.35F * (float) Math.PI;

            float f2 = (time / interval);
            f2 = .5f * Mth.cos(.5f * Mth.PI * (f2 - 1)) + .5f;
            f2 *= f2;
            this.upperJaw.y = -18F * f2 + 16f;
            this.lowerJaw.y = this.upperJaw.y;
            this.base.y = this.upperJaw.y;
        }
    }
}
