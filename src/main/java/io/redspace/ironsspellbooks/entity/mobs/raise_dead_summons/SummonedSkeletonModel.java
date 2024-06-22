package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SummonedSkeletonModel extends GeoModel<SummonedSkeleton> {
    //public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/summoned_skeleton.png");

    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/skeleton_mob.geo.json");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(IronsSpellbooks.MODID, "animations/casting_animations.json");


    @Override
    public ResourceLocation getTextureResource(SummonedSkeleton object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(SummonedSkeleton object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/skeleton_mob.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(SummonedSkeleton animatable) {
        return ANIMATIONS;
    }

}