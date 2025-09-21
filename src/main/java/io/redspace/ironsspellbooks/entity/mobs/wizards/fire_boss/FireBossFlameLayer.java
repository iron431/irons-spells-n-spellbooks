package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

@OnlyIn(Dist.CLIENT)
public class FireBossFlameLayer extends GeoRenderLayer<AbstractSpellCastingMob> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/fire_boss/tyros_flame.png");

    public FireBossFlameLayer(GeoEntityRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void renderForBone(PoseStack poseStack, AbstractSpellCastingMob animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (bone.getName().equals(PartNames.HEAD) && animatable instanceof FireBossEntity fireBossEntity && fireBossEntity.isSoulMode()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));
            RenderUtils.translateToPivotPoint(poseStack, bone);
            poseStack.scale(1 / 2f, 1 / 2f, 1 / 2f);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
            Matrix4f poseMatrix = poseStack.last().pose();

            int anim = (animatable.tickCount / ticksPerFrame) % frameCount;
            float uvMin = anim / (float) frameCount;
            float uvMax = (anim + 1) / (float) frameCount;
            float halfsqrt2 = 0.7071f;
            for (int i = 0; i < 4; i++) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                consumer.vertex(poseMatrix, 0, 0, -halfsqrt2).color(255, 255, 255, 255).uv(0f, uvMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, 0, 1, -halfsqrt2).color(255, 255, 255, 255).uv(0f, uvMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, 0, 1, halfsqrt2).color(255, 255, 255, 255).uv(1f, uvMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
                consumer.vertex(poseMatrix, 0, 0, halfsqrt2).color(255, 255, 255, 255).uv(1f, uvMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
            }
            poseStack.popPose();
            //fixme: this is scary but it seems swapping the texture changes it for the base model's cubes rendered after this layer. revert it to what was passed in
            bufferSource.getBuffer(renderType);
        }
    }

    static int frameCount = 8;
    static int ticksPerFrame = 1;
}