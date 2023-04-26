package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    /*
    Necessary to display how many times a piece of gear has been upgraded on its name
     */
    @Inject(method = "getHoverName", at = @At("TAIL"), cancellable = true)
    public void getHoverName(CallbackInfoReturnable<Component> cir) {
        //Neat trick from apotheosis
        ItemStack stack = (ItemStack) (Object) this;
        if (UpgradeUtils.isUpgraded(stack))
            cir.setReturnValue(Component.translatable("tooltip.irons_spellbooks.upgrade_plus_format", stack.getItem().getName(stack), UpgradeUtils.getUpgradeCount(stack)));
    }
}
