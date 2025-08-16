package io.redspace.ironsspellbooks.entity.spells.ice_spike;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class IceSpikeRenderer extends EntityRenderer<IceSpikeEntity> {

    private final IceSpikeModel model;

    public IceSpikeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new IceSpikeModel(pContext.bakeLayer(IceSpikeModel.LAYER_LOCATION));
    }

    public void render(IceSpikeEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        if (entity.tickCount < entity.getWaitTime())
            return;
        float f = entity.tickCount + partialTicks;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        float anim = entity.getPositionOffset(partialTicks);
        poseStack.scale(1, -1, 1);
        float scale = entity.getSpikeSize();
//        if (scale < 3 && scale > 2) {
//            scale -= 1;
//        } else if (scale >= 3) {
//            scale -= 2;
//        }
        scale = (scale - 1) * .25f + 1;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -anim * (22 + 22 + 24) / 16f, 0);

        this.model.setupAnim(entity, partialTicks, 0.0F, 0.0F, entity.getYRot(), entity.getXRot());
        VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpikeEntity pEntity) {
        return IronsSpellbooks.id("textures/entity/ice_spike.png");
    }

    public static class IceSpikeModel extends EntityModel<IceSpikeEntity> {
        public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "ice_spike"), "main");
        private final ModelPart bottom;
        private final ModelPart middle;
        private final ModelPart top;

        public IceSpikeModel(ModelPart root) {
            this.bottom = root.getChild("bottom");
            this.middle = root.getChild("middle");
            this.top = root.getChild("top");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -25.0F, -9.0F, 10.0F, 24.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 25.0F, 4.0F));

            PartDefinition cube_r1 = bottom.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, -8.0F, 0.3295F, -0.1172F, 0.3295F));

            PartDefinition cube_r2 = bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.812F, 0.1172F, 2.812F));

            PartDefinition middle = partdefinition.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -25.0F, -1.0F, 8.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 3.0F, -3.0F));

            PartDefinition cube_r3 = middle.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, 6.0F, -1.3526F, -1.3526F, 1.5708F));

            PartDefinition cube_r4 = middle.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.789F, 1.3526F, 1.5708F));

            PartDefinition top = partdefinition.addOrReplaceChild("top", CubeListBuilder.create().texOffs(39, 38).addBox(-1.0F, -25.0F, -3.0F, 4.0F, 22.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -19.0F, 1.0F));

            PartDefinition cube_r5 = top.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, 0.1719F, -0.0302F, 0.1719F));

            PartDefinition cube_r6 = top.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(40, 3).addBox(-5.0F, -10.0F, -1.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.9697F, 0.0302F, 2.9697F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }

        @Override
        public void setupAnim(IceSpikeEntity entity, float partialTicks, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            float scale = entity.getSpikeSize();
            top.visible = false;
            bottom.visible = false;

            int ypos = 26;
            if (scale >= 3) {
                bottom.visible = true;
                ypos -= 26;
                bottom.y = ypos;
            }
            ypos -= 22;
            middle.y = ypos;
            if (scale >= 2) {
                top.visible = true;
                ypos -= 22;
                top.y = ypos;
            }
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int pColor) {
            bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, pColor);
            middle.render(poseStack, vertexConsumer, packedLight, packedOverlay, pColor);
            top.render(poseStack, vertexConsumer, packedLight, packedOverlay, pColor);
        }
    }
}
