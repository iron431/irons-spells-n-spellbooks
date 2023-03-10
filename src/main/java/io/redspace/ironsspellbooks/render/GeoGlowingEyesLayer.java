package io.redspace.ironsspellbooks.render;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

@OnlyIn(Dist.CLIENT)
public class GeoGlowingEyesLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    public GeoGlowingEyesLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation) {
        return GlowingEyesLayer.EYES;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, AbstractSpellCastingMob abstractSpellCastingMob, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var eye = GlowingEyesLayer.getEyeType(abstractSpellCastingMob);
        if (eye != GlowingEyesLayer.EyeType.None) {
            var packedLightInOverride = 15728640; //WTF is this?
            VertexConsumer vertexconsumer = multiBufferSource.getBuffer(GlowingEyesLayer.EYES);
            var modelResource = entityRenderer.getGeoModelProvider().getModelResource(abstractSpellCastingMob);
            var model = entityRenderer.getGeoModelProvider().getModel(modelResource);
            var bone = model.getBone(PartNames.HEAD).get();

            poseStack.pushPose();
            poseStack.scale(eye.scale*2, eye.scale*2, eye.scale*2);
            RenderUtils.translateMatrixToBone(poseStack, bone);

            this.getRenderer().render(model, abstractSpellCastingMob, partialTicks, GlowingEyesLayer.EYES, poseStack, multiBufferSource,
                    vertexconsumer, packedLightInOverride, OverlayTexture.NO_OVERLAY, eye.r, eye.g, eye.b, 1.0f);

            poseStack.popPose();


//            if (model.getBone("leftear").isPresent()) {
//                animatable.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL,
//                        model.getBone("leftear").get().getWorldPosition().x,
//                        model.getBone("leftear").get().getWorldPosition().y,
//                        model.getBone("leftear").get().getWorldPosition().z,

        }
    }
}