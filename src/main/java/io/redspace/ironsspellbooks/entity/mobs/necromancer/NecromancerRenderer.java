package io.redspace.ironsspellbooks.entity.mobs.necromancer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class NecromancerRenderer extends AbstractSpellCastingMobRenderer {

    public NecromancerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NecromancerModel());
    }

    @Override
    public void render(GeoModel model, AbstractSpellCastingMob animatable, float partialTick, RenderType type, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //RenderSystem.disableCull();
        //type = RenderType.entityCutoutNoCull(modelProvider.getTextureResource(animatable));
        //buffer = bufferSource.getBuffer(type);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(modelProvider.getTextureResource(animatable)));


        super.render(model, animatable, partialTick, type, poseStack, bufferSource, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
