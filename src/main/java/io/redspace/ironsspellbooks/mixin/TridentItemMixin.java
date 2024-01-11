package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
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
    public void releaseUsing(ItemStack p_43394_, Level p_43395_, LivingEntity livingEntity, int p_43397_, CallbackInfo ci) {
        if (livingEntity.level.isClientSide) {
            ClientMagicData.getSyncedSpellData(livingEntity).setSpinAttackType(SpinAttackType.RIPTIDE);
        } else {
            MagicData.getPlayerMagicData(livingEntity).getSyncedData().setSpinAttackType(SpinAttackType.RIPTIDE);
        }
    }
}
