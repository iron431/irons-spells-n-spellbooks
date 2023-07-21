package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.block.pedestal.PedestalTile;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;


public class AlchemistCauldronRenderer implements BlockEntityRenderer<AlchemistCauldronTile> {
    ItemRenderer itemRenderer;

    public AlchemistCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final Vec3 ITEM_POS = new Vec3(.5, 1.5, .5);

    @Override
    public void render(AlchemistCauldronTile cauldron, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (cauldron.getBlockState().getValue(AlchemistCauldronBlock.LEVEL) > 0) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.beaconBeam(new ResourceLocation("textures/block/water_still.png"), true));
            long color = cauldron.getAverageWaterColor();
            var rgb = colorFromLong(color);

            Matrix4f pose = poseStack.last().pose();
            int frames = 32;
            float frameSize = 1f / frames;
            long frame = (cauldron.getLevel().getGameTime() / 3) % frames;
            float min_u = 0;
            float max_u = 1;
            float min_v = (frameSize * frame);
            float max_v = (frameSize * (frame + 1));


            float yPos = Mth.lerp(cauldron.getBlockState().getValue(AlchemistCauldronBlock.LEVEL) / (float) AlchemistCauldronBlock.MAX_LEVELS, .2f, .9f);

            consumer.vertex(pose, 1, yPos, 0).color(rgb.x(), rgb.y(), rgb.z(), 1f).uv(max_u, min_v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, 0, yPos, 0).color(rgb.x(), rgb.y(), rgb.z(), 1f).uv(min_u, min_v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, 0, yPos, 1).color(rgb.x(), rgb.y(), rgb.z(), 1f).uv(min_u, max_v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, 1, yPos, 1).color(rgb.x(), rgb.y(), rgb.z(), 1f).uv(max_u, max_v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();

        }

    }

    private Vector3f colorFromLong(long color) {
        return new Vector3f(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f
        );
    }

    private void renderItem(ItemStack itemStack, Vec3 offset, float yRot, PedestalTile pedestalTile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        //renderId seems to be some kind of uuid/salt
        int renderId = (int) pedestalTile.getBlockPos().asLong();
        //BakedModel model = itemRenderer.getModel(itemStack, null, null, renderId);

        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
        if (itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof DiggerItem) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-45));

        }
        //poseStack.mulPose(Vector3f.ZP.rotationDegrees(yRot));

        //poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        poseStack.scale(0.65f, 0.65f, 0.65f);

        itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, LevelRenderer.getLightColor(pedestalTile.getLevel(), pedestalTile.getBlockPos()), packedOverlay, poseStack, bufferSource, renderId);
        poseStack.popPose();
    }

}
