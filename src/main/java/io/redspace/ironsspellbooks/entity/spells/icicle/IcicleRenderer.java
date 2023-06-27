package io.redspace.ironsspellbooks.entity.spells.icicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class IcicleRenderer extends EntityRenderer<IcicleProjectile> {
    public static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/icicle_projectile.png");
    public static final ResourceLocation TEXTURE_2 = IronsSpellbooks.id("textures/entity/icicle_projectile2.png");
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(IronsSpellbooks.MODID, "icicle_model"), "main");

    private final ModelPart body;
    private final ModelPart tip;

    public IcicleRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(IcicleRenderer.MODEL_LAYER_LOCATION);
        this.tip = modelpart.getChild("tip");
        this.body = modelpart.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -6.0F, 1.5F, 1.5F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.25F, -1.25F, -1.0F, 2.5F, 2.5F, 5.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 20, 10);
    }

    @Override
    public void render(IcicleProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();


        //poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        //poseStack.mulPose(Vector3f.YP.rotationDegrees(180f));

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));

        VertexConsumer consumer2 = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_2));
        this.body.render(poseStack, consumer2, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        this.tip.render(poseStack, consumer2, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        //poseStack.scale(0.0625f, 0.0625f, 0.0625f);
//        poseStack.scale(.875f, .875f, .875f);
//        //Vertical plane
//        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
//        consumer.vertex(poseMatrix, 0, -0.5f, -0.5f).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, 0.5f, -0.5f).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, 0.5f, 0.5f).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, -0.5f, 0.5f).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
////
//        //Horizontal plane
//        consumer.vertex(poseMatrix, -0.5f, 0, -0.5f).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0.5f, 0, -0.5f).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0.5f, 0, 0.5f).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, -0.5f, 0, 0.5f).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//
//        //VertexConsumer consumer2 = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_2));
//        //this.body.render(poseStack, consumer2, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(IcicleProjectile entity) {
        return TEXTURE;
    }
}