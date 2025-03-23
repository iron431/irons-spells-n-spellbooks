package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.gui.overlays.ScreenTooltipOverlay;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class AlchemistCauldronRenderer implements BlockEntityRenderer<AlchemistCauldronTile> {
    ItemRenderer itemRenderer;

    public AlchemistCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final Vec3 ITEM_POS = new Vec3(.5, 1.5, .5);

    @Override
    public void render(AlchemistCauldronTile cauldron, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int waterLevel = cauldron.getFluidAmount();

        float waterOffset = Mth.lerp(waterLevel / 1000f, .25f, .9f);

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
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (Math.abs(player.getX() - cauldron.getBlockPos().getX()) < 5 && Math.abs(player.getY() - cauldron.getBlockPos().getY()) < 5 && Math.abs(player.getZ() - cauldron.getBlockPos().getZ()) < 5) {
                if (player.isCrouching() && Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(cauldron.getBlockPos())) {
                    List<Component> text = new ArrayList<>();
                    text.add(Component.translatable("block.irons_spellbooks.alchemist_cauldron").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.WHITE));
                    var fluids = cauldron.fluidInventory.fluids();
                    if (fluids.isEmpty()) {
                        text.add(Component.translatable("ui.irons_spellbooks.empty").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    } else {
                        List<ObjectIntImmutablePair<MutableComponent>> fluidInfo = new ArrayList<>();
                        for (int i = fluids.size() - 1; i >= 0; i--) {
                            var fluid  = fluids.get(i);
                            fluidInfo.add(new ObjectIntImmutablePair<>(fluid.getFluidType().getDescription(fluid).copy().withStyle(ChatFormatting.DARK_AQUA), fluid.getAmount()));
                        }

                        for (ObjectIntImmutablePair<MutableComponent> info : fluidInfo) {
                            text.add(Component.literal("  ").append(info.left()).append(": ").append(Component.literal(info.rightInt() + "mb").withStyle(ChatFormatting.GOLD)));
                        }
                    }
                    ScreenTooltipOverlay.renderTooltip(text, (sw, sh, mx, my, tw, th) -> new Vector2i(sw / 2 + 30, sh / 2 - th / 2));
                }
            }
        }
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
        Matrix4f pose = poseStack.last().pose();
        float totalFluid = cauldron.getFluidAmount();
        float runningFluid = totalFluid;
        float f = 0;
        float padding = 1 / 16f;
        for (FluidStack fluid : cauldron.fluidInventory.fluids()) {
            int skylight = packedLight >> 4 & 15;
            int luminosity = Math.max(skylight, fluid.getFluidType().getLightLevel(fluid));
            int fluidlight = packedLight & 0xF00000 | luminosity << 4;
            IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid.getFluid());
            Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            TextureAtlasSprite texture = spriteAtlas.apply(clientFluid.getStillTexture(fluid.getFluid().defaultFluidState(), cauldron.getLevel(), cauldron.getBlockPos()));
            VertexConsumer consumer = texture.wrap(bufferSource.getBuffer(RenderType.translucent()));
            var rgb = colorFromLong(clientFluid.getTintColor(fluid) & clientFluid.getTintColor(fluid.getFluid().defaultFluidState(), cauldron.getLevel(), cauldron.getBlockPos())); // if either returns 0xFFFFFF (white) the bitwise and will choose the one that doesnt. if they return the same, we get the same
            float opacity = runningFluid / totalFluid; // creates naturally weighted sum for the opacity of proceeding layers
            runningFluid -= fluid.getAmount();
            consumer.addVertex(pose, 1 - padding, waterOffset + f, 0 + padding).setColor(rgb.x(), rgb.y(), rgb.z(), opacity).setUv(1 - padding, 0 + padding).setOverlay(OverlayTexture.NO_OVERLAY).setLight(fluidlight).setNormal(0, 1, 0);
            consumer.addVertex(pose, 0 + padding, waterOffset + f, 0 + padding).setColor(rgb.x(), rgb.y(), rgb.z(), opacity).setUv(0 + padding, 0 + padding).setOverlay(OverlayTexture.NO_OVERLAY).setLight(fluidlight).setNormal(0, 1, 0);
            consumer.addVertex(pose, 0 + padding, waterOffset + f, 1 - padding).setColor(rgb.x(), rgb.y(), rgb.z(), opacity).setUv(0 + padding, 1 - padding).setOverlay(OverlayTexture.NO_OVERLAY).setLight(fluidlight).setNormal(0, 1, 0);
            consumer.addVertex(pose, 1 - padding, waterOffset + f, 1 - padding).setColor(rgb.x(), rgb.y(), rgb.z(), opacity).setUv(1 - padding, 1 - padding).setOverlay(OverlayTexture.NO_OVERLAY).setLight(fluidlight).setNormal(0, 1, 0);
            f += 0.001f;
        }
    }

    private Vector3f colorFromLong(long color) {
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
