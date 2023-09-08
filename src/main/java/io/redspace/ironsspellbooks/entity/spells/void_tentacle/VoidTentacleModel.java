package io.redspace.ironsspellbooks.entity.spells.void_tentacle;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import java.util.List;

public class VoidTentacleModel extends AnimatedGeoModel<VoidTentacle> {
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/void_tentacle.geo.json");
    public static final ResourceLocation textureResource = IronsSpellbooks.id("textures/entity/void_tentacle/void_tentacle.png");
    public static final ResourceLocation animationResource = new ResourceLocation(IronsSpellbooks.MODID, "animations/void_tentacle_animations.json");

    @Override
    public ResourceLocation getModelResource(VoidTentacle object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(VoidTentacle mob) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(VoidTentacle animatable) {
        return animationResource;
    }

    @Override
    public void setCustomAnimations(VoidTentacle animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        float seed = (float) (animatable.getX() * animatable.getZ());
        float speed = (seed % 6) * .125f + 1f;
        float f = (float) (((animatable.getX() * animatable.getZ() % 173) + (animatable.tickCount + animationEvent.getPartialTick()) * speed) * .8f);
        List<IBone> bones = List.of(
                this.getAnimationProcessor().getBone("root"),
                this.getAnimationProcessor().getBone("segment_1"),
                this.getAnimationProcessor().getBone("segment_2"),
                this.getAnimationProcessor().getBone("segment_3"),
                this.getAnimationProcessor().getBone("segment_4")
        );

        int age = animatable.tickCount;
        float tween = Mth.clamp(age < 15 ? (age - 5) / 10f : age > 240 ? 1 - (age - 240) / 50f : 1f, 0, 1);
        for (int i = 0; i < bones.size(); i++) {
            var bone = bones.get(i);
            float f2 = 1.2f + i * .85f;
            bone.setRotationX(Mth.lerp(tween, bone.getRotationX(), f2 * Mth.DEG_TO_RAD * shittyNoise(f + 100 + i)));
            bone.setRotationZ(Mth.lerp(tween, bone.getRotationZ(), f2 * Mth.DEG_TO_RAD * shittyNoise(f + i)));
        }
        this.getAnimationProcessor().getBone("root").setRotationY(Mth.DEG_TO_RAD * (shittyNoise(f + 150) * .25f + animatable.tickCount + animationEvent.getPartialTick()));
    }

    private static float shittyNoise(float f) {
        return (Mth.sin(f * .1f) + Mth.sin(f * .25f) + 2 * Mth.sin(f * .333f) + 3 * Mth.sin(f * .5f) + 4 * Mth.sin(f)) * 1.5f;
    }
}