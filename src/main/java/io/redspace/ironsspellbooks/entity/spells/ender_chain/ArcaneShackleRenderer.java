package io.redspace.ironsspellbooks.entity.spells.ender_chain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArcaneShackleRenderer extends EntityRenderer<ArcaneShackleProjectile> {
    public ArcaneShackleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(ArcaneShackleProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        for (int i = 0; i < 3; i++) {
            poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 8));
            poseStack.pushPose();
            float scale = (1 + i * .4f) * 0.5f;
            poseStack.scale(scale, scale, scale);
            float radius = 0.5f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(40 * (i - 1)));
            renderChainRing(poseStack, bufferSource, radius);
            poseStack.popPose();
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    public static void renderChainRing(PoseStack poseStack, MultiBufferSource bufferSource, float radius) {
        Vec3[] corners = new Vec3[]{new Vec3(1, 0, 1), new Vec3(-1, 0, 1), new Vec3(-1, 0, -1), new Vec3(1, 0, -1)};
        for (int i = 0; i < 4; i++) {
            EnderChainRenderer.renderChainBetween(corners[i].scale(radius), corners[(i + 1) % 4].scale(radius), poseStack, bufferSource);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ArcaneShackleProjectile entity) {
        return EnderChainRenderer.CHAIN_TEXTURE;
    }


}
