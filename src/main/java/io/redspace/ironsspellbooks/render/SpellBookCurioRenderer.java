package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class SpellBookCurioRenderer implements ICurioRenderer {
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    Optional<BakedModel> model = Optional.empty();
    boolean resolved = false;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!resolved) {
            resolveModel(itemStack);
        }
        model.ifPresent((bakedModel) -> {
            if (renderLayerParent.getModel() instanceof HumanoidModel<?>) {
                var humanoidModel = (HumanoidModel<LivingEntity>) renderLayerParent.getModel();
                humanoidModel.setupAnim(slotContext.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                humanoidModel.prepareMobModel(slotContext.entity(), limbSwing, limbSwingAmount, partialTicks);

                poseStack.pushPose();
                humanoidModel.body.translateAndRotate(poseStack);
                //Negative X is right, Negative Z is Forward
                //Scale by 1/16th, we are now dealing with units of pixels
                poseStack.translate(-4 * .0625f, 11 * .0625f, 0);
                poseStack.mulPose(Vector3f.YP.rotation(Mth.PI * .5f));
                poseStack.mulPose(Vector3f.ZP.rotation(Mth.PI));
                poseStack.scale(.4f, .4f, .4f);
                itemRenderer.render(itemStack, ItemTransforms.TransformType.FIXED, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);
                poseStack.popPose();
            }
        });

    }

    private void resolveModel(ItemStack itemStack) {
        IronsSpellbooks.LOGGER.debug("SpellBookCurioRenderer.resolveModel {}: {}", itemStack.getItem().toString(), ForgeRegistries.ITEMS.getKey(itemStack.getItem()));
        var manager = itemRenderer.getItemModelShaper().getModelManager();
        ResourceLocation modelLocation = getCurioModelLocation(itemStack.getItem());
        if (modelLocation != null) {
            var bakedModel = manager.getModel(modelLocation);
            if (bakedModel != manager.getMissingModel()) {
                this.model = Optional.of(bakedModel);
            }
        }
        resolved = true;
    }

    @Nullable
    public static ResourceLocation getCurioModelLocation(Item item) {
        ResourceLocation modelLocation = ForgeRegistries.ITEMS.getKey(item);
        return modelLocation == null ? null : new ResourceLocation(modelLocation.getNamespace(), String.format("item/%s_curio", modelLocation.getPath()));
    }
}
