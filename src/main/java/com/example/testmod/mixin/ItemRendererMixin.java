package com.example.testmod.mixin;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataProvider;
import com.example.testmod.item.SpellBook;
import com.example.testmod.item.WimpySpellBook;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.floats.Float2IntAVLTreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FenceGateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.struct.CallbackInjectionInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow
    private void fillRect(BufferBuilder p_115153_, int p_115154_, int p_115155_, int p_115156_, int p_115157_, int p_115158_, int p_115159_, int p_115160_, int p_115161_) {
    }

    @Inject(method = "renderGuiItemDecorations", at = @At(value = "TAIL"))
    public void renderSpellbookCooldown(Font font, ItemStack stack, int one, int two, CallbackInfo ci) {
        Item item = stack.getItem();
        if (item instanceof WimpySpellBook) {
            //copied from ItemRenderer renderGuiItemDecorations cooldown section
            LocalPlayer localplayer = Minecraft.getInstance().player;
            var s = stack.getCapability(SpellBookDataProvider.SPELL_BOOK_DATA).resolve().get();
            float f = (localplayer == null||s == null) ? 0.5F : s.getActiveSpell().getPercentCooldown();

            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                fillRect(bufferbuilder1, one, two + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            //TestMod.LOGGER.info("hooked: " + (f * 100) + "% cooldown");
            //TestMod.LOGGER.info(s.getActiveSpell().getLevel()+"");
        }

    }
}
