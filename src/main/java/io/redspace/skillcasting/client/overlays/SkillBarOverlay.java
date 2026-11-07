package io.redspace.skillcasting.client.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.config.SkillcastingClientConfig;
import io.redspace.skillcasting.data.selection.SkillSelectionPriority;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.client.render.ClientRenderCache;
import io.redspace.skillcasting.data.skill.SkillData;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.selection.SkillSelectionManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public final class SkillBarOverlay implements LayeredDraw.Layer {
    public static final SkillBarOverlay instance = new SkillBarOverlay();

    public static final ResourceLocation TEXTURE = Skillcasting.id("textures/gui/icons.png");
    static final int IMAGE_HEIGHT = 21;
    static final int CONTEXTUAL_FADE_WAIT = 80;
    static final int SLOT_SIZE = 22;
    static final int EDGE_MARGIN = 6;
    static float alpha = 1f;
    static int lastSkillCount;
    static int fadeoutDelay;
    static int lastTick;

    public enum Display {
        Never,
        Always,
        Contextual
    }

    public enum Anchor {
        Hotbar(0, 0),
        TopLeft(0, 0),
        TopRight(1, 0),
        BottomLeft(0, 1),
        BottomRight(1, 1);
        final int m1, m2;

        Anchor(int mx, int my) {
            this.m1 = mx;
            this.m2 = my;
        }
    }

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        int screenWidth = guiHelper.guiWidth();
        int screenHeight = guiHelper.guiHeight();
        Display display = SkillcastingClientConfig.SKILL_BAR_DISPLAY.get();
        if (display == Display.Never) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        var data = SkillcastingData.get(player);
        SkillSelectionManager manager = data.selectionManager();
        if (manager.getSkillCount() != lastSkillCount) {
            lastSkillCount = manager.getSkillCount();
            ClientRenderCache.generateRelativeLocations(manager, 20, 22);
            fadeoutDelay = CONTEXTUAL_FADE_WAIT;
        }
        if (manager.getSkillCount() <= 0) {
            return;
        }
        if (display == Display.Contextual) {
            handleFading(player, data);
            if (fadeoutDelay <= 0) {
                return;
            }
        } else {
            alpha = 1f;
        }

        var options = manager.getAllOptions();
        var locations = ClientRenderCache.relativeSkillBarSlotLocations;
        int centerX;
        int centerY;
        Anchor anchor = SkillcastingClientConfig.SKILL_BAR_ANCHOR.get();
        if (anchor == Anchor.Hotbar) {
            int approximateWidth = locations.size() / 3;
            centerX = screenWidth / 2 - Math.max(110, screenWidth / 4) - approximateWidth * 5;
            centerY = screenHeight - Math.max(55, screenHeight / 8);
        } else {
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            for (Vec2 location : locations) {
                minX = Math.min(minX, location.x);
                maxX = Math.max(maxX, location.x + SLOT_SIZE);
                minY = Math.min(minY, location.y);
                maxY = Math.max(maxY, location.y + SLOT_SIZE);
            }
            if (anchor.m1 == 0) {
                centerX = (int) (EDGE_MARGIN - minX);
            } else {
                centerX = (int) (screenWidth - EDGE_MARGIN - maxX);
            }
            if (anchor.m2 == 0) {
                centerY = (int) (EDGE_MARGIN - minY);
            } else {
                centerY = (int) (screenHeight - EDGE_MARGIN - maxY);
            }
        }
        centerX += SkillcastingClientConfig.SKILL_BAR_X_OFFSET.get();
        centerY += SkillcastingClientConfig.SKILL_BAR_Y_OFFSET.get();
        int selectedSkillIndex = manager.getSelectionIndex();

        prepTranslucency();
        for (Vec2 location : locations) {
            guiHelper.blit(TEXTURE, centerX + (int) location.x, centerY + (int) location.y, 66, 84, 22, 22);
        }
        for (int i = 0; i < locations.size(); i++) {
            SkillData skillData = manager.getSkillData(i);
            if (skillData != null && skillData.getSkill() != null) {
                AbstractSkill skill = skillData.getSkill();
                guiHelper.blit(skill.getIconLocation(), centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3,
                        0, 0, 16, 16, 16, 16);
            }
        }
        for (int i = 0; i < locations.size(); i++) {
            if (i != selectedSkillIndex) {
                boolean primarySource = i < options.size()
                        && options.get(i).priority().equals(SkillSelectionPriority.PRIMARY_SKILL_SOURCE);
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y,
                        22 + (primarySource ? 0 : 110), 84, 22, 22);
            }
            SkillData skillData = manager.getSkillData(i);
            if (skillData != null && skillData.getSkill() != null) {
                float f = data.cooldowns().getCooldownPercent(skillData.getSkill());
                if (f > 0) {
                    int pixels = (int) (16 * f);
                    guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels,
                            47, 87, 16, pixels);
                }
            }
        }
        for (int i = 0; i < locations.size(); i++) {
            if (i == selectedSkillIndex) {
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 0, 84, 22, 22);
            }
        }
        flushTranslucency();
    }

    private static void handleFading(Player player, SkillcastingData data) {
        if (lastTick != player.tickCount) {
            lastTick = player.tickCount;
            if (data.isCasting() || data.cooldowns().hasCooldownsActive() || data.recasts().hasRecastsActive()) {
                fadeoutDelay = CONTEXTUAL_FADE_WAIT;
            }
            if (fadeoutDelay > 0) {
                fadeoutDelay--;
            }
        }
        alpha = Mth.clamp(fadeoutDelay / 20f, 0, 1);
    }

    private static void prepTranslucency() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    private static void flushTranslucency() {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
