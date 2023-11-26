package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class AirborneEffect extends MobEffect {
    public AirborneEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public static final float damage_per_amp = 0.5f;

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        if (!livingEntity.level.isClientSide) {
            if (livingEntity.horizontalCollision) {
                double d11 = livingEntity.getDeltaMovement().horizontalDistance();
                float f1 = (float) (d11 * 10.0D - 1.0D);
                //IronsSpellbooks.LOGGER.debug("AirborneEffect horizontalCollision: {}, {}", livingEntity.getDeltaMovement().horizontalDistance(),f1);
                if (f1 > 0.0F) {
                    livingEntity.playSound(SoundEvents.HOSTILE_BIG_FALL, 2.0F, 1.5F);
                livingEntity.hurt(livingEntity.damageSources().flyIntoWall(), getDamageFromLevel(pAmplifier + 1));
                    livingEntity.removeEffect(MobEffectRegistry.AIRBORNE.get());
                }
            }
        }
    }

    public static float getDamageFromLevel(int level) {
        return 4 + level * damage_per_amp;
    }
}
