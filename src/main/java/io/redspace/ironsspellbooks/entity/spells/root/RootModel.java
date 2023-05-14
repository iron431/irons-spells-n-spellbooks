package io.redspace.ironsspellbooks.entity.spells.root;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RootModel extends AnimatedGeoModel<RootEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/root.png");
    private static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/root.geo.json");
    public static final ResourceLocation ANIMS = new ResourceLocation(IronsSpellbooks.MODID, "animations/root_animations.json");

    public RootModel() {
    }

    @Override
    public ResourceLocation getTextureResource(RootEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(RootEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(RootEntity animatable) {
        return ANIMS;
    }
}