package io.redspace.ironsspellbooks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ARMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALWAYS_SHOW_MANA_BAR;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("animations");
        SHOW_FIRST_PERSON_ARMS = BUILDER.define("showFirstPersonArms", true);
        SHOW_FIRST_PERSON_ITEMS = BUILDER.define("showFirstPersonItems", true);
        ALWAYS_SHOW_MANA_BAR = BUILDER.define("alwaysShowManaBar", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
