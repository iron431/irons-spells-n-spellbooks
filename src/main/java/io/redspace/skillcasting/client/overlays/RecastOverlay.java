package io.redspace.skillcasting.client.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.config.SkillcastingClientConfig;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.SkillcastingData;
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

public final class RecastOverlay implements LayeredDraw.Layer {
    public static final RecastOverlay instance = new RecastOverlay();

    public static final ResourceLocation TEXTURE = Skillcasting.id("textures/gui/icons.png");
    static final int ORB_WIDTH = 10;
    static final int ORB_TEXTURE_OFFSET_X = 99;
    static final int ORB_TEXTURE_OFFSET_Y = 5;
    static final int CONNECTOR_TEXTURE_OFFSET_X = 109;
    static final int CONNECTOR_TEXTURE_OFFSET_Y = 8;
    static final int CONNECTOR_WIDTH = 6;
    static final int ROW_HEIGHT = 18;
    static final int EDGE_MARGIN = 6;
    static final int ICON_LEFT_OVERHANG = 20;
    static final int ICON_TOP_OVERHANG = 4;
    static final int ICON_BOTTOM_OVERHANG = 14;

    public enum Anchor {
        Center(0.5f, 0.5f),
        TopCenter(0.5f, 0),
        TopLeft(0, 0),
        TopRight(1, 0),
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
        var activeRecasts = data.recasts().getActiveRecasts();
        if (activeRecasts.isEmpty()) {
            return;
        }

        Anchor anchor = SkillcastingClientConfig.RECAST_ANCHOR.get();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int castIndex = 0;
        for (RecastInstance recastInstance : activeRecasts) {
            var skill = recastInstance.skill().value();
            int total = recastInstance.config().totalCasts();
            int remaining = recastInstance.remainingCasts();
            int totalWidth = total * ORB_WIDTH + (total - 1) * CONNECTOR_WIDTH;
            String time = formatTime(recastInstance.ticksRemaining(), recastInstance.config().durationTicks());
            int barX = (int) (screenWidth * anchor.m1);
            int barY = (int) (screenHeight * anchor.m2);
            if (anchor.m1 == 0) {
                barX += EDGE_MARGIN + ICON_LEFT_OVERHANG;
            } else if (anchor.m1 == 1) {
                barX -= EDGE_MARGIN + (ORB_WIDTH + CONNECTOR_WIDTH) * total + Minecraft.getInstance().font.width(time);
            } else {
                barX -= totalWidth / 2;
            }
            int rowOffset = ROW_HEIGHT * castIndex;
            if (anchor == Anchor.TopCenter) {
                barY += EDGE_MARGIN + bossbarsActive * 19 + rowOffset;
            } else if (anchor.m2 == 0) {
                barY += EDGE_MARGIN + ICON_TOP_OVERHANG + rowOffset;
            } else if (anchor.m2 == 1) {
                barY -= EDGE_MARGIN + ICON_BOTTOM_OVERHANG + rowOffset;
            } else {
                barY += rowOffset;
            }
            barX += SkillcastingClientConfig.RECAST_X_OFFSET.get();
            barY += SkillcastingClientConfig.RECAST_Y_OFFSET.get();

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
                    // connector
                    guiGraphics.blit(TEXTURE, connectorX, barY + 3, CONNECTOR_TEXTURE_OFFSET_X, CONNECTOR_TEXTURE_OFFSET_Y, 6, 4, 256, 256);
                }
                //orb filling
                boolean charged = i < remaining;
                guiGraphics.blit(TEXTURE, orbX, barY, ORB_TEXTURE_OFFSET_X + (charged ? 0 : 10), ORB_TEXTURE_OFFSET_Y + 21, ORB_WIDTH, ORB_WIDTH, 256, 256);
                if (charged) {
                    Vector3f color = skill.getAccentColor();
                    RenderSystem.setShaderColor(color.x(), color.y(), color.z(), 1f);
                    guiGraphics.blit(TEXTURE, orbX, barY, ORB_TEXTURE_OFFSET_X + (charged ? 0 : 10), ORB_TEXTURE_OFFSET_Y + 21, ORB_WIDTH, ORB_WIDTH, 256, 256);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                }
                //orb
                guiGraphics.blit(TEXTURE, orbX, barY, ORB_TEXTURE_OFFSET_X, ORB_TEXTURE_OFFSET_Y, ORB_WIDTH, ORB_WIDTH, 256, 256);
            }
            int textX = (barX + (ORB_WIDTH + CONNECTOR_WIDTH) * total);
            guiGraphics.drawString(Minecraft.getInstance().font, time, textX, barY + (ORB_WIDTH - Minecraft.getInstance().font.lineHeight) / 2, ChatFormatting.WHITE.getColor());
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
