package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.object.Color;

@Mixin(GuiGraphics.class)
public class ItemRendererMixin {
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At(value = "HEAD"))
    public void renderSpellbookCooldown(Font p_281721_, ItemStack itemStack, int one, int two, CallbackInfo ci) {
        Item item = itemStack.getItem();
        if (item instanceof SpellBook) {
            AbstractSpell spell = SpellBookData.getSpellBookData(itemStack).getActiveSpell();
            renderSpellCooldown(one, two, spell);
        } else if (SpellData.hasSpellData(itemStack) && !itemStack.getItem().equals(ItemRegistry.SCROLL.get())) {
            AbstractSpell spell = SpellData.getSpellData(itemStack).getSpell();
            renderSpellCooldown(one, two, spell);
        }
    }

    private void renderSpellCooldown(int p_282641_, int p_282146_, AbstractSpell spell) {
        if (spell.getSpellType().getValue() > 0) {
            float f = ClientMagicData.getCooldownPercent(spell.getSpellType());
            if (f > 0.0F) {
                GuiGraphics self = (GuiGraphics) (Object)this;
//                RenderSystem.disableDepthTest();
//                //RenderSystem.disableTexture();
//                RenderSystem.enableBlend();
//                RenderSystem.defaultBlendFunc();
//                Tesselator tesselator = Tesselator.getInstance();
//                BufferBuilder bufferbuilder = tesselator.getBuilder();
//                self.fill(poseStack, one, two + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), Color.ofRGBA(1f, 1f, 1f, .5f).getColor());
//                //RenderSystem.enableTexture();
//                RenderSystem.enableDepthTest();

                int i1 = p_282146_ + Mth.floor(16.0F * (1.0F - f));
                int j1 = i1 + Mth.ceil(16.0F * f);
                self.fill(RenderType.guiOverlay(), p_282641_, i1, p_282641_ + 16, j1, Integer.MAX_VALUE);
            }
        }
    }
}
