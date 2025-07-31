package io.redspace.ironsspellbooks.entity.spells.ice_tomb;

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

public class IceTombRenderer extends EntityRenderer<IceTombEntity> {

    public static final ResourceLocation NOCULL = IronsSpellbooks.id("textures/entity/ice_tomb/ice_tomb.png");
    public static final ResourceLocation CULL = IronsSpellbooks.id("textures/entity/ice_tomb/ice_tomb_cull.png");
    private final IceTombModel model;

    public IceTombRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new IceTombModel(pContext.bakeLayer(IceTombModel.LAYER_LOCATION));
    }

    public void render(IceTombEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        float xScaleFactor = entity.getBbWidth() / entity.getType().getDimensions().width();
        float yScaleFactor = entity.getBbHeight() / entity.getType().getDimensions().height();
        poseStack.scale(xScaleFactor, -yScaleFactor, -xScaleFactor);
        poseStack.translate(0, -1.501, 0);
        this.model.setupAnim(entity, partialTicks, 0.0F, 0.0F, entity.getYRot(), entity.getXRot());
        VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(NOCULL));
        this.model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, -1);
        vertexconsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentCull(CULL));
        this.model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(IceTombEntity pEntity) {
        return NOCULL;
    }

    public static class IceTombModel extends EntityModel<IceTombEntity> {
        public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "ice_tomb"), "main");
        private final ModelPart model;

        public IceTombModel(ModelPart root) {
            this.model = root.getChild("model");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition bb_main = partdefinition.addOrReplaceChild("model", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -36.0F, -8.0F, 16.0F, 36.0F, 16.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 67).addBox(4.0F, -9.0F, 4.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 52).addBox(1.0F, -24.0F, -11.0F, 10.0F, 24.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(64, 0).addBox(-8.0F, -36.0F, -8.0F, 16.0F, 36.0F, 16.0F, new CubeDeformation(-0.01F))
                    .texOffs(40, 52).addBox(-10.0F, -9.0F, -10.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 86).addBox(-11.0F, -24.0F, 1.0F, 10.0F, 24.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 128, 128);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int pColor) {
            model.render(poseStack, vertexConsumer, packedLight, packedOverlay, pColor);
        }

        @Override
        public void setupAnim(IceTombEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        }
    }
}
