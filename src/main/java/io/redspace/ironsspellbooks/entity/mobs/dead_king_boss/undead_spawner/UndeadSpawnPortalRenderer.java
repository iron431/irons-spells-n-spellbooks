package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.undead_spawner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class UndeadSpawnPortalRenderer extends PortalRenderer<UndeadSpawnPortalEntity> {

    public UndeadSpawnPortalRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(UndeadSpawnPortalEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));

        renderPortal(BLOOD, poseStack, bufferSource, entity.tickCount, partialTicks, true, -1);

        poseStack.popPose();
    }
}