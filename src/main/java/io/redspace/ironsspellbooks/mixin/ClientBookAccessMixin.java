package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookViewScreen.BookAccess.class)
public class ClientBookAccessMixin {
    @Inject(
            method = "Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;fromItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void modifyLecternContents(ItemStack stack, CallbackInfoReturnable<BookViewScreen.BookAccess> cir) {
        if (stack.getItem() instanceof SpellBook) {
            var spellbookData = ISpellContainer.get(stack);
            if (spellbookData != null) {
                var pages = spellbookData.getActiveSpells().stream().map(slot -> (Component) (Component.translatable(slot.getSpell().getComponentId() + ".guide"))).toList();
                cir.setReturnValue(new BookViewScreen.BookAccess(pages));
            }
        }
    }
}
