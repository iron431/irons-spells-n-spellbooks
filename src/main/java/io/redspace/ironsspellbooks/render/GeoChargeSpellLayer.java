package io.redspace.ironsspellbooks.render;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.lightning_lance.LightningLanceRenderer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.PartNames;
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
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractSpellCastingMob entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);

        //irons_spellbooks.LOGGER.debug("GeoChargeSpellLayer.render: {}", syncedSpellData);
        var spell = syncedSpellData.getCastingSpellType();
        if (spell == SpellType.LIGHTNING_LANCE_SPELL) {
            var modelResource = entityRenderer.getGeoModelProvider().getModelResource(entity);
            var model = entityRenderer.getGeoModelProvider().getModel(modelResource);
            var bone = model.getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT).get();

            IronsSpellbooks.LOGGER.debug("GeoChargeSpellLayer pos:{}, {}, {} local:{}, {}, {} world:{}, {}, {} model:{}, {}, {} pivot:{}, {}, {}",
                    bone.getPosition().x, bone.getPosition().y, bone.getPosition().y,
                    bone.getLocalPosition().x, bone.getLocalPosition().y, bone.getLocalPosition().z,
                    bone.getWorldPosition().x, bone.getWorldPosition().y, bone.getWorldPosition().z,
                    bone.getModelPosition().x, bone.getModelPosition().y, bone.getModelPosition().z,
                    bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());

            poseStack.pushPose();

            //Cast completion
            //float castCompletion = Utils.smoothstep(.35f, 1, ClientMagicData.getCastCompletionPercent());
            //poseStack.scale(castCompletion, castCompletion, castCompletion);

            //Lance position
            //RenderUtils.translateMatrixToBone(poseStack, bone.get());
            RenderUtils.prepMatrixForBone(poseStack, bone);
            RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);
            //this.getRenderer().renderCube(bone.get().childCubes.get(0), poseStack, bufferSource.getBuffer(RenderType.eyes(LightningLanceRenderer.TEXTURES[0])),1, 1,1,1,1,1 );
            // RenderUtils.translateToPivotPint(poseStack, bone.childCubes.get(0));
            LightningLanceRenderer.renderModel(poseStack, bufferSource, entity.tickCount);

            poseStack.popPose();


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
