package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    /**
     Necessary make entities appear glowing on our client while we have the echolocation effect
     */
    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void changeGlowOutline(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(MobEffectRegistry.PLANAR_SIGHT.get()) && pEntity instanceof LivingEntity && Mth.abs((float) (pEntity.getY() - Minecraft.getInstance().player.getY())) < 18) {
            cir.setReturnValue(true);
        }/* else if (pEntity instanceof LivingEntity entity && entity.hasEffect(MobEffectRegistry.GUIDING_BOLT.get())) {
            cir.setReturnValue(true);
        }*/
        //TODO: better guiding bolt glowing. need some way to sync a flag across all mobs (our synced data is only for casting mobs)
    }
}
