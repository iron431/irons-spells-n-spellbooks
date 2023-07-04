package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
import io.redspace.ironsspellbooks.spells.DefaultConfig;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import io.redspace.ironsspellbooks.spells.blood.*;
import io.redspace.ironsspellbooks.spells.ender.*;
import io.redspace.ironsspellbooks.spells.evocation.*;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.ironsspellbooks.spells.holy.*;
import io.redspace.ironsspellbooks.spells.ice.*;
import io.redspace.ironsspellbooks.spells.lightning.*;
import io.redspace.ironsspellbooks.spells.poison.*;
import io.redspace.ironsspellbooks.spells.void_school.AbyssalShroudSpell;
import io.redspace.ironsspellbooks.spells.void_school.BlackHoleSpell;
import io.redspace.ironsspellbooks.spells.void_school.VoidTentaclesSpell;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final SpellConfigParameters DEFAULT_CONFIG = new SpellConfigParameters(() -> true, () -> SchoolType.EVOCATION, () -> 10, () -> SpellRarity.COMMON, () -> 1d, () -> 1d, () -> 10d);
    public static final ForgeConfigSpec.ConfigValue<Boolean> SWORDS_CONSUME_MANA;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CAN_ATTACK_OWN_SUMMONS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_UPGRADES;
    public static final ForgeConfigSpec.ConfigValue<Double> MANA_SPAWN_PERCENT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UPGRADE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UPGRADE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IMBUE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IMBUE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<Integer> PRIEST_TOWER_SPAWNRATE;

    //public static final ForgeConfigSpec.ConfigValue<String[]> UPGRADE_BLACKLIST;

    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> RARITY_CONFIG;

    //https://forge.gemwire.uk/wiki/Configs

    private static final Map<String, SpellConfigParameters> SPELL_CONFIGS = new HashMap<>();

    static {
        BUILDER.comment("Other Configuration");
        BUILDER.push("Misc");

        RARITY_CONFIG = BUILDER.worldRestart()
                .comment(String.format("rarityConfig array values must sum to 1: [%s, %s, %s, %s, %s]. Default: [.3d, .25d, .2d, .15d, .1d]", SpellRarity.COMMON, SpellRarity.UNCOMMON, SpellRarity.RARE, SpellRarity.EPIC, SpellRarity.LEGENDARY))
                .defineList("rarityConfig", List.of(.3d, .25d, .2d, .15d, .1d), x -> true);

        BUILDER.comment("Whether or not imbued weapons require mana to be casted. Default: true");
        SWORDS_CONSUME_MANA = BUILDER.worldRestart().define("swordsConsumeMana", true);
        BUILDER.comment("Whether or not players can harm their own magic summons. Default: false");
        CAN_ATTACK_OWN_SUMMONS = BUILDER.worldRestart().define("canAttackOwnSummons", false);
        BUILDER.comment("The maximum amount of times an applicable piece of equipment can be upgraded in the arcane anvil. Default: 3");
        MAX_UPGRADES = BUILDER.worldRestart().define("maxUpgrades", 3);
        BUILDER.comment("From 0-1, the percent of max mana a player respawns with. Default: 0.0");
        MANA_SPAWN_PERCENT = BUILDER.worldRestart().define("manaSpawnPercent", 0.0);
        BUILDER.pop();

        BUILDER.push("Upgrade Overrides");
        BUILDER.comment("Use these lists to change what items can interact with the Arcane Anvil's upgrade system. This can also be done via datapack.");
        BUILDER.comment("Upgrade Whitelist. Use an item's id to allow it to be upgraded, ex: \"minecraft:iron_sword\"");
        UPGRADE_WHITELIST = BUILDER.defineList("upgradeWhitelist", ArrayList::new, (string) -> true);
        BUILDER.comment("Upgrade Blacklist. Use an item's id to prevent it from being upgraded, ex: \"minecraft:iron_sword\"");
        UPGRADE_BLACKLIST = BUILDER.defineList("upgradeBlacklist", ArrayList::new, (string) -> true);
        BUILDER.pop();

        BUILDER.push("Imbue Overrides");
        BUILDER.comment("Use these lists to change what items can interact with the Arcane Anvil's imbue system.");
        BUILDER.comment("!THIS MAY HAVE UNINTENDED CONSEQUENCES!");
        BUILDER.comment("Upgrade Whitelist. Use an item's id to allow it to be imbued, ex: \"minecraft:iron_sword\"");
        IMBUE_WHITELIST = BUILDER.defineList("imbueWhitelist", ArrayList::new, (string) -> true);
        BUILDER.comment("Upgrade Blacklist. Use an item's id to prevent it from being imbued, ex: \"minecraft:iron_sword\"");
        IMBUE_BLACKLIST = BUILDER.defineList("imbueBlacklist", ArrayList::new, (string) -> true);
        BUILDER.pop();

        BUILDER.push("Worldgen");
        BUILDER.comment("The weight of the priest house spawning in a village. Default: 4");
        PRIEST_TOWER_SPAWNRATE = BUILDER.define("priestHouseWeight", 4);
        BUILDER.pop();
        //IronsSpellbooks.LOGGER.debug("CFG: static");
        BUILDER.comment("Individual Spell Configuration");
        BUILDER.push("Spells");

        SpellRegistry.REGISTRY.get().getValues()
                .stream()
                .collect(Collectors.groupingBy(x -> x.getDefaultConfig().school))
                .forEach((school, spells) -> {
                    BUILDER.comment(school.name());
                    spells.forEach(ServerConfigs::createSpellConfig);
                });

        BUILDER.pop();


        SPEC = BUILDER.build();
    }

    public static SpellConfigParameters getSpellConfig(AbstractSpell abstractSpell) {
        //IronsSpellbooks.LOGGER.debug("CFG: getSpellConfig {} {}", spellType, SPELL_CONFIGS.containsKey(spellType));
        return SPELL_CONFIGS.getOrDefault(abstractSpell.getSpellResource().toString(), DEFAULT_CONFIG);
    }

    public static Map<String, SpellConfigParameters> getSpellConfigs() {
        return SPELL_CONFIGS;
    }

    private static void createSpellConfig(AbstractSpell spell) {
        DefaultConfig config = spell.getDefaultConfig();
        //IronsSpellbooks.LOGGER.debug("CFG: createSpellConfig");
        BUILDER.push(spell.getSpellResource().toString());

        SPELL_CONFIGS.put(spell.getSpellResource().toString(), new SpellConfigParameters(
                BUILDER.define("Enabled", config.enabled),
                BUILDER.defineEnum("School", config.school),
                BUILDER.define("MaxLevel", config.maxLevel),
                BUILDER.defineEnum("MinRarity", config.minRarity),
                BUILDER.define("ManaCostMultiplier", 1d),
                BUILDER.define("SpellPowerMultiplier", 1d),
                BUILDER.define("CooldownInSeconds", config.cooldownInSeconds)
        ));

        BUILDER.pop();
    }

    private static String createSpellConfigTitle(String str) {
        var words = str.split("[_| ]");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        return Arrays.stream(words).sequential().collect(Collectors.joining("-"));
    }
