package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SpecialItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final ItemRenderer renderer;
    public final String name;

    public SpecialItemRenderer(ItemRenderer renderDispatcher, EntityModelSet modelSet, String name) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), modelSet);
        this.renderer = renderDispatcher;
        this.name = name;
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        BakedModel model;
        if (transformType == ItemTransforms.TransformType.GUI) {
            Lighting.setupForFlatItems();
            model = renderer.getItemModelShaper().getModelManager().getModel(new ResourceLocation(IronsSpellbooks.MODID, "item/" + name + "_gui"));
            renderer.render(itemStack, transformType, false, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            Lighting.setupFor3DItems();
        } else {
            model = renderer.getItemModelShaper().getModelManager().getModel(new ResourceLocation(IronsSpellbooks.MODID, "item/" + name + "_normal"));
            boolean leftHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
            renderer.render(itemStack, transformType, leftHand, poseStack, bufferSource, combinedLightIn, combinedOverlayIn, model);
        }
        poseStack.popPose();
    }
}
