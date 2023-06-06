package io.redspace.ironsspellbooks.entity.spells.ice_block;


import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IceBlockRenderer extends GeoEntityRenderer<IceBlockProjectile> {

    public IceBlockRenderer(EntityRendererProvider.Context context) {
        super(context, new IceBlockModel());
        this.shadowRadius = 1.5f;
    }

//    @Override
//    public void render(IceBlockProjectile animatable, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//        animatable.setXRot(0);
//        animatable.xRotO = animatable.getXRot();
//        if (animatable.getDeltaMovement().horizontalDistanceSqr() > 1)
//            animatable.setYRot((float) (Mth.atan2(animatable.getDeltaMovement().x, animatable.getDeltaMovement().z) * (double) (180F / (float) Math.PI)));
//
//        double x = (animatable.getX() - animatable.xOld) * partialTick;
//        double y = (animatable.getY() - animatable.yOld) * partialTick;
//        double z = (animatable.getZ() - animatable.zOld) * partialTick;
//        poseStack.translate(-x, -y, -z);
//        super.render(animatable, yaw, partialTick, poseStack, bufferSource, packedLight);
//    }
}
