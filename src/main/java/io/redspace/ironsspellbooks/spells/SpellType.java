package io.redspace.ironsspellbooks.spells;

import com.google.common.util.concurrent.AtomicDouble;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.spells.blood.*;
import io.redspace.ironsspellbooks.spells.ender.*;
import io.redspace.ironsspellbooks.spells.evocation.*;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.ironsspellbooks.spells.holy.*;
import io.redspace.ironsspellbooks.spells.ice.*;
import io.redspace.ironsspellbooks.spells.lightning.*;
import io.redspace.ironsspellbooks.spells.void_school.AbyssalShroudSpell;
import io.redspace.ironsspellbooks.spells.void_school.VoidTentaclesSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;

public enum SpellType {
    /**
     * When adding spell, add:
     * Spell Type
     * SpellType.getCastType
     * SpellType.getSchoolType
     * SpellType.getSpellForType
     * Translation
     * Icon
     */
    NONE_SPELL(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TELEPORT_SPELL(3),
    MAGIC_MISSILE_SPELL(4),
    ELECTROCUTE_SPELL(5),
    CONE_OF_COLD_SPELL(6),
    HEAL_SPELL(7),
    BLOOD_SLASH_SPELL(8),
    SUMMON_VEX_SPELL(9),
    FIREBOLT_SPELL(10),
    FIRE_BREATH_SPELL(11),
    ICICLE_SPELL(12),
    FIRECRACKER_SPELL(13),
    SUMMON_HORSE_SPELL(14),
    ANGEL_WING_SPELL(15),
    SHIELD_SPELL(16),
    WALL_OF_FIRE_SPELL(17),
    WISP_SPELL(18),
    FANG_STRIKE_SPELL(19),
    FANG_WARD_SPELL(20),
    EVASION_SPELL(21),
    HEARTSTOP_SPELL(22),
    LIGHTNING_LANCE_SPELL(23),
    LIGHTNING_BOLT_SPELL(24),
    RAISE_DEAD_SPELL(25),
    WITHER_SKULL_SPELL(26),
    GREATER_HEAL_SPELL(27),
    CLOUD_OF_REGENERATION_SPELL(28),
    RAY_OF_SIPHONING_SPELL(29),
    MAGIC_ARROW_SPELL(30),
    LOB_CREEPER_SPELL(31),
    CHAIN_CREEPER_SPELL(32),
    BLAZE_STORM_SPELL(33),
    FROST_STEP(34),
    ABYSSAL_SHROUD_SPELL(35),
    FROSTBITE_SPELL(36),
    ASCENSION_SPELL(37),
    INVISIBILITY_SPELL(38),
    BLOOD_STEP_SPELL(39),
    SUMMON_POLAR_BEAR_SPELL(40),
    BLESSING_OF_LIFE_SPELL(41),
    DRAGON_BREATH_SPELL(42),
    FORTIFY_SPELL(43),
    COUNTERSPELL_SPELL(44),
    SPECTRAL_HAMMER_SPELL(45),
    CHARGE_SPELL(46),
    VOID_TENTACLES_SPELL(47),
    ICE_BLOCK_SPELL(48)
    ;

    private final int value;
    private final LazyOptional<Boolean> isEnabled;
    private final LazyOptional<Integer> maxLevel;
    private final LazyOptional<Integer> minRarity;
    private final int maxRarity;

    private List<Double> rarityWeights;

    SpellType(final int newValue) {
        value = newValue;
        isEnabled = LazyOptional.of(() -> (ServerConfigs.getSpellConfig(this).ENABLED));
        maxLevel = LazyOptional.of(() -> (ServerConfigs.getSpellConfig(this).MAX_LEVEL));
        minRarity = LazyOptional.of(() -> (ServerConfigs.getSpellConfig(this).MIN_RARITY.getValue()));
        maxRarity = SpellRarity.LEGENDARY.getValue();
    }

    public int getValue() {
        return value;
    }

    public int getMinRarity() {
        return minRarity.orElse(0);
    }

    public int getMaxRarity() {
        return maxRarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return maxLevel.orElse(10);
    }

    public static SpellType getTypeFromValue(int value) {
        return SpellType.values()[value];
    }

    public CastType getCastType() {
        return switch (this) {
            case FIREBALL_SPELL, WISP_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL, SUMMON_VEX_SPELL, RAISE_DEAD_SPELL, GREATER_HEAL_SPELL, CHAIN_CREEPER_SPELL, INVISIBILITY_SPELL, SUMMON_POLAR_BEAR_SPELL, BLESSING_OF_LIFE_SPELL, FORTIFY_SPELL, VOID_TENTACLES_SPELL, SUMMON_HORSE_SPELL, ICE_BLOCK_SPELL ->
                    CastType.LONG;
            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL, CLOUD_OF_REGENERATION_SPELL, RAY_OF_SIPHONING_SPELL, BLAZE_STORM_SPELL, DRAGON_BREATH_SPELL ->
                    CastType.CONTINUOUS;
            case LIGHTNING_LANCE_SPELL, MAGIC_ARROW_SPELL -> CastType.CHARGE;
            default -> CastType.INSTANT;
        };
    }

    public UseAnim getUseAnim() {
        return switch (this) {
            case LIGHTNING_LANCE_SPELL -> UseAnim.SPEAR;
            default -> UseAnim.BOW;
        };
    }

    private static final SpellType[] FIRE_SPELLS = {FIREBALL_SPELL, BURNING_DASH_SPELL, FIREBOLT_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL, BLAZE_STORM_SPELL};
    private static final SpellType[] ICE_SPELLS = {CONE_OF_COLD_SPELL, ICICLE_SPELL, FROST_STEP, FROSTBITE_SPELL, SUMMON_POLAR_BEAR_SPELL, ICE_BLOCK_SPELL};
    private static final SpellType[] LIGHTNING_SPELLS = {ELECTROCUTE_SPELL, LIGHTNING_LANCE_SPELL, LIGHTNING_BOLT_SPELL, ASCENSION_SPELL, CHARGE_SPELL};
    private static final SpellType[] HOLY_SPELLS = {HEAL_SPELL, ANGEL_WING_SPELL, WISP_SPELL, GREATER_HEAL_SPELL, CLOUD_OF_REGENERATION_SPELL, BLESSING_OF_LIFE_SPELL, FORTIFY_SPELL};
    private static final SpellType[] ENDER_SPELLS = {TELEPORT_SPELL, MAGIC_MISSILE_SPELL, EVASION_SPELL, MAGIC_ARROW_SPELL, DRAGON_BREATH_SPELL, COUNTERSPELL_SPELL};
    private static final SpellType[] BLOOD_SPELLS = {BLOOD_SLASH_SPELL, HEARTSTOP_SPELL, RAISE_DEAD_SPELL, WITHER_SKULL_SPELL, RAY_OF_SIPHONING_SPELL, BLOOD_STEP_SPELL};
    private static final SpellType[] EVOCATION_SPELLS = {SUMMON_VEX_SPELL, FIRECRACKER_SPELL, SUMMON_HORSE_SPELL, SHIELD_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL, LOB_CREEPER_SPELL, CHAIN_CREEPER_SPELL, INVISIBILITY_SPELL};
    private static final SpellType[] VOID_SPELLS = {ABYSSAL_SHROUD_SPELL, VOID_TENTACLES_SPELL};

    public AbstractSpell getSpellForType(int level) {
        switch (this) {
            case BURNING_DASH_SPELL -> {
                return new BurningDashSpell(level);
            }
            case FIREBALL_SPELL -> {
                return new FireballSpell(level);
            }
            case TELEPORT_SPELL -> {
                return new TeleportSpell(level);
            }
            case MAGIC_MISSILE_SPELL -> {
                return new MagicMissileSpell(level);
            }
            case CONE_OF_COLD_SPELL -> {
                return new ConeOfColdSpell(level);
            }
            case ELECTROCUTE_SPELL -> {
                return new ElectrocuteSpell(level);
            }
            case HEAL_SPELL -> {
                return new HealSpell(level);
            }
            case BLOOD_SLASH_SPELL -> {
                return new BloodSlashSpell(level);
            }
            case SUMMON_VEX_SPELL -> {
                return new SummonVexSpell(level);
            }
            case FIREBOLT_SPELL -> {
                return new FireboltSpell(level);
            }
            case FIRE_BREATH_SPELL -> {
                return new FireBreathSpell(level);
            }
            case ICICLE_SPELL -> {
                return new IcicleSpell(level);
            }
            case FIRECRACKER_SPELL -> {
                return new FirecrackerSpell(level);
            }
            case SUMMON_HORSE_SPELL -> {
                return new SummonHorseSpell(level);
            }
            case ANGEL_WING_SPELL -> {
                return new AngelWingsSpell(level);
            }
            case SHIELD_SPELL -> {
                return new ShieldSpell(level);
            }
            case WALL_OF_FIRE_SPELL -> {
                return new WallOfFireSpell(level);
            }
            case WISP_SPELL -> {
                return new WispSpell(level);
            }
            case FANG_STRIKE_SPELL -> {
                return new FangStrikeSpell(level);
            }
            case FANG_WARD_SPELL -> {
                return new FangWardSpell(level);
            }
            case EVASION_SPELL -> {
                return new EvasionSpell(level);
            }
            case HEARTSTOP_SPELL -> {
                return new HeartstopSpell(level);
            }
            case LIGHTNING_LANCE_SPELL -> {
                return new LightningLanceSpell(level);
            }
            case LIGHTNING_BOLT_SPELL -> {
                return new LightningBoltSpell(level);
            }
            case RAISE_DEAD_SPELL -> {
                return new RaiseDeadSpell(level);
            }
            case WITHER_SKULL_SPELL -> {
                return new WitherSkullSpell(level);
            }
            case GREATER_HEAL_SPELL -> {
                return new GreaterHealSpell(level);
            }
            case CLOUD_OF_REGENERATION_SPELL -> {
                return new CloudOfRegenerationSpell(level);
            }
            case RAY_OF_SIPHONING_SPELL -> {
                return new RayOfSiphoningSpell(level);
            }
            case MAGIC_ARROW_SPELL -> {
                return new MagicArrowSpell(level);
            }
            case LOB_CREEPER_SPELL -> {
                return new LobCreeperSpell(level);
            }
            case CHAIN_CREEPER_SPELL -> {
                return new ChainCreeperSpell(level);
            }
            case BLAZE_STORM_SPELL -> {
                return new BlazeStormSpell(level);
            }
            case FROST_STEP -> {
                return new FrostStepSpell(level);
            }
            case ABYSSAL_SHROUD_SPELL -> {
                return new AbyssalShroudSpell(level);
            }
            case FROSTBITE_SPELL -> {
                return new FrostbiteSpell(level);
            }
            case ASCENSION_SPELL -> {
                return new AscensionSpell(level);
            }
            case INVISIBILITY_SPELL -> {
                return new InvisibilitySpell(level);
            }
            case BLOOD_STEP_SPELL -> {
                return new BloodStepSpell(level);
            }
            case SUMMON_POLAR_BEAR_SPELL -> {
                return new SummonPolarBearSpell(level);
            }
            case BLESSING_OF_LIFE_SPELL -> {
                return new BlessingOfLifeSpell(level);
            }
            case DRAGON_BREATH_SPELL -> {
                return new DragonBreathSpell(level);
            }
            case FORTIFY_SPELL -> {
                return new FortifySpell(level);
            }
            case COUNTERSPELL_SPELL -> {
                return new CounterspellSpell(level);
            }
            case SPECTRAL_HAMMER_SPELL -> {
                return new SpectralHammerSpell(level);
            }
            case CHARGE_SPELL -> {
                return new ChargeSpell(level);
            }
            case VOID_TENTACLES_SPELL -> {
                return new VoidTentaclesSpell(level);
            }
            case ICE_BLOCK_SPELL -> {
                return new IceBlockSpell(level);
            }
            default -> {
                return new NoneSpell(0);
            }
        }
    }
    //    public SpellRarity getRarity(int level) {
//        //float adjustedRarity = getRarityMapped(minLevel, maxLevel, minRarity, maxRarity, level);
//        int maxLevel = this.maxLevel.resolve().get();
//        int minRarity = this.minRarity.resolve().get();
//        //https://www.desmos.com/calculator/fumipfwdfr
//        float rarityPercent = level / (float) maxLevel;
//        float scaledRarity = Mth.clamp(lerp(minRarity, maxRarity, rarityPercent * rarityPercent), minRarity, maxRarity);
//        return SpellRarity.values()[(int) scaledRarity];
//        //return SpellRarity.getRarityFromPercent(adjustedRarity);
//    }

    public static float getRarityMapped(float levelMin, float levelMax, float rarityMin, float rarityMax, float levelToMap) {
        return rarityMin + ((levelToMap - levelMin) * (rarityMax - rarityMin)) / (levelMax - levelMin);
    }

    private void initializeRarityWeights() {
        int minRarity = getMinRarity();
        int maxRarity = getMaxRarity();
        List<Double> rarityRawConfig = SpellRarity.getRawRarityConfig();
        List<Double> rarityConfig = SpellRarity.getRarityConfig();

        List<Double> rarityRawWeights;
        if (minRarity != 0) {
            //Must balance remaining weights

            var subList = rarityRawConfig.subList(minRarity, maxRarity + 1);
            double subtotal = subList.stream().reduce(0d, Double::sum);
            rarityRawWeights = subList.stream().map(item -> ((item / subtotal) * (1 - subtotal)) + item).toList();

            var counter = new AtomicDouble();
            rarityWeights = new ArrayList<>();
            rarityRawWeights.forEach(item -> {
                rarityWeights.add(counter.addAndGet(item));
            });
        } else {
            //rarityRawWeights = rarityRawConfig;
            rarityWeights = rarityConfig;
        }
    }

    public SpellRarity getRarity(int level) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int maxLevel = getMaxLevel();
        int maxRarity = getMaxRarity();
        if (maxLevel == 1)
            return SpellRarity.values()[getMinRarity()];
        double percentOfMaxLevel = (double) level / (double) maxLevel;

        //irons_spellbooks.LOGGER.debug("getRarity: {} {} {} {} {} {}", this.toString(), rarityRawWeights, rarityWeights, percentOfMaxLevel, minRarity, maxRarity);

        int lookupOffset = maxRarity + 1 - rarityWeights.size();

        for (int i = 0; i < rarityWeights.size(); i++) {
            if (percentOfMaxLevel <= rarityWeights.get(i)) {
                return SpellRarity.values()[i + lookupOffset];
            }
        }

        return SpellRarity.COMMON;
    }

