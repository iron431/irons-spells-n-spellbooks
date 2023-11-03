package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get()))
            self.setInvisible(true);
    }

    /**
     * Vanilla still hardcodes the minimum sight range to 2 blocks, but this at least removes the effect of wearing armor
     */
    @Inject(method = "getArmorCoverPercentage", at = @At(value = "HEAD"), cancellable = true)
    public void getArmorCoverPercentage(CallbackInfoReturnable<Float> cir) {
        if (((LivingEntity) (Object) this).hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get())) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide() && self.hasEffect(MobEffectRegistry.GUIDING_BOLT.get())) {
            cir.setReturnValue(true);
        }
    }

}