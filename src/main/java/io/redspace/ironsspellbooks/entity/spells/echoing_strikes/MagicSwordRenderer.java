package io.redspace.ironsspellbooks.entity.spells.echoing_strikes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

import java.util.function.Supplier;

public class MagicSwordRenderer<T extends Entity & GeoEntity> extends GeoEntityRenderer<T> {
    public MagicSwordRenderer(EntityRendererProvider.Context renderManager, Supplier<GeoModel<T>> model) {
        super(renderManager, model.get());
    }

    @Override
    public void preRender(PoseStack poseStack, T entity, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTicks, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTicks, packedLight, packedOverlay, colour);
        poseStack.translate(0, entity.getBbHeight() * .5f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderHelper.CustomerRenderType.magic(texture);
    }

    @Override
    public Color getRenderColor(T animatable, float partialTick, int packedLight) {
        return Color.LIGHT_GRAY;
    }
}
