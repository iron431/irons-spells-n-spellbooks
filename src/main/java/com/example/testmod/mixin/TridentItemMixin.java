package com.example.testmod.mixin;

import com.example.testmod.player.ClientMagicData;
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
    public void releaseUsing(ItemStack p_43394_, Level p_43395_, LivingEntity p_43396_, int p_43397_, CallbackInfo ci) {
        ClientMagicData.lastSpinAttack = ClientMagicData.SpinAttackType.RIPTIDE;
    }

}
