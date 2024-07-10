package io.redspace.ironsspellbooks.entity.spells.root;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.render.GeoLivingEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class RootRenderer extends GeoLivingEntityRenderer<RootEntity> {
    public RootRenderer(EntityRendererProvider.Context context) {
        super(context, new RootModel());
    }

    @Override
    public void preRender(PoseStack poseStack, RootEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        var rooted = animatable.getFirstPassenger();

        if (rooted != null) {
            float scale = rooted.getBbWidth() / 0.6f; //.6 is the default player bb width
            poseStack.scale(scale, scale, scale);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
