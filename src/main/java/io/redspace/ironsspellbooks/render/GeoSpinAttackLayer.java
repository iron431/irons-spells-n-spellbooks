package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.SpinAttackModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@OnlyIn(Dist.CLIENT)
public class GeoSpinAttackLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    //private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/evasion.png");
    private final GeoModelProvider<AbstractSpellCastingMob> modelProvider;

    public GeoSpinAttackLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
        modelProvider = new SpinAttackModel();
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isAutoSpinAttack()) {
            for (int i = 0; i < 3; ++i) {
                matrixStackIn.pushPose();
                float f = entity.tickCount * (float) (-(45 + i * 5));
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f));
                float f1 = 0.75F * (float) i;
                matrixStackIn.scale(f1, f1, f1);
                matrixStackIn.translate(0.0D, (double) (-0.2F + 0.6F * (float) i), 0.0D);
                renderModel(modelProvider, modelProvider.getTextureResource(entity), matrixStackIn, bufferIn, packedLightIn, entity, partialTicks, 1, 1, 1);
                matrixStackIn.popPose();
            }
        }
    }
}