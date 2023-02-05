package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.util.CapeProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ArmorCapeLayer extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {
    public ArmorCapeLayer(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> pRenderer) {
        super(pRenderer);
        this.capeModel = new ArmorCapeModel(Minecraft.getInstance().getEntityModels().bakeLayer(ArmorCapeModel.ARMOR_CAPE_LAYER));
    }

    private final ArmorCapeModel capeModel;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    public float oBob;
    public float bob;
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, LivingEntity entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        TestMod.LOGGER.debug("rendering Cape");
        TestMod.LOGGER.debug("{} {} {} {} {} {}",xCloakO,
        yCloakO,
        zCloakO,
        xCloak,
        yCloak,
        zCloak);
        ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof CapeProvider capeItem || true) {
            moveCloak(entity);

            pMatrixStack.pushPose();
            pMatrixStack.translate(0.0D, 0.0D, 0.125D);
            double d0 = Mth.lerp((double) pPartialTicks, this.xCloakO, this.xCloak) - Mth.lerp((double) pPartialTicks, entity.xo, entity.getX());
            double d1 = Mth.lerp((double) pPartialTicks, this.yCloakO, this.yCloak) - Mth.lerp((double) pPartialTicks, entity.yo, entity.getY());
            double d2 = Mth.lerp((double) pPartialTicks, this.zCloakO, this.zCloak) - Mth.lerp((double) pPartialTicks, entity.zo, entity.getZ());
            float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
            double d3 = (double) Mth.sin(f * ((float) Math.PI / 180F));
            double d4 = (double) (-Mth.cos(f * ((float) Math.PI / 180F)));
            float f1 = (float) d1 * 10.0F;
            f1 = Mth.clamp(f1, -6.0F, 32.0F);
            float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
            f2 = Mth.clamp(f2, 0.0F, 150.0F);
            float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
            f3 = Mth.clamp(f3, -20.0F, 20.0F);
            if (f2 < 0.0F) {
                f2 = 0.0F;
            }
            this.oBob = this.bob;
            float b;
            if (entity.isOnGround() && !entity.isDeadOrDying()) {
                b = (float)Math.min(0.1D, entity.getDeltaMovement().horizontalDistance());
            } else {
                b = 0.0F;
            }

            this.bob += (b - this.bob) * 0.4F;
            float f4 = Mth.lerp(pPartialTicks, oBob, bob);
            f1 += Mth.sin(Mth.lerp(pPartialTicks, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * f4;
            if (entity.isCrouching()) {
                f1 += 25.0F;
            }

            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f3 / 2.0F));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - f3 / 2.0F));
            ResourceLocation TEMP_CLOCK_RESOURCELOCATION = new ResourceLocation("textures/entity/cape/minecon_2011_cape.png");
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(TEMP_CLOCK_RESOURCELOCATION));
            //this.getParentModel().renderCloak(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
            capeModel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            pMatrixStack.popPose();
        }

    }

    private void moveCloak(LivingEntity livingEntity) {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d0 = livingEntity.getX() - this.xCloak;
        double d1 = livingEntity.getY() - this.yCloak;
        double d2 = livingEntity.getZ() - this.zCloak;
        double d3 = 10.0D;
        if (d0 > 10.0D) {
            this.xCloak = livingEntity.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 > 10.0D) {
            this.zCloak = livingEntity.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 > 10.0D) {
            this.yCloak = livingEntity.getY();
            this.yCloakO = this.yCloak;
        }

        if (d0 < -10.0D) {
            this.xCloak = livingEntity.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 < -10.0D) {
            this.zCloak = livingEntity.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 < -10.0D) {
            this.yCloak = livingEntity.getY();
            this.yCloakO = this.yCloak;
        }

        this.xCloak += d0 * 0.25D;
        this.zCloak += d2 * 0.25D;
        this.yCloak += d1 * 0.25D;
    }
}
