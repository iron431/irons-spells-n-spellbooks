package com.example.testmod.block.scroll_forge;

import com.example.testmod.TestMod;
import com.example.testmod.item.InkItem;
import com.example.testmod.util.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.minecraft.core.Direction.SOUTH;


public class ScrollForgeRenderer implements BlockEntityRenderer<ScrollForgeTile> {

    ItemRenderer itemRenderer;

    public ScrollForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ScrollForgeTile scrollForgeTile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack inkStack = scrollForgeTile.getStackInSlot(0);


        ItemStack focusStack = scrollForgeTile.getItemHandler().getStackInSlot(2);

        if (!inkStack.isEmpty() && inkStack.getItem() instanceof InkItem) {
            renderItem(inkStack, new Vec3(.25, 1.125, .4), scrollForgeTile, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (!focusStack.isEmpty() && focusStack.is(ModTags.SCHOOL_FOCUS)) {
            renderItem(focusStack, new Vec3(.75, 1.125, .4), scrollForgeTile, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
//        if (itemStack != null && !itemStack.isEmpty()) {
//            matrixStack.pushPose();
//
//            int renderId = (int) workbenchTile.getBlockPos().asLong();
//
//            BakedModel model = itemRenderer.getModel(itemStack, workbenchTile.getLevel(), null, renderId);
//            if (itemStack.getItem() instanceof ModularShieldItem) {
//                matrixStack.translate(0.375, 0.9125, 0.5);
//                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
////                matrixStack.scale(0.5f, 0.5f, 0.5f);
//            } else if (model.isGui3d()) {
//                matrixStack.translate(0.5, 1.125, 0.5);
//                matrixStack.scale(.5f, .5f, .5f);
//            } else {
//                matrixStack.translate(0.5, 1.0125, 0.5);
//                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//                matrixStack.scale(0.5f, 0.5f, 0.5f);
//            }
//
//            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED,
//                    LevelRenderer.getLightColor(workbenchTile.getLevel(), workbenchTile.getBlockPos().above()),
//                    combinedOverlay, matrixStack, buffer, renderId);
//
//            matrixStack.popPose();
//        }
    }

    private void renderItem(ItemStack itemStack, Vec3 offset, ScrollForgeTile scrollForgeTile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        //renderId seems to be some kind of uuid/salt
        int renderId = (int) scrollForgeTile.getBlockPos().asLong();
        //BakedModel model = itemRenderer.getModel(itemStack, null, null, renderId);

        Vec3 center = new Vec3(0.5, 0.5, 0.5);
        poseStack.translate(center.x, center.y, center.z);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(getBlockFacingDegrees(scrollForgeTile)));
        poseStack.translate(-center.x, -center.y, -center.z);

        poseStack.translate(offset.x, offset.y, offset.z);

        //poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        poseStack.scale(0.35f, 0.35f, 0.35f);
        float angle = getAngle(Minecraft.getInstance().player, partialTick);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(angle * 2));
        double floatOffset = Math.sin(angle * .1f) * .15f;
        poseStack.translate(0, floatOffset, 0);

        itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, LevelRenderer.getLightColor(scrollForgeTile.getLevel(), scrollForgeTile.getBlockPos().above()), packedOverlay, poseStack, bufferSource, renderId);
        poseStack.popPose();
    }

    private float getAngle(Player player, float partialTick) {
        return (player.tickCount + partialTick) % 360;
    }

    private int getBlockFacingDegrees(ScrollForgeTile tileEntity) {
        var block = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
        if (block.getBlock() instanceof ScrollForgeBlock) {
            var facing = block.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return switch (facing) {
                case NORTH -> 180;
                case EAST -> 90;
                case WEST -> -90;
                default -> 0;
            };
        } else
            return 0;

    }
}
