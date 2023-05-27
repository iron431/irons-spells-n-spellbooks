package io.redspace.ironsspellbooks.entity.spells.electrocute;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.BiFunction;

@OnlyIn(Dist.CLIENT)
public class ElectrocuteRenderer extends EntityRenderer<ElectrocuteProjectile> {
    private static ResourceLocation TEXTURES[] = {
            IronsSpellbooks.id("textures/entity/electric_beams/beam_1.png"),
            IronsSpellbooks.id("textures/entity/electric_beams/beam_2.png"),
            IronsSpellbooks.id("textures/entity/electric_beams/beam_3.png"),
            IronsSpellbooks.id("textures/entity/electric_beams/beam_4.png")
    };
    private static ResourceLocation SOLID = IronsSpellbooks.id("textures/entity/electric_beams/solid.png");

    public ElectrocuteRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ElectrocuteProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity.getOwner() == null)
            return;
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

//        RenderSystem.disableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);

        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        poseStack.translate(0, entity.getEyeHeight() * .5f, 0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getOwner().getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getOwner().getXRot()));

        if (entity.getAge() % 2 == 0 && !Minecraft.getInstance().isPaused())
            entity.generateLightningBeams();
        List<Vec3> segments = entity.getBeamCache();
        //irons_spellbooks.LOGGER.debug("ElectrocuteRenderer.segments.length: {}",segments.size());

        //was entityEmissive. Doesnt exist in 1.18?
        VertexConsumer consumer = bufferSource.getBuffer(EntityEmissive.entityTranslucentEmissive(SOLID));
        float width = .25f;
        float height = width;
        Vec3 start = Vec3.ZERO;//entity.getOwner().getEyePosition().add(entity.getForward().normalize().scale(.15f));
        for (int i = 0; i < segments.size() - 1; i += 2) {
            var from = segments.get(i).add(start);
            var to = segments.get(i + 1).add(start);
            drawHull(from, to, width, height, pose, consumer, 0, 156, 255, 30);
            drawHull(from, to, width * .55f, height * .55f, pose, consumer, 0, 226, 255, 30);

        }

        consumer = bufferSource.getBuffer(RenderType.energySwirl(getTextureLocation(entity),0,0));
        for (int i = 0; i < segments.size() - 1; i += 2) {
            var from = segments.get(i).add(start);
            var to = segments.get(i + 1).add(start);
            drawHull(from, to, width * .2f, height * .2f, pose, consumer, 255, 255, 255, 255);

        }


//        drawSegment(new Vec3(0, 0, 0), new Vec3(-1, 1, 3), 1, pose, consumer, entity, bufferSource, light);
//        drawSegment(new Vec3(-1, 1, 3), new Vec3(1, 1, 5), 1, pose, consumer, entity, bufferSource, light);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        //Bottom
        drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Top
        drawQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Left
        drawQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
        //Right
        drawQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
    }

    private void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        //to = new Vec3(1, 0, 10);
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        //float height = (float) (Math.random() * .25f) + .25f;
        consumer.vertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();

    }

    @Override
    public ResourceLocation getTextureLocation(ElectrocuteProjectile p_115264_) {
        //return TEXTURES[(int) (Math.random() * 4)];
        return SOLID;
    }

    private class EntityEmissive extends RenderType{

        public EntityEmissive(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize((p_234333_, p_234334_) -> {
            RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_234333_, false, false)).setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setOverlayState(OVERLAY).createCompositeState(p_234334_);
            return create("entity_translucent_emissive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
        });

        public static RenderType entityTranslucentEmissive(ResourceLocation pLocation) {
            return ENTITY_TRANSLUCENT_EMISSIVE.apply(pLocation, true);
        }
    }
}