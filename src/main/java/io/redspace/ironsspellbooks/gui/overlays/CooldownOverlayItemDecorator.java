package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class CooldownOverlayItemDecorator implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        Item item = stack.getItem();
        if (item instanceof CastingItem && MinecraftInstanceHelper.getPlayer() != null) {
            SpellSelectionManager manager = ClientMagicData.getSpellSelectionManager();
            var spell = manager.getSelectedSpellData().getSpell();
            float f = spell == SpellRegistry.none() ? 0 : ClientMagicData.getCooldownPercent(spell);
            if (f > 0.0F) {
                renderCooldown(xOffset, yOffset, f);
                return true;
            }

        } else if (item instanceof AutoloaderCrossbow) {
            float f = !AutoloaderCrossbow.isLoading(stack) ? 0.0F : 1 - AutoloaderCrossbow.getLoadingTicks(stack) / (float) AutoloaderCrossbow.getChargeDuration(stack);
            if (f > 0.0F) {
                renderCooldown(xOffset, yOffset, f);
                return true;
            }
        }
        return false;
    }

    private void renderCooldown(int one, int two, float f) {
        RenderSystem.disableDepthTest();
        //RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        fillRect(bufferbuilder, one, two + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
        //RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private void fillRect(BufferBuilder pRenderer, int pX, int pY, int pWidth, int pHeight, int pRed, int pGreen, int pBlue, int pAlpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        pRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        pRenderer.vertex((double) (pX + 0), (double) (pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double) (pX + 0), (double) (pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double) (pX + pWidth), (double) (pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double) (pX + pWidth), (double) (pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        BufferUploader.drawWithShader(pRenderer.end());
    }

}
