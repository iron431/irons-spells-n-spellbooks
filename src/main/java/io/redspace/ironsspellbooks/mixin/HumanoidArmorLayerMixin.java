package io.redspace.ironsspellbooks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogClientHandler;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.Color;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Shadow
    protected abstract void setPartVisibility(A baseModel, EquipmentSlot equipmentSlot);

    @WrapWithCondition(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"))
    public boolean irons_spellbooks$handleTransmog(HumanoidArmorLayer<T, M, A> renderLayer, PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, A baseModel,
                                                   float limbSwing, float limbSwingAmount, float partialTick, float lerpedTickCount, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        ItemStack stack = player.getInventory().getArmor(equipmentSlot.getIndex()); // circumvent "getItemBySlot", which is disabled during transmog rendering
        if (stack.isEmpty()) {
            return true;
        }
        if (!TransmogClientHandler.canUseTransmog(player, stack)) {
            return true;
        }
        TransmogHolder transmogHolder = TransmogHolder.get(stack);
        if (transmogHolder == null) {
            return true;
        }

        final GeoArmorRenderer<?> geckolibModel = transmogHolder.getArmorRenderer();

        renderLayer.getParentModel().copyPropertiesTo(baseModel);
        setPartVisibility(baseModel, equipmentSlot);
        //fixme: item is require to implement GeoItem. currently we hotswap for a known GeoItem. need dedicated ghost items or a workaround
        ItemStack transmogStack = new ItemStack(ItemRegistry.PYROMANCER_CHESTPLATE, stack.getCount(), stack.getComponentsPatch());

        geckolibModel.prepForRender(entity, transmogStack, equipmentSlot, baseModel, bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        baseModel.copyPropertiesTo(geckolibModel);
        geckolibModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, Color.WHITE.argbInt());
        if (transmogStack.has(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get())) {
            // if a cache ID is created, propagate the ID back to the original stack to preserve animation/data continuity. hopefully.
            // realistically, I don't think animations are even possible since we don't have access to custom controllers.
            stack.set(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), transmogStack.get(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get()));
        }

        return false;
    }
}