package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.player.ClientMagicData;
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

    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "RETURN"), cancellable = true)
    public void irons_spellbooks$changeGlowOutline(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player == null || pEntity == null || cir.getReturnValue()) {
            return;
        }
        if (ClientConfigs.SUMMONS_GLOW.get() && ClientMagicData.getActiveSummons().contains(pEntity.getUUID())) {
            cir.setReturnValue(true);
        } else if (Minecraft.getInstance().player.hasEffect(MobEffectRegistry.PLANAR_SIGHT) && pEntity instanceof LivingEntity && Mth.abs((float) (pEntity.getY() - Minecraft.getInstance().player.getY())) < 18) {
            cir.setReturnValue(true);
        }
    }
}
