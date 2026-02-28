package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.mixin.CuriosImplMixinHooks;

@Mixin(CuriosImplMixinHooks.class)
public class CurioHooksMixin {

    @Inject(method = "isStackValid", at = @At("HEAD"), cancellable = true)
    private static void killGenericCurio(SlotContext slotContext, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slotContext.identifier().equals("curio")) {
            if (stack.is(ModTags.SPELLBOOK_CURIO)) {
                cir.setReturnValue(false);
            }
        }
    }
}
