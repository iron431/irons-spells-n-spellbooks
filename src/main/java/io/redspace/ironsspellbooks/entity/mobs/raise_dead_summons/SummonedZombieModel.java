package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SummonedZombieModel extends GeoModel<SummonedZombie> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID,"textures/entity/summoned_zombie.png");
    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/casting_animations.json");


    @Override
    public ResourceLocation getTextureResource(SummonedZombie object) {
        return TEXTURE;
    }
    @Override
    public ResourceLocation getModelResource(SummonedZombie object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(SummonedZombie animatable) {
        return ANIMATIONS;
    }

}