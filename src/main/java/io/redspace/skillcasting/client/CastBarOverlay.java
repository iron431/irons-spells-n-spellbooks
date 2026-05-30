package io.redspace.skillcasting.client;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/**
 * Channel cast progress bar, ported from the original skillcasting-api client HUD.
 */
public final class CastBarOverlay implements LayeredDraw.Layer {
    public static final CastBarOverlay instance = new CastBarOverlay();

    public static final ResourceLocation TEXTURE = Skillcasting.id("textures/gui/icons.png");
    static final int IMAGE_WIDTH = 54;
    static final int COMPLETION_BAR_WIDTH = 44;
    static final int IMAGE_HEIGHT = 21;

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        SkillcastingData data = SkillcastingData.get(player);
        if (!data.isCasting() || data.getActiveSkill() == null || data.getActiveCastType() == CastType.INSTANT) {
            return;
        }

        float castDuration = data.castDuration();
        float castCompletionPercent = data.castCompletionPercent();
        String castTimeString = String.valueOf(data.castDurationRemaining() / 20f);

        int screenWidth = guiHelper.guiWidth();
        int screenHeight = guiHelper.guiHeight();
        int barX = screenWidth / 2 - IMAGE_WIDTH / 2;
        int barY = screenHeight / 2 + screenHeight / 8;

        guiHelper.blit(TEXTURE, barX, barY, 0, IMAGE_HEIGHT * 2, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        guiHelper.blit(TEXTURE, barX, barY, 0, IMAGE_HEIGHT * 3,
                (int) (COMPLETION_BAR_WIDTH * castCompletionPercent + (IMAGE_WIDTH - COMPLETION_BAR_WIDTH) / 2f), IMAGE_HEIGHT);

        var font = Minecraft.getInstance().font;
        int textX = barX + (IMAGE_WIDTH - font.width(castTimeString)) / 2;
        int textY = barY + IMAGE_HEIGHT / 2 - font.lineHeight / 2 + 1;
        guiHelper.drawString(font, castTimeString, textX, textY, 0xFFFFFF);
    }
}
