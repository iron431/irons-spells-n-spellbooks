package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
@Mixin(ItemModularHandheld.class)
public abstract class TetraBladedItemMixin {

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!SpellData.getSpellData(stack).equals(SpellData.EMPTY)) {
            cir.setReturnValue(7200);
        }
    }
}
