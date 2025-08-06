package io.redspace.ironsspellbooks.entity.spells.ice_block;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IceBlockModel extends GeoModel<IceBlockProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/ice_block.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/ice_block_projectile.geo.json");
    public static final ResourceLocation ANIMS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/ice_block_animations.json");


    public IceBlockModel() {
    }

    @Override
    public ResourceLocation getTextureResource(IceBlockProjectile object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(IceBlockProjectile object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(IceBlockProjectile animatable) {
        return ANIMS;
    }
}