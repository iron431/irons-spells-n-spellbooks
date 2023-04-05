package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@OnlyIn(Dist.CLIENT)
public class GeoHealTargetLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    public GeoHealTargetLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation) {
        return GlowingEyesLayer.EYES;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, AbstractSpellCastingMob abstractSpellCastingMob, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (HealTargetLayer.shouldRender(abstractSpellCastingMob)) {
            //Its upsidedown???
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
            poseStack.translate(0, -(abstractSpellCastingMob.getBbWidth() + abstractSpellCastingMob.getBbHeight()) / 2, 0);
            HealTargetLayer.renderTargetLayer(poseStack, multiBufferSource, abstractSpellCastingMob);
        }
    }
}