package io.redspace.ironsspellbooks.entity.spells.root;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RootRenderer extends GeoEntityRenderer<RootEntity> {
    public RootRenderer(EntityRendererProvider.Context context) {
        super(context, new RootModel());
    }

    @Override
    public void renderEarly(RootEntity animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        var rooted = animatable.getFirstPassenger();

        if (rooted != null) {
            float scale = rooted.getBbWidth() / 0.6f; //.6 is the default player bb width
            poseStack.scale(scale, scale, scale);
        }

        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
    }

    @Override
    public RenderType getRenderType(RootEntity animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
