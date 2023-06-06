package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class GeoSpinAttackLayer extends GeoRenderLayer<AbstractSpellCastingMob> {
    public GeoSpinAttackLayer(GeoRenderer<AbstractSpellCastingMob> entityRendererIn) {
        super(entityRendererIn);
    }

    //TODO: (1.19.4 port) i have no clue how to port this. complete geckolib overhaul here.
//    //private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/evasion.png");
//    private final GeoModel<AbstractSpellCastingMob> modelProvider;
//
//    public GeoSpinAttackLayer(GeoEntityRenderer entityRendererIn) {
//        super(entityRendererIn);
//        modelProvider = new SpinAttackModel();
//    }
//
//    @Override
//    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        if (entity.isAutoSpinAttack()) {
//            for (int i = 0; i < 3; ++i) {
//                matrixStackIn.pushPose();
//                float f = entity.tickCount * (float) (-(45 + i * 5));
//                matrixStackIn.mulPose(Axis.YP.rotationDegrees(f));
//                float f1 = 0.75F * (float) i;
//                matrixStackIn.scale(f1, f1, f1);
//                matrixStackIn.translate(0.0D, (double) (-0.2F + 0.6F * (float) i), 0.0D);
//                renderModel(modelProvider, modelProvider.getTextureResource(entity), matrixStackIn, bufferIn, packedLightIn, entity, partialTicks, 1, 1, 1);
//                matrixStackIn.popPose();
//            }
//        }
//    }
}