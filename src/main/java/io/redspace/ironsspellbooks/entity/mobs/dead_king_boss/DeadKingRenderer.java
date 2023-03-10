package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class DeadKingRenderer extends AbstractSpellCastingMobRenderer {

    public DeadKingRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DeadKingModel());
    }

    @Override
    public void render(GeoModel model, AbstractSpellCastingMob animatable, float partialTick, RenderType type, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        RenderSystem.disableCull();
        poseStack.scale(1.3f, 1.3f, 1.3f);
        if (animatable instanceof DeadKingBoss king && king.isPhase(DeadKingBoss.Phases.FinalPhase)) {
            model.getBone(PartNames.LEFT_LEG).ifPresent((bone) -> bone.setHidden(true));
            model.getBone(PartNames.RIGHT_LEG).ifPresent((bone) -> bone.setHidden(true));
        } else {
            model.getBone(PartNames.LEFT_LEG).ifPresent((bone) -> bone.setHidden(false));
            model.getBone(PartNames.RIGHT_LEG).ifPresent((bone) -> bone.setHidden(false));
        }
        type = RenderType.entityTranslucentCull(modelProvider.getTextureResource(animatable));
        super.render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
