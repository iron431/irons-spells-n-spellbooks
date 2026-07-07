package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(method = "releaseUsing", at = @At(value = "TAIL"))
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfo ci) {
        if (MagicData.has(entityLiving)) {
            MagicData.get(entityLiving).setSpinAttackType(SpinAttackType.RIPTIDE);
        }
    }
}
