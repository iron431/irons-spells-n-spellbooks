package io.redspace.ironsspellbooks.entity.spells.spectral_hammer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SpectralHammerModel extends AnimatedGeoModel<SpectralHammer> {
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/spectral_hammer.geo.json");
    public static final ResourceLocation textureResource = ShieldRenderer.SPECTRAL_OVERLAY_TEXTURE;
    public static final ResourceLocation animationResource = new ResourceLocation(IronsSpellbooks.MODID, "animations/spectral_hammer.animation.json");

    @Override
    public ResourceLocation getModelLocation(SpectralHammer object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralHammer object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SpectralHammer animatable) {
        return animationResource;
    }
}
