package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironslib.util.Color;
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
import org.jetbrains.annotations.NotNull;

public class DeadKingSoulRenderer extends EntityRenderer<DeadKingSoulEntity> {
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "dead_king_orb_model"), "main");
    public static final ModelLayerLocation CROWN_CUBE_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "dead_king_soul_crown_cube"), "main");
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/magic_missile/magic_missile.png");
    private static final ResourceLocation CROWN_TEXTURE = IronsSpellbooks.id("textures/entity/dead_king/dead_king_crown.png");
    private final ModelPart body;
    private final ModelPart crownCube;

    public DeadKingSoulRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.body = modelpart.getChild("body");
        this.crownCube = context.bakeLayer(CROWN_CUBE_LAYER_LOCATION).getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, 4.0F, 4.0F, -8.0F, -8.0F, -8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 48, 24);
    }

    public static LayerDefinition createCrownCubeLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    @Override
    public void render(DeadKingSoulEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() * 1.5, 0);
        poseStack.scale(1.5f, -1.5f, 1.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount + partialTicks));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin((entity.tickCount + partialTicks) / 100f) * 6f));
        VertexConsumer crownConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEXTURE));
        this.crownCube.render(poseStack, crownConsumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.translate(0, entity.getBbHeight() * .5f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.scale(0.75f, 0.75f, 0.75f);
        float f = entity.tickCount + entity.getId() * 31 + partialTicks;

        VertexConsumer consumer;// = bufferSource.getBuffer(renderType(getTextureLocation(entity)));

        float noise = ((Mth.sin(f * .37f) + Mth.cos(f * .09f)) / 3 + 2) * .25f;
        float noise1 = ((Mth.cos(f * .065f) + Mth.sin(f * .09f)) / 3) * .15f;
        float noise2 = ((Mth.sin(f * .05f) + Mth.cos(f * .07f)) / 3) * .15f;
        float noise3 = ((Mth.sin(f * .08f) + Mth.cos(f * .05f)) / 3) * .15f;
        poseStack.translate(noise1, noise2, noise3);

        Color color = new Color(0x2ac9cf);
        consumer = bufferSource.getBuffer(renderType(TEXTURE));
        renderOrb(poseStack, consumer, 1f, noise, 0.25f, 255, 255, 255);
        consumer = bufferSource.getBuffer(renderType(TEXTURE));
        renderOrb(poseStack, consumer, 0.6f, noise, 1f, color.red(), color.blue(), color.green());
        consumer = bufferSource.getBuffer(renderType(TEXTURE));
        renderOrb(poseStack, consumer, 0.4f, noise, 1.5f, color.red(), color.blue(), color.green());

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void renderOrb(PoseStack poseStack, VertexConsumer consumer, float intensity, float jitter, float scale, int r, int g, int b) {
        poseStack.pushPose();
        scale += jitter;
        poseStack.scale(scale, scale, scale);
        this.body.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r * intensity / 255f, g * intensity / 255f, b * intensity / 255f, intensity);
        poseStack.popPose();
    }

    public RenderType renderType(ResourceLocation TEXTURE) {
        return RenderType.lightning();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DeadKingSoulEntity entity) {
        return TEXTURE;
    }

}