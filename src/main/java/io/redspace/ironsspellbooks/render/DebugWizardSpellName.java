package io.redspace.ironsspellbooks.render;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizard;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class DebugWizardSpellName extends GeoRenderLayer<AbstractSpellCastingMob> {
    Font font;

    public DebugWizardSpellName(GeoEntityRenderer entityRendererIn) {
        super(entityRendererIn);
        this.font = Minecraft.getInstance().font;
    }

    //TODO: 1.19.4 port
//    @Override
//    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob abstractSpellCastingMob, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        if (abstractSpellCastingMob instanceof DebugWizard debugWizard) {
//            var pDisplayName = debugWizard.getSpellInfo();
//            if (pDisplayName != null) {
//                boolean flag = !debugWizard.isDiscrete();
//                float f = debugWizard.getBbHeight() + 0.5F;
//                int i = 0;
//                matrixStackIn.pushPose();
//                matrixStackIn.translate(0.0D, (double) f, 0.0D);
//
//                //matrixStackIn.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
//                matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
//                Matrix4f matrix4f = matrixStackIn.last().pose();
//                float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
//                int j = (int) (f1 * 255.0F) << 24;
//
//                float f2 = (float) (-font.width(pDisplayName) / 2);
//                font.drawInBatch(pDisplayName, f2, (float) i, 553648127, false, matrix4f, bufferIn, flag, j, packedLightIn);
//                if (flag) {
//                    font.drawInBatch(pDisplayName, f2, (float) i, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
//                }
//            }
//            matrixStackIn.popPose();
//        }
//    }
}