package io.redspace.ironsspellbooks.entity.spells.summoned_weapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.function.Supplier;

public class SummonedSwordRenderer extends GeoEntityRenderer<SummonedWeaponEntity> {
    public SummonedSwordRenderer(EntityRendererProvider.Context renderManager, Supplier<GeoModel<SummonedWeaponEntity>> model) {
        super(renderManager, model.get());
    }

    @Override
    public void preRender(PoseStack poseStack, SummonedWeaponEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.translate(0, animatable.getBbHeight() * .5f, 0);
    }

    @Override
    public @Nullable RenderType getRenderType(SummonedWeaponEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderHelper.CustomerRenderType.magic(texture);
    }

    @Override
    public Color getRenderColor(SummonedWeaponEntity animatable, float partialTick, int packedLight) {
        return Color.LIGHT_GRAY;
    }
}
