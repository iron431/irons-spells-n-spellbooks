package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.entity.spells.SpinAttackModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class GeoSpinAttackLayer extends GeoRenderLayer<AbstractSpellCastingMob> {
    public GeoSpinAttackLayer(GeoRenderer<AbstractSpellCastingMob> entityRendererIn) {
        super(entityRendererIn);
    }

    private GeoModel<AbstractSpellCastingMob> modelProvider;

    public GeoSpinAttackLayer(AbstractSpellCastingMobRenderer entityRendererIn) {
        super(entityRendererIn);
        modelProvider = new SpinAttackModel();
    }

    @Override
    public void render(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        //TODO: 1.20 port
        if (animatable.isAutoSpinAttack() && false ) {
            for (int i = 0; i < 3; ++i) {
                poseStack.pushPose();
                float f = animatable.tickCount * (float) (-(45 + i * 5));
                poseStack.mulPose(Axis.YP.rotationDegrees(f));
                float f1 = 0.75F * (float) i;
                poseStack.scale(f1, f1, f1);
                poseStack.translate(0.0D, (double) (-0.2F + 0.6F * (float) i), 0.0D);
                getRenderer().actuallyRender(poseStack, animatable, modelProvider.getBakedModel(modelProvider.getModelResource(animatable)), RenderType.entityCutoutNoCull(modelProvider.getTextureResource(animatable)), bufferSource, buffer, true, partialTick, LightTexture.FULL_BRIGHT, packedOverlay, 1, 1, 1, 1);
                poseStack.popPose();
            }
        }
    }
}