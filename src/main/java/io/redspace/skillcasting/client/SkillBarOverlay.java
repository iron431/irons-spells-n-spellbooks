package io.redspace.skillcasting.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public final class SkillBarOverlay implements LayeredDraw.Layer {
    public static final SkillBarOverlay instance = new SkillBarOverlay();

    public static final ResourceLocation TEXTURE = Skillcasting.id("textures/gui/icons.png");
    static final int IMAGE_HEIGHT = 21;
    static float alpha = 1f;
    static int lastSkillCount;

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        int screenWidth = guiHelper.guiWidth();
        int screenHeight = guiHelper.guiHeight();
        Player player = Minecraft.getInstance().player;
        var data = SkillcastingData.get(player);
        SkillSelectionManager manager = data.selectionManager();
        if (manager.getSkillCount() != lastSkillCount) {
            lastSkillCount = manager.getSkillCount();
            ClientRenderCache.generateRelativeLocations(manager, 20, 22);
        }
        if (manager.getSkillCount() <= 0) {
            return;
        }

        int centerX = screenWidth / 2 - Math.max(110, screenWidth / 4);
        int centerY = screenHeight - Math.max(55, screenHeight / 8);

        var options = manager.getAllOptions();
        var locations = ClientRenderCache.relativeSpellBarSlotLocations;
        int approximateWidth = locations.size() / 3;
        centerX -= approximateWidth * 5;
        int selectedSpellIndex = manager.getSelectionIndex();

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
            if (i != selectedSpellIndex) {
                boolean primarySource = i < options.size()
                        && options.get(i).priority == SkillSelectionPriority.PRIMARY_SKILL_SOURCE;
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
            if (i == selectedSpellIndex) {
                guiHelper.blit(TEXTURE, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 0, 84, 22, 22);
            }
        }
        flushTranslucency();
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
