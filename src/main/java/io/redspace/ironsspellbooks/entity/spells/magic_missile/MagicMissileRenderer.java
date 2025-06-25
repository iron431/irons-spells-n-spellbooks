package io.redspace.ironsspellbooks.entity.spells.magic_missile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MagicMissileRenderer extends EntityRenderer<MagicMissileProjectile> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/magic_missile/magic_missile.png");
    private static final ResourceLocation FLARE = IronsSpellbooks.id("textures/entity/lens_flare.png");
    private final ModelPart body;

    public MagicMissileRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(FireballRenderer.MODEL_LAYER_LOCATION);
        this.body = modelpart.getChild("body");
    }

    @Override
    public void render(MagicMissileProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.scale(0.35f, 0.35f, 0.35f);

        VertexConsumer consumer = bufferSource.getBuffer(renderType(getTextureLocation(entity)));
        this.body.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderHelper.colorf(.8f, .8f, .8f));

        poseStack.popPose();

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        float f = entity.tickCount + partialTicks;
        float scale = 0.5f + Mth.sin(f) * .125f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTicks) * 15));
        consumer = bufferSource.getBuffer(RenderType.entityTranslucent(FLARE));
//        int unpackedlight = Math.max(LightTexture.block(light), LightTexture.sky(light));
//        int blowout = (int) Mth.lerp(unpackedlight / 15f, 0, 180);
        consumer.addVertex(poseMatrix, 0, -1, -1).setColor(255, 180, 255, 255).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, 0, 1, -1).setColor(255, 180, 255, 255).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, 0, 1, 1).setColor(255, 180, 255, 255).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, 0, -1, 1).setColor(255, 180, 255, 255).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public RenderType renderType(ResourceLocation TEXTURE) {
        return RenderType.energySwirl(TEXTURE, 0, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicMissileProjectile entity) {
        return TEXTURE;
    }

}