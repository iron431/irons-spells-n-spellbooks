package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.spells.DefaultConfig;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.SpellType;
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
    public static final ForgeConfigSpec.ConfigValue<Double> SWORDS_CD_MULTIPLIER;
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

    private static final Map<SpellType, SpellConfigParameters> SPELL_CONFIGS = new HashMap<>();

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

        //Blood
        BUILDER.comment("Blood Spells");
        createSpellConfig(SpellType.BLOOD_SLASH_SPELL, BloodSlashSpell.defaultConfig, true);
        createSpellConfig(SpellType.BLOOD_STEP_SPELL, BloodStepSpell.defaultConfig, true);
        createSpellConfig(SpellType.HEARTSTOP_SPELL, HeartstopSpell.defaultConfig, true);
        createSpellConfig(SpellType.RAISE_DEAD_SPELL, RaiseDeadSpell.defaultConfig, true);
        createSpellConfig(SpellType.RAY_OF_SIPHONING_SPELL, RayOfSiphoningSpell.defaultConfig, true);
        createSpellConfig(SpellType.WITHER_SKULL_SPELL, WitherSkullSpell.defaultConfig, true);
        createSpellConfig(SpellType.BlOOD_NEEDLES_SPELL, BloodNeedlesSpell.defaultConfig, true);
        createSpellConfig(SpellType.ACUPUNCTURE_SPELL, AcupunctureSpell.defaultConfig, true);
        createSpellConfig(SpellType.DEVOUR_SPELL, DevourSpell.defaultConfig, true);
        //Ender
        BUILDER.comment("Ender Spells");
        createSpellConfig(SpellType.EVASION_SPELL, EvasionSpell.defaultConfig, true);
        createSpellConfig(SpellType.MAGIC_ARROW_SPELL, MagicArrowSpell.defaultConfig, true);
        createSpellConfig(SpellType.MAGIC_MISSILE_SPELL, MagicMissileSpell.defaultConfig, true);
        createSpellConfig(SpellType.TELEPORT_SPELL, TeleportSpell.defaultConfig, true);
        createSpellConfig(SpellType.COUNTERSPELL_SPELL, CounterspellSpell.defaultConfig, true);
        createSpellConfig(SpellType.DRAGON_BREATH_SPELL, DragonBreathSpell.defaultConfig, true);
        createSpellConfig(SpellType.STARFALL_SPELL, StarfallSpell.defaultConfig, true);

        //Evocation
        BUILDER.comment("Evocation Spells");
        createSpellConfig(SpellType.CHAIN_CREEPER_SPELL, ChainCreeperSpell.defaultConfig, true);
        createSpellConfig(SpellType.FANG_STRIKE_SPELL, FangStrikeSpell.defaultConfig, true);
        createSpellConfig(SpellType.FANG_WARD_SPELL, FangWardSpell.defaultConfig, true);
        createSpellConfig(SpellType.FIRECRACKER_SPELL, FirecrackerSpell.defaultConfig, true);
        createSpellConfig(SpellType.INVISIBILITY_SPELL, InvisibilitySpell.defaultConfig, true);
        createSpellConfig(SpellType.LOB_CREEPER_SPELL, LobCreeperSpell.defaultConfig, true);
        createSpellConfig(SpellType.SHIELD_SPELL, ShieldSpell.defaultConfig, true);
        createSpellConfig(SpellType.SUMMON_HORSE_SPELL, SummonHorseSpell.defaultConfig, true);
        createSpellConfig(SpellType.SUMMON_VEX_SPELL, SummonVexSpell.defaultConfig, true);
        createSpellConfig(SpellType.SPECTRAL_HAMMER_SPELL, SpectralHammerSpell.defaultConfig, true);
        createSpellConfig(SpellType.GUST_SPELL, GustSpell.defaultConfig, true);
        //Fire
        BUILDER.comment("Fire Spells");
        createSpellConfig(SpellType.BLAZE_STORM_SPELL, BlazeStormSpell.defaultConfig, true);
        createSpellConfig(SpellType.BURNING_DASH_SPELL, BurningDashSpell.defaultConfig, true);
        createSpellConfig(SpellType.FIREBALL_SPELL, FireballSpell.defaultConfig, true);
        createSpellConfig(SpellType.FIREBOLT_SPELL, FireboltSpell.defaultConfig, true);
        createSpellConfig(SpellType.FIRE_BREATH_SPELL, FireBreathSpell.defaultConfig, true);
        createSpellConfig(SpellType.WALL_OF_FIRE_SPELL, WallOfFireSpell.defaultConfig, true);
        createSpellConfig(SpellType.MAGMA_BOMB_SPELL, MagmaBombSpell.defaultConfig, true);
        //Holy
        BUILDER.comment("Holy Spells");
        createSpellConfig(SpellType.ANGEL_WING_SPELL, AngelWingsSpell.defaultConfig, true);
        createSpellConfig(SpellType.CLOUD_OF_REGENERATION_SPELL, CloudOfRegenerationSpell.defaultConfig, false);
        createSpellConfig(SpellType.GREATER_HEAL_SPELL, GreaterHealSpell.defaultConfig, true);
        createSpellConfig(SpellType.HEAL_SPELL, HealSpell.defaultConfig, true);
        createSpellConfig(SpellType.WISP_SPELL, WispSpell.defaultConfig, true);
        createSpellConfig(SpellType.FORTIFY_SPELL, FortifySpell.defaultConfig, true);
        createSpellConfig(SpellType.BLESSING_OF_LIFE_SPELL, BlessingOfLifeSpell.defaultConfig, true);
        createSpellConfig(SpellType.HEALING_CIRCLE_SPELL, HealingCircleSpell.defaultConfig, true);
        createSpellConfig(SpellType.GUIDING_BOLT_SPELL, GuidingBoltSpell.defaultConfig, true);
        createSpellConfig(SpellType.SUNBEAM_SPELL, SunbeamSpell.defaultConfig, false);
        //Ice
        BUILDER.comment("Ice Spells");
        createSpellConfig(SpellType.CONE_OF_COLD_SPELL, ConeOfColdSpell.defaultConfig, true);
        createSpellConfig(SpellType.FROST_STEP_SPELL, FrostStepSpell.defaultConfig, true);
        createSpellConfig(SpellType.ICICLE_SPELL, IcicleSpell.defaultConfig, true);
        createSpellConfig(SpellType.SUMMON_POLAR_BEAR_SPELL, SummonPolarBearSpell.defaultConfig, true);
        createSpellConfig(SpellType.ICE_BLOCK_SPELL, IceBlockSpell.defaultConfig, true);
        createSpellConfig(SpellType.FROSTBITE_SPELL, FrostbiteSpell.defaultConfig, false);
        //Lightning
        BUILDER.comment("Lightning Spells");
        createSpellConfig(SpellType.ELECTROCUTE_SPELL, ElectrocuteSpell.defaultConfig, true);
        createSpellConfig(SpellType.LIGHTNING_BOLT_SPELL, LightningBoltSpell.defaultConfig, true);
        createSpellConfig(SpellType.LIGHTNING_LANCE_SPELL, LightningLanceSpell.defaultConfig, true);
        createSpellConfig(SpellType.CHARGE_SPELL, ChargeSpell.defaultConfig, true);
        createSpellConfig(SpellType.ASCENSION_SPELL, AscensionSpell.defaultConfig, true);
        createSpellConfig(SpellType.CHAIN_LIGHTNING_SPELL, ChainLightningSpell.defaultConfig, true);
        //Void
        BUILDER.comment("Void Spells");
        createSpellConfig(SpellType.ABYSSAL_SHROUD_SPELL, AbyssalShroudSpell.defaultConfig, true);
        createSpellConfig(SpellType.VOID_TENTACLES_SPELL, VoidTentaclesSpell.defaultConfig, true);
        createSpellConfig(SpellType.BLACK_HOLE_SPELL, BlackHoleSpell.defaultConfig, true);
        //Poison
        BUILDER.comment("Poison Spells");
        createSpellConfig(SpellType.POISON_ARROW_SPELL, PoisonArrowSpell.defaultConfig, true);
        createSpellConfig(SpellType.POISON_SPLASH_SPELL, PoisonSplashSpell.defaultConfig, true);
        createSpellConfig(SpellType.POISON_BREATH_SPELL, PoisonBreathSpell.defaultConfig, true);
        createSpellConfig(SpellType.ACID_ORB_SPELL, AcidOrbSpell.defaultConfig, true);
        createSpellConfig(SpellType.SPIDER_ASPECT_SPELL, SpiderAspectSpell.defaultConfig, true);
        createSpellConfig(SpellType.BLIGHT_SPELL, BlightSpell.defaultConfig, true);
        createSpellConfig(SpellType.ROOT_SPELL, RootSpell.defaultConfig, true);
        BUILDER.pop();


        SPEC = BUILDER.build();
    }

    public static SpellConfigParameters getSpellConfig(SpellType spellType) {
        //IronsSpellbooks.LOGGER.debug("CFG: getSpellConfig {} {}", spellType, SPELL_CONFIGS.containsKey(spellType));
        return SPELL_CONFIGS.getOrDefault(spellType, DEFAULT_CONFIG);
    }

    public static SpellConfigParameters getSpellConfig(int spellId) {
        //IronsSpellbooks.LOGGER.debug("CFG: getSpellConfig {}", spellId);
        return getSpellConfig(SpellType.getTypeFromValue(spellId));
    }

    public static Map<SpellType, SpellConfigParameters> getSpellConfigs() {
        return SPELL_CONFIGS;
    }

    private static void createSpellConfig(SpellType spell, DefaultConfig config, boolean enabledByDefault) {
        //IronsSpellbooks.LOGGER.debug("CFG: createSpellConfig");
        BUILDER.push(createSpellConfigTitle(spell.getId()));

        SPELL_CONFIGS.put(spell, new SpellConfigParameters(
                BUILDER.define("Enabled", enabledByDefault),
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