//
//    public static void cacheConfigs() {
//        //IronsSpellbooks.LOGGER.debug("CFG: cacheConfigs {}", CONFIG_LIST.size());
//        while (!CONFIG_LIST.isEmpty())
//            CONFIG_LIST.remove().construct();
//    }

    public static class SpellConfigParameters {

        final Supplier<Boolean> ENABLED;
        final Supplier<SchoolType> SCHOOL;
        final Supplier<Integer> MAX_LEVEL;
        final Supplier<SpellRarity> MIN_RARITY;
        final Supplier<Double> M_MULT;
        final Supplier<Double> P_MULT;
        final Supplier<Double> CS;
        private SchoolType resolvedSchool = null;

        SpellConfigParameters(
                Supplier<Boolean> ENABLED,
                Supplier<SchoolType> SCHOOL,
                Supplier<Integer> MAX_LEVEL,
                Supplier<SpellRarity> MIN_RARITY,
                Supplier<Double> M_MULT,
                Supplier<Double> P_MULT,
                Supplier<Double> CS) {
            this.ENABLED = ENABLED;
            this.SCHOOL = SCHOOL;
            this.MAX_LEVEL = MAX_LEVEL;
            this.MIN_RARITY = MIN_RARITY;
            this.M_MULT = M_MULT;
            this.P_MULT = P_MULT;
            this.CS = CS;
        }

        public boolean enabled() {
            return ENABLED.get();
        }

        public int maxLevel() {
            return MAX_LEVEL.get();
        }

        public SpellRarity minRarity() {
            return MIN_RARITY.get();
        }

        public double powerMultiplier() {
            return P_MULT.get();
        }

        public double manaMultiplier() {
            return M_MULT.get();
        }

        public int cooldownInTicks() {
            return (int) (CS.get() * 20);
        }

        public SchoolType school() {
            return SCHOOL.get();
        }
    }

}
