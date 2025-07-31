//package io.redspace.ironsspellbooks.api.spells;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import io.redspace.ironsspellbooks.IronsSpellbooks;
//import io.redspace.ironsspellbooks.config.ServerConfigs;
//import io.redspace.ironsspellbooks.damage.DamageSources;
//import io.redspace.ironsspellbooks.spells.NoneSpell;
//import io.redspace.ironsspellbooks.api.spells.SchoolType;
//import io.redspace.ironsspellbooks.api.spells.SpellRarity;
//import io.redspace.ironsspellbooks.spells.blood.*;
//import io.redspace.ironsspellbooks.spells.ender.*;
//import io.redspace.ironsspellbooks.spells.evocation.*;
//import io.redspace.ironsspellbooks.spells.fire.*;
//import io.redspace.ironsspellbooks.spells.holy.*;
//import io.redspace.ironsspellbooks.spells.ice.*;
//import io.redspace.ironsspellbooks.spells.lightning.*;
//import io.redspace.ironsspellbooks.spells.poison.*;
//import io.redspace.ironsspellbooks.spells.void_school.AbyssalShroudSpell;
//import io.redspace.ironsspellbooks.spells.void_school.BlackHoleSpell;
//import io.redspace.ironsspellbooks.spells.void_school.VoidTentaclesSpell;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.item.UseAnim;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//
//public enum SpellType {
//    /**
//     * When adding spell, add:
//     * Spell Type
//     * SpellType.getCastType
//     * Translation
//     * Icon
//     * Sounds
//     * Particles
//     */
//    NONE_SPELL(0, NoneSpell::new),
//    FIREBALL_SPELL(1, FireballSpell::new),
//    BURNING_DASH_SPELL(2, BurningDashSpell::new),
//    TELEPORT_SPELL(3, TeleportSpell::new),
//    MAGIC_MISSILE_SPELL(4, MagicMissileSpell::new),
//    ELECTROCUTE_SPELL(5, ElectrocuteSpell::new),
//    CONE_OF_COLD_SPELL(6, ConeOfColdSpell::new),
//    HEAL_SPELL(7, HealSpell::new),
//    BLOOD_SLASH_SPELL(8, BloodSlashSpell::new),
//    SUMMON_VEX_SPELL(9, SummonVexSpell::new),
//    FIREBOLT_SPELL(10, FireboltSpell::new),
//    FIRE_BREATH_SPELL(11, FireBreathSpell::new),
//    ICICLE_SPELL(12, IcicleSpell::new),
//    FIRECRACKER_SPELL(13, FirecrackerSpell::new),
//    SUMMON_HORSE_SPELL(14, SummonHorseSpell::new),
//    ANGEL_WING_SPELL(15, AngelWingsSpell::new),
//    SHIELD_SPELL(16, ShieldSpell::new),
//    WALL_OF_FIRE_SPELL(17, WallOfFireSpell::new),
//    WISP_SPELL(18, WispSpell::new),
//    FANG_STRIKE_SPELL(19, FangStrikeSpell::new),
//    FANG_WARD_SPELL(20, FangWardSpell::new),
//    EVASION_SPELL(21, EvasionSpell::new),
//    HEARTSTOP_SPELL(22, HeartstopSpell::new),
//    LIGHTNING_LANCE_SPELL(23, LightningLanceSpell::new),
//    LIGHTNING_BOLT_SPELL(24, LightningBoltSpell::new),
//    RAISE_DEAD_SPELL(25, RaiseDeadSpell::new),
//    WITHER_SKULL_SPELL(26, WitherSkullSpell::new),
//    GREATER_HEAL_SPELL(27, GreaterHealSpell::new),
//    CLOUD_OF_REGENERATION_SPELL(28, CloudOfRegenerationSpell::new),
//    RAY_OF_SIPHONING_SPELL(29, RayOfSiphoningSpell::new),
//    MAGIC_ARROW_SPELL(30, MagicArrowSpell::new),
//    LOB_CREEPER_SPELL(31, LobCreeperSpell::new),
//    CHAIN_CREEPER_SPELL(32, ChainCreeperSpell::new),
//    BLAZE_STORM_SPELL(33, BlazeStormSpell::new),
//    FROST_STEP_SPELL(34, FrostStepSpell::new),
//    ABYSSAL_SHROUD_SPELL(35, AbyssalShroudSpell::new),
//    FROSTBITE_SPELL(36, FrostbiteSpell::new),
//    ASCENSION_SPELL(37, AscensionSpell::new),
//    INVISIBILITY_SPELL(38, InvisibilitySpell::new),
//    BLOOD_STEP_SPELL(39, BloodStepSpell::new),
//    SUMMON_POLAR_BEAR_SPELL(40, SummonPolarBearSpell::new),
//    BLESSING_OF_LIFE_SPELL(41, BlessingOfLifeSpell::new),
//    DRAGON_BREATH_SPELL(42, DragonBreathSpell::new),
//    FORTIFY_SPELL(43, FortifySpell::new),
//    COUNTERSPELL_SPELL(44, CounterspellSpell::new),
//    SPECTRAL_HAMMER_SPELL(45, SpectralHammerSpell::new),
//    CHARGE_SPELL(46, ChargeSpell::new),
//    VOID_TENTACLES_SPELL(47, VoidTentaclesSpell::new),
//    ICE_BLOCK_SPELL(48, IceBlockSpell::new),
//    POISON_BREATH_SPELL(49, PoisonBreathSpell::new),
//    POISON_ARROW_SPELL(50, PoisonArrowSpell::new),
//    POISON_SPLASH_SPELL(51, PoisonSplashSpell::new),
//    ACID_ORB_SPELL(52, AcidOrbSpell::new),
//    SPIDER_ASPECT_SPELL(53, SpiderAspectSpell::new),
//    BLIGHT_SPELL(54, BlightSpell::new),
//    ROOT_SPELL(55, RootSpell::new),
//    BLACK_HOLE_SPELL(56, BlackHoleSpell::new),
//    BlOOD_NEEDLES_SPELL(57, BloodNeedlesSpell::new),
//    ACUPUNCTURE_SPELL(58, AcupunctureSpell::new),
//    MAGMA_BOMB_SPELL(59, MagmaBombSpell::new),
//    STARFALL_SPELL(60, StarfallSpell::new),
//    HEALING_CIRCLE_SPELL(61, HealingCircleSpell::new),
//    GUIDING_BOLT_SPELL(62, GuidingBoltSpell::new),
//    SUNBEAM_SPELL(63, SunbeamSpell::new),
//    GUST_SPELL(64, GustSpell::new),
//    CHAIN_LIGHTNING_SPELL(65, ChainLightningSpell::new),
//    DEVOUR_SPELL(66, DevourSpell::new),
//    ;
//
//    private final int value;
//    private final int maxRarity;
//    private final GetSpellForType getSpellForType;
//    private volatile List<Double> rarityWeights;
//
//    SpellType(final int newValue, GetSpellForType getter) {
//        value = newValue;
//        maxRarity = SpellRarity.LEGENDARY.getValue();
//        getSpellForType = getter;
//    }
//
//    public int getValue() {
//        return value;
//    }
//
//    public int getMaxRarity() {
//        return maxRarity;
//    }
//
//    public int getMinLevel() {
//        return 1;
//    }
//
//    public static SpellType getTypeFromValue(int value) {
//        return SpellType.values()[value];
//    }
//
//    public CastType getCastType() {
//        return switch (this) {
//            case FIREBALL_SPELL, WISP_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL, SUMMON_VEX_SPELL, RAISE_DEAD_SPELL, GREATER_HEAL_SPELL, CHAIN_CREEPER_SPELL, INVISIBILITY_SPELL, SUMMON_POLAR_BEAR_SPELL, BLESSING_OF_LIFE_SPELL, FORTIFY_SPELL, VOID_TENTACLES_SPELL, SUMMON_HORSE_SPELL, ICE_BLOCK_SPELL, POISON_SPLASH_SPELL, BLIGHT_SPELL, ROOT_SPELL, HEALING_CIRCLE_SPELL, SUNBEAM_SPELL, LIGHTNING_LANCE_SPELL, MAGIC_ARROW_SPELL, POISON_ARROW_SPELL, ACID_ORB_SPELL, BLACK_HOLE_SPELL, MAGMA_BOMB_SPELL, GUST_SPELL ->
//                    CastType.LONG;
//            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL, CLOUD_OF_REGENERATION_SPELL, RAY_OF_SIPHONING_SPELL, BLAZE_STORM_SPELL, DRAGON_BREATH_SPELL, POISON_BREATH_SPELL, STARFALL_SPELL ->
//                    CastType.CONTINUOUS;
//            default -> CastType.INSTANT;
//        };
//    }
//
//    public UseAnim getUseAnim() {
//        return switch (this) {
//            case LIGHTNING_LANCE_SPELL -> UseAnim.SPEAR;
//            default -> UseAnim.BOW;
//        };
//    }
//
//    //private static final SpellType[] FIRE_SPELLS = {FIREBALL_SPELL, BURNING_DASH_SPELL, FIREBOLT_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL, BLAZE_STORM_SPELL};
//    //private static final SpellType[] ICE_SPELLS = {CONE_OF_COLD_SPELL, ICICLE_SPELL, FROST_STEP_SPELL, FROSTBITE_SPELL, SUMMON_POLAR_BEAR_SPELL, ICE_BLOCK_SPELL};
//    //private static final SpellType[] LIGHTNING_SPELLS = {ELECTROCUTE_SPELL, LIGHTNING_LANCE_SPELL, LIGHTNING_BOLT_SPELL, ASCENSION_SPELL, CHARGE_SPELL};
//    //private static final SpellType[] HOLY_SPELLS = {HEAL_SPELL, ANGEL_WING_SPELL, WISP_SPELL, GREATER_HEAL_SPELL, CLOUD_OF_REGENERATION_SPELL, BLESSING_OF_LIFE_SPELL, FORTIFY_SPELL};
//    //private static final SpellType[] ENDER_SPELLS = {TELEPORT_SPELL, MAGIC_MISSILE_SPELL, EVASION_SPELL, MAGIC_ARROW_SPELL, DRAGON_BREATH_SPELL, COUNTERSPELL_SPELL};
//    //private static final SpellType[] BLOOD_SPELLS = {BLOOD_SLASH_SPELL, HEARTSTOP_SPELL, RAISE_DEAD_SPELL, WITHER_SKULL_SPELL, RAY_OF_SIPHONING_SPELL, BLOOD_STEP_SPELL, BlOOD_NEEDLES_SPELL, ACUPUNCTURE_SPELL};
//    //private static final SpellType[] EVOCATION_SPELLS = {SUMMON_VEX_SPELL, FIRECRACKER_SPELL, SUMMON_HORSE_SPELL, SHIELD_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL, LOB_CREEPER_SPELL, CHAIN_CREEPER_SPELL, INVISIBILITY_SPELL, SPECTRAL_HAMMER_SPELL};
//    //private static final SpellType[] VOID_SPELLS = {ABYSSAL_SHROUD_SPELL, VOID_TENTACLES_SPELL, BLACK_HOLE_SPELL};
//    //private static final SpellType[] POISON_SPELLS = {POISON_BREATH_SPELL, POISON_ARROW_SPELL, POISON_SPLASH_SPELL, ACID_ORB_SPELL, SPIDER_ASPECT_SPELL, BLIGHT_SPELL, ROOT_SPELL};
//
//    public AbstractSpell getSpellForType(int level) {
//        return getSpellForType.get(level);
//    }
//
//    public static float getRarityMapped(float levelMin, float levelMax, float rarityMin, float rarityMax, float levelToMap) {
//        return rarityMin + ((levelToMap - levelMin) * (rarityMax - rarityMin)) / (levelMax - levelMin);
//    }
//
//    private void initializeRarityWeights() {
//        synchronized (SpellType.NONE_SPELL) {
//            if (rarityWeights == null) {
//                int minRarity = getMinRarity();
//                int maxRarity = getMaxRarity();
//                List<Double> rarityRawConfig = SpellRarity.getRawRarityConfig();
//                List<Double> rarityConfig = SpellRarity.getRarityConfig();
//                //IronsSpellbooks.LOGGER.debug("rarityRawConfig: {} rarityConfig:{}, {}, {}", rarityRawConfig.size(), rarityConfig.size(), this.hashCode(), this.name());
//
//                List<Double> rarityRawWeights;
//                if (minRarity != 0) {
//                    //Must balance remaining weights
//
//                    var subList = rarityRawConfig.subList(minRarity, maxRarity + 1);
//                    double subtotal = subList.stream().reduce(0d, Double::sum);
//                    rarityRawWeights = subList.stream().map(item -> ((item / subtotal) * (1 - subtotal)) + item).toList();
//
//                    var counter = new AtomicDouble();
//                    rarityWeights = new ArrayList<>();
//                    rarityRawWeights.forEach(item -> {
//                        rarityWeights.add(counter.addAndGet(item));
//                    });
//                } else {
//                    //rarityRawWeights = rarityRawConfig;
//                    rarityWeights = rarityConfig;
//                }
//            }
//        }
//    }
//
//    public SpellRarity getRarity(int level) {
//        if (rarityWeights == null) {
//            initializeRarityWeights();
//        }
//
//        int maxLevel = getMaxLevel();
//        int maxRarity = getMaxRarity();
//        if (maxLevel == 1)
//            return SpellRarity.values()[getMinRarity()];
//        double percentOfMaxLevel = (double) level / (double) maxLevel;
//
//        //irons_spellbooks.LOGGER.debug("getRarity: {} {} {} {} {} {}", this.toString(), rarityRawWeights, rarityWeights, percentOfMaxLevel, minRarity, maxRarity);
//
//        int lookupOffset = maxRarity + 1 - rarityWeights.size();
//
//        for (int i = 0; i < rarityWeights.size(); i++) {
//            if (percentOfMaxLevel <= rarityWeights.get(i)) {
//                return SpellRarity.values()[i + lookupOffset];
//            }
//        }
//
//        return SpellRarity.COMMON;
//    }
//
//    public int getMinLevelForRarity(SpellRarity rarity) {
//        if (rarityWeights == null) {
//            initializeRarityWeights();
//        }
//
//        int minRarity = getMinRarity();
//        int maxLevel = getMaxLevel();
//        if (rarity.getValue() < minRarity) {
//            return 0;
//        }
//
//        if (rarity.getValue() == minRarity) {
//            return 1;
//        }
//
//        //irons_spellbooks.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity);
//        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;
//
//
////        int lookupOffset = maxRarity + 1 - rarityWeights.size();
////        irons_spellbooks.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity, lookupOffset);
////        int index = rarity.getValue() - lookupOffset;
////
////        if (index < 0) {
////            return 1;
////        } else {
////            double rarityWeight = rarityWeights.get(index);
////            return (int) (maxLevel * rarityWeight);
////        }
//    }
//
//    public AbstractSpell getSpellForRarity(SpellRarity rarity) {
//        int level = getMinLevelForRarity(rarity);
//
//        if (level == 0) {
//            return AbstractSpell.getSpell(SpellType.NONE_SPELL, 0);
//        }
//
//        return getSpellForType(level);
//    }
//
//    private float lerp(float a, float b, float f) {
//        return a + f * (b - a);
//    }
//
//    public SchoolType getSchoolType() {
//        return ServerConfigs.getSpellConfig(this).school();
////        if (quickSearch(FIRE_SPELLS, this))
////            return SchoolType.FIRE;
////        if (quickSearch(ICE_SPELLS, this))
////            return SchoolType.ICE;
////        if (quickSearch(LIGHTNING_SPELLS, this))
////            return SchoolType.LIGHTNING;
////        if (quickSearch(HOLY_SPELLS, this))
////            return SchoolType.HOLY;
////        if (quickSearch(ENDER_SPELLS, this))
////            return SchoolType.ENDER;
////        if (quickSearch(BLOOD_SPELLS, this))
////            return SchoolType.BLOOD;
////        if (quickSearch(VOID_SPELLS, this))
////            return SchoolType.VOID;
////        if (quickSearch(POISON_SPELLS, this))
////            return SchoolType.POISON;
////        else
////            return SchoolType.EVOCATION;
//    }
//
//
//    public static List<SpellType> getSpellsFromSchool(SchoolType school) {
//        return Arrays.stream(SpellType.values()).filter(spellType -> spellType.getSchoolType() == school && spellType.isEnabled() && spellType != NONE_SPELL).toList();
////        return spellsForSchool.getOrDefault(school, List.of(NONE_SPELL));
//    }
//
//    private static final HashMap<SchoolType, List<SpellType>> spellsForSchool = new HashMap<>();
//
//    public static void assignSpellToSchool(SpellType spellType, SchoolType schoolType) {
//        var spells = spellsForSchool.getOrDefault(schoolType, new ArrayList<>());
//        spells.add(spellType);
//        spellsForSchool.put(schoolType, spells);
//    }
//
//    public String getComponentId() {
//        return String.format("spell.%s.%s", IronsSpellbooks.MODID, getId());
//    }
//
//    public MutableComponent getDisplayName() {
//        return Component.translatable(getComponentId());
//    }
//
////    public ResourceLocation getResourceLocation() {
////        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
////    }
//
//    public DamageSource getDamageSource() {
//        return new DamageSource(this.getId() + "_spell");
//    }
//
//    public DamageSource getDamageSource(Entity attacker) {
//        return DamageSources.directDamageSource(getDamageSource(), attacker);
//    }
//
//    public DamageSource getDamageSource(Entity projectile, Entity attacker) {
//        return DamageSources.indirectDamageSource(getDamageSource(), projectile, attacker);
//    }
//
//    public boolean isEnabled() {
//        return ServerConfigs.getSpellConfig(this).enabled();
//    }
//
//    public String getId() {
//        return this.toString().toLowerCase().replace("_spell", "");
//    }
//
//    private boolean quickSearch(SpellType[] array, SpellType query) {
//        for (SpellType spellType : array)
//            if (spellType.equals(query))
//                return true;
//        return false;
//    }
//
//    private interface GetSpellForType {
//        AbstractSpell get(int level);
//    }
//
//}