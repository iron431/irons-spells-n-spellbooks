package io.redspace.ironsspellbooks.entity.spells.spectral_hammer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpectralHammerModel extends GeoModel<SpectralHammer> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/spectral_hammer.geo.json");
    public static final ResourceLocation textureResource = ShieldRenderer.SPECTRAL_OVERLAY_TEXTURE;
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/spectral_hammer.animation.json");

    @Override
    public ResourceLocation getModelResource(SpectralHammer object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(SpectralHammer object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(SpectralHammer animatable) {
        return animationResource;
    }
}
