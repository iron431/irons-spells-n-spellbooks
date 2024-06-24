package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.RecastOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import net.neoforged.neoforge.common.ModConfigSpec;


public class ClientConfigs {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ARMS;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_FIRST_PERSON_ITEMS;
    public static final ModConfigSpec.ConfigValue<Boolean> REPLACE_GHAST_FIREBALL;
    public static final ModConfigSpec.ConfigValue<Boolean> REPLACE_BLAZE_FIREBALL;
    public static final ModConfigSpec.ConfigValue<Integer> MANA_BAR_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> MANA_BAR_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> MANA_TEXT_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> MANA_TEXT_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Boolean> MANA_BAR_TEXT_VISIBLE;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_BOSS_MUSIC;
    public static final ModConfigSpec.ConfigValue<ManaBarOverlay.Anchor> MANA_BAR_ANCHOR;
    public static final ModConfigSpec.ConfigValue<ManaBarOverlay.Display> MANA_BAR_DISPLAY;
    public static final ModConfigSpec.ConfigValue<ManaBarOverlay.Display> SPELL_BAR_DISPLAY; //reusing same enum
    public static final ModConfigSpec.ConfigValue<Integer> SPELL_BAR_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> SPELL_BAR_X_OFFSET;
    public static final ModConfigSpec.ConfigValue<SpellBarOverlay.Anchor> SPELL_BAR_ANCHOR;

    public static final ModConfigSpec.ConfigValue<RecastOverlay.Anchor> RECAST_ANCHOR;
    public static final ModConfigSpec.ConfigValue<Integer> RECAST_Y_OFFSET;
    public static final ModConfigSpec.ConfigValue<Integer> RECAST_X_OFFSET;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("#######################################################################################################################");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##   ATTENTION: These are client configs. For gameplay settings, go to the SERVER CONFIGS (in the world save file)   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("##                                                                                                                   ##");
        BUILDER.comment("#######################################################################################################################");
        BUILDER.comment("");

        BUILDER.push("UI");
        BUILDER.push("ManaBar");
        BUILDER.comment("By default (Contextual), the mana bar only appears when you are holding a magic item or are not at max mana.");
        MANA_BAR_DISPLAY = BUILDER.defineEnum("manaBarDisplay", ManaBarOverlay.Display.Contextual);
        BUILDER.comment("Used to adjust mana bar's position (11 is one full hunger bar up).");
        MANA_BAR_X_OFFSET = BUILDER.define("manaBarXOffset", 0);
        MANA_BAR_Y_OFFSET = BUILDER.define("manaBarYOffset", 0);
        MANA_BAR_TEXT_VISIBLE = BUILDER.define("manaBarTextVisible", true);
        MANA_BAR_ANCHOR = BUILDER.defineEnum("manaBarAnchor", ManaBarOverlay.Anchor.Hunger);
        MANA_TEXT_X_OFFSET = BUILDER.define("manaTextXOffset", 0);
        MANA_TEXT_Y_OFFSET = BUILDER.define("manaTextYOffset", 0);
        BUILDER.pop();
        BUILDER.push("SpellBar");
        BUILDER.comment("By default (Always), the spell bar always shows the spells in your equipped spellbook. Contextual will hide them when not in use.");
        SPELL_BAR_DISPLAY = BUILDER.defineEnum("spellBarDisplay", ManaBarOverlay.Display.Always);
        BUILDER.comment("Used to adjust spell bar's position.");
        SPELL_BAR_X_OFFSET = BUILDER.define("spellBarXOffset", 0);
        SPELL_BAR_Y_OFFSET = BUILDER.define("spellBarYOffset", 0);
        SPELL_BAR_ANCHOR = BUILDER.defineEnum("spellBarAnchor", SpellBarOverlay.Anchor.Hotbar);
        BUILDER.pop();
        BUILDER.push("RecastOverlay");
        RECAST_ANCHOR = BUILDER.defineEnum("recastAnchor", RecastOverlay.Anchor.TopCenter);
        RECAST_X_OFFSET = BUILDER.define("recastXOffset", 0);
        RECAST_Y_OFFSET = BUILDER.define("recastYOffset", 0);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Animations");
        BUILDER.comment("What to render in first person while casting.");
        SHOW_FIRST_PERSON_ARMS = BUILDER.define("showFirstPersonArms", true);
        SHOW_FIRST_PERSON_ITEMS = BUILDER.define("showFirstPersonItems", true);
        BUILDER.pop();

        BUILDER.push("Renderers");
        BUILDER.comment("By default, both fireballs are replaced with an enhanced model used by fire spells.");
        REPLACE_GHAST_FIREBALL = BUILDER.define("replaceGhastFireballs", true);
        REPLACE_BLAZE_FIREBALL = BUILDER.define("replaceBlazeFireballs", true);
        BUILDER.pop();

        BUILDER.push("Music");
        ENABLE_BOSS_MUSIC = BUILDER.define("enableBossMusic", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
