package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrbRenderer;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
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

public class OminousFireOrbRenderer extends EntityRenderer<OminousFireOrbEntity> {
    protected final ModelPart fireball;
    protected final ModelPart outline;

    public static final ResourceLocation BASE_TEXTURE = IronsSpellbooks.id("textures/entity/fireball/fireball_core_soul.png");
    public static ResourceLocation[] SWIRL_TEXTURES = {
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_0.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_1.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_2.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_3.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_4.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_5.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_6.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_7.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_8.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_9.png"),
            IronsSpellbooks.id("textures/entity/fireball/soul_swirl_10.png")
    };

    public OminousFireOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(FireballRenderer.MODEL_LAYER_LOCATION);
        this.fireball = modelpart.getChild("body");
        this.outline = context.bakeLayer(AcidOrbRenderer.MODEL_LAYER_LOCATION).getChild("swirl");
    }

    @Override
    public void render(OminousFireOrbEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        float f = entity.tickCount + partialTick;

        int fuse = entity.getFuse();
        int chargeTime = entity.getChargeTime();

        float fuseProgress = entity.getFuseProgressTicks() + partialTick;
        float fusePercent = fuse > 0 ? Mth.clamp(fuseProgress / fuse, 0f, 1f) : 0f;
        float chargePercent = chargeTime > 0 ? Mth.clamp(f / chargeTime, 0, 1f) : 0f;

        poseStack.pushPose();
        float scale = fuse > 0 ? Mth.lerp(fusePercent, 1f, 1.85f) : 1f;
        poseStack.translate(0, 2 + Mth.sin(f * .2f) * .1f, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.scale(1.2f, 1.2f, 1.2f);
        float swirlX = Mth.cos(.08f * f) * 180;
        float swirlY = Mth.sin(.08f * f) * 180;
        float swirlZ = Mth.cos(.08f * f + 5464) * 180;
        poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
        poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));

        if (entity.getFuseProgressTicks() > 0) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(BASE_TEXTURE));
            fireball.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
        if (fuse > 0 || chargeTime > 0) {
            float percent = entity.tickCount >= chargeTime ? fusePercent : 1f - chargePercent;
            float intensity = Mth.lerp(percent, .5f, 1.25f);
            float outlineScale = Mth.lerp(percent, 0.8f, 2.5f) + Mth.sin(f * intensity) * .4f * intensity;
            poseStack.scale(outlineScale, outlineScale, outlineScale);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getSwirlTextureLocation(entity)));
            poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
            poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));

            outline.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
        poseStack.popPose();

    }

    private static ResourceLocation getSwirlTextureLocation(Entity entity) {
        int frame = (entity.tickCount) % SWIRL_TEXTURES.length;
        return SWIRL_TEXTURES[frame];
    }

    @Override
    public ResourceLocation getTextureLocation(OminousFireOrbEntity entity) {
        return BASE_TEXTURE;
    }
}
