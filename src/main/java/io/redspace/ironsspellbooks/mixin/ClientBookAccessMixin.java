package io.redspace.ironsspellbooks.mixin;


import io.redspace.ironsspellbooks.gui.IronBookAccess;
import io.redspace.ironsspellbooks.item.ILecternPlaceable;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//1.20.1 cant mixin to interfaces, need to take a step outward
//@Mixin(BookViewScreen.BookAccess.class)
//public class ClientBookAccessMixin {
//    @Inject(
//            method = "Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;fromItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;",
//            remap = false,
//            at = @At(value = "HEAD"),
//            cancellable = true)
//    private static void modifyLecternContents(ItemStack stack, CallbackInfoReturnable<BookViewScreen.BookAccess> cir) {
//        if (stack.getItem() instanceof ILecternPlaceable lecternPlaceable) {
//            cir.setReturnValue(new IronBookAccess(lecternPlaceable.getPages(stack)));
//
//        }
//    }
//}
@Mixin(LecternScreen.class)
public abstract class ClientBookAccessMixin extends BookViewScreen {
    @Final
    @Shadow
    private LecternMenu menu;

    @Inject(
            method = "bookChanged",
            at = @At(value = "HEAD"),
            cancellable = true)
    void irons_spellbooks$injectCustomBookContents(CallbackInfo ci) {
        ItemStack itemstack = menu.getBook();
        if (itemstack.getItem() instanceof ILecternPlaceable placeable) {
            setBookAccess(new IronBookAccess(placeable.getPages(itemstack)));
            ci.cancel();
        }
    }
}