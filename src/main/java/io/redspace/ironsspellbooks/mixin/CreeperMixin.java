package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperMixin {
    @Inject(method = "thunderHit", at = @At(value = "HEAD"))
    void betterThunderHit(ServerLevel pLevel, LightningBolt pLightning, CallbackInfo ci) {
        if (ServerConfigs.BETTER_CREEPER_THUNDERHIT.get()) {
            Creeper self = (Creeper) (Object) this;
            if (!self.isPowered()) {
                self.heal(self.getMaxHealth());
            }
        }
    }
}
