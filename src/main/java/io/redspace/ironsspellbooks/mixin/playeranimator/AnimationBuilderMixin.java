package io.redspace.ironsspellbooks.mixin.playeranimator;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyframeAnimation.AnimationBuilder.class)
public class AnimationBuilderMixin {
    @Inject(method = "<init>(Ldev/kosmx/playerAnim/core/data/AnimationFormat;)V", at = @At("RETURN"))
    private void fixGeckolibEasing(AnimationFormat source, CallbackInfo ci) {
        if (source == AnimationFormat.JSON_MC_ANIM) {
            ((KeyframeAnimation.AnimationBuilder) (Object) this).isEasingBefore = true;
        }
    }
}
