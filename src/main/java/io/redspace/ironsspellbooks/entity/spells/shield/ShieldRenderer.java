package io.redspace.ironsspellbooks.entity.spells.shield;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class ShieldRenderer extends EntityRenderer<ShieldEntity> implements RenderLayerParent<ShieldEntity, ShieldModel> {

    public static ResourceLocation SPECTRAL_OVERLAY_TEXTURE = IronsSpellbooks.id("textures/entity/shield/shield_overlay.png");
    private static ResourceLocation SIGIL_TEXTURE = IronsSpellbooks.id("textures/block/scroll_forge_sigil.png");
    //private static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("textures/entity/creeper/creeper_armor.png");
    private final ShieldModel model;
    protected final List<RenderLayer<ShieldEntity, ShieldModel>> layers = new ArrayList<>();

    public ShieldRenderer(Context context) {
        super(context);
        this.model = new ShieldModel(context.bakeLayer(ShieldModel.LAYER_LOCATION));
        layers.add(new ShieldTrimLayer(this, context));
    }

    @Override
    public void render(ShieldEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        var offset = getEnergySwirlOffset(entity, partialTicks);
        VertexConsumer consumer = bufferSource.getBuffer(MagicArrowRenderer.CustomRenderType.magicSwirl(getTextureLocation(entity), offset.x, offset.y));

        float width = entity.width * .65f;
        poseStack.scale(width, width, width);
        RenderSystem.disableBlend();
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.65F, 0.65F, 0.65F, 1.0F);


        for (RenderLayer<ShieldEntity, ShieldModel> layer : layers) {
            layer.render(poseStack, bufferSource, light, entity, 0f, 0f, 0f, 0f, 0f, 0f);
        }
        
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private static float shittyNoise(float f) {
        return (float) (Math.sin(f / 4) + 2 * Math.sin(f / 3) + 3 * Math.sin(f / 2) + 4 * Math.sin(f)) * .25f;
    }

    public static Vec2 getEnergySwirlOffset(ShieldEntity entity, float partialTicks, int offset) {
        float f = (entity.tickCount + partialTicks) * .02f;
        return new Vec2(shittyNoise(1.2f * f + offset), shittyNoise(f + 456 + offset));
    }

    public static Vec2 getEnergySwirlOffset(ShieldEntity entity, float partialTicks) {
        return getEnergySwirlOffset(entity, partialTicks, 0);
    }

    @Override
    public ShieldModel getModel() {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureLocation(ShieldEntity entity) {
        return SPECTRAL_OVERLAY_TEXTURE;
    }

}