    public int getMinLevelForRarity(SpellRarity rarity) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int minRarity = getMinRarity();
        int maxLevel = getMaxLevel();
        if (rarity.getValue() < minRarity) {
            return 0;
        }

        if (rarity.getValue() == minRarity) {
            return 1;
        }

        //irons_spellbooks.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity);
        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;


//        int lookupOffset = maxRarity + 1 - rarityWeights.size();
//        irons_spellbooks.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity, lookupOffset);
//        int index = rarity.getValue() - lookupOffset;
//
//        if (index < 0) {
//            return 1;
//        } else {
//            double rarityWeight = rarityWeights.get(index);
//            return (int) (maxLevel * rarityWeight);
//        }
    }

    public AbstractSpell getSpellForRarity(SpellRarity rarity) {
        int level = getMinLevelForRarity(rarity);

        if (level == 0) {
            return AbstractSpell.getSpell(SpellType.NONE_SPELL, 0);
        }

        return getSpellForType(level);
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public SchoolType getSchoolType() {
        if (quickSearch(FIRE_SPELLS, this))
            return SchoolType.FIRE;
        if (quickSearch(ICE_SPELLS, this))
            return SchoolType.ICE;
        if (quickSearch(LIGHTNING_SPELLS, this))
            return SchoolType.LIGHTNING;
        if (quickSearch(HOLY_SPELLS, this))
            return SchoolType.HOLY;
        if (quickSearch(ENDER_SPELLS, this))
            return SchoolType.ENDER;
        if (quickSearch(BLOOD_SPELLS, this))
            return SchoolType.BLOOD;
        if (quickSearch(VOID_SPELLS, this))
            return SchoolType.VOID;
        else
            return SchoolType.EVOCATION;
    }


    public static SpellType[] getSpellsFromSchool(SchoolType school) {
        if (school.equals(SchoolType.FIRE))
            return FIRE_SPELLS;
        else if (school.equals(SchoolType.ICE))
            return ICE_SPELLS;
        else if (school.equals(SchoolType.LIGHTNING))
            return LIGHTNING_SPELLS;
        else if (school.equals(SchoolType.HOLY))
            return HOLY_SPELLS;
        else if (school.equals(SchoolType.ENDER))
            return ENDER_SPELLS;
        else if (school.equals(SchoolType.BLOOD))
            return BLOOD_SPELLS;
        else if (school.equals(SchoolType.EVOCATION))
            return EVOCATION_SPELLS;
        else if (school.equals(SchoolType.VOID))
            return VOID_SPELLS;
        else
            return new SpellType[]{SpellType.NONE_SPELL};
    }

    public String getComponentId() {
        return String.format("spell.%s.%s", IronsSpellbooks.MODID, getId());
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(getComponentId());
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
    }

    public DamageSource getDamageSource() {
        return new DamageSource(this.getId() + "_spell");
    }

    public DamageSource getDamageSource(Entity attacker) {
        return DamageSources.directDamageSource(getDamageSource(), attacker);
    }

    public DamageSource getDamageSource(Entity projectile, Entity attacker) {
        return DamageSources.indirectDamageSource(getDamageSource(), projectile, attacker);
    }

    public boolean isEnabled() {
        return isEnabled.orElse(false);
    }

    public String getId() {
        return this.toString().toLowerCase().replace("_spell", "");
    }

    private boolean quickSearch(SpellType[] array, SpellType query) {
        for (SpellType spellType : array)
            if (spellType.equals(query))
                return true;
        return false;
    }
}