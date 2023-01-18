package com.example.testmod.config;

import com.example.testmod.spells.SpellRarity;
import com.example.testmod.spells.SpellType;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    //public static final List<SpellConfigParameters> SPELL_PARAMETERS;
    //public static final ForgeConfigSpec.ConfigValue<SpellConfigParameters> FIREBALL;
    //https://forge.gemwire.uk/wiki/Configs

    static {
        final SpellConfigParameters DEFAULT_FIREBALL = new SpellConfigParameters(3, SpellRarity.UNCOMMON);
        //SPELL_PARAMETERS = new ArrayList<>();
        BUILDER.push("Common Configs");
//        List<SpellConfigParameters> spellParameters = new ArrayList<>();

        var spells = SpellType.values();
        BUILDER.comment("Individual Spell Configurations.").comment("Format = SPELL_NAME: [MAX LEVEL, MIN RARITY]").comment("Common = 0\nUncommon = 1\nRare = 2\nEpic = 3\nLegendary = 4");
        BUILDER.pop();
//        spellParameters.add(toSpellConfigParameter(BUILDER.defineList(spells[1].toString().toLowerCase(),
//                DEFAULT_FIREBALL.asList(),
//                entry -> true).get()
//        ));

        //SPELL_PARAMETERS = new ForgeConfigSpec.ConfigValue<List<SpellConfigParameters>>(spellParameters) ;
        BUILDER.push("Fireball");
        BUILDER.define("Max Level",10);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    static SpellConfigParameters toSpellConfigParameter(List<? extends Integer> parameters) {
        return new SpellConfigParameters(parameters.get(0), SpellRarity.values()[parameters.get(1)]);
    }

}
