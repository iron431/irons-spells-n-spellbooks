package io.redspace.ironsspellbooks.entity.spells.spectral_hammer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.render.GeoLivingEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SpectralHammerRenderer extends GeoLivingEntityRenderer<SpectralHammer> {
    public SpectralHammerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SpectralHammerModel());
        this.shadowRadius = 0.3f;
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralHammer animatable) {
        return SpectralHammerModel.textureResource;
    }

    @Override
    public void preRender(PoseStack poseStack, SpectralHammer animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.scale(2, 2, 2);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(SpectralHammer animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        Vec2 vec2 = getEnergySwirlOffset(animatable, partialTick);
        return RenderType.energySwirl(texture, vec2.x, vec2.y);
    }

    private static float shittyNoise(float f) {
        return (float) (Math.sin(f / 4) + 2 * Math.sin(f / 3) + 3 * Math.sin(f / 2) + 4 * Math.sin(f)) * .25f;
    }

    public static Vec2 getEnergySwirlOffset(SpectralHammer entity, float partialTicks, int offset) {
        float f = (entity.tickCount + partialTicks) * .02f;
        return new Vec2(shittyNoise(1.2f * f + offset), shittyNoise(f + 456 + offset));
    }

    public static Vec2 getEnergySwirlOffset(SpectralHammer entity, float partialTicks) {
        return getEnergySwirlOffset(entity, partialTicks, 0);
    }

}
