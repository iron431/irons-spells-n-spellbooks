package com.example.testmod.config;

import com.example.testmod.spells.SpellRarity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

//    public static final ForgeConfigSpec.ConfigValue<List<SpellConfigParameters>> SPELL_PARAMETERS;

    //https://forge.gemwire.uk/wiki/Configs

    static {
        BUILDER.push("Common Configs");

//        SPELL_PARAMETERS = BUILDER.comment("Individual Spell Configurations.")
//                .comment("Format = SPELL_NAME: [MAX LEVEL, MIN RARITY]")
//                        .define()


        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private class SpellConfigParameters {
        final int MAX_LEVEL;
        final SpellRarity MIN_RARITY;

        //Not implemented:
        final boolean ENABLED;
        final int BASE_MANA_COST;
        final int MANA_COST_PER_LEVEL;
        final int BASE_POWER;
        final int POWER_PER_LEVEL;

        SpellConfigParameters(int MAX_LEVEL, SpellRarity MIN_RARITY) {
            this.MAX_LEVEL = MAX_LEVEL;
            this.MIN_RARITY = MIN_RARITY;

            this.ENABLED = true;
            this.BASE_MANA_COST = 0;
            this.MANA_COST_PER_LEVEL = 0;
            this.BASE_POWER = 0;
            this.POWER_PER_LEVEL = 0;
        }
    }
}
