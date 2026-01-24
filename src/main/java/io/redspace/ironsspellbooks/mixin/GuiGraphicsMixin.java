package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void irons_spellbooks$attachCustomDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (!stack.isEmpty() && TransmogHolder.has(stack)) {
            GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.blitSprite(IronsSpellbooks.id("transmog_indicator"), x + 16 - 6, y, 6, 6);
            guiGraphics.pose().popPose();
        }
    }
}
