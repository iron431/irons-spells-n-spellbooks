package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public class AlchemistCauldronRenderer implements BlockEntityRenderer<AlchemistCauldronTile> {
    ItemRenderer itemRenderer;

    public AlchemistCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final Vec3 ITEM_POS = new Vec3(.5, 1.5, .5);

    @Override
    public void render(AlchemistCauldronTile cauldron, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int waterLevel = cauldron.getLiquidLevel();

        float waterOffset = Mth.lerp(waterLevel / (float) AlchemistCauldronTile.MAX_LEVELS, .25f, .9f);

        if (waterLevel > 0) {
            renderWater(cauldron, poseStack, bufferSource, packedLight, waterOffset);
        }

        var floatingItems = cauldron.inputItems;
        for (int i = 0; i < floatingItems.size(); i++) {
            var itemStack = floatingItems.get(i);
            if (!itemStack.isEmpty()) {
                float f = waterLevel > 0 ? cauldron.getLevel().getGameTime() + partialTick : 15;
                Vec2 floatOffset = getFloatingItemOffset(f, i * 587);
                float yRot = (f + i * 213) / (i + 1) * 1.5f;
                renderItem(itemStack,
                        new Vec3(
                                floatOffset.x,
                                waterOffset + i * .01f,
                                floatOffset.y),
                        yRot, cauldron, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

            }
        }
        if (Minecraft.getInstance().player.isCrouching()) {
            for (int i = 0; i < cauldron.outputItems.size(); i++) {
                var itemStack = cauldron.outputItems.get(i);
                if (!itemStack.isEmpty()) {
                    renderWorldText(itemStack, Component.translatable(itemStack.getDescriptionId()), Display.TextDisplay.Align.LEFT, new Vec3(0.5, 1.2 + i * .2, 0.5), poseStack, bufferSource, packedLight, partialTick);
                }
            }
        }
    }

    public void renderWorldText(
            ItemStack stack,
            Component text,
            Display.TextDisplay.Align alignment,
            Vec3 offset,
            PoseStack poseStack,
            MultiBufferSource pBuffer,
            int pLightmapUV,
            float pPartialTick
    ) {
        boolean seeTextThroughBlocks = false;//(b0 & 2) != 0;
        boolean dropShadow = false;//(b0 & 1) != 0;
        byte opacity = -1;
        float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int i = (int) (f * 255.0F) << 24;
        text = Component.literal("    ").append(text);
        float f2 = 0.0F;
        poseStack.pushPose();
        poseStack.translate((float) offset.x, (float) offset.y, (float) offset.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        Matrix4f matrix4f = poseStack.last().pose();
        matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
        matrix4f.scale(-0.025F, -0.025F, -0.025F);
        var font = Minecraft.getInstance().font;
        int lineHeight = 9 + 1;
        float customScale = .7f;

        int textWidth = (int) (font.width(text) * .7f) + lineHeight;
        int textHeight = (int) (lineHeight * .85f);
        matrix4f.translate(1.0F - (float) textWidth / 2.0F, (float) (-textHeight), 0.0F);
        if (i != 0) {
            RenderHelper.quadBuilder()
                    .matrix(matrix4f)
                    .color(i)
                    .light(pLightmapUV)
                    .vertex(-1, -1, 0)
                    .vertex(-1, textHeight, 0)
                    .vertex(textWidth, textHeight, 0)
                    .vertex(textWidth, -1, 0)
                    .build(pBuffer.getBuffer(RenderType.textBackground()));
//            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.textBackground());
//            vertexconsumer.addVertex(matrix4f, -1.0F, -1.0F, 0.0F).setColor(i).setLight(pLightmapUV);
//            vertexconsumer.addVertex(matrix4f, -1.0F, (float) textHeight, 0.0F).setColor(i).setLight(pLightmapUV);
//            vertexconsumer.addVertex(matrix4f, (float) textWidth, (float) textHeight, 0.0F).setColor(i).setLight(pLightmapUV);
//            vertexconsumer.addVertex(matrix4f, (float) textWidth, -1.0F, 0.0F).setColor(i).setLight(pLightmapUV);
        }

        float f1 = 0;
        matrix4f.scale(customScale);
        matrix4f.translate(0, lineHeight * (1 - customScale) * .5f, 0);

        font
                .drawInBatch(
                        text,
                        f1 + lineHeight / 2f,
                        f2,
                        opacity << 24 | 16777215,
                        dropShadow,
                        matrix4f,
                        pBuffer,
                        seeTextThroughBlocks ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        pLightmapUV
                );
        poseStack.pushPose();
        poseStack.scale(-0.4f / 0.025F, -0.4f / 0.025F, -0.4f / 0.025F);
        poseStack.translate(-0.5, -0.25, -.1);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, pLightmapUV, OverlayTexture.NO_OVERLAY, poseStack, pBuffer, null, 0);
        poseStack.popPose();
        poseStack.popPose();
    }

    public Vec2 getFloatingItemOffset(float time, int offset) {
        //for our case, offset never changes
        float xspeed = offset % 2 == 0 ? .0075f : .025f * (1 + (offset % 88) * .001f);
        float yspeed = offset % 2 == 0 ? .025f : .0075f * (1 + (offset % 88) * .001f);
        float x = (time + offset) * xspeed;
        x = (Math.abs((x % 2) - 1) + 1) / 2;
        float y = (time + offset + 4356) * yspeed;
        y = (Math.abs((y % 2) - 1) + 1) / 2;

        //these values are "bouncing" between 0-1. however, this needs to be bounded to inside the limits of the cauldron, taking into account the item size
        x = Mth.lerp(x, -.2f, .75f);
        y = Mth.lerp(y, -.2f, .75f);
        return new Vec2(x, y);

    }

    private void renderWater(AlchemistCauldronTile cauldron, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float waterOffset) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.beaconBeam(new ResourceLocation(IronsSpellbooks.MODID, "textures/block/water_still.png"), true));
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

//        if (lastv != min_v) {
//            IronsSpellbooks.LOGGER.debug("[{} {}] [{} {}]", min_u, max_u, min_v, max_v);
//            lastv = min_v;
//        }
        consumer.addVertex(pose, 1, waterOffset, 0).setColor(rgb.x(), rgb.y(), rgb.z(), 1f).setUv(max_u, min_v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(pose, 0, waterOffset, 0).setColor(rgb.x(), rgb.y(), rgb.z(), 1f).setUv(min_u, min_v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(pose, 0, waterOffset, 1).setColor(rgb.x(), rgb.y(), rgb.z(), 1f).setUv(min_u, max_v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(pose, 1, waterOffset, 1).setColor(rgb.x(), rgb.y(), rgb.z(), 1f).setUv(max_u, max_v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
    }

//    float lastv;

    private Vector3f colorFromLong(long color) {
        //Copied from potion utils
        return new Vector3f(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f
        );
    }

    private void renderItem(ItemStack itemStack, Vec3 offset, float yRot, AlchemistCauldronTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        //renderId seems to be some kind of uuid/salt
        int renderId = (int) tile.getBlockPos().asLong();
        //BakedModel model = itemRenderer.getModel(itemStack, null, null, renderId);
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(0.4f, 0.4f, 0.4f);

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos()), packedOverlay, poseStack, bufferSource, tile.getLevel(), renderId);

        poseStack.popPose();
    }

}
