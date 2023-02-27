package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.registries.ItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class GlowingEyesLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends EyesLayer<T, M> {
    private static final RenderType EYES = RenderType.eyes(new ResourceLocation(TestMod.MODID, "textures/entity/purple_eyes.png"));

    public GlowingEyesLayer(RenderLayerParent pRenderer) {
        super(pRenderer);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }


    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        var eye = getEyeType(pLivingEntity);
        if (eye != EyeType.None) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.renderType());

            //pMatrixStack.translate(0, -eye.yOffset, -eye.forwardOffset);
            pMatrixStack.scale(eye.scale, eye.scale, eye.scale);
            this.getParentModel().renderToBuffer(pMatrixStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, eye.r, eye.g, eye.b, 1.0F);

//            //hat is 1.125
//            float scale = 1.1275f;
//            //repos anchor point
//            pMatrixStack.translate(0, 1 / 32f, 0);
//            pMatrixStack.scale(scale, scale, scale);
//            this.getParentModel().renderToBuffer(pMatrixStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, eye.r, eye.g, eye.b, 1.0F);

        }
    }

    public EyeType getEyeType(LivingEntity entity) {
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.ABYSSAL_SHROUD))
            return EyeType.Abyssal;
        else if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.SHADOWWALKER_HELMET.get()))
            return EyeType.Ender_Armor;
        else return EyeType.None;
    }

    private enum EyeType {
        None(0, 0, 0, 0),
        Abyssal(1f, 1f, 1f, 1f),
        Ender_Armor(.816f, 0f, 1f, 1.15f);

        public final float r, g, b, scale;

        EyeType(float r, float g, float b, float scale) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
        }
    }
}
