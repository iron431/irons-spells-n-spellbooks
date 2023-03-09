package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow
    private void fillRect(BufferBuilder p_115153_, int p_115154_, int p_115155_, int p_115156_, int p_115157_, int p_115158_, int p_115159_, int p_115160_, int p_115161_) {
    }

    @Inject(method = "renderGuiItemDecorations", at = @At(value = "TAIL"))
    public void renderSpellbookCooldown(Font font, ItemStack stack, int one, int two, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof SpellBook spellBook) {

            AbstractSpell spell = spellBook.getSpellBookData(stack).getActiveSpell();
            renderSpellCooldown(one, two, spell);
            //irons_spellbooks.LOGGER.debug("hooked: " + (f * 100) + "% cooldown");
            //irons_spellbooks.LOGGER.debug(s.getActiveSpell().getLevel()+"");
        } else if (item instanceof SwordItem swordItem) {
            AbstractSpell spell = Utils.getScrollData(stack).getSpell();
            renderSpellCooldown(one, two, spell);
        }

    }
    private void renderSpellCooldown(int one, int two, AbstractSpell spell) {
        if (spell != null) {
            float f = ClientMagicData.getCooldownPercent(spell.getSpellType());

            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                fillRect(bufferbuilder, one, two + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }
}
