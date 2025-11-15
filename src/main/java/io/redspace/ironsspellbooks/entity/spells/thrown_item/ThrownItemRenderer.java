package io.redspace.ironsspellbooks.entity.spells.thrown_item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ThrownItemRenderer extends EntityRenderer<ThrownItemProjectile> {

    public ThrownItemRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ThrownItemProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var item = entity.getThrownItem();
        if (item.isEmpty()) {
            item = Items.STONE.getDefaultInstance();
        }
        poseStack.pushPose();
        Vec3 motion = entity.deltaMovementOld.add(entity.getDeltaMovement().subtract(entity.deltaMovementOld).scale(partialTick));
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot - (entity.tickCount + partialTick) * 36));
        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level, 0);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownItemProjectile entity) {
        return IronsSpellbooks.id("empty");
    }
}