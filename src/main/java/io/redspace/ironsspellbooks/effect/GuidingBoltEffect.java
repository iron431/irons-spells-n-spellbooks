package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class GuidingBoltEffect extends MobEffect {
    public GuidingBoltEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return duration % 2 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        livingEntity.level.getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(Math.min(4 + pAmplifier, 10)), (projectile) -> projectile.getOwner() != livingEntity && !projectile.noPhysics).forEach((projectile) -> {
            Vec3 magnetization = livingEntity.getEyePosition().subtract(projectile.position()).normalize().scale(.25f + .075f).scale(2);
            projectile.setDeltaMovement(projectile.getDeltaMovement().add(magnetization));
        });
    }

}
