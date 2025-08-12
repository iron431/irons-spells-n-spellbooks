package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrbRenderer;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import static io.redspace.ironsspellbooks.entity.spells.magma_ball.MagmaBallRenderer.SWIRL_TEXTURES;

public class FireOrbRenderer extends EntityRenderer<FireOrbEntity> {
    protected final ModelPart fireball;
    protected final ModelPart outline;

    public FireOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(FireballRenderer.MODEL_LAYER_LOCATION);
        this.fireball = modelpart.getChild("body");
        this.outline = context.bakeLayer(AcidOrbRenderer.MODEL_LAYER_LOCATION).getChild("swirl");
    }

    @Override
    public void render(FireOrbEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        if (true/*tick > FireBossEntity.HALF_HEALTH_JUMP_TIMESTAMP && tick < FireBossEntity.HALF_HEALTH_CAST_TIMESTAMP*/) {
            poseStack.pushPose();
            float f = entity.tickCount + partialTick;
            poseStack.translate(0, entity.getBoundingBox().getYsize() + Mth.sin(f * .2f) * .1f, 0);
            int fuse = entity.getFuse();
            float scale = fuse > 0 ? Mth.lerp(entity.tickCount / (float) fuse, 1f, 1.5f) : 1f;
            poseStack.scale(scale, scale, scale);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(FireballRenderer.BASE_TEXTURE));

            float swirlX = Mth.cos(.08f * f) * 180;
            float swirlY = Mth.sin(.08f * f) * 180;
            float swirlZ = Mth.cos(.08f * f + 5464) * 180;
            poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
            poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));

            fireball.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
            if (fuse > 0) {
                poseStack.pushPose();
                float outlineScale = Mth.lerp(entity.tickCount / (float) fuse, 0.8f, 2.5f) + Mth.sin(f * Mth.lerp(entity.tickCount / (float) fuse, .5f, 1.25f)) * .3f;
                poseStack.scale(outlineScale, outlineScale, outlineScale);
                consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getSwirlTextureLocation(entity)));
                poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
                poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
                poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));

                outline.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    private static ResourceLocation getSwirlTextureLocation(Entity entity) {
        int frame = (entity.tickCount) % SWIRL_TEXTURES.length;
        return SWIRL_TEXTURES[frame];
    }

    @Override
    public ResourceLocation getTextureLocation(FireOrbEntity entity) {
        return FireballRenderer.BASE_TEXTURE;
    }
}
