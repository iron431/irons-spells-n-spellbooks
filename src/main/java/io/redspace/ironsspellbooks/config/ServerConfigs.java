package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final SpellConfigParameters DEFAULT_CONFIG = new SpellConfigParameters(null, () -> true, SchoolRegistry.EVOCATION_RESOURCE::toString, () -> 10, () -> SpellRarity.COMMON, () -> 1d, () -> 1d, () -> 10d, () -> true);
    public static final ModConfigSpec.ConfigValue<Boolean> SWORDS_CONSUME_MANA;
    public static final ModConfigSpec.ConfigValue<Double> SWORDS_CD_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<Boolean> CAN_ATTACK_OWN_SUMMONS;
    public static final ModConfigSpec.ConfigValue<Integer> MAX_UPGRADES;
    public static final ModConfigSpec.ConfigValue<Double> MANA_SPAWN_PERCENT;
    public static final ModConfigSpec.ConfigValue<Double> SCROLL_RECYCLE_CHANCE;
    public static final ModConfigSpec.ConfigValue<Boolean> SCROLL_MERGING;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> UPGRADE_WHITELIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> UPGRADE_BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> IMBUE_WHITELIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> IMBUE_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<Integer> PRIEST_TOWER_SPAWNRATE;
    public static final ModConfigSpec.ConfigValue<Boolean> AQUIFER_DETECTION;
    public static final ModConfigSpec.ConfigValue<Boolean> ALLOW_CAULDRON_BREWING;
    public static final ModConfigSpec.ConfigValue<Boolean> FURLED_MAPS_SKIP_CHUNKS;
    public static final ModConfigSpec.ConfigValue<Boolean> APPLY_ALL_MULTIHAND_ATTRIBUTES;
    public static final ModConfigSpec.ConfigValue<Boolean> BETTER_CREEPER_THUNDERHIT;
    public static final ModConfigSpec.ConfigValue<Boolean> SPELL_GREIFING;
    public static final ModConfigSpec.ConfigValue<Boolean> ADDITIONAL_WANDERING_TRADER_TRADES;
    public static final ModConfigSpec.ConfigValue<Boolean> DISABLE_ADVENTURE_MODE_CASTING;
    public static final ModConfigSpec.ConfigValue<Boolean> HOGLIN_OFFSPRING_PROTECTION;
    public static final ModConfigSpec.ConfigValue<Double> MANA_REGEN_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<Boolean> CREATIVE_MANA_COST;
    public static final ModConfigSpec.ConfigValue<Boolean> CREATIVE_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<Boolean> ICE_SPIDER_PATROLS;
    public static final ModConfigSpec.ConfigValue<Boolean> TYROS_OMINOUS_FIGHT;

    public static final ModConfigSpec.ConfigValue<Boolean> PORTAL_FRAME_RESTRICT_DYE;
    public static final ModConfigSpec.ConfigValue<Boolean> PORTAL_FRAME_RESTRICT_BREAKING;


    public static final ModConfigSpec.ConfigValue<Double> TYROS_ADDITIONAL_HEALTH;
    public static final ModConfigSpec.ConfigValue<Double> TYROS_ADDITIONAL_ATTACK_DAMAGE;
    public static final ModConfigSpec.ConfigValue<Double> TYROS_ADDITIONAL_SPELL_POWER;
    public static final ModConfigSpec.ConfigValue<Double> DEAD_KING_ADDITIONAL_HEALTH;
    public static final ModConfigSpec.ConfigValue<Double> DEAD_KING_ADDITIONAL_ATTACK_DAMAGE;
    public static final ModConfigSpec.ConfigValue<Double> DEAD_KING_ADDITIONAL_SPELL_POWER;

    //public static final ModConfigSpec.ConfigValue<String[]> UPGRADE_BLACKLIST;

    public static final ModConfigSpec.ConfigValue<List<? extends Double>> RARITY_CONFIG;
    public static final Set<Item> UPGRADE_WHITELIST_ITEMS = new HashSet<>();
    public static final Set<Item> UPGRADE_BLACKLIST_ITEMS = new HashSet<>();
    public static final Set<Item> IMBUE_WHITELIST_ITEMS = new HashSet<>();
    public static final Set<Item> IMBUE_BLACKLIST_ITEMS = new HashSet<>();

    static {
        BUILDER.comment("##############################################################################################");
        BUILDER.comment("##                                                                                          ##");
        BUILDER.comment("##                                      ATTENTION:                                          ##");
        BUILDER.comment("##           If you are looking for spell configs, they are now datapack driven!            ##");
        BUILDER.comment("##                     Use '/ironsSpellbooks config' in-game for hints!                     ##");
        BUILDER.comment("##                                                                                          ##");
        BUILDER.comment("##                                                                                          ##");
        BUILDER.comment("##############################################################################################");
        BUILDER.comment("");
        BUILDER.comment("Other Configuration");
        {
            BUILDER.push("Blocks");
            PORTAL_FRAME_RESTRICT_DYE = BUILDER.comment("Whether Portal Frames can only be dyed by the block's owner. Default: true").define("portalFrameRestrictDye", true);
            PORTAL_FRAME_RESTRICT_BREAKING = BUILDER.comment("Whether Portal Frames can only be destroyed by the block's owner. Default: false").define("portalFrameRestrictBreaking", false);
            BUILDER.pop();
        }
        {
            BUILDER.push("Misc");

            RARITY_CONFIG = BUILDER.worldRestart()
                    .comment("Defines percentage brackets of spell level to corresponding rarity, ie first 30% of spell levels are common.")
                    .comment(String.format("rarityConfig array values must sum to 1: [%s, %s, %s, %s, %s]. Default: [.3d, .25d, .2d, .15d, .1d]", SpellRarity.COMMON, SpellRarity.UNCOMMON, SpellRarity.RARE, SpellRarity.EPIC, SpellRarity.LEGENDARY))
                    .defineList("rarityConfig", List.of(.3d, .25d, .2d, .15d, .1d), x -> true);

            BUILDER.comment("Whether or not imbued weapons require mana to be casted. Default: true");
            SWORDS_CONSUME_MANA = BUILDER.define("swordsConsumeMana", true);
            BUILDER.comment("The multiplier on the cooldown of imbued weapons. Default: 0.5 (50% of default cooldown)");
            SWORDS_CD_MULTIPLIER = BUILDER.define("swordsCooldownMultiplier", .5);
            BUILDER.comment("Whether or not players can harm their own magic summons. Default: false");
            CAN_ATTACK_OWN_SUMMONS = BUILDER.define("canAttackOwnSummons", false);
            BUILDER.comment("The maximum amount of times an applicable piece of equipment can be upgraded in the arcane anvil. Default: 3");
            MAX_UPGRADES = BUILDER.define("maxUpgrades", 3);
            BUILDER.comment("From 0-1, the percent of max mana a player respawns with. Default: 0.0");
            MANA_SPAWN_PERCENT = BUILDER.define("manaSpawnPercent", 0.0);
            BUILDER.comment("From 0-1, the percent chance for scrolls to be successfully recycled. Default: 0.5 (50%)");
            SCROLL_RECYCLE_CHANCE = BUILDER.define("scrollRecycleChance", 0.5);
            BUILDER.comment("Whether or not potions should be allowed to be brewed in the alchemist cauldron)");
            ALLOW_CAULDRON_BREWING = BUILDER.define("allowCauldronBrewing", true);
            BUILDER.comment("Whether or not Furled Map items should skip chunks when searching for structures (only find new structures). Can impact performance while searching. Default: true");
            FURLED_MAPS_SKIP_CHUNKS = BUILDER.define("furledMapSkipsExistingChunks", true);
            BUILDER.comment("[Deprecated] Whether or not casting items should apply all attributes while in the offhand, or just magic related ones. Default: true");
            APPLY_ALL_MULTIHAND_ATTRIBUTES = BUILDER.define("applyAllMultihandAttributes", true);
            BUILDER.comment("Whether or not creepers should be healed and become fire immune when struck by lightning. Default: true");
            BETTER_CREEPER_THUNDERHIT = BUILDER.define("betterCreeperThunderHit", true);
            BUILDER.comment("Whether or not spells such as Fireball or Fire Breath should destroy terrain or create fire. Default: false");
            SPELL_GREIFING = BUILDER.define("spellGriefing", false);
            BUILDER.comment("Whether or not the wandering trader can have magic related trades, such as ink or scrolls. Default: true");
            ADDITIONAL_WANDERING_TRADER_TRADES = BUILDER.define("additionalWanderingTraderTrades", true);
            BUILDER.comment("Whether casting spells should be disabled in adventure mode. Default: false");
            DISABLE_ADVENTURE_MODE_CASTING = BUILDER.define("disableAdventureModeCasting", false);
            BUILDER.comment("Whether hoglins have the ability to pass overworld zombification immunity to their offspring. Default: true");
            HOGLIN_OFFSPRING_PROTECTION = BUILDER.define("hoglinOffspringProtection", true);
            BUILDER.comment("Global multiplier to all players' mana regeneration. Default: 1.0");
            MANA_REGEN_MULTIPLIER = BUILDER.define("manaRegenMultiplier", 1.0);
            BUILDER.comment("Whether merging scrolls with ink to upgrade them in the Arcane Anvil is enabled.");
            SCROLL_MERGING = BUILDER.define("scrollMerging", true);
            BUILDER.comment("Whether mana is required in creative mode. Default: false");
            CREATIVE_MANA_COST = BUILDER.define("creativeMana", false);
            BUILDER.comment("Whether cooldowns are respected in creative mode. Default: false");
            CREATIVE_COOLDOWN = BUILDER.define("creativeCooldowns", false);
            BUILDER.pop();
        }

        {
            BUILDER.push("Upgrade Overrides");
            BUILDER.comment("Use these lists to change what items can interact with the Arcane Anvil's upgrade system. This can also be done via datapack.");
            BUILDER.comment("Upgrade Whitelist. Use an item's id to allow it to be upgraded, ex: \"minecraft:iron_sword\"");
            UPGRADE_WHITELIST = BUILDER.defineListAllowEmpty("upgradeWhitelist", ArrayList::new, (string) -> true);
            BUILDER.comment("Upgrade Blacklist. Use an item's id to prevent it from being upgraded, ex: \"minecraft:iron_sword\"");
            UPGRADE_BLACKLIST = BUILDER.defineListAllowEmpty("upgradeBlacklist", ArrayList::new, (string) -> true);
            BUILDER.pop();
        }

        {
            BUILDER.push("Imbue Overrides");
            BUILDER.comment("Use these lists to change what items can interact with the Arcane Anvil's imbue system.");
            BUILDER.comment("/!\\ Unsupported item types are not guaranteed to work out of the box.");
            BUILDER.comment("Imbue Whitelist. Use an item's id to allow it to be imbued, ex: \"minecraft:iron_sword\"");
            IMBUE_WHITELIST = BUILDER.defineListAllowEmpty("imbueWhitelist", ArrayList::new, (string) -> true);
            BUILDER.comment("Imbue Blacklist. Use an item's id to prevent it from being imbued, ex: \"minecraft:iron_sword\"");
            IMBUE_BLACKLIST = BUILDER.defineListAllowEmpty("imbueBlacklist", ArrayList::new, (string) -> true);
            BUILDER.pop();
        }

        {
            BUILDER.push("Worldgen");
            PRIEST_TOWER_SPAWNRATE = BUILDER.comment("The weight of the priest house spawning in a village. Default: 4")
                    .define("priestHouseWeight", 4);
            AQUIFER_DETECTION = BUILDER.comment("Whether to prevent aquifers from intersecting designated underground structures. May affect performance. Default: true")
                    .define("aquiferDetection", true);
            ICE_SPIDER_PATROLS = BUILDER.comment("Whether to enabled Ice Spider patrols in snowy biomes during snowy weather. Default: true")
                    .define("iceSpiderPatrols", true);
            BUILDER.pop();
        }

        {
            BUILDER.push("Boss Config");
            BUILDER.comment("Configure Boss Stats");
            {
                BUILDER.push("Tyros");
                TYROS_ADDITIONAL_HEALTH = BUILDER.comment("Additional Health").define("additionalHealth", 0.0);
                TYROS_ADDITIONAL_ATTACK_DAMAGE = BUILDER.comment("Additional Melee Attack Damage").define("additionalAttackDamage", 0.0);
                TYROS_ADDITIONAL_SPELL_POWER = BUILDER.comment("Additional Spell Power (additive percent)").define("additionalSpellPower", 0.0);
                TYROS_OMINOUS_FIGHT = BUILDER.comment("[Experimental] Whether Tyros has an Ominous Bossfight. Default: false").define("tyrosOminousEnabled", false);
                BUILDER.pop();
            }
            {
                BUILDER.push("Dead King");
                DEAD_KING_ADDITIONAL_HEALTH = BUILDER.comment("Additional Health").define("additionalHealth", 0.0);
                DEAD_KING_ADDITIONAL_ATTACK_DAMAGE = BUILDER.comment("Additional Melee Attack Damage").define("additionalAttackDamage", 0.0);
                DEAD_KING_ADDITIONAL_SPELL_POWER = BUILDER.comment("Additional Spell Power (additive percent)").define("additionalSpellPower", 0.0);
                BUILDER.pop();
            }
            BUILDER.pop();
        }

        SPEC = BUILDER.build();
    }

    public static void onConfigReload() {
        IronsSpellbooks.LOGGER.debug("ServerConfigs load item blacklists:");
        cacheItemList(UPGRADE_WHITELIST.get(), UPGRADE_WHITELIST_ITEMS);
        cacheItemList(UPGRADE_BLACKLIST.get(), UPGRADE_BLACKLIST_ITEMS);
        cacheItemList(IMBUE_WHITELIST.get(), IMBUE_WHITELIST_ITEMS);
        cacheItemList(IMBUE_BLACKLIST.get(), IMBUE_BLACKLIST_ITEMS);
    }

    private static void cacheItemList(List<? extends String> ids, Set<Item> output) {
        output.clear();
        for (String name : ids) {
            try {
                if (name.startsWith("#")) {
                    var tag = new TagKey<Item>(Registries.ITEM, ResourceLocation.parse(name.substring(1)));
                    output.addAll(BuiltInRegistries.ITEM.stream().filter(item -> item.builtInRegistryHolder().is(tag)).toList());
                } else {
                    var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(name));
                    if (item != null) {
                        output.add(item);
                    } else {
                        IronsSpellbooks.LOGGER.warn("Unable to add item to config, no such item id: {}", name);
                    }
                }
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.warn("Unable to validate item config: {}", e.getMessage());
            }
        }
    }
}
