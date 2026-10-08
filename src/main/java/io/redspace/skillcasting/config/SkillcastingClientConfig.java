package io.redspace.skillcasting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SkillcastingClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ARMS;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ITEMS;

    static {
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
