package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.item.armor.IArmorCapeProvider;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
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
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ArmorCapeLayer extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {
    private ModelPart cape;

    private Consumer<PoseStack> bodyTransformer;
    public static ModelLayerLocation ARMOR_CAPE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "armor_cape"), "main");

    public ArmorCapeLayer(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> pRenderer) {
        super(pRenderer);
        this.cape = Minecraft.getInstance().getEntityModels().bakeLayer(ARMOR_CAPE_LAYER).getChild("cape");
        this.bodyTransformer = (poseStack) -> this.getParentModel().body.translateAndRotate(poseStack);
    }

    public ArmorCapeLayer(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> pRenderer, Consumer<PoseStack> bodyTransformer) {
        this(pRenderer);
        this.bodyTransformer = bodyTransformer;
    }


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "cape",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE, 1.0F, 0.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }



    public void render(
            PoseStack pPoseStack,
            MultiBufferSource pBuffer,
            int pPackedLight,
            LivingEntity livingEntity,
            float pLimbSwing,
            float pLimbSwingAmount,
            float pPartialTicks,
            float pAgeInTicks,
            float pNetHeadYaw,
            float pHeadPitch
    ) {
        if (shouldRender(livingEntity)) {
            var texture = ((IArmorCapeProvider) livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem()).getCapeResourceLocation();
            var capeData= livingEntity.getData(DataAttachmentRegistry.CAPE_DATA);
            int lastTick = capeData.lastTick;
            if (lastTick != livingEntity.tickCount) {
                capeData.moveCloak(livingEntity);
                capeData.lastTick = livingEntity.tickCount;
            }
            pPoseStack.pushPose();
            pPoseStack.translate(0.0F, 0.0F, 0.125F);
            double d0 = Mth.lerp((double) pPartialTicks, capeData.xCloakO, capeData.xCloak) - Mth.lerp((double) pPartialTicks, livingEntity.xo, livingEntity.getX());
            double d1 = Mth.lerp((double) pPartialTicks, capeData.yCloakO, capeData.yCloak) - Mth.lerp((double) pPartialTicks, livingEntity.yo, livingEntity.getY());
            double d2 = Mth.lerp((double) pPartialTicks, capeData.zCloakO, capeData.zCloak) - Mth.lerp((double) pPartialTicks, livingEntity.zo, livingEntity.getZ());
            float f = Mth.rotLerp(pPartialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            double d3 = (double) Mth.sin(f * (float) (Math.PI / 180.0));
            double d4 = (double) (-Mth.cos(f * (float) (Math.PI / 180.0)));
            float f1 = (float) d1 * 10.0F;
            f1 = Mth.clamp(f1, -6.0F, 32.0F);
            float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
            f2 = Mth.clamp(f2, 0.0F, 150.0F);
            float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
            f3 = Mth.clamp(f3, -20.0F, 20.0F);
            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            float f4 = Mth.lerp(pPartialTicks, capeData.oBob, capeData.bob);
            f1 += Mth.sin(Mth.lerp(pPartialTicks, livingEntity.walkDistO, livingEntity.walkDist) * 6.0F) * 32.0F * f4;
            if (livingEntity.isCrouching()) {
                f1 += 25.0F;
                this.cape.z = 1.4F;
                this.cape.y = 1.85F;
            } else {
                this.cape.z = 0.0F;
                this.cape.y = 0.0F;
            }
            this.bodyTransformer.accept(pPoseStack);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(texture));
            this.cape.render(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
            pPoseStack.popPose();
        }
    }

    private boolean shouldRender(LivingEntity livingEntity) {
        ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        return !itemstack.is(Items.ELYTRA)
                && itemstack.getItem() instanceof IArmorCapeProvider
                && !hasPlayerCape(livingEntity)
                && !livingEntity.hasEffect(MobEffectRegistry.ANGEL_WINGS);
    }

    private boolean hasPlayerCape(LivingEntity livingEntity) {
        return livingEntity instanceof AbstractClientPlayer player && player.getSkin().capeTexture() != null;
    }
}
