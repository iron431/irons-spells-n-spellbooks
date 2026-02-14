package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Util.class)
public class ClientUtilMixin {
    @Inject(method = "shutdownExecutors", at = @At("TAIL"))
    private static void shutdownPatreonExecutor(CallbackInfo ci) {
        PatreonHandler.shutdown();
    }
}
