package io.redspace.skillcasting.config;

import io.redspace.skillcasting.client.overlays.RecastOverlay;
import io.redspace.skillcasting.client.overlays.SkillBarOverlay;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SkillcastingClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ARMS;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ITEMS;
    public static final ModConfigSpec.ConfigValue<SkillBarOverlay.Display> SKILL_BAR_DISPLAY;
    public static final ModConfigSpec.ConfigValue<SkillBarOverlay.Anchor> SKILL_BAR_ANCHOR;
    public static final ModConfigSpec.ConfigValue<Integer> SKILL_BAR_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> SKILL_BAR_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<RecastOverlay.Anchor> RECAST_ANCHOR;
    public static final ModConfigSpec.ConfigValue<Integer> RECAST_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> RECAST_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Boolean> SKILL_WHEEL_IGNORE_GUI_SCALE;
    public static final ModConfigSpec.ConfigValue<Double> SKILL_WHEEL_SCALE;

    static {
        BUILDER.push("UI");
        BUILDER.push("SkillBar");
        BUILDER.comment("Always shows the skill bar. Contextual hides it while no skill is casting, cooling down or awaiting a recast.");
        SKILL_BAR_DISPLAY = BUILDER.defineEnum("skillBarDisplay", SkillBarOverlay.Display.Always);
        SKILL_BAR_ANCHOR = BUILDER.defineEnum("skillBarAnchor", SkillBarOverlay.Anchor.Hotbar);
        SKILL_BAR_X_OFFSET = BUILDER.define("skillBarXOffset", 0);
        SKILL_BAR_Y_OFFSET = BUILDER.define("skillBarYOffset", 0);
        BUILDER.pop();
        BUILDER.push("RecastOverlay");
        RECAST_ANCHOR = BUILDER.defineEnum("recastAnchor", RecastOverlay.Anchor.TopCenter);
        RECAST_X_OFFSET = BUILDER.define("recastXOffset", 0);
        RECAST_Y_OFFSET = BUILDER.define("recastYOffset", 0);
        BUILDER.pop();
        BUILDER.push("SkillWheel");
        SKILL_WHEEL_IGNORE_GUI_SCALE = BUILDER.comment("Whether the skill wheel size ignores the GUI scale option").define("ignoreGuiScale", false);
        SKILL_WHEEL_SCALE = BUILDER.comment("If ignoreGuiScale is enabled, apply this multiplier to its size").define("ignoreGuiScaleSizeMultiplier", 1.0);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Animations");
        BUILDER.comment("What to render in first person while casting.");
        SHOW_FIRST_PERSON_ARMS = BUILDER.define("showFirstPersonArms", true);
        SHOW_FIRST_PERSON_ITEMS = BUILDER.define("showFirstPersonItems", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private SkillcastingClientConfig() {
    }
}
