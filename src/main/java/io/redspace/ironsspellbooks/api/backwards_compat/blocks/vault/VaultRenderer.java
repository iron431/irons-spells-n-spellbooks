package io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data.VaultBlockEntity;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data.VaultClientData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public VaultRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    public void render(VaultBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (VaultBlockEntity.Client.shouldDisplayActiveEffects(blockEntity.getSharedData())) {
            Level level = blockEntity.getLevel();
            if (level != null) {
                ItemStack itemstack = blockEntity.getSharedData().getDisplayItem();
                if (!itemstack.isEmpty()) {
                    this.random.setSeed(0/*(long)ItemEntityRenderer.getSeedForItemStack(itemstack)*/);
                    VaultClientData vaultclientdata = blockEntity.getClientData();
                    renderItemInside(
                            partialTick,
                            level,
                            poseStack,
                            bufferSource,
                            packedLight,
                            itemstack,
                            this.itemRenderer,
                            vaultclientdata.previousSpin(),
                            vaultclientdata.currentSpin(),
                            this.random
                    );
                }
            }
        }
    }

    public static void renderItemInside(
            float partialTick,
            Level level,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            ItemStack item,
            ItemRenderer itemRenderer,
            float previousSpin,
            float currentSpin,
            RandomSource random
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.4F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, previousSpin, currentSpin)));
        renderMultipleFromCount(itemRenderer, poseStack, buffer, packedLight, item, random, level);
        poseStack.popPose();
    }

    public static void renderMultipleFromCount(
            ItemRenderer itemRenderer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack item, RandomSource random, Level level
    ) {
        BakedModel bakedmodel = itemRenderer.getModel(item, level, null, 0);
        renderMultipleFromCount(itemRenderer, poseStack, buffer, packedLight, item, bakedmodel, bakedmodel.isGui3d(), random);
    }

    static int getRenderedAmount(int count) {
        if (count <= 1) {
            return 1;
        } else if (count <= 16) {
            return 2;
        } else if (count <= 32) {
            return 3;
        } else {
            return count <= 48 ? 4 : 5;
        }
    }

    public static void renderMultipleFromCount(
            ItemRenderer itemRenderer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            ItemStack item,
            BakedModel model,
            boolean isGui3d,
            RandomSource random
    ) {
        int i = getRenderedAmount(item.getCount());
        float f = model.getTransforms().ground.scale.x();
        float f1 = model.getTransforms().ground.scale.y();
        float f2 = model.getTransforms().ground.scale.z();
        if (!isGui3d) {
            float f3 = -0.0F * (float) (i - 1) * 0.5F * f;
            float f4 = -0.0F * (float) (i - 1) * 0.5F * f1;
            float f5 = -0.09375F * (float) (i - 1) * 0.5F * f2;
            poseStack.translate(f3, f4, f5);
        }

        boolean shouldSpread = false;//net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(item).shouldSpreadAsEntity(item);
        for (int j = 0; j < i; j++) {
            poseStack.pushPose();
            if (j > 0 && shouldSpread) {
                if (isGui3d) {
                    float f7 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    poseStack.translate(f7, f9, f6);
                } else {
                    float f8 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    poseStack.translate(f8, f10, 0.0F);
                }
            }

            itemRenderer.render(item, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
            if (!isGui3d) {
                poseStack.translate(0.0F * f, 0.0F * f1, 0.09375F * f2);
            }
        }
    }
}
