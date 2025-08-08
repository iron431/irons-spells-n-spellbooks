package io.redspace.ironsspellbooks.entity.spells.fiery_dagger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FieryDaggerRenderer extends GeoEntityRenderer<FieryDaggerEntity> {
    public FieryDaggerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FieryDaggerModel());
    }

    @Override
    public void preRender(PoseStack poseStack, FieryDaggerEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        Vec3 motion = entity.deltaMovementOld.add(entity.getDeltaMovement().subtract(entity.deltaMovementOld).scale(partialTick));
        float xRot = ((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) - 90.0F);
        poseStack.translate(0, entity.getBbHeight() * .5f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
    }

    @Override
    public Color getRenderColor(FieryDaggerEntity animatable, float partialTick, int packedLight) {
        return Color.LIGHT_GRAY;
    }
}
