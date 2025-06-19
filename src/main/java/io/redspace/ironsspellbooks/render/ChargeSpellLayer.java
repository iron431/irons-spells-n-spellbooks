package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.fire_arrow.FireArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceRenderer;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrowRenderer;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.DefaultBipedBoneIdents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

public class ChargeSpellLayer {
    public static class Vanilla<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

        public Vanilla(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
            if (!syncedSpellData.isCasting()) {
                return;
            }
            var spellId = syncedSpellData.getCastingSpellId();
            poseStack.pushPose();
            this.getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
            handleRender(poseStack, bufferSource, pPackedLight, entity, spellId, false);
            poseStack.popPose();
        }
    }

    private static <T extends LivingEntity> void handleRender(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, String spellId, boolean offhand) {
        if (spellId.equals(SpellRegistry.LIGHTNING_LANCE_SPELL.get().getSpellId())) {
            poseStack.translate((double) ((float) (offhand ? -1 : 1) / 32.0F) - .125, .5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            LightningLanceRenderer.renderModel(poseStack, bufferSource, entity.tickCount);
        } else if (spellId.equals(SpellRegistry.MAGIC_ARROW_SPELL.get().getSpellId())) {
            poseStack.translate(((float) (offhand ? -1 : 1) / 32.0F), .5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            MagicArrowRenderer.renderModel(poseStack, bufferSource);
        } else if (spellId.equals(SpellRegistry.POISON_ARROW_SPELL.get().getSpellId())) {
            poseStack.translate(((float) (offhand ? -1 : 1) / 32.0F), 1, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            PoisonArrowRenderer.renderModel(poseStack, bufferSource, pPackedLight);
        } else if (spellId.equals(SpellRegistry.FIRE_ARROW_SPELL.get().getSpellId())) {
            poseStack.translate(((float) (offhand ? -1 : 1) / 32.0F), .5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            FireArrowRenderer.renderModel(poseStack, bufferSource);
        }
    }

    public static class Geo extends GeoRenderLayer<AbstractSpellCastingMob> {
        public Geo(GeoEntityRenderer<AbstractSpellCastingMob> entityRenderer) {
            super(entityRenderer);
        }

        @Override
        public void render(PoseStack poseStack, AbstractSpellCastingMob entity, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
            var spellId = syncedSpellData.getCastingSpellId();
            var boneOpt = bakedModel.getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT);
            if (boneOpt.isPresent()) {
                var bone = boneOpt.get();
                poseStack.pushPose();
                RenderUtil.translateMatrixToBone(poseStack, bone);
                RenderUtil.rotateMatrixAroundBone(poseStack, bone);
                handleRender(poseStack, bufferSource, packedLight, entity, spellId, false);
                poseStack.popPose();
            }
        }
    }
}
