package io.redspace.ironsspellbooks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ARMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALWAYS_SHOW_MANA_BAR;
    public static final ForgeConfigSpec.ConfigValue<Integer> MANA_BAR_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> MANA_BAR_X_OFFSET;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Animations");
        BUILDER.comment("What to render in first person while casting.");
        SHOW_FIRST_PERSON_ARMS = BUILDER.define("showFirstPersonArms", true);
        SHOW_FIRST_PERSON_ITEMS = BUILDER.define("showFirstPersonItems", true);
        BUILDER.pop();

        BUILDER.push("UI");
        BUILDER.comment("By default, the mana bar only appears when you are holding a magic item or are not at max mana.");
        ALWAYS_SHOW_MANA_BAR = BUILDER.define("alwaysShowManaBar", false);
        BUILDER.comment("Use to adjust if the mana bar conflicts with other mod's ui elements (11 is one full hunger bar up).");
        MANA_BAR_Y_OFFSET = BUILDER.define("manaBarYOffset", 0);
        MANA_BAR_X_OFFSET = BUILDER.define("manaBarXOffset", 0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
