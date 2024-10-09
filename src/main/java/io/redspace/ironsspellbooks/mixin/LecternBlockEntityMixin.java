package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.SpellBook;
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

    @Inject(
            method = "hasBook",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private void fudgeLecternValidity(CallbackInfoReturnable<Boolean> cir) {
        if (book.getItem() instanceof SpellBook) {
            cir.setReturnValue(true);
        }
    }
}
