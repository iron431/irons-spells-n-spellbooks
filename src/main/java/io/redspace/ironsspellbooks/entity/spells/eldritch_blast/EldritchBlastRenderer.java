package io.redspace.ironsspellbooks.entity.spells.eldritch_blast;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Function;

public class EldritchBlastRenderer extends EntityRenderer<EldritchBlastVisualEntity> {

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(IronsSpellbooks.MODID, "eldritch_blast_model"), "main");
    private static final ResourceLocation TEXTURE_CORE = IronsSpellbooks.id("textures/entity/eldritch_blast/core.png");
    private static final ResourceLocation TEXTURE_OVERLAY = IronsSpellbooks.id("textures/entity/eldritch_blast/overlay.png");

    private final ModelPart body;

    public EldritchBlastRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.body = modelpart.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8, -16, -8, 16, 32, 16), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public boolean shouldRender(EldritchBlastVisualEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public void render(EldritchBlastVisualEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        float lifetime = EldritchBlastVisualEntity.lifetime;
        float scalar = .25f;
        float length = 32 * scalar * scalar;
        float f = entity.tickCount + partialTicks;
        poseStack.translate(0, entity.getBoundingBox().getYsize() * .5f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot() - 90));
        poseStack.scale(scalar, scalar, scalar);

        //float scale = Mth.lerp(Mth.clamp(f / 6f, 0, 1), 1, 2.3f);

        float alpha = Mth.clamp(1f - f / lifetime, 0, 1);

        for (float i = 0; i < entity.distance * 4; i += length) {
            poseStack.translate(0, length, 0);
            //Render overlay
            //VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE_OVERLAY));
            VertexConsumer consumer = bufferSource.getBuffer(EldritchBlastRenderType.energySwirl(TEXTURE_OVERLAY, 0, 0));
            {
                poseStack.pushPose();
                float expansion = Mth.clampedLerp(1.2f, 0, f / (lifetime));
                poseStack.mulPose(Axis.YP.rotationDegrees(f * 5));
                poseStack.scale(expansion, 1, expansion);
                poseStack.mulPose(Axis.YP.rotationDegrees(45));
                this.body.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, alpha);
                poseStack.popPose();
            }
            //Render core
            consumer = bufferSource.getBuffer(EldritchBlastRenderType.eldritchBlast(TEXTURE_CORE));
            //consumer = bufferSource.getBuffer(EldritchBlastRenderType.eldritchBlast(TEXTURE_CORE));
            {
                poseStack.pushPose();
                float expansion = Mth.clampedLerp(1, 0, f / (lifetime - 8));
                poseStack.scale(expansion, 1, expansion);
                poseStack.mulPose(Axis.YP.rotationDegrees(f * -10));
                this.body.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                poseStack.popPose();
            }
        }


        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchBlastVisualEntity entity) {
        return TEXTURE_CORE;
    }

    static class EldritchBlastRenderType extends RenderType {
        private static final Function<ResourceLocation, RenderType> ELDRITCH_BLAST = Util.memoize((p_173253_) -> {
            RenderStateShard.TextureStateShard renderstateshard$texturestateshard = new RenderStateShard.TextureStateShard(p_173253_, false, false);
            return create("eldritch_blast", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_CRUMBLING_SHADER).setTextureState(renderstateshard$texturestateshard).setTransparencyState(CRUMBLING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
        });

        public EldritchBlastRenderType(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static RenderType eldritchBlast(ResourceLocation pLocation) {
            return ELDRITCH_BLAST.apply(pLocation);
        }
    }
}