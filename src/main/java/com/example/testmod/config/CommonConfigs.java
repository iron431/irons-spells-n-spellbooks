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

    //TODO: make an external app to deal with this file... forge config formatting is retarded... you (I) can't (figure out how to) create instance variables that aren't ConfigValues or reference an outside class with a non-ConfigValue variable
    //static final SpellConfigParameters DEFAULT_FIREBALL = new SpellConfigParameters(3, SpellRarity.UNCOMMON);
    //static final SpellConfigParameters FIREBALL;

    //public static final List<SpellConfigParameters> SPELL_PARAMETERS;
    //public static final ForgeConfigSpec.ConfigValue<SpellConfigParameters> FIREBALL;
    //https://forge.gemwire.uk/wiki/Configs

    //TODO: repeat x999999
    public static final ForgeConfigSpec.ConfigValue<Integer> fireball_max_level;
    public static final ForgeConfigSpec.ConfigValue<SpellRarity> fireball_min_rarity;
    static {

        //var spells = SpellType.values();
        BUILDER.comment("Individual Spell Configurations.");

//        FIREBALL = new SpellConfigParameters(
//                BUILDER.define("Max Level",10).get(),
//                BUILDER.define("Min Rarity",SpellRarity.UNCOMMON.getValue()).get()
//        );
        BUILDER.push("Fireball");
        fireball_max_level = BUILDER.define("Max Level", 10);
        fireball_min_rarity = BUILDER.define("Min Rarity", SpellRarity.UNCOMMON);
        BUILDER.pop();

        //buildSpell(777);


        SPEC = BUILDER.build();

        //var z = new SpellConfigParameters(x.get(),y.get().getValue());
    }
//    static ForgeConfigSpec.ConfigValue<Integer> buildSpell(int test){
//        BUILDER.push("buildSpell");
//        var x = BUILDER.define("value",test);
//        //FIREBALL
//        SpellConfigParameters.FIREBALL = new SpellConfigParameters(x.get(),x.get());
//        BUILDER.pop();
//        return x;
//
//    }
//    static SpellConfigParameters toSpellConfigParameter(List<? extends Integer> parameters) {
//        return new SpellConfigParameters(parameters.get(0), SpellRarity.values()[parameters.get(1)]);
//    }

}
