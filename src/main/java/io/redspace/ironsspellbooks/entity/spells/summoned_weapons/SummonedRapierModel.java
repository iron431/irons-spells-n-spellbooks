package io.redspace.ironsspellbooks.entity.spells.summoned_weapons;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SummonedRapierModel extends GeoModel<SummonedWeaponEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/summoned_weapons/summoned_rapier.png");
    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/summoned_rapier.geo.json");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(IronsSpellbooks.MODID, "animations/summoned_weapon_animations.json");

    @Override
    public ResourceLocation getModelResource(SummonedWeaponEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SummonedWeaponEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SummonedWeaponEntity animatable) {
        return ANIMATIONS;
    }
}
