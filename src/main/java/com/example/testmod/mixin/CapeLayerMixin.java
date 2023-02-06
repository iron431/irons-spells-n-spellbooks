package com.example.testmod.mixin;

import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {

//   @Shadow
//   @Dynamic
//   VertexConsumer vertexConsumer;

    //    @Inject(method = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
//            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
//            , locals = LocalCapture.CAPTURE_FAILHARD)
//    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci,VertexConsumer vertexConsumer) {
////        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
////        if(itemstack.getItem() instanceof ArmorCapeProvider capeProvider){
////            cir.setReturnValue(capeProvider.getCapeResourceLocation());
////        }
//        vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(pLivingEntity.getCloakTextureLocation()));
//
//        TestMod.LOGGER.debug("Mixin: rendering cape !!");
//    }
//    @ModifyVariable(
//            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
//            at = @At(value = "HEAD"
//            ),
//            index = 0
//
//    )
//    private VertexConsumer mixin(){
//
//        return null;
//    }
//    @ModifyArg(method = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PlayerModel;renderCloak(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V")
//            , index = 1
//    )
//    private VertexConsumer mixin(VertexConsumer pBuffer) {
//        return null;
//    }

}
