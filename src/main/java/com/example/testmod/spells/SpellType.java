package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.config.ServerConfigs;
import com.example.testmod.spells.blood.BloodSlashSpell;
import com.example.testmod.spells.blood.HeartstopSpell;
import com.example.testmod.spells.ender.EvasionSpell;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.evocation.*;
import com.example.testmod.spells.fire.*;
import com.example.testmod.spells.holy.AngelWingsSpell;
import com.example.testmod.spells.holy.HealSpell;
import com.example.testmod.spells.holy.WispSpell;
import com.example.testmod.spells.ice.ConeOfColdSpell;
import com.example.testmod.spells.ice.IcicleSpell;
import com.example.testmod.spells.lightning.ElectrocuteSpell;
import com.example.testmod.spells.lightning.LightningBoltSpell;
import com.example.testmod.spells.lightning.LightningLanceSpell;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
    LIGHTNING_BOLT_SPELL(24);

    private final int value;
    private final LazyOptional<Integer> maxLevel;
    private final LazyOptional<Integer> minRarity;
    private final int maxRarity;

    private List<Double> rarityWeights;

    SpellType(final int newValue) {
        value = newValue;
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
            case FIREBALL_SPELL, WISP_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL, SUMMON_VEX_SPELL -> CastType.LONG;
            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL -> CastType.CONTINUOUS;
            case LIGHTNING_LANCE_SPELL -> CastType.CHARGE;
            default -> CastType.INSTANT;
        };
    }

    public UseAnim getUseAnim(){
        return switch (this){
            case LIGHTNING_LANCE_SPELL -> UseAnim.SPEAR;
            default -> UseAnim.BOW;
        };
    }

    private static final SpellType[] FIRE_SPELLS = {FIREBALL_SPELL, BURNING_DASH_SPELL, FIREBOLT_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL};
    private static final SpellType[] ICE_SPELLS = {CONE_OF_COLD_SPELL, ICICLE_SPELL};
    private static final SpellType[] LIGHTNING_SPELLS = {ELECTROCUTE_SPELL, LIGHTNING_LANCE_SPELL, LIGHTNING_BOLT_SPELL};
    private static final SpellType[] HOLY_SPELLS = {HEAL_SPELL, ANGEL_WING_SPELL, WISP_SPELL};
    private static final SpellType[] ENDER_SPELLS = {TELEPORT_SPELL, MAGIC_MISSILE_SPELL, EVASION_SPELL};
    private static final SpellType[] BLOOD_SPELLS = {BLOOD_SLASH_SPELL, HEARTSTOP_SPELL};
    private static final SpellType[] EVOCATION_SPELLS = {SUMMON_VEX_SPELL, FIRECRACKER_SPELL, SUMMON_HORSE_SPELL, SHIELD_SPELL, FANG_STRIKE_SPELL, FANG_WARD_SPELL};

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
        double percentOfMaxLevel = (double) level / (double) maxLevel;

        //TestMod.LOGGER.debug("getRarity: {} {} {} {} {} {}", this.toString(), rarityRawWeights, rarityWeights, percentOfMaxLevel, minRarity, maxRarity);

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

        //TestMod.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity);
        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;


//        int lookupOffset = maxRarity + 1 - rarityWeights.size();
//        TestMod.LOGGER.debug("getMinLevelForRarity: {} {} {} {} {} {} {} {}", this.toString(), rarity, rarityRawWeights, rarityWeights, maxLevel, minRarity, maxRarity, lookupOffset);
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
        //if (quickSearch(EVOCATION_SPELLS, this))
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
        //else if (school.equals(SchoolType.EVOCATION))
        return EVOCATION_SPELLS;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("spell." + TestMod.MODID + "." + this.getId());
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(TestMod.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
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