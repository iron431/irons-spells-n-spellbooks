package io.redspace.ironsspellbooks.entity.spells.skull_projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SkullProjectileRenderer extends EntityRenderer<AbstractMagicProjectile> {

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(IronsSpellbooks.MODID, "skull_model"), "main");

    private final ModelPart model;
    private final ResourceLocation textureLocation;

    public SkullProjectileRenderer(Context context, ResourceLocation textureLocation) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.model = modelpart.getChild("head");
        this.textureLocation = textureLocation;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void render(AbstractMagicProjectile entity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float f = Mth.rotLerp(pPartialTicks, entity.yRotO, entity.getYRot());
        float f1 = Mth.lerp(pPartialTicks, entity.xRotO, entity.getXRot());
        model.yRot = f * (float) (Math.PI / 180.0);
        model.xRot = f1 * (float) (Math.PI / 180.0);

        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.render(poseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, pEntityYaw, pPartialTicks, poseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractMagicProjectile entity) {
        return textureLocation;
    }
}