package io.redspace.ironsspellbooks.entity.spells.void_tentacle;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidTentacleRenderer extends GeoEntityRenderer<VoidTentacle> {

    public VoidTentacleRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidTentacleModel());
        addRenderLayer(new VoidTentacleEmissiveLayer(this));
        this.shadowRadius = 1f;
    }

//    @Override
//    public RenderType getRenderType(VoidTentacle animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
//        return RenderType.endGateway();
//    }


    @Override
    public void render(VoidTentacle animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
