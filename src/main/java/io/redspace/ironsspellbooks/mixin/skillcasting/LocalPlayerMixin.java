package io.redspace.ironsspellbooks.mixin.skillcasting;

import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isHandsBusy", at = @At("HEAD"), cancellable = true)
    private void disableAttackingWhileCasting(CallbackInfoReturnable<Boolean> cir) {
        if (SkillcastingData.get((LocalPlayer) (Object) this).isCasting()) {
            cir.setReturnValue(true);
        }
    }
}
