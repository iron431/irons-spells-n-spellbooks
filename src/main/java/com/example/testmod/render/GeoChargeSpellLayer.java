package com.example.testmod.render;

import com.example.testmod.entity.lightning_lance.LightningLanceRenderer;
import com.example.testmod.entity.mobs.AbstractSpellCastingMob;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class GeoChargeSpellLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    public GeoChargeSpellLayer(IGeoRenderer entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, AbstractSpellCastingMob entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
        //TestMod.LOGGER.debug("GeoChargeSpellLayer.render: {}", syncedSpellData);
        var spell = syncedSpellData.getCastingSpellType();
        if (spell == SpellType.LIGHTNING_LANCE_SPELL) {
            //var model = this.getEntityModel().getModel(AbstractSpellCastingMob.modelResource);
            //var model = entityRenderer.getGeoModelProvider().getModel(AbstractSpellCastingMob.modelResource);
            //var model = this.getRenderer().getGeoModelProvider().getModel(AbstractSpellCastingMob.modelResource);
            var model = this.getRenderer().getGeoModelProvider().getModel(this.getEntityModel().getModelResource(entity));

            Optional<GeoBone> bone;
            if (entity.isLeftHanded()) {
                bone = model.getBone(DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT);
            } else {
                bone = model.getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT);
            }

            if (bone.isPresent()) {
                poseStack.pushPose();

                //Cast completion
                float castCompletion = Utils.smoothstep(.35f, 1, ClientMagicData.getCastCompletionPercent());
                poseStack.scale(castCompletion, castCompletion, castCompletion);

                //Lance position
                RenderUtils.prepMatrixForBone(poseStack, bone.get());
                RenderUtils.translateAndRotateMatrixForBone(poseStack, bone.get());
                //RenderUtils.translateToPivotPoint(poseStack, bone.get().childCubes.get(0));
                LightningLanceRenderer.renderModel(poseStack, bufferSource, entity.tickCount);

                poseStack.popPose();
            }

        } else if (spell == SpellType.MAGIC_ARROW_SPELL) {
//            //TODO: arm based on handedness
//            var arm = HumanoidArm.RIGHT;
//            this.getParentModel().translateToHand(arm, poseStack);
//            boolean flag = arm == HumanoidArm.LEFT;
//            poseStack.translate((double) ((float) (flag ? -1 : 1) / 32.0F) + .125, .5, 0);
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//            float castCompletion = Utils.smoothstep(.65f, 1, ClientMagicData.getCastCompletionPercent());
//            poseStack.scale(castCompletion, castCompletion, castCompletion);
//            MagicArrowRenderer.renderModel(poseStack, bufferSource);
        }
    }
}

//		RenderUtils.prepMatrixForBone(poseStack, bone);
//        RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);
