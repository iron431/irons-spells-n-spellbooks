package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

public class DeadKingEmissiveLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    public static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_glowing.png");
    public static final ResourceLocation TEXTURE_ENRAGED = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_enraged_glowing.png");

    public DeadKingEmissiveLayer(GeoEntityRenderer renderer) {
        super(renderer);
    }

    public static ResourceLocation currentTexture(AbstractSpellCastingMob entity) {
        return entity instanceof DeadKingBoss boss && boss.isPhase(DeadKingBoss.Phases.FinalPhase) ? TEXTURE_ENRAGED : TEXTURE_NORMAL;
    }

    public static ResourceLocation currentModel(AbstractSpellCastingMob deadKingBoss) {
        return DeadKingModel.MODEL;
    }

    public static RenderType renderType(ResourceLocation resourceLocation) {
        return RenderType.energySwirl(resourceLocation, 0, 0);
    }


    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityLivingBaseIn instanceof DeadKingCorpseEntity)
            return;
        var model = getEntityModel().getModel(currentModel(entityLivingBaseIn));

        matrixStackIn.pushPose();
        float scale = 1 / (1.3f);
        matrixStackIn.scale(scale, scale, scale);
        var renderType = renderType(currentTexture(entityLivingBaseIn));
        VertexConsumer vertexconsumer = bufferIn.getBuffer(renderType);
        this.getRenderer().render(
                model,
                entityLivingBaseIn, partialTicks, renderType, matrixStackIn, bufferIn,
                vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f
        );
        matrixStackIn.popPose();
    }
}
