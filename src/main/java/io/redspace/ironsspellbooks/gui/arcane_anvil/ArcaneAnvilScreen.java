package io.redspace.ironsspellbooks.gui.arcane_anvil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneAnvilScreen extends ItemCombinerScreen<ArcaneAnvilMenu> {
    private static final ResourceLocation ANVIL_LOCATION = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/arcane_anvil.png");

    public ArcaneAnvilScreen(ArcaneAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, ANVIL_LOCATION);
        this.titleLabelX = 48;
        this.titleLabelY = 24;
    }

    @Override
    protected void renderBg(GuiGraphics guiHelper, float pPartialTick, int pX, int pY) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, ANVIL_LOCATION);

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        guiHelper.blit(ANVIL_LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

        // X over arrow
        if (((this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem())) {
            guiHelper.blit(ANVIL_LOCATION, leftPos + 99, topPos + 45, this.imageWidth, 0, 28, 21);
        }

    }

    @Override
    protected void renderErrorIcon(GuiGraphics p_281990_, int p_266822_, int p_267045_) {
    }

}
