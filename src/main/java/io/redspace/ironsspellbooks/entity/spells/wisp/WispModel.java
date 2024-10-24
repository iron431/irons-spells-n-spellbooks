package io.redspace.ironsspellbooks.entity.spells.wisp;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WispModel extends GeoModel<WispEntity> {
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/wisp.geo.json");
    public static final ResourceLocation textureResource = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/wisp/wisp.png");
    public static final ResourceLocation animationResource = new ResourceLocation(IronsSpellbooks.MODID, "animations/wisp.animation.json");


    @Override
    public ResourceLocation getModelResource(WispEntity object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(WispEntity object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(WispEntity animatable) {
        return animationResource;
    }
}
