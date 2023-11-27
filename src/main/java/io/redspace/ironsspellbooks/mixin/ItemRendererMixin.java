package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.object.Color;

@Mixin(GuiGraphics.class)
public class ItemRendererMixin {
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At(value = "HEAD"))
    public void renderSpellbookCooldown(Font p_281721_, ItemStack stack, int one, int two, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof SpellBook) {
            AbstractSpell spell = SpellBookData.getSpellBookData(stack).getActiveSpell().getSpell();
            float f = spell == SpellRegistry.none() ? 0 : ClientMagicData.getCooldownPercent(spell);
            renderSpellCooldown(one, two, f);
        } else if (SpellData.hasSpellData(stack) && !stack.getItem().equals(ItemRegistry.SCROLL.get())) {
            AbstractSpell spell = SpellData.getSpellData(stack).getSpell();
            float f = spell == SpellRegistry.none() ? 0 : ClientMagicData.getCooldownPercent(spell);
            renderSpellCooldown(one, two, f);
        } else if (item instanceof AutoloaderCrossbow) {
            renderSpellCooldown(one, two, !AutoloaderCrossbow.isLoading(stack) ? 0.0F : 1 - AutoloaderCrossbow.getLoadingTicks(stack) / (float) AutoloaderCrossbow.getChargeDuration(stack));
        }
    }

    private void renderSpellCooldown(int one, int two, float f) {
        if (f > 0.0F) {
            GuiGraphics self = (GuiGraphics) (Object) this;
            var i1 = two + Mth.floor(16.0F * (1.0F - f));
            var j1 = i1 + Mth.ceil(16.0F * f);
            self.fill(RenderType.guiOverlay(), one, i1, one + 16, j1, Integer.MAX_VALUE);
        }
    }
}
