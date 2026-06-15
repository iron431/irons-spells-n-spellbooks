package io.redspace.skillcasting.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.api.recast.RecastInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.joml.Vector3f;

/**
 * Recast charge orbs overlay, ported from the original skillcasting-api client HUD.
 */
public final class RecastOverlay implements LayeredDraw.Layer {
    public static final RecastOverlay instance = new RecastOverlay();

    public static final ResourceLocation TEXTURE = Skillcasting.id("textures/gui/icons.png");
    static final int ORB_WIDTH = 10;
    static final int ORB_TEXTURE_OFFSET_X = 99;
    static final int ORB_TEXTURE_OFFSET_Y = 5;
    static final int CONNECTOR_TEXTURE_OFFSET_X = 109;
    static final int CONNECTOR_TEXTURE_OFFSET_Y = 8;
    static final int CONNECTOR_WIDTH = 6;

    public enum Anchor {
        Center(0.5f, 0.5f),
        TopCenter(0.5f, 0),
        TopLeft(0, 0),
        TopRight(0, 1),
        BottomLeft(0, 1),
        BottomRight(1, 1);
        final float m1, m2;

        Anchor(float mx, float my) {
            this.m1 = mx;
            this.m2 = my;
        }
    }

    int bossbarsActive;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        var data = SkillcastingData.get(player);
        var activeRecasts = data.recasts().asMap();
        if (activeRecasts.isEmpty()) {
            return;
        }

        int totalHeightPerBar = 18;
        int screenTopBuffer = 6;
        Anchor anchor = Anchor.TopCenter;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int castIndex = 0;
        for (var entry : activeRecasts.entrySet()) {
            RecastInstance recast = entry.getValue();
            AbstractSkill skill = entry.getKey().value();
            int total = recast.config().totalCasts();
            int remaining = recast.remainingCasts();
            int totalWidth = total * ORB_WIDTH + (total - 1) * CONNECTOR_WIDTH;
            int barX = (int) (screenWidth * anchor.m1);
            int barY = (int) (screenHeight * anchor.m2);
            if (anchor == Anchor.Center || anchor == Anchor.TopCenter) {
                barX -= totalWidth / 2;
            }
            if (anchor == Anchor.TopCenter) {
                barY += screenTopBuffer + bossbarsActive * 19;
            }
            barY += totalHeightPerBar * castIndex;

            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(barX - 18, barY - 2, 0);
            poseStack.scale(0.85f, 0.85f, 0.85f);
            guiGraphics.blit(skill.getIconLocation(), 0, 0, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderTexture(0, TEXTURE);
            guiGraphics.blit(TEXTURE, -2, -2, 116, 0, 20, 20, 256, 256);
            poseStack.popPose();

            for (int i = 0; i < total; i++) {
                int orbX = barX + (ORB_WIDTH + CONNECTOR_WIDTH) * i;
                int connectorX = orbX + ORB_WIDTH;
                if (i + 1 < total) {
                    guiGraphics.blit(TEXTURE, connectorX, barY + 3, CONNECTOR_TEXTURE_OFFSET_X, CONNECTOR_TEXTURE_OFFSET_Y, 6, 4, 256, 256);
                }
                boolean charged = i < remaining;
                if (charged) {
                    Vector3f color = new Vector3f(1, 1, 1);
                    RenderSystem.setShaderColor(color.x(), color.y(), color.z(), 1f);
                }
                guiGraphics.blit(TEXTURE, orbX, barY,
                        ORB_TEXTURE_OFFSET_X + (charged ? 0 : 10), ORB_TEXTURE_OFFSET_Y + (charged ? 0 : 21),
                        ORB_WIDTH, ORB_WIDTH, 256, 256);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            int textX = barX + (ORB_WIDTH + CONNECTOR_WIDTH) * total;
            int ticksToLive = recast.config().durationTicks();
            guiGraphics.drawString(Minecraft.getInstance().font,
                    formatTime(recast.ticksRemaining(), ticksToLive),
                    textX, barY + (ORB_WIDTH - Minecraft.getInstance().font.lineHeight) / 2, ChatFormatting.WHITE.getColor());
            castIndex++;
        }
        bossbarsActive = 0;
    }

    private static String formatTime(int ticksRemaining, int totalTicks) {
        int totalSeconds = totalTicks / 20;
        int remainingSeconds = ticksRemaining / 20;
        String time = "";
        if (totalSeconds > 60) {
            time += (remainingSeconds / 60) + ":";
            remainingSeconds %= 60;
        }
        if (totalSeconds >= 10) {
            time += remainingSeconds / 10;
        }
        time += remainingSeconds % 10;
        return time + "s";
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void countBossBars(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!event.isCanceled()) {
            RecastOverlay.instance.bossbarsActive++;
        }
    }
}
