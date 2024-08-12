package io.redspace.ironsspellbooks.block.portal_frame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PortalFrameRenderer implements BlockEntityRenderer<PortalFrameBlockEntity> {

    public PortalFrameRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PortalFrameBlockEntity pBlockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        var direction = pBlockEntity.getBlockState().getValue(PortalFrameBlock.FACING);
        if (direction == Direction.EAST || direction == Direction.WEST) {
            poseStack.mulPose(Axis.YP.rotation(Mth.HALF_PI));
        }
        var n = direction.getNormal();
        Vec3 dir = new Vec3(n.getX(), 0, n.getZ()).scale(-(6.5 / 16f));
        poseStack.translate(dir.x, 0, dir.z);
        PortalRenderer.renderPortal(poseStack, pBufferSource, pBlockEntity.getLevel() == null ? 0 : (int) pBlockEntity.getLevel().getGameTime(), pPartialTick, false);
        poseStack.popPose();
    }
}
