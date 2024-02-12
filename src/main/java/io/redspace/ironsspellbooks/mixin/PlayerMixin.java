package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "canEat", at = @At(value = "RETURN"), cancellable = true)
    void canEatForGluttony(boolean pCanAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        if (((Player) (Object) this).hasEffect(MobEffectRegistry.GLUTTONY.get())) {
            cir.setReturnValue(true);
        }
    }
}
