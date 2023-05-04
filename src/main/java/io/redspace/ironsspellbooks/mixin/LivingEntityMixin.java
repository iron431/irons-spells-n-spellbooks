package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin{

    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object)this;
        if (self.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get()))
            self.setInvisible(true);
    }
}
