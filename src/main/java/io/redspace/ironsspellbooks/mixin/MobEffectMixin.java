package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public class MobEffectMixin {
    @Inject(method = "applyEffectTick", at = @At(value = "HEAD"))
    private void markPoisoned(LivingEntity pLivingEntity, int pAmplifier, CallbackInfo ci) {
        if ((Object) this == MobEffects.POISON && pLivingEntity instanceof ServerPlayer serverPlayer) {
            MagicData.getPlayerMagicData(serverPlayer).markPoisoned();
        }
    }
}
