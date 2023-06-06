package io.redspace.ironsspellbooks.entity.spells.magma_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrbRenderer;
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

public class MagmaBallRenderer extends EntityRenderer<FireBomb> {

    private static ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/fireball/magma.png");
    private static ResourceLocation SWIRL_TEXTURES[] = {
            IronsSpellbooks.id("textures/entity/fireball/swirl_0.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_1.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_2.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_3.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_4.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_5.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_6.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_7.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_8.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_9.png"),
            IronsSpellbooks.id("textures/entity/fireball/swirl_10.png")
    };

    private final ModelPart orb;
    private final ModelPart swirl;

    public MagmaBallRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(AcidOrbRenderer.MODEL_LAYER_LOCATION);
        this.orb = modelpart.getChild("orb");
        this.swirl = modelpart.getChild("swirl");
    }

    @Override
    public void render(FireBomb entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBoundingBox().getYsize() * .5f, 0);

        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        float f = entity.tickCount + partialTicks;
        float swirlX = Mth.cos(.08f * f) * 130;
        float swirlY = Mth.sin(.08f * f) * 130;
        float swirlZ = Mth.cos(.08f * f + 5464) * 130;
        poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
        poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.orb.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.mulPose(Axis.XP.rotationDegrees(swirlX));
        poseStack.mulPose(Axis.YP.rotationDegrees(swirlY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ));
        consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getSwirlTextureLocation(entity)));
        poseStack.scale(1.15f, 1.15f, 1.15f);
        this.swirl.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);


        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBomb entity) {
        return TEXTURE;
    }

    private ResourceLocation getSwirlTextureLocation(FireBomb entity) {
        int frame = (entity.tickCount) % SWIRL_TEXTURES.length;
        return SWIRL_TEXTURES[frame];
    }
}