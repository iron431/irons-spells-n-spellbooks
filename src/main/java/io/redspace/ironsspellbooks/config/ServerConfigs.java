package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerConfigs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final SpellConfigParameters DEFAULT_CONFIG = new SpellConfigParameters(null, () -> true, SchoolRegistry.EVOCATION_RESOURCE::toString, () -> 10, () -> SpellRarity.COMMON, () -> 1d, () -> 1d, () -> 10d);
    public static final ForgeConfigSpec.ConfigValue<Boolean> SWORDS_CONSUME_MANA;
    public static final ForgeConfigSpec.ConfigValue<Double> SWORDS_CD_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CAN_ATTACK_OWN_SUMMONS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_UPGRADES;
    public static final ForgeConfigSpec.ConfigValue<Double> MANA_SPAWN_PERCENT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RUN_WORLD_UPGRADER;
    public static final ForgeConfigSpec.ConfigValue<Double> SCROLL_RECYCLE_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UPGRADE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UPGRADE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IMBUE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IMBUE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<Integer> PRIEST_TOWER_SPAWNRATE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_CAULDRON_BREWING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FURLED_MAPS_SKIP_CHUNKS;

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
        BUILDER.comment("The multiplier on the cooldown of imbued weapons. Default: 0.5 (50% of default cooldown)");
        SWORDS_CD_MULTIPLIER = BUILDER.worldRestart().define("swordsCooldownMultiplier", .5);
        BUILDER.comment("Whether or not players can harm their own magic summons. Default: false");
        CAN_ATTACK_OWN_SUMMONS = BUILDER.worldRestart().define("canAttackOwnSummons", false);
        BUILDER.comment("The maximum amount of times an applicable piece of equipment can be upgraded in the arcane anvil. Default: 3");
        MAX_UPGRADES = BUILDER.worldRestart().define("maxUpgrades", 3);
        BUILDER.comment("From 0-1, the percent of max mana a player respawns with. Default: 0.0");
        MANA_SPAWN_PERCENT = BUILDER.worldRestart().define("manaSpawnPercent", 0.0);
        BUILDER.comment("If true the world will attempt to be upgraded from an older version of ISS");
        RUN_WORLD_UPGRADER = BUILDER.worldRestart().define("runWorldUpgrader", true);
        BUILDER.comment("From 0-1, the percent chance for scrolls to be successfully recycled. Default: 0.5 (50%)");
        SCROLL_RECYCLE_CHANCE = BUILDER.worldRestart().define("scrollRecycleChance", 0.5);
        BUILDER.comment("Whether or not potions should be allowed to be brewed in the alchemist cauldron)");
        ALLOW_CAULDRON_BREWING = BUILDER.worldRestart().define("allowCauldronBrewing", true);
        BUILDER.comment("Whether or not Furled Map items should skip chunks when searching for structures (only find new structures). Can impact performance while searching. Default: true");
        FURLED_MAPS_SKIP_CHUNKS = BUILDER.worldRestart().define("furledMapSkipsExistingChunks", true);
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

        SpellDiscovery.getSpellsForConfig()
                .stream()
                .collect(Collectors.groupingBy(x -> x.getDefaultConfig().schoolResource))
                .forEach((school, spells) -> {
                    BUILDER.comment(school.toString());
                    spells.forEach(ServerConfigs::createSpellConfig);
                });

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static SpellConfigParameters getSpellConfig(AbstractSpell abstractSpell) {
        //IronsSpellbooks.LOGGER.debug("CFG: getSpellConfig {} {}", spellType, SPELL_CONFIGS.containsKey(spellType));
        return SPELL_CONFIGS.getOrDefault(abstractSpell.getSpellId(), DEFAULT_CONFIG);
    }

    public static Map<String, SpellConfigParameters> getSpellConfigs() {
        return SPELL_CONFIGS;
    }
//TODO:
//    private static boolean validateItemName(final Object obj) {
//        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
//    }
//    @SubscribeEvent
//    static void onLoad(final ModConfigEvent event) {
//        // convert the list of strings into a set of items
//        items = ITEM_STRINGS.get().stream()
//                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
//                .collect(Collectors.toSet());
//    }
//
    private static void createSpellConfig(AbstractSpell spell) {
        DefaultConfig config = spell.getDefaultConfig();
        //IronsSpellbooks.LOGGER.debug("CFG: createSpellConfig");
        BUILDER.push(spell.getSpellId());

        SPELL_CONFIGS.put(spell.getSpellId(), new SpellConfigParameters(
                config,
                BUILDER.define("Enabled", config.enabled),
                BUILDER.define("School", config.schoolResource.toString()),
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
        //why did i do all this manually why isnt it a record :D
        final Supplier<Boolean> ENABLED;
        final Supplier<String> SCHOOL;
        final LazyOptional<SchoolType> ACTUAL_SCHOOL;
        final Supplier<Integer> MAX_LEVEL;
        final Supplier<SpellRarity> MIN_RARITY;
        final Supplier<Double> M_MULT;
        final Supplier<Double> P_MULT;
        final Supplier<Double> CS;

        SpellConfigParameters(
                DefaultConfig defaultConfig,
                Supplier<Boolean> ENABLED,
                Supplier<String> SCHOOL,
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
            this.ACTUAL_SCHOOL = LazyOptional.of(() -> {
                if (ResourceLocation.isValidResourceLocation(SCHOOL.get())) {
                    var school = SchoolRegistry.getSchool(new ResourceLocation(SCHOOL.get()));
                    if (school != null) {
                        return school;
                    }
                }
                IronsSpellbooks.LOGGER.warn("Bad school config entry: {}. Reverting to default ({}).", SCHOOL.get(), defaultConfig.schoolResource);
                return SchoolRegistry.getSchool(defaultConfig.schoolResource);
            });
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
            return ACTUAL_SCHOOL.resolve().get();
        }
    }

}
