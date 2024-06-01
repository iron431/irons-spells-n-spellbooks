package io.redspace.ironsspellbooks.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GeoLivingEntityRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {
    public GeoLivingEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    @Override
    public boolean shouldShowName(T animatable) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(animatable);
        float f = animatable.isCrouching() ? 32.0F : 64.0F;
        return !(d0 >= (double) (f * f)) && animatable.isCustomNameVisible();
    }
}
