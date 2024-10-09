package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.ILecternPlaceable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin {
    @Shadow
    ItemStack book;

    @Shadow
    private int page;

    @Inject(
            method = "hasBook",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private void fudgeLecternValidity(CallbackInfoReturnable<Boolean> cir) {
        if (book.getItem() instanceof ILecternPlaceable) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "getPageCount",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void getPageCount(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        if (pStack.getItem() instanceof ILecternPlaceable lecternPlaceable) {
            cir.setReturnValue(lecternPlaceable.getPages(pStack).size());
        }
    }
}
