package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface IMobEffectEndCallback {
    /**
     * Automatically provides callback for a mob effect ending via {@link io.redspace.ironsspellbooks.mixin.LivingEntityMixin#onEffectRemoved(MobEffectInstance, CallbackInfo)}
     */
    void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier);
}
