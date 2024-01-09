package io.redspace.ironsspellbooks.effect.guiding_bolt;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class GuidingBoltEffect extends MagicMobEffect {
    public GuidingBoltEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return duration % 2 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
//        livingEntity.level.getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(Math.min(4 + pAmplifier, 10)), (projectile) -> projectile.getOwner() != livingEntity && !projectile.noPhysics).forEach((projectile) -> {
//            Vec3 magnetization = livingEntity.getEyePosition().subtract(projectile.position()).normalize().scale(.25f + .075f).scale(2);
//            projectile.setDeltaMovement(projectile.getDeltaMovement().add(magnetization));
//        });
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        //IronsSpellbooks.LOGGER.debug("GuidingBoltEffect adding to tracked entities: {}", pLivingEntity);
        GuidingBoltManager.INSTANCE.startTracking(pLivingEntity);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        //IronsSpellbooks.LOGGER.debug("GuidingBoltEffect removing from tracked entities: {}", pLivingEntity);
        GuidingBoltManager.INSTANCE.stopTracking(pLivingEntity);
    }
}
