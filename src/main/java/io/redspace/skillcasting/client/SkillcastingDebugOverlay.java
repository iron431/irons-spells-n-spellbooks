package io.redspace.skillcasting.client;

import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SkillcastingDebugOverlay implements LayeredDraw.Layer {
    public static final SkillcastingDebugOverlay instance = new SkillcastingDebugOverlay();

    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int HEADER_COLOR = 0xFFFF55;
    private static final int BACKGROUND_COLOR = 0xA0000000;

    private static boolean visible;

    private SkillcastingDebugOverlay() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (FMLLoader.isProduction()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!visible || minecraft.options.hideGui) {
            return;
        }
        var player = minecraft.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        List<String> lines = buildLines(SkillcastingData.get(player));
        if (lines.isEmpty()) {
            return;
        }

        var font = minecraft.font;
        int x = 2;
        int y = 2;
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        int height = lines.size() * font.lineHeight;
        guiGraphics.fill(x - 1, y - 1, x + maxWidth + 2, y + height + 1, BACKGROUND_COLOR);

        for (String line : lines) {
            int color = line.startsWith("[") ? HEADER_COLOR : TEXT_COLOR;
            guiGraphics.drawString(font, line, x, y, color);
            y += font.lineHeight;
        }
    }

    static List<String> buildLines(SkillcastingData data) {
        List<String> lines = new ArrayList<>();
        lines.add("[Skillcasting]");
        appendSelection(lines, data.selectionManager());
        appendActiveCast(lines, data);
        appendCooldowns(lines, data);
        appendRecasts(lines, data);
        return lines;
    }

    private static void appendSelection(List<String> lines, SkillSelectionManager manager) {
        lines.add("[Selection]");
        lines.add("  count: " + manager.getSkillCount());
        lines.add("  selected: " + manager.getSelectionIndex());
        lines.add("  pointer: " + manager.getSkillSelection());
        if (manager.getSkillCount() == 0) {
            lines.add("  (empty)");
            return;
        }
        for (int i = 0; i < manager.getSkillCount(); i++) {
            var option = manager.getOptionAt(i);
            if (option == null) {
                continue;
            }
            String marker = i == manager.getSelectionIndex() ? ">" : " ";
            lines.add("  " + marker + " [" + i + "] " + option.skillData.skillId() + " lv" + option.skillData.getLevel()
                    + " slot=" + option.equipmentSlot);
        }
    }

    private static void appendActiveCast(List<String> lines, SkillcastingData data) {
        long gameTime = Minecraft.getInstance().level.getGameTime();
        lines.add("[Active Cast]");
        ActiveCast activeCast = data.getActiveCast();
        if (activeCast == null) {
            lines.add("  none");
            return;
        }
        AbstractSkill skill = data.getActiveSkill();
        CastType castType = data.getActiveCastType();
        lines.add("  skill: " + (skill == null ? "?" : skill.getSkillId()));
        lines.add("  type: " + (castType == null ? "?" : castType.name()));
        lines.add("  duration: " + data.castDuration() + " ticks");
        lines.add("  progress: " + String.format("%.1f%%", data.castCompletionPercent(gameTime) * 100f));
        lines.add("  startedAt: " + activeCast.startedAtGameTime() + " (gameTime=" + gameTime + ")");
        appendSyncedComponents(lines, "  ", activeCast.context().components().getAllSynced());
    }

    private static void appendCooldowns(List<String> lines, SkillcastingData data) {
        lines.add("[Cooldowns]");
        Map<Holder<AbstractSkill>, CooldownInstance> cooldowns = data.cooldowns().view();
        if (cooldowns.isEmpty()) {
            lines.add("  none");
            return;
        }
        for (Map.Entry<Holder<AbstractSkill>, CooldownInstance> entry : cooldowns.entrySet()) {
            CooldownInstance instance = entry.getValue();
            lines.add("  " + entry.getKey().value().getSkillId() + ": " + instance.remainingTicks() + "/" + instance.totalTicks() + " ticks");
        }
    }

    private static void appendRecasts(List<String> lines, SkillcastingData data) {
        lines.add("[Recasts]");
        if (data.recasts().isEmpty()) {
            lines.add("  none");
            return;
        }
        for (RecastInstance recast : data.recasts().getActiveRecasts()) {
            lines.add("  " + recast.skill().value().getSkillId() + ": casts=" + recast.remainingCasts()
                    + "/" + recast.config().totalCasts()
                    + " window=" + recast.ticksRemaining() + " ticks");
            appendSyncedComponents(lines, "    ", recast.components().getAllSynced());
        }
    }

    private static void appendSyncedComponents(List<String> lines, String indent, Map<ComponentType<?>, Object> components) {
        if (components.isEmpty()) {
            return;
        }
        lines.add(indent + "synced components:");
        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            ResourceLocation typeId = SkillcastingComponentTypes.id(entry.getKey());
            lines.add(indent + "  " + (typeId == null ? entry.getKey() : typeId) + "=" + entry.getValue());
        }
    }
}
