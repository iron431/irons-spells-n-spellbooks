package com.example.testmod.entity.mobs.debug_wizard;


import com.example.testmod.render.GeoEvasionLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DebugWizardRenderer extends GeoEntityRenderer<DebugWizard> {
    public DebugWizardRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DebugWizardModel());
        this.shadowRadius = 0.3f;
        this.addLayer(new GeoEvasionLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(DebugWizard animatable) {
        return DebugWizardModel.textureResource;
    }

    @Override
    public RenderType getRenderType(DebugWizard animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }

    @Override
    public void render(GeoModel model, DebugWizard animatable, float partialTick, RenderType type, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}