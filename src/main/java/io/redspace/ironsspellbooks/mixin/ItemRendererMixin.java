package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelectionManager;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    //TODO: can't this be an event?
    @Inject(method = "renderGuiItemDecorations", at = @At(value = "TAIL"))
    public void renderSpellbookCooldown(Font font, ItemStack stack, int one, int two, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof SpellBook) {
            var player = MinecraftInstanceHelper.getPlayer();
            if (player != null) {
                SpellSelectionManager manager = new SpellSelectionManager(player);
                if (manager.getCurrentSelection().equipmentSlot.equals(Curios.SPELLBOOK_SLOT)) {
                    var spell = manager.getSelectedSpellData().getSpell();
                    float f = spell == SpellRegistry.none() ? 0 : ClientMagicData.getCooldownPercent(spell);
                    renderSpellCooldown(one, two, f);
                }
            }
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
