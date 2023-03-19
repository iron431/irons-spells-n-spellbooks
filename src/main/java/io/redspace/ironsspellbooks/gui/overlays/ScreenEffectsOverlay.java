package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class ScreenEffectsOverlay extends GuiComponent {
    public final static ResourceLocation MAGIC_AURA_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/.png");
    public final static ResourceLocation HEARSTOP_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/overlays/heartstop.png");
    static final int IMAGE_WIDTH = 256;
    static final int IMAGE_HEIGHT = 256;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player.hasEffect(MobEffectRegistry.HEARTSTOP.get())) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, HEARSTOP_TEXTURE);

            gui.blit(poseStack, 0, 0, 0, 0, Minecraft.getInstance().screen.width,  Minecraft.getInstance().screen.height, 256, 256);
        }

//        gui.blit(poseStack, barX, barY, 0, IMAGE_HEIGHT * 3, (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2), IMAGE_HEIGHT);
//
//        int textX, textY;
//        var textColor = ChatFormatting.WHITE;
//        var font = gui.getFont();
//
//
//
//        textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
//        textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;
//
//        gui.getFont().draw(poseStack, castTimeString, textX, textY, textColor.getColor());
    }
}
