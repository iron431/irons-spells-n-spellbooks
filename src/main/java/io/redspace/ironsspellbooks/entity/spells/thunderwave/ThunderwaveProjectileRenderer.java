package io.redspace.ironsspellbooks.entity.spells.thunderwave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ThunderwaveProjectileRenderer extends EntityRenderer<ThunderwaveProjectile> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance.png");

    public ThunderwaveProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }


    @Override
    public void render(ThunderwaveProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
//        LightningLanceRenderer.renderModel(poseStack, bufferSource, entity.tickCount);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }


    @Override
    public ResourceLocation getTextureLocation(ThunderwaveProjectile entity) {
        return TEXTURE;
    }
}
