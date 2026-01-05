package io.redspace.ironsspellbooks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogPermissions;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.Color;

/**
 * Injection into the render point for armour on HumanoidModels (Players, Zombies, etc) to defer to GeckoLib item-armor rendering as applicable
 * <p>
 * Does nothing if GeckoLib has nothing to handle for the given arguments
 */
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
        TransmogPermissions permission;
        //todo: implement PatreonHandler
        {
            permission = TransmogPermissions.None;
            try {
                permission = PatreonHandler.getTransmogPermissions(player);
                if (permission == TransmogPermissions.None) {
                    return true;
                }
            } catch (NotImplementedException e) {

            }
        }

        ItemStack stack = entity.getItemBySlot(equipmentSlot);
        TransmogHolder transmogHolder = stack.get(ComponentRegistry.TRANSMOG);
        if (transmogHolder == null || !permission.canUse(transmogHolder)) {
            return true;
        }
        final GeoArmorRenderer<?> geckolibModel = transmogHolder.getArmorModel();

        renderLayer.getParentModel().copyPropertiesTo(baseModel);
        setPartVisibility(baseModel, equipmentSlot);
        //fixme: item is require to implement GeoItem. currently we hotswap for a known GeoItem. need dedicated ghost items or a workaround
        ItemStack transmogStack = new ItemStack(ItemRegistry.PYROMANCER_CHESTPLATE, stack.getCount(), stack.getComponentsPatch());

        geckolibModel.prepForRender(entity, transmogStack, equipmentSlot, baseModel, bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        baseModel.copyPropertiesTo((A) geckolibModel);
        geckolibModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, Color.WHITE.argbInt());

        return false;
    }
}