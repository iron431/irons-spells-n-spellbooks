package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class AngelWingsLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation(TestMod.MODID, "textures/entity/angel_wings.png");
    private final AngelWingsModel<T> angelWingsModel;

    public AngelWingsLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
        this.angelWingsModel = new AngelWingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AngelWingsModel.ANGEL_WINGS_LAYER));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (shouldRender(pLivingEntity)) {
            ResourceLocation resourcelocation;
            if (pLivingEntity instanceof AbstractClientPlayer) {
                AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer) pLivingEntity;
                if (abstractclientplayer.isElytraLoaded() && abstractclientplayer.getElytraTextureLocation() != null) {
                    resourcelocation = abstractclientplayer.getElytraTextureLocation();
                } else if (abstractclientplayer.isCapeLoaded() && abstractclientplayer.getCloakTextureLocation() != null && abstractclientplayer.isModelPartShown(PlayerModelPart.CAPE)) {
                    resourcelocation = abstractclientplayer.getCloakTextureLocation();
                } else {
                    resourcelocation = getAngelWingsTexture(pLivingEntity);
                }
            } else {
                resourcelocation = getAngelWingsTexture(pLivingEntity);
            }

            pMatrixStack.pushPose();
            pMatrixStack.translate(0.0D, 0.0D, 0.125D);
            this.getParentModel().copyPropertiesTo(this.angelWingsModel);
            this.angelWingsModel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(pBuffer, RenderType.armorCutoutNoCull(resourcelocation), false, false);
            this.angelWingsModel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            pMatrixStack.popPose();
        }
    }

    public boolean shouldRender(T entity) {
        TestMod.LOGGER.debug("AngelWingsLayer.shouldRender {} {}", entity.getName().getString(), entity.getActiveEffects().stream().map(x -> x.getEffect().getDisplayName().getString()).collect(Collectors.toSet()));
        return entity.hasEffect(MobEffectRegistry.ANGEL_WINGS.get()) || ClientMagicData.getPlayerSyncedData(entity.getId()).getHasAngelWings();
    }

    public ResourceLocation getAngelWingsTexture(T entity) {
        return WINGS_LOCATION;
    }
}
