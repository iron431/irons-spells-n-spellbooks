package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.TrialSpawner;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.TrialSpawnerData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrialSpawnerRenderer implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    public void render(TrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level != null) {
            TrialSpawner trialspawner = blockEntity.getTrialSpawner();
            TrialSpawnerData trialspawnerdata = trialspawner.getData();
            Entity entity = trialspawnerdata.getOrCreateDisplayEntity(trialspawner, level, trialspawner.getState());
            if (entity != null) {
                renderEntityInSpawner(
                    partialTick, poseStack, bufferSource, packedLight, entity, this.entityRenderer, trialspawnerdata.getOSpin(), trialspawnerdata.getSpin()
                );
            }
        }
    }

    public static void renderEntityInSpawner(
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Entity entity,
            EntityRenderDispatcher entityRenderer,
            double oSpin,
            double spin
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        float f = 0.53125F;
        float f1 = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if ((double)f1 > 1.0) {
            f /= f1;
        }

        poseStack.translate(0.0F, 0.4F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp((double)partialTick, oSpin, spin) * 10.0F));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(f, f, f);
        entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

//    @Override
//    public boolean shouldRender(TrialSpawnerBlockEntity pBlockEntity, Vec3 pCameraPos) {
//        return BlockEntityRenderer.super.shouldRender(pBlockEntity, pCameraPos);
//    }
//
//    @Override
//    public net.minecraft.world.phys.AABB getRenderBoundingBox(TrialSpawnerBlockEntity blockEntity) {
//        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
//        return new net.minecraft.world.phys.AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
//    }
}
