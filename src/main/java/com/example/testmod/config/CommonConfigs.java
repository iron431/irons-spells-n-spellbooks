package com.example.testmod.config;

import com.example.testmod.spells.SpellRarity;
import com.example.testmod.spells.SpellType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CommonConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final SpellConfigParameters DEFAULT_CONFIG = new SpellConfigParameters(10, SpellRarity.COMMON);
    public static final ForgeConfigSpec.ConfigValue<Boolean> SWORDS_CONSUME_MANA;

    //https://forge.gemwire.uk/wiki/Configs

    private static final Map<SpellType, SpellConfigParameters> SPELL_CONFIGS = new HashMap<>();
    private static final Queue<DelayedConfigConstructor> CONFIG_LIST = new LinkedList<>();

    static {
        BUILDER.comment("Individual Spell Configurations.");

        //TODO: Fill out all spells with real values
        createSpellConfigEntry(SpellType.FIREBALL_SPELL, 100, SpellRarity.LEGENDARY);
        createSpellConfigEntry(SpellType.FIREBOLT_SPELL, 15, SpellRarity.UNCOMMON);
        createSpellConfigEntry(SpellType.MAGIC_MISSILE_SPELL, 1, SpellRarity.UNCOMMON);
        createSpellConfigEntry(SpellType.ELECTROCUTE_SPELL, 1000, SpellRarity.LEGENDARY);
        createSpellConfigEntry(SpellType.ICICLE_SPELL, 10, SpellRarity.EPIC);

        BUILDER.comment("Misc.");
        SWORDS_CONSUME_MANA = BUILDER.push(createTitle("Do swords consume mana")).define("Consume Mana", true);

        SPEC = BUILDER.build();
    }

    public static SpellConfigParameters getByType(SpellType spellType) {
        return SPELL_CONFIGS.getOrDefault(spellType, DEFAULT_CONFIG);
    }

    public static SpellConfigParameters getById(int spellId) {
        return getByType(SpellType.getTypeFromValue(spellId));
    }

    private static void createSpellConfigEntry(SpellType spell, int defaultMaxLevel, SpellRarity defaultMinRarity) {

        BUILDER.push(createTitle(spell.getId()));

        CONFIG_LIST.add(new DelayedConfigConstructor(
                BUILDER.define("MaxLevel", defaultMaxLevel),
                BUILDER.defineEnum("MinRarity", defaultMinRarity),
                spell
        ));

        BUILDER.pop();
    }

    private static String createTitle(String str) {
        var words = str.split("[_| ]");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return Arrays.stream(words).sequential().collect(Collectors.joining("-"));
    }

    public static void resolveQueue() {
        while (!CONFIG_LIST.isEmpty())
            CONFIG_LIST.remove().construct();
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
