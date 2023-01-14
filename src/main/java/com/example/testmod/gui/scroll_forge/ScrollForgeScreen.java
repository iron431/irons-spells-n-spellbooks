package com.example.testmod.gui.scroll_forge;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScrollForgeScreen extends AbstractContainerScreen<ScrollForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/brewing_stand.png");

    public ScrollForgeScreen(ScrollForgeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        setTexture(TEXTURE);

        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    private void setTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }
}
