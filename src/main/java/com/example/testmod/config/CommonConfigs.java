package com.example.testmod.config;

import com.example.testmod.spells.SpellRarity;
import com.example.testmod.spells.SpellType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class CommonConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final SpellConfigParameters DEFAULT_CONFIG = new SpellConfigParameters(10,SpellRarity.COMMON);
    //https://forge.gemwire.uk/wiki/Configs

    private static final Map<SpellType, SpellConfigParameters> SPELL_CONFIGS;
    private static final Queue<DelayedConfigConstructor> CONFIG_QUEUE;

    static {
        SPELL_CONFIGS = new HashMap<>();
        CONFIG_QUEUE = new LinkedList<>();
        BUILDER.comment("Individual Spell Configurations.");

        createEntry(SpellType.FIREBALL_SPELL, 100, SpellRarity.LEGENDARY);
        createEntry(SpellType.ELECTROCUTE_SPELL, 1000, SpellRarity.LEGENDARY);


        SPEC = BUILDER.build();
    }

    public static SpellConfigParameters getByType(SpellType spellType) {
        return SPELL_CONFIGS.getOrDefault(spellType, DEFAULT_CONFIG);
    }

    public static SpellConfigParameters getById(int spellId) {
        return getByType(SpellType.getTypeFromValue(spellId));
    }

    private static void createEntry(SpellType spell, int defaultMaxLevel, SpellRarity defaultMinRarity) {
        BUILDER.push(spell.getId().substring(0, 1).toUpperCase() + spell.getId().substring(1));

        CONFIG_QUEUE.add(new DelayedConfigConstructor(
                BUILDER.define("Max Level", defaultMaxLevel),
                BUILDER.define("Min Rarity", defaultMinRarity),
                spell
        ));

        BUILDER.pop();
    }

    public static void resolveQueue() {
        while (!CONFIG_QUEUE.isEmpty())
            CONFIG_QUEUE.remove().construct();
    }

    //TODO: is this being static going to fuck shit up? (seems to work fine...)
    private static class DelayedConfigConstructor {
        final ForgeConfigSpec.ConfigValue<Integer> MAX_LEVEL;
        final ForgeConfigSpec.ConfigValue<SpellRarity> MIN_RARITY;
        SpellType spellType;

        DelayedConfigConstructor(ForgeConfigSpec.ConfigValue<Integer> MAX_LEVEL, ForgeConfigSpec.ConfigValue<SpellRarity> MIN_RARITY, SpellType spellType) {
            this.MAX_LEVEL = MAX_LEVEL;
            this.MIN_RARITY = MIN_RARITY;
            this.spellType = spellType;
        }

        void construct() {
            SPELL_CONFIGS.put(spellType, new SpellConfigParameters(MAX_LEVEL.get(), MIN_RARITY.get()));
        }
    }

}
