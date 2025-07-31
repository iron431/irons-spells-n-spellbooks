package io.redspace.ironsspellbooks.block.scroll_forge;

import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.util.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;


public class  ScrollForgeRenderer implements BlockEntityRenderer<ScrollForgeTile> {
    private static final ResourceLocation PAPER_TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/block/scroll_forge_paper.png");
    private static final ResourceLocation SIGIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/block/scroll_forge_sigil.png");
    ItemRenderer itemRenderer;

    public ScrollForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final Vec3 INK_POS = new Vec3(.175, .876, .25);
    private static final Vec3 FOCUS_POS = new Vec3(.75, .876, .4);
    private static final Vec3 PAPER_POS = new Vec3(.5, .876, .7);

    @Override
    public void render(ScrollForgeTile scrollForgeTile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack inkStack = scrollForgeTile.getStackInSlot(0);
        ItemStack paperStack = scrollForgeTile.getStackInSlot(1);
        ItemStack focusStack = scrollForgeTile.getItemHandler().getStackInSlot(2);

        if (!inkStack.isEmpty() && inkStack.getItem() instanceof InkItem) {
            renderItem(inkStack, INK_POS, 15, scrollForgeTile, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (!focusStack.isEmpty() && focusStack.is(ModTags.SCHOOL_FOCUS)) {
            renderItem(focusStack, FOCUS_POS, 5, scrollForgeTile, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }

        if (!paperStack.isEmpty() && paperStack.is(Items.PAPER)) {
            poseStack.pushPose();
            rotatePoseWithBlock(poseStack, scrollForgeTile);
            poseStack.translate(PAPER_POS.x, PAPER_POS.y, PAPER_POS.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(85));
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(PAPER_TEXTURE));
            var light = LevelRenderer.getLightColor(scrollForgeTile.getLevel(), scrollForgeTile.getBlockPos());

            drawQuad(.45f, poseStack.last(), consumer, light);
            poseStack.popPose();

        }

        /*
        //Test Sigil
        float angle = (Minecraft.getInstance().player.tickCount + partialTick )%360;
        poseStack.pushPose();
        rotatePoseWithBlock(poseStack, scrollForgeTile);
        poseStack.translate(INK_POS.x, INK_POS.y, INK_POS.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(SIGIL_TEXTURE));
        drawQuad(.5f, poseStack.last(), consumer, LightTexture.FULL_BRIGHT);
        poseStack.popPose();

        */


    }

    private void renderItem(ItemStack itemStack, Vec3 offset, float yRot, ScrollForgeTile scrollForgeTile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        //renderId seems to be some kind of uuid/salt
        int renderId = (int) scrollForgeTile.getBlockPos().asLong();
        //BakedModel model = itemRenderer.getModel(itemStack, null, null, renderId);

        rotatePoseWithBlock(poseStack, scrollForgeTile);

        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-yRot));

        //poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.45f, 0.45f, 0.45f);

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, LevelRenderer.getLightColor(scrollForgeTile.getLevel(), scrollForgeTile.getBlockPos()), packedOverlay, poseStack, bufferSource, scrollForgeTile.getLevel(), renderId);
        poseStack.popPose();
    }

    private void drawQuad(float width, PoseStack.Pose pose, VertexConsumer consumer, int light) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float halfWidth = width * .5f;
        consumer.vertex(poseMatrix, -halfWidth, 0, -halfWidth).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, -1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, 0, -halfWidth).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, -1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, 0, halfWidth).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, -1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -halfWidth, 0, halfWidth).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, -1f, 0f).endVertex();

    }

    private void rotatePoseWithBlock(PoseStack poseStack, ScrollForgeTile scrollForgeTile) {
        Vec3 center = new Vec3(0.5, 0.5, 0.5);
        poseStack.translate(center.x, center.y, center.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(getBlockFacingDegrees(scrollForgeTile)));
        poseStack.translate(-center.x, -center.y, -center.z);
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